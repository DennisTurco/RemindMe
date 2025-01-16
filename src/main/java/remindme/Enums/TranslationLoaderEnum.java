package remindme.Enums;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import remindme.Logger;
import remindme.Logger.LogLevel;

public class TranslationLoaderEnum {

    public enum TranslationCategory {
        GENERAL("General"),
        MENU("Menu"),
        TIME_PICKER_DIALOG("TimePickerDialog"),
        PREFERENCES_DIALOG("PreferencesDialog"),
        REMIND_LIST("RemindList"),
        TRAY_ICON("TrayIcon"),
        DIALOGS("Dialogs");
    
        private final String categoryName;
        private final Map<TranslationKey, String> translations = new HashMap<>();
    
        TranslationCategory(String categoryName) {
            this.categoryName = categoryName;
        }
    
        public String getCategoryName() {
            return categoryName;
        }
    
        public void addTranslation(TranslationKey key, String value) {
            translations.put(key, value);
        }
    
        // Updated getTranslation method
        public String getTranslation(TranslationKey key) {
            return translations.getOrDefault(key, key.getDefaultValue());
        }
    }

    public enum TranslationKey {
        // General
        APP_NAME("AppName", "Remind Me"),
        VERSION("Version", "Version"),
        FROM("From", "From"),
        TO("To", "To"),
        CLOSE_BUTTON("CloseButton", "Close"),
        OK_BUTTON("OkButton", "Ok"),
        CANCEL_BUTTON("CancelButton", "Cancel"),
        APPLY_BUTTON("ApplyButton", "Apply"),

        // Menu
        FILE("File", "File"),
        OPTIONS("Options", "Options"),
        ABOUT("About", "About"),
        HELP("Help", "Help"),
        BUG_REPORT("BugReport", "Report a bug"),
        CLEAR("Clear", "Clear"),
        DONATE("Donate", "Donate"),
        HISTORY("History", "History"),
        INFO_PAGE("InfoPage", "Info"),
        NEW("New", "New"),
        QUIT("Quit", "Quit"),
        SAVE("Save", "Save"),
        PREFERENCES("Preferences", "Preferences"),
        IMPORT("Import", "Import"),
        EXPORT("Export", "Export"),
        SAVE_WITH_NAME("SaveWithName", "Save with name"),
        SHARE("Share", "Share"),
        SUPPORT("Support", "Support"),
        WEBSITE("Website", "Website"),

        // MainFrame
        EXPORT_AS("ExportAs", "Export as: "),
        EXPORT_AS_PDF_TOOLTIP("ExportAsPdfTooltip", "Export as PDF"),
        EXPORT_AS_CSV_TOOLTIP("ExportAsCsvTooltip", "Export as CSV"),
        RESEARCH_BAR_TOOLTIP("ResearchBarTooltip", "Research bar"),
        RESEARCH_BAR_PLACEHOLDER("ResearchBarPlaceholder", "Search..."),

        // RemindList
        EDIT_POPUP("EditPopup", "Edit"),
        DELETE_POPUP("DeletePopup", "Delete"),
        DUPLICATE_POPUP("DuplicatePopup", "Duplicate"),
        RENAME_BACKUP_POPUP("RenameBackupPopup", "Rename"),
        NAME_COLUMN("NameColumn", "Name"),
        LAST_EXECUTION_COLUMN("LastExecutionColumn", "Last Execution"),
        NEXT_EXECUTION_COLUMN("NextExecutionColumn", "Next Execution"),
        IS_ACTIVE_COLUMN("IsActiveColumn", "Active"),
        IS_TOP_LEVEL_COLUMN("IsTopLevelColumn", "Top Level"),
        TIME_INTERVAL_COLUMN("TimeIntervalColumn", "Time Interval"),
        NAME_DETAIL("NameDetail", "Name"),
        DESCRIPTION_DETAIL("DescriptionDetail", "Description"),
        COUNT_DETAIL("CountDetail", "Count"),
        LAST_EXECUTION_DETAIL("LastExecutionDetail", "LastExecution"),
        NEXT_EXECUTION_DETAIL("NextExecutionDetail", "NextExecution"),
        IS_ACTIVE_DETAIL("IsActiveDetail", "Active"),
        IS_TOP_LEVEL_DETAIL("IsTopLevelDetail", "TopLevel"),
        CREATION_DATE_DETAIL("CreationDateDetail", "CreationDate"),
        LAST_UPDATE_DATE_DETAIL("LastUpdateDateDetail", "LastUpdateDate"),
        TIME_INTERVAL_DETAIL("TimeIntervalDetail", "TimeInterval"),

