import { app, BrowserWindow, Menu, dialog, ipcMain, nativeTheme } from "electron";
import { spawn, type ChildProcess } from "child_process";
import * as fs from "fs/promises";
import * as path from "path";
import { apiClient } from "./apiClient";
import { buildAppMenu } from "./appMenu";
import { loadAppConfig } from "./services/appConfigService";
import { DEFAULT_LANGUAGE, loadTranslations } from "./services/i18nService";
import type { LanguageCode, Translations } from "./services/i18nService";
import { setupTray, showAndFocus } from "./tray";
import type { AppTray } from "./tray";
import type { Remind } from "./types";

const isDev = !app.isPackaged;

let mainWindow: BrowserWindow | null = null;
let poller: DuePoller;
let appTray: AppTray | null = null;
let currentLanguage: LanguageCode = DEFAULT_LANGUAGE;
let currentTranslations: Translations | null = null;
let isQuitting = false;
let backendProcess: ChildProcess | null = null;

const openPopups = new Map<string, BrowserWindow>();

/** Directory of resources bundled with the app itself (read-only, not user data). */
function getBundledResDir(): string {
  return isDev ? path.join(__dirname, "..", "res") : path.join(process.resourcesPath, "res");
}

function getLanguagesDir(): string {
  return path.join(getBundledResDir(), "languages");
}

async function refreshTranslations(): Promise<void> {
  try {
    currentTranslations = await loadTranslations(getLanguagesDir(), currentLanguage);
  } catch {
    currentTranslations = null;
  }
}

function quitApp(): void {
  isQuitting = true;
  app.quit();
}

async function rebuildMenuAndTray(config: Awaited<ReturnType<typeof loadAppConfig>>): Promise<void> {
  await refreshTranslations();
  Menu.setApplicationMenu(
    buildAppMenu({
      getMainWindow: () => mainWindow,
      config,
      translations: currentTranslations,
      quit: quitApp,
    }),
  );
  appTray?.refresh();
}

/**
 * Polls the Java backend's GET /reminders/due on a fixed interval, replacing
 * the old in-process SchedulerService now that scheduling logic lives
 * server-side (remindme.Services.SchedulingService). Mirrors DailyPill's
 * checkSchedules/checkDailyFact polling pattern in its Electron main.ts.
 */
class DuePoller {
  private timer: ReturnType<typeof setInterval> | null = null;
  private paused = false;

  constructor(
    private readonly intervalMs: number,
    private readonly onDue: (remind: Remind) => void,
  ) {}

  start(): void {
    if (this.timer) return;
    void this.tick();
    this.timer = setInterval(() => void this.tick(), this.intervalMs);
  }

