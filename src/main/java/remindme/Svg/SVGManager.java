package remindme.Svg;

import java.awt.Color;
import java.awt.Component;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class SVGManager extends Component {

    // to update dinamically the svg color
    public static FlatSVGIcon applySvgColor(String svgImagePath, int svgWidth, int svgHeight, Color color) {
        Color contrastColor = getContrastingColor(color); // get contstrasting color based of the bg color
        FlatSVGIcon svgIcon = new FlatSVGIcon(svgImagePath, svgWidth, svgHeight);

        // color filter
        svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return contrastColor; // apply contrasting color
            }
        });

        return svgIcon;
    }

    public static Color getContrastingColor(Color bgColor) {
        int brightness = (int) Math.sqrt(
            bgColor.getRed() * bgColor.getRed() * 0.241 +
            bgColor.getGreen() * bgColor.getGreen() * 0.691 +
            bgColor.getBlue() * bgColor.getBlue() * 0.068
        );

        return (brightness > 130) ? Color.BLACK : Color.WHITE;
    }
}
