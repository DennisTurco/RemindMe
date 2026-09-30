import { useEffect, useRef, useState } from "react";
import { ConfirmDialog } from "../components/ConfirmDialog";
import { MarkdownContent } from "../components/MarkdownContent";
import { PreferencesDialog } from "../components/PreferencesDialog";
import { PromptDialog } from "../components/PromptDialog";
import { ReminderFormDialog } from "../components/ReminderFormDialog";
import { ReminderPreviewDialog } from "../components/ReminderPreviewDialog";
import { EXECUTION_METHOD_TRANSLATION, iconPath, timeIntervalToString } from "../lib/catalog";
import { useI18n } from "../lib/i18n";
import { remindMe } from "../lib/ipc";
import { getEffectiveTheme, toggleTheme } from "../lib/theme";
import type { Remind } from "../lib/types";
import { formatDate } from "../utils/date_formatter";

interface ContextMenuState {
  x: number;
  y: number;
  remind: Remind;
}

export function MainPage() {
  const { t } = useI18n();
  const [reminds, setReminds] = useState<Remind[]>([]);
  const [search, setSearch] = useState("");
  const [selectedName, setSelectedName] = useState<string | null>(null);
  const [contextMenu, setContextMenu] = useState<ContextMenuState | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingRemind, setEditingRemind] = useState<Remind | null>(null);
  const [renamingRemind, setRenamingRemind] = useState<Remind | null>(null);
  const [renameConflict, setRenameConflict] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<"selection" | null>(null);
  const [previewRemind, setPreviewRemind] = useState<Remind | null>(null);
  const [infoMessage, setInfoMessage] = useState<{ title: string; message: string } | null>(null);
  const [theme, setThemeState] = useState(getEffectiveTheme);
  const [showPreferences, setShowPreferences] = useState(false);
  const tableWrapperRef = useRef<HTMLDivElement>(null);

  function handleToggleTheme() {
    setThemeState(toggleTheme());
  }

  async function refresh(query: string) {
    const list = query ? await remindMe.search(query) : await remindMe.getAll();
    setReminds(list);
  }

  useEffect(() => {
    refresh("");
  }, []);

  useEffect(() => {
    function closeMenu() {
      setContextMenu(null);
    }
    window.addEventListener("click", closeMenu);
    return () => window.removeEventListener("click", closeMenu);
  }, []);

  useEffect(() => remindMe.onNewReminderRequested(() => setShowCreateForm(true)), []);
  useEffect(() => remindMe.onOpenPreferencesRequested(() => setShowPreferences(true)), []);
  useEffect(() => remindMe.onRemindersChanged(() => refresh(search)), [search]);

  async function handleExportCsv() {
    const result = await remindMe.exportCsv();
    if (!result.canceled) {
      setInfoMessage({
        title: t("Dialogs", "SuccessGenericTitle", "Success"),
        message: t("Dialogs", "SuccessfullyExportedToCsvMessage", "Backups exported to CSV successfully!"),
      });
    }
  }

  async function handleExportPdf() {
    const result = await remindMe.exportPdf();
    if (!result.canceled) {
      setInfoMessage({
        title: t("Dialogs", "SuccessGenericTitle", "Success"),
        message: t("Dialogs", "SuccessfullyExportedToPdfMessage", "Backups exported to PDF successfully!"),
      });
    }
  }

  async function handleSearchChange(value: string) {
    setSearch(value);
    await refresh(value);
  }

  const selected = reminds.find((r) => r.name === selectedName) ?? null;

  function selectRow(remind: Remind) {
    setSelectedName(remind.name);
  }

  function openContextMenu(e: React.MouseEvent, remind: Remind) {
    e.preventDefault();
    selectRow(remind);
    setContextMenu({ x: e.clientX, y: e.clientY, remind });
  }

  async function handleCreate(remind: Remind) {
    await remindMe.create(remind);
    setShowCreateForm(false);
    await refresh(search);
  }

  async function handleUpdate(remind: Remind) {
    if (editingRemind) {
      await remindMe.update(editingRemind.name, remind);
      setEditingRemind(null);
      await refresh(search);
    }
  }

  async function handleDuplicate(remind: Remind) {
    await remindMe.duplicate(remind.name);
    setContextMenu(null);
    await refresh(search);
  }

  async function handleDeleteNoConfirm(remind: Remind) {
    await remindMe.remove(remind.name);
    setContextMenu(null);
    if (selectedName === remind.name) setSelectedName(null);
    await refresh(search);
  }

  async function handleConfirmedDelete() {
    if (selected) {
      await remindMe.remove(selected.name);
      setSelectedName(null);
    }
    setDeleteTarget(null);
    await refresh(search);
  }

  async function handleToggleActive(remind: Remind, value: boolean) {
    await remindMe.setActive(remind.name, value);
    setContextMenu(null);
    await refresh(search);
  }

  async function handleToggleTopLevel(remind: Remind, value: boolean) {
    await remindMe.setTopLevel(remind.name, value);
    setContextMenu(null);
    await refresh(search);
  }

  async function handleRenameConfirm(newName: string) {
    if (!renamingRemind) return;
    if (newName !== renamingRemind.name && reminds.some((r) => r.name === newName)) {
      setRenameConflict(t("Dialogs", "RemindNameAlreadyUsedMessage", "Remind name already used!"));
      return;
    }
    await remindMe.rename(renamingRemind.name, newName);
    setRenamingRemind(null);
    if (selectedName === renamingRemind.name) setSelectedName(newName);
    await refresh(search);
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (!selected) return;
    if (e.key === "Delete") {
      setDeleteTarget("selection");
    } else if (e.key === "Enter") {
      setEditingRemind(selected);
    }
  }

  return (
    <div className="main-page">
      <div className="toolbar">
        <button
          className="btn btn-icon"
          title={t("MainFrame", "AddBackupTooltip", "Add new reminder")}
          onClick={() => setShowCreateForm(true)}
        >
          +
        </button>
        <input
          className="search-bar"
          placeholder={t("MainFrame", "ResearchBarPlaceholder", "Search...")}
          title={t("MainFrame", "ResearchBarTooltip", "Research bar")}
          value={search}
          onChange={(e) => handleSearchChange(e.target.value)}
        />
        <span className="toolbar-spacer" />
        <span className="toolbar-label">{t("MainFrame", "ExportAs", "Export as: ")}</span>
        <div className="toolbar-group">
          <button className="btn" title={t("MainFrame", "ExportAsCsvTooltip", "Export as CSV")} onClick={handleExportCsv}>
            CSV
          </button>
          <button className="btn" title={t("MainFrame", "ExportAsPdfTooltip", "Export as PDF")} onClick={handleExportPdf}>
            PDF
          </button>
        </div>
        <button
          className="btn btn-icon"
          title={
            theme === "dark"
              ? t("MainFrame", "SwitchToLightThemeTooltip", "Switch to light theme")
              : t("MainFrame", "SwitchToDarkThemeTooltip", "Switch to dark theme")
          }
          onClick={handleToggleTheme}
        >
          {theme === "dark" ? "☀" : "🌙"}
        </button>
      </div>

      <div className="remind-table-wrapper" ref={tableWrapperRef} tabIndex={0} onKeyDown={handleKeyDown}>
        <table className="remind-table">
          <thead>
            <tr>
              <th>{t("RemindList", "IconColumn", "Icon")}</th>
              <th>{t("RemindList", "NameColumn", "Name")}</th>
              <th>{t("RemindList", "IsActiveColumn", "Active")}</th>
              <th>{t("RemindList", "IsTopLevelColumn", "Show on Top")}</th>
              <th>{t("RemindList", "LastExecutionColumn", "Last Execution")}</th>
              <th>{t("RemindList", "NextExecutionColumn", "Next Execution")}</th>
              <th>{t("RemindList", "TimeIntervalColumn", "Time Interval")}</th>
            </tr>
          </thead>
          <tbody>
            {reminds.map((remind, index) => (
              <tr
                key={remind.name}
                className={`${index % 2 === 0 ? "row-even" : "row-odd"} ${selectedName === remind.name ? "row-selected" : ""}`}
                onClick={() => selectRow(remind)}
                onDoubleClick={() => setEditingRemind(remind)}
                onContextMenu={(e) => openContextMenu(e, remind)}
              >
                <td>
                  <img src={iconPath(remind.icon)} alt="" width={24} height={24} />
                </td>
                <td>{remind.name}</td>
                <td className="checkbox-cell">
                  <input type="checkbox" checked={remind.isActive} readOnly />
                </td>
                <td className="checkbox-cell">
                  <input type="checkbox" checked={remind.isTopLevel} readOnly />
                </td>
                <td>{formatDate(remind.lastExecution ?? "")}</td>
                <td>{formatDate(remind.nextExecution ?? "")}</td>
                <td title={t("TimePickerDialog", "Format", "dd.HH:mm")}>{timeIntervalToString(remind.timeInterval)}</td>
              </tr>
            ))}
            {reminds.length === 0 && (
              <tr>
                <td colSpan={7} className="empty-row">
                  {t("General", "NoRemindText", "No reminders")}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {selected && (
        <div className="details-panel">
          <div className="details-panel-header">
            <h3>{selected.name}</h3>
            <div className="details-panel-actions">
              <button className="btn" onClick={() => setEditingRemind(selected)}>
                {t("RemindList", "EditPopup", "Edit")}
              </button>
              <button className="btn" onClick={() => setPreviewRemind(selected)}>
                {t("RemindList", "PreviewButton", "Preview")}
              </button>
              <button className="btn btn-danger" onClick={() => setDeleteTarget("selection")}>
                {t("RemindList", "DeletePopup", "Delete")}
              </button>
            </div>
          </div>
          {selected.description && <MarkdownContent className="details-description" text={selected.description} />}
          <dl className="details-grid">
            <dt>{t("RemindList", "IsActiveColumn", "Active")}</dt>
            <dd>
              <span className={`badge ${selected.isActive ? "badge-on" : ""}`}>
                {selected.isActive ? t("General", "YesText", "Yes") : t("General", "NoText", "No")}
              </span>
            </dd>
            <dt>{t("RemindList", "IsTopLevelColumn", "Show on Top")}</dt>
            <dd>
              <span className={`badge ${selected.isTopLevel ? "badge-on" : ""}`}>
                {selected.isTopLevel ? t("General", "YesText", "Yes") : t("General", "NoText", "No")}
              </span>
            </dd>
            <dt>{t("RemindList", "LastExecutionColumn", "Last Execution")}</dt>
            <dd>{formatDate(selected.lastExecution ?? "")}</dd>
            <dt>{t("RemindList", "NextExecutionColumn", "Next Execution")}</dt>
            <dd>{formatDate(selected.nextExecution ?? "")}</dd>
            <dt>{t("RemindList", "TimeIntervalColumn", "Time Interval")}</dt>
            <dd>{timeIntervalToString(selected.timeInterval)}</dd>
            <dt>{t("RemindList", "CreationDateLabel", "Creation date")}</dt>
            <dd>{formatDate(selected.creationDate ?? "")}</dd>
            <dt>{t("RemindList", "LastUpdateDateLabel", "Last update date")}</dt>
            <dd>{formatDate(selected.lastUpdateDate ?? "")}</dd>
            <dt>{t("RemindList", "CountDetail", "Count")}</dt>
            <dd>{selected.remindCount}</dd>
            <dt>{t("ManageRemindDialog", "ExecutionMethodText", "Execution method")}</dt>
            <dd>
              {t(
                "ExecutionMethod",
                EXECUTION_METHOD_TRANSLATION[selected.executionMethod].key,
                EXECUTION_METHOD_TRANSLATION[selected.executionMethod].fallback,
              )}
            </dd>
            {selected.executionMethod === "CUSTOM_TIME_RANGE" && selected.timeRange && (
              <>
                <dt>{t("RemindList", "TimeFromLabel", "Start time")}</dt>
                <dd>{selected.timeRange.start}</dd>
                <dt>{t("RemindList", "TimeToLabel", "End time")}</dt>
                <dd>{selected.timeRange.end}</dd>
              </>
            )}
            {selected.executionMethod === "ONE_TIME_PER_DAY" && selected.timeRange && (
              <>
                <dt>{t("RemindList", "TimeFromLabel", "Start time")}</dt>
                <dd>{selected.timeRange.start}</dd>
              </>
            )}
          </dl>
        </div>
      )}

      <footer className="app-footer">
        <span>
          {t("General", "Version", "Version")} {__APP_VERSION__}
        </span>
        <span className="app-footer-links">
          <button className="btn btn-link" onClick={() => remindMe.openLink("infoPage")}>
            {t("MainFrame", "GithubButton", "GitHub")}
          </button>
          <button className="btn btn-link" onClick={() => remindMe.openLink("issuePage")}>
            {t("MainFrame", "ReportIssueButton", "Report an issue")}
          </button>
          <button className="btn btn-link" onClick={() => remindMe.openLink("donatePaypal")}>
            {t("MainFrame", "DonatePaypalButton", "Donate via PayPal")}
          </button>
        </span>
      </footer>

      {contextMenu && (
        <ul className="context-menu" style={{ top: contextMenu.y, left: contextMenu.x }} onClick={(e) => e.stopPropagation()}>
          <li
            onClick={() => {
              setEditingRemind(contextMenu.remind);
              setContextMenu(null);
            }}
          >
            {t("RemindList", "EditPopup", "Edit")}
          </li>
          <li onClick={() => handleDuplicate(contextMenu.remind)}>{t("RemindList", "DuplicatePopup", "Duplicate")}</li>
          <li
            onClick={() => {
              setRenamingRemind(contextMenu.remind);
              setContextMenu(null);
            }}
          >
            {t("RemindList", "RenamePopup", "Rename")}
          </li>
          <li className="context-menu-separator" />
          <li className="context-menu-submenu-label">{t("RemindList", "EnableDisablePopup", "Enable / disable")}</li>
          <li className="context-menu-checkbox">
            <label>
              <input
                type="checkbox"
                checked={contextMenu.remind.isActive}
                onChange={(e) => handleToggleActive(contextMenu.remind, e.target.checked)}
              />
              {t("RemindList", "ActivePopup", "Active")}
            </label>
          </li>
          <li className="context-menu-checkbox">
            <label>
              <input
                type="checkbox"
                checked={contextMenu.remind.isTopLevel}
                onChange={(e) => handleToggleTopLevel(contextMenu.remind, e.target.checked)}
              />
              {t("RemindList", "TopLevelPopup", "Show on top")}
            </label>
          </li>
          <li className="context-menu-separator" />
          <li className="context-menu-item-danger" onClick={() => handleDeleteNoConfirm(contextMenu.remind)}>
            {t("RemindList", "DeletePopup", "Delete")}
          </li>
        </ul>
      )}

      {showCreateForm && (
        <ReminderFormDialog
          mode="create"
          isNameTaken={(name) => reminds.some((r) => r.name === name)}
          onSave={handleCreate}
          onCancel={() => setShowCreateForm(false)}
        />
      )}

      {editingRemind && (
        <ReminderFormDialog
          mode="edit"
          initialRemind={editingRemind}
          isNameTaken={() => false}
          onSave={handleUpdate}
          onCancel={() => setEditingRemind(null)}
        />
      )}

      {renamingRemind && (
        <PromptDialog
          title={t("RemindList", "RenamePopup", "Rename")}
          label={t("Dialogs", "RemindNameInput", "Name of the reminder")}
          initialValue={renamingRemind.name}
          onConfirm={handleRenameConfirm}
          onCancel={() => setRenamingRemind(null)}
        />
      )}

      {renameConflict && (
        <ConfirmDialog
          title={t("Dialogs", "ErrorGenericTitle", "Error")}
          message={renameConflict}
          onConfirm={() => setRenameConflict(null)}
          onCancel={() => setRenameConflict(null)}
        />
      )}

      {deleteTarget === "selection" && selected && (
        <ConfirmDialog
          title={t("Dialogs", "ConfirmationDeletionTitle", "Confirm Deletion")}
          message={t("Dialogs", "ConfirmationDeletionMessage", "Are you sure you want to delete the selected rows?")}
          onConfirm={handleConfirmedDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}

      {previewRemind && (
        <ReminderPreviewDialog
          name={previewRemind.name}
          description={previewRemind.description}
          icon={previewRemind.icon}
          sound={previewRemind.sound}
          isTopLevel={previewRemind.isTopLevel}
          onClose={() => setPreviewRemind(null)}
        />
      )}

      {showPreferences && <PreferencesDialog onClose={() => setShowPreferences(false)} />}

      {infoMessage && (
        <ConfirmDialog
          title={infoMessage.title}
          message={infoMessage.message}
          onConfirm={() => setInfoMessage(null)}
          onCancel={() => setInfoMessage(null)}
        />
      )}
    </div>
  );
}