        // TimePickerDialog
        TIME_INTERVAL_TITLE("TimeIntervalTitle", "Time interval for reminder"),
        DESCRIPTION("Description", ""),
        DAYS("Days", "Days"),
        HOURS("Hours", "Hours"),
        MINUTES("Minutes", "Minutes"),
        SECONDS("Seconds", "Seconds"),
        SPINNER_TOOLTIP("SpinnerTooltip", "Mouse wheel to adjust the value"),

        // PreferencesDialog
        PREFERENCES_TITLE("PreferencesTitle", "Preferences"),
        LANGUAGE("Language", "Language"),
        THEME("Theme", "Theme"),

        // TrayIcon
        TRAY_TOOLTIP("TrayTooltip", "Remind Service"),

        // Dialogs
        EXCEPTION_MESSAGE_TITLE("ExceptionMessageTitle", "Error..."),
        EXCEPTION_MESSAGE_CLIPBOARD_MESSAGE("ExceptionMessageClipboardMessage", "Error text has been copied to the clipboard."),
        EXCEPTION_MESSAGE_CLIPBOARD_BUTTON("ExceptionMessageClipboardButton", "Copy to clipboard"),
        EXCEPTION_MESSAGE_REPORT_BUTTON("ExceptionMessageReportButton", "Report the Problem"),
        EXCEPTION_MESSAGE_REPORT_MESSAGE("ExceptionMessageReportMessage", "Please report this error, either with an image of the screen or by copying the following error text (it is appreciable to provide a description of the operations performed before the error):"),
        ERROR_MESSAGE_OPENING_WEBSITE("ErrorMessageOpeningWebsite", "Failed to open the web page. Please try again."),
        ERROR_GENERIC_TITLE("ErrorGenericTitle", "Error"),
        ERROR_WRONG_TIME_INTERVAL("ErrorWrongTimeInterval", "The time interval is not correct");

        private final String keyName;
        private final String defaultValue;

        private static final Map<String, TranslationKey> lookup = new HashMap<>();

        static {
            for (TranslationKey key : TranslationKey.values()) {
                lookup.put(key.keyName, key);
            }
        }

        // Constructor to assign both key and default value
        private TranslationKey(String keyName, String defaultValue) {
            this.keyName = keyName;
            this.defaultValue = defaultValue;
        }

        public String getKeyName() {
            return keyName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        // Lookup by keyName (JSON key)
        public static TranslationKey fromKeyName(String keyName) {
            return lookup.get(keyName);
        }

        @Override
        public String toString() {
            return keyName;
        }
    }

    public static void loadTranslations(String filePath) throws IOException {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            for (TranslationCategory category : TranslationCategory.values()) {
                JsonObject categoryTranslations = jsonObject.getAsJsonObject(category.getCategoryName());

                if (categoryTranslations != null) {
                    for (Map.Entry<String, JsonElement> entry : categoryTranslations.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue().getAsString();

                        // Use fromKeyName to get the TranslationKey from the JSON key
                        TranslationKey translationKey = TranslationKey.fromKeyName(key);
                        if (translationKey != null) {
                            // If value is null or empty, fall back to the default value from the enum
                            String translationValue = (value != null && !value.isEmpty()) ? value : translationKey.getDefaultValue();
                            category.addTranslation(translationKey, translationValue);
                        } else {
                            // If the key is not recognized in the enum, log it and use the default value
                            Logger.logMessage("Warning: Unrecognized key in JSON: " + key + ", using default value.", LogLevel.WARN);
                            category.addTranslation(translationKey, translationKey.getDefaultValue());
                        }
                    }
                }
            }
        }
    }

    public static String getTranslation(TranslationCategory category, TranslationKey key) {
        return category.translations.getOrDefault(key, key.getDefaultValue()); // Use default value if not found
    }

    // only for test
    public static void main(String[] args) {
        try {
            loadTranslations("src/main/resources/res/languages/ita.json");

            System.out.println(TranslationCategory.MENU.getTranslation(TranslationKey.FILE));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
