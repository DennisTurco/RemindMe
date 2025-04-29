package remindme.Email;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class DecryptPassword {
    public static String decrypt(String encryptedPassword) throws Exception {
        String secretKey = System.getenv("REMINDME_AES_KEY");
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("Secret key not set in REMINDME_AES_KEY");
        }

        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
        byte[] decrypted = cipher.doFinal(decodedBytes);

        return new String(decrypted);
    }

    public static void main(String[] args) throws Exception {
        String encryptedPassword = "password_to_decrypt";
        String decryptedPassword = decrypt(encryptedPassword);
        System.out.println("\n"+decryptedPassword);
    }
}
