import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.iq80.leveldb.DB;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import org.iq80.leveldb.Options;

public class MiniChain implements Iterable<Block> {
	private int difficulty = 4; 	// Mining difficulty - The number of preceding 0 of the hash.
	private DB database;
	private Options options;
	private byte[] latestBlock;		// Key of the latest block in database
	private Queue<String> transactions;
	
	public MiniChain() throws IOException {
		options = new Options();
		options.createIfMissing(true);
		database = factory.open(new File("chain-data"), options);
		transactions = new ConcurrentLinkedQueue<String>();
		
		// Check if this is a new chain by checking if the last block exist
		latestBlock = database.get(bytes("l"));
		if (latestBlock == null) {
			// This is a new chain.
			// Add genesis block
			System.out.println("New blockchain.");
			Block genesis = new Block("Genesis Block. Created by Kou Chibin.", "");
			latestBlock = bytes(genesis.getHash());
			database.put(latestBlock, genesis.serialize());
			database.put(bytes("l"), latestBlock);
		} 
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
	 * @param transaction 
	 */
	public void newTransaction(String transaction) {
		transactions.offer(transaction);
	}
	
	/**
	 * Return the next transaction to be processed.
	 * @return
	 */
	public String getNextTransaction() {
		return transactions.peek();
	}
	
	/* Miners use this method to submit the block they mined. */
	public void submit(Block block) {
		// Check the validity of the block before adding to the chain
		String previousHash = getLatesBlockHash();
		if (block.getPreviousBlockHash().equals(previousHash) && 
				PoW.verifyBlock(block, difficulty)) {
			
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
			//for (int i = 0; i < 4; i++)
			Thread t = new Miner(chain);
			t.start();
			Thread cli = new CLI(chain);
			cli.start();
			
			t.join();
			cli.join();
		} finally {
			chain.close();
		}
		
	}
}
