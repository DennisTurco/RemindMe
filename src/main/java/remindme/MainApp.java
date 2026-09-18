package remindme;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import remindme.Api.ApiServer;
import remindme.Api.ReminderController;
import remindme.Entities.Preferences;
import remindme.Entities.RemindListPath;
import remindme.Enums.ConfigKey;
import remindme.Enums.LanguagesEnum;
import remindme.Enums.ThemesEnum;
import remindme.Enums.TranslationLoaderEnum;
import remindme.Services.SuggestionsSeeder;
import remindme.Sqlite.Database;
import remindme.Sqlite.PreferencesRepository;
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
            Connection connection = Database.open(dbPath);

            ReminderRepository reminderRepository = new ReminderRepository(connection);
            reminderRepository.recomputeAllNextExecutions();

            PreferencesRepository preferencesRepository = new PreferencesRepository(connection);
            Preferences.init(preferencesRepository);
            migrateLegacyPreferencesIfNeeded(preferencesRepository);
            Preferences.loadPreferencesFromDb();
            try {
                TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
            } catch (IOException ex) {
                logger.error("Failed to load translations: " + ex.getMessage(), ex);
            }

            var legacyList = Preferences.getRemindList();
            if (reminderRepository.getAll().isEmpty()) {
                try {
                    reminderRepository.insertMany(remindme.Json.JSONReminder.readRemindListFromJSON(legacyList.directory(), legacyList.file()));
                } catch (IOException ex) {
                    logger.info("No legacy remind list to migrate: " + ex.getMessage());
                }
            }

            SuggestionsSeeder.seedIfEmpty(reminderRepository, resDirectory, "suggestions_remind.json");

            ApiServer server = new ApiServer(new ReminderController(reminderRepository));
            server.start(ApiServer.DEFAULT_PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } catch (SQLException ex) {
            logger.error("Failed to start API server: " + ex.getMessage(), ex);
        }
    }

    /**
     * One-time setup of the preferences table: migrates the old
     * preferences.json file if one exists (mirroring how legacy remind lists
     * are migrated above), otherwise seeds the row with default preferences
     * (mirroring SuggestionsSeeder for reminders). Runs only while the
     * preferences table is still empty, so it never overwrites a value the
     * user already saved through the DB.
     */
    private static void migrateLegacyPreferencesIfNeeded(PreferencesRepository preferencesRepository) {
        if (preferencesRepository.exists()) {
            return;
        }

        String legacyPath = ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.PREFERENCES_FILE_STRING.getValue();
        try (FileReader reader = new FileReader(legacyPath)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            RemindListPath defaultRemindList = PreferencesRepository.defaults().remindList();

            LanguagesEnum language = LanguagesEnum.ENG;
            if (json.has("Language") && !json.get("Language").isJsonNull()) {
                String fileName = json.get("Language").getAsString();
                for (LanguagesEnum lang : LanguagesEnum.values()) {
                    if (lang.getFileName().equals(fileName)) {
                        language = lang;
                        break;
                    }
                }
            }

            ThemesEnum theme = ThemesEnum.INTELLIJ;
            if (json.has("Theme") && !json.get("Theme").isJsonNull()) {
                String themeName = json.get("Theme").getAsString();
                for (ThemesEnum t : ThemesEnum.values()) {
                    if (t.getThemeName().equals(themeName)) {
                        theme = t;
                        break;
                    }
                }
            }

            RemindListPath remindList = defaultRemindList;
            if (json.has("RemindList") && !json.get("RemindList").isJsonNull()) {
                JsonObject remindListJson = json.getAsJsonObject("RemindList");
                String directory = remindListJson.has("Directory") && !remindListJson.get("Directory").isJsonNull()
                    ? remindListJson.get("Directory").getAsString()
                    : defaultRemindList.directory();
                String file = remindListJson.has("File") && !remindListJson.get("File").isJsonNull()
                    ? remindListJson.get("File").getAsString()
                    : defaultRemindList.file();
                remindList = new RemindListPath(directory, file);
            }

            preferencesRepository.save(new PreferencesRepository.Preferences(language, theme, remindList));
            logger.info("Migrated legacy preferences.json into the SQLite database");
        } catch (IOException ex) {
            logger.info("No legacy preferences.json to migrate, seeding default preferences instead: " + ex.getMessage());
            preferencesRepository.save(PreferencesRepository.defaults());
        } catch (Exception ex) {
            // Malformed/corrupt legacy file (e.g. invalid JSON): don't let a
            // bad old file crash startup, fall back to defaults like the old
            // Preferences.loadPreferencesFromJson() did.
            logger.error("Failed to parse legacy preferences.json, seeding default preferences instead: " + ex.getMessage(), ex);
            preferencesRepository.save(PreferencesRepository.defaults());
        }
    }
}
