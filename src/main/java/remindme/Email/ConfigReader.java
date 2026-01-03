package remindme.Email;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ConfigReader {

    private static final String SECRET_KEY = "RemindMeSbureria";

    public static String getSMTPPassword() throws Exception {
        byte[] encryptedBytes = Files.readAllBytes(Paths.get("config.enc"));
        byte[] decoded = Base64.getDecoder().decode(encryptedBytes);

        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decrypted = cipher.doFinal(decoded);
        String config = new String(decrypted);

        for (String line : config.split("\n")) {
            if (line.startsWith("SMTP_PASSWORD=")) {
                return line.substring("SMTP_PASSWORD=".length());
            }
        }
        throw new IllegalStateException("SMTP_PASSWORD not found in config.enc");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("SMTP Password: " + getSMTPPassword());
    }
}
