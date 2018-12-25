import java.io.Serializable;
import org.apache.commons.lang3.SerializationUtils;

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
        TXInput txin = new TXInput(null, -1, data);
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
    
    public final String txId;        // The ID of the referenced transaction
    public final int vOut;           // The index of the referenced out put in the above transaction
    public final String scriptSig;   // The proof of the right to spend that referenced output
	
	public TXInput(String txId, int vOut, String scriptSig) {
	    this.txId = txId;
	    this.vOut = vOut;
	    this.scriptSig = scriptSig;
	}
	
	public boolean canUnlockOutputWith(String unlockingData) {
	    return scriptSig == unlockingData;
	}
	
	public String toString() {
	    return txId.toString() + " : " + vOut + " : " + scriptSig;
	}
}

class TXOutput implements Serializable {

    private static final long serialVersionUID = 1L;
    public final int value;
    public final String scriptPubKey;
    
    public TXOutput(int value, String scriptPubKey) {
        this.value = value;
        this.scriptPubKey = scriptPubKey;
    }
    
    public boolean canBeUnlockedWith(String unlockingData) {
        return scriptPubKey.equals(unlockingData);
    }
    
    public String toString() {
        return "" + value + " : " + scriptPubKey;
    }
}
