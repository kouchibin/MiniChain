import java.io.Serializable;
import java.security.PublicKey;
import java.util.Arrays;

import org.apache.commons.lang3.SerializationUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Utils;

public class Transaction implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static final int SUBSIDY = 100;  // Constant reward for mining a block
    
    private String ID;
    private TXInput[] inputs;
    private TXOutput[] outputs;
    
    public Transaction(TXInput[] inputs, TXOutput[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        setID();
    }
    
    public String getID() {
        return ID;
    }

    public TXInput[] getInputs() {
        return inputs;
    }

    public void setInputs(TXInput[] inputs) {
        this.inputs = inputs;
    }

    public TXOutput[] getOutputs() {
        return outputs;
    }

    public void setOutputs(TXOutput[] outputs) {
        this.outputs = outputs;
    }
    
    public static Transaction newCoinbaseTX(String to, String data) {
        if (data == null || data.equals(""))
            data = "Reward to " + to;
        TXInput txin = new TXInput(null, -1, data, null);
        TXOutput txout = new TXOutput(SUBSIDY, to);
        Transaction tx = new Transaction(new TXInput[] {txin}, new TXOutput[] {txout});
        return tx;
    }
    
    public void setID() {
        ID = StringUtil.getHash(serialize().toString());
    }
    
    public boolean isCoinbase() {
        return inputs[0].vOut == -1;
    }
    
    public byte[] serialize() {
        return SerializationUtils.serialize(this);
    }
    
    public String toString() {
        if (ID == null) 
            ID = "";
        return ID + " : \nInputs: " + inputs.toString() + "\nOutputs: " + outputs.toString() + "\n";
    }
    
}


class AvailableOutput {
    public final TXOutput output;
    public final Transaction transaction;
    public final int vOut;
    
    public AvailableOutput(TXOutput output, Transaction transaction, int vOut) {
        this.output = output;
        this.transaction = transaction;
        this.vOut = vOut;
    }
}

class TXInput implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public final String txId;           // The ID of the referenced transaction
    public final int vOut;              // The index of the referenced out put in the above transaction
    public final String signature;      // The proof of the right to spend that referenced output
    public final byte[] publicKey;      // Raw public key. 
	
	public TXInput(String txId, int vOut, String signature, byte[] publicKey) {
	    this.txId = txId;
	    this.vOut = vOut;
	    this.signature = signature;
	    this.publicKey = publicKey;
	}
	
	public boolean usesKey(byte[] pubKeyHash) {
	    byte[] lockingHash = Utils.sha256hash160(publicKey);
	    return Arrays.equals(pubKeyHash, lockingHash);
	}
	
	public String toString() {
	    return txId.toString() + " : " + vOut + " : " + signature;
	}
}

class TXOutput implements Serializable {

    private static final long serialVersionUID = 1L;
    public final int value;
    public final byte[] pubKeyHash;
    
    public TXOutput(int value, String address) {
        this.value = value;
        pubKeyHash = lock(address);
    }
    
    public byte[] lock(String address) {
        byte[] pubKeyHash = Base58.decode(address);
        // Remove version and checksum.
        return Arrays.copyOfRange(pubKeyHash, 1, pubKeyHash.length - 4); 
    }
    
    public boolean isLockedWithKey(byte[] pubKeyHash) {
        return Arrays.equals(this.pubKeyHash, pubKeyHash);
    }
    
    public String toString() {
        return "" + value + " : " + pubKeyHash;
    }
}
