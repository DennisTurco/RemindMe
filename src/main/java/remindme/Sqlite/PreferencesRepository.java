package remindme.Sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Entities.RemindListPath;
import remindme.Enums.ConfigKey;
import remindme.Enums.LanguagesEnum;
import remindme.Enums.ThemesEnum;

/**
 * CRUD on the single-row preferences table. Replaces the old
 * Entities/Preferences.java JSON file store: the headless API process is the
 * sole owner of this database, mirroring how ReminderRepository replaced
 * Json/JSONReminder.java for reminders.
 */
public class PreferencesRepository {

    private static final Logger logger = LoggerFactory.getLogger(PreferencesRepository.class);

    private final Connection connection;

    public PreferencesRepository(Connection connection) {
        this.connection = connection;
    }

    public record Preferences(LanguagesEnum language, ThemesEnum theme, RemindListPath remindList) { }

    public boolean exists() {
        String sql = "SELECT 1 FROM preferences WHERE id = 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            logger.error("Failed to check preferences existence: " + ex.getMessage(), ex);
            return false;
        }
    }

    public Preferences get() {
        String sql = "SELECT language, theme, remindListDirectory, remindListFile FROM preferences WHERE id = 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException ex) {
            logger.error("Failed to read preferences: " + ex.getMessage(), ex);
        }
        return defaults();
    }

    public void save(Preferences preferences) {
        String sql = """
            INSERT INTO preferences (id, language, theme, remindListDirectory, remindListFile)
            VALUES (1, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                language = excluded.language,
                theme = excluded.theme,
                remindListDirectory = excluded.remindListDirectory,
                remindListFile = excluded.remindListFile
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, preferences.language().getFileName());
            stmt.setString(2, preferences.theme().getThemeName());
            stmt.setString(3, preferences.remindList().directory());
            stmt.setString(4, preferences.remindList().file());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to save preferences: " + ex.getMessage(), ex);
        }
    }

    public void setLanguage(String languageName) {
        Preferences current = get();
        LanguagesEnum resolved = current.language();
        for (LanguagesEnum lang : LanguagesEnum.values()) {
            if (lang.getLanguageName().equalsIgnoreCase(languageName)) {
                resolved = lang;
                break;
            }
        }
        save(new Preferences(resolved, current.theme(), current.remindList()));
    }

    public void setTheme(String themeName) {
        Preferences current = get();
        ThemesEnum resolved = current.theme();
        for (ThemesEnum t : ThemesEnum.values()) {
            if (t.getThemeName().equalsIgnoreCase(themeName)) {
                resolved = t;
                break;
            }
        }
        save(new Preferences(current.language(), resolved, current.remindList()));
    }

    public static Preferences defaults() {
        return new Preferences(
            LanguagesEnum.ENG,
            ThemesEnum.INTELLIJ,
            new RemindListPath(
                ConfigKey.RES_DIRECTORY_STRING.getValue(),
                ConfigKey.REMIND_LIST_FILE_STRING.getValue() + ConfigKey.VERSION.getValue() + ".json"
            )
        );
    }

    private Preferences mapRow(ResultSet rs) throws SQLException {
        LanguagesEnum language = LanguagesEnum.ENG;
        String languageFileName = rs.getString("language");
        for (LanguagesEnum lang : LanguagesEnum.values()) {
            if (lang.getFileName().equals(languageFileName)) {
                language = lang;
                break;
            }
        }

        ThemesEnum theme = ThemesEnum.INTELLIJ;
        String themeName = rs.getString("theme");
        for (ThemesEnum t : ThemesEnum.values()) {
            if (t.getThemeName().equals(themeName)) {
                theme = t;
                break;
            }
        }

        String directory = rs.getString("remindListDirectory");
        String file = rs.getString("remindListFile");
        RemindListPath remindList = (directory != null && file != null)
            ? new RemindListPath(directory, file)
            : defaults().remindList();

        return new Preferences(language, theme, remindList);
    }
}
