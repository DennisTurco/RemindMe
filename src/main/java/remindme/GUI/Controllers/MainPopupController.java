package remindme.GUI.Controllers;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Entities.Remind;

public class MainPopupController {

    private static final Logger logger = LoggerFactory.getLogger(MainPopupController.class);

    public static void popupRename(MainController mainController, javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
        if (remindName != null)
            mainController.renameRemind(Remind.getRemindByName(remindName));
    }

    public static void popupDelete(MainController mainController, javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
        if (remindName != null)
            mainController.removeReminder(Remind.getRemindByName(remindName), false);
    }

    public static void popupEdit(MainController mainController, javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
        if (remindName != null)
            mainController.editRemindViaDialog(Remind.getRemindByName(remindName));
    }

    public static void popupDuplicate(MainController mainController, javax.swing.JTable table) {
        String remindName = getRemindNameByTableRow(table);
        if (remindName != null)
            mainController.duplicateReminder(Remind.getRemindByName(remindName));
    }

    public void popupCopyRemindName(javax.swing.JTable table) {
        logger.info("Event --> copying reminder name to the clipboard");

        String remindName = getRemindNameByTableRow(table);
        Remind remind = Remind.getRemindByName(remindName);

        StringSelection selection = new StringSelection(remind.getName());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    public static void popupActive(MainController mainController, javax.swing.JTable table, javax.swing.JCheckBoxMenuItem activePopupItem) {
        boolean newState = activePopupItem.isSelected();
        activePopupItem.setSelected(newState);
        Remind remind = Remind.getRemindByName(getRemindNameByTableRow(table));
        mainController.switchActiveState(remind, newState);
    }

    public static void popupTopLevl(MainController mainController, javax.swing.JTable table, javax.swing.JCheckBoxMenuItem topLevelPopupItem) {
        boolean newState = topLevelPopupItem.isSelected();
        topLevelPopupItem.setSelected(newState);
        Remind remind = Remind.getRemindByName(getRemindNameByTableRow(table));
        mainController.switchTopLevelState(remind, newState);
    }

    private static String getRemindNameByTableRow(javax.swing.JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            return (String) table.getValueAt(selectedRow, 1);
        }

        return null;
    }
}