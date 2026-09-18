package com.revature.Manbujin.Utility;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PasswordEncryption {

    //private static final String key = "1234567890123456"; - hard coded key for early tests
    private static final String key = System.getenv("PASSWORD-KEY");
    //System.out.println(System.getenv("PASSWORD-KEY")); - a check to see if env variable config worked

    public static String encrypt(String password) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedPassword =
                    cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedPassword);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String password) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] encryptedPassword =
                    Base64.getDecoder().decode(password);

            return new String(
                    cipher.doFinal(encryptedPassword),
                    StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
