import * as fs from "fs/promises";
import * as fsSync from "fs";
import * as path from "path";

/**
 * Mirrors remindme.Json.JSONReminder: ensures the target file exists (creating
 * an empty JSON array if missing/empty) before reading, matching the Java
 * behaviour so first-run and pre-existing installs behave the same way.
 */
export async function ensureJsonFile(filePath: string, emptyValue: string): Promise<void> {
  await fs.mkdir(path.dirname(filePath), { recursive: true });
  let exists = true;
  try {
    await fs.access(filePath);
  } catch {
    exists = false;
  }
  if (!exists) {
    await fs.writeFile(filePath, emptyValue, "utf-8");
    return;
  }
  const stat = await fs.stat(filePath);
  if (stat.size === 0) {
    await fs.writeFile(filePath, emptyValue, "utf-8");
  }
}

export async function readJsonFile<T>(filePath: string, emptyValue: string): Promise<T> {
  await ensureJsonFile(filePath, emptyValue);
  const content = await fs.readFile(filePath, "utf-8");
  try {
    return JSON.parse(content) as T;
  } catch {
    return JSON.parse(emptyValue) as T;
  }
}

/**
 * Mirrors JSONReminder.updateRemindListJSON: write to a temp file then
 * rename atomically over the target, so a crash mid-write never corrupts data.
 */
export async function writeJsonFileAtomic(filePath: string, data: unknown): Promise<void> {
  await fs.mkdir(path.dirname(filePath), { recursive: true });
  const tempPath = `${filePath}.tmp`;
  await fs.writeFile(tempPath, JSON.stringify(data, null, 2), "utf-8");
  await fs.rename(tempPath, filePath);
}

export async function deleteTempFileIfExists(filePath: string): Promise<void> {
  const tempPath = `${filePath}.tmp`;
  try {
    await fs.unlink(tempPath);
  } catch {
    // Nothing to delete, mirrors Files.deleteIfExists.
  }
}

export function directoryExistsSync(directoryPath: string): boolean {
  try {
    return fsSync.statSync(directoryPath).isDirectory();
  } catch {
    return false;
  }
}
