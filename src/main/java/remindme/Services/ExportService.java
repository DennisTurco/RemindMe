package remindme.Services;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import remindme.Entities.Remind;

/**
 * Headless port of remindme.Managers.ImportExportManager's CSV/PDF export
 * logic (minus the Swing file chooser / JOptionPane parts, which the
 * Electron frontend now handles via its own native save dialog). Exposed
 * over HTTP by the API so the frontend just needs the resulting bytes.
 */
public final class ExportService {

    private ExportService() {
    }

    public static String toCsv(List<Remind> reminds) {
        StringBuilder sb = new StringBuilder(Remind.getCSVHeader()).append("\n");
        for (Remind remind : reminds) {
            sb.append(Arrays.stream(remind.toArrayString())
                    .map(ExportService::escapeCsv)
                    .collect(Collectors.joining(",")))
                .append("\n");
        }
        return sb.toString();
    }

    public static byte[] toPdfBytes(List<Remind> reminds, String title) throws java.io.IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);

            try (Document document = new Document(pdfDoc)) {
                document.add(new Paragraph(title).setFontSize(12f).setBold());

                String[] headers = Remind.getCSVHeader().split(",");
                Table table = new Table(headers.length);

                for (String header : headers) {
                    table.addCell(new Cell().add(new Paragraph(header.trim())).setFontSize(8f));
                }

                for (Remind remind : reminds) {
                    for (String value : remind.toArrayString()) {
                        table.addCell(new Cell().add(new Paragraph(wrapText(value.trim(), 25))).setFontSize(5f));
                    }
                }

                document.add(table);
            }

            return out.toByteArray();
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
