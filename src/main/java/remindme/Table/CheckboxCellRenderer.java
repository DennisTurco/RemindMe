package remindme.Table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CheckboxCellRenderer extends DefaultTableCellRenderer {
    private final JCheckBox checkBox = new JCheckBox();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Boolean aBoolean) {
            checkBox.setSelected(aBoolean);
            checkBox.setHorizontalAlignment(CENTER);

            if (row % 2 == 0) {
                checkBox.setBackground(new Color(223, 222, 243));
            } else {
                checkBox.setBackground(Color.WHITE);
            }

            if (isSelected) {
                checkBox.setBackground(table.getSelectionBackground());
                checkBox.setForeground(table.getSelectionForeground());
            } else {
                checkBox.setForeground(Color.BLACK);
            }

            return checkBox;
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
