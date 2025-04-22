package remindme.Table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import remindme.Svg.SVGLabel;

public class SvgImageRenderer extends DefaultTableCellRenderer {
    private final int iconWidth;
    private final int iconHeight;
    private final Color evenRowColor = new Color(223, 222, 243);
    private final Color oddRowColor = Color.WHITE;

    public SvgImageRenderer(int width, int height) {
        this.iconWidth = width;
        this.iconHeight = height;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object imagePath, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!(imagePath instanceof String)) {
            return super.getTableCellRendererComponent(table, imagePath, isSelected, hasFocus, row, column);
        }

        SVGLabel label;
        try {
            label = new SVGLabel((String) imagePath, iconWidth, iconHeight);
        } catch (Exception e) {
            label = new SVGLabel("icons/default.svg", iconWidth, iconHeight);
        }

        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setOpaque(true);

        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
        } else {
            if (row % 2 == 0) {
                label.setBackground(evenRowColor);
            } else {
                label.setBackground(oddRowColor);
            }
            label.setForeground(table.getForeground());
        }

        return label;
    }
}