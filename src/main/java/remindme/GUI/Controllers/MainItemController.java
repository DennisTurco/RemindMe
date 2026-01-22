package remindme.GUI.Controllers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.Managers.WebsiteManager;

public class MainItemController {

    private static final Logger logger = LoggerFactory.getLogger(MainItemController.class);

    public static void menuSupport() {
        logger.info("Event --> support");
        WebsiteManager.sendEmail();
    }

    public static void menuWebsite() {
        logger.info("Event --> shard website");
        WebsiteManager.openWebSite(ConfigKey.SHARD_WEBSITE.getValue());
    }

    public static void menuItemDonateViaBuymeacoffe() {
        logger.info("Event --> buymeacoffe donation");
        WebsiteManager.openWebSite(ConfigKey.DONATE_BUYMEACOFFE_LINK.getValue());
    }

    public static void menuItemDonateViaPaypal() {
        logger.info("Event --> paypal donation");
        WebsiteManager.openWebSite(ConfigKey.DONATE_PAYPAL_LINK.getValue());
    }

    public static void menuBugReport() {
        logger.info("Event --> bug report");
        WebsiteManager.openWebSite(ConfigKey.ISSUE_PAGE_LINK.getValue());
    }

    public static void menuShare() {
        // pop-up message
        JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.SHARE_LINK_COPIED_MESSAGE));

        // copy link to the clipboard
        StringSelection stringSelectionObj = new StringSelection(ConfigKey.SHARE_LINK.getValue());
        Clipboard clipboardObj = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboardObj.setContents(stringSelectionObj, null);
    }

    public static void menuItemHistory() {
        logger.info("Event --> history");
        try {
            logger.debug("Opening log file with path: " + ConfigKey.LOG_DIRECTORY_STRING.getValue() + ConfigKey.LOG_FILE_STRING.getValue());
            new ProcessBuilder("notepad.exe", ConfigKey.LOG_DIRECTORY_STRING.getValue() + ConfigKey.LOG_FILE_STRING.getValue()).start();
        } catch (IOException e) {
            logger.error("Error opening history file: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_OPEN_HISTORY_FILE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void menuQuit() {
        logger.info("Event --> exit");
        System.exit(0);
    }

    public static void menuInfoPage() {
        logger.info("Event --> shard website");
        WebsiteManager.openWebSite(ConfigKey.INFO_PAGE_LINK.getValue());
    }
}
