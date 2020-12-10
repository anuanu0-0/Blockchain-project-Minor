package blockchain.utils;

import java.util.*;
import java.security.*;

// Signature for a block is generated through base64 encoder
public class SignatureUtils {

    // Use of RSA to generate Key-Pair
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Takes in transaction data and id of the block and returns current
    // blocks unique signature using SHA 256. Is assigned by hash function
    // in Transactions file.
    //
    public static String generateSignature(String data, PrivateKey privateKey) {
        String signature = null;

        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            sign.update(data.getBytes("UTF-8"));
            signature = Base64.getEncoder().encodeToString(sign.sign());
        } catch (Exception ignored) { }

        return signature;
    }

    // Verifies that the signature is unique and  decodes it back.
    public static boolean verifySignature(String data, String signature, PublicKey publicKey) {
        boolean verification = false;

        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(publicKey);
            sign.update(data.getBytes("UTF-8"));
            verification = sign.verify(Base64.getDecoder().decode(signature));
        } catch (Exception ignored) { }

        return verification;
    }
}