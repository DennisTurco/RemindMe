package remindme.Json;

import com.google.gson.*;
import java.io.FileReader;
import java.io.IOException;

import remindme.Logger;

public class JSONConfigReader {

    private final String filename;
    private final String directoryPath;
    private JsonObject config;

    public JSONConfigReader(String filename, String directoryPath) {
        this.filename = filename;
        this.directoryPath = directoryPath;
        loadConfig(); // Load configuration at instantiation
    }

    public boolean isLogLevelEnabled(String level) {
        if (config == null) {
            Logger.logMessage("Configuration not loaded. Cannot check log level", Logger.LogLevel.ERROR);
            return false;
        }

        JsonObject logService = config.getAsJsonObject("LogService");
        if (logService != null) {
            JsonElement isEnabled = logService.get(level);
            return isEnabled != null && isEnabled.getAsBoolean();
        }
        return false; // Default to false if LogService or level is missing
    }

    public boolean isMenuItemEnabled(String menuItem) {
        if (config == null) {
            Logger.logMessage("Configuration not loaded. Cannot check menu items", Logger.LogLevel.ERROR);
            return false;
        }

        JsonObject menuService = config.getAsJsonObject("MenuItems");
        if (menuService != null) {
            JsonElement isEnabled = menuService.get(menuItem);
            return isEnabled != null && isEnabled.getAsBoolean();
        }
        return true; // Default to true
    }

    public int getMaxLines() {
        return getConfigValue("MaxLines", 1500); // Default to 1500
    }

    public int getLinesToKeepAfterFileClear() {
        return getConfigValue("LinesToKeepAfterFileClear", 150); // Default to 150
    }

    // return seconds to wait after next check
    public int readCheckForReminderTimeInterval() throws IOException {
        try {
            JsonObject reminderService = getReminderServiceConfig();
            JsonElement interval = reminderService.get("value");

            // if the interval is null, set to default of 5 minutes
            int timeInterval = (interval != null) ? interval.getAsInt() : 5;

            Logger.logMessage("Time interval set to " + timeInterval + " seconds", Logger.LogLevel.INFO);
            return timeInterval;
        } catch (NullPointerException e) {
            Logger.logMessage("Error retrieving remind time interval, defaulting to 5 seconds: " + e.getMessage(), Logger.LogLevel.ERROR);
            return 5; // Default to 5 minutes
        }
    }

    private int getConfigValue(String key, int defaultValue) {
        try {
            JsonObject logService = getLogServiceConfig();
            JsonElement value = logService.get(key);

            return (value != null && value.isJsonPrimitive()) ? value.getAsInt() : defaultValue;
        } catch (IOException | NullPointerException e) {
            Logger.logMessage("Error retrieving config value for " + key + ": " + e.getMessage(), Logger.LogLevel.ERROR);
            return defaultValue;
        }
    }

    private void loadConfig() {
        String filePath = directoryPath + filename;
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            config = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            Logger.logMessage("Failed to load configuration: " + e.getMessage(), Logger.LogLevel.ERROR);
        }
    }

    private JsonObject getLogServiceConfig() throws IOException {
        if (config == null) {
            throw new IOException("Configuration not loaded.");
        }
        return config.getAsJsonObject("LogService");
    }

    private JsonObject getReminderServiceConfig() throws IOException {
        if (config == null) {
            throw new IOException("Configuration not loaded.");
        }
        return config.getAsJsonObject("ReminderService");
    }
}