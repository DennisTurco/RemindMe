import { LANGUAGE_DISPLAY_NAMES, useI18n } from "../lib/i18n";
import type { LanguageCode } from "../lib/i18n";
import { remindMe } from "../lib/ipc";
import { triggerOnboarding } from "../lib/onboarding";
import { getEffectiveTheme, setTheme } from "../lib/theme";
import type { ThemeMode } from "../lib/theme";
import { useEffect, useState } from "react";

interface PreferencesDialogProps {
  onClose: () => void;
}

const LANGUAGE_OPTIONS = Object.keys(LANGUAGE_DISPLAY_NAMES) as LanguageCode[];

/** Opened from Options > Preferences in the app menu. */
export function PreferencesDialog({ onClose }: PreferencesDialogProps) {
  const { t, language, setLanguage } = useI18n();
  const [theme, setThemeState] = useState<ThemeMode>(getEffectiveTheme);
  const [autoLaunch, setAutoLaunchState] = useState(false);

  useEffect(() => {
    void remindMe.getAutoLaunch().then(setAutoLaunchState);
  }, []);

  function chooseTheme(mode: ThemeMode) {
    setThemeState(setTheme(mode));
  }

  function toggleAutoLaunch(enabled: boolean) {
    setAutoLaunchState(enabled);
    void remindMe.setAutoLaunch(enabled);
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>{t("Menu", "Preferences", "Preferences")}</h2>

        <fieldset className="field">
          <legend>{t("General", "LanguageText", "Language")}</legend>
          <select value={language} onChange={(e) => setLanguage(e.target.value as LanguageCode)}>
            {LANGUAGE_OPTIONS.map((code) => (
              <option key={code} value={code}>
                {LANGUAGE_DISPLAY_NAMES[code]}
              </option>
            ))}
          </select>
        </fieldset>

        <fieldset className="field">
          <legend>{t("General", "ThemeText", "Theme")}</legend>
          <label className="checkbox-field">
            <input type="radio" name="theme" checked={theme === "light"} onChange={() => chooseTheme("light")} />
            {t("General", "LightThemeText", "Light")}
          </label>
          <label className="checkbox-field">
            <input type="radio" name="theme" checked={theme === "dark"} onChange={() => chooseTheme("dark")} />
            {t("General", "DarkThemeText", "Dark")}
          </label>
        </fieldset>

        <fieldset className="field">
          <legend>{t("General", "StartupText", "Startup")}</legend>
          <label className="checkbox-field">
            <input type="checkbox" checked={autoLaunch} onChange={(e) => toggleAutoLaunch(e.target.checked)} />
            {t("General", "AutoLaunchText", "Launch RemindMe at system startup")}
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
            {t("Menu", "ReviewTutorialButton", "Review tutorial")}
          </button>
          <button className="btn btn-primary" onClick={onClose}>
            {t("General", "CloseButton", "Close")}
          </button>
        </div>
      </div>
    </div>
  );
}

