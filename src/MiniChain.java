import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
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
    
    public boolean emptyChain() {
        return latestBlock == null;
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
