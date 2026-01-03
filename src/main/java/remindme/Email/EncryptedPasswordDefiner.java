package remindme.Email;

import ch.qos.logback.core.PropertyDefinerBase;

public class EncryptedPasswordDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        try {
            return ConfigReader.getSMTPPassword();
        } catch (Exception e) {
            System.err.println("Error reading SMTP password from config.enc: " + e.getMessage());
            return "";
        }
    }
}