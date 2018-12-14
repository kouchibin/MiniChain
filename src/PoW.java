import java.util.Collections;

public class PoW {
	
	public static String getHashPrefix(int difficulty) {
		String prefix = String.join("", Collections.nCopies(difficulty, "0"));
		return prefix;
	}
	
	public static Block mineBlock(Block block, int difficulty) {
		long nonce = 0;
		String prefix = getHashPrefix(difficulty);
		String baseContent = block.getBaseContent();
		String hash;
		do {
			nonce++;
			hash = StringUtil.getHash(baseContent + Long.toString(nonce));	
		} while (!hash.startsWith(prefix));
		block.setNonce(nonce);
		block.setHash(hash);
		return block;
	}
	
	public static boolean verifyBlock(Block block, int difficulty) {
		String prefix = getHashPrefix(difficulty);
		String hash = StringUtil.getHash(block.getBaseContent() + block.getNonce());
		if (hash.startsWith(prefix) && hash.equals(block.getHash()))
			return true;
		else 
			return false;
	}
	
	public static boolean verifyChain(MiniChain chain) {
		return false;
	}
}
