import java.util.ArrayList;
public class MiniChain {
	private ArrayList<Block> chain;
	
	
	public MiniChain() {
		// Create genesis block
		chain = new ArrayList<>();
		chain.add(new Block("Genesis Block. Created by Kou Chibin.", ""));
	}
	
	public void addBlock(String data) {
		String previousHash = chain.get(chain.size() - 1).getHash();
		chain.add(new Block(data, previousHash));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Block block : chain)
			sb.append(block.toString() + "\n");
		return sb.toString();
	}
	
	public static void main(String[] args) {
		MiniChain chain = new MiniChain();
		for (int i = 0; i < 10; i++)
			chain.addBlock("block " + i);
		System.out.println(chain);
	}
}
