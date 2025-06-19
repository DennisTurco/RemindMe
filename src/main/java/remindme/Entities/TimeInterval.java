package remindme.Entities;

public class TimeInterval {
    private int days;
    private int hours;
    private int minutes;

    public TimeInterval(int days, int hours, int minutes) {
        if (days < 0) throw new IllegalArgumentException("Days cannot be negative");
        if (hours < 0 || hours > 23) throw new IllegalArgumentException("Hours must be between 0 and 23");
        if (minutes < 0 || minutes > 59) throw new IllegalArgumentException("Minutes must be between 0 and 59");

        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return days + "." + hours + ":" + minutes;
    }

    public static TimeInterval getTimeIntervalFromString(String time) {
        if (time != null && !time.matches("\\d+\\.\\d{1,2}:\\d{1,2}")) {
            throw new IllegalArgumentException("Invalid time format. Expected format: days.hours:minutes:seconds (e.g., 1.12:30)");
        }

        if (time == null) return null;

        String[] dayAndTime = time.split("\\.");
        int parsedDays = Integer.parseInt(dayAndTime[0]);

        String[] hourMinuteAndSecond = dayAndTime[1].split(":");
        int parsedHours = Integer.parseInt(hourMinuteAndSecond[0]);
        int parsedMinutes = Integer.parseInt(hourMinuteAndSecond[1]);

        if (parsedDays < 0) {
            throw new IllegalArgumentException("Days cannot be negative");
        }
        if (parsedHours < 0 || parsedHours > 23) {
            throw new IllegalArgumentException("Hours must be between 0 and 23");
        }
        if (parsedMinutes < 0 || parsedMinutes > 59) {
            throw new IllegalArgumentException("Minutes must be between 0 and 59");
        }

        return new TimeInterval(parsedDays, parsedHours, parsedMinutes);
    }

    public static TimeInterval getDefaultTimeInterval() {
        return new TimeInterval(0, 1, 0);
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }
}
