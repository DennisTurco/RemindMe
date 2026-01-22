package remindme.GUI.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Entities.Remind;
import remindme.Managers.ImportExportManager;
import remindme.Services.RemindService;

public class MainImportExportController {

    private static final Logger logger = LoggerFactory.getLogger(MainImportExportController.class);

    public static void importRemindListFromJSON(MainController mainController) {
        logger.info("Event --> importing remind list from json");
        List<Remind> newReminds = ImportExportManager.importRemindListFromJson(mainController.getMain(), RemindService.dateForfolderNameFormatter);

        // replace the current list with the imported one
        if (newReminds != null) {
            RemindService.setReminds(newReminds);
            RemindService.updateAllNextExecutions();
            MainController.updateTable();
        }
    }

    public static void exportRemindListTOJSON() {
        logger.info("Event --> exporting remind list to json");
        ImportExportManager.exportRemindListToJson();
    }

    public static void exportRemindListAsPDF(MainController mainController) {
        logger.info("Event --> exporting remind list as pdf");
        ImportExportManager.exportRemindListAsPDF(mainController, Remind.getCSVHeader());
    }

    public static void exportRemindListAsCSV(MainController mainController) {
        logger.info("Event --> exporting remind list as csv");
        ImportExportManager.exportRemindListAsCSV(mainController, Remind.getCSVHeader());
    }
}
