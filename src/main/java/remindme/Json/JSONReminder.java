package remindme.Json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import remindme.Logger;
import remindme.Managers.ExceptionManager;

public class JSONReminder {
    public List<Remind> readRemindListFromJSON(String directoryPath, String filename) throws IOException {
        List<Remind> remindList = new ArrayList<>();
    
        // Check if the directory is correct, otherwise reset to default
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            Logger.logMessage("Directory of the remind list file doesn't exist, reset to default value.", Logger.LogLevel.INFO);
            Preferences.setRemindList(Preferences.getDefaultRemindList());
            Preferences.updatePreferencesToJSON();
            directoryPath = Preferences.getRemindList().getDirectory();
        }
    
        String filePath = directoryPath + filename;
        File file = new File(filePath);
    
        // Check if the file exists and is not empty
        if (!file.exists()) {
            file.createNewFile();
            Logger.logMessage("New remind list created with name: " + filePath, Logger.LogLevel.INFO);
        }
        if (file.length() == 0) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]");
                Logger.logMessage("File initialized with empty JSON array: []", Logger.LogLevel.INFO);
            } catch (IOException e) {
                Logger.logMessage("Error initializing file: " + e.getMessage(), Logger.LogLevel.ERROR, e);
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

                int countValue = remindObj.has("count") ? remindObj.get("count").getAsInt() : 0;
    
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
                    TimeInterval.getTimeIntervalFromString(timeIntervalStr)
                ));
            }
    
        } catch (IOException | JsonSyntaxException | NullPointerException ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
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

                updatedRemindArray.add(remindObject);
            }

            // Write the JSON array to the file
            gson.toJson(updatedRemindArray, writer);
        } catch (IOException ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
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
                    break;
                }
            }

            // Write updated list back to the JSON file
            try (Writer writer = new FileWriter(filePath)) {
                gson.toJson(remindList, writer);
            } catch (IOException ex) {
                ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            }

        } catch (IOException ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        } catch (JsonSyntaxException ex) {
            Logger.logMessage("Invalid JSON format: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}