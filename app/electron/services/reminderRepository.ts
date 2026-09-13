import type { Database as SqlJsDatabase } from "sql.js";
import type { ExecutionMethod, IconName, Remind, SoundName } from "../types";
import type { RemindMeDatabase } from "./database";
import { formatLocalDateTime } from "./dateTime";
import {
  getNextExecutionBasedOnMethod,
  getNextExecutionByTimeInterval,
  getNextExecutionByTimeIntervalFromSpecificTime,
} from "./timeIntervalService";

interface RemindRow {
  name: string;
  description: string;
  remindCount: number;
  isActive: number;
  isTopLevel: number;
  lastExecution: string | null;
  nextExecution: string | null;
  creationDate: string | null;
  lastUpdateDate: string | null;
  timeIntervalDays: number | null;
  timeIntervalHours: number | null;
  timeIntervalMinutes: number | null;
  icon: string;
  sound: string;
  executionMethod: string;
  timeRangeStart: string | null;
  timeRangeEnd: string | null;
  maxExecutionPerDay: number;
}

const REMIND_COLUMNS = [
  "name",
  "description",
  "remindCount",
  "isActive",
  "isTopLevel",
  "lastExecution",
  "nextExecution",
  "creationDate",
  "lastUpdateDate",
  "timeIntervalDays",
  "timeIntervalHours",
  "timeIntervalMinutes",
  "icon",
  "sound",
  "executionMethod",
  "timeRangeStart",
  "timeRangeEnd",
  "maxExecutionPerDay",
] as const;

function rowToRemind(row: RemindRow): Remind {
  return {
    name: row.name,
    description: row.description,
    remindCount: row.remindCount,
    isActive: !!row.isActive,
    isTopLevel: !!row.isTopLevel,
    lastExecution: row.lastExecution,
    nextExecution: row.nextExecution,
    creationDate: row.creationDate,
    lastUpdateDate: row.lastUpdateDate,
    timeInterval:
      row.timeIntervalDays !== null && row.timeIntervalHours !== null && row.timeIntervalMinutes !== null
        ? { days: row.timeIntervalDays, hours: row.timeIntervalHours, minutes: row.timeIntervalMinutes }
        : null,
    icon: row.icon as IconName,
    sound: row.sound as SoundName,
    executionMethod: row.executionMethod as ExecutionMethod,
    timeRange:
      row.timeRangeStart !== null && row.timeRangeEnd !== null
        ? { start: row.timeRangeStart, end: row.timeRangeEnd }
        : null,
    maxExecutionPerDay: row.maxExecutionPerDay,
  };
}

function remindToRowValues(remind: Remind): (string | number | null)[] {
  return [
    remind.name,
    remind.description,
    remind.remindCount,
    remind.isActive ? 1 : 0,
    remind.isTopLevel ? 1 : 0,
    remind.lastExecution,
    remind.nextExecution,
    remind.creationDate,
    remind.lastUpdateDate,
    remind.timeInterval?.days ?? null,
    remind.timeInterval?.hours ?? null,
    remind.timeInterval?.minutes ?? null,
    remind.icon,
    remind.sound,
    remind.executionMethod,
    remind.timeRange?.start ?? null,
    remind.timeRange?.end ?? null,
    remind.maxExecutionPerDay,
  ];
}

function queryAll(db: SqlJsDatabase, sql: string, params: (string | number | null)[] = []): RemindRow[] {
  const stmt = db.prepare(sql);
  stmt.bind(params as never);
  const rows: RemindRow[] = [];
  while (stmt.step()) {
    rows.push(stmt.getAsObject() as unknown as RemindRow);
  }
  stmt.free();
  return rows;
}

function queryOne(db: SqlJsDatabase, sql: string, params: (string | number | null)[] = []): RemindRow | null {
  const rows = queryAll(db, sql, params);
  return rows[0] ?? null;
}

export class ReminderRepository {
  private readonly db: SqlJsDatabase;

  constructor(private readonly handle: RemindMeDatabase) {
    this.db = handle.db;
  }

  getAll(): Remind[] {
    return queryAll(this.db, "SELECT * FROM reminders ORDER BY creationDate ASC").map(rowToRemind);
  }

  getByName(name: string): Remind | null {
    const row = queryOne(this.db, "SELECT * FROM reminders WHERE name = ?", [name]);
    return row ? rowToRemind(row) : null;
  }

  existsByName(name: string): boolean {
    return this.getByName(name) !== null;
  }

  search(query: string): Remind[] {
    const like = `%${query.toLowerCase()}%`;
    return queryAll(this.db, "SELECT * FROM reminders WHERE LOWER(name) LIKE ? ORDER BY creationDate ASC", [
      like,
    ]).map(rowToRemind);
  }

  insert(remind: Remind): void {
    this.insertNoPersist(remind);
    this.handle.persist();
  }

  /** Bulk import (e.g. legacy JSON migration): inserts many rows, persisting to disk once at the end. */
  insertMany(reminds: Remind[]): void {
    for (const remind of reminds) {
      this.insertNoPersist(remind);
    }
    if (reminds.length > 0) {
      this.handle.persist();
    }
  }

  /** Replaces the entire list (File > Importa elenco): mirrors switching to a different remind list file in Java. */
  replaceAll(reminds: Remind[]): void {
    this.db.run("DELETE FROM reminders");
    for (const remind of reminds) {
      this.insertNoPersist(remind);
    }
    this.handle.persist();
  }

