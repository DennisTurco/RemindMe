import { remindMe } from "./ipc";

export type ThemeMode = "light" | "dark";

const STORAGE_KEY = "remindme-theme";

function readStoredTheme(): ThemeMode | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY);
    return value === "light" || value === "dark" ? value : null;
  } catch {
    return null;
  }
}

function systemPrefersDark(): boolean {
  return window.matchMedia?.("(prefers-color-scheme: dark)").matches ?? false;
}

/** The theme actually in effect: an explicit user choice, or the OS setting otherwise. */
export function getEffectiveTheme(): ThemeMode {
  return readStoredTheme() ?? (systemPrefersDark() ? "dark" : "light");
}

function applyTheme(theme: ThemeMode | null): void {
  if (theme) {
    document.documentElement.setAttribute("data-theme", theme);
  } else {
    document.documentElement.removeAttribute("data-theme");
  }
}

/**
 * Applies the persisted (or system) theme. Call once at startup. Also syncs
 * Electron's native theme (nativeTheme.themeSource) so the native menu bar
 * and dialogs match the web content instead of following the OS default.
 */
export function initTheme(): void {
  const effective = getEffectiveTheme();
  applyTheme(readStoredTheme());
  void remindMe.setNativeTheme(effective);
}

/** Explicitly sets and persists the theme, overriding the OS setting from now on. */
export function setTheme(theme: ThemeMode): ThemeMode {
  try {
    localStorage.setItem(STORAGE_KEY, theme);
  } catch {
    // Ignore write failures (e.g. private browsing); the theme still applies for this session.
  }
  applyTheme(theme);
  void remindMe.setNativeTheme(theme);
  return theme;
}

export function toggleTheme(): ThemeMode {
  return setTheme(getEffectiveTheme() === "dark" ? "light" : "dark");
}
