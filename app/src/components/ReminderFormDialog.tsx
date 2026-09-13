import { useState } from "react";
import { EXECUTION_METHOD_OPTIONS, ICON_OPTIONS, SOUND_OPTIONS, iconPath, soundPath } from "../lib/catalog";
import { useI18n } from "../lib/i18n";
import { createDefaultRemind } from "../lib/types";
import type { ExecutionMethod, Remind, TimeInterval } from "../lib/types";
import { ReminderPreviewDialog } from "./ReminderPreviewDialog";
import { TimePickerDialog } from "./TimePickerDialog";

interface ReminderFormDialogProps {
  mode: "create" | "edit";
  initialRemind?: Remind;
  isNameTaken: (name: string) => boolean;
  onSave: (remind: Remind) => void;
  onCancel: () => void;
}

function timeOfDayInputValue(value: string | null): string {
  return value ? value.slice(0, 5) : "08:00";
}

/** Mirrors remindme.Dialogs.ManageRemind: create/edit form for a single reminder. */
export function ReminderFormDialog({ mode, initialRemind, isNameTaken, onSave, onCancel }: ReminderFormDialogProps) {
  const { t } = useI18n();
  const base = initialRemind ?? createDefaultRemind();

  const [name, setName] = useState(base.name);
  const [description, setDescription] = useState(base.description);
  const [icon, setIcon] = useState(base.icon);
  const [sound, setSound] = useState(base.sound);
  const [isActive, setIsActive] = useState(base.isActive);
  const [isTopLevel, setIsTopLevel] = useState(base.isTopLevel);
  const [executionMethod, setExecutionMethod] = useState<ExecutionMethod>(base.executionMethod);
  const [timeFrom, setTimeFrom] = useState(timeOfDayInputValue(base.timeRange?.start ?? null));
  const [timeTo, setTimeTo] = useState(timeOfDayInputValue(base.timeRange?.end ?? base.timeRange?.start ?? null));
  const [timeInterval, setTimeInterval] = useState<TimeInterval>(base.timeInterval ?? { days: 0, hours: 1, minutes: 0 });
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isEdit = mode === "edit";
  const timeRangeEnabled = executionMethod === "CUSTOM_TIME_RANGE";
  const timeFromEnabled = executionMethod === "CUSTOM_TIME_RANGE" || executionMethod === "ONE_TIME_PER_DAY";
  const intervalEnabled = executionMethod !== "ONE_TIME_PER_DAY";

  function playSoundPreview() {
    const path = soundPath(sound);
    if (!path) return;
    new Audio(path).play().catch(() => {});
  }

  function handleSave() {
    const trimmedName = name.trim();
    if (!trimmedName) {
      setError(t("Dialogs", "ErrorMessageForEmptyRemindName", "Il nome del promemoria non può essere vuoto"));
      return;
    }
    if (!isEdit && isNameTaken(trimmedName)) {
      setError(
        t(
          "Dialogs",
          "ErrorMessageDuplicatedRedind",
          "Impossibile creare un promemoria con questo nome perché ne esiste già uno.",
        ),
      );
      return;
    }

    let timeRange: Remind["timeRange"] = null;
    if (executionMethod === "CUSTOM_TIME_RANGE") {
      if (!(timeFrom < timeTo)) {
        setError(t("Dialogs", "ErrorMessageForWrongTimeRange", "L'intervallo di tempo non è valido"));
        return;
      }
      timeRange = { start: `${timeFrom}:00`, end: `${timeTo}:00` };
    } else if (executionMethod === "ONE_TIME_PER_DAY") {
      timeRange = { start: `${timeFrom}:00`, end: `${timeFrom}:00` };
    }

    const remind: Remind = {
      ...base,
      name: trimmedName,
      description,
      isActive,
      isTopLevel,
      icon,
      sound,
      executionMethod,
      timeInterval,
      timeRange,
    };
    onSave(remind);
  }

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal reminder-form" onClick={(e) => e.stopPropagation()}>
        <h2>
          {isEdit
            ? t("ManageRemindDialog", "EditTitle", "Modifica promemoria")
            : t("ManageRemindDialog", "CreateTitle", "Crea un nuovo promemoria")}
        </h2>

        <label className="field">
          <span>{t("ManageRemindDialog", "NameText", "Nome")}</span>
          <input
            value={name}
            disabled={isEdit}
            placeholder={t("ManageRemindDialog", "NamePlaceholder", "Inserisci il nome del promemoria")}
            title={t("ManageRemindDialog", "NameTooltip", "Inserisci un nome per il promemoria")}
            onChange={(e) => setName(e.target.value)}
          />
        </label>

        <label className="field">
          <span className="field-label-with-hint">
            {t("General", "DescriptionText", "Descrizione")}
            <span
              className="info-icon"
              title="Puoi usare la sintassi Markdown (es. **grassetto**, elenchi puntati, link) per formattare la descrizione."
            >
              i
            </span>
          </span>
          <textarea
            value={description}
            placeholder={t("ManageRemindDialog", "DescriptionPlaceholder", "Inserisci una descrizione (opzionale)")}
            title={t("ManageRemindDialog", "DescriptionTooltip", "Fornisci dettagli aggiuntivi per questo promemoria")}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
          />
        </label>

        <hr className="form-section" />

        <div className="field-row">
          <label className="field">
            <span>{t("General", "IconText", "Icona")}</span>
            <select
              value={icon}
              title={t("ManageRemindDialog", "IconTooltip", "Scegli un'icona per la notifica del promemoria")}
              onChange={(e) => setIcon(e.target.value as Remind["icon"])}
            >
              {ICON_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </label>
          <img className="icon-preview" src={iconPath(icon)} alt="" width={40} height={40} />
        </div>

        <div className="field-row">
          <label className="field">
            <span>{t("General", "SoundText", "Suono")}</span>
            <select
              value={sound}
              title={t("ManageRemindDialog", "SoundTooltip", "Scegli un suono per la notifica del promemoria")}
              onChange={(e) => setSound(e.target.value as Remind["sound"])}
            >
              {SOUND_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </label>
          <button
            className="btn btn-icon"
            type="button"
            title={t("ManageRemindDialog", "SoundButtonTooltip", "Ascolta l'anteprima del suono selezionato")}
            disabled={sound === "NO_SOUND"}
            onClick={playSoundPreview}
          >
            ▶
          </button>
        </div>

        <hr className="form-section" />

        <div className="field-row">
          <label className="checkbox-field">
            <input type="checkbox" checked={isActive} onChange={(e) => setIsActive(e.target.checked)} />
            <span title={t("ManageRemindDialog", "ActiveTooltip", "Attiva o disattiva il promemoria")}>
              {t("ManageRemindDialog", "ActiveText", "Attivo")}
            </span>
          </label>
          <label className="checkbox-field">
            <input type="checkbox" checked={isTopLevel} onChange={(e) => setIsTopLevel(e.target.checked)} />
            <span
              title={t(
                "ManageRemindDialog",
                "TopLevelTooltip",
                "Se attivo, il promemoria sarà sempre visibile sopra le altre finestre",
              )}
            >
              {t("ManageRemindDialog", "TopLevelText", "Mostra in alto")}
            </span>
          </label>
        </div>

        <hr className="form-section" />

        <label className="field">
          <span>{t("General", "ExecutionMethodText", "Metodo di esecuzione")}</span>
          <select
            value={executionMethod}
            title={t("ManageRemindDialog", "ExecutionMethodTooltip", "Seleziona come deve essere attivato il promemoria.")}
            onChange={(e) => setExecutionMethod(e.target.value as ExecutionMethod)}
          >
            {EXECUTION_METHOD_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </label>

        <div className="field-row">
          <label className="field">
            <span>{t("ManageRemindDialog", "DateFromText", "Da")}</span>
            <input type="time" value={timeFrom} disabled={!timeFromEnabled} onChange={(e) => setTimeFrom(e.target.value)} />
          </label>
          <label className="field">
            <span>{t("ManageRemindDialog", "DateToText", "A")}</span>
            <input type="time" value={timeTo} disabled={!timeRangeEnabled} onChange={(e) => setTimeTo(e.target.value)} />
          </label>
        </div>

        <div className="field-row">
          <button className="btn" type="button" disabled={!intervalEnabled} onClick={() => setShowTimePicker(true)}>
            {t("TimePickerDialog", "TimeIntervalTitle", "Intervallo di tempo")}
          </button>
          <span className="time-frequency-label" title={t("TimePickerDialog", "Format", "gg.OO:mm")}>
            {timeInterval.days}.{timeInterval.hours}:{timeInterval.minutes}
          </span>
        </div>

        {error && <p className="field-error">{error}</p>}

        <div className="modal-actions reminder-form-actions">
          <button className="btn" type="button" onClick={() => setShowPreview(true)}>
            {t("ManageRemindDialog", "PreviewText", "Anteprima promemoria")}
          </button>
          <div className="modal-actions-right">
            <button className="btn" onClick={onCancel}>
              {t("General", "CancelButton", "Annulla")}
            </button>
            <button className="btn btn-primary" onClick={handleSave}>
              {t("General", "OkButton", "Ok")}
            </button>
          </div>
        </div>
      </div>

      {showTimePicker && (
        <TimePickerDialog
          initialValue={timeInterval}
          onCancel={() => setShowTimePicker(false)}
          onConfirm={(value) => {
            setTimeInterval(value);
            setShowTimePicker(false);
          }}
        />
      )}

      {showPreview && (
        <ReminderPreviewDialog
          name={name}
          description={description}
          icon={icon}
          sound={sound}
          isTopLevel={isTopLevel}
          onClose={() => setShowPreview(false)}
        />
      )}
    </div>
  );
}
