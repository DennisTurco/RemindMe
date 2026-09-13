package test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import remindme.Entities.Remind;
import remindme.Entities.TimeInterval;
import remindme.Enums.ExecutionMethod;
import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;
import remindme.Helpers.TimeRange;
import remindme.Services.SchedulingService;

class SchedulingServiceTest {

    private Remind remind(String name, boolean active, ExecutionMethod method, LocalDateTime nextExecution, TimeRange range) {
        return new Remind(
            name, "desc", 0, active, false,
            null, nextExecution, LocalDateTime.now(), LocalDateTime.now(),
            new TimeInterval(0, 1, 0),
            IconsEnum.ALERT, SoundsEnum.NO_SOUND,
            method, range, 0
        );
    }

    @Test
    void reportsAnOverduePcStartupReminder() {
        Remind overdue = remind("Overdue", true, ExecutionMethod.PC_STARTUP, LocalDateTime.now().minusMinutes(1), null);

        List<Remind> due = SchedulingService.getRemindsToExecute(List.of(overdue), 1);

        assertEquals(1, due.size());
        assertEquals("Overdue", due.get(0).getName());
    }

    @Test
    void ignoresInactiveAndNotYetDueReminders() {
        Remind inactive = remind("Inactive", false, ExecutionMethod.PC_STARTUP, LocalDateTime.now().minusMinutes(1), null);
        Remind future = remind("NotYetDue", true, ExecutionMethod.PC_STARTUP, LocalDateTime.now().plusDays(1), null);

        List<Remind> due = SchedulingService.getRemindsToExecute(List.of(inactive, future), 1);

        assertTrue(due.isEmpty());
    }

    @Test
    void onlyReportsCustomTimeRangeWhenNowIsWithinRange() {
        LocalTime now = LocalTime.now();
        TimeRange farInTheFuture = TimeRange.of(now.plusHours(1), now.plusHours(2));
        Remind outOfRange = remind("OutOfRange", true, ExecutionMethod.CUSTOM_TIME_RANGE, LocalDateTime.now().minusMinutes(1), farInTheFuture);

        List<Remind> due = SchedulingService.getRemindsToExecute(List.of(outOfRange), 1);

        assertTrue(due.isEmpty());
    }

    @Test
    void capsAtOneReminderPerTickPrioritizingOneTimePerDayOverPcStartup() {
        // Truncate to the minute so the window has a full minute of slack against the
        // service's own LocalTime.now() call a few milliseconds later.
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        TimeRange nowWindow = TimeRange.of(now, now.plusMinutes(10));

        Remind startup = remind("Startup", true, ExecutionMethod.PC_STARTUP, LocalDateTime.now().minusMinutes(1), null);
        Remind onceADay = remind("OnceADay", true, ExecutionMethod.ONE_TIME_PER_DAY, LocalDateTime.now().minusMinutes(1), nowWindow);

        List<Remind> due = SchedulingService.getRemindsToExecute(new ArrayList<>(List.of(startup, onceADay)), 1);

        assertEquals(1, due.size());
        assertEquals("OnceADay", due.get(0).getName());
    }
}
