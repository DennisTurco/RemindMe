package remindme.Sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens (and creates if needed) the SQLite database backing the reminder
 * list. Replaces the old Json/JSONReminder.java file-based store: the
 * headless API process is the sole owner of this file, so SQLite gives real
 * transactional semantics instead of the atomic-rename dance JSON needed.
 */
public final class Database {

    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private Database() {
    }

    public static Connection open(String dbFilePath) throws SQLException {
        File dbFile = new File(dbFilePath);
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA foreign_keys = ON");
        }

        migrate(connection);
        logger.info("SQLite database opened at " + dbFile.getAbsolutePath());
        return connection;
    }

    private static void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS reminders (
                    name TEXT PRIMARY KEY,
                    description TEXT NOT NULL DEFAULT '',
                    remindCount INTEGER NOT NULL DEFAULT 0,
                    isActive INTEGER NOT NULL DEFAULT 0,
                    isTopLevel INTEGER NOT NULL DEFAULT 0,
                    lastExecution TEXT,
                    nextExecution TEXT,
                    creationDate TEXT,
                    lastUpdateDate TEXT,
                    timeIntervalDays INTEGER,
                    timeIntervalHours INTEGER,
                    timeIntervalMinutes INTEGER,
                    icon TEXT NOT NULL DEFAULT 'ALERT',
                    sound TEXT NOT NULL DEFAULT 'NO_SOUND',
                    executionMethod TEXT NOT NULL DEFAULT 'PC_STARTUP',
                    timeRangeStart TEXT,
                    timeRangeEnd TEXT,
                    maxExecutionPerDay INTEGER NOT NULL DEFAULT 0
                )
                """);
        }
    }
}
