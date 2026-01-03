package remindme.Email;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptConfigFile {
    private static final String SECRET_KEY = "RemindMeSbureria";

    public static void main(String[] args) throws Exception {
        byte[] plaintext = Files.readAllBytes(Paths.get("config.txt"));

        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted = cipher.doFinal(plaintext);
        String encoded = Base64.getEncoder().encodeToString(encrypted);

        Files.write(Paths.get("config.enc"), encoded.getBytes());
        System.out.println("File config.enc created succesfully");
    }
}