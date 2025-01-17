package remindme.Managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import remindme.Logger;
import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Entities.RemindListPath;
import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.GUI.MainGUI;
import remindme.Json.JSONReminder;
import remindme.Table.TableDataManager;

class ImportExportManager {

    // return the Remind list. Null if the operations fail or cancelled by the user
    public static List<Remind> importRemindListFromJson(MainGUI main, JSONReminder JSON, DateTimeFormatter formatter) {
        JFileChooser jfc = new JFileChooser(ConfigKey.RES_DIRECTORY_STRING.getValue());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("JSON Files (*.json)", "json");
        jfc.setFileFilter(jsonFilter);
        int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.isFile() && selectedFile.getName().toLowerCase().endsWith(".json")) {
                Logger.logMessage("File imported: " + selectedFile, Logger.LogLevel.INFO);

                Preferences.setRemindList(new RemindListPath(selectedFile.getParent()+File.separator, selectedFile.getName()));
                Preferences.updatePreferencesToJSON();

                try {
                    List<Remind> reminds = JSON.readRemindListFromJSON(Preferences.getRemindList().getDirectory(), Preferences.getRemindList().getFile());
                    TableDataManager.updateTableWithNewRemindList(reminds, formatter);
                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_LIST_CORRECTLY_IMPORTED_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_LIST_CORRECTLY_IMPORTED_TITLE), JOptionPane.INFORMATION_MESSAGE);
                    return reminds;
                } catch (IOException ex) {
                    Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
                }
            } else {
                JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_TITLE), JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public static void exportRemindListToJson() {
        Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop", Preferences.getRemindList().getFile());
        Path sourcePath = Paths.get(Preferences.getRemindList().getDirectory() + Preferences.getRemindList().getFile());

        try {
            Files.copy(sourcePath, desktopPath, StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_LIST_CORRECTLY_EXPORTED_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_LIST_CORRECTLY_EXPORTED_TITLE), JOptionPane.INFORMATION_MESSAGE);
        } catch (java.nio.file.NoSuchFileException ex) {
            Logger.logMessage("Source file not found: " + ex.getMessage(), Logger.LogLevel.ERROR);
            JOptionPane.showMessageDialog(null, "Error: The source file was not found.\nPlease check the file path.", "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.nio.file.AccessDeniedException ex) {
            Logger.logMessage("Access denied to desktop: " + ex.getMessage(), Logger.LogLevel.ERROR);
            JOptionPane.showMessageDialog(null, "Error: Access to the Desktop is denied.\nPlease check folder permissions and try again.","Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.logMessage("Unexpected error: " + ex.getMessage(), Logger.LogLevel.ERROR);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}