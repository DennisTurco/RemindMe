package remindme.Controllers;

import java.awt.Frame;
import java.io.IOException;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.GUI.MainGUI;
import remindme.Services.BackgroundService;

public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    private MainGUI guiInstance;

    private final BackgroundService backgroundService;
    private final TrayController trayController;

    public static AppController startBackgroundProcess() throws IOException {
        return new AppController();
    }

    private AppController() throws IOException {
        logger.info("Starting RemindMe application");

        this.backgroundService = new BackgroundService();

        this.trayController = new TrayController(
            this::openGui,
            backgroundService::pause,
            backgroundService::resume,
            this::exitApp
        );

        backgroundService.start();
        trayController.start();
    }

    private void openGui() {
        logger.info("Opening main GUI");

        if (guiInstance == null) {
            guiInstance = new MainGUI();
            guiInstance.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }

        guiInstance.setVisible(true);
        guiInstance.toFront();
        guiInstance.requestFocus();

        if (guiInstance.getState() == Frame.ICONIFIED) {
            guiInstance.setState(Frame.NORMAL);
        }
    }

    private void exitApp() {
        logger.info("Exiting application");

        backgroundService.stop();
        System.exit(0);
    }
}
