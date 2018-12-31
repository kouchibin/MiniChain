import java.security.*;
import java.security.spec.*;
import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Utils;

public class Wallets extends HashMap<String, Wallet>{

    private static final long serialVersionUID = 1L;
    
    public Wallet newWallet() throws Exception {
        Wallet wallet = new Wallet();
        put(wallet.address, wallet);
        return wallet;
    }
}

class Wallet {
    
    public final KeyPair keyPair;
    
    // Bitcoin style address generated from the public key of the keyPair.
    public final String address;  
    
    public Wallet() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyGen.initialize(ecSpec);
        keyPair = keyGen.generateKeyPair();
        address = generateAddress();
    }
    
    public String generateAddress() throws Exception {
        byte[] pubKeyHash = generatePubKeyHash(keyPair.getPublic());
        byte[] versionedPayload = ArrayUtils.addAll(new byte[] {0}, pubKeyHash);
        byte[] checksum = generateChecksum(versionedPayload);
        byte[] fullPayload = ArrayUtils.addAll(versionedPayload, checksum);
        return Base58.encode(fullPayload);
    }
    
    public static byte[] generatePubKeyHash(PublicKey publicKey) {
        byte[] pubKeyHash = Utils.sha256hash160(publicKey.getEncoded());
        return pubKeyHash;
    }
    
    private byte[] generateChecksum(byte[] payload) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] firstHash = sha.digest(payload);
        byte[] secondHash = sha.digest(firstHash);
        return Arrays.copyOfRange(secondHash, 0, 4);
    }
    
}
