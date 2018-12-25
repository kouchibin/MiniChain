import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.iq80.leveldb.DB;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import org.iq80.leveldb.Options;

public class MiniChain implements Iterable<Block> {
    private int difficulty = 4; // Mining difficulty - The number of preceding 0 of the hash.
    private DB database;
    private Options options;
    private byte[] latestBlock; // Key of the latest block in database
    private Queue<Transaction> transactions;

    public MiniChain() throws IOException {
        options = new Options();
        options.createIfMissing(true);
        database = factory.open(new File("chain-data"), options);
        transactions = new ConcurrentLinkedQueue<>();

        // Check if there exists a blockchain in database 
        latestBlock = database.get(bytes("l"));
        if (latestBlock == null) {
            System.out.println("Currently there is no blockchain.\n"
                    + "Use <createblockchain -addr \"address\"> to create a new blockchain.");
        }
    }
    
    public void createBlockchain(String creator) {
        Transaction coinbase = Transaction.newCoinbaseTX(creator, "");
        Block genesis = new Block(new Transaction[] {coinbase}, "");
        latestBlock = bytes(genesis.getHash());
        database.put(latestBlock, genesis.serialize());
        database.put(bytes("l"), latestBlock);
    }
    
    /**
     * Test if the database stores a blockchain.
     * @return True if database is empty. False otherwise.
     */
    public boolean emptyChain() {
        return latestBlock == null;
    }
    
    
    /**
     * Add a sending coins transaction to the blockchain.
     * @param from The source account. 
     * @param to The destination account.
     * @param amount The amount will be sent in the transaction.
     */
    public void sendTransaction(String from, String to, int amount) {
        Transaction transaction = newUTXOTransaction(from, to, amount);
        if (transaction == null) {
            System.out.println("Not enough funds.");
            return;
        }
        
        // Add the transaction to the queue to be mined.
        newTransaction(transaction);
    }
    
    
    /**
     * Construct a sending coins transaction. 
     * @param from The source account. 
     * @param to The destination account.
     * @param amount The amount will be sent in the transaction.
     * @return The constructed transaction. 
     *         Or null if source account doesn't have enough coins.
     */
    public Transaction newUTXOTransaction(String from, String to, int amount) {
        ArrayList<TXInput> inputs = new ArrayList<>();
        ArrayList<TXOutput> outputs = new ArrayList<>();
        int sum = 0;
        
        List<AvailableOutput> availableOutputs = findAvailableOutputs(from);
        for (AvailableOutput out : availableOutputs) {
            if (sum >= amount)
                break;
            sum += out.output.value;
            TXInput in = new TXInput(out.transaction.getID(), out.vOut, from);
            inputs.add(in);
        }
        
        
        // Not enough coins in the source account.
        if (sum < amount) 
            return null;
        
        outputs.add(new TXOutput(amount, to));
        if (amount < sum) {
            // Send a change to self.
            outputs.add(new TXOutput(sum-amount, from));
        }
        
        Transaction transaction = new Transaction(inputs.toArray(new TXInput[0]), 
                outputs.toArray(new TXOutput[0]));
        return transaction;
    }
    
    public List<AvailableOutput> findAvailableOutputs(String address) {
        ArrayList<AvailableOutput> availableOutputs = new ArrayList<>();
        Map<String, Set<Integer>> spentTXOs = new HashMap<>();
        for (Block block : this) {
            for (Transaction tx : block.getTransactions()) {
                String txID = tx.getID();
                Set<Integer> spentIdx = spentTXOs.get(txID);
                TXOutput[] outputs = tx.getOutputs();
                              
                for (int i = 0; i < outputs.length; i++) {
                    TXOutput output = outputs[i];
                    
                    // This output was already spent. Skip it.
                    if (spentIdx != null && spentIdx.contains(i)) 
                        continue;

                    if (output.canBeUnlockedWith(address)) {
                        availableOutputs.add(new AvailableOutput(output, tx, i));
                    }
                }
                
                // Add spent transaction outputs to spentTXOs
                if (!tx.isCoinbase()) {
                    for (TXInput input : tx.getInputs()) {
                        if (spentIdx == null)
                            spentIdx = new HashSet<>();
                        spentIdx.add(input.vOut);
                        spentTXOs.put(input.txId, spentIdx);
                    }
                }
            }
        }
        
        return availableOutputs;
    }
    
    public int getBalance(String address) {
        List<AvailableOutput> availableOutputs = findAvailableOutputs(address);
        int balance = 0;
        for (AvailableOutput out : availableOutputs) {
            balance += out.output.value;
        }
        return balance;
    }
    
    private void addBlock(Block block) {
        latestBlock = bytes(block.getHash());
        database.put(latestBlock, block.serialize());
        database.put(bytes("l"), latestBlock);
    }

    public String getLatesBlockHash() {
        return asString(latestBlock);
    }

    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Add a new transaction to the FIFO queue waiting to be processed by miners.
     * 
     * @param transaction
     */
    public void newTransaction(Transaction transaction) {
        transactions.offer(transaction);
    }

    /**
     * Return unprocessed new transactions to be processed.
     * 
     * @return
     */
    public Transaction[] getNewTransactions() {
        Transaction tx = transactions.peek();
        if (tx == null)
            return null;
        return new Transaction[] {tx};
    }

    /* Miners use this method to submit the block they mined. */
    public void submit(Block block) {
        // Check the validity of the block before adding to the chain
        String previousHash = getLatesBlockHash();
        if (block.getPreviousBlockHash().equals(previousHash) && PoW.verifyBlock(block, difficulty)) {

            // Add the mined block to the chain
            addBlock(block);

            // Remove the mined transaction from the queue
            transactions.poll();
        } else
            System.out.println("Illegal block rejected.");
    }

    public void close() throws IOException {
        database.close();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Block block : this) {
            sb.append(block);
        }
        return sb.toString();
    }

    @Override
    public Iterator<Block> iterator() {
        return new BlockchainIterator();
    }

    public class BlockchainIterator implements Iterator<Block> {

        private Block currentBlock;

        public BlockchainIterator() {
            currentBlock = Block.deserialize(database.get(latestBlock));
        }

        @Override
        public boolean hasNext() {
            if (currentBlock == null)
                return false;
            return true;
        }

        @Override
        public Block next() {
            Block temp = currentBlock;
            byte[] nextBlockBytes = database.get(bytes(temp.getPreviousBlockHash()));
            if (nextBlockBytes == null)
                currentBlock = null;
            else
                currentBlock = Block.deserialize(nextBlockBytes);
            return temp;
        }

    }

    /* Test */
    public static void main(String[] args) throws IOException, InterruptedException {
        MiniChain chain = null;
        try {
            chain = new MiniChain();
            // for (int i = 0; i < 4; i++)
            Thread t = new Miner(chain);
            t.start();
            Thread cli = new CLI(chain);
            cli.start();
            //Transaction tx = Transaction.newCoinbaseTX("second test", "something");
            //chain.newTransaction(tx);
            t.join();
            cli.join();
            
        } finally {
            chain.close();
        }

    }
}

