import { LANGUAGE_DISPLAY_NAMES, useI18n } from "../lib/i18n";
import type { LanguageCode } from "../lib/i18n";
import { triggerOnboarding } from "../lib/onboarding";
import { getEffectiveTheme, setTheme } from "../lib/theme";
import type { ThemeMode } from "../lib/theme";
import { useState } from "react";

interface PreferencesDialogProps {
  onClose: () => void;
}

const LANGUAGE_OPTIONS = Object.keys(LANGUAGE_DISPLAY_NAMES) as LanguageCode[];

/** Opened from Options > Preferenze in the app menu. */
export function PreferencesDialog({ onClose }: PreferencesDialogProps) {
  const { t, language, setLanguage } = useI18n();
  const [theme, setThemeState] = useState<ThemeMode>(getEffectiveTheme);

  function chooseTheme(mode: ThemeMode) {
    setThemeState(setTheme(mode));
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>{t("Menu", "Preferences", "Preferenze")}</h2>

        <fieldset className="field">
          <legend>{t("General", "LanguageText", "Lingua")}</legend>
          <select value={language} onChange={(e) => setLanguage(e.target.value as LanguageCode)}>
            {LANGUAGE_OPTIONS.map((code) => (
              <option key={code} value={code}>
                {LANGUAGE_DISPLAY_NAMES[code]}
              </option>
            ))}
          </select>
        </fieldset>

        <fieldset className="field">
          <legend>{t("General", "ThemeText", "Tema")}</legend>
          <label className="checkbox-field">
            <input type="radio" name="theme" checked={theme === "light"} onChange={() => chooseTheme("light")} />
            {t("General", "LightThemeText", "Chiaro")}
          </label>
          <label className="checkbox-field">
            <input type="radio" name="theme" checked={theme === "dark"} onChange={() => chooseTheme("dark")} />
            {t("General", "DarkThemeText", "Scuro")}
          </label>
        </fieldset>

        <div className="modal-actions reminder-form-actions">
          <button
            className="btn"
            onClick={() => {
              triggerOnboarding();
              onClose();
            }}
          >
            {t("Menu", "ReviewTutorialButton", "Rivedi il tutorial")}
          </button>
          <button className="btn btn-primary" onClick={onClose}>
            {t("General", "CloseButton", "Chiudi")}
          </button>
        </div>
      </div>
    </div>
  );
}

