package remindme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import remindme.Enums.ConfigKey;
import remindme.Json.JSONConfigReader;

public class Logger {
    
    private static String LOG_PATH = ConfigKey.RES_DIRECTORY_STRING.getValue() + ConfigKey.LOG_FILE_STRING.getValue();
    private static final ReentrantLock lock = new ReentrantLock();
    private static boolean consoleLoggingEnabled = true; // Toggle for console logging

    // Cached configuration
    public static JSONConfigReader configReader;

    public enum LogLevel {
        INFO, DEBUG, WARN, ERROR
    }

    public static void logMessage(String message) {
        logMessage(message, LogLevel.INFO);
    }

    public static void logMessage(String message, LogLevel level) {
        logMessage(message, level, null);
    }

    public static void logMessage(String message, LogLevel level, Throwable throwable) {
        if (!isLogLevelEnabled(level)) {
            return; // Skip logging if the level is not enabled in config
        }

        File logFile = new File(LOG_PATH);

        lock.lock();
        try {
            int maxLines = configReader.getMaxLines();
            int linesToKeep = configReader.getLinesToKeepAfterFileClear();
            
            List<String> lines = new ArrayList<>();
            if (logFile.exists()) {
                lines = Files.readAllLines(logFile.toPath());
            }

            // Keep only the most recent lines if exceeding max lines
            if (lines.size() > maxLines) {
                lines = lines.subList(lines.size() - linesToKeep, lines.size());
            }

            String formattedMessage = String.format("%s [%s] %s", LocalDateTime.now(), level, message);
            lines.add(0, formattedMessage);

            if (throwable != null) {
                lines.add("Exception: " + throwable.getClass().getName() + " - " + throwable.getMessage());
                for (StackTraceElement element : throwable.getStackTrace()) {
                    lines.add("\tat " + element.toString());
                }
            }

            writeLinesToFile(logFile, lines);

            if (consoleLoggingEnabled) {
                System.out.println(formattedMessage);
            }

        } catch (IOException ex) {
            System.err.println("Logging failed: " + ex.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private static void writeLinesToFile(File logFile, List<String> lines) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }

    private static boolean isLogLevelEnabled(LogLevel level) {
        return configReader.isLogLevelEnabled(level.name());
    }

    public static void setConsoleLoggingEnabled(boolean enabled) {
        consoleLoggingEnabled = enabled;
    }

    public static void setLogFilePath(String filepath) {
        LOG_PATH = filepath;
    }
}
