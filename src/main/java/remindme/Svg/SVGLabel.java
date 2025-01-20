package remindme.Svg;

import javax.swing.JLabel;
import java.awt.Cursor;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class SVGLabel extends JLabel {

    public SVGLabel() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void setSvgImage(String imagePath, int width, int height) {
        if (imagePath == null) return;
        FlatSVGIcon svgIcon = new FlatSVGIcon(imagePath, width, height);
        setIcon(svgIcon);
    }
}

