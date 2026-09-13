import * as fs from "fs";
import * as os from "os";
import * as path from "path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createDefaultRemind } from "../../types";
import { openDatabase } from "../database";
import { ReminderRepository } from "../reminderRepository";
import { seedSuggestionsIfEmpty } from "../seedSuggestions";

const SUGGESTIONS_PATH = path.join(__dirname, "..", "..", "..", "res", "suggestions_remind.json");

describe("seedSuggestionsIfEmpty", () => {
  let dbPath: string;
  let repo: ReminderRepository;

  beforeEach(async () => {
    dbPath = path.join(fs.mkdtempSync(path.join(os.tmpdir(), "remindme-test-")), "reminders.db");
    repo = new ReminderRepository(await openDatabase(dbPath));
  });

  afterEach(() => {
    fs.rmSync(path.dirname(dbPath), { recursive: true, force: true });
  });

  it("seeds the bundled suggestions into an empty database", async () => {
    const inserted = await seedSuggestionsIfEmpty(repo, SUGGESTIONS_PATH);

    expect(inserted).toBe(6);
    const all = repo.getAll();
    expect(all).toHaveLength(6);
    expect(all.map((r) => r.name)).toContain("Drink water! Stay hydrated.");

    const bedTime = repo.getByName("Bed Time!");
    expect(bedTime?.executionMethod).toBe("ONE_TIME_PER_DAY");
    expect(bedTime?.timeRange).toEqual({ start: "22:30:00", end: "22:30:00" });
    expect(bedTime?.nextExecution).not.toBeNull();
  });

  it("does not seed when the database already has reminders", async () => {
    repo.insert({ ...createDefaultRemind(), name: "Existing" });

    const inserted = await seedSuggestionsIfEmpty(repo, SUGGESTIONS_PATH);

    expect(inserted).toBe(0);
    expect(repo.getAll()).toHaveLength(1);
  });
});
