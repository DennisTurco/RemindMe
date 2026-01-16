package remindme.Json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Entities.TimeInterval;
import remindme.Json.Adapters.LocalDateTimeAdapter;
import remindme.Json.Adapters.LocalTimeAdapter;
import remindme.Json.Adapters.TimeIntervalAdapter;
import remindme.Managers.ExceptionManager;

public class JSONReminder {

    private static final Object FILE_LOCK = new Object();
    private static final Logger logger = LoggerFactory.getLogger(JSONReminder.class);

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .registerTypeAdapter(TimeInterval.class, new TimeIntervalAdapter())
        .setPrettyPrinting()
        .create();

    public static List<Remind> readRemindListFromJSON(String directoryPath, String filename) throws IOException {
        synchronized (FILE_LOCK) {

            directoryPath = validateOrResetDirectoryPath(directoryPath);
            String filePath = Paths.get(directoryPath, filename).toString();

            checkIfFileExistsAndNotEmpty(filePath);

            try (Reader reader = new FileReader(filePath)) {
                var type = new com.google.gson.reflect.TypeToken<List<Remind>>() {}.getType();
                List<Remind> reminds = GSON.fromJson(reader, type);
                return reminds != null ? reminds : new ArrayList<>();
            } catch (JsonSyntaxException ex) {
                logger.error("Invalid JSON format", ex);
                return new ArrayList<>();
            }
        }
    }

    public static void updateRemindListJSON(String directoryPath, String filename, List<Remind> reminds) {
        Path target = Paths.get(directoryPath, filename);
        Path temp = Paths.get(directoryPath, filename + ".tmp");

        synchronized (FILE_LOCK) {
            try {
                // Write to temp file first
                try (Writer writer = Files.newBufferedWriter(temp)) {
                    GSON.toJson(reminds, writer);
                }

                // Atomically replace original file
                Files.move(
                    temp,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                );

                logger.debug("Remind list written atomically");

            } catch (IOException ex) {
                logger.error("Error writing remind list atomically", ex);
                ExceptionManager.openExceptionMessage(
                    ex.getMessage(),
                    Arrays.toString(ex.getStackTrace())
                );
            }
        }
    }

    public static void deleteTempFileIfExist(String directoryPath, String filename) throws IOException {
        synchronized (FILE_LOCK) {
            Files.deleteIfExists(Paths.get(directoryPath, filename + ".tmp"));
        }
    }

    private static String validateOrResetDirectoryPath(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            logger.info("Directory of the remind list file doesn't exist, reset to default value");
            Preferences.setRemindList(Preferences.getDefaultRemindList());
            Preferences.updatePreferencesToJSON();
            directoryPath = Preferences.getRemindList().directory();
        }
        return directoryPath;
    }

    private static void checkIfFileExistsAndNotEmpty(String filePath) throws IOException {
        File file = new File(filePath);
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
    }
}