package remindme.Table;

import javax.swing.table.DefaultTableModel;

public class RemindTableModel extends DefaultTableModel {
    public RemindTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }
            
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 4 ? Boolean.class : super.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
