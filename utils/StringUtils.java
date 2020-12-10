package blockchain.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

// Message digest to calculate crypto hash functions
public class StringUtils {

    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte elem : hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String randomAlphaString(int length) {
        String alpha = "abcdefg hijklmn opqrst uvwxyz ABCDEFG HIJKLMN OPQRST UVWXYZ";
        Random random = new Random();
        char randomString[] = new char[length];

        for (int i = 0; i < length; i++) {
            randomString[i] = alpha.charAt(random.nextInt(59));
        }
        return new String(randomString);
    }

}