package remindme.Managers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import remindme.Entities.TimeInterval;
import remindme.Enums.ConfigKey;
import remindme.Enums.ExecutionMethod;
import remindme.Enums.TranslationLoaderEnum;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.GUI.MainGUI;
import remindme.Helpers.TimeRange;
import remindme.Json.JSONReminder;
import remindme.Table.TableDataManager;

public final class RemindManager {

    private static final Logger logger = LoggerFactory.getLogger(RemindManager.class);

    public static final DateTimeFormatter dateForfolderNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm.ss");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private JSONReminder JSON;
    private static MainGUI main; // static, cause i need only one for instance
    public static List<Remind> reminds; // static, cause i need only one for instance

    public RemindManager(MainGUI mainGui) {
        main = mainGui;
        initManager();
    }

    public RemindManager() {
        initManager();
    }

    private void initManager() {
        reminds = getReminds();
        JSON = new JSONReminder();
    }

    public void openPreferences() {
        logger.info("Event --> opening preferences dialog");

        PreferencesDialog prefs = new PreferencesDialog(main, true, this);
        prefs.setVisible(true);
    }

    public void reloadPreferences() {
        logger.info("Reloading preferences");

        loadLanguage();
        loadTheme();

        main.setSvgImages();
    }

    public void updateRemindList() {
        logger.info("Updating remind list");

        JSON.updateRemindListJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file(), reminds);

        getRemindList();

        if (MainGUI.model != null)
            TableDataManager.updateTableWithNewRemindList(reminds, formatter);
    }

    public void getRemindList() {
        try {
            reminds = JSON.readRemindListFromJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file());
        } catch (IOException e) {
            logger.error("An error occurred while trying to get the remind list from json file: " + e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            logger.error("An error occurred: " + e.getMessage(), e);
        }
    }

    private String insertAndGetRemindName(List<Remind> reminds, String oldName, boolean canOverwrite) {
        while (true) {
            String remindName = JOptionPane.showInputDialog(null, 
                TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_NAME_INPUT), oldName);

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

    public static String pathSearchWithFileChooser(boolean allowFiles) {
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

    public void researchInTable(String research) {
        List<Remind> tempReminds = new ArrayList<>();

        for (Remind remind : reminds) {
            if (remind.getName().toLowerCase().contains(research)) {
                tempReminds.add(remind);
            }
        }

        TableDataManager.updateTableWithNewRemindList(tempReminds, formatter);
    }

    private String getRemindNameByTableRow(javax.swing.JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            return (String) table.getValueAt(selectedRow, 1);
        }

        return null;
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

    ///////////////////////////////////////////////////////////////////

    public void addReminder() {
        logger.info("Event --> adding new reminder");

        ManageRemind manage = new ManageRemind(main, true,TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.CREATE_TITLE),TranslationCategory.GENERAL.getTranslation(TranslationKey.ADD_BUTTON));

        Remind remind = retriveRemindInsertedByDialog(manage);
        if (remind == null) return;

        reminds.add(remind);
        updateRemindList();
    }

    public void editRemind(Remind remind) {
        logger.info("Event --> editing reminder \"" + remind.getName() + "\"");

        ManageRemind dialog = new ManageRemind(main, true, TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.EDIT_TITLE), TranslationCategory.GENERAL.getTranslation(TranslationKey.SAVE_BUTTON), remind);

        Remind updatedRemind = retriveRemindUpdatedByDialog(dialog);
        if (updatedRemind == null) return;

        remind.updateReming(updatedRemind);
        updateRemindList();
    }

    private Remind retriveRemindInsertedByDialog(ManageRemind dialog) {
        Remind remind;

        do {
            dialog.setVisible(true);
            remind = dialog.getRemindInserted();
            if (remind == null) return null;

            if (dialog.isClosedOk()) {
                if (remind.getName().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_EMPTY_REMIND_NAME), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                } else if (isRemindNameDuplicated(remind.getName())) {
                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_DUPLICATED_REMIND), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_TITLE), JOptionPane.ERROR_MESSAGE);
                } else if (!dialog.isTimeRangeValid()) {
                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_TIME_RANGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                } else {
                    return remind;
                }
            }
        } while (true);
    }

    private Remind retriveRemindUpdatedByDialog(ManageRemind dialog) {
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

    private boolean isRemindNameDuplicated(String remindName) {
        for (Remind remind : reminds) {
            if (remind.getName().equals(remindName))
                return true;
        }
        return false;
    }

    public void removeReminder(Remind remind, boolean d) {
        logger.info("Event --> removing reminder");

        // remind list update
        for (Remind rem : reminds) {
            if (remind.getName().equals(rem.getName())) {
                reminds.remove(rem);
                logger.info("Remind removed successfully: " + rem.toString());
                break;
            }
        }

        updateRemindList();
    }

    public void removeReminder(int row, boolean d) {
        logger.info("Event --> removing reminder");

        Remind remind = reminds.remove(row);
        logger.info("Remind removed successfully: " + remind.toString());

        updateRemindList();
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

        reminds.add(newRemind);
        updateRemindList();
    }

    public void importRemindListFromJSON() {
        logger.info("Event --> importing remind list from json");
        List<Remind> newReminds = ImportExportManager.importRemindListFromJson(main, JSON, dateForfolderNameFormatter);

        // replace the current list with the imported one
        if (newReminds != null)
            reminds = newReminds;
    }

    public void exportRemindListTOJSON() {
        logger.info("Event --> exporting remind list to json");
        ImportExportManager.exportRemindListToJson();
    }

    public void exportRemindListAsPDF() {
        logger.info("Event --> exporting remind list as pdf");
        ImportExportManager.exportRemindListAsPDF(reminds, Remind.getCSVHeader());
    }

    public void exportRemindListAsCSV() {
        logger.info("Event --> exporting remind list as csv");
        ImportExportManager.exportRemindListAsCSV(reminds, Remind.getCSVHeader());
    }

    public void renameRemind(Remind remind) {
        logger.info("Event --> remind renaming");

        String remindName = insertAndGetRemindName(reminds, remind.getName(), false);
        if (remindName == null || remindName.isEmpty()) return;

        for (Remind rem : reminds) {
            if (remind.getName().equals(rem.getName())) {
                rem.setName(remindName);
                rem.setLastUpdateDate(LocalDateTime.now());
            }
        }

        updateRemindList();
    }

    public static LocalDateTime getNextExecutionBasedOnMethod(ExecutionMethod method, TimeRange range, TimeInterval interval) {
        if (method == ExecutionMethod.CUSTOM_TIME_RANGE && ManageRemind.isTimeRangeValid(range.start(), range.end())) {
            return RemindManager.getNextExecutionByTimeIntervalFromSpecificTime(interval, range.start());
        }
        else if (method == ExecutionMethod.ONE_TIME_PER_DAY)  {
            return LocalDateTime.of(LocalDate.now(), range.start());
        }
        else {
            return RemindManager.getNextExecutionByTimeInterval(interval);
        }
    }

    public static LocalDateTime getNextExecutionByTimeInterval(TimeInterval timeInterval) {
        if (timeInterval == null) return null;

        return LocalDateTime.now().plusDays(timeInterval.days())
            .plusHours(timeInterval.hours())
            .plusMinutes(timeInterval.minutes());
    }

    public static LocalDateTime getNextExecutionByTimeIntervalFromSpecificTime(TimeInterval timeInterval, LocalTime timeFrom) {
        if (timeInterval == null || timeFrom == null) return null;

        // Base time: timeFrom
        LocalDateTime baseTime = LocalDateTime.of(LocalDate.now(), timeFrom)
            .plusDays(timeInterval.days())
            .plusHours(timeInterval.hours())
            .plusMinutes(timeInterval.minutes());

        // If the date is passed, posticipate by one day
        if (baseTime.isBefore(LocalDateTime.now())) {
            baseTime = baseTime.plusDays(1);
        }

        return baseTime;
    }

    ///////////////////////////////////////////////////////////////////

    public void menuSupport() {
        logger.info("Event --> support");
        WebsiteManager.sendEmail();
    }

    public void menuWebsite() {
        logger.info("Event --> shard website");
        WebsiteManager.openWebSite(ConfigKey.SHARD_WEBSITE.getValue());
    }

    public void menuItemDonateViaBuymeacoffe() {
        logger.info("Event --> buymeacoffe donation");
        WebsiteManager.openWebSite(ConfigKey.DONATE_BUYMEACOFFE_LINK.getValue());
    }

    public void menuItemDonateViaPaypal() {
        logger.info("Event --> paypal donation");
        WebsiteManager.openWebSite(ConfigKey.DONATE_PAYPAL_LINK.getValue());
    }

    public void menuBugReport() {
        logger.info("Event --> bug report");
        WebsiteManager.openWebSite(ConfigKey.ISSUE_PAGE_LINK.getValue());
    }

    public void menuShare() {
        // pop-up message
        JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.SHARE_LINK_COPIED_MESSAGE));

        // copy link to the clipboard
        StringSelection stringSelectionObj = new StringSelection(ConfigKey.SHARE_LINK.getValue());
        Clipboard clipboardObj = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboardObj.setContents(stringSelectionObj, null);
    }

    public void menuItemHistory() {
        logger.info("Event --> history");
        try {
            logger.debug("Opening log file with path: " + ConfigKey.LOG_DIRECTORY_STRING.getValue() + ConfigKey.LOG_FILE_STRING.getValue());
            new ProcessBuilder("notepad.exe", ConfigKey.LOG_DIRECTORY_STRING.getValue() + ConfigKey.LOG_FILE_STRING.getValue()).start();
        } catch (IOException e) {
            logger.error("Error opening history file: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_OPEN_HISTORY_FILE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void menuQuit() {
        logger.info("Event --> exit");
        System.exit(main.EXIT_ON_CLOSE);
    }

    public void menuInfoPage() {
        logger.info("Event --> shard website");
        WebsiteManager.openWebSite(ConfigKey.INFO_PAGE_LINK.getValue());
    }

    ///////////////////////////////////////////////////////////////////

    public void popupRename(javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
        if (remindName != null)
            renameRemind(Remind.getRemindByName(remindName));
    }

    public void popupDelete(javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
        if (remindName != null)
            removeReminder(Remind.getRemindByName(remindName), false);
    }

    public void popupEdit(javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
        if (remindName != null)
            editRemind(Remind.getRemindByName(remindName));
    }

    public void popupDuplicate(javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
        if (remindName != null)
            duplicateReminder(Remind.getRemindByName(remindName));
    }

    public void popupCopyRemindName(javax.swing.JTable table) {
        logger.info("Event --> copying reminder name to the clipboard");

        String remindName = getRemindNameByTableRow(table);
        Remind remind = Remind.getRemindByName(remindName);

        StringSelection selection = new StringSelection(remind.getName());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    public void popupActive(javax.swing.JTable table, javax.swing.JCheckBoxMenuItem activePopupItem) {
        boolean newState = activePopupItem.isSelected();
        activePopupItem.setSelected(newState);

        logger.info("Event --> changing state for active popup to: " + newState);

        Remind remind = Remind.getRemindByName(getRemindNameByTableRow(table));
        for (Remind rem : reminds) {
            if (remind.getName().equals(rem.getName())) {
                rem.setIsActive(newState);

                if (newState) {
                    LocalDateTime nextExecution = getNextExecutionBasedOnMethod(remind.getExecutionMethod(), remind.getTimeRange(), remind.getTimeInterval());
                    rem.setNextExecution(nextExecution);
                } else {
                    rem.setNextExecution(null);
                }

                break;
            }
        }
        updateRemindList();
    }

    public void popupTopLevl(javax.swing.JTable table, javax.swing.JCheckBoxMenuItem topLevelPopupItem) {
        boolean newState = topLevelPopupItem.isSelected();
        topLevelPopupItem.setSelected(newState);

        logger.info("Event --> changing state for top level popup to: " + newState);

        Remind remind = Remind.getRemindByName(getRemindNameByTableRow(table));
        for (Remind rem : reminds) {
            if (remind.getName().equals(rem.getName())) {
                rem.setIsTopLevel(newState);
                break;
            }
        }
        updateRemindList();
    }

    ///////////////////////////////////////////////////////////////////


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

    public List<Remind> getReminds() {
        return reminds;
    }

    public List<Remind> retriveAndGetReminds() {
        try {
            reminds = JSON.readRemindListFromJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file());
        } catch (IOException ex) {
            reminds = null;
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
        return reminds;
    }
}
