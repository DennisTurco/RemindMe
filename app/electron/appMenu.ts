import { BrowserWindow, Menu, MenuItemConstructorOptions, clipboard, dialog, shell } from "electron";
import type { AppConfig } from "./services/appConfigService";
import { t, type Translations } from "./services/i18nService";
import { exportRemindListToJson, readRemindListFromJson } from "./services/jsonListIO";
import type { ReminderRepository } from "./services/reminderRepository";

export interface AppMenuContext {
  getMainWindow: () => BrowserWindow | null;
  getRepository: () => ReminderRepository;
  config: AppConfig;
  translations: Translations | null;
}

/**
 * Mirrors MainGUI's menu bar (File / Options / About / Help), replacing
 * Electron's default File/Edit/View/Window/Help menu. Which items appear is
 * driven by config.json's MenuItems flags, same as MainGUI#setMenuItems in
 * the Java app; labels come from the selected language's Menu/Dialogs
 * translations (res/languages/*.json), same source as the Java app used.
 */
export function buildAppMenu({ getMainWindow, getRepository, config, translations }: AppMenuContext): Menu {
  const flags = config.menuItems;
  const links = config.links;
  const m = (key: string, fallback: string) => t(translations, "Menu", key, fallback);
  const d = (key: string, fallback: string) => t(translations, "Dialogs", key, fallback);

  function notify(message: string, type: "info" | "error" = "info"): void {
    const win = getMainWindow();
    if (win) {
      void dialog.showMessageBox(win, { type, message });
    } else {
      void dialog.showMessageBox({ type, message });
    }
  }

  const fileItems: MenuItemConstructorOptions[] = [];
  if (flags.New) {
    fileItems.push({
      label: m("New", "Nuovo"),
      accelerator: "CmdOrCtrl+N",
      click: () => getMainWindow()?.webContents.send("menu:new-reminder"),
    });
  }
  if (flags.Import || flags.Export) {
    fileItems.push({ type: "separator" });
  }
  if (flags.Import) {
    fileItems.push({
      label: m("Import", "Importa elenco da .json"),
      click: async () => {
        const win = getMainWindow();
        if (!win) return;
        const { canceled, filePaths } = await dialog.showOpenDialog(win, {
          title: m("Import", "Importa elenco da .json"),
          filters: [{ name: "JSON Files", extensions: ["json"] }],
          properties: ["openFile"],
        });
        if (canceled || !filePaths[0]) return;

        try {
          const reminds = await readRemindListFromJson(filePaths[0]);
          getRepository().replaceAll(reminds);
          win.webContents.send("reminders:changed");
          notify(d("RemindListCorrectlyImportedMessage", "Elenco promemoria importato con successo!"));
        } catch {
          notify(d("ErrorMessageForWrongFileExtensionMessage", "Errore: seleziona un file JSON valido."), "error");
        }
      },
    });
  }
  if (flags.Export) {
    fileItems.push({
      label: m("Export", "Esporta elenco in .json"),
      click: async () => {
        const win = getMainWindow();
        if (!win) return;
        const { canceled, filePath } = await dialog.showSaveDialog(win, {
          title: m("Export", "Esporta elenco in .json"),
          defaultPath: "remind_list.json",
          filters: [{ name: "JSON Files", extensions: ["json"] }],
        });
        if (canceled || !filePath) return;

        await exportRemindListToJson(getRepository().getAll(), filePath);
        notify(d("RemindListCorrectlyExportedMessage", "Elenco promemoria esportato con successo!"));
      },
    });
  }

  const optionsItems: MenuItemConstructorOptions[] = [];
  if (flags.Preferences) {
    optionsItems.push({
      label: `${m("Preferences", "Preferenze")}...`,
      click: () => getMainWindow()?.webContents.send("menu:open-preferences"),
    });
    optionsItems.push({ type: "separator" });
  }
  if (flags.Quit) {
    optionsItems.push({
      label: m("Quit", "Esci"),
      accelerator: process.platform === "darwin" ? "Cmd+Q" : "Alt+F4",
      click: () => getMainWindow()?.close(),
    });
  }

  const infoItems: MenuItemConstructorOptions[] = [];
  if (flags.Website) infoItems.push({ label: m("Website", "Sito Web"), click: () => shell.openExternal(links.website) });
  if (flags.InfoPage) infoItems.push({ label: m("InfoPage", "Info"), click: () => shell.openExternal(links.infoPage) });
  if (flags.Share) {
    infoItems.push({
      label: m("Share", "Condividi"),
      click: () => {
        clipboard.writeText(links.sharePage);
        notify(d("ShareLinkCopiedMessage", "Link copiato negli appunti!"));
      },
    });
  }
  if (flags.Donate && (flags.PaypalDonate || flags.BuymeacoffeeDonate)) {
    const donateSubmenu: MenuItemConstructorOptions[] = [];
    if (flags.PaypalDonate) donateSubmenu.push({ label: "PayPal", click: () => shell.openExternal(links.donatePaypal) });
    if (flags.BuymeacoffeeDonate) {
      donateSubmenu.push({ label: "Buy me a coffee", click: () => shell.openExternal(links.donateBuyMeACoffee) });
    }
    infoItems.push({ label: m("Donate", "Dona"), submenu: donateSubmenu });
  }

  const helpItems: MenuItemConstructorOptions[] = [];
  if (flags.BugReport) {
    helpItems.push({ label: m("BugReport", "Segnala un bug"), click: () => shell.openExternal(links.issuePage) });
  }
  if (flags.Support) {
    helpItems.push({ label: m("Support", "Supporto"), click: () => shell.openExternal(`mailto:${config.supportEmail}`) });
  }

  const template: MenuItemConstructorOptions[] = [];
  if (fileItems.length > 0) template.push({ label: m("File", "File"), submenu: fileItems });
  if (optionsItems.length > 0) template.push({ label: m("Options", "Opzioni"), submenu: optionsItems });
  if (infoItems.length > 0) template.push({ label: m("About", "Informazioni"), submenu: infoItems });
  if (helpItems.length > 0) template.push({ label: m("Help", "Aiuto"), submenu: helpItems });

  return Menu.buildFromTemplate(template);
}
