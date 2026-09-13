package remindme.Services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import remindme.Entities.Remind;
import remindme.Enums.ExecutionMethod;
import remindme.Helpers.TimeRange;

/**
 * Mirrors remindme.Services.BackgroundService.RemindTask (shouldRun /
 * getRemindsToExecute): decides which reminders are due right now. Unlike
 * the old Swing app, this is stateless and called on-demand by the API's
 * GET /reminders/due, once per poll from the Electron frontend, instead of
 * running its own internal ScheduledExecutorService.
 */
public final class SchedulingService {

    private SchedulingService() {
    }

    public static List<Remind> getRemindsToExecute(List<Remind> reminds, int max) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime();

        List<Remind> candidates = new ArrayList<>();
        for (Remind remind : reminds) {
            if (shouldRun(remind, now, nowTime)) {
                candidates.add(remind);
            }
        }

        candidates.sort(Comparator.comparingInt(r -> ExecutionMethod.executionMethodPriority(r.getExecutionMethod())));

        return candidates.subList(0, Math.min(max, candidates.size()));
    }

    private static boolean shouldRun(Remind remind, LocalDateTime now, LocalTime nowTime) {
        if (!remind.isActive() || remind.getNextExecution() == null) {
            return false;
        }
        if (!remind.getNextExecution().isBefore(now)) {
            return false;
        }

        TimeRange range = remind.getTimeRange();
        return switch (remind.getExecutionMethod()) {
            case ONE_TIME_PER_DAY -> {
                if (range == null) yield false;
                LocalTime from = range.start();
                TimeRange window = TimeRange.of(from, from.plusMinutes(5));
                yield window.contains(nowTime);
            }
            case CUSTOM_TIME_RANGE -> range != null && range.contains(nowTime);
            case PC_STARTUP -> true;
        };
    }
}
