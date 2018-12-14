import java.util.Date;
import com.google.gson.GsonBuilder;

public class Block {
	private long timeStamp;
	private String data;
	private String previousBlockHash;
	private String hash;
	
	public Block(String data, String previousBlockHash) {
		this.data = data;
		this.previousBlockHash = previousBlockHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash();
	}
	
	public String calculateHash() {
		String hash = StringUtil.getHash(
				previousBlockHash +
				Long.toString(timeStamp) + 
				data
				);
		return hash;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
	
}
