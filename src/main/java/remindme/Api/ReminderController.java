package remindme.Api;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import io.javalin.http.Context;

import remindme.Entities.Remind;
import remindme.Json.Adapters.LocalDateTimeAdapter;
import remindme.Json.Adapters.LocalTimeAdapter;
import remindme.Services.ExportService;
import remindme.Services.SchedulingService;
import remindme.Services.TimeIntervalService;
import remindme.Sqlite.ReminderRepository;

/**
 * HTTP handlers for the reminder API. Wire format mirrors Gson's default
 * serialization of Remind (same field names / enum-as-name-string / date
 * strings the Java Swing app already used), but WITHOUT the TimeIntervalAdapter
 * so timeInterval/timeRange serialize as plain {days,hours,minutes} /
 * {start,end} objects, matching the TypeScript frontend's Remind type
 * one-to-one (no frontend changes needed for this pivot).
 */
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        // Without this, Gson omits null fields entirely; the frontend's Remind
        // type expects e.g. lastExecution/timeRange to always be present (string | null).
        .serializeNulls()
        .create();

    private final ReminderRepository repository;

    public ReminderController(ReminderRepository repository) {
        this.repository = repository;
    }

    public void getAll(Context ctx) {
        json(ctx, repository.getAll());
    }

    public void search(Context ctx) {
        String query = ctx.queryParam("q");
        json(ctx, repository.search(query != null ? query : ""));
    }

    public void getDue(Context ctx) {
        List<Remind> due = SchedulingService.getRemindsToExecute(repository.getAll(), 1);
        for (Remind remind : due) {
            repository.markShown(remind.getName());
        }
        json(ctx, due);
    }

    public void getByName(Context ctx) {
        Remind remind = repository.getByName(ctx.pathParam("name"));
        if (remind == null) {
            ctx.status(404);
            return;
        }
        json(ctx, remind);
    }

    public void create(Context ctx) {
        Remind body = GSON.fromJson(ctx.body(), Remind.class);

        if (body.getName() == null || body.getName().isBlank()) {
            ctx.status(400).result("Remind name cannot be empty");
            return;
        }
        if (repository.existsByName(body.getName())) {
            ctx.status(409).result("A reminder with this name already exists");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (body.getCreationDate() == null) {
            body.setCreationDate(now);
        }
        body.setLastUpdateDate(now);
        body.setNextExecution(TimeIntervalService.getNextExecutionBasedOnMethod(
            body.getExecutionMethod(), body.getTimeRange(), body.getTimeInterval()));

        repository.insert(body);
        json(ctx, body);
    }

    public void update(Context ctx) {
        String currentName = ctx.pathParam("name");
        Remind body = GSON.fromJson(ctx.body(), Remind.class);

        body.setLastUpdateDate(LocalDateTime.now());
        body.setNextExecution(TimeIntervalService.getNextExecutionBasedOnMethod(
            body.getExecutionMethod(), body.getTimeRange(), body.getTimeInterval()));

        repository.update(currentName, body);
        json(ctx, body);
    }

    public void remove(Context ctx) {
        repository.remove(ctx.pathParam("name"));
        ctx.status(204);
    }

    public void duplicate(Context ctx) {
        Remind duplicated = repository.duplicate(ctx.pathParam("name"));
        if (duplicated == null) {
            ctx.status(404);
            return;
        }
        json(ctx, duplicated);
    }

    public void rename(Context ctx) {
        JsonObject body = GSON.fromJson(ctx.body(), JsonObject.class);
        String newName = body.get("newName").getAsString();
        repository.rename(ctx.pathParam("name"), newName);
        ctx.status(204);
    }

    public void setActive(Context ctx) {
        JsonObject body = GSON.fromJson(ctx.body(), JsonObject.class);
        repository.setActiveState(ctx.pathParam("name"), body.get("value").getAsBoolean());
        ctx.status(204);
    }

    public void setTopLevel(Context ctx) {
        JsonObject body = GSON.fromJson(ctx.body(), JsonObject.class);
        repository.setTopLevelState(ctx.pathParam("name"), body.get("value").getAsBoolean());
        ctx.status(204);
    }

    public void exportCsv(Context ctx) {
        ctx.contentType("text/csv").result(ExportService.toCsv(repository.getAll()));
    }

    public void exportPdf(Context ctx) {
        try {
            byte[] bytes = ExportService.toPdfBytes(repository.getAll(), "RemindMe");
            ctx.contentType("application/pdf").result(bytes);
        } catch (java.io.IOException ex) {
            logger.error("Failed to generate PDF export: " + ex.getMessage(), ex);
            ctx.status(500).result("Failed to generate PDF");
        }
    }

    public void exportJson(Context ctx) {
        json(ctx, repository.getAll());
    }

    public void importJson(Context ctx) {
        Remind[] reminds = GSON.fromJson(ctx.body(), Remind[].class);
        repository.replaceAll(List.of(reminds));
        ctx.status(204);
    }

    private void json(Context ctx, Object value) {
        ctx.contentType("application/json").result(GSON.toJson(value));
    }
}
