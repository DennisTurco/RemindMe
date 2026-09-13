package remindme.Sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Entities.Remind;
import remindme.Entities.TimeInterval;
import remindme.Enums.ExecutionMethod;
import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;
import remindme.Helpers.TimeRange;
import remindme.Services.TimeIntervalService;

/**
 * CRUD + scheduling-support operations on the reminders table. Mirrors
 * remindme.Services.RemindService (in-memory + JSONReminder file store in
 * the Java Swing app) and its TypeScript port at
 * app/electron/services/reminderRepository.ts, now backed by SQLite.
 */
public class ReminderRepository {

    private static final Logger logger = LoggerFactory.getLogger(ReminderRepository.class);

    private static final String COLUMNS = "name, description, remindCount, isActive, isTopLevel, "
        + "lastExecution, nextExecution, creationDate, lastUpdateDate, "
        + "timeIntervalDays, timeIntervalHours, timeIntervalMinutes, "
        + "icon, sound, executionMethod, timeRangeStart, timeRangeEnd, maxExecutionPerDay";

    private final Connection connection;

    public ReminderRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Remind> getAll() {
        String sql = "SELECT " + COLUMNS + " FROM reminders ORDER BY creationDate ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            return mapAll(rs);
        } catch (SQLException ex) {
            logger.error("Failed to read reminders: " + ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    public Remind getByName(String name) {
        String sql = "SELECT " + COLUMNS + " FROM reminders WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            logger.error("Failed to read reminder '" + name + "': " + ex.getMessage(), ex);
            return null;
        }
    }

    public boolean existsByName(String name) {
        return getByName(name) != null;
    }

    public List<Remind> search(String query) {
        String sql = "SELECT " + COLUMNS + " FROM reminders WHERE LOWER(name) LIKE ? ORDER BY creationDate ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + query.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapAll(rs);
            }
        } catch (SQLException ex) {
            logger.error("Failed to search reminders: " + ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    public void insert(Remind remind) {
        String sql = "INSERT INTO reminders (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindRemind(stmt, remind, 1);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to insert reminder '" + remind.getName() + "': " + ex.getMessage(), ex);
        }
    }

    public void insertMany(List<Remind> reminds) {
        for (Remind remind : reminds) {
            insert(remind);
        }
    }

