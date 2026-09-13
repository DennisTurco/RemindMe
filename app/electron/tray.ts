import { BrowserWindow, Menu, Tray } from "electron";
import type { Translations } from "./services/i18nService";
import { t } from "./services/i18nService";

export interface TrayScheduler {
  pause(): void;
  resume(): void;
  isPaused(): boolean;
}

export interface TrayContext {
  showMainWindow: () => void;
  scheduler: TrayScheduler;
  quit: () => void;
  iconPath: string;
  getTranslations: () => Translations | null;
}

export interface AppTray {
  refresh(): void;
}

/** Mirrors remindme.Controllers.TrayController: Apri / Pausa-Riprendi / Esci. */
export function setupTray(ctx: TrayContext): AppTray {
  const tray = new Tray(ctx.iconPath);
  tray.on("click", () => ctx.showMainWindow());

  function refresh(): void {
    const translations = ctx.getTranslations();
    const tr = (key: string, fallback: string) => t(translations, "TrayIcon", key, fallback);

    tray.setToolTip(tr("TrayTooltip", "Remind Service"));

    const paused = ctx.scheduler.isPaused();
    tray.setContextMenu(
      Menu.buildFromTemplate([
        { label: tr("OpenAction", "Accesso Rapido"), click: () => ctx.showMainWindow() },
        { type: "separator" },
        paused
          ? {
              label: tr("ResumeAction", "Riprendi"),
              click: () => {
                ctx.scheduler.resume();
                refresh();
              },
            }
          : {
              label: tr("PauseAction", "Pausa"),
              click: () => {
                ctx.scheduler.pause();
                refresh();
              },
            },
        { type: "separator" },
        { label: tr("ExitAction", "Esci"), click: () => ctx.quit() },
      ]),
    );
  }

  refresh();
  return { refresh };
}

export function showAndFocus(win: BrowserWindow | null): void {
  if (!win) return;
  if (win.isMinimized()) win.restore();
  win.show();
  win.focus();
}
