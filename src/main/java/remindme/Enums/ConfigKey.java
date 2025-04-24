package remindme.Enums;

import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public enum ConfigKey {
    LOG_FILE_STRING,
    LOG_DIRECTORY_STRING,
    REMIND_LIST_FILE_STRING,
    CONFIG_FILE_STRING,
    PREFERENCES_FILE_STRING,
    USER_FILE_STRING,
    PROPERTIES_FILE_STRING,
    RESURCES_DIRECTORY_STRING,
    RES_DIRECTORY_STRING,
    LANGUAGES_DIRECTORY_STRING,
    CONFIG_DIRECTORY_STRING,
    DONATE,
    DONATE_PAYPAL_LINK,
    DONATE_BUYMEACOFFE_LINK,
    ISSUE_PAGE_LINK,
    INFO_PAGE_LINK,
    EMAIL,
    SHARD_WEBSITE,
    LOGO_IMG,
    SHARE_LINK,
    VERSION,
    GUI_WIDTH,
    GUI_HEIGHT;

    private static final Logger logger = LoggerFactory.getLogger(ConfigKey.class);
    private static final Map<ConfigKey, String> configValues = new EnumMap<>(ConfigKey.class);

    public static void loadFromJson(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            for (ConfigKey key : ConfigKey.values()) {
                if (jsonObject.has(key.name())) {
                    configValues.put(key, jsonObject.get(key.name()).getAsString());
                }
            }
        } catch (IOException ex) {
            logger.error("An error occurred when loading configs from json: " + ex.getMessage(), ex);
        }
    }

    public String getValue() {
        return configValues.get(this);
    }
}