package remindme;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Api.ApiServer;
import remindme.Api.ReminderController;
import remindme.Entities.Preferences;
import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum;
import remindme.Services.SuggestionsSeeder;
import remindme.Sqlite.Database;
import remindme.Sqlite.ReminderRepository;

/**
 * Entry point for the headless API backend (remindme.Api.ApiServer),
 * consumed by the Electron/React frontend (app/). The old Swing GUI and its
 * background-service/tray mode have been retired now that Electron owns the
 * UI and tray entirely; this class only ever starts the API server.
 */
public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static final String CONFIG = "src/main/resources/res/config/config.json";

    public static void main(String[] args) {
        ConfigKey.loadFromJson(CONFIG);

        loadPreferredLanguage();

        boolean isServeMode = args.length > 0 && args[0].equalsIgnoreCase("--serve");
        if (!isServeMode) {
            logger.warn("Usage: java -jar RemindMe.jar --serve");
            throw new IllegalArgumentException("Expected --serve argument");
        }

        logger.info("Application started");
        runApiServer();
    }

    private static void runApiServer() {
        try {
            String resDirectory = ConfigKey.RES_DIRECTORY_STRING.getValue();
            String dbPath = resDirectory + "reminders.db";

            ReminderRepository repository = new ReminderRepository(Database.open(dbPath));
            repository.recomputeAllNextExecutions();

            Preferences.loadPreferencesFromJson();
            var legacyList = Preferences.getRemindList();
            if (repository.getAll().isEmpty()) {
                try {
                    repository.insertMany(remindme.Json.JSONReminder.readRemindListFromJSON(legacyList.directory(), legacyList.file()));
                } catch (IOException ex) {
                    logger.info("No legacy remind list to migrate: " + ex.getMessage());
                }
            }

            SuggestionsSeeder.seedIfEmpty(repository, resDirectory, "suggestions_remind.json");

            ApiServer server = new ApiServer(new ReminderController(repository));
            server.start(ApiServer.DEFAULT_PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } catch (SQLException ex) {
            logger.error("Failed to start API server: " + ex.getMessage(), ex);
        }
    }

    private static void loadPreferredLanguage() {
        try {
            Preferences.loadPreferencesFromJson();
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
        } catch (IOException ex) {
            logger.error("An error occurred during loading preferences: " + ex.getMessage(), ex);
        }
    }
}
