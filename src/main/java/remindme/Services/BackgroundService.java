package remindme.Services;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Enums.ConfigKey;
import remindme.GUI.MainGUI;
import remindme.Json.JSONConfigReader;
import remindme.Json.JSONReminder;

public class BackgroundService {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundService.class);

    private ScheduledExecutorService scheduler;
    private final JSONReminder json = new JSONReminder();
    private final JSONConfigReader jsonConfig = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
    private TrayIcon trayIcon = null;
    private MainGUI guiInstance = null;

    public void startService() throws IOException {
        if (trayIcon == null) {
            createHiddenIcon();
        }
        
        scheduler = Executors.newSingleThreadScheduledExecutor();
        long interval = jsonConfig.readCheckForReminderTimeInterval();
        scheduler.scheduleAtFixedRate(new RemindTask(), 0, interval, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopService));
    }

    public void stopService() {
        logger.debug("Stopping remind service");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            logger.info("Background service stopped");
        }
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    private void createHiddenIcon() {
        if (!SystemTray.isSupported()) {
            logger.warn("System tray is not supported!");
            return;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(ConfigKey.LOGO_IMG.getValue()));
        SystemTray tray = SystemTray.getSystemTray();
        PopupMenu popup = new PopupMenu();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener((ActionEvent e) -> {
            stopService(); // close the backup service
            System.exit(0);
        });
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "Remind Service", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
            logger.info("TrayIcon added");
        } catch (AWTException e) {
            logger.error("TrayIcon could not be added: " + e.getMessage(), e);
        }

        // Listener for click to tray icon
        trayIcon.addActionListener((ActionEvent e) -> {
            javax.swing.SwingUtilities.invokeLater(this::showMainGUI); // show the GUI
        });

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    showMainGUI(); // left button mouse
                }
            }
        });
    }

    private void showMainGUI() {
        logger.info("Showing the GUI");
        
        if (guiInstance == null) {
            guiInstance = new MainGUI();
            guiInstance.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            guiInstance.setVisible(true);
        } else {
            guiInstance.setVisible(true);
            guiInstance.toFront();
            guiInstance.requestFocus();
            if (guiInstance.getState() == Frame.ICONIFIED) {
                guiInstance.setState(Frame.NORMAL);
            }
        }
    }

    class RemindTask implements Runnable {
        @Override
        public void run() {
            logger.debug("Checking for reminds...");
            try {
                List<Remind> reminds = json.readRemindListFromJSON(Preferences.getRemindList().getDirectory(), Preferences.getRemindList().getFile());
                List<Remind> needsRemind = getRemindsToDo(reminds, 1);
                if (needsRemind != null && !needsRemind.isEmpty()) {
                    logger.info("Start remind process.");
                    executeReminds(needsRemind);
                } else {
                    logger.debug("No remind needed at this time.");
                }
            } catch (IOException ex) {
                logger.error("An error occurred: " + ex.getMessage(), ex);
            }
        }

        private List<Remind> getRemindsToDo(List<Remind> reminds, int maxRemindsToAdd) {
            List<Remind> remindsToDo = new ArrayList<>();

            for (Remind remind : reminds) {

                if (maxRemindsToAdd > 0 && remind.isActive() && remind.getNextExecution() != null && remind.getNextExecution().isBefore(LocalDateTime.now())) {
                    remindsToDo.add(remind);
                    maxRemindsToAdd--;
                }
            }
            return remindsToDo;
        }

        private void executeReminds(List<Remind> reminds) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                for (Remind remind : reminds) {
                    // open notification
                }
            });
        }
    }
}
