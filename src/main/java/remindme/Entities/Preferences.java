package remindme.Entities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import remindme.Enums.ConfigKey;
import remindme.Enums.LanguagesEnum;
import remindme.Enums.ThemesEnum;
import remindme.Logger;
import remindme.Logger.LogLevel;
import remindme.Managers.ExceptionManager;

public class Preferences {
    private static LanguagesEnum language;
    private static ThemesEnum theme;
    private static RemindListPath remindList;

    public static void loadPreferencesFromJSON() {
        try (FileReader reader = new FileReader(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.PREFERENCES_FILE_STRING.getValue())) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            
            language = getLanguageFromJson(jsonObject);
            theme = getThemeFromJson(jsonObject);
            remindList = getRemindListFromJson(jsonObject);

            Logger.logMessage("Preferences loaded from JSON file: language = " + language.getFileName() + ", theme = " + theme.getThemeName(), Logger.LogLevel.DEBUG);

            updatePreferencesToJSON();

        } catch (FileNotFoundException e) {
            Logger.logMessage("Preferences file not found. Using default preferences.", Logger.LogLevel.WARN);
            updatePreferencesToJSON(); // Create the JSON file with default preferences
        } catch (Exception ex) {
            Logger.logMessage("An error occurred while loading preferences: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public static void updatePreferencesToJSON() {
        try (FileWriter writer = new FileWriter(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.PREFERENCES_FILE_STRING.getValue())) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("Language", language.getFileName());
            jsonObject.addProperty("Theme", theme.getThemeName());

            JsonObject RemindListObject = new JsonObject();
            RemindListObject.addProperty("Directory", remindList.getDirectory());
            RemindListObject.addProperty("File", remindList.getFile());  

            jsonObject.add("RemindList", RemindListObject);

            // Convert JsonObject to JSON string using Gson
            Gson gson = new Gson();
            gson.toJson(jsonObject, writer);

            Logger.logMessage("Preferences updated to JSON file: language = " + language.getFileName() + ", theme = " + theme.getThemeName() , Logger.LogLevel.INFO);

        } catch (IOException ex) {
            Logger.logMessage("An error occurred during updating preferences to json operation: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static LanguagesEnum getLanguageFromJson(JsonObject jsonObject) {
        if (jsonObject.has("Language") && !jsonObject.get("Language").isJsonNull()) {
            String languageFileName = jsonObject.get("Language").getAsString();
            for (LanguagesEnum lang : LanguagesEnum.values()) {
                if (lang.getFileName().equals(languageFileName)) {
                    return lang;
                }
            }
        }
        return LanguagesEnum.ENG;
    }
    
    private static ThemesEnum getThemeFromJson(JsonObject jsonObject) {
        if (jsonObject.has("Theme") && !jsonObject.get("Theme").isJsonNull()) {
            String themeName = jsonObject.get("Theme").getAsString();
            for (ThemesEnum t : ThemesEnum.values()) {
                if (t.getThemeName().equals(themeName)) {
                    return t;
                }
            }
        }
        return ThemesEnum.INTELLIJ;
    }
    
    private static RemindListPath getRemindListFromJson(JsonObject jsonObject) {
        if (jsonObject.has("RemindList") && !jsonObject.get("RemindList").isJsonNull()) {
            JsonObject RemindListObject = jsonObject.getAsJsonObject("RemindList");

            String directory = RemindListObject.has("Directory") && !RemindListObject.get("Directory").isJsonNull()
                ? RemindListObject.get("Directory").getAsString()
                : ConfigKey.RES_DIRECTORY_STRING.getValue();

            String file = RemindListObject.has("File") && !RemindListObject.get("File").isJsonNull()
                ? RemindListObject.get("File").getAsString()
                : ConfigKey.REMIND_LIST_FILE_STRING.getValue() + ConfigKey.VERSION.getValue() + ".json";

            return new RemindListPath(directory, file);
        }
        return getDefaultRemindList();
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
        return new RemindListPath(
            ConfigKey.RES_DIRECTORY_STRING.getValue(),
            ConfigKey.REMIND_LIST_FILE_STRING.getValue() + ConfigKey.VERSION.getValue() + ".json"
        );
    }
    public static void setLanguage(LanguagesEnum language) {
        Preferences.language = language;
    }
    public static void setTheme(ThemesEnum theme) {
        Preferences.theme = theme;
    }
    public static void setRemindList(RemindListPath remindList) {
        Preferences.remindList = remindList;
    }
    public static void setLanguage(String selectedLanguage) {
        try {
            for (LanguagesEnum lang : LanguagesEnum.values()) {
                if (lang.getLanguageName().equalsIgnoreCase(selectedLanguage)) {
                    language = lang;
                    Logger.logMessage("Language set to: " + language.getLanguageName(), LogLevel.INFO);
                    return;
                }
            }
            Logger.logMessage("Invalid language name: " + selectedLanguage, LogLevel.WARN);
        } catch (Exception ex) {
            Logger.logMessage("An error occurred during setting language operation: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
    public static void setTheme(String selectedTheme) {
        try {
            for (ThemesEnum t : ThemesEnum.values()) {
                if (t.getThemeName().equalsIgnoreCase(selectedTheme)) {
                    theme = t;
                    Logger.logMessage("Theme set to: " + theme.getThemeName(), LogLevel.INFO);
                    return;
                }
            }
            Logger.logMessage("Invalid theme name: " + selectedTheme, LogLevel.WARN);
        } catch (Exception ex) {
            Logger.logMessage("An error occurred during setting theme operation: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}
