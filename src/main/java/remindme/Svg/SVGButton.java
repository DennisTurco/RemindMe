package remindme.Svg;

import java.awt.Cursor;

import javax.swing.JButton;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class SVGButton extends JButton {

    public SVGButton() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setSvgImage(String imagePath, int width, int height) {
        if (imagePath == null) return;

        FlatSVGIcon svgIcon = SVGManager.applySvgColor(imagePath, width, height, getBackground());
        setIcon(svgIcon);
    }
}
