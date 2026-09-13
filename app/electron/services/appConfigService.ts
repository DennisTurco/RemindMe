import * as fs from "fs/promises";

/**
 * Mirrors res/config/config.json from the Java app, trimmed to only the
 * keys this app actually uses: static external links and the flags that
 * control which menu items are shown (remindme.Enums.MenuItems /
 * MainGUI#setMenuItems). Java-specific keys (file paths, GUI pixel size,
 * log service tuning) were dropped since they don't apply to this
 * Electron/SQLite architecture.
 */
export interface MenuItemFlags {
  New: boolean;
  Import: boolean;
  Export: boolean;
  Preferences: boolean;
  Quit: boolean;
  Website: boolean;
  InfoPage: boolean;
  Share: boolean;
  Donate: boolean;
  PaypalDonate: boolean;
  BuymeacoffeeDonate: boolean;
  BugReport: boolean;
  Support: boolean;
}

export interface AppConfig {
  supportEmail: string;
  links: {
    website: string;
    infoPage: string;
    issuePage: string;
    sharePage: string;
    donatePaypal: string;
    donateBuyMeACoffee: string;
  };
  menuItems: MenuItemFlags;
}

const DEFAULT_CONFIG: AppConfig = {
  supportEmail: "assistenza@shardpc.it",
  links: {
    website: "https://www.shardpc.it/",
    infoPage: "https://github.com/DennisTurco/RemindMe",
    issuePage: "https://github.com/DennisTurco/RemindMe/issues",
    sharePage: "https://github.com/DennisTurco/RemindMe/releases",
    donatePaypal: "https://www.paypal.com/donate/?hosted_button_id=M7CJXS929334U",
    donateBuyMeACoffee: "https://buymeacoffee.com/denno",
  },
  menuItems: {
    New: true,
    Import: true,
    Export: true,
    Preferences: true,
    Quit: true,
    Website: true,
    InfoPage: true,
    Share: true,
    Donate: true,
    PaypalDonate: true,
    BuymeacoffeeDonate: true,
    BugReport: true,
    Support: true,
  },
};

interface RawConfigJson {
  SupportEmail?: string;
  Links?: Partial<{
    Website: string;
    InfoPage: string;
    IssuePage: string;
    SharePage: string;
    DonatePaypal: string;
    DonateBuyMeACoffee: string;
  }>;
  MenuItems?: Partial<MenuItemFlags>;
}

export async function loadAppConfig(configJsonPath: string): Promise<AppConfig> {
  let raw: RawConfigJson;
  try {
    const content = await fs.readFile(configJsonPath, "utf-8");
    raw = JSON.parse(content);
  } catch {
    return DEFAULT_CONFIG;
  }

  return {
    supportEmail: raw.SupportEmail ?? DEFAULT_CONFIG.supportEmail,
    links: {
      website: raw.Links?.Website ?? DEFAULT_CONFIG.links.website,
      infoPage: raw.Links?.InfoPage ?? DEFAULT_CONFIG.links.infoPage,
      issuePage: raw.Links?.IssuePage ?? DEFAULT_CONFIG.links.issuePage,
      sharePage: raw.Links?.SharePage ?? DEFAULT_CONFIG.links.sharePage,
      donatePaypal: raw.Links?.DonatePaypal ?? DEFAULT_CONFIG.links.donatePaypal,
      donateBuyMeACoffee: raw.Links?.DonateBuyMeACoffee ?? DEFAULT_CONFIG.links.donateBuyMeACoffee,
    },
    menuItems: { ...DEFAULT_CONFIG.menuItems, ...raw.MenuItems },
  };
}
