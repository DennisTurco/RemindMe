import { app, BrowserWindow, Menu, dialog, ipcMain, nativeTheme, shell } from "electron";
import { spawn, type ChildProcess } from "child_process";
import * as fsSync from "fs";
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

// Windows groups tray/notification/taskbar behavior (and lists apps in
// Settings > Startup apps) by this id; without it, a login item registered
// via setLoginItemSettings can silently fail to show up there.
if (process.platform === "win32") {
  app.setAppUserModelId("it.dennisturco.remindme");
}

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

/** "Start with the system" toggle, run minimized to the tray via the "--hidden" flag. */
/**
 * On Windows, getLoginItemSettings() only reports openAtLogin=true when queried
 * with the exact same path + args the login item was written with, so both
 * calls must share these. In dev, process.execPath is the bare electron.exe:
 * the entry script has to be passed too or the login item launches nothing.
 */
function getLoginItemOptions(): { path: string; args: string[] } {
  const args = isDev ? [path.resolve(process.argv[1]), "--hidden"] : ["--hidden"];
  return { path: process.execPath, args };
}

/**
 * Electron's login item API is Windows/macOS only: on Linux, desktops follow
 * the XDG autostart spec, i.e. a .desktop file in ~/.config/autostart.
 */
function getLinuxAutostartFile(): string {
  const configHome = process.env.XDG_CONFIG_HOME || path.join(app.getPath("home"), ".config");
  return path.join(configHome, "autostart", "remindme.desktop");
}

function setLinuxAutostartEnabled(enabled: boolean): void {
  const file = getLinuxAutostartFile();
  if (!enabled) {
    fsSync.rmSync(file, { force: true });
    return;
  }

  // Inside an AppImage, execPath points into a temporary mount that changes
  // every run: APPIMAGE is the path of the .AppImage file itself. Packaged
  // builds run as "<name>.bin" behind the sandbox-detecting launcher script
  // (build/linux-after-pack.cjs): autostart goes through the launcher too.
  const { path: execPath, args } = getLoginItemOptions();
  const launcher = isDev ? execPath : execPath.replace(/\.bin$/, "");
  const exec = [process.env.APPIMAGE || launcher, ...args].map((part) => `"${part}"`).join(" ");
  const entry = [
    "[Desktop Entry]",
    "Type=Application",
    "Name=RemindMe",
    `Exec=${exec}`,
    "Terminal=false",
    "X-GNOME-Autostart-enabled=true",
    "",
  ].join("\n");

  fsSync.mkdirSync(path.dirname(file), { recursive: true });
  fsSync.writeFileSync(file, entry);
}

function getAutoLaunchEnabled(): boolean {
  if (process.platform === "linux") {
    return fsSync.existsSync(getLinuxAutostartFile());
  }
  return app.getLoginItemSettings(getLoginItemOptions()).openAtLogin;
}

function setAutoLaunchEnabled(enabled: boolean): void {
  if (process.platform === "linux") {
    setLinuxAutostartEnabled(enabled);
    return;
  }
  app.setLoginItemSettings({
    ...getLoginItemOptions(),
    openAtLogin: enabled,
    openAsHidden: enabled,
    // Windows: also (re)approves the entry in Task Manager's Startup tab, otherwise
    // turning it back on here has no effect if it was once disabled there.
    enabled,
  });
}

/**
 * Auto-launch is on by default for new installs, but Windows only remembers
 * "on" once we actually write the login item ourselves: getLoginItemSettings()
 * has no way to distinguish "never configured" from "user turned it off", so
 * a marker file in userData (not the OS setting itself) is the only reliable
 * record of whether this default has already been applied once. Runs at most
 * once per install; after that the user's own choice (on or off) is always
 * left alone.
 */
