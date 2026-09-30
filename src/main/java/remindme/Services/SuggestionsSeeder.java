package remindme.Services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Entities.Remind;
import remindme.Json.JSONReminder;
import remindme.Sqlite.ReminderRepository;

/**
 * Seeds the bundled example reminders (classpath resource
 * /res/suggestions_remind.json, embedded in the jar) into a brand-new,
 * still-empty database, so first-time users see curated examples instead of
 * a blank list. Only runs when the DB is empty (i.e. no legacy JSON was
 * migrated either), so it never overwrites a user's own data. Loaded from
 * the classpath rather than the filesystem so it keeps working regardless
 * of the process's working directory in a packaged install.
 */
public final class SuggestionsSeeder {

    private static final Logger logger = LoggerFactory.getLogger(SuggestionsSeeder.class);

    private SuggestionsSeeder() {
    }

    public static void seedIfEmpty(ReminderRepository repository, String classpathResource) {
        if (!repository.getAll().isEmpty()) {
            return;
        }

        List<Remind> suggestions;
        try {
            suggestions = JSONReminder.readRemindListFromClasspath(classpathResource);
        } catch (IOException ex) {
            logger.info("No suggestions file found at " + classpathResource + ", skipping seed");
            return;
        }

        if (suggestions.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Remind remind : suggestions) {
            remind.setRemindCount(0);
            remind.setLastExecution(null);
            remind.setCreationDate(now);
            remind.setLastUpdateDate(now);
        }

        repository.insertMany(suggestions);
        repository.recomputeAllNextExecutions();
        logger.info("Seeded " + suggestions.size() + " example reminders");
    }
}
