package remindme.Enums;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TranslationLoaderEnum {

    private static final Logger logger = LoggerFactory.getLogger(TranslationLoaderEnum.class);

    public enum TranslationCategory {
        GENERAL("General"),
        MENU("Menu"),
        MANAGE_REMIND_DIALOG("ManageRemindDialog"),
        TIME_PICKER_DIALOG("TimePickerDialog"),
        PREFERENCES_DIALOG("PreferencesDialog"),
        USER_DIALOG("UserDialog"),
        REMIND_LIST("RemindList"),
        MAIN_FRAME("MainFrame"),
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
        SAVE_BUTTON("SaveButton", "Save"),
        ADD_BUTTON("AddButton", "Add"),

        // Menu
        FILE("File", "File"),
        OPTIONS("Options", "Options"),
        ABOUT("About", "About"),
        HELP("Help", "Help"),
        BUG_REPORT("BugReport", "Report a bug"),
        DONATE("Donate", "Donate"),
        HISTORY("History", "History"),
        INFO_PAGE("InfoPage", "Info"),
        NEW("New", "New"),
        QUIT("Quit", "Quit"),
        PREFERENCES("Preferences", "Preferences"),
        IMPORT("Import", "Import from .json"),
        EXPORT("Export", "Export to .json"),
        SHARE("Share", "Share"),
        SUPPORT("Support", "Support"),
        WEBSITE("Website", "Website"),

        // MainFrame
        NEW_REMIND_TOOLTIP("AddBackupTooltip", "Add new reminder"),
        EXPORT_AS("ExportAs", "Export as: "),
        EXPORT_AS_PDF_TOOLTIP("ExportAsPdfTooltip", "Export as PDF"),
        EXPORT_AS_CSV_TOOLTIP("ExportAsCsvTooltip", "Export as CSV"),
        RESEARCH_BAR_TOOLTIP("ResearchBarTooltip", "Research bar"),
        RESEARCH_BAR_PLACEHOLDER("ResearchBarPlaceholder", "Search..."),

        // RemindList
        EDIT_POPUP("EditPopup", "Edit"),
        DELETE_POPUP("DeletePopup", "Delete"),
        DUPLICATE_POPUP("DuplicatePopup", "Duplicate"),
        RENAME_POPUP("RenamePopup", "Rename"),
        COPY_NAME_POPUP("CopyNamePopup", "Copy remind name"),
        NAME_COLUMN("NameColumn", "Name"),
        LAST_EXECUTION_COLUMN("LastExecutionColumn", "Last Execution"),
        NEXT_EXECUTION_COLUMN("NextExecutionColumn", "Next Execution"),
        IS_ACTIVE_COLUMN("IsActiveColumn", "Active"),
        IS_TOP_LEVEL_COLUMN("IsTopLevelColumn", "Show On Top"),
        TIME_INTERVAL_COLUMN("TimeIntervalColumn", "Time Interval"),
        NAME_DETAIL("NameDetail", "Name"),
        DESCRIPTION_DETAIL("DescriptionDetail", "Description"),
        COUNT_DETAIL("CountDetail", "Count"),
        LAST_EXECUTION_DETAIL("LastExecutionDetail", "LastExecution"),
        NEXT_EXECUTION_DETAIL("NextExecutionDetail", "NextExecution"),
        IS_ACTIVE_DETAIL("IsActiveDetail", "Active"),
        IS_TOP_LEVEL_DETAIL("IsTopLevelDetail", "ShowOnTop"),
        CREATION_DATE_DETAIL("CreationDateDetail", "CreationDate"),
        LAST_UPDATE_DATE_DETAIL("LastUpdateDateDetail", "LastUpdateDate"),
        TIME_INTERVAL_DETAIL("TimeIntervalDetail", "TimeInterval"),

        // ManageRemindDialog
        EDIT_TITLE("EditTitle", "Time interval for reminder"),
        CREATE_TITLE("CreateTitle", "Time interval for reminder"),
        NAME_TEXT("NameText", "Name"),
        ACTIVE_TEXT("ActiveText", "Active"),
        TOP_LEVEL_TEXT("TopLevelText", "Show on Top"),
        PREVIEW_TEXT("PreviewText", "Reminder preview"),
        NAME_PLACEHOLDER("NamePlaceholder", "Enter reminder name"),
        DESCRIPTION_PLACEHOLDER("DescriptionPlaceholder", "Enter description (optional)"),
        NAME_TOOLTIP("NameTooltip", "Enter a name for the reminder"),
        ACTIVE_TOOLTIP("ActiveTooltip", "Activate or disable the reminder"),
        TOP_LEVEL_TOOLTIP("TopLevelTooltip", "If enabled, the reminder will always appear on top of other windows"),
        DESCRIPTION_TOOLTIP("DescriptionTooltip", "Provide additional details for this reminder"),
        ICON_TOOLTIP("IconTooltip", "Choose an icon for the reminder notification"),
        SOUND_TOOLTIP("SoundTooltip", "Choose a sound for the reminder notification"),
        SOUND_BUTTON_TOOLTIP("SoundButtonTooltip", "Preview the selected sound"),

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

        // User dialog
        USER_TITLE("UserTitle", "Insert your data"),
        USER_NAME("Name", "Name"),
        USER_SURNAME("Surname", "Surname"),
        USER_EMAIL("Email", "Email"),
        ERROR_MESSAGE_FOR_MISSING_DATA("ErrorMessageForMissingData", "Please fill in all the required fields."),
        ERROR_MESSAGE_FOR_WRONG_EMAIL("ErrorMessageForWrongEmail", "The provided email address is invalid. Please provide a correct one."),
        EMAIL_CONFIRMATION_SUBJECT("EmailConfirmationSubject", "Thank you for choosing Backup Manager!"),
        EMAIL_CONFIRMATION_BODY("EmailConfirmationBody", "Hi [UserName],\n\nThank you for downloading and registering **Backup Manager**, your new tool for secure and efficient backup management!\n\nThis is an automated email sent to confirm your registration. We will contact you by email only to inform you about new releases or important updates of the application.\n\nIn the meantime, if you have any questions, need assistance, or have suggestions, we are always here for you. You can reach us at **[SupportEmail]**.\n\nThank you again for choosing Backup Manager, and enjoy managing your backups!\n\nBest regards,\nThe Backup Manager Team"),

        // TrayIcon
        TRAY_TOOLTIP("TrayTooltip", "Remind Service"),

        // Dialogs
        EXCEPTION_MESSAGE_TITLE("ExceptionMessageTitle", "Error..."),
        REMIND_SAVED_CORRECTLY_TITLE("RemindSavedCorrectlyTitle", "Remind saved"),
        REMIND_SAVED_CORRECTLY_MESSAGE("RemindSavedCorrectlyMessage", "saved successfully!"),
        REMIND_NAME_INPUT("RemindNameInput", "Name of the reminder"),
        CONFIRMATION_REQUIRED_TITLE("ConfirmationRequiredTitle", "Confirmation required"),
        DUPLICATED_REMIND_NAME_MESSAGE("DuplicatedRemindNameMessage", "A reminder with the same name already exists, do you want to overwrite it?"),
        REMIND_LIST_CORRECTLY_EXPORTED_TITLE("RemindListCorrectlyExportedTitle", "Menu Export"),
        REMIND_LIST_CORRECTLY_EXPORTED_MESSAGE("RemindListCorrectlyExportedMessage", "Remind list successfully exported to the Desktop!"),
        REMIND_LIST_CORRECTLY_IMPORTED_TITLE("RemindListCorrectlyImportedTitle", "Menu Import"),
        REMIND_LIST_CORRECTLY_IMPORTED_MESSAGE("RemindListCorrectlyImportedMessage", "Remind list successfully imported!"),
        REMIND_NAME_ALREADY_USED_MESSAGE("RemindNameAlreadyUsedMessage", "Remind name already used!"),
        EXCEPTION_MESSAGE_CLIPBOARD_MESSAGE("ExceptionMessageClipboardMessage", "Error text has been copied to the clipboard."),
        EXCEPTION_MESSAGE_CLIPBOARD_BUTTON("ExceptionMessageClipboardButton", "Copy to clipboard"),
        EXCEPTION_MESSAGE_REPORT_BUTTON("ExceptionMessageReportButton", "Report the Problem"),
        EXCEPTION_MESSAGE_REPORT_MESSAGE("ExceptionMessageReportMessage", "Please report this error, either with an image of the screen or by copying the following error text (it is appreciable to provide a description of the operations performed before the error):"),
        ERROR_MESSAGE_OPEN_HISTORY_FILE("ErrorMessageOpenHistoryFile", "Error opening history file."),
        ERROR_SAVING_REMIND_MESSAGE("ErrorSavingRemindMessage", "Error saving reminder"),
        ERROR_MESSAGE_OPENING_WEBSITE("ErrorMessageOpeningWebsite", "Failed to open the web page. Please try again."),
        ERROR_GENERIC_TITLE("ErrorGenericTitle", "Error"),
        ERROR_WRONG_TIME_INTERVAL("ErrorWrongTimeInterval", "The time interval is not correct"),
        ERROR_MESSAGE_UNABLE_TO_SEND_EMAIL("ErrorMessageUnableToSendEmail", "Unable to send email. Please try again later."),
        ERROR_MESSAGE_NOT_SUPPORTED_EMAIL("ErrorMessageNotSupportedEmail", "Your system does not support sending emails directly from this application."),
        ERROR_MESSAGE_NOT_SUPPORTED_EMAIL_GENERIC("ErrorMessageNotSupportedEmailGeneric", "Your system does not support sending emails."),
        ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_TITLE("ErrorMessageForWrongFileExtensionTitle", "Invalid File"),
        ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_MESSAGE("ErrorMessageForWrongFileExtensionMessage", "Error: Please select a valid JSON file."),
        SHARE_LINK_COPIED_MESSAGE("ShareLinkCopiedMessage", "Share link copied to clipboard!"),
        SUCCESS_GENERIC_TITLE( "SuccessGenericTitle", "Success"),
        SUCCESSFULLY_EXPORTED_TO_CSV_MESSAGE("SuccessfullyExportedToCsvMessage", "Backups exported to CSV successfully!"),
        SUCCESSFULLY_EXPORTED_TO_PDF_MESSAGE("SuccessfullyExportedToPdfMessage", "Backups exported to PDF successfully!"),
        ERROR_MESSAGE_FOR_EXPORTING_TO_CSV("ErrorMessageForExportingToCsv", "Error exporting backups to CSV: "),
        ERROR_MESSAGE_FOR_EXPORTING_TO_PDF("ErrorMessageForExportingToPdf", "Error exporting backups to PDF: "),
        CSV_NAME_MESSAGE_INPUT("CsvNameMessageInput", "Enter the name of the CSV file."),
        PDF_NAME_MESSAGE_INPUT("PdfNameMessageInput", "Enter the name of the PDF file."),
        DUPLICATED_FILE_NAME_MESSAGE("DuplicatedFileNameMessage", "File already exists. Overwrite?"),
        ERROR_MESSAGE_INVALID_FILENAME("ErrorMessageInvalidFilename", "Invalid file name. Use only alphanumeric characters, dashes, and underscores."),
        CONFIRMATION_DELETION_TITLE("ConfirmationDeletionTitle", "Confirm Deletion"),
        CONFIRMATION_DELETION_MESSAGE("ConfirmationDeletionMessage", "Are you sure you want to delete the selected rows?");

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
                            logger.warn("Warning: Unrecognized key in JSON: " + key + ", using default value");
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
