package remindme.Services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import remindme.Dialogs.ManageRemind;
import remindme.Entities.TimeInterval;
import remindme.Enums.ExecutionMethod;
import remindme.Helpers.TimeRange;

public class TimeIntervalService {
    public static LocalDateTime getNextExecutionBasedOnMethod(ExecutionMethod method, TimeRange range, TimeInterval interval) {
        if (method == ExecutionMethod.CUSTOM_TIME_RANGE && ManageRemind.isTimeRangeValid(range.start(), range.end())) {
            return getNextExecutionByTimeIntervalFromSpecificTime(interval, range.start());
        }
        else if (method == ExecutionMethod.ONE_TIME_PER_DAY)  {
            return LocalDateTime.of(LocalDate.now(), range.start());
        }
        else {
            return getNextExecutionByTimeInterval(interval);
        }
    }

    public static LocalDateTime getNextExecutionByTimeInterval(TimeInterval timeInterval) {
        if (timeInterval == null) return null;

        return LocalDateTime.now().plusDays(timeInterval.days())
            .plusHours(timeInterval.hours())
            .plusMinutes(timeInterval.minutes());
    }

    public static LocalDateTime getNextExecutionByTimeIntervalFromSpecificTime(TimeInterval timeInterval, LocalTime timeFrom) {
        if (timeInterval == null || timeFrom == null) return null;

        // Base time: timeFrom
        LocalDateTime baseTime = LocalDateTime.of(LocalDate.now(), timeFrom)
            .plusDays(timeInterval.days())
            .plusHours(timeInterval.hours())
            .plusMinutes(timeInterval.minutes());

        // If the date is passed, posticipate by one day
        if (baseTime.isBefore(LocalDateTime.now())) {
            baseTime = baseTime.plusDays(1);
        }

        return baseTime;
    }
}
