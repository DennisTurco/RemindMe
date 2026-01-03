package remindme.Services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Dialogs.ReminderDialog;
import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Entities.RemindNotification;
import remindme.Enums.ExecutionMethod;
import remindme.Helpers.TimeRange;
import remindme.Json.JSONConfigReader;
import remindme.Json.JSONReminder;
import remindme.Managers.RemindManager;

public class BackgroundService {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundService.class);

    private ScheduledExecutorService scheduler;

    private final JSONConfigReader jsonConfigReader = new JSONConfigReader(remindme.Enums.ConfigKey.CONFIG_FILE_STRING.getValue(), remindme.Enums.ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Map<String, ReminderDialog> openedDialogs = new ConcurrentHashMap<>();

    public void start() throws IOException {
        if (isRunning()) {
            logger.warn("BackgroundService already running");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Remind-Background-Service"));

        RemindManager.updateAllNextExecutions();

        long intervalMinutes = jsonConfigReader.readCheckForReminderTimeInterval();

        scheduler.scheduleAtFixedRate(new RemindTask(), 0, intervalMinutes, TimeUnit.MINUTES);

        logger.info("BackgroundService started");
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
            logger.info("BackgroundService stopped");
        }

        openedDialogs.values().forEach(ReminderDialog::dispose);
        openedDialogs.clear();
    }

    public void pause() {
        paused.set(true);
        logger.info("BackgroundService paused");
    }

    public void resume() {
        paused.set(false);
        logger.info("BackgroundService resumed");
    }

    public boolean isPaused() {
        return paused.get();
    }

    private boolean isRunning() {
        return scheduler != null && !scheduler.isShutdown();
    }

    private class RemindTask implements Runnable {

        @Override
        public void run() {

            logger.debug("Background service task started");

            if (paused.get()) {
                logger.debug("Service paused, skipping execution");
                return;
            }

            try {
                List<Remind> reminds = JSONReminder.readRemindListFromJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file());

                RemindManager.reminds = reminds;

                List<Remind> toExecute = getRemindsToExecute(reminds, 1);

                if (!toExecute.isEmpty()) {
                    executeReminds(toExecute);
                } else {
                    logger.debug("No remind needed at this time");
                }

            } catch (IOException e) {
                logger.error("Error during remind check", e);
            }
        }

        private List<Remind> getRemindsToExecute(List<Remind> reminds, int max) {
            logger.debug("Checking for reminds...");
            List<Remind> result = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Remind remind : reminds) {
                if (shouldRun(remind, now)) {
                    logger.debug("Remind found to be executed: " + remind.getName());
                    result.add(remind);
                }
            }

            if (result.isEmpty()) {
                return result;
            }

            result.sort(Comparator.comparingInt(r -> ExecutionMethod.executionMethodPriority(r.getExecutionMethod())));

            return result.subList(0, Math.min(max, result.size()));
        }

        private boolean shouldRun(Remind remind, LocalDateTime now) {
            if (!remind.isActive() || remind.getNextExecution() == null) {
                return false;
            }

            LocalTime nowTime = now.toLocalTime();

            TimeRange remindRange = remind.getTimeRange();
            switch (remind.getExecutionMethod()) {
                case ONE_TIME_PER_DAY -> {
                    LocalTime from = remindRange.start();
                    TimeRange range = TimeRange.of(from, from.plusMinutes(5));
                    return remind.getNextExecution().isBefore(now) && range.contains(nowTime);
                }
                case CUSTOM_TIME_RANGE -> {
                    return remind.getNextExecution().isBefore(now) && remindRange.contains(nowTime);
                }
                case PC_STARTUP -> {
                    return remind.getNextExecution().isBefore(now);
                }
            }

            return false;
        }

        private void executeReminds(List<Remind> reminds) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                for (Remind remind : reminds) {

                    String key = remind.getName();

                    ReminderDialog old = openedDialogs.remove(key);
                    if (old != null) {
                        old.dispose();
                    }

                    ReminderDialog dialog = new ReminderDialog(null, false, new RemindNotification(remind), false);

                    dialog.setVisible(true);
                    openedDialogs.put(key, dialog);
                }
            });
        }
    }
}
