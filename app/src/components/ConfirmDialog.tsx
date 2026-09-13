import { useI18n } from "../lib/i18n";

interface ConfirmDialogProps {
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({ title, message, onConfirm, onCancel }: ConfirmDialogProps) {
  const { t } = useI18n();
  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>{title}</h2>
        <p>{message}</p>
        <div className="modal-actions">
          <button className="btn" onClick={onCancel}>
            {t("General", "CancelButton", "Annulla")}
          </button>
          <button className="btn btn-primary" onClick={onConfirm} autoFocus>
            {t("General", "OkButton", "Ok")}
          </button>
        </div>
      </div>
    </div>
  );
}
