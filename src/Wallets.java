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
    public final String address;
    
    public Wallet() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyGen.initialize(ecSpec);
        keyPair = keyGen.generateKeyPair();
        address = generateAddress();
    }
    
    public static byte[] getPubKeyHash(PublicKey publicKey) {
        byte[] pubKeyHash = Utils.sha256hash160(publicKey.getEncoded());
        return pubKeyHash;
    }
    
    public String generateAddress() throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[] pubKeyHash = getPubKeyHash(keyPair.getPublic());
        byte[] versionedPayload = ArrayUtils.addAll(new byte[] {0}, pubKeyHash);
        byte[] checksum = checksum(versionedPayload);
        byte[] fullPayload = ArrayUtils.addAll(versionedPayload, checksum);
        return Base58.encode(fullPayload);
    }
    
    private byte[] checksum(byte[] payload) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] firstHash = sha.digest(payload);
        byte[] secondHash = sha.digest(firstHash);
        return Arrays.copyOfRange(secondHash, 0, 4);
    }
    
}
