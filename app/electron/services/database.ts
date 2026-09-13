import initSqlJs, { type Database as SqlJsDatabase } from "sql.js";
import * as fs from "fs";
import * as path from "path";

/**
 * Reminders live in SQLite (not JSON) because they are read/written both by
 * the background scheduler and the renderer UI; SQLite gives real query/
 * update semantics instead of the whole-file atomic-rename dance the old
 * Java JSON store needed. Uses sql.js (SQLite compiled to WASM) rather than
 * a native addon (e.g. better-sqlite3) so installing/building this app never
 * depends on a local C++ toolchain (node-gyp).
 */
export interface RemindMeDatabase {
  db: SqlJsDatabase;
  /** Flushes the in-memory database to disk (crash-safe: write temp file then rename). */
  persist(): void;
}

export async function openDatabase(dbFilePath: string): Promise<RemindMeDatabase> {
  fs.mkdirSync(path.dirname(dbFilePath), { recursive: true });

  const SQL = await initSqlJs();
  const existing = fs.existsSync(dbFilePath) ? fs.readFileSync(dbFilePath) : undefined;
  const db = new SQL.Database(existing);

  migrate(db);

  function persist(): void {
    const data = db.export();
    const tempPath = `${dbFilePath}.tmp`;
    fs.writeFileSync(tempPath, Buffer.from(data));
    fs.renameSync(tempPath, dbFilePath);
  }

  if (!existing) {
    persist();
  }

  return { db, persist };
}

function migrate(db: SqlJsDatabase): void {
  db.run(`
    CREATE TABLE IF NOT EXISTS reminders (
      name TEXT PRIMARY KEY,
      description TEXT NOT NULL DEFAULT '',
      remindCount INTEGER NOT NULL DEFAULT 0,
      isActive INTEGER NOT NULL DEFAULT 0,
      isTopLevel INTEGER NOT NULL DEFAULT 0,
      lastExecution TEXT,
      nextExecution TEXT,
      creationDate TEXT,
      lastUpdateDate TEXT,
      timeIntervalDays INTEGER,
      timeIntervalHours INTEGER,
      timeIntervalMinutes INTEGER,
      icon TEXT NOT NULL DEFAULT 'ALERT',
      sound TEXT NOT NULL DEFAULT 'NO_SOUND',
      executionMethod TEXT NOT NULL DEFAULT 'PC_STARTUP',
      timeRangeStart TEXT,
      timeRangeEnd TEXT,
      maxExecutionPerDay INTEGER NOT NULL DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS meta (
      key TEXT PRIMARY KEY,
      value TEXT NOT NULL
    );
  `);
}
