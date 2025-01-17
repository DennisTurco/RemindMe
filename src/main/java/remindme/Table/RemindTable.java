package remindme.Table;

import java.awt.Point;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class RemindTable extends JTable {
    public RemindTable(TableModel model) {
        super(model);
        setRowHeight(35);
        
        //! TODO: I disable table sorting bacause indexes are not correct when is active
        //setAutoCreateRowSorter(true); // Enable column sorting

        // Make sure the table is focusable
        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent e) {
        Point point = e.getPoint();
        int row = rowAtPoint(point);
        int col = columnAtPoint(point);

        if (col == 6) {
            Object value = getValueAt(row, col);
            return value != null ? "dd.HH:mm" : null;
        }
        return null;
    }
}
