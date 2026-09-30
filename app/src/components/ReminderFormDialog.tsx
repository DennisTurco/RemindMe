import { useState } from "react";
import {
  EXECUTION_METHOD_OPTIONS,
  EXECUTION_METHOD_TRANSLATION,
  ICON_OPTIONS,
  SOUND_OPTIONS,
  iconPath,
  soundPath,
  timeIntervalToString,
} from "../lib/catalog";
import { useI18n } from "../lib/i18n";
import { createDefaultRemind } from "../lib/types";
import type { ExecutionMethod, Remind, TimeInterval } from "../lib/types";
import { Icon } from "./Icon";
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
      setError(t("Dialogs", "ErrorMessageForEmptyRemindName", "Remind name cannot be empty"));
      return;
    }
    if (!isEdit && isNameTaken(trimmedName)) {
      setError(
        t(
          "Dialogs",
          "ErrorMessageDuplicatedRedind",
          "Cannot create a reminder with this name because one already exists.",
        ),
      );
      return;
    }

    let timeRange: Remind["timeRange"] = null;
    if (executionMethod === "CUSTOM_TIME_RANGE") {
      if (!(timeFrom < timeTo)) {
        setError(t("Dialogs", "ErrorMessageForWrongTimeRange", "Time range is not valid"));
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
      <div className="modal modal-scrollable reminder-form" onClick={(e) => e.stopPropagation()}>
        <h2>
          {isEdit
            ? t("ManageRemindDialog", "EditTitle", "Edit reminder")
            : t("ManageRemindDialog", "CreateTitle", "Create a new reminder")}
        </h2>

        <div className="modal-body">
          <label className="field">
            <span>{t("ManageRemindDialog", "NameText", "Name")}</span>
            <input
              value={name}
              disabled={isEdit}
              placeholder={t("ManageRemindDialog", "NamePlaceholder", "Enter reminder name")}
              title={t("ManageRemindDialog", "NameTooltip", "Enter a name for the reminder")}
              onChange={(e) => setName(e.target.value)}
            />
          </label>

          <label className="field">
            <span className="field-label-with-hint">
              {t("ManageRemindDialog", "DescriptionText", "Description")}
              <span
                className="info-icon"
                title={t(
                  "ManageRemindDialog",
                  "DescriptionMarkdownHint",
                  "Puoi usare la sintassi Markdown (es. **grassetto**, elenchi puntati, link) per formattare la descrizione.",
                )}
              >
                i
              </span>
            </span>
            <textarea
              value={description}
              placeholder={t("ManageRemindDialog", "DescriptionPlaceholder", "Enter description (optional)")}
              title={t("ManageRemindDialog", "DescriptionTooltip", "Provide additional details for this reminder")}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
            />
          </label>

          <div className="field-row">
            <label className="field">
              <span>{t("ManageRemindDialog", "IconText", "Icon")}</span>
              <select
                value={icon}
                title={t("ManageRemindDialog", "IconTooltip", "Choose an icon for the reminder notification")}
                onChange={(e) => setIcon(e.target.value as Remind["icon"])}
              >
                {ICON_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
            <img className="icon-preview" src={iconPath(icon)} alt="" />
          </div>

          <div className="field-row">
            <label className="field">
              <span>{t("ManageRemindDialog", "SoundText", "Sound")}</span>
              <select
                value={sound}
                title={t("ManageRemindDialog", "SoundTooltip", "Choose a sound for the reminder notification")}
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
              title={t("ManageRemindDialog", "SoundButtonTooltip", "Preview the selected sound")}
              disabled={sound === "NO_SOUND"}
              onClick={playSoundPreview}
            >
              <Icon name="play" size={14} />
            </button>
          </div>

          <div className="checkbox-row">
            <label className="checkbox-field checkbox-card">
              <input type="checkbox" checked={isActive} onChange={(e) => setIsActive(e.target.checked)} />
              <span title={t("ManageRemindDialog", "ActiveTooltip", "Activate or disable the reminder")}>
                {t("ManageRemindDialog", "ActiveText", "Active")}
              </span>
            </label>
            <label className="checkbox-field checkbox-card">
              <input type="checkbox" checked={isTopLevel} onChange={(e) => setIsTopLevel(e.target.checked)} />
              <span
                title={t(
                  "ManageRemindDialog",
                  "TopLevelTooltip",
                  "If enabled, the reminder will always appear on top of other windows",
                )}
              >
                {t("ManageRemindDialog", "TopLevelText", "Show on Top")}
              </span>
            </label>
          </div>

          <label className="field">
            <span>{t("ManageRemindDialog", "ExecutionMethodText", "Execution method")}</span>
            <select
              value={executionMethod}
              title={t("ManageRemindDialog", "ExecutionMethodTooltip", "Select how the reminder should be triggered. 'PC Startup' runs the reminder when the computer starts, ignoring time intervals. 'Custom Time Range' triggers it only within a defined daily time window.")}
              onChange={(e) => setExecutionMethod(e.target.value as ExecutionMethod)}
            >
              {EXECUTION_METHOD_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {t("ExecutionMethod", EXECUTION_METHOD_TRANSLATION[opt.value].key, EXECUTION_METHOD_TRANSLATION[opt.value].fallback)}
                </option>
              ))}
            </select>
          </label>

          <div className="field-row">
            <label className="field">
              <span>{t("ManageRemindDialog", "DateFromText", "From")}</span>
              <input type="time" value={timeFrom} disabled={!timeFromEnabled} onChange={(e) => setTimeFrom(e.target.value)} />
            </label>
            <label className="field">
              <span>{t("ManageRemindDialog", "DateToText", "To")}</span>
              <input type="time" value={timeTo} disabled={!timeRangeEnabled} onChange={(e) => setTimeTo(e.target.value)} />
            </label>
          </div>

          <div className={`interval-row ${intervalEnabled ? "" : "is-disabled"}`}>
            <span className="interval-value" title={t("TimePickerDialog", "Format", "dd.HH:mm")}>
              <Icon name="clock" size={15} />
              {timeIntervalToString(timeInterval)}
            </span>
            <button className="btn" type="button" disabled={!intervalEnabled} onClick={() => setShowTimePicker(true)}>
              {t("TimePickerDialog", "TimeIntervalTitle", "Time interval for reminder")}
            </button>
          </div>

          {error && <p className="field-error">{error}</p>}
        </div>

        <div className="modal-actions reminder-form-actions">
          <button className="btn" type="button" onClick={() => setShowPreview(true)}>
            <Icon name="eye" size={15} />
            {t("ManageRemindDialog", "PreviewText", "Reminder preview")}
          </button>
          <div className="modal-actions-right">
            <button className="btn" onClick={onCancel}>
              {t("General", "CancelButton", "Cancel")}
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
