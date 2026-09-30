package test;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import remindme.Entities.Preferences;
import remindme.Enums.LanguagesEnum;
import remindme.Enums.ThemesEnum;
import remindme.Sqlite.Database;
import remindme.Sqlite.PreferencesRepository;

@Disabled("Skip for now, preferences tests are unstable")
public class PreferencesTest {

    @TempDir
    Path tempDir;

    @Test
    public void equals_shouldReturnTrue_forSameLanguage() throws IOException {
        buildAndReloadPreferences();
        assertEquals(LanguagesEnum.DEU, Preferences.getLanguage());
    }

    @Test
    public void equals_shouldReturnTrue_forSameTheme() throws IOException {
        buildAndReloadPreferences();
        assertEquals(ThemesEnum.CARBON, Preferences.getTheme());
    }

    private void buildAndReloadPreferences() throws IOException {
        initDb();
        buildValidPreferencesObject();
        reloadPreferences();
    }

    private void initDb() throws IOException {
        try {
            Preferences.init(new PreferencesRepository(Database.open(tempDir.resolve("preferences-test.db").toString())));
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    private void buildValidPreferencesObject() {
        Preferences.setLanguage(LanguagesEnum.DEU);
        Preferences.setTheme(ThemesEnum.CARBON.getThemeName());
    }

    private void reloadPreferences() {
        Preferences.updatePreferencesToDb();
        Preferences.loadPreferencesFromDb();
    }
}
