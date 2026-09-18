package remindme.Services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import remindme.Entities.Remind;

/**
 * Headless port of remindme.Managers.ImportExportManager's CSV export logic
 * (minus the Swing file chooser / JOptionPane parts, which the Electron
 * frontend now handles via its own native save dialog). Exposed over HTTP by
 * the API so the frontend just needs the resulting bytes.
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

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
