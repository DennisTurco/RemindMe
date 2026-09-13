package test;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import remindme.Entities.Remind;
import remindme.Entities.TimeInterval;
import remindme.Enums.ExecutionMethod;
import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;
import remindme.Sqlite.Database;
import remindme.Sqlite.ReminderRepository;

class ReminderRepositoryTest {

    @TempDir
    Path tempDir;

    private ReminderRepository repository;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        String dbPath = tempDir.resolve("reminders.db").toString();
        connection = Database.open(dbPath);
        repository = new ReminderRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    private Remind sampleRemind(String name) {
        return new Remind(
            name,
            "description",
            0,
            false,
            false,
            null,
            null,
            LocalDateTime.now(),
            LocalDateTime.now(),
            new TimeInterval(0, 1, 0),
            IconsEnum.ALERT,
            SoundsEnum.NO_SOUND,
            ExecutionMethod.PC_STARTUP,
            null,
            0
        );
    }

    @Test
    void insertsAndRetrievesAReminder() {
        repository.insert(sampleRemind("Drink water"));

        Remind found = repository.getByName("Drink water");

        assertNotNull(found);
        assertEquals("Drink water", found.getName());
        assertEquals(1, repository.getAll().size());
    }

    @Test
    void duplicatesAppendingCopyUntilNameIsFree() {
        repository.insert(sampleRemind("Task"));
        repository.insert(sampleRemind("Task_copy"));

        Remind duplicated = repository.duplicate("Task");

        assertEquals("Task_copy_copy", duplicated.getName());
        assertEquals(3, repository.getAll().size());
    }

    @Test
    void renamesAReminder() {
        repository.insert(sampleRemind("Old name"));

        repository.rename("Old name", "New name");

        assertNull(repository.getByName("Old name"));
        assertNotNull(repository.getByName("New name"));
    }

    @Test
    void removesAReminder() {
        repository.insert(sampleRemind("Gone soon"));

        repository.remove("Gone soon");

        assertNull(repository.getByName("Gone soon"));
    }

    @Test
    void computesNextExecutionWhenActivated() {
        repository.insert(sampleRemind("PC startup task"));

        repository.setActiveState("PC startup task", true);

        Remind updated = repository.getByName("PC startup task");
        assertTrue(updated.isActive());
        assertNotNull(updated.getNextExecution());
        assertTrue(updated.getNextExecution().isAfter(LocalDateTime.now()));
    }

    @Test
    void clearsNextExecutionWhenDeactivated() {
        Remind remind = sampleRemind("Task");
        repository.insert(remind);
        repository.setActiveState("Task", true);

        repository.setActiveState("Task", false);

        Remind updated = repository.getByName("Task");
        assertNull(updated.getNextExecution());
    }
}