  private insertNoPersist(remind: Remind): void {
    const placeholders = REMIND_COLUMNS.map(() => "?").join(", ");
    this.db.run(
      `INSERT INTO reminders (${REMIND_COLUMNS.join(", ")}) VALUES (${placeholders})`,
      remindToRowValues(remind) as never,
    );
  }

  /** Full replace of every field, mirroring Java's Remind#updateRemind (name included, so this can rename too). */
  update(currentName: string, remind: Remind): void {
    const assignments = REMIND_COLUMNS.map((c) => `${c} = ?`).join(", ");
    this.db.run(`UPDATE reminders SET ${assignments} WHERE name = ?`, [
      ...remindToRowValues(remind),
      currentName,
    ] as never);
    this.handle.persist();
  }

  remove(name: string): void {
    this.db.run("DELETE FROM reminders WHERE name = ?", [name]);
    this.handle.persist();
  }

  rename(currentName: string, newName: string, now: Date = new Date()): void {
    this.db.run("UPDATE reminders SET name = ?, lastUpdateDate = ? WHERE name = ?", [
      newName,
      formatLocalDateTime(now),
      currentName,
    ]);
    this.handle.persist();
  }

  duplicate(sourceName: string, now: Date = new Date()): Remind | null {
    const source = this.getByName(sourceName);
    if (!source) return null;

    let newName = source.name;
    do {
      newName += "_copy";
    } while (this.existsByName(newName));

    const duplicated: Remind = {
      ...source,
      name: newName,
      remindCount: 0,
      creationDate: formatLocalDateTime(now),
      lastUpdateDate: formatLocalDateTime(now),
    };
    this.insert(duplicated);
    return duplicated;
  }

  setActiveState(name: string, isActive: boolean, now: Date = new Date()): void {
    const remind = this.getByName(name);
    if (!remind) return;

    const nextExecution = isActive
      ? getNextExecutionBasedOnMethod(remind.executionMethod, remind.timeRange, remind.timeInterval, now)
      : null;

    this.db.run("UPDATE reminders SET isActive = ?, nextExecution = ? WHERE name = ?", [
      isActive ? 1 : 0,
      nextExecution ? formatLocalDateTime(nextExecution) : null,
      name,
    ]);
    this.handle.persist();
  }

  setTopLevelState(name: string, isTopLevel: boolean): void {
    this.db.run("UPDATE reminders SET isTopLevel = ? WHERE name = ?", [isTopLevel ? 1 : 0, name]);
    this.handle.persist();
  }

  /** Mirrors RemindService.updateRemindAfterShow: called once a notification popup has been shown. */
  markShown(name: string, now: Date = new Date()): void {
    const remind = this.getByName(name);
    if (!remind) return;

    let nextExecution: Date | null;
    switch (remind.executionMethod) {
      case "ONE_TIME_PER_DAY": {
        const tomorrow = new Date(now);
        tomorrow.setDate(tomorrow.getDate() + 1);
        nextExecution = remind.timeRange ? combineDateAtTime(tomorrow, remind.timeRange.start) : null;
        break;
      }
      case "CUSTOM_TIME_RANGE":
        nextExecution = getNextExecutionByTimeIntervalFromSpecificTime(
          remind.timeInterval,
          remind.timeRange?.start ?? null,
          now,
        );
        break;
      case "PC_STARTUP":
      default:
        nextExecution = getNextExecutionByTimeInterval(remind.timeInterval, now);
        break;
    }

    this.db.run("UPDATE reminders SET lastExecution = ?, remindCount = remindCount + 1, nextExecution = ? WHERE name = ?", [
      formatLocalDateTime(now),
      nextExecution ? formatLocalDateTime(nextExecution) : null,
      name,
    ]);
    this.handle.persist();
  }

  /** Mirrors RemindService.updateAllNextExecutions, run once at startup. */
  recomputeAllNextExecutions(now: Date = new Date()): void {
    const nowTimeOfDay = formatTimeOfDay(now);
    const active = this.getAll().filter((r) => r.isActive);

    for (const remind of active) {
      let nextExecution: Date | null;
      switch (remind.executionMethod) {
        case "PC_STARTUP":
          nextExecution = getNextExecutionByTimeIntervalFromSpecificTime(remind.timeInterval, nowTimeOfDay, now);
          break;
        case "CUSTOM_TIME_RANGE": {
          const inRange =
            remind.timeRange !== null &&
            nowTimeOfDay >= remind.timeRange.start &&
            nowTimeOfDay <= remind.timeRange.end;
          const reference = inRange ? nowTimeOfDay : remind.timeRange?.start ?? nowTimeOfDay;
          nextExecution = getNextExecutionByTimeIntervalFromSpecificTime(remind.timeInterval, reference, now);
          break;
        }
        case "ONE_TIME_PER_DAY": {
          if (!remind.timeRange) {
            nextExecution = null;
            break;
          }
          const dayOffset = nowTimeOfDay < remind.timeRange.start ? 0 : 1;
          const day = new Date(now);
          day.setDate(day.getDate() + dayOffset);
          nextExecution = combineDateAtTime(day, remind.timeRange.start);
          break;
        }
        default:
          nextExecution = null;
      }

      this.db.run("UPDATE reminders SET nextExecution = ? WHERE name = ?", [
        nextExecution ? formatLocalDateTime(nextExecution) : null,
        remind.name,
      ]);
    }

    if (active.length > 0) {
      this.handle.persist();
    }
  }
}

function formatTimeOfDay(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function combineDateAtTime(date: Date, timeOfDay: string): Date {
  const [h, m, s] = timeOfDay.split(":").map(Number);
  const result = new Date(date);
  result.setHours(h, m, s || 0, 0);
  return result;
}
