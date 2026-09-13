import { app, BrowserWindow, Menu, dialog, ipcMain, nativeTheme } from "electron";
import * as path from "path";
import { buildAppMenu } from "./appMenu";
import { loadAppConfig } from "./services/appConfigService";
import { formatLocalDateTime } from "./services/dateTime";
import { openDatabase } from "./services/database";
import { exportToCsv, exportToPdf } from "./services/exportService";
import { DEFAULT_LANGUAGE, loadTranslations } from "./services/i18nService";
import type { LanguageCode, Translations } from "./services/i18nService";
import { importLegacyJsonIfEmpty } from "./services/legacyImport";
import { ReminderRepository } from "./services/reminderRepository";
import { seedSuggestionsIfEmpty } from "./services/seedSuggestions";
import { getNextExecutionBasedOnMethod } from "./services/timeIntervalService";
import type { Remind } from "./types";
import { createDefaultRemind } from "./types";

/**
 * Mirrors ManageRemind#getRemindInserted: nextExecution is (re)computed
 * whenever a reminder is created or edited via the form, regardless of the
 * active flag.
 */
function withComputedNextExecution(remind: Remind, now: Date = new Date()): Remind {
  const nextExecution = getNextExecutionBasedOnMethod(remind.executionMethod, remind.timeRange, remind.timeInterval, now);
  return { ...remind, nextExecution: nextExecution ? formatLocalDateTime(nextExecution) : null };
}

const isDev = !app.isPackaged;

let mainWindow: BrowserWindow | null = null;
let repository: ReminderRepository;
let currentLanguage: LanguageCode = DEFAULT_LANGUAGE;
let currentTranslations: Translations | null = null;

function getUserDataResDir(): string {
  return path.join(app.getPath("userData"), "res");
}

/** Directory of resources bundled with the app itself (read-only, not user data). */
function getBundledResDir(): string {
  return isDev ? path.join(__dirname, "..", "res") : path.join(process.resourcesPath, "res");
}

function getLanguagesDir(): string {
  return path.join(getBundledResDir(), "languages");
}

async function rebuildMenu(config: Awaited<ReturnType<typeof loadAppConfig>>): Promise<void> {
  try {
    currentTranslations = await loadTranslations(getLanguagesDir(), currentLanguage);
  } catch {
    currentTranslations = null;
  }
  Menu.setApplicationMenu(
    buildAppMenu({
      getMainWindow: () => mainWindow,
      getRepository: () => repository,
      config,
      translations: currentTranslations,
    }),
  );
}

async function initDatabase(): Promise<void> {
  const dbPath = path.join(getUserDataResDir(), "reminders.db");
  repository = new ReminderRepository(await openDatabase(dbPath));
  repository.recomputeAllNextExecutions();

  // Best-effort one-time import from a pre-existing Java-era remind list, if present.
  const legacyJsonPath = path.join(getUserDataResDir(), "remind_list1.2.2.json");
  await importLegacyJsonIfEmpty(repository, legacyJsonPath);

  // Brand-new install with no legacy data: seed the curated example reminders instead of an empty list.
  const suggestionsJsonPath = path.join(getBundledResDir(), "suggestions_remind.json");
  await seedSuggestionsIfEmpty(repository, suggestionsJsonPath);
}

/** Mirrors MainGUI's minimum window size (750x450). */
const MIN_WIDTH = 750;
const MIN_HEIGHT = 450;

function createMainWindow(): void {
  mainWindow = new BrowserWindow({
    width: 982,
    height: 715,
    minWidth: MIN_WIDTH,
    minHeight: MIN_HEIGHT,
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });

  if (isDev) {
    mainWindow.loadURL("http://localhost:5173");
  } else {
    mainWindow.loadFile(path.join(__dirname, "../dist/index.html"));
  }

  mainWindow.on("closed", () => {
    mainWindow = null;
  });
}

function registerIpcHandlers(config: Awaited<ReturnType<typeof loadAppConfig>>): void {
  ipcMain.handle("reminders:getAll", (): Remind[] => repository.getAll());

  ipcMain.handle("reminders:search", (_event, query: string): Remind[] => repository.search(query));

  ipcMain.handle("reminders:create", (_event, remind: Remind): Remind => {
    const now = formatLocalDateTime(new Date());
    const toInsert: Remind = withComputedNextExecution({
      ...createDefaultRemind(),
      ...remind,
      creationDate: remind.creationDate ?? now,
      lastUpdateDate: now,
    });
    repository.insert(toInsert);
    return toInsert;
  });

  ipcMain.handle("reminders:update", (_event, currentName: string, remind: Remind): Remind => {
    const updated: Remind = withComputedNextExecution({
      ...remind,
      lastUpdateDate: formatLocalDateTime(new Date()),
    });
    repository.update(currentName, updated);
    return updated;
  });

  ipcMain.handle("reminders:remove", (_event, name: string): void => repository.remove(name));

  ipcMain.handle("reminders:duplicate", (_event, name: string): Remind | null => repository.duplicate(name));

  ipcMain.handle("reminders:rename", (_event, currentName: string, newName: string): void =>
    repository.rename(currentName, newName),
  );

  ipcMain.handle(
    "reminders:setActive",
    (_event, name: string, isActive: boolean): void => repository.setActiveState(name, isActive),
  );

  ipcMain.handle(
    "reminders:setTopLevel",
    (_event, name: string, isTopLevel: boolean): void => repository.setTopLevelState(name, isTopLevel),
  );

  ipcMain.handle("reminders:exportCsv", async (): Promise<{ canceled: boolean; path?: string }> => {
    const win = mainWindow;
    if (!win) return { canceled: true };

    const { canceled, filePath } = await dialog.showSaveDialog(win, {
      title: "Esporta come CSV",
      defaultPath: "remind_list.csv",
      filters: [{ name: "CSV", extensions: ["csv"] }],
    });
    if (canceled || !filePath) return { canceled: true };

    exportToCsv(repository.getAll(), filePath);
    return { canceled: false, path: filePath };
  });

  ipcMain.handle("reminders:exportPdf", async (): Promise<{ canceled: boolean; path?: string }> => {
    const win = mainWindow;
    if (!win) return { canceled: true };

    const { canceled, filePath } = await dialog.showSaveDialog(win, {
      title: "Esporta come PDF",
      defaultPath: "remind_list.pdf",
      filters: [{ name: "PDF", extensions: ["pdf"] }],
    });
    if (canceled || !filePath) return { canceled: true };

    await exportToPdf(repository.getAll(), filePath, path.basename(filePath));
    return { canceled: false, path: filePath };
  });

  ipcMain.handle("app:setThemeSource", (_event, mode: "light" | "dark"): void => {
    nativeTheme.themeSource = mode;
  });

  ipcMain.handle("app:setLanguage", async (_event, language: LanguageCode): Promise<void> => {
    currentLanguage = language;
    await rebuildMenu(config);
  });
}

app.whenReady().then(async () => {
  await initDatabase();

  const configPath = path.join(getBundledResDir(), "config.json");
  const config = await loadAppConfig(configPath);

  registerIpcHandlers(config);
  await rebuildMenu(config);

  createMainWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createMainWindow();
    }
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
