package remindme.Json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Entities.TimeInterval;
import remindme.Enums.ExecutionMethod;
import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;
import remindme.Managers.ExceptionManager;

public class JSONReminder {

    private static final Logger logger = LoggerFactory.getLogger(JSONReminder.class);

    public List<Remind> readRemindListFromJSON(String directoryPath, String filename) throws IOException {
        List<Remind> remindList = new ArrayList<>();

        // Check if the directory is correct, otherwise reset to default
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            logger.info("Directory of the remind list file doesn't exist, reset to default value");
            Preferences.setRemindList(Preferences.getDefaultRemindList());
            Preferences.updatePreferencesToJSON();
            directoryPath = Preferences.getRemindList().directory();
        }

        String filePath = directoryPath + filename;
        File file = new File(filePath);

        // Check if the file exists and is not empty
        if (!file.exists()) {
            file.createNewFile();
            logger.info("New remind list created with name: " + filePath);
        }
        if (file.length() == 0) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]");
                logger.info("File initialized with empty JSON array: []");
            } catch (IOException e) {
                logger.error("Error initializing file: " + e.getMessage(), e);
                throw e;
            }
        }

        try (Reader reader = new FileReader(filePath)) {
            JsonArray remindArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : remindArray) {
                JsonObject remindObj = element.getAsJsonObject();

                String nameValue = getStringOrNull(remindObj, "name");
                String descriptionValue = getStringOrNull(remindObj, "description");
                String lastExecutionStr = getStringOrNull(remindObj, "lastExecution");
                String nextExecutionStr = getStringOrNull(remindObj, "nextExecution");
                String creationDateStr = getStringOrNull(remindObj, "creationDate");
                String lastUpdateDateStr = getStringOrNull(remindObj, "lastUpdateDate");
                String timeIntervalStr = getStringOrNull(remindObj, "timeInterval");
                IconsEnum icon = IconsEnum.getIconbyName(getStringOrNull(remindObj, "icon"));
                SoundsEnum sound = SoundsEnum.getSoundbyName(getStringOrNull(remindObj, "sound"));
                ExecutionMethod executionMethod = ExecutionMethod.getExecutionMethodbyName(getStringOrNull(remindObj, "executionMethod"));
                String timeFromStr = getStringOrNull(remindObj, "timeFrom");
                String timeToStr = getStringOrNull(remindObj, "timeTo");

                int countValue = remindObj.has("count") ? remindObj.get("count").getAsInt() : 0;
                int maxExecutionPerDayValue = remindObj.has("maxPerDay") ? remindObj.get("maxPerDay").getAsInt() : 0;

                Boolean isActiveValue = remindObj.has("isActive") && !remindObj.get("isActive").isJsonNull()
                    ? remindObj.get("isActive").getAsBoolean()
                    : null;

                Boolean isTopLevelValue = remindObj.has("isTopLevel") && !remindObj.get("isTopLevel").isJsonNull()
                    ? remindObj.get("isTopLevel").getAsBoolean()
                    : null;

                LocalDateTime lastExecutionValue = lastExecutionStr != null ? LocalDateTime.parse(lastExecutionStr) : null;
                LocalDateTime nextExecutionValue = nextExecutionStr != null ? LocalDateTime.parse(nextExecutionStr) : null;
                LocalDateTime creationDateValue = creationDateStr != null ? LocalDateTime.parse(creationDateStr) : null;
                LocalDateTime lastUpdateDateValue = lastUpdateDateStr != null ? LocalDateTime.parse(lastUpdateDateStr) : null;
                LocalTime timeFromValue = timeFromStr != null ? LocalTime.parse(timeFromStr) : null;
                LocalTime timeToValue = timeToStr != null ? LocalTime.parse(timeToStr) : null;

                if (icon == null)
                    icon = IconsEnum.getDefaultIcon();

                if (sound == null)
                    sound = SoundsEnum.getDefaultSound();

                remindList.add(new Remind(
                    nameValue,
                    descriptionValue,
                    countValue,
                    isActiveValue,
                    isTopLevelValue,
                    lastExecutionValue,
                    nextExecutionValue,
                    creationDateValue,
                    lastUpdateDateValue,
                    TimeInterval.getTimeIntervalFromString(timeIntervalStr),
                    icon,
                    sound,
                    executionMethod,
                    timeFromValue,
                    timeToValue,
                    maxExecutionPerDayValue
                ));
            }

        } catch (IOException | JsonSyntaxException | NullPointerException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
        return remindList;
    }

    // Helper method to safely retrieve a string or null
    private String getStringOrNull(JsonObject obj, String property) {
        return obj.has(property) && !obj.get(property).isJsonNull() ? obj.get(property).getAsString() : null;
    }

    public void updateRemindListJSON(String directoryPath, String filename, List<Remind> reminds) {
        String filePath = directoryPath + filename;

        try (Writer writer = new FileWriter(filePath)) {
            // Use Gson to convert the list of reminds into a JSON array
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonArray updatedRemindArray = new JsonArray();

            for (Remind remind : reminds) {
                JsonObject remindObject = new JsonObject();
                remindObject.addProperty("name", remind.getName());
                remindObject.addProperty("description", remind.getDescription());
                remindObject.addProperty("count", remind.getRemindCount());
                remindObject.addProperty("isActive", remind.isActive());
                remindObject.addProperty("isTopLevel", remind.isTopLevel());
                remindObject.addProperty("lastExecution", remind.getLastExecution() != null ? remind.getLastExecution().toString() : null);
                remindObject.addProperty("nextExecution", remind.getNextExecution() != null ? remind.getNextExecution().toString() : null);
                remindObject.addProperty("timeInterval", remind.getTimeInterval() != null ? remind.getTimeInterval().toString() : null);
                remindObject.addProperty("creationDate", remind.getCreationDate() != null ? remind.getCreationDate().toString() : null);
                remindObject.addProperty("lastUpdateDate", remind.getLastUpdateDate() != null ? remind.getLastUpdateDate().toString() : null);
                remindObject.addProperty("icon", remind.getIcon().getIconName());
                remindObject.addProperty("sound", remind.getSound().getSoundName());
                remindObject.addProperty("executionMethod", remind.getExecutionMethod().getExecutionMethodName());
                remindObject.addProperty("timeFrom", remind.getTimeFrom() != null ? remind.getTimeFrom().toString() : null);
                remindObject.addProperty("timeTo", remind.getTimeTo() != null ? remind.getTimeTo().toString() : null);
                remindObject.addProperty("maxPerDay", remind.getMaxExecutionsPerDay());

                updatedRemindArray.add(remindObject);
            }

            // Write the JSON array to the file
            gson.toJson(updatedRemindArray, writer);
        } catch (IOException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public void updateSingleRemindInJSON(String directoryPath, String filename, Remind updatedRemind) {
        String filePath = directoryPath + filename;

        try (Reader reader = new FileReader(filePath)) {
            // Parse JSON file into a list of Remind objects using Gson
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type listType = new TypeToken<List<JsonObject>>() {}.getType();
            List<JsonObject> remindList = gson.fromJson(reader, listType);

            // Find and update the specific remind
            for (JsonObject remindObject : remindList) {
                String remindName = remindObject.get("remind_name").getAsString();
                if (remindName.equals(updatedRemind.getName())) {
                    remindObject.addProperty("description", updatedRemind.getDescription());
                    remindObject.addProperty("count", updatedRemind.getRemindCount());
                    remindObject.addProperty("isActive", updatedRemind.isActive());
                    remindObject.addProperty("isTopLevel", updatedRemind.isTopLevel());
                    remindObject.addProperty("lastExecution", updatedRemind.getLastExecution() != null ? updatedRemind.getLastExecution().toString() : null);
                    remindObject.addProperty("nextExecution", updatedRemind.getNextExecution() != null ? updatedRemind.getNextExecution().toString() : null);
                    remindObject.addProperty("timeInterval", updatedRemind.getTimeInterval() != null ? updatedRemind.getTimeInterval().toString() : null);
                    remindObject.addProperty("creationDate", updatedRemind.getCreationDate() != null ? updatedRemind.getCreationDate().toString() : null);
                    remindObject.addProperty("lastUpdateDate", updatedRemind.getLastUpdateDate() != null ? updatedRemind.getLastUpdateDate().toString() : null);
                    remindObject.addProperty("icon", updatedRemind.getIcon().getIconName());
                    remindObject.addProperty("sound", updatedRemind.getSound().getSoundName());
                    remindObject.addProperty("executionMethod", updatedRemind.getExecutionMethod().getExecutionMethodName());
                    remindObject.addProperty("timeFrom", updatedRemind.getTimeFrom() != null ? updatedRemind.getTimeFrom().toString() : null);
                    remindObject.addProperty("timeTo", updatedRemind.getTimeTo() != null ? updatedRemind.getTimeTo().toString() : null);
                    remindObject.addProperty("maxPerDay", updatedRemind.getMaxExecutionsPerDay());
                    break;
                }
            }

            // Write updated list back to the JSON file
            try (Writer writer = new FileWriter(filePath)) {
                gson.toJson(remindList, writer);
            } catch (IOException ex) {
                logger.error("An error occurred: " + ex.getMessage(), ex);
                ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            }
        } catch (IOException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        } catch (JsonSyntaxException ex) {
            logger.error("Invalid JSON format: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}