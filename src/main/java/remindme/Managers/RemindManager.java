package remindme.Managers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import remindme.Dialogs.ManageRemind;
import remindme.Dialogs.PreferencesDialog;
import remindme.Dialogs.TimePicker;
import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Entities.TimeInterval;
import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.GUI.MainGUI;
import remindme.Json.JSONReminder;
import remindme.Logger;
import remindme.Logger.LogLevel;
import remindme.Table.TableDataManager;

public final class RemindManager {

    public static final DateTimeFormatter dateForfolderNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm.ss");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    
    private final JSONReminder JSON;
    private final MainGUI main;
    private List<Remind> reminds;

    public RemindManager(MainGUI main) {
        this.main = main;
        JSON = new JSONReminder();
    }

    public TimeInterval openTimePicker(TimeInterval time) {
        TimePicker picker = new TimePicker(main, time, true);
        picker.setVisible(true);
        return picker.getTimeInterval();
    }
    
    public void openPreferences() {
        Logger.logMessage("Event --> opening preferences dialog", LogLevel.INFO);

        PreferencesDialog prefs = new PreferencesDialog(main, true, this);
        prefs.setVisible(true);
    }

    public void reloadPreferences() {
        Logger.logMessage("Reloading preferences", LogLevel.INFO);

        // load language
        try {
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
            main.setTranslations();
        } catch (IOException ex) {
            Logger.logMessage("An error occurred during reloading preferences operation: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
        
        // load theme
        ThemeManager.updateThemeFrame(main);
        ThemeManager.refreshPopup(main.getTablePopup());
        main.setSvgImages();
    }

    public void updateRemindList(List<Remind> reminds) {            
        JSON.updateRemindListJSON(Preferences.getRemindList().getDirectory(), Preferences.getRemindList().getFile(), reminds);
        
        if (MainGUI.model != null)
            TableDataManager.updateTableWithNewRemindList(reminds, formatter);
    }

    private String insertAndGetRemindName(boolean canOverwrite) {
        String remindName;
        do {
            remindName = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_NAME_INPUT)); // pop-up message
            for (Remind remind : reminds) {
                if (remind.getName().equals(remindName) && canOverwrite) {
                    int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_REMIND_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        reminds.remove(remind);
                        break;
                    } else {
                        remindName = null;
                    }
                } else if (remind.getName().equals(remindName)) {
                    Logger.logMessage("Error saving remind", Logger.LogLevel.WARN);
                    JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_NAME_ALREADY_USED_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
            if (remindName == null) return null;
        } while (remindName.equals("null") ||  remindName.equals("null*"));	
        if (remindName.isEmpty()) return null;
        return remindName;
    }

    public static String pathSearchWithFileChooser(boolean allowFiles) {
        Logger.logMessage("Event --> File chooser", Logger.LogLevel.INFO);
        
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        
        if (allowFiles)
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        else
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = jfc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();

            if (selectedFile.isDirectory()) {
                Logger.logMessage("You selected the directory: " + selectedFile, Logger.LogLevel.INFO);
            } else if (selectedFile.isFile()) {
                Logger.logMessage("You selected the file: " + selectedFile, Logger.LogLevel.INFO);
            }

            return selectedFile.toString();
        }

        return null;
    }

    public void researchInTable(String research) {
        List<Remind> tempReminds = new ArrayList<>();
        
        for (Remind remind : reminds) {
            if (remind.getName().contains(research) || 
                    (remind.getLastExecution() != null && remind.getLastExecution().toString().contains(research)) ||
                    (remind.getNextExecution() != null && remind.getNextExecution().toString().contains(research)) ||
                    (remind.getTimeInterval() != null && remind.getTimeInterval().toString().contains(research))) {
                tempReminds.add(remind);
            }
        }
        
        TableDataManager.updateTableWithNewRemindList(tempReminds, formatter);
    }

    private String getRemindNameByTableRow(javax.swing.JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            return (String) table.getValueAt(selectedRow, 0);
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////

    public void addReminder() {
        Logger.logMessage("Event --> adding new reminder", Logger.LogLevel.INFO);
        
        ManageRemind manage = new ManageRemind(main, true, TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.CREATE_TITLE), TranslationCategory.GENERAL.getTranslation(TranslationKey.ADD_BUTTON));
        manage.setVisible(true);
        Remind remind = manage.getRemindInserted();

        if (remind == null)
            return;

        reminds.add(remind);
        updateRemindList(reminds);
    }

    public void saveReminder() {
        Logger.logMessage("Event --> saving reminder", Logger.LogLevel.INFO);
    }

    public void removeReminder(Remind remind, boolean d) {
        Logger.logMessage("Event --> removing reminder", Logger.LogLevel.INFO);
    }

    public void updateReminder() {
        Logger.logMessage("Event --> updating reminder", Logger.LogLevel.INFO);
    }

    public void copyReminderName() {
        Logger.logMessage("Event --> cipying reminder name", Logger.LogLevel.INFO);
    }

    public void duplicateReminder(Remind remind) {
        Logger.logMessage("Event --> duplicating reminder", Logger.LogLevel.INFO);
    }

    public void importRemindListFromJSON() {
        Logger.logMessage("Event --> importing remind list from json", Logger.LogLevel.INFO);
        List<Remind> newReminds = ImportExportManager.importRemindListFromJson(main, JSON, dateForfolderNameFormatter);
        
        // replace the current list with the imported one
        if (newReminds != null)
            reminds = newReminds;
    }

    public void exportRemindListTOJSON() {
        Logger.logMessage("Event --> exporting remind list to json", Logger.LogLevel.INFO);

        ImportExportManager.exportRemindListToJson();
    }

    public void exportRemindListAsPDF() {
        Logger.logMessage("Event --> exporting remind list as pdf", Logger.LogLevel.INFO);

        ImportExportManager.exportRemindListAsPDF(reminds, Remind.getCSVHeader());
    }

    public void exportRemindListAsCSV() {
        Logger.logMessage("Event --> exporting remind list as csv", Logger.LogLevel.INFO);

        ImportExportManager.exportRemindListAsCSV(reminds, Remind.getCSVHeader());
    }

    public void renameRemind(Remind remind) {
        Logger.logMessage("Event --> backup renaming", Logger.LogLevel.INFO);
        
        String remindName = insertAndGetRemindName(false);
        if (remindName == null || remindName.isEmpty()) return;
        
        remind.setName(remindName);
        remind.setLastUpdateDate(LocalDateTime.now());
        updateRemindList(reminds);
    }

    public void editRemind(Remind remind) {
        Logger.logMessage("Event --> editing reminder", Logger.LogLevel.INFO);

        ManageRemind manage = new ManageRemind(main, true, TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.EDIT_TITLE), TranslationCategory.GENERAL.getTranslation(TranslationKey.SAVE_BUTTON), remind);
        manage.setVisible(true);
        Remind updatedRemind = manage.getRemindInserted();
        
        if (updatedRemind == null)
            return;
        
        remind.updateReming(updatedRemind);
        updateRemindList(reminds);
    }

    public static LocalDateTime getnextExecutionByTimeInterval(TimeInterval timeInterval) {
        if (timeInterval == null) return null;

        return LocalDateTime.now().plusDays(timeInterval.getDays())
            .plusHours(timeInterval.getHours())
            .plusMinutes(timeInterval.getMinutes());
    }

    ///////////////////////////////////////////////////////////////////

    public void menuSupport() {
        Logger.logMessage("Event --> support", Logger.LogLevel.INFO);
        WebsiteManager.sendEmail();
    }

    public void menuWebsite() {
        Logger.logMessage("Event --> shard website", Logger.LogLevel.INFO);
        WebsiteManager.openWebSite(ConfigKey.SHARD_WEBSITE.getValue());
    }

    public void menuDonate() {
        Logger.logMessage("Event --> donate", Logger.LogLevel.INFO);
        WebsiteManager.openWebSite(ConfigKey.DONATE_PAGE_LINK.getValue());
    }

    public void menuBugReport() {
        Logger.logMessage("Event --> bug report", Logger.LogLevel.INFO);
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

    public void menuHistory() {
        Logger.logMessage("Event --> history", Logger.LogLevel.INFO);
        try {
            new ProcessBuilder("notepad.exe", ConfigKey.RES_DIRECTORY_STRING.getValue() + ConfigKey.LOG_FILE_STRING.getValue()).start();
        } catch (IOException e) {
            Logger.logMessage("Error opening history file.", Logger.LogLevel.WARN);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_OPEN_HISTORY_FILE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void menuQuit() {
        Logger.logMessage("Event --> exit", Logger.LogLevel.INFO);
        System.exit(main.EXIT_ON_CLOSE);
    }

    public void menuInfoPage() {
        
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
        Logger.logMessage("Event --> copying reminder name to the clipboard", Logger.LogLevel.INFO);

        String remindName = getRemindNameByTableRow(table);
    }

    public void popupActive(javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
    }

    public void popupTopLevl(javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
    }

    ///////////////////////////////////////////////////////////////////
    

    public String[] getColumnTranslations() {
        String[] columnNames = {
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
            reminds = JSON.readRemindListFromJSON(Preferences.getRemindList().getDirectory(), Preferences.getRemindList().getFile());
        } catch (IOException ex) {
            reminds = null;
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
        return reminds;
    }

    public void setReminds(List<Remind> reminds) {
        this.reminds = reminds;
    }
}
