import * as fs from "fs/promises";
import type { ExecutionMethod, IconName, Remind, SoundName, TimeRange } from "../types";
import { timeIntervalFromString } from "../types";
import { formatLocalDateTime } from "./dateTime";
import type { ReminderRepository } from "./reminderRepository";

/**
 * Curated example reminders bundled with the app (res/suggestions_remind.json),
 * shown to brand-new users instead of an empty list. Only entries needed to
 * recreate the reminder are kept in the file; bookkeeping fields
 * (remindCount, lastExecution, creation/update dates) are always set fresh
 * at seed time.
 */
interface SuggestionJson {
  name: string;
  description: string;
  isActive: boolean;
  isTopLevel: boolean;
  timeInterval: string | null;
  icon: string;
  sound: string;
  executionMethod: string;
  timeRange?: TimeRange | null;
  maxExecutionPerDay: number;
}

function suggestionToRemind(raw: SuggestionJson, now: string): Remind {
  return {
    name: raw.name,
    description: raw.description ?? "",
    remindCount: 0,
    isActive: !!raw.isActive,
    isTopLevel: !!raw.isTopLevel,
    lastExecution: null,
    nextExecution: null,
    creationDate: now,
    lastUpdateDate: now,
    timeInterval: raw.timeInterval ? timeIntervalFromString(raw.timeInterval) : null,
    icon: (raw.icon as IconName) ?? "ALERT",
    sound: (raw.sound as SoundName) ?? "NO_SOUND",
    executionMethod: (raw.executionMethod as ExecutionMethod) ?? "PC_STARTUP",
    timeRange: raw.timeRange ?? null,
    maxExecutionPerDay: raw.maxExecutionPerDay ?? 0,
  };
}

/**
 * Seeds the bundled suggestion reminders, but only when the database is
 * still empty (i.e. no legacy JSON was imported either) so an existing
 * user's own data is never overwritten with sample content.
 */
export async function seedSuggestionsIfEmpty(
  repository: ReminderRepository,
  suggestionsJsonPath: string,
): Promise<number> {
  if (repository.getAll().length > 0) {
    return 0;
  }

  let content: string;
  try {
    content = await fs.readFile(suggestionsJsonPath, "utf-8");
  } catch {
    return 0;
  }

  let raw: SuggestionJson[];
  try {
    raw = JSON.parse(content);
  } catch {
    return 0;
  }

  if (!Array.isArray(raw) || raw.length === 0) {
    return 0;
  }

  const now = formatLocalDateTime(new Date());
  const toInsert = raw.filter((entry) => entry?.name).map((entry) => suggestionToRemind(entry, now));

  repository.insertMany(toInsert);
  repository.recomputeAllNextExecutions();

  return toInsert.length;
}
