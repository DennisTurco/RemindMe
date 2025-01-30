package remindme;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Entities.Preferences;
import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum;
import remindme.GUI.MainGUI;
import remindme.Managers.ExceptionManager;
import remindme.Services.BackgroundService;

public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static final String CONFIG = "src/main/resources/res/config/config.json";

    public static void main(String[] args) {
        // load config keys
        ConfigKey.loadFromJson(CONFIG);

        // load preferred language
        try { 
            Preferences.loadPreferencesFromJSON();
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
        } catch (IOException ex) {
            logger.error("An error occurred during loading preferences: " + ex.getMessage(), ex);
        }

        boolean isBackgroundMode = args.length > 0 && args[0].equalsIgnoreCase("--background");

        // check argument correction
        if (!isBackgroundMode && args.length > 0) {
            logger.warn("Argument \""+ args[0] +"\" not valid!");
            throw new IllegalArgumentException("Argument passed is not valid!");
        }
        
        logger.info("Application started");
        logger.debug("Background mode: " + isBackgroundMode);
        
        if (isBackgroundMode) {
            logger.info("Backup service starting in the background");
            BackgroundService service = new BackgroundService();
            try {
                service.startService();
            } catch (IOException ex) {
                logger.error("An error occurred: " + ex.getMessage(), ex);
                ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            }
        }
        else if (!isBackgroundMode) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                MainGUI gui = new MainGUI();
                gui.showWindow();
            });
        }
    }
}
