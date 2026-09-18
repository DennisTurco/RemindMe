package remindme.Entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Enums.LanguagesEnum;
import remindme.Enums.ThemesEnum;
import remindme.Sqlite.PreferencesRepository;

/**
 * In-memory cache of the current preferences, backed by the SQLite
 * "preferences" table (remindme.Sqlite.PreferencesRepository). Replaces the
 * old preferences.json file store: call init() once with the repository
 * before using any other method.
 */
public class Preferences {
    private static final Logger logger = LoggerFactory.getLogger(Preferences.class);
    private static PreferencesRepository repository;
    private static LanguagesEnum language;
    private static ThemesEnum theme;
    private static RemindListPath remindList;

    private Preferences() {
    }

    public static void init(PreferencesRepository preferencesRepository) {
        repository = preferencesRepository;
    }

    public static void loadPreferencesFromDb() {
        if (repository == null) {
            logger.error("Preferences repository not initialized; using default preferences.");
            setDefaultPreferences();
            return;
        }

        PreferencesRepository.Preferences loaded = repository.get();
        language = loaded.language();
        theme = loaded.theme();
        remindList = loaded.remindList();

        logger.info("Preferences loaded from database: language = " + language.getFileName() + ", theme = " + theme.getThemeName());
    }

    private static void setDefaultPreferences() {
        PreferencesRepository.Preferences defaults = PreferencesRepository.defaults();
        language = defaults.language();
        theme = defaults.theme();
        remindList = defaults.remindList();
    }

    public static void updatePreferencesToDb() {
        if (repository == null) {
            logger.error("Preferences repository not initialized; cannot persist preferences.");
            return;
        }

        repository.save(new PreferencesRepository.Preferences(language, theme, remindList));
        logger.info("Preferences updated in database: language = " + language.getFileName() + ", theme = " + theme.getThemeName());
    }

    public static LanguagesEnum getLanguage() {
        return language;
    }
    public static ThemesEnum getTheme() {
        return theme;
    }
    public static RemindListPath getRemindList() {
        return remindList;
    }
    public static RemindListPath getDefaultRemindList() {
        return PreferencesRepository.defaults().remindList();
    }
    public static void setLanguage(LanguagesEnum language) {
        Preferences.language = language;
    }
    public static void setRemindList(RemindListPath remindList) {
        Preferences.remindList = remindList;
    }
    public static void setLanguage(String selectedLanguage) {
        try {
            for (LanguagesEnum lang : LanguagesEnum.values()) {
                if (lang.getLanguageName().equalsIgnoreCase(selectedLanguage)) {
                    language = lang;
                    logger.info("Language set to: " + language.getLanguageName());
                    return;
                }
            }
            logger.warn("Invalid language name: " + selectedLanguage);
        } catch (Exception ex) {
            logger.error("An error occurred during setting language operation: " + ex.getMessage(), ex);        }
    }
    public static void setTheme(String selectedTheme) {
        try {
            for (ThemesEnum t : ThemesEnum.values()) {
                if (t.getThemeName().equalsIgnoreCase(selectedTheme)) {
                    theme = t;
                    logger.info("Theme set to: " + theme.getThemeName());
                    return;
                }
            }
            logger.warn("Invalid theme name: " + selectedTheme);
        } catch (Exception ex) {
            logger.error("An error occurred during setting theme operation: " + ex.getMessage(), ex);        }
    }
}
