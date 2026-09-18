package remindme.Api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;

/**
 * Embedded HTTP server for the headless backend (MainApp --serve), consumed
 * by the Electron/React frontend (app/) instead of the old Swing GUI.
 * Mirrors how DailyPill's .NET backend exposes a local REST API to its
 * Electron frontend.
 */
public class ApiServer {

    /** Fixed local port the Electron frontend expects (see app/electron/apiClient.ts). */
    public static final int DEFAULT_PORT = 8765;

    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);

    private final Javalin app;

    public ApiServer(ReminderController controller) {
        app = Javalin.create(config -> {
            // Equivalent of DailyPill's Program.cs SetIsOriginAllowed(_ => true): the
            // renderer is served from http://localhost:5173 in dev and file:// in prod.
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));
        });

        app.get("/health", ctx -> ctx.result("ok"));

        app.get("/reminders/due", controller::getDue);
        app.get("/reminders/search", controller::search);
        app.get("/reminders/{name}", controller::getByName);
        app.get("/reminders", controller::getAll);
        app.post("/reminders", controller::create);
        app.put("/reminders/{name}", controller::update);
        app.delete("/reminders/{name}", controller::remove);
        app.post("/reminders/{name}/duplicate", controller::duplicate);
        app.post("/reminders/{name}/rename", controller::rename);
        app.post("/reminders/{name}/active", controller::setActive);
        app.post("/reminders/{name}/topLevel", controller::setTopLevel);

        app.get("/export/csv", controller::exportCsv);
        app.get("/export/json", controller::exportJson);
        app.post("/import/json", controller::importJson);
    }

    public void start(int port) {
        app.start(port);
        logger.info("RemindMe API listening on http://localhost:" + port);
    }

    public void stop() {
        app.stop();
    }
}
