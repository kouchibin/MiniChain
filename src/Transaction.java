import java.io.Serializable;
import org.apache.commons.lang3.SerializationUtils;

public class Transaction implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static final int SUBSIDY = 100;  // Constant reward for mining a block
    
    private String ID;
    private TXInput[] inputs;
    private TXOutput[] outputs;
    
    public Transaction(String ID, TXInput[] inputs, TXOutput[] outputs) {
        this.ID = ID;
        this.inputs = inputs;
        this.outputs = outputs;
    }
    
    public static Transaction newCoinbaseTX(String to, String data) {
        if (data == null || data.equals(""))
            data = "Reward to " + to;
        TXInput txin = new TXInput(null, -1, data);
        TXOutput txout = new TXOutput(SUBSIDY, to);
        Transaction tx = new Transaction(null, new TXInput[] {txin}, new TXOutput[] {txout});
        tx.setID();
        return tx;
    }
    
    public void setID() {
        ID = StringUtil.getHash(serialize().toString());
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

class TXInput implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private byte[] txId;
	private int vOut;
	private String scriptSig;
	
	public TXInput(byte[] txId, int vOut, String scriptSig) {
	    this.txId = txId;
	    this.vOut = vOut;
	    this.scriptSig = scriptSig;
	}
	
	public String toString() {
	    return txId.toString() + " : " + vOut + " : " + scriptSig;
	}
}

class TXOutput implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int value;
    private String scriptPubKey;
    
    public TXOutput(int value, String scriptPubKey) {
        this.value = value;
        this.scriptPubKey = scriptPubKey;
    }
    
    public String toString() {
        return "" + value + " : " + scriptPubKey;
    }
}
