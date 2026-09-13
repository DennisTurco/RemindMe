import { useEffect } from "react";
import { iconPath, soundPath } from "../lib/catalog";
import { useI18n } from "../lib/i18n";
import type { IconName, SoundName } from "../lib/types";
import { MarkdownContent } from "./MarkdownContent";

interface ReminderPreviewDialogProps {
  name: string;
  description: string;
  icon: IconName;
  sound: SoundName;
  isTopLevel: boolean;
  onClose: () => void;
}

const MAX_NAME_LENGTH = 30;

function truncateName(name: string): string {
  return name.length > MAX_NAME_LENGTH ? `${name.slice(0, MAX_NAME_LENGTH)}...` : name;
}

/** Mirrors remindme.Dialogs.ReminderDialog: preview of how the notification popup will look. */
export function ReminderPreviewDialog({ name, description, icon, sound, isTopLevel, onClose }: ReminderPreviewDialogProps) {
  const { t } = useI18n();
  const time = new Date().toLocaleTimeString("it-IT");

  useEffect(() => {
    const path = soundPath(sound);
    if (path) {
      new Audio(path).play().catch(() => {});
    }
    // Plays once, on open, mirroring ReminderDialog's initDialog behaviour.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className={`modal reminder-preview ${isTopLevel ? "reminder-preview-top-level" : ""}`} onClick={(e) => e.stopPropagation()}>
        <div className="reminder-preview-header">
          <img src={iconPath(icon)} alt="" width={50} height={50} />
          <span className="reminder-preview-name">{truncateName(name) || "(senza nome)"}</span>
        </div>
        <MarkdownContent className="reminder-preview-description" text={description} />
        <div className="modal-actions reminder-preview-footer">
          <span className="reminder-preview-time">{time}</span>
          <button className="btn btn-primary" onClick={onClose} autoFocus>
            {t("General", "OkButton", "Ok")}
          </button>
        </div>
      </div>
    </div>
  );
}