async function applyDefaultAutoLaunchOnFirstRun(): Promise<void> {
  if (isDev) return;

  const marker = path.join(app.getPath("userData"), ".autolaunch-default-applied");
  try {
    await fs.access(marker);
    return;
  } catch {
    // Marker doesn't exist yet: first run since install (or since userData was cleared).
  }

  setAutoLaunchEnabled(true);
  await fs.writeFile(marker, "");
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

/**
 * macOS keeps an app in the Dock for as long as it runs, even with every
 * window hidden. RemindMe should look like a tray-only app while it runs in
 * the background, so the Dock icon is shown only while some window (main
 * window or a reminder popup) is visible.
 */
function updateDockVisibility(): void {
  if (process.platform !== "darwin") return;

  const anyVisible = BrowserWindow.getAllWindows().some((win) => !win.isDestroyed() && win.isVisible());
  if (anyVisible) {
    void app.dock.show();
  } else {
    app.dock.hide();
  }
}

app.on("browser-window-created", (_event, win) => {
  win.on("show", updateDockVisibility);
  win.on("hide", updateDockVisibility);
  win.on("closed", updateDockVisibility);
});

function createMainWindow(): void {
  // macOS login items don't receive command-line args: there the OS reports
  // whether this launch came from the login item instead.
  const startHidden =
    process.argv.includes("--hidden") ||
    (process.platform === "darwin" && app.getLoginItemSettings().wasOpenedAtLogin);

  mainWindow = new BrowserWindow({
    width: 982,
    height: 715,
    minWidth: MIN_WIDTH,
    minHeight: MIN_HEIGHT,
    icon: getAppIconPath(),
    show: !startHidden,
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
  // The install folder is read-only on Linux (AppImage mount, root-owned
  // /opt for .deb/.rpm), so the database and logs live in the per-user
  // userData folder instead, on every platform.
  const dataDir = app.getPath("userData");
  const javaArgs = [
    `-Dremindme.dataDir=${dataDir}`,
    `-Dremindme.logDir=${path.join(dataDir, "logs")}`,
    "-jar",
    jarPath,
    "--serve",
  ];

  backendProcess = spawn(javaExe, javaArgs, { cwd, windowsHide: true });
  backendProcess.stdout?.on("data", (chunk) => console.log(`[backend] ${chunk}`));
  backendProcess.stderr?.on("data", (chunk) => console.error(`[backend] ${chunk}`));
}

/**
 * Blocks until the Java backend responds to GET /health (or the timeout
 * elapses), so the renderer's first `getAll()` on mount doesn't race the
 * JVM's startup time and silently render an empty table: apiClient.getAll()
 * has no retry, so a `fetch` that fails because the backend isn't listening
 * yet just leaves the initial reminder list empty forever (it only recovers
 * once something else, e.g. creating a reminder, triggers another refresh).
 * No-op in dev, where the backend is started separately and may already be
 * up or intentionally not running yet.
 */
async function waitForBackendReady(timeoutMs = 20_000, intervalMs = 200): Promise<void> {
  if (isDev) return;

  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (await apiClient.health()) return;
    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }
  console.error(`[backend] did not become ready within ${timeoutMs}ms; continuing anyway`);
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

  ipcMain.handle("reminders:exportJson", async (): Promise<{ canceled: boolean; path?: string }> => {
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

  ipcMain.handle("app:getAutoLaunch", (): boolean => getAutoLaunchEnabled());

  ipcMain.handle("app:setAutoLaunch", (_event, enabled: boolean): void => {
    setAutoLaunchEnabled(enabled);
  });

  ipcMain.handle("app:openLink", (_event, key: keyof Awaited<ReturnType<typeof loadAppConfig>>["links"]): void => {
    const url = config.links[key];
    if (url) void shell.openExternal(url);
  });
}

app.whenReady().then(async () => {
  spawnBackend();
  await applyDefaultAutoLaunchOnFirstRun();
  await waitForBackendReady();

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
  // Started hidden (login item): no "show" event will fire, hide the Dock icon now
  updateDockVisibility();

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
