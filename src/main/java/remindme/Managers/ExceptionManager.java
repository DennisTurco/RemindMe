package remindme.Managers;

public class ExceptionManager {
    public static void openExceptionMessage(String errorMessage, String stackTrace) {
        // Object[] options = {TranslationCategory.GENERAL.getTranslation(TranslationKey.CLOSE_BUTTON), TranslationCategory.DIALOGS.getTranslation(TranslationKey.EXCEPTION_MESSAGE_CLIPBOARD_BUTTON), TranslationCategory.DIALOGS.getTranslation(TranslationKey.EXCEPTION_MESSAGE_REPORT_BUTTON)};

        // if (errorMessage == null) {
        //     errorMessage = "";
        // }
        // stackTrace = !errorMessage.isEmpty() ? errorMessage + "\n" + stackTrace : errorMessage + stackTrace;
        // String stackTraceMessage = TranslationCategory.DIALOGS.getTranslation(TranslationKey.EXCEPTION_MESSAGE_REPORT_MESSAGE) + "\n" + stackTrace;

        // int choice;

        // // Set a maximum width for the error message
        // final int MAX_WIDTH = 500;

        // // Keep displaying the dialog until the "Close" option (index 0) is chosen
        // do {
        //     if (stackTraceMessage.length() > 1500) {
        //         stackTraceMessage = stackTraceMessage.substring(0, 1500) + "...";
        //     }

        //     // Create a JTextArea to hold the error message with line wrapping
        //     JTextArea messageArea = new JTextArea(stackTraceMessage);
        //     messageArea.setLineWrap(true);
        //     messageArea.setWrapStyleWord(true);
        //     messageArea.setEditable(false);
        //     messageArea.setColumns(50); // Approximate width, adjust as necessary

        //     // Limit the maximum width
        //     messageArea.setSize(new Dimension(MAX_WIDTH, Integer.MAX_VALUE));
        //     messageArea.setPreferredSize(new Dimension(MAX_WIDTH, messageArea.getPreferredSize().height));

        //     // Put the JTextArea in a JScrollPane for scrollable display if needed
        //     JScrollPane scrollPane = new JScrollPane(messageArea);
        //     scrollPane.setPreferredSize(new Dimension(MAX_WIDTH, 300));

        //     // Display the option dialog with the JScrollPane
        //     String error = TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE);
        //     choice = JOptionPane.showOptionDialog(
        //         null,
        //         scrollPane,                           // The JScrollPane containing the error message
        //         error,                                // The error message/title
        //         JOptionPane.DEFAULT_OPTION,           // Option type (default option type)
        //         JOptionPane.ERROR_MESSAGE,            // Message type (error message icon)
        //         null,                            // Icon (null means default icon)
        //         options,                              // The options for the buttons
        //         options[0]                            // The default option (Close)
        //     );

        //     if (choice == 1) {
        //         StringSelection selection = new StringSelection(stackTrace);
        //         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        //         Logger.logMessage("Error text has been copied to the clipboard", Logger.LogLevel.INFO);
        //         JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.EXCEPTION_MESSAGE_CLIPBOARD_MESSAGE));
        //     } else if (choice == 2) {
        //         openWebSite(ConfigKey.ISSUE_PAGE_LINK.getValue());
        //     }
        // } while (choice == 1 || choice == 2);
    }
}
