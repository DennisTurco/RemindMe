package remindme.Controllers;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;

public class TrayController {

    private static final Logger logger = LoggerFactory.getLogger(TrayController.class);

    private TrayIcon trayIcon;

    private final Runnable onOpen;
    private final Runnable onPause;
    private final Runnable onResume;
    private final Runnable onExit;

    private boolean paused = false;

    public TrayController(Runnable onOpen, Runnable onPause, Runnable onResume, Runnable onExit) {
        this.onOpen = onOpen;
        this.onPause = onPause;
        this.onResume = onResume;
        this.onExit = onExit;
    }

    public void start() {
        createHiddenIcon();
    }

    private void createHiddenIcon() {
        if (!SystemTray.isSupported()) {
            logger.warn("System tray is not supported!");
            return;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(ConfigKey.LOGO_IMG.getValue()));

        PopupMenu popup = setupAndGetPopupMenu();

        trayIcon = new TrayIcon(image, "Remind Service", popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
            logger.info("TrayIcon added");
        } catch (AWTException e) {
            logger.error("TrayIcon could not be added", e);
        }

        trayIcon.addActionListener((ActionEvent e) -> onOpen.run());

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onOpen.run();
                }
            }
        });
    }

    private PopupMenu setupAndGetPopupMenu() {
        PopupMenu popup = new PopupMenu();

        MenuItem openItem = new MenuItem(TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.OPEN_ACTION));
        MenuItem pauseItem = new MenuItem(TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.PAUSE_ACTION));
        MenuItem resumeItem = new MenuItem(TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.RESUME_ACTION));
        MenuItem exitItem = new MenuItem(TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.EXIT_ACTION));

        resumeItem.setEnabled(false);

        popup.add(openItem);
        popup.addSeparator();
        popup.add(pauseItem);
        popup.add(resumeItem);
        popup.addSeparator();
        popup.add(exitItem);

        openItem.addActionListener(e -> onOpen.run());

        pauseItem.addActionListener(e -> {
            if (!paused) {
                paused = true;
                pauseItem.setEnabled(false);
                resumeItem.setEnabled(true);
                onPause.run();
            }
        });

        resumeItem.addActionListener(e -> {
            if (paused) {
                paused = false;
                pauseItem.setEnabled(true);
                resumeItem.setEnabled(false);
                onResume.run();
            }
        });

        exitItem.addActionListener(e -> {
            onExit.run();
        });

        return popup;
    }

    public void removeTrayIcon() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
            logger.info("TrayIcon removed");
        }
    }
}
