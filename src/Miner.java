
public class Miner extends Thread{
	
	private final MiniChain chain;
	
	public Miner(MiniChain chain) {
		this.chain = chain;
	}
	
	private String getTransactions() {
		return "Send 1 million to KouChibin";
	}
	
	public void run() {
		while (true) {
			String transaction = getTransactions();
			String previousHash = chain.getHighestBlock().getHash();
			Block block = new Block(transaction, previousHash);
			Block minedBlock = PoW.mineBlock(block, chain.getDifficulty()); 
			System.out.println("Mined a new block:");
			System.out.println(minedBlock);
			chain.submit(minedBlock);
		}
		
	}
}
