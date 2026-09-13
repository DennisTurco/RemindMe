import { useState } from "react";
import { useI18n } from "../lib/i18n";

interface PromptDialogProps {
  title: string;
  label: string;
  initialValue: string;
  onConfirm: (value: string) => void;
  onCancel: () => void;
}

export function PromptDialog({ title, label, initialValue, onConfirm, onCancel }: PromptDialogProps) {
  const { t } = useI18n();
  const [value, setValue] = useState(initialValue);

  function handleConfirm() {
    const trimmed = value.trim();
    if (trimmed) {
      onConfirm(trimmed);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>{title}</h2>
        <label className="field">
          <span>{label}</span>
          <input
            autoFocus
            value={value}
            onChange={(e) => setValue(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleConfirm();
              if (e.key === "Escape") onCancel();
            }}
          />
        </label>
        <div className="modal-actions">
          <button className="btn" onClick={onCancel}>
            {t("General", "CancelButton", "Annulla")}
          </button>
          <button className="btn btn-primary" onClick={handleConfirm} disabled={!value.trim()}>
            {t("General", "OkButton", "Ok")}
          </button>
        </div>
      </div>
    </div>
  );
}
