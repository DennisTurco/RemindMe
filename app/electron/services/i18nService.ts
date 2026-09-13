import * as fs from "fs/promises";
import * as path from "path";

export type LanguageCode = "ITA" | "ENG" | "DEU" | "ESP" | "FRA";

export const LANGUAGE_FILE_NAMES: Record<LanguageCode, string> = {
  ITA: "ita.json",
  ENG: "eng.json",
  DEU: "deu.json",
  ESP: "esp.json",
  FRA: "fra.json",
};

export const DEFAULT_LANGUAGE: LanguageCode = "ITA";

export type Translations = Record<string, Record<string, string>>;

export async function loadTranslations(languagesDir: string, language: LanguageCode): Promise<Translations> {
  const filePath = path.join(languagesDir, LANGUAGE_FILE_NAMES[language]);
  const content = await fs.readFile(filePath, "utf-8");
  return JSON.parse(content);
}

export function t(translations: Translations | null, category: string, key: string, fallback: string): string {
  return translations?.[category]?.[key] ?? fallback;
}
