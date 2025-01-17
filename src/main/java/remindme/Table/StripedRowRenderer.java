package remindme.Table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class StripedRowRenderer extends DefaultTableCellRenderer {
    private final Color evenRowColor = new Color(223, 222, 243);
    private final Color oddRowColor = Color.WHITE;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Apply striped row colors
        if (row % 2 == 0) {
            c.setBackground(evenRowColor);
        } else {
            c.setBackground(oddRowColor);
        }

        // Handle selection
        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        } else {
            c.setForeground(Color.BLACK);
        }

        return c;
    }
}
