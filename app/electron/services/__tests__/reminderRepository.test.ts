import * as fs from "fs";
import * as os from "os";
import * as path from "path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createDefaultRemind } from "../../types";
import { openDatabase } from "../database";
import { importLegacyJsonIfEmpty } from "../legacyImport";
import { ReminderRepository } from "../reminderRepository";

describe("ReminderRepository", () => {
  let dbPath: string;
  let repo: ReminderRepository;

  beforeEach(async () => {
    dbPath = path.join(fs.mkdtempSync(path.join(os.tmpdir(), "remindme-test-")), "reminders.db");
    repo = new ReminderRepository(await openDatabase(dbPath));
  });

  afterEach(() => {
    fs.rmSync(path.dirname(dbPath), { recursive: true, force: true });
  });

  it("inserts and retrieves a reminder", () => {
    const remind = { ...createDefaultRemind(), name: "Drink water" };
    repo.insert(remind);

    expect(repo.getByName("Drink water")).toEqual(remind);
    expect(repo.getAll()).toHaveLength(1);
  });

  it("duplicates a reminder appending _copy until the name is free", () => {
    repo.insert({ ...createDefaultRemind(), name: "Task" });
    repo.insert({ ...createDefaultRemind(), name: "Task_copy" });

    const duplicated = repo.duplicate("Task");

    expect(duplicated?.name).toBe("Task_copy_copy");
    expect(repo.getAll()).toHaveLength(3);
  });

  it("renames a reminder", () => {
    repo.insert({ ...createDefaultRemind(), name: "Old name" });
    repo.rename("Old name", "New name");

    expect(repo.getByName("Old name")).toBeNull();
    expect(repo.getByName("New name")).not.toBeNull();
  });

  it("removes a reminder", () => {
    repo.insert({ ...createDefaultRemind(), name: "Gone soon" });
    repo.remove("Gone soon");

    expect(repo.getByName("Gone soon")).toBeNull();
  });

  it("computes nextExecution when activated", () => {
    const remind = {
      ...createDefaultRemind(),
      name: "PC startup task",
      executionMethod: "PC_STARTUP" as const,
      timeInterval: { days: 0, hours: 1, minutes: 0 },
    };
    repo.insert(remind);

    repo.setActiveState("PC startup task", true, new Date(2024, 0, 1, 10, 0, 0));

    expect(repo.getByName("PC startup task")?.nextExecution).toBe("2024-01-01T11:00:00");
  });

  it("clears nextExecution when deactivated", () => {
    repo.insert({ ...createDefaultRemind(), name: "Task", isActive: true, nextExecution: "2024-01-01T11:00:00" });
    repo.setActiveState("Task", false);

    expect(repo.getByName("Task")?.nextExecution).toBeNull();
  });

  it("imports legacy JSON only when the database is empty", async () => {
    const jsonPath = path.join(path.dirname(dbPath), "legacy.json");
    fs.writeFileSync(
      jsonPath,
      JSON.stringify([
        {
          name: "Legacy reminder",
          description: "from java",
          remindCount: 3,
          isActive: true,
          isTopLevel: false,
          lastExecution: "2024-01-01T10:00:00",
          nextExecution: "2024-01-02T10:00:00",
          creationDate: "2023-12-01T10:00:00",
          lastUpdateDate: "2023-12-01T10:00:00",
          timeInterval: "1.0:0",
          icon: "ALERT",
          sound: "NO_SOUND",
          executionMethod: "PC_STARTUP",
          timeRange: null,
          maxExecutionPerDay: 0,
        },
      ]),
    );

    const imported = await importLegacyJsonIfEmpty(repo, jsonPath);

    expect(imported).toBe(1);
    expect(repo.getByName("Legacy reminder")?.timeInterval).toEqual({ days: 1, hours: 0, minutes: 0 });

    const importedAgain = await importLegacyJsonIfEmpty(repo, jsonPath);
    expect(importedAgain).toBe(0);
  });
});
