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
 * Seeds the bundled example reminders (res/suggestions_remind.json) into a
 * brand-new, still-empty database, so first-time users see curated examples
 * instead of a blank list. Only runs when the DB is empty (i.e. no legacy
 * JSON was migrated either), so it never overwrites a user's own data.
 */
public final class SuggestionsSeeder {

    private static final Logger logger = LoggerFactory.getLogger(SuggestionsSeeder.class);

    private SuggestionsSeeder() {
    }

    public static void seedIfEmpty(ReminderRepository repository, String directory, String filename) {
        if (!repository.getAll().isEmpty()) {
            return;
        }

        List<Remind> suggestions;
        try {
            suggestions = JSONReminder.readRemindListFromJSON(directory, filename);
        } catch (IOException ex) {
            logger.info("No suggestions file found at " + directory + filename + ", skipping seed");
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
