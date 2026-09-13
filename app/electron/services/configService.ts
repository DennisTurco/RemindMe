import type { LanguageCode, PreferencesFile, RemindListPath, ThemeCode } from "../types";
import { LANGUAGE_FILE_NAMES, THEME_NAMES } from "../types";
import { readJsonFile, writeJsonFileAtomic } from "./jsonStore";

export interface AppPreferences {
  language: LanguageCode;
  theme: ThemeCode;
  remindList: RemindListPath;
}

const DEFAULT_LANGUAGE: LanguageCode = "ENG";
const DEFAULT_THEME: ThemeCode = "INTELLIJ";

function languageFromFileName(fileName: string | null): LanguageCode {
  const entry = Object.entries(LANGUAGE_FILE_NAMES).find(([, value]) => value === fileName);
  return (entry?.[0] as LanguageCode | undefined) ?? DEFAULT_LANGUAGE;
}

function themeFromDisplayName(displayName: string | null): ThemeCode {
  const entry = Object.entries(THEME_NAMES).find(([, value]) => value === displayName);
  return (entry?.[0] as ThemeCode | undefined) ?? DEFAULT_THEME;
}

/** Preferences are still plain JSON: written rarely, by a single process (Settings page), no concurrency risk. */
export async function loadPreferences(
  preferencesFilePath: string,
  defaultRemindList: RemindListPath,
): Promise<AppPreferences> {
  const raw = await readJsonFile<Partial<PreferencesFile>>(preferencesFilePath, "{}");

  const remindList =
    raw.RemindList?.Directory && raw.RemindList?.File
      ? { directory: raw.RemindList.Directory, file: raw.RemindList.File }
      : defaultRemindList;

  const prefs: AppPreferences = {
    language: languageFromFileName(raw.Language ?? null),
    theme: themeFromDisplayName(raw.Theme ?? null),
    remindList,
  };

  await savePreferences(preferencesFilePath, prefs);
  return prefs;
}

export async function savePreferences(preferencesFilePath: string, prefs: AppPreferences): Promise<void> {
  const data: PreferencesFile = {
    Language: LANGUAGE_FILE_NAMES[prefs.language],
    Theme: THEME_NAMES[prefs.theme],
    RemindList: { Directory: prefs.remindList.directory, File: prefs.remindList.file },
  };
  await writeJsonFileAtomic(preferencesFilePath, data);
}
