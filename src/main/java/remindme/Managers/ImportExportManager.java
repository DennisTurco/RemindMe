package remindme.Managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Entities.RemindListPath;
import remindme.Enums.ConfigKey;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.GUI.Controllers.MainController;
import remindme.GUI.MainGUI;
import remindme.Json.JSONReminder;
import remindme.Services.RemindService;
import remindme.Table.TableDataManager;

public class ImportExportManager {

    private static final Logger logger = LoggerFactory.getLogger(ImportExportManager.class);

    public static List<Remind> importRemindListFromJson(MainGUI main, DateTimeFormatter formatter) {
        JFileChooser jfc = new JFileChooser(ConfigKey.RES_DIRECTORY_STRING.getValue());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));

        int returnValue = jfc.showOpenDialog(main);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.isFile() && selectedFile.getName().toLowerCase().endsWith(".json")) {
                logger.info("File imported: " + selectedFile);

                Preferences.setRemindList(new RemindListPath(selectedFile.getParent() + File.separator, selectedFile.getName()));
                Preferences.updatePreferencesToJson();

                try {
                    List<Remind> reminds = JSONReminder.readRemindListFromJSON(Preferences.getRemindList().directory(), Preferences.getRemindList().file());
                    TableDataManager.updateTableWithNewRemindList(reminds, formatter);

                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_LIST_CORRECTLY_IMPORTED_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_LIST_CORRECTLY_IMPORTED_TITLE), JOptionPane.INFORMATION_MESSAGE);

                    return reminds;
                } catch (IOException ex) {
                    logger.error("An error occurred: " + ex.getMessage(), ex);
                }
            } else {
                JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_TITLE), JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public static void exportRemindListToJson() {
        Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop", Preferences.getRemindList().file());
        Path sourcePath = Paths.get(Preferences.getRemindList().directory() + Preferences.getRemindList().file());

        try {
            Files.copy(sourcePath, desktopPath, StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(null,TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_LIST_CORRECTLY_EXPORTED_MESSAGE),TranslationCategory.DIALOGS.getTranslation(TranslationKey.REMIND_LIST_CORRECTLY_EXPORTED_TITLE),JOptionPane.INFORMATION_MESSAGE
            );
        } catch (java.nio.file.NoSuchFileException ex) {
            logger.error("Source file not found: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error: The source file was not found.\nPlease check the file path.", "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.nio.file.AccessDeniedException ex) {
            logger.error("Access denied to desktop: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error: Access to the Desktop is denied.\nPlease check folder permissions and try again.", "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            logger.error("Unexpected error: " + ex.getMessage());
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public static void exportRemindListAsPDF(MainController mainController, String headers) {
        logger.info("Exporting reminds to PDF");

        String path = mainController.pathSearchWithFileChooser(false);
        if (path == null) return;

        String filename = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.PDF_NAME_MESSAGE_INPUT));
        if (filename == null || filename.isEmpty()) return;

        if (!filename.matches("[a-zA-Z0-9-_ ]+")) {
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INVALID_FILENAME), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullPath = Paths.get(path, filename + ".pdf").toString();
        File file = new File(fullPath);
        if (file.exists() && JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_FILE_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdfDoc = new PdfDocument(writer);

            try (Document document = new Document(pdfDoc)) {
                document.add(new Paragraph(Preferences.getRemindList().file()).setFontSize(12f).setBold());

                String[] headerArray = headers.split(",");
                Table table = new Table(headerArray.length);

                for (String header : headerArray) {
                    table.addCell(new Cell().add(new Paragraph(header.trim())).setFontSize(8f));
                }

                List<Remind> reminds = RemindService.getReminds();
                if (reminds != null) {
                    for (Remind remind : reminds) {
                        for (String value : remind.toArrayString()) {
                            table.addCell(new Cell().add(new Paragraph(wrapText(value.trim(), 25))).setFontSize(5f));
                        }
                    }
                }

                document.add(table);
            }

            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.SUCCESSFULLY_EXPORTED_TO_PDF_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.SUCCESS_GENERIC_TITLE), JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            logger.error("Error exporting reminds to PDF: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_EXPORTING_TO_PDF) + ex.getMessage(), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void exportRemindListAsCSV(MainController mainController, String header) {
        logger.info("Exporting reminds to CSV");

        String path = mainController.pathSearchWithFileChooser(false);
        if (path == null) return;

        String filename = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CSV_NAME_MESSAGE_INPUT));
        if (filename == null || filename.isEmpty()) return;

        if (!filename.matches("[a-zA-Z0-9-_ ]+")) {
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INVALID_FILENAME), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullPath = Paths.get(path, filename + ".csv").toString();
        File file = new File(fullPath);
        if (file.exists() && JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_FILE_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try (FileWriter writer = new FileWriter(fullPath)) {
            if (header != null && !header.isEmpty()) writer.append(header).append("\n");

            List<Remind> reminds = RemindService.getReminds();
            if (reminds != null) {
                for (Remind remind : reminds) {
                    writer.append(Arrays.stream(remind.toArrayString())
                            .map(ImportExportManager::escapeCsv)
                            .collect(Collectors.joining(",")))
                          .append("\n");
                }
            }

            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.SUCCESSFULLY_EXPORTED_TO_CSV_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.SUCCESS_GENERIC_TITLE), JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            logger.error("Error exporting reminds to CSV: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_EXPORTING_TO_CSV) + ex.getMessage(), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String wrapText(String text, int max) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (char c : text.toCharArray()) {
            sb.append(c);
            count++;
            if (count == max) {
                sb.append('\n');
                count = 0;
            }
        }
        return sb.toString();
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
