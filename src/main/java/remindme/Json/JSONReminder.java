package remindme.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import remindme.Entities.Remind;
import remindme.Entities.TimeInterval;
import remindme.Json.Adapters.LocalDateTimeAdapter;
import remindme.Json.Adapters.LocalTimeAdapter;
import remindme.Json.Adapters.TimeIntervalAdapter;

public class JSONReminder {

    private static final Logger logger = LoggerFactory.getLogger(JSONReminder.class);

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .registerTypeAdapter(TimeInterval.class, new TimeIntervalAdapter())
        .setPrettyPrinting()
        .create();

    private JSONReminder() {
    }

    /**
     * Reads a bundled, read-only reminder list from the application's own
     * classpath (e.g. a jar-embedded resource under src/main/resources)
     * instead of the filesystem. Used for data that ships with the app
     * itself, such as the suggested/example reminders, so it keeps working
     * regardless of the current working directory in a packaged install.
     */
    public static List<Remind> readRemindListFromClasspath(String resourcePath) throws IOException {
        try (InputStream in = JSONReminder.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found on classpath: " + resourcePath);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                var type = new com.google.gson.reflect.TypeToken<List<Remind>>() {}.getType();
                List<Remind> reminds = GSON.fromJson(reader, type);
                return reminds != null ? reminds : new ArrayList<>();
            } catch (JsonSyntaxException ex) {
                logger.error("Invalid JSON format", ex);
                return new ArrayList<>();
            }
        }
    }
}
