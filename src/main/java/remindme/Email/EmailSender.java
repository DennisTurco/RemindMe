package remindme.Email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SMTPAppender;
import remindme.Entities.User;
import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.Json.JsonUser;

/**
 * Utility class for sending emails through logback SMTPAppender.
 */
public class EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    // Logger for sending critical error emails
    private static final Logger emailErrorLogger = LoggerFactory.getLogger("EMAIL_ERROR_LOGGER");

    // Logger for sending informational emails
    private static final Logger emailInfoLogger = LoggerFactory.getLogger("EMAIL_INFO_LOGGER");

    // Logger for sending confirmation email
    private static final Logger emailConfirmationLogger = LoggerFactory.getLogger("EMAIL_CONFIRMATION_LOGGER");

    /**
     * Sends a critical error email.
     * @param subject The email subject.
     * @param body The email body.
    */
    public static void sendErrorEmail(String subject, String body) {
        User user = getCurrentUser();

        if (user == null) {
            logger.warn("User is null, using a default user for the email");
            user = User.getDefaultUser();
        }

        int rows = 300;
        String emailMessage = String.format(
            "Subject: %s\n\nUser: %s \nEmail: %s \nLanguage: %s \nInstalled Version: %s \n\nHas encountered the following error:\n%s \n\nLast %d rows of the application.log file:\n%s",
            subject,
            user.getUserCompleteName(),
            user.email(),
            user.language(),
            ConfigKey.VERSION.getValue(),
            body,
            rows,
            getTextFromLogFile(rows)
        );

        emailErrorLogger.error(emailMessage); // Log the message as ERROR, triggering the SMTPAppender

        logger.info("Error email sent with subject: " + subject);
    }

    /**
     * Sends an informational email.
     */
    public static void sendUserCreationEmail(User user) {
        String userDetails = "New user registered. \n\nName: " + user.getUserCompleteName()+ "\nEmail: " + user.email() + "\nLanguage: " + user.language() + "\nInstalled version: " + ConfigKey.VERSION.getValue();

        String emailMessage = "\n\n" + userDetails;

        // Should be info, but if you change it, it doesn't work
        emailInfoLogger.error(emailMessage); // Log the message as INFO, triggering the SMTPAppender

        logger.info("User creation info email sent with user: " + user.toString());
    }

    /**
     * Sends an informational email.
     */
    public static void sendConfirmEmailToUser(User user) {
        if (user == null) throw new IllegalArgumentException("User object cannot be null");

        String subject = TranslationCategory.USER_DIALOG.getTranslation(TranslationKey.EMAIL_CONFIRMATION_SUBJECT);
        String body = TranslationCategory.USER_DIALOG.getTranslation(TranslationKey.EMAIL_CONFIRMATION_BODY);

        // Assicurati di assegnare il risultato della sostituzione
        body = body.replace("[UserName]", user.getUserCompleteName());
        body = body.replace("[SupportEmail]", ConfigKey.EMAIL.getValue());

        String emailMessage = subject + "\n\n" + body;

        updateEmailRecipient(user.email());

        // Should be info, but if you change it, it doesn't work
        emailConfirmationLogger.error(emailMessage); // Log the message as INFO, triggering the SMTPAppender

        logger.info("Confirmation registration email sent to the user: " + user.toString());
    }

    private static User getCurrentUser() {
        try {
            User user = JsonUser.readUserFromJson(ConfigKey.USER_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
            return user;
        } catch (IOException e) {
            logger.error("Unable to retrieve user details for the email: " + e.getMessage(), e);
        }

        return null;
    }

    public static String getTextFromLogFile(int rows) {
        File file = new File(ConfigKey.LOG_DIRECTORY_STRING.getValue() + ConfigKey.LOG_FILE_STRING.getValue());

        if (!file.exists() || !file.isFile() || file.length() == 0) {
            return "Log file does not exist or is empty.";
        }

        List<String> lastLines = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (lastLines.size() == rows) {
                    lastLines.remove(0); // remove the older
                }
                lastLines.add(line);
            }
        } catch (IOException e) {
            logger.error("An error occurred during reading the log file for getting the last rows: " + e.getMessage(), e);
            return "Error reading the log file.";
        }

        return String.join("\n", lastLines);
    }

    private static void updateEmailRecipient(String newRecipient) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        //get the'appender SMTP
        SMTPAppender smtpAppender = (SMTPAppender) context.getLogger("EMAIL_CONFIRMATION_LOGGER").getAppender("EMAIL_CONFIRMATION_LOGGER");

        // if exists -> update
        if (smtpAppender != null) {
            smtpAppender.getToList().clear();
            smtpAppender.addTo(newRecipient);
        }
    }
}

