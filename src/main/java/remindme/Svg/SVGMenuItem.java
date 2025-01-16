package remindme.Svg;

import javax.swing.JMenuItem;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class SVGMenuItem extends JMenuItem {

    public void setSvgImage(String imagePath, int width, int height) {
        if (imagePath == null) return;
        
        FlatSVGIcon svgIcon = SVGManager.applySvgColor(imagePath, width, height, getBackground());
        setIcon(svgIcon);
    }
}
