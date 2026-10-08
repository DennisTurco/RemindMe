import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { ConfirmDialog } from "../components/ConfirmDialog";
import { Icon } from "../components/Icon";
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

const MENU_EDGE_MARGIN = 8;

/** Read-only yes/no indicator for table cells (a real checkbox here would look clickable). */
function StatusMark({ on, label }: { on: boolean; label: string }) {
  return (
    <span className={`status-mark ${on ? "on" : "off"}`} role="img" aria-label={label} title={label}>
      {on && <Icon name="check" size={13} />}
    </span>
  );
}

function DateValue({ value }: { value: string | null }) {
  const formatted = formatDate(value ?? "");
  return formatted ? <>{formatted}</> : <span className="value-empty">—</span>;
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
  const contextMenuRef = useRef<HTMLUListElement>(null);

  // Keep the context menu fully on screen when opened near the right/bottom edge.
  useLayoutEffect(() => {
    const menu = contextMenuRef.current;
    if (!menu || !contextMenu) return;
    // offsetWidth/Height ignore the open animation's transform, unlike getBoundingClientRect.
    const width = menu.offsetWidth;
    const height = menu.offsetHeight;
    menu.style.left = `${Math.max(MENU_EDGE_MARGIN, Math.min(contextMenu.x, window.innerWidth - width - MENU_EDGE_MARGIN))}px`;
    menu.style.top = `${Math.max(MENU_EDGE_MARGIN, Math.min(contextMenu.y, window.innerHeight - height - MENU_EDGE_MARGIN))}px`;
  }, [contextMenu]);

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

  async function handleexportJson() {
    const result = await remindMe.exportJson();
    if (!result.canceled) {
      setInfoMessage({
        title: t("Dialogs", "SuccessGenericTitle", "Success"),
        message: t("Dialogs", "SuccessfullyExportedToJsonMessage", "Backups exported to JSON successfully!"),
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
          className="btn btn-primary"
          title={t("MainFrame", "AddBackupTooltip", "Add new reminder")}
          onClick={() => setShowCreateForm(true)}
        >
          <Icon name="plus" />
          {t("Menu", "New", "New")}
        </button>
        <div className="search-field">
          <Icon name="search" size={15} />
          <input
            className="search-bar"
            type="search"
            placeholder={t("MainFrame", "ResearchBarPlaceholder", "Search...")}
            title={t("MainFrame", "ResearchBarTooltip", "Research bar")}
            value={search}
            onChange={(e) => handleSearchChange(e.target.value)}
          />
        </div>
        <span className="toolbar-spacer" />
        <span className="toolbar-label">{t("MainFrame", "ExportAs", "Export as: ")}</span>
        <div className="segmented">
          <button className="btn" title={t("MainFrame", "ExportAsCsvTooltip", "Export as CSV")} onClick={handleExportCsv}>
            CSV
          </button>
          <button className="btn" title={t("MainFrame", "ExportAsJsonTooltip", "Export as JSON")} onClick={handleexportJson}>
            JSON
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
          <Icon name={theme === "dark" ? "sun" : "moon"} />
        </button>
      </div>

      <div className="remind-table-wrapper" ref={tableWrapperRef} tabIndex={0} onKeyDown={handleKeyDown}>
        <table className="remind-table">
          <thead>
            <tr>
              <th className="col-icon">{t("RemindList", "IconColumn", "Icon")}</th>
              <th className="col-name">{t("RemindList", "NameColumn", "Name")}</th>
              <th className="col-center">{t("RemindList", "IsActiveColumn", "Active")}</th>
              <th className="col-center">{t("RemindList", "IsTopLevelColumn", "Show on Top")}</th>
              <th>{t("RemindList", "LastExecutionColumn", "Last Execution")}</th>
              <th>{t("RemindList", "NextExecutionColumn", "Next Execution")}</th>
              <th title={t("TimePickerDialog", "Format", "dd.HH:mm")}>{t("RemindList", "TimeIntervalColumn", "Time Interval")}</th>
            </tr>
          </thead>
          <tbody>
            {reminds.map((remind) => (
              <tr
                key={remind.name}
                className={`${selectedName === remind.name ? "is-selected" : ""} ${remind.isActive ? "" : "is-inactive"}`}
                onClick={() => selectRow(remind)}
                onDoubleClick={() => setEditingRemind(remind)}
                onContextMenu={(e) => openContextMenu(e, remind)}
              >
                <td className="col-icon">
                  <img className="remind-icon" src={iconPath(remind.icon)} alt="" />
                </td>
                <td className="col-name">{remind.name}</td>
                <td className="col-center">
                  <StatusMark
                    on={remind.isActive}
                    label={remind.isActive ? t("General", "YesText", "Yes") : t("General", "NoText", "No")}
                  />
                </td>
                <td className="col-center">
                  <StatusMark
                    on={remind.isTopLevel}
                    label={remind.isTopLevel ? t("General", "YesText", "Yes") : t("General", "NoText", "No")}
                  />
                </td>
                <td className="col-date">
                  <DateValue value={remind.lastExecution} />
                </td>
                <td className="col-date">
                  <DateValue value={remind.nextExecution} />
                </td>
                <td className="col-interval" title={t("TimePickerDialog", "Format", "dd.HH:mm")}>
                  {timeIntervalToString(remind.timeInterval)}
                </td>
              </tr>
            ))}
            {reminds.length === 0 && (
              <tr className="empty-row">
                <td colSpan={7} className="empty-state">
                  <div className="empty-state-icon">
                    <Icon name="bell" size={22} />
                  </div>
                  <p className="empty-state-text">{t("General", "NoRemindText", "No reminders")}</p>
                  {!search && (
                    <button className="btn btn-primary" onClick={() => setShowCreateForm(true)}>
                      <Icon name="plus" />
                      {t("MainFrame", "AddBackupTooltip", "Add new reminder")}
                    </button>
                  )}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {selected && (
        <div className="details-panel">
          <div className="details-panel-header">
            <img className="details-panel-icon" src={iconPath(selected.icon)} alt="" />
            <div className="details-panel-title">
              <h3>{selected.name}</h3>
              <div className="details-panel-badges">
                <span className={`badge ${selected.isActive ? "badge-on" : ""}`}>
                  {t("RemindList", "IsActiveColumn", "Active")}:{" "}
                  {selected.isActive ? t("General", "YesText", "Yes") : t("General", "NoText", "No")}
                </span>
                <span className={`badge ${selected.isTopLevel ? "badge-on" : ""}`}>
                  {t("RemindList", "IsTopLevelColumn", "Show on Top")}:{" "}
                  {selected.isTopLevel ? t("General", "YesText", "Yes") : t("General", "NoText", "No")}
                </span>
              </div>
            </div>
            <div className="details-panel-actions">
              <button className="btn" onClick={() => setEditingRemind(selected)}>
                <Icon name="edit" size={15} />
                {t("RemindList", "EditPopup", "Edit")}
              </button>
              <button className="btn" onClick={() => setPreviewRemind(selected)}>
                <Icon name="eye" size={15} />
                {t("RemindList", "PreviewButton", "Preview")}
              </button>
              <button
                className="btn btn-danger btn-icon"
                title={t("RemindList", "DeletePopup", "Delete")}
                aria-label={t("RemindList", "DeletePopup", "Delete")}
                onClick={() => setDeleteTarget("selection")}
              >
                <Icon name="trash" size={15} />
              </button>
            </div>
          </div>
          {selected.description && <MarkdownContent className="details-description" text={selected.description} />}
          <dl className="details-grid">
            <div className="details-item">
              <dt>{t("ManageRemindDialog", "ExecutionMethodText", "Execution method")}</dt>
              <dd>
                {t(
                  "ExecutionMethod",
                  EXECUTION_METHOD_TRANSLATION[selected.executionMethod].key,
                  EXECUTION_METHOD_TRANSLATION[selected.executionMethod].fallback,
                )}
              </dd>
            </div>
            {selected.executionMethod !== "PC_STARTUP" && selected.timeRange && (
              <div className="details-item">
                <dt>{t("RemindList", "TimeFromLabel", "Start time")}</dt>
                <dd>{selected.timeRange.start.slice(0, 5)}</dd>
              </div>
            )}
            {selected.executionMethod === "CUSTOM_TIME_RANGE" && selected.timeRange && (
              <div className="details-item">
                <dt>{t("RemindList", "TimeToLabel", "End time")}</dt>
                <dd>{selected.timeRange.end.slice(0, 5)}</dd>
              </div>
            )}
            <div className="details-item">
              <dt>{t("RemindList", "TimeIntervalColumn", "Time Interval")}</dt>
              <dd title={t("TimePickerDialog", "Format", "dd.HH:mm")}>{timeIntervalToString(selected.timeInterval)}</dd>
            </div>
            <div className="details-item">
              <dt>{t("RemindList", "LastExecutionColumn", "Last Execution")}</dt>
              <dd>
                <DateValue value={selected.lastExecution} />
              </dd>
            </div>
            <div className="details-item">
              <dt>{t("RemindList", "NextExecutionColumn", "Next Execution")}</dt>
              <dd>
                <DateValue value={selected.nextExecution} />
              </dd>
            </div>
            <div className="details-item">
              <dt>{t("RemindList", "CountDetail", "Count")}</dt>
              <dd>{selected.remindCount}</dd>
            </div>
            <div className="details-item">
              <dt>{t("RemindList", "CreationDateLabel", "Creation date")}</dt>
              <dd>
                <DateValue value={selected.creationDate} />
              </dd>
            </div>
            <div className="details-item">
              <dt>{t("RemindList", "LastUpdateDateLabel", "Last update date")}</dt>
              <dd>
                <DateValue value={selected.lastUpdateDate} />
              </dd>
            </div>
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
        <ul className="context-menu" ref={contextMenuRef} onClick={(e) => e.stopPropagation()}>
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
