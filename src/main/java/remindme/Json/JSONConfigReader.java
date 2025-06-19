package remindme.Json;

import com.google.gson.*;

import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONConfigReader {
    private static final Logger logger = LoggerFactory.getLogger(JSONConfigReader.class);

    private final String filename;
    private final String directoryPath;
    private JsonObject config;

    public JSONConfigReader(String filename, String directoryPath) {
        this.filename = filename;
        this.directoryPath = directoryPath;
        loadConfig(); // Load configuration at instantiation
    }

    public boolean isMenuItemEnabled(String menuItem) {
        if (config == null) {
            logger.error("Configuration not loaded. Cannot check menu items");
            return false;
        }

        JsonObject menuService = config.getAsJsonObject("MenuItems");
        if (menuService != null) {
            JsonElement isEnabled = menuService.get(menuItem);
            return isEnabled != null && isEnabled.getAsBoolean();
        }
        return true; // Default to true
    }

    // return seconds to wait after next check
    public int readCheckForReminderTimeInterval() throws IOException {
        try {
            JsonObject reminderService = getReminderServiceConfig();
            JsonElement interval = reminderService.get("value");

            // if the interval is null, set to default of 5 minutes
            int timeInterval = (interval != null) ? interval.getAsInt() : 5;

            logger.info("Time interval set to " + timeInterval + " seconds");
            return timeInterval;
        } catch (NullPointerException e) {
            logger.error("Error retrieving remind time interval, defaulting to 5 seconds: " + e.getMessage(), e);
            return 5; // Default to 5 minutes
        }
    }

    private void loadConfig() {
        String filePath = directoryPath + filename;
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            config = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            logger.error("Failed to load configuration: " + e.getMessage(), e);
        }
    }

    private JsonObject getReminderServiceConfig() throws IOException {
        if (config == null) {
            throw new IOException("Configuration not loaded.");
        }
        return config.getAsJsonObject("ReminderService");
    }
}