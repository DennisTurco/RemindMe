import * as fs from "fs/promises";
import type { ExecutionMethod, IconName, Remind, SoundName, TimeInterval, TimeRange } from "../types";
import { timeIntervalFromString } from "../types";

/** File > Esporta elenco: writes the current reminders as JSON, re-importable by this same app. */
export async function exportRemindListToJson(reminds: Remind[], filePath: string): Promise<void> {
  await fs.writeFile(filePath, JSON.stringify(reminds, null, 2), "utf-8");
}

interface RawRemindJson {
  name?: unknown;
  description?: unknown;
  remindCount?: unknown;
  isActive?: unknown;
  isTopLevel?: unknown;
  lastExecution?: unknown;
  nextExecution?: unknown;
  creationDate?: unknown;
  lastUpdateDate?: unknown;
  timeInterval?: unknown;
  icon?: unknown;
  sound?: unknown;
  executionMethod?: unknown;
  timeRange?: unknown;
  maxExecutionPerDay?: unknown;
}

function toTimeInterval(value: unknown): TimeInterval | null {
  if (!value) return null;
  if (typeof value === "string") return timeIntervalFromString(value);
  if (typeof value === "object") {
    const v = value as Record<string, unknown>;
    if (typeof v.days === "number" && typeof v.hours === "number" && typeof v.minutes === "number") {
      return { days: v.days, hours: v.hours, minutes: v.minutes };
    }
  }
  return null;
}

function toTimeRange(value: unknown): TimeRange | null {
  if (!value || typeof value !== "object") return null;
  const v = value as Record<string, unknown>;
  return typeof v.start === "string" && typeof v.end === "string" ? { start: v.start, end: v.end } : null;
}

function toRemind(raw: RawRemindJson): Remind | null {
  if (typeof raw.name !== "string" || raw.name.trim() === "") return null;

  return {
    name: raw.name,
    description: typeof raw.description === "string" ? raw.description : "",
    remindCount: typeof raw.remindCount === "number" ? raw.remindCount : 0,
    isActive: Boolean(raw.isActive),
    isTopLevel: Boolean(raw.isTopLevel),
    lastExecution: typeof raw.lastExecution === "string" ? raw.lastExecution : null,
    nextExecution: typeof raw.nextExecution === "string" ? raw.nextExecution : null,
    creationDate: typeof raw.creationDate === "string" ? raw.creationDate : null,
    lastUpdateDate: typeof raw.lastUpdateDate === "string" ? raw.lastUpdateDate : null,
    timeInterval: toTimeInterval(raw.timeInterval),
    icon: (typeof raw.icon === "string" ? raw.icon : "ALERT") as IconName,
    sound: (typeof raw.sound === "string" ? raw.sound : "NO_SOUND") as SoundName,
    executionMethod: (typeof raw.executionMethod === "string" ? raw.executionMethod : "PC_STARTUP") as ExecutionMethod,
    timeRange: toTimeRange(raw.timeRange),
    maxExecutionPerDay: typeof raw.maxExecutionPerDay === "number" ? raw.maxExecutionPerDay : 0,
  };
}

/**
 * File > Importa elenco: reads a JSON file (either this app's own export, or
 * a legacy Java remind_list*.json) and returns the parsed reminders. Throws
 * on anything that isn't a JSON array, so the caller can show an error
 * mirroring Java's "select a valid JSON file" message.
 */
export async function readRemindListFromJson(filePath: string): Promise<Remind[]> {
  const content = await fs.readFile(filePath, "utf-8");
  const raw = JSON.parse(content);
  if (!Array.isArray(raw)) {
    throw new Error("Invalid file: expected a JSON array of reminders");
  }
  const reminds = raw.map(toRemind).filter((r): r is Remind => r !== null);
  return reminds;
}
