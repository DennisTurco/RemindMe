package remindme.GUI.Controllers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Dialogs.ManageRemind;
import remindme.Dialogs.PreferencesDialog;
import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.GUI.MainGUI;
import remindme.Managers.ExceptionManager;
import remindme.Managers.ThemeManager;
import remindme.Services.RemindService;
import remindme.Table.TableDataManager;

public final class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private static MainGUI main; // static because i want to save a single instance across the program
    private static RemindService remindService; // static because i want to save a single instance across the program

    public MainController(MainGUI mainGui, RemindService service) {
        main = mainGui;
        remindService = service;
    }

    public static void updateRemindAfterShow(String remindName) {
        RemindService.updateRemindAfterShow(remindName);
        updateTable();
    }

    public void renameRemind(Remind remind) {
        logger.info("Event --> remind renaming");

        String remindName = insertAndGetRemindNameViaDialog(RemindService.getReminds(), remind.getName(), false);
        if (remindName == null || remindName.isEmpty()) return;

        remindService.renameRemind(remind, remindName);
        updateTable();
    }

    public void removeReminder(Remind remindByName, boolean b) {
        logger.info("Event --> removing reminder");
        remindService.removeReminder(remindByName, b);
        updateTable();
    }

    public void removeReminder(int i, boolean b) {
        logger.info("Event --> removing reminder");
        remindService.removeReminder(i, b);
        updateTable();
    }

    public void duplicateReminder(Remind remindByName) {
        remindService.duplicateReminder(remindByName);
        updateTable();
    }

    public void switchTopLevelState(Remind remind, boolean newState) {
        logger.info("Event --> changing state for top level popup to: " + newState);
        remindService.switchTopLevelState(remind, newState);
        updateTable();
    }

    public void switchActiveState(Remind remind, boolean newState) {
        logger.info("Event --> changing state for active popup to: " + newState);
        remindService.switchActiveState(remind, newState);
        updateTable();
    }

    public void editRemindViaDialog(Remind remind) {
        logger.info("Event --> editing reminder \"" + remind.getName() + "\"");

        ManageRemind dialog = new ManageRemind(main, true, TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.EDIT_TITLE), TranslationCategory.GENERAL.getTranslation(TranslationKey.SAVE_BUTTON), remind);
        Remind updatedRemind = retriveRemindUpdatedByDialog(dialog);
        if (updatedRemind == null) return;

        remindService.editRemindByRemind(remind, updatedRemind);

        updateTable();
    }

    public void addReminderViaDialog() {
        logger.info("Event --> adding new reminder");

        ManageRemind manage = new ManageRemind(main, true, TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.CREATE_TITLE), TranslationCategory.GENERAL.getTranslation(TranslationKey.ADD_BUTTON));

        Remind remind = retriveRemindInsertedByDialog(manage);
        if (remind == null) return;

        remindService.addRemindAndReload(remind);
        updateTable();
    }

    public static void updateTable() {
        if (MainGUI.model != null)
            TableDataManager.updateTableWithNewRemindList(RemindService.getReminds(), RemindService.formatter);
    }

    public void reloadPreferences() {
        logger.info("Reloading preferences");

        loadLanguage();
        loadTheme();

        main.setSvgImages();
    }

    public void openPreferencesDialog() {
        logger.info("Event --> opening preferences dialog");

        PreferencesDialog prefs = new PreferencesDialog(main, true, this);
        prefs.setVisible(true);
    }

    private void loadLanguage() {
        try {
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
            main.setTranslations();
        } catch (IOException ex) {
            logger.error("An error occurred during reloading preferences operation: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private void loadTheme() {
        ThemeManager.updateThemeFrame(main);
        ThemeManager.refreshPopup(main.getTablePopup());
    }

    private static Remind retriveRemindInsertedByDialog(ManageRemind dialog) {
        Remind remind;

        do {
            dialog.setVisible(true);
            remind = dialog.getRemindInserted();
            if (remind == null) return null;

            if (dialog.isClosedOk()) {
                if (remind.getName().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_EMPTY_REMIND_NAME), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                } else if (remindService.isRemindNameDuplicated(remind.getName())) {
                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_DUPLICATED_REMIND), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_TITLE), JOptionPane.ERROR_MESSAGE);
                } else if (!dialog.isTimeRangeValid()) {
                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_TIME_RANGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                } else {
                    return remind;
                }
            }
        } while (true);
    }

    public String[] getColumnTranslations() {
        String[] columnNames = {
            TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.ICON_COLUMN),
            TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.NAME_COLUMN),
            TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.IS_ACTIVE_COLUMN),
            TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.IS_TOP_LEVEL_COLUMN),
            TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.LAST_EXECUTION_COLUMN),
            TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.NEXT_EXECUTION_COLUMN),
            TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.TIME_INTERVAL_COLUMN)
        };
        return columnNames;
    }

    public String pathSearchWithFileChooser(boolean allowFiles) {
        logger.info("Event --> File chooser");

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        if (allowFiles)
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        else
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = jfc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();

            if (selectedFile.isDirectory()) {
                logger.info("You selected the directory: " + selectedFile);
            } else if (selectedFile.isFile()) {
                logger.info("You selected the file: " + selectedFile);
            }

            return selectedFile.toString();
        }

        return null;
    }

    public MainGUI getMain() {
        return main;
    }

    private static Remind retriveRemindUpdatedByDialog(ManageRemind dialog) {
        Remind updatedRemind;
        do {
            dialog.setVisible(true);
            updatedRemind = dialog.getRemindInserted();

            if (updatedRemind == null) return null;

            if (!dialog.isTimeRangeValid() && dialog.isClosedOk()) {
                JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_TIME_RANGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            } else {
                return updatedRemind;
            }
        } while (true);
    }

    private String insertAndGetRemindNameViaDialog(List<Remind> reminds, String oldName, boolean canOverwrite) {
        while (true) {
            String remindName = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_NAME_INPUT), oldName);

            // If the user cancels the operation
            if (remindName == null || remindName.trim().isEmpty()) {
                return null;
            }

            Optional<Remind> existingBackup = reminds.stream()
                .filter(b -> b.getName().equals(remindName))
                .findFirst();

            if (existingBackup.isPresent()) {
                if (canOverwrite) {
                    int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_REMIND_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (response == JOptionPane.YES_OPTION) {
                        reminds.remove(existingBackup.get());
                        return remindName;
                    }
                } else {
                    logger.warn("Remind name '{}' is already in use", remindName);
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_NAME_ALREADY_USED_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                return remindName;  // Return valid name
            }
        }
    }
}
