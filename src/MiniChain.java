import java.util.ArrayList;

public class MiniChain {
	private ArrayList<Block> chain;
	private int difficulty = 8; 	// Mining difficulty - The number of preceding 0 of the hash.
	
	
	public MiniChain() {
		// Create genesis block
		chain = new ArrayList<>();
		chain.add(new Block("Genesis Block. Created by Kou Chibin.", ""));
	}
	
	public void addBlock(String data) {
		String previousHash = chain.get(chain.size() - 1).getHash();
		chain.add(new Block(data, previousHash));
	}
	
	public Block getHighestBlock() {
		return chain.get(chain.size() - 1);
	}
	
	public int getDifficulty() {
		return difficulty;
	}
	
	
	/* Miners use this method to submit the block they mined. */
	public void submit(Block block) {
		// Check the validity of the block before adding to the chain
		String previousHash = getHighestBlock().getHash();
		if (block.getPreviousBlockHash().equals(previousHash) && 
				PoW.verifyBlock(block, difficulty))
			chain.add(block);
		else
			System.out.println("Illegal block rejected.");
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Block block : chain)
			sb.append(block.toString() + "\n");
		return sb.toString();
	}
	
	/* Test */
	public static void main(String[] args) {
		MiniChain chain = new MiniChain();
		//for (int i = 0; i < 4; i++)
		Thread t = new Miner(chain);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