  stop(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  pause(): void {
    this.paused = true;
  }

  resume(): void {
    this.paused = false;
  }

  isPaused(): boolean {
    return this.paused;
  }

  private async tick(): Promise<void> {
    if (this.paused) return;
    try {
      const due = await apiClient.getDue();
      due.forEach((remind) => this.onDue(remind));
      // The backend already updated lastExecution/nextExecution for these
      // reminders as a side effect of GET /reminders/due (see markShown in
      // ReminderRepository); tell the main window to refresh so its table
      // reflects the new dates instead of staying stale until a manual search.
      if (due.length > 0) {
        mainWindow?.webContents.send("reminders:changed");
      }
    } catch {
      // Backend not reachable yet (e.g. still starting up in dev); try again next tick.
    }
  }
}

/** Mirrors MainGUI's minimum window size (750x450). */
const MIN_WIDTH = 750;
const MIN_HEIGHT = 450;

function getAppIconPath(): string {
  return path.join(getBundledResDir(), "img", process.platform === "win32" ? "logo.ico" : "logo.png");
}

function loadRoute(win: BrowserWindow, hash: string): void {
  if (isDev) {
    win.loadURL(`http://localhost:5173/#${hash}`);
  } else {
    win.loadFile(path.join(__dirname, "../dist/index.html"), { hash });
  }
}

function createMainWindow(): void {
  mainWindow = new BrowserWindow({
    width: 982,
    height: 715,
    minWidth: MIN_WIDTH,
    minHeight: MIN_HEIGHT,
    icon: getAppIconPath(),
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });

  loadRoute(mainWindow, "/");

  // Closing the window hides it instead of quitting: RemindMe keeps checking
  // for due reminders in the background, mirroring the Java app's tray-driven
  // background service (see execute_background_service.bat / "MainApp Background").
  mainWindow.on("close", (event) => {
    if (!isQuitting) {
      event.preventDefault();
      mainWindow?.hide();
    }
  });

  mainWindow.on("closed", () => {
    mainWindow = null;
  });
}

/** Opens (or refocuses) the notification popup for a due reminder. Mirrors ReminderDialog + BackgroundService's openedDialogs map. */
function openReminderPopup(remind: Remind): void {
  const existing = openPopups.get(remind.name);
  if (existing && !existing.isDestroyed()) {
    existing.close();
    openPopups.delete(remind.name);
  }

  const popup = new BrowserWindow({
    width: 420,
    height: 280,
    resizable: false,
    alwaysOnTop: remind.isTopLevel,
    autoHideMenuBar: true,
    icon: getAppIconPath(),
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });

  loadRoute(popup, `/popup/reminder?name=${encodeURIComponent(remind.name)}`);

  popup.on("closed", () => openPopups.delete(remind.name));
  openPopups.set(remind.name, popup);
}

/**
 * Launches the Java backend as a child process in production, mirroring
 * DailyPill's spawnBackend(): in dev the backend is expected to already be
 * running (started manually or via the ".vscode/launch.json" "MainApp Serve"
 * config), same as DailyPill's `dotnet run` during development.
 */
function spawnBackend(): void {
  if (isDev) return;

  const javaExe = path.join(process.resourcesPath, "jre", "bin", process.platform === "win32" ? "java.exe" : "java");
  const jarPath = path.join(process.resourcesPath, "backend", "RemindMe.jar");
  const cwd = path.join(process.resourcesPath, "backend");

  backendProcess = spawn(javaExe, ["-jar", jarPath, "--serve"], { cwd, windowsHide: true });
  backendProcess.stdout?.on("data", (chunk) => console.log(`[backend] ${chunk}`));
  backendProcess.stderr?.on("data", (chunk) => console.error(`[backend] ${chunk}`));
}

function registerIpcHandlers(config: Awaited<ReturnType<typeof loadAppConfig>>): void {
  ipcMain.handle("reminders:getAll", () => apiClient.getAll());

  ipcMain.handle("reminders:getByName", (_event, name: string) => apiClient.getByName(name));

  ipcMain.handle("reminders:search", (_event, query: string) => apiClient.search(query));

  ipcMain.handle("reminders:create", (_event, remind: Remind) => apiClient.create(remind));

  ipcMain.handle("reminders:update", (_event, currentName: string, remind: Remind) =>
    apiClient.update(currentName, remind),
  );

  ipcMain.handle("reminders:remove", (_event, name: string) => apiClient.remove(name));

  ipcMain.handle("reminders:duplicate", (_event, name: string) => apiClient.duplicate(name));

  ipcMain.handle("reminders:rename", (_event, currentName: string, newName: string) =>
    apiClient.rename(currentName, newName),
  );

  ipcMain.handle("reminders:setActive", (_event, name: string, isActive: boolean) => apiClient.setActive(name, isActive));

  ipcMain.handle("reminders:setTopLevel", (_event, name: string, isTopLevel: boolean) =>
    apiClient.setTopLevel(name, isTopLevel),
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

    const csv = await apiClient.exportCsv();
    await fs.writeFile(filePath, csv, "utf-8");
    return { canceled: false, path: filePath };
  });

  ipcMain.handle("reminders:exportPdf", async (): Promise<{ canceled: boolean; path?: string }> => {
    const win = mainWindow;
    if (!win) return { canceled: true };

    const { canceled, filePath } = await dialog.showSaveDialog(win, {
      title: "Esporta come Json",
      defaultPath: "remind_list.json",
      filters: [{ name: "JSON", extensions: ["json"] }],
    });
    if (canceled || !filePath) return { canceled: true };

    const json = await apiClient.exportJson();
    await fs.writeFile(filePath, JSON.stringify(json, null, 2));
    return { canceled: false, path: filePath };
  });

  ipcMain.handle("app:setThemeSource", (_event, mode: "light" | "dark"): void => {
    nativeTheme.themeSource = mode;
  });

  ipcMain.handle("app:setLanguage", async (_event, language: LanguageCode): Promise<void> => {
    currentLanguage = language;
    await rebuildMenuAndTray(config);
  });
}

app.whenReady().then(async () => {
  spawnBackend();

  const configPath = path.join(getBundledResDir(), "config.json");
  const config = await loadAppConfig(configPath);

  registerIpcHandlers(config);
  await refreshTranslations();
  Menu.setApplicationMenu(
    buildAppMenu({
      getMainWindow: () => mainWindow,
      config,
      translations: currentTranslations,
      quit: quitApp,
    }),
  );

  createMainWindow();

  poller = new DuePoller(config.schedulerIntervalMinutes * 60_000, openReminderPopup);
  poller.start();

  appTray = setupTray({
    showMainWindow: () => showAndFocus(mainWindow),
    scheduler: poller,
    quit: quitApp,
    iconPath: getAppIconPath(),
    getTranslations: () => currentTranslations,
  });

  app.on("activate", () => {
    if (mainWindow) {
      showAndFocus(mainWindow);
    } else {
      createMainWindow();
    }
  });
});

app.on("before-quit", () => {
  isQuitting = true;
  poller?.stop();
  backendProcess?.kill();
});

app.on("window-all-closed", () => {
  // Never quit here: the main window hides instead of closing, and popups
  // closing on their own shouldn't end the background reminder service.
});
