package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import remindme.Entities.Preferences;
import remindme.Entities.RemindListPath;
import remindme.Enums.LanguagesEnum;
import remindme.Enums.ThemesEnum;

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

    @Test
    public void equal_shouldReturnTrue_forSameRemindListPath() throws IOException {
        buildAndReloadPreferences();
        assertEquals(new RemindListPath(tempDir.toString() + "/", "tempLogFile.json"),
                     Preferences.getRemindList());
    }

    private void buildAndReloadPreferences() throws IOException {
        buildTempFile();
        buildValidPreferencesObject();
        reloadPreferences();
    }

    private void buildTempFile() throws IOException {
        File tempLogFile = tempDir.resolve("tempLogFile.json").toFile();
        if (!tempLogFile.exists()) {
            tempLogFile.createNewFile();
        }
    }

    private void buildValidPreferencesObject() {
        Preferences.setLanguage(LanguagesEnum.DEU);
        Preferences.setTheme(ThemesEnum.CARBON.getThemeName());

        Preferences.setRemindList(
            new RemindListPath(tempDir.toString() + "/", "tempLogFile.json")
        );
    }

    private void reloadPreferences() {
        Preferences.updatePreferencesToJson();
        Preferences.loadPreferencesFromJson();
    }
}
