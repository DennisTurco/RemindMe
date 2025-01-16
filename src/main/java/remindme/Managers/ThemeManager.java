package remindme.Managers;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.Arrays;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme;

import remindme.Entities.Preferences;
import remindme.Logger;

// https://www.formdev.com/flatlaf/#demo
// https://www.formdev.com/flatlaf/themes/
// https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-intellij-themes

public class ThemeManager {

    public static void updateThemeFrame(Frame frame) {
        updateTheme();
        repaint(frame);
    }

    public static void refreshPopup(JPopupMenu popup) {
        repaint(popup);
    }

    public static void updateThemeDialog(Dialog dialog) {
        updateTheme();
        repaint(dialog);
    }

    private static void repaint(Object objectToRepaint) {
        if (objectToRepaint instanceof Dialog || objectToRepaint instanceof JPopupMenu || objectToRepaint instanceof Frame) {
            // Update all components and revalidate and repaint
            SwingUtilities.updateComponentTreeUI((Component) objectToRepaint);
            ((Component) objectToRepaint).revalidate();
            ((Component) objectToRepaint).repaint();
        } else {
            throw new IllegalArgumentException("Unsupported object type for repainting: " + objectToRepaint.getClass().getName());
        }
    }

    private static void updateTheme() {
        try {
            String selectedTheme = Preferences.getTheme().getThemeName();

            switch (selectedTheme.toLowerCase()) {
                case "light":
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    break;
                case "dark":
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                    break;
                case "carbon":
                    UIManager.setLookAndFeel(new FlatCarbonIJTheme());
                    break;
                case "arc - orange":
                    UIManager.setLookAndFeel(new FlatArcOrangeIJTheme());
                    break;
                case "arc dark - orange":
                    UIManager.setLookAndFeel(new FlatArcDarkOrangeIJTheme());
                    break;
                case "cyan light":
                    UIManager.setLookAndFeel(new FlatCyanLightIJTheme());
                    break;
                case "nord":
                    UIManager.setLookAndFeel(new FlatNordIJTheme());
                    break;
                case "high contrast":
                    UIManager.setLookAndFeel(new FlatHighContrastIJTheme());
                    break;
                case "solarized dark":
                    UIManager.setLookAndFeel(new FlatSolarizedDarkIJTheme());
                    break;
                case "solarized light":
                    UIManager.setLookAndFeel(new FlatSolarizedLightIJTheme());
                    break;
                default:
                    // If no match, apply the default theme
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    break;
            }

        } catch (UnsupportedLookAndFeelException ex) {
            Logger.logMessage("Error setting LookAndFeel: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}