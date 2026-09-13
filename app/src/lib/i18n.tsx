import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { remindMe } from "./ipc";

export type LanguageCode = "ITA" | "ENG" | "DEU" | "ESP" | "FRA";

export const LANGUAGE_FILE_NAMES: Record<LanguageCode, string> = {
  ITA: "ita.json",
  ENG: "eng.json",
  DEU: "deu.json",
  ESP: "esp.json",
  FRA: "fra.json",
};

export const LANGUAGE_DISPLAY_NAMES: Record<LanguageCode, string> = {
  ITA: "Italiano",
  ENG: "English",
  DEU: "Deutsch",
  ESP: "Español",
  FRA: "Français",
};

const STORAGE_KEY = "remindme-language";
const DEFAULT_LANGUAGE: LanguageCode = "ITA";

type Translations = Record<string, Record<string, string>>;

function readStoredLanguage(): LanguageCode {
  try {
    const value = localStorage.getItem(STORAGE_KEY);
    return value && value in LANGUAGE_FILE_NAMES ? (value as LanguageCode) : DEFAULT_LANGUAGE;
  } catch {
    return DEFAULT_LANGUAGE;
  }
}

interface I18nContextValue {
  language: LanguageCode;
  /** Returns the translated string, or `fallback` (the current hardcoded text) while loading or if the key is missing. */
  t: (category: string, key: string, fallback: string) => string;
  setLanguage: (language: LanguageCode) => void;
}

const I18nContext = createContext<I18nContextValue | null>(null);

export function I18nProvider({ children }: { children: ReactNode }) {
  const [language, setLanguageState] = useState<LanguageCode>(readStoredLanguage);
  const [translations, setTranslations] = useState<Translations | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetch(`/languages/${LANGUAGE_FILE_NAMES[language]}`)
      .then((res) => res.json())
      .then((data) => {
        if (!cancelled) setTranslations(data);
      })
      .catch(() => {
        if (!cancelled) setTranslations(null);
      });
    void remindMe.setLanguage(language);
    return () => {
      cancelled = true;
    };
  }, [language]);

  const setLanguage = useCallback((next: LanguageCode) => {
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // Ignore write failures (e.g. private browsing); the choice still applies for this session.
    }
    setLanguageState(next);
  }, []);

  const t = useCallback(
    (category: string, key: string, fallback: string) => translations?.[category]?.[key] ?? fallback,
    [translations],
  );

  const value = useMemo(() => ({ language, t, setLanguage }), [language, t, setLanguage]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nContextValue {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useI18n must be used within an I18nProvider");
  return ctx;
}
