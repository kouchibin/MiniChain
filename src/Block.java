import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.SerializationUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class Block implements Serializable {
    @Expose
    private long timeStamp;                 // When this block is mined
    @Expose
    private String previousBlockHash;       // Hash value of previous block
    @Expose
    private String hash;                    // Hash value of this block
    @Expose
    private long nonce;                     // For PoW
    
    private Transaction[] transactions;     // Transactions stored in this block

    private static final long serialVersionUID = -4259345706878498801L;

    public Block(Transaction[] transactions, String previousBlockHash) {
        this.transactions = transactions;
        this.previousBlockHash = previousBlockHash;
        this.timeStamp = new Date().getTime();
        this.hash = StringUtil.getHash(previousBlockHash + Long.toString(timeStamp) + transactions);
    }

    public Transaction[] getTransactions() {
        return transactions;
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
     * Return the string used for finding and verifying nonce. The hash of a block =
     * Hash(block.getBaseContent() + block.getNonce())
     */
    public String getBaseContent() {
        StringBuilder sb = new StringBuilder();
        for (Transaction tx : transactions) {
            sb.append(StringUtil.getHash(tx.toString()));
        }
        return previousBlockHash + Long.toString(timeStamp) + sb.toString();
    }

    public String toString() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(this);
    }

    public byte[] serialize() {
        return SerializationUtils.serialize(this);
    }

    public static Block deserialize(byte[] source) {
        return (Block) SerializationUtils.deserialize(source);
    }

}
