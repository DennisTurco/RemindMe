package remindme.Entities;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import remindme.Json.JSONReminder;
import remindme.Logger;
import remindme.Managers.ExceptionManager;

public class Remind {
    private String _name;
    private String _description;
    private int _remindCount;
    private boolean _isActive;
    private boolean _isTopLevel;
    private LocalDateTime _lastExecution;
    private LocalDateTime _nextExecution;
    private LocalDateTime _creationDate;
    private LocalDateTime _lastUpdateDate;
    private TimeInterval _timeInterval;
    //private RemindIcon _icon;

    public Remind() {
        this._name = "";
        this._description = "";
        this._remindCount = 0;
        this._isActive = false;
        this._isTopLevel = false;
        this._lastExecution = null;
        this._nextExecution = null;
        this._creationDate = null;
        this._lastUpdateDate = null;
        this._timeInterval = null;
    }

    public Remind(String name, String description, int remindCount, boolean isActive, boolean isTopLevel, LocalDateTime lastExecution, LocalDateTime nextExecution, LocalDateTime creationDate, LocalDateTime lastUpdateDate, TimeInterval timeInterval) {
        this._name = name;
        this._description = description;
        this._remindCount = remindCount;
        this._isActive = isActive;
        this._isTopLevel = isTopLevel;
        this._lastExecution = lastExecution;
        this._nextExecution = nextExecution;
        this._creationDate = creationDate;
        this._lastUpdateDate = lastUpdateDate;
        this._timeInterval = timeInterval;
    }

    @Override
    public String toString() {
        return String.format("[Name: %s, Description: %s, IsActive: %s, IsTopLevel: %s, TimeInterval: %s]",
            this._name,
            this._description,
            this._isActive,
            this._isTopLevel,
            this._timeInterval != null ? this._timeInterval.toString() : ""
        );
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s",
            this._name,
            this._isActive,
            this._isTopLevel,
            this._lastExecution != null ? _lastExecution.toString() : "",
            this._nextExecution != null ? _nextExecution.toString() : "",
            this._timeInterval != null ? this._timeInterval.toString() : ""
        );
    }

    public static String getCSVHeader() {
        return "Name,Active,TopLevel,LastExecution,NextExecution,Interval (gg.HH:mm:ss)";
    }

    public static Remind getRemindByName(List<Remind> reminds, String remindName) {
        for (Remind rem : reminds) {
            if (rem.getName().equals(remindName)) {
                return rem;
            }
        }
        return null;
    }

    public static Remind getBackupByName(String remindName) {
        List<Remind> reminds;
        try {
            reminds = new JSONReminder().readRemindListFromJSON(Preferences.getRemindList().getDirectory(), Preferences.getRemindList().getFile());
            for (Remind remind : reminds) {
                if (remind.getName().equals(remindName)) {
                    return remind;
                }
            }
        } catch (IOException ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }

        return null;
    }

    public String getName() {
        return _name;
    }
    public String getDescription() {
        return _description;
    }
    public int getRemindCount() {
        return _remindCount;
    }
    public boolean isActive() {
        return _isActive;
    }
    public boolean isTopLevel() {
        return _isTopLevel;
    }
    public LocalDateTime getLastExecution() {
        return _lastExecution;
    }
    public LocalDateTime getNextExecution() {
        return _nextExecution;
    }
    public LocalDateTime getCreationDate() {
        return _creationDate;
    }
    public LocalDateTime getLastUpdateDate() {
        return _lastUpdateDate;
    }
    public TimeInterval getTimeInterval() {
        return _timeInterval;
    }

    public void setName(String name) {
        this._name = name;
    }
    public void setDescription(String description) {
        this._description = description;
    }
    public void setRemindCount(int remindCount) {
        this._remindCount = remindCount;
    }
    public void setIsActive(boolean isActive) {
        this._isActive = isActive;
    }
    public void setIsTopLevel(boolean isTopLevel) {
        this._isTopLevel = isTopLevel;
    }
    public void setLastExecution(LocalDateTime lastExecution) {
        this._lastExecution = lastExecution;
    }
    public void setNextExecution(LocalDateTime nextExecution) {
        this._nextExecution = nextExecution;
    }
    public void setCreationDate(LocalDateTime creationDate) {
        this._creationDate = creationDate;
    }
    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this._lastUpdateDate = lastUpdateDate;
    }
    public void setTimeInterval(TimeInterval timeInterval) {
        this._timeInterval = timeInterval;
    }
    
}
