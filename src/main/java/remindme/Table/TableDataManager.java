package remindme.Table;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.SwingUtilities;

import remindme.Logger;
import remindme.Entities.Remind;
import remindme.GUI.MainGUI;

public class TableDataManager {

    public static void updateTableWithNewRemindList(List<Remind> updatedReminds, DateTimeFormatter formatter) { 
        Logger.logMessage("updating remind list", Logger.LogLevel.DEBUG);
        
        SwingUtilities.invokeLater(() -> {
            MainGUI.model.setRowCount(0);

            for (Remind remind : updatedReminds) {
                MainGUI.model.addRow(new Object[]{
                    remind.getName(),
                    remind.isActive(),
                    remind.isTopLevel(),
                    remind.getLastExecution() != null ? remind.getLastExecution().format(formatter) : "",
                    remind.getNextExecution() != null ? remind.getNextExecution().format(formatter) : "",
                    remind.getTimeInterval() != null ? remind.getTimeInterval().toString() : ""
                });
            }
        });
    }

    private static int findRemindRowIndex(Remind remind, RemindTable table) {
        if (remind == null) throw new IllegalArgumentException("Remind cannot be null");
        if (table == null) throw new IllegalArgumentException("Table cannot be null");

        for (int i = 0; i < table.getRowCount(); i++) {
            if (table.getValueAt(i, 0).equals(remind.getName())) { // first column holds unique remind names
                return i;
            }
        }
        return -1;
    }   
}
