import * as fs from "fs/promises";
import type { ExecutionMethod, IconName, Remind, SoundName, TimeRange } from "../types";
import { timeIntervalFromString } from "../types";
import type { ReminderRepository } from "./reminderRepository";

/**
 * Raw shape of a Remind entry as written by the legacy Java app's Gson
 * serializer: same field names as our Remind type, except timeInterval is a
 * plain string ("d.hh:mm") rather than a {days,hours,minutes} object.
 */
interface LegacyRemindJson {
  name: string;
  description: string;
  remindCount: number;
  isActive: boolean;
  isTopLevel: boolean;
  lastExecution: string | null;
  nextExecution: string | null;
  creationDate: string | null;
  lastUpdateDate: string | null;
  timeInterval: string | null;
  icon: string;
  sound: string;
  executionMethod: string;
  timeRange: TimeRange | null;
  maxExecutionPerDay: number;
}

function legacyToRemind(raw: LegacyRemindJson): Remind {
  return {
    name: raw.name,
    description: raw.description ?? "",
    remindCount: raw.remindCount ?? 0,
    isActive: !!raw.isActive,
    isTopLevel: !!raw.isTopLevel,
    lastExecution: raw.lastExecution ?? null,
    nextExecution: raw.nextExecution ?? null,
    creationDate: raw.creationDate ?? null,
    lastUpdateDate: raw.lastUpdateDate ?? null,
    timeInterval: raw.timeInterval ? timeIntervalFromString(raw.timeInterval) : null,
    icon: (raw.icon as IconName) ?? "ALERT",
    sound: (raw.sound as SoundName) ?? "NO_SOUND",
    executionMethod: (raw.executionMethod as ExecutionMethod) ?? "PC_STARTUP",
    timeRange: raw.timeRange ?? null,
    maxExecutionPerDay: raw.maxExecutionPerDay ?? 0,
  };
}

/**
 * One-time migration from the legacy remind_list*.json file into SQLite.
 * Only runs when the database is empty, so it never overwrites data a user
 * has already started managing through the new app.
 */
export async function importLegacyJsonIfEmpty(repository: ReminderRepository, legacyJsonPath: string): Promise<number> {
  if (repository.getAll().length > 0) {
    return 0;
  }

  let content: string;
  try {
    content = await fs.readFile(legacyJsonPath, "utf-8");
  } catch {
    return 0;
  }

  let raw: LegacyRemindJson[];
  try {
    raw = JSON.parse(content);
  } catch {
    return 0;
  }

  if (!Array.isArray(raw) || raw.length === 0) {
    return 0;
  }

  const toImport = raw.filter((entry) => entry?.name && !repository.existsByName(entry.name)).map(legacyToRemind);
  repository.insertMany(toImport);

  return toImport.length;
}
