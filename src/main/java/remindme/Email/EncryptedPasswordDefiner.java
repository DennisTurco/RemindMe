package remindme.Email;

import ch.qos.logback.core.PropertyDefinerBase;

public class EncryptedPasswordDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        try {
            String encryptedPassword = System.getenv("REMINDME_SMTP_PASSWORD");
            return remindme.Email.DecryptPassword.decrypt(encryptedPassword);
        } catch (Exception e) {
            System.err.println("Error decrypting SMTP password: " + e.getMessage());
            return "";
        }
    }
}