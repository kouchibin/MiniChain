import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

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
    
    public void setID() {
        String inputsSerialize = new String(SerializationUtils.serialize(inputs));
        String outputsSerialize = new String(SerializationUtils.serialize(outputs));
        ID = StringUtil.getHash(inputsSerialize + outputsSerialize);
    }
    
    public void setID(String newID) {
        ID = newID;
    }
    
    public String getID() {
        return ID;
    }

    public void setInputs(TXInput[] inputs) {
        this.inputs = inputs;
    }
    
    public TXInput[] getInputs() {
        return inputs;
    }

    public void setOutputs(TXOutput[] outputs) {
        this.outputs = outputs;
    }

    public TXOutput[] getOutputs() {
        return outputs;
    }

    public static Transaction newCoinbaseTX(String to, String data) {
        if (data == null || data.equals(""))
            data = "Reward to " + to;
        TXInput txin = new TXInput(null, -1, data.getBytes(), null);
        TXOutput txout = new TXOutput(SUBSIDY, to);
        Transaction tx = new Transaction(new TXInput[] {txin}, new TXOutput[] {txout});
        return tx;
    }
    
    public boolean isCoinbase() {
        return inputs[0].vOut == -1;
    }
    
    public void sign(PrivateKey priKey, Map<String, Transaction> prevTXs) throws Exception {
        if (isCoinbase()) return;
        Transaction txCopy = trimmedCopy();
        for (int i = 0; i < inputs.length; i++) {
            TXInput input = inputs[i];
            TXInput copyInput = txCopy.inputs[i];
            Transaction prevTX = prevTXs.get(input.txId);
            copyInput.signature = null;
            copyInput.publicKey = prevTX.outputs[copyInput.vOut].pubKeyHash;
            txCopy.setID();
            
            copyInput.publicKey = null;
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initSign(priKey);
            sig.update(txCopy.ID.getBytes());
            input.signature = sig.sign();
        }
    }
    
    public boolean verify(Map<String, Transaction> prevTXs) throws Exception {
        Transaction txCopy = trimmedCopy();
        for (int i = 0; i < inputs.length; i++) {
            TXInput input = inputs[i];
            TXInput copyInput = txCopy.inputs[i];
            Transaction prevTX = prevTXs.get(input.txId);
            copyInput.signature = null;
            copyInput.publicKey = prevTX.outputs[copyInput.vOut].pubKeyHash;
            txCopy.setID();
            
            copyInput.publicKey = null;
            Signature sig = Signature.getInstance("SHA256withECDSA");
            PublicKey pubKey = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(input.publicKey));
            sig.initVerify(pubKey);
            sig.update(txCopy.ID.getBytes());
            boolean result = sig.verify(input.signature);
            if (!result)
                return false;
        }
        return true;
    }
    
    private Transaction trimmedCopy() {
        ArrayList<TXInput> inputs = new ArrayList<>();
        ArrayList<TXOutput> outputs = new ArrayList<>();
        for (TXInput input : this.inputs) 
            inputs.add(new TXInput(input.txId, input.vOut, null, null));
        for (TXOutput output : this.outputs) 
            outputs.add(new TXOutput(output.value, output.address));
        Transaction copy = new Transaction(inputs.toArray(new TXInput[0]), outputs.toArray(new TXOutput[0]));
        copy.setID(this.getID());
        return copy;
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
    
    public final String txId;     // The ID of the referenced transaction
    public final int vOut;        // The index of the referenced output in the above transaction
    public byte[] signature;      // The proof of the right to spend that referenced output
    public byte[] publicKey;      // Raw public key
	
	public TXInput(String txId, int vOut, byte[] signature, byte[] publicKey) {
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
    public final String address;
    public final byte[] pubKeyHash;
    
    public TXOutput(int value, String address) {
        this.value = value;
        this.address = address;
        pubKeyHash = generatePubKeyHashFromAddress(address);
    }
    
    public byte[] generatePubKeyHashFromAddress(String address) {
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
