package remindme.Table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import remindme.Svg.SVGLabel;

public class SvgImageRenderer extends DefaultTableCellRenderer {
    private final int iconWidth;
    private final int iconHeight;

    public SvgImageRenderer(int width, int height) {
        this.iconWidth = width;
        this.iconHeight = height;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object imagePath, boolean isSelected, boolean hasFocus, int row, int column) {
        if (imagePath == null || !(imagePath instanceof String)) {
            return super.getTableCellRendererComponent(table, imagePath, isSelected, hasFocus, row, column);
        }

        SVGLabel label = new SVGLabel((String) imagePath, iconWidth, iconHeight);
        label.setHorizontalAlignment(JLabel.CENTER);
        
        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
            label.setOpaque(true);
        } else {
            label.setOpaque(false);
        }

        return label;
    }
}