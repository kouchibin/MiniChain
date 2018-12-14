import java.util.Date;
import com.google.gson.GsonBuilder;

public class Block {
	private long timeStamp;				// When this block is mined
	private String data;				// Actual data in this block
	private String previousBlockHash;	// Hash value of previous block
	private String hash;				// Hash value of this block
	private long nonce; 				// For PoW
	
	public Block(String data, String previousBlockHash) {
		this.data = data;
		this.previousBlockHash = previousBlockHash;
		this.timeStamp = new Date().getTime();
		this.hash = StringUtil.getHash(previousBlockHash +
									Long.toString(timeStamp) + 
									data);
	}
	
	public String calculateHash() {
		String hash = StringUtil.getHash(
				previousBlockHash +
				Long.toString(timeStamp) + 
				data
				);
		return hash;
	}
	
	public String getData() {
		return data;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public String getPreviousBlockHash() {
		return previousBlockHash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getHash() {
		return hash;
	}
	
	public void setNonce(long nonce) {
		this.nonce = nonce;
	}
	
	public long getNonce() {
		return nonce;
	}
	
	/** 
	 * Return the string used for finding and verifying nonce. 
	 * The hash of a block = Hash(block.getBaseContent() + block.getNonce())
	 */
	public String getBaseContent() {
		return previousBlockHash +
			   Long.toString(timeStamp) + 
			   data;
	}
	
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
	
}
