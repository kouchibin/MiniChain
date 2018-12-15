import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.iq80.leveldb.DB;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import org.iq80.leveldb.Options;

public class MiniChain {
	private int difficulty = 4; 	// Mining difficulty - The number of preceding 0 of the hash.
	private DB database;
	private Options options;
	private byte[] latestBlock;		// Key of the latest block in database
	
	public MiniChain() throws IOException {
		options = new Options();
		options.createIfMissing(true);
		database = factory.open(new File("chain-data"), options);
		
		// Check if this is a new chain by checking if the last block exist
		latestBlock = database.get(bytes("l"));
		if (latestBlock == null) {
			// This is a new chain.
			// Add genesis block
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
	
	
	/* Miners use this method to submit the block they mined. */
	public void submit(Block block) {
		// Check the validity of the block before adding to the chain
		String previousHash = getLatesBlockHash();
		if (block.getPreviousBlockHash().equals(previousHash) && 
				PoW.verifyBlock(block, difficulty))
			addBlock(block);
		else
			System.out.println("Illegal block rejected.");
	}
	
	public String toString() {
		return null;
	}
	
	/* Test */
	public static void main(String[] args) throws IOException, InterruptedException {
		MiniChain chain = new MiniChain();
		//for (int i = 0; i < 4; i++)
		Thread t = new Miner(chain);
		t.start();
		t.join();
	}
}
