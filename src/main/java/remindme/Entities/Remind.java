package remindme.Entities;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Enums.ExecutionMethod;
import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;
import remindme.Helpers.TimeRange;
import remindme.Json.JSONReminder;
import remindme.Managers.ExceptionManager;

public class Remind {
    private static final Logger logger = LoggerFactory.getLogger(Remind.class);

    private String name;
    private String description;
    private int remindCount;
    private boolean isActive;
    private boolean isTopLevel;
    private LocalDateTime lastExecution;
    private LocalDateTime nextExecution;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
    private TimeInterval timeInterval;
    private IconsEnum icon;
    private SoundsEnum sound;
    private ExecutionMethod executionMethod;
    private TimeRange timeRange;
    private int maxExecutionPerDay;

    public Remind() {
        this.name = "";
        this.description = "";
        this.remindCount = 0;
        this.isActive = false;
        this.isTopLevel = false;
        this.lastExecution = null;
        this.nextExecution = null;
        this.creationDate = null;
        this.lastUpdateDate = null;
        this.timeInterval = null;
        this.icon = IconsEnum.ALERT;
        this.sound = SoundsEnum.NO_SOUND;
        this.executionMethod = ExecutionMethod.PC_STARTUP;
        this.timeRange = null;
        this.maxExecutionPerDay = 0;
    }

    public Remind(String name, String description, int remindCount, boolean isActive, boolean isTopLevel, LocalDateTime lastExecution, LocalDateTime nextExecution, LocalDateTime creationDate, LocalDateTime lastUpdateDate, TimeInterval timeInterval, IconsEnum icon, SoundsEnum sound, ExecutionMethod executionMethod, TimeRange timeRange, int maxExecutionsPerDay) {
        this.name = name;
        this.description = description;
        this.remindCount = remindCount;
        this.isActive = isActive;
        this.isTopLevel = isTopLevel;
        this.lastExecution = lastExecution;
        this.nextExecution = nextExecution;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.timeInterval = timeInterval;
        this.icon = icon;
        this.sound = sound;
        this.executionMethod = executionMethod;
        this.timeRange = timeRange;
        this.maxExecutionPerDay = maxExecutionsPerDay;
    }

    public void updateReming(Remind newRemind) {
        this.name = newRemind.getName();
        this.description = newRemind.getDescription();
        this.remindCount = newRemind.getRemindCount();
        this.isActive = newRemind.isActive();
        this.isTopLevel = newRemind.isTopLevel();
        this.lastExecution = newRemind.getLastExecution();
        this.nextExecution = newRemind.getNextExecution();
        this.creationDate = newRemind.getCreationDate();
        this.lastUpdateDate = newRemind.getLastUpdateDate();
        this.timeInterval = newRemind.getTimeInterval();
        this.icon = newRemind.getIcon();
        this.sound = newRemind.getSound();
        this.executionMethod = newRemind.getExecutionMethod();
        this.timeRange = newRemind.getTimeRange();
    }

    @Override
    public String toString() {
        return String.format("[Name: %s, Description: %s, IsActive: %s, IsTopLevel: %s, TimeInterval: %s, ExecutionMethod: %s, TimeFrom: %s, TimeTo: %s]",
            this.name,
            this.description,
            this.isActive,
            this.isTopLevel,
            this.timeInterval != null ? this.timeInterval.toString() : "",
            this.executionMethod.getExecutionMethodName(),
            getTimeFromString(),
            getTimeToString()
        );
    }

    public String[] toArrayString() {
        return new String[] {
            name,
            String.valueOf(isActive),
            String.valueOf(isTopLevel),
            lastExecution != null ? lastExecution.toString() : "",
            nextExecution != null ? nextExecution.toString() : "",
            timeInterval != null ? timeInterval.toString() : "",
            executionMethod.getExecutionMethodName(),
            getTimeFromString(),
            getTimeToString()
        };
    }

    public static String getCSVHeader() {
        return "Name,Active,TopLevel,LastExecution,NextExecution,Interval (gg.HH:mm),ExecutionMethod,TimeFrom,TimeTo";
    }

    public static Remind getRemindByName(List<Remind> reminds, String remindName) {
        for (Remind rem : reminds) {
            if (rem.getName().equals(remindName)) {
                return rem;
            }
        }
        return null;
    }

    public static Remind getRemindByName(String remindName) {
        List<Remind> reminds;
        try {
            reminds = JSONReminder.readRemindListFromJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file());
            for (Remind remind : reminds) {
                if (remind.getName().equals(remindName)) {
                    return remind;
                }
            }
        } catch (IOException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }

        return null;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public int getRemindCount() {
        return remindCount;
    }
    public boolean isActive() {
        return isActive;
    }
    public boolean isTopLevel() {
        return isTopLevel;
    }
    public LocalDateTime getLastExecution() {
        return lastExecution;
    }
    public LocalDateTime getNextExecution() {
        return nextExecution;
    }
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }
    public IconsEnum getIcon() {
        return icon != null ? icon : IconsEnum.getDefaultIcon();
    }
    public SoundsEnum getSound() {
        return sound != null ? sound : SoundsEnum.getDefaultSound();
    }
    public ExecutionMethod getExecutionMethod() {
        return executionMethod != null ? executionMethod : ExecutionMethod.getDefaultExecutionMethod();
    }
    public TimeRange getTimeRange() {
        return timeRange;
    }
    public String getTimeFromString() {
        return (timeRange != null && timeRange.start() != null ? timeRange.start().toString() : "");
    }

    public String getTimeToString() {
        return (timeRange != null && timeRange.end() != null ? timeRange.end().toString() : "");
    }
    public int getMaxExecutionsPerDay() {
        return maxExecutionPerDay;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setRemindCount(int remindCount) {
        this.remindCount = remindCount;
    }
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    public void setIsTopLevel(boolean isTopLevel) {
        this.isTopLevel = isTopLevel;
    }
    public void setLastExecution(LocalDateTime lastExecution) {
        this.lastExecution = lastExecution;
    }
    public void setNextExecution(LocalDateTime nextExecution) {
        this.nextExecution = nextExecution;
    }
    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }
    public void setMaxExecutionPerDay(int maxExecutionPerDay) {
        this.maxExecutionPerDay = maxExecutionPerDay;
    }
}