    /** Full replace of every field, mirroring Remind#updateRemind (name included, so this can rename too). */
    public void update(String currentName, Remind remind) {
        String sql = "UPDATE reminders SET name=?, description=?, remindCount=?, isActive=?, isTopLevel=?, "
            + "lastExecution=?, nextExecution=?, creationDate=?, lastUpdateDate=?, "
            + "timeIntervalDays=?, timeIntervalHours=?, timeIntervalMinutes=?, "
            + "icon=?, sound=?, executionMethod=?, timeRangeStart=?, timeRangeEnd=?, maxExecutionPerDay=? "
            + "WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int nextIndex = bindRemind(stmt, remind, 1);
            stmt.setString(nextIndex, currentName);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to update reminder '" + currentName + "': " + ex.getMessage(), ex);
        }
    }

    public void remove(String name) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM reminders WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to remove reminder '" + name + "': " + ex.getMessage(), ex);
        }
    }

    public void rename(String currentName, String newName) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE reminders SET name = ?, lastUpdateDate = ? WHERE name = ?")) {
            stmt.setString(1, newName);
            stmt.setString(2, LocalDateTime.now().toString());
            stmt.setString(3, currentName);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to rename reminder '" + currentName + "': " + ex.getMessage(), ex);
        }
    }

    public Remind duplicate(String sourceName) {
        Remind source = getByName(sourceName);
        if (source == null) {
            return null;
        }

        String newName = source.getName();
        do {
            newName += "_copy";
        } while (existsByName(newName));

        LocalDateTime now = LocalDateTime.now();
        Remind duplicated = new Remind(
            newName,
            source.getDescription(),
            0,
            source.isActive(),
            source.isTopLevel(),
            source.getLastExecution(),
            source.getNextExecution(),
            now,
            now,
            source.getTimeInterval(),
            source.getIcon(),
            source.getSound(),
            source.getExecutionMethod(),
            source.getTimeRange(),
            source.getMaxExecutionsPerDay()
        );
        insert(duplicated);
        return duplicated;
    }

    public void setActiveState(String name, boolean isActive) {
        Remind remind = getByName(name);
        if (remind == null) {
            return;
        }

        LocalDateTime nextExecution = isActive
            ? TimeIntervalService.getNextExecutionBasedOnMethod(remind.getExecutionMethod(), remind.getTimeRange(), remind.getTimeInterval())
            : null;

        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE reminders SET isActive = ?, nextExecution = ? WHERE name = ?")) {
            stmt.setInt(1, isActive ? 1 : 0);
            stmt.setString(2, nextExecution != null ? nextExecution.toString() : null);
            stmt.setString(3, name);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to set active state for '" + name + "': " + ex.getMessage(), ex);
        }
    }

    public void setTopLevelState(String name, boolean isTopLevel) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE reminders SET isTopLevel = ? WHERE name = ?")) {
            stmt.setInt(1, isTopLevel ? 1 : 0);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to set top-level state for '" + name + "': " + ex.getMessage(), ex);
        }
    }

    /** Mirrors RemindService.updateRemindAfterShow: called once a notification has been (or is about to be) shown. */
    public void markShown(String name) {
        Remind remind = getByName(name);
        if (remind == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecution;
        switch (remind.getExecutionMethod()) {
            case ONE_TIME_PER_DAY -> nextExecution = LocalDateTime.of(now.toLocalDate().plusDays(1), remind.getTimeRange().start());
            case CUSTOM_TIME_RANGE -> nextExecution = TimeIntervalService.getNextExecutionByTimeIntervalFromSpecificTime(remind.getTimeInterval(), remind.getTimeRange().start());
            case PC_STARTUP -> nextExecution = TimeIntervalService.getNextExecutionByTimeInterval(remind.getTimeInterval());
            default -> nextExecution = LocalDateTime.of(now.toLocalDate().plusDays(1), remind.getTimeRange().start());
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE reminders SET lastExecution = ?, remindCount = remindCount + 1, nextExecution = ? WHERE name = ?")) {
            stmt.setString(1, now.toString());
            stmt.setString(2, nextExecution != null ? nextExecution.toString() : null);
            stmt.setString(3, name);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to mark reminder '" + name + "' as shown: " + ex.getMessage(), ex);
        }
    }

    /** Mirrors RemindService.updateAllNextExecutions, run once at startup. */
    public void recomputeAllNextExecutions() {
        LocalTime now = LocalTime.now();

        for (Remind remind : getAll()) {
            if (!remind.isActive()) {
                continue;
            }

            TimeRange range = remind.getTimeRange();
            LocalDateTime nextExecution;
            switch (remind.getExecutionMethod()) {
                case PC_STARTUP -> nextExecution = TimeIntervalService.getNextExecutionByTimeIntervalFromSpecificTime(remind.getTimeInterval(), now);
                case CUSTOM_TIME_RANGE -> {
                    LocalTime reference = range != null && range.contains(now) ? now : (range != null ? range.start() : now);
                    nextExecution = TimeIntervalService.getNextExecutionByTimeIntervalFromSpecificTime(remind.getTimeInterval(), reference);
                }
                case ONE_TIME_PER_DAY -> {
                    if (range == null) {
                        nextExecution = null;
                    } else {
                        LocalDate day = now.isBefore(range.start()) ? LocalDate.now() : LocalDate.now().plusDays(1);
                        nextExecution = LocalDateTime.of(day, range.start());
                    }
                }
                default -> nextExecution = null;
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE reminders SET nextExecution = ? WHERE name = ?")) {
                stmt.setString(1, nextExecution != null ? nextExecution.toString() : null);
                stmt.setString(2, remind.getName());
                stmt.executeUpdate();
            } catch (SQLException ex) {
                logger.error("Failed to recompute nextExecution for '" + remind.getName() + "': " + ex.getMessage(), ex);
            }
        }
    }

    /** Replaces the entire list (File > Importa elenco): mirrors switching to a different remind list file in Java. */
    public void replaceAll(List<Remind> reminds) {
        try (PreparedStatement del = connection.prepareStatement("DELETE FROM reminders")) {
            del.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failed to clear reminders before import: " + ex.getMessage(), ex);
            return;
        }
        insertMany(reminds);
    }

    private int bindRemind(PreparedStatement stmt, Remind remind, int startIndex) throws SQLException {
        int i = startIndex;
        stmt.setString(i++, remind.getName());
        stmt.setString(i++, remind.getDescription());
        stmt.setInt(i++, remind.getRemindCount());
        stmt.setInt(i++, remind.isActive() ? 1 : 0);
        stmt.setInt(i++, remind.isTopLevel() ? 1 : 0);
        stmt.setString(i++, remind.getLastExecution() != null ? remind.getLastExecution().toString() : null);
        stmt.setString(i++, remind.getNextExecution() != null ? remind.getNextExecution().toString() : null);
        stmt.setString(i++, remind.getCreationDate() != null ? remind.getCreationDate().toString() : null);
        stmt.setString(i++, remind.getLastUpdateDate() != null ? remind.getLastUpdateDate().toString() : null);
        TimeInterval interval = remind.getTimeInterval();
        if (interval != null) {
            stmt.setInt(i++, interval.days());
            stmt.setInt(i++, interval.hours());
            stmt.setInt(i++, interval.minutes());
        } else {
            stmt.setNull(i++, java.sql.Types.INTEGER);
            stmt.setNull(i++, java.sql.Types.INTEGER);
            stmt.setNull(i++, java.sql.Types.INTEGER);
        }
        stmt.setString(i++, remind.getIcon().name());
        stmt.setString(i++, remind.getSound().name());
        stmt.setString(i++, remind.getExecutionMethod().name());
        TimeRange range = remind.getTimeRange();
        stmt.setString(i++, range != null ? range.start().toString() : null);
        stmt.setString(i++, range != null ? range.end().toString() : null);
        stmt.setInt(i++, remind.getMaxExecutionsPerDay());
        return i;
    }

    private List<Remind> mapAll(ResultSet rs) throws SQLException {
        List<Remind> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs));
        }
        return result;
    }

    private Remind mapRow(ResultSet rs) throws SQLException {
        String timeRangeStart = rs.getString("timeRangeStart");
        String timeRangeEnd = rs.getString("timeRangeEnd");
        TimeRange timeRange = (timeRangeStart != null && timeRangeEnd != null)
            ? new TimeRange(LocalTime.parse(timeRangeStart), LocalTime.parse(timeRangeEnd))
            : null;

        Integer days = (Integer) rs.getObject("timeIntervalDays");
        Integer hours = (Integer) rs.getObject("timeIntervalHours");
        Integer minutes = (Integer) rs.getObject("timeIntervalMinutes");
        TimeInterval timeInterval = (days != null && hours != null && minutes != null)
            ? new TimeInterval(days, hours, minutes)
            : null;

        String lastExecution = rs.getString("lastExecution");
        String nextExecution = rs.getString("nextExecution");
        String creationDate = rs.getString("creationDate");
        String lastUpdateDate = rs.getString("lastUpdateDate");

        return new Remind(
            rs.getString("name"),
            rs.getString("description"),
            rs.getInt("remindCount"),
            rs.getInt("isActive") != 0,
            rs.getInt("isTopLevel") != 0,
            lastExecution != null ? LocalDateTime.parse(lastExecution) : null,
            nextExecution != null ? LocalDateTime.parse(nextExecution) : null,
            creationDate != null ? LocalDateTime.parse(creationDate) : null,
            lastUpdateDate != null ? LocalDateTime.parse(lastUpdateDate) : null,
            timeInterval,
            IconsEnum.valueOf(rs.getString("icon")),
            SoundsEnum.valueOf(rs.getString("sound")),
            ExecutionMethod.valueOf(rs.getString("executionMethod")),
            timeRange,
            rs.getInt("maxExecutionPerDay")
        );
    }
}
