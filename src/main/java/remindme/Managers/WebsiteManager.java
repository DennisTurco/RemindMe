package remindme.Managers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;

public class WebsiteManager {

    private static final Logger logger = LoggerFactory.getLogger(WebsiteManager.class);

    public static void openWebSite(String reportUrl) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(reportUrl));
                }
            }
        } catch (IOException | URISyntaxException e) {
            logger.warn("Failed to open the web page. Please try again");
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_OPENING_WEBSITE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void sendEmail() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            if (desktop.isSupported(Desktop.Action.MAIL)) {
                String subject = "Support - Remind Me";
                String mailTo = "mailto:" + ConfigKey.EMAIL.getValue() + "?subject=" + encodeURI(subject);

                try {
                    URI uri = new URI(mailTo);
                    desktop.mail(uri);
                } catch (IOException | URISyntaxException ex) {
                    logger.error("Failed to send email: " + ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_UNABLE_TO_SEND_EMAIL), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                logger.warn("Mail action is unsupported in your system's desktop environment");
                JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_NOT_SUPPORTED_EMAIL), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            logger.warn("Desktop integration is unsupported on this system");
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_NOT_SUPPORTED_EMAIL_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to properly encode the URI with special characters (spaces, symbols, etc.)
    private static String encodeURI(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (IOException e) {
            logger.error("An error occurred during URI encode: " + e.getMessage(), e);
            return value; // If encoding fails, return the original value
        }
    }
}
