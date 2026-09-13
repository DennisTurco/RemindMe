import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { MarkdownContent } from "../components/MarkdownContent";
import { iconPath, soundPath } from "../lib/catalog";
import { useI18n } from "../lib/i18n";
import { remindMe } from "../lib/ipc";
import type { Remind } from "../lib/types";

const MAX_NAME_LENGTH = 30;

function truncateName(name: string): string {
  return name.length > MAX_NAME_LENGTH ? `${name.slice(0, MAX_NAME_LENGTH)}...` : name;
}

/**
 * Mirrors remindme.Dialogs.ReminderDialog: the actual notification window
 * shown for a due reminder (as opposed to ReminderPreviewDialog, which is
 * the in-form "what would this look like" preview).
 */
export function ReminderPopupPage() {
  const { t } = useI18n();
  const [params] = useSearchParams();
  const [remind, setRemind] = useState<Remind | null>(null);
  const name = params.get("name") ?? "";

  useEffect(() => {
    remindMe.getByName(name).then(setRemind);
  }, [name]);

  useEffect(() => {
    if (!remind) return;
    const path = soundPath(remind.sound);
    if (path) {
      new Audio(path).play().catch(() => {});
    }
    // Plays once, when the reminder data has loaded, mirroring ReminderDialog's initDialog.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [remind?.name]);

  if (!remind) return null;

  const time = new Date().toLocaleTimeString();

  return (
    <div className={`popup-page ${remind.isTopLevel ? "popup-page-top-level" : ""}`}>
      <div className="reminder-preview-header">
        <img src={iconPath(remind.icon)} alt="" width={50} height={50} />
        <span className="reminder-preview-name">{truncateName(remind.name)}</span>
      </div>
      <MarkdownContent className="reminder-preview-description" text={remind.description} />
      <div className="modal-actions reminder-preview-footer">
        <span className="reminder-preview-time">{time}</span>
        <button className="btn btn-primary" onClick={() => window.close()} autoFocus>
          {t("General", "OkButton", "Ok")}
        </button>
      </div>
    </div>
  );
}
