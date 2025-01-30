package remindme.Svg;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.JMenu;

public class SVGMenu extends JMenu {

    public void setSvgImage(String imagePath, int width, int height) {
        if (imagePath == null) return;
        
        FlatSVGIcon svgIcon = SVGManager.applySvgColor(imagePath, width, height, getBackground());
        setIcon(svgIcon);
    }
}

