package remindme;

import java.io.IOException;
import java.util.Arrays;

import remindme.Entities.Preferences;
import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum;
import remindme.GUI.MainGUI;
import remindme.Json.JSONConfigReader;
import remindme.Logger.LogLevel;
import remindme.Managers.ExceptionManager;
import remindme.Services.BackgroundService;

public class MainApp {
    private static final String CONFIG = "src/main/resources/res/config/config.json";

    public static void main(String[] args) {
        // load config keys
        ConfigKey.loadFromJson(CONFIG);
        Logger.configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());

        // load preferred language
        try { 
            Preferences.loadPreferencesFromJSON();
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
        } catch (IOException ex) {
            Logger.logMessage("An error occurred during loading preferences: ", LogLevel.DEBUG, ex);
        }

        boolean isBackgroundMode = args.length > 0 && args[0].equalsIgnoreCase("--background");

        // check argument correction
        if (!isBackgroundMode && args.length > 0) {
            Logger.logMessage("Argument \""+ args[0] +"\" not valid!", Logger.LogLevel.WARN);
            throw new IllegalArgumentException("Argument passed is not valid!");
        }
        
        Logger.logMessage("Application started", Logger.LogLevel.INFO);
        Logger.logMessage("Background mode: " + isBackgroundMode, Logger.LogLevel.DEBUG);
        
        if (isBackgroundMode) {
            Logger.logMessage("Backup service starting in the background", Logger.LogLevel.INFO);
            BackgroundService service = new BackgroundService();
            try {
                service.startService();
            } catch (IOException ex) {
                Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
                ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            }
        }
        else if (!isBackgroundMode) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                MainGUI gui = new MainGUI();
                gui.showWindow();
            });
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.showWindow();
        });
    }
}
