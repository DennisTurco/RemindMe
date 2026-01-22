package remindme.Services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Helpers.TimeRange;
import remindme.Json.JSONReminder;
import remindme.Managers.ExceptionManager;

public class RemindService {
    private static final Logger logger = LoggerFactory.getLogger(RemindService.class);
    public static final DateTimeFormatter dateForfolderNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm.ss");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static List<Remind> reminds; // static because i want to save only one reference across the program

    public RemindService() {
        loadRemindListFromJson();
    }

    public static void reloadRemindList() {
        logger.info("Reloading remind list");
        JSONReminder.updateRemindListJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file(), reminds);
        loadRemindListFromJson();
    }

    public List<Remind> getSubListWithFilterResearchByString(String research) {
        List<Remind> tempReminds = new ArrayList<>();
        for (Remind remind : reminds) {
            if (remind.getName().toLowerCase().contains(research)) {
                tempReminds.add(remind);
            }
        }
        return tempReminds;
    }

    public boolean isRemindNameDuplicated(String remindName) {
        for (Remind remind : reminds) {
            if (remind.getName().equals(remindName))
                return true;
        }
        return false;
    }

    public void removeReminder(Remind remind, boolean d) {
        for (Remind rem : reminds) {
            if (remind.getName().equals(rem.getName())) {
                reminds.remove(rem);
                logger.info("Remind removed successfully: " + rem.toString());
                break;
            }
        }

        reloadRemindList();
    }

    public void removeReminder(int row, boolean d) {
        Remind remind = reminds.remove(row);
        logger.info("Remind removed successfully: " + remind.toString());
        reloadRemindList();
    }

    public void duplicateReminder(Remind remind) {
        logger.info("Event --> duplicating reminder");

        String remindName = remind.getName();
        do {
            remindName += "_copy";
        } while (isRemindNameDuplicated(remindName));


        LocalDateTime dateNow = LocalDateTime.now();
        Remind newRemind = new Remind(
            remindName,
            remind.getDescription(),
            0,
            remind.isActive(),
            remind.isTopLevel(),
            remind.getLastExecution(),
            remind.getNextExecution(),
            dateNow,
            dateNow,
            remind.getTimeInterval(),
            remind.getIcon(),
            remind.getSound(),
            remind.getExecutionMethod(),
            remind.getTimeRange(),
            remind.getMaxExecutionsPerDay()
        );
        addRemindAndReload(newRemind);
    }

    public void renameRemind(Remind remind, String remindName) {
        for (Remind rem : reminds) {
            if (remind.getName().equals(rem.getName())) {
                rem.setName(remindName);
                rem.setLastUpdateDate(LocalDateTime.now());
            }
        }
        reloadRemindList();
    }

    public static void updateRemindAfterShow(String remindName) {
        for (Remind rem : RemindService.getReminds()) {
            if (remindName.equals(rem.getName())) {
                rem.setLastExecution(LocalDateTime.now());
                rem.setRemindCount(rem.getRemindCount()+1);

                switch (rem.getExecutionMethod()) {
                    case ONE_TIME_PER_DAY -> rem.setNextExecution(LocalDateTime.of(LocalDateTime.now().toLocalDate().plusDays(1), rem.getTimeRange().start()));
                    case CUSTOM_TIME_RANGE -> rem.setNextExecution(TimeIntervalService.getNextExecutionByTimeIntervalFromSpecificTime(rem.getTimeInterval(), rem.getTimeRange().start()));
                    case PC_STARTUP -> rem.setNextExecution(TimeIntervalService.getNextExecutionByTimeInterval(rem.getTimeInterval()));
                    default -> rem.setNextExecution(LocalDateTime.of(LocalDateTime.now().toLocalDate().plusDays(1), rem.getTimeRange().start()));
                }
            }
        }
        reloadRemindList();
    }

    public void editRemindByRemind(Remind remindToUpdate, Remind updatedRemind) {
        remindToUpdate.updateRemind(updatedRemind);
        reloadRemindList();
    }

    public void switchTopLevelState(Remind remind, boolean newState) {
        for (Remind rem : RemindService.getReminds()) {
            if (remind.getName().equals(rem.getName())) {
                rem.setIsTopLevel(newState);
                break;
            }
        }
        reloadRemindList();
    }

    public void switchActiveState(Remind remind, boolean newState) {
        for (Remind rem : RemindService.getReminds()) {
            if (remind.getName().equals(rem.getName())) {
                rem.setIsActive(newState);

                if (newState) {
                    LocalDateTime nextExecution = TimeIntervalService.getNextExecutionBasedOnMethod(remind.getExecutionMethod(), remind.getTimeRange(), remind.getTimeInterval());
                    rem.setNextExecution(nextExecution);
                } else {
                    rem.setNextExecution(null);
                }

                break;
            }
        }
        reloadRemindList();
    }

    public void addRemindAndReload(Remind remind) {
        reminds.add(remind);
        reloadRemindList();
    }

    public static List<Remind> getReminds() {
        return reminds;
    }

    public static void setReminds(List<Remind> remindList) {
        reminds = remindList;
    }

    public static void setRemindsAndReload(List<Remind> remindList) {
        reminds = remindList;
        reloadRemindList();
    }

    /**
     * Called once at application startup
     */
    public static void updateAllNextExecutions() {
        logger.debug("Updating all next executions time...");
        try {
            List<Remind> tempReminds = JSONReminder.readRemindListFromJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file());

            LocalTime now = LocalTime.now();

            for (Remind remind : tempReminds) {
                if (!remind.isActive()) {
                    continue;
                }

                TimeRange range = remind.getTimeRange();
                switch (remind.getExecutionMethod()) {
                    case PC_STARTUP -> {
                        remind.setNextExecution(TimeIntervalService.getNextExecutionByTimeIntervalFromSpecificTime(remind.getTimeInterval(), now));
                    }
                    case CUSTOM_TIME_RANGE -> {
                        LocalTime reference = range.contains(now) ? now : range.start();
                        remind.setNextExecution(TimeIntervalService.getNextExecutionByTimeIntervalFromSpecificTime(remind.getTimeInterval(), reference));
                    }
                    case ONE_TIME_PER_DAY -> {
                        LocalDate today = LocalDate.now();
                        LocalDate day = now.isBefore(range.start()) ? today : today.plusDays(1);
                        remind.setNextExecution(LocalDateTime.of(day, range.start()));
                    }
                }
            }

            setRemindsAndReload(tempReminds);

            logger.debug("Next executions time updated succesfully");

        } catch (IOException e) {
            logger.error("Failed to update next executions", e);
        }
    }

    private static void loadRemindListFromJson() {
        try {
            reminds = JSONReminder.readRemindListFromJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file());
        } catch (IOException e) {
            logger.error("An error occurred while trying to get the remind list from json file: " + e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            logger.error("An error occurred: " + e.getMessage(), e);
        }
    }
}
