import { useState } from "react";
import { useI18n } from "../lib/i18n";
import type { TimeInterval } from "../lib/types";

interface TimePickerDialogProps {
  initialValue: TimeInterval;
  onConfirm: (value: TimeInterval) => void;
  onCancel: () => void;
}

function clamp(value: number, min: number, max: number): number {
  if (Number.isNaN(value)) return min;
  return Math.min(max, Math.max(min, value));
}

/** Mirrors remindme.Dialogs.TimePicker: days>=0 (no max), hours 0-23, minutes 0-59, mouse-wheel adjustable. */
export function TimePickerDialog({ initialValue, onConfirm, onCancel }: TimePickerDialogProps) {
  const { t } = useI18n();
  const [days, setDays] = useState(initialValue.days);
  const [hours, setHours] = useState(initialValue.hours);
  const [minutes, setMinutes] = useState(initialValue.minutes);
  const [error, setError] = useState<string | null>(null);

  const spinnerTooltip = t("TimePickerDialog", "SpinnerTooltip", "Usa la rotella del mouse per modificare il valore");

  function wheelAdjust(setter: (updater: (v: number) => number) => void, min: number, max: number) {
    return (e: React.WheelEvent<HTMLInputElement>) => {
      e.preventDefault();
      const delta = e.deltaY < 0 ? 1 : -1;
      setter((v) => clamp(v + delta, min, max));
    };
  }

  function handleConfirm() {
    if (days === 0 && hours === 0 && minutes === 0) {
      setError(t("Dialogs", "ErrorWrongTimeInterval", "L'intervallo di tempo non è corretto"));
      return;
    }
    onConfirm({ days, hours, minutes });
  }

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>{t("TimePickerDialog", "TimeIntervalTitle", "Intervallo di tempo del promemoria")}</h2>
        <p className="modal-description">
          {t(
            "TimePickerDialog",
            "Description",
            "Seleziona ogni quanto mostrare il promemoria scegliendo la frequenza in giorni, ore e minuti",
          )}
        </p>
        <div className="time-picker-fields">
          <label className="field">
            <span>{t("TimePickerDialog", "Days", "Giorni")}</span>
            <input
              type="number"
              min={0}
              value={days}
              title={spinnerTooltip}
              onChange={(e) => setDays(clamp(Number(e.target.value), 0, Number.MAX_SAFE_INTEGER))}
              onWheel={wheelAdjust((updater) => setDays(updater), 0, Number.MAX_SAFE_INTEGER)}
            />
          </label>
          <label className="field">
            <span>{t("TimePickerDialog", "Hours", "Ore")}</span>
            <input
              type="number"
              min={0}
              max={23}
              value={hours}
              title={spinnerTooltip}
              onChange={(e) => setHours(clamp(Number(e.target.value), 0, 23))}
              onWheel={wheelAdjust((updater) => setHours(updater), 0, 23)}
            />
          </label>
          <label className="field">
            <span>{t("TimePickerDialog", "Minutes", "Minuti")}</span>
            <input
              type="number"
              min={0}
              max={59}
              value={minutes}
              title={spinnerTooltip}
              onChange={(e) => setMinutes(clamp(Number(e.target.value), 0, 59))}
              onWheel={wheelAdjust((updater) => setMinutes(updater), 0, 59)}
            />
          </label>
        </div>
        {error && <p className="field-error">{error}</p>}
        <div className="modal-actions">
          <button className="btn" onClick={onCancel}>
            {t("General", "CancelButton", "Annulla")}
          </button>
          <button className="btn btn-primary" onClick={handleConfirm}>
            {t("General", "OkButton", "Ok")}
          </button>
        </div>
      </div>
    </div>
  );
}
