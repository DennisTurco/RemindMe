package remindme.Helpers;

import java.time.Duration;
import java.time.LocalTime;

public record TimeRange (LocalTime start, LocalTime end) {

    public TimeRange(LocalTime start, LocalTime end) {
        if (start == null) throw new NullPointerException("Start time cannot be null");
        if (end == null) throw new NullPointerException("End time cannot be null");
        if (end.isBefore(start)) throw new IllegalArgumentException("End time must be after start time");
        this.start = start;
        this.end = end;
    }

    public static TimeRange of(LocalTime start, LocalTime end) {
        return new TimeRange(start, end);
    }

    public boolean contains(LocalTime localTime) {
        return (localTime == start || localTime.isAfter(start)) &&
               (localTime == end || localTime.isBefore(end));
    }

    public boolean containsExclusive(LocalTime localTime) {
        return localTime.isAfter(start) && localTime.isBefore(end);
    }

    public boolean overlaps(TimeRange other) {
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    @Override
    public String toString() {
        return "TimeRange[" + start + " -> " + end + "]";
    }
}
