package remindme.Json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import remindme.Entities.User;
import remindme.Managers.ExceptionManager;

public class JsonUser {
    private static final Logger logger = LoggerFactory.getLogger(JsonUser.class);

    public static User readUserFromJson(String filename, String directoryPath) throws IOException {
        User user = null;
        String filePath = directoryPath + File.separator + filename;
        File file = new File(filePath);

        // Check if the file exists and is not empty
        if (!file.exists() || file.length() == 0) {
            logger.warn("User file doesn't exist or is empty");
            return null;
        }

        try (Reader reader = new FileReader(filePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Check if "name" and "surname" fields are present and not empty
            // I don't care if email is empty. This is not a probelm
            String name = jsonObject.has("name") && !jsonObject.get("name").getAsString().isBlank()
                    ? jsonObject.get("name").getAsString()
                    : null;
            String surname = jsonObject.has("surname") && !jsonObject.get("surname").getAsString().isBlank()
                    ? jsonObject.get("surname").getAsString()
                    : null;
            String email = jsonObject.has("email") && !jsonObject.get("email").getAsString().isBlank()
                    ? jsonObject.get("email").getAsString()
                    : null;

            // Return null if either field is null
            if (name == null || surname == null) {
                logger.info("User data is incomplete: name or surname is missing");
                return null;
            }

            // Create and return a User object
            user = new User(name, surname, email);
            return user;
        } catch (JsonSyntaxException | NullPointerException ex) {
            logger.error("An error occurred while parsing the user JSON: " + ex.getMessage(), ex);
            return null;
        }
    }

    public static void writeUserToJson(User user, String filename, String directoryPath) {
        String filePath = directoryPath + File.separator + filename;
        File file = new File(filePath);

        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();

            // Populate the JSON object
            jsonObject.addProperty("name", user.name != null ? user.name : "");
            jsonObject.addProperty("surname", user.surname != null ? user.surname : "");
            jsonObject.addProperty("email", user.email != null ? user.email : "");

            // Write JSON to file
            writer.write(gson.toJson(jsonObject));
            logger.info("User successfully written to JSON user file with data: " + user.toString());
        } catch (IOException ex) {
            logger.error("An error occurred while writing the user JSON: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}