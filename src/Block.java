import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.SerializationUtils;

import com.google.gson.GsonBuilder;

public class Block implements Serializable {
	private long timeStamp;				// When this block is mined
	private String data;				// Actual data in this block
	private String previousBlockHash;	// Hash value of previous block
	private String hash;				// Hash value of this block
	private long nonce; 				// For PoW
	
	private static final long serialVersionUID = -4259345706878498801L;
	
	public Block(String data, String previousBlockHash) {
		this.data = data;
		this.previousBlockHash = previousBlockHash;
		this.timeStamp = new Date().getTime();
		this.hash = StringUtil.getHash(previousBlockHash +
						Long.toString(timeStamp) + 
						data);
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
	
	public byte[] serialize() {
		return SerializationUtils.serialize(this);
	}
	
	public static Block deserialize(byte[] source) {
		return (Block) SerializationUtils.deserialize(source);
	}
	
}
