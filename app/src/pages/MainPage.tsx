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
        title: t("Dialogs", "SuccessGenericTitle", "Successo"),
        message: t("Dialogs", "SuccessfullyExportedToCsvMessage", "Backup esportati in CSV con successo!"),
      });
    }
  }

  async function handleExportPdf() {
    const result = await remindMe.exportPdf();
    if (!result.canceled) {
      setInfoMessage({
        title: t("Dialogs", "SuccessGenericTitle", "Successo"),
        message: t("Dialogs", "SuccessfullyExportedToPdfMessage", "Backup esportati in PDF con successo!"),
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
      setRenameConflict(t("Dialogs", "RemindNameAlreadyUsedMessage", "Nome del promemoria già in uso!"));
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
          title={t("MainFrame", "AddBackupTooltip", "Aggiungi nuovo promemoria")}
          onClick={() => setShowCreateForm(true)}
        >
          +
        </button>
        <input
          className="search-bar"
          placeholder={t("MainFrame", "ResearchBarPlaceholder", "Cerca...")}
          title={t("MainFrame", "ResearchBarTooltip", "Barra di ricerca")}
          value={search}
          onChange={(e) => handleSearchChange(e.target.value)}
        />
        <span className="toolbar-spacer" />
        <span className="toolbar-label">{t("MainFrame", "ExportAs", "Esporta come: ")}</span>
        <div className="toolbar-group">
          <button className="btn" title={t("MainFrame", "ExportAsCsvTooltip", "Esporta come CSV")} onClick={handleExportCsv}>
            CSV
          </button>
          <button className="btn" title={t("MainFrame", "ExportAsPdfTooltip", "Esporta come PDF")} onClick={handleExportPdf}>
            PDF
          </button>
        </div>
        <button
          className="btn btn-icon"
          title={
            theme === "dark"
              ? t("MainFrame", "SwitchToLightThemeTooltip", "Passa al tema chiaro")
              : t("MainFrame", "SwitchToDarkThemeTooltip", "Passa al tema scuro")
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
              <th>{t("RemindList", "IconColumn", "Icona")}</th>
              <th>{t("RemindList", "NameColumn", "Nome")}</th>
              <th>{t("RemindList", "IsActiveColumn", "Attivo")}</th>
              <th>{t("RemindList", "IsTopLevelColumn", "Mostra in alto")}</th>
              <th>{t("RemindList", "LastExecutionColumn", "Ultima esecuzione")}</th>
              <th>{t("RemindList", "NextExecutionColumn", "Prossima esecuzione")}</th>
              <th>{t("RemindList", "TimeIntervalColumn", "Intervallo di tempo")}</th>
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
                <td title={t("TimePickerDialog", "Format", "gg.OO:mm")}>{timeIntervalToString(remind.timeInterval)}</td>
              </tr>
            ))}
            {reminds.length === 0 && (
              <tr>
                <td colSpan={7} className="empty-row">
                  {t("General", "NoRemindText", "Nessun promemoria")}
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
                {t("RemindList", "EditPopup", "Modifica")}
              </button>
              <button className="btn" onClick={() => setPreviewRemind(selected)}>
                {t("RemindList", "PreviewButton", "Anteprima")}
              </button>
              <button className="btn btn-danger" onClick={() => setDeleteTarget("selection")}>
                {t("RemindList", "DeletePopup", "Elimina")}
              </button>
            </div>
          </div>
          {selected.description && <MarkdownContent className="details-description" text={selected.description} />}
          <dl className="details-grid">
            <dt>{t("RemindList", "IsActiveColumn", "Attivo")}</dt>
            <dd>
              <span className={`badge ${selected.isActive ? "badge-on" : ""}`}>
                {selected.isActive ? t("General", "YesText", "Sì") : t("General", "NoText", "No")}
              </span>
            </dd>
            <dt>{t("RemindList", "IsTopLevelColumn", "Mostra in alto")}</dt>
            <dd>
              <span className={`badge ${selected.isTopLevel ? "badge-on" : ""}`}>
                {selected.isTopLevel ? t("General", "YesText", "Sì") : t("General", "NoText", "No")}
              </span>
            </dd>
            <dt>{t("RemindList", "LastExecutionColumn", "Ultima esecuzione")}</dt>
            <dd>{formatDate(selected.lastExecution ?? "")}</dd>
            <dt>{t("RemindList", "NextExecutionColumn", "Prossima esecuzione")}</dt>
            <dd>{formatDate(selected.nextExecution ?? "")}</dd>
            <dt>{t("RemindList", "TimeIntervalColumn", "Intervallo di tempo")}</dt>
            <dd>{timeIntervalToString(selected.timeInterval)}</dd>
            <dt>{t("RemindList", "CreationDateLabel", "Data creazione")}</dt>
            <dd>{formatDate(selected.creationDate ?? "")}</dd>
            <dt>{t("RemindList", "LastUpdateDateLabel", "Data ultima modifica")}</dt>
            <dd>{formatDate(selected.lastUpdateDate ?? "")}</dd>
            <dt>{t("RemindList", "CountDetail", "Conteggio")}</dt>
            <dd>{selected.remindCount}</dd>
            <dt>{t("ManageRemindDialog", "ExecutionMethodText", "Metodo di esecuzione")}</dt>
            <dd>
              {t(
                "ExecutionMethod",
                EXECUTION_METHOD_TRANSLATION[selected.executionMethod].key,
                EXECUTION_METHOD_TRANSLATION[selected.executionMethod].fallback,
              )}
            </dd>
            {selected.executionMethod === "CUSTOM_TIME_RANGE" && selected.timeRange && (
              <>
                <dt>{t("RemindList", "TimeFromLabel", "Ora inizio")}</dt>
                <dd>{selected.timeRange.start}</dd>
                <dt>{t("RemindList", "TimeToLabel", "Ora fine")}</dt>
                <dd>{selected.timeRange.end}</dd>
              </>
            )}
            {selected.executionMethod === "ONE_TIME_PER_DAY" && selected.timeRange && (
              <>
                <dt>{t("RemindList", "TimeFromLabel", "Ora inizio")}</dt>
                <dd>{selected.timeRange.start}</dd>
              </>
            )}
          </dl>
        </div>
      )}

      <footer className="app-footer">
        {t("General", "AppName", "Remind Me")} — {t("General", "Version", "Versione")} {__APP_VERSION__}
      </footer>

      {contextMenu && (
        <ul className="context-menu" style={{ top: contextMenu.y, left: contextMenu.x }} onClick={(e) => e.stopPropagation()}>
          <li
            onClick={() => {
              setEditingRemind(contextMenu.remind);
              setContextMenu(null);
            }}
          >
            {t("RemindList", "EditPopup", "Modifica")}
          </li>
          <li onClick={() => handleDuplicate(contextMenu.remind)}>{t("RemindList", "DuplicatePopup", "Duplica")}</li>
          <li
            onClick={() => {
              setRenamingRemind(contextMenu.remind);
              setContextMenu(null);
            }}
          >
            {t("RemindList", "RenamePopup", "Rinomina")}
          </li>
          <li className="context-menu-separator" />
          <li className="context-menu-submenu-label">{t("RemindList", "EnableDisablePopup", "Abilita / disabilita")}</li>
          <li className="context-menu-checkbox">
            <label>
              <input
                type="checkbox"
                checked={contextMenu.remind.isActive}
                onChange={(e) => handleToggleActive(contextMenu.remind, e.target.checked)}
              />
              {t("RemindList", "ActivePopup", "Attivo")}
            </label>
          </li>
          <li className="context-menu-checkbox">
            <label>
              <input
                type="checkbox"
                checked={contextMenu.remind.isTopLevel}
                onChange={(e) => handleToggleTopLevel(contextMenu.remind, e.target.checked)}
              />
              {t("RemindList", "TopLevelPopup", "Mostra in primo piano")}
            </label>
          </li>
          <li className="context-menu-separator" />
          <li className="context-menu-item-danger" onClick={() => handleDeleteNoConfirm(contextMenu.remind)}>
            {t("RemindList", "DeletePopup", "Elimina")}
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
          title={t("RemindList", "RenamePopup", "Rinomina promemoria")}
          label={t("Dialogs", "RemindNameInput", "Nome del promemoria")}
          initialValue={renamingRemind.name}
          onConfirm={handleRenameConfirm}
          onCancel={() => setRenamingRemind(null)}
        />
      )}

      {renameConflict && (
        <ConfirmDialog
          title={t("Dialogs", "ErrorGenericTitle", "Errore")}
          message={renameConflict}
          onConfirm={() => setRenameConflict(null)}
          onCancel={() => setRenameConflict(null)}
        />
      )}

      {deleteTarget === "selection" && selected && (
        <ConfirmDialog
          title={t("Dialogs", "ConfirmationDeletionTitle", "Conferma eliminazione")}
          message={t("Dialogs", "ConfirmationDeletionMessage", "Sei sicuro di voler eliminare le righe selezionate?")}
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
