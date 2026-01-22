package test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import remindme.Entities.Preferences;
import remindme.Entities.RemindListPath;
import remindme.Enums.LanguagesEnum;
import remindme.Enums.ThemesEnum;

public class PreferencesTest {

    private final String directory = "src/test/resources/";
    private final String filename = "tempLogFile.json";
    private File tempLogFile;

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
        assertEquals(new RemindListPath(directory, filename), Preferences.getRemindList());
    }

    private void buildAndReloadPreferences() throws IOException {
        buildTempFile();
        buildValidPreferencesObject();
        realodPreferences();

        deleteTempFile();
    }

    private void buildTempFile() throws IOException {
        tempLogFile = File.createTempFile(directory + filename, "");
    }

    private void buildValidPreferencesObject()  {
        Preferences.setLanguage(LanguagesEnum.DEU);
        Preferences.setTheme(ThemesEnum.CARBON.getThemeName());
        Preferences.setRemindList(new RemindListPath(directory, filename));
    }

    private void realodPreferences() {
        Preferences.updatePreferencesToJson();
        Preferences.loadPreferencesFromJson();
    }

    private void deleteTempFile() {
        if (tempLogFile.exists()) {
            tempLogFile.delete();
        }
    }

}
