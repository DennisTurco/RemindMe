import { contextBridge, ipcRenderer } from "electron";
import type { Remind } from "./types";

export interface ExportResult {
  canceled: boolean;
  path?: string;
}

export interface RemindMeBridge {
  getAll(): Promise<Remind[]>;
  getByName(name: string): Promise<Remind | null>;
  search(query: string): Promise<Remind[]>;
  create(remind: Remind): Promise<Remind>;
  update(currentName: string, remind: Remind): Promise<Remind>;
  remove(name: string): Promise<void>;
  duplicate(name: string): Promise<Remind | null>;
  rename(currentName: string, newName: string): Promise<void>;
  setActive(name: string, isActive: boolean): Promise<void>;
  setTopLevel(name: string, isTopLevel: boolean): Promise<void>;
  exportCsv(): Promise<ExportResult>;
  exportPdf(): Promise<ExportResult>;
  setNativeTheme(mode: "light" | "dark"): Promise<void>;
  setLanguage(language: string): Promise<void>;
  onNewReminderRequested(callback: () => void): () => void;
  onOpenPreferencesRequested(callback: () => void): () => void;
  onRemindersChanged(callback: () => void): () => void;
}

function subscribe(channel: string, callback: () => void): () => void {
  const listener = () => callback();
  ipcRenderer.on(channel, listener);
  return () => ipcRenderer.removeListener(channel, listener);
}

const bridge: RemindMeBridge = {
  getAll: () => ipcRenderer.invoke("reminders:getAll"),
  getByName: (name) => ipcRenderer.invoke("reminders:getByName", name),
  search: (query) => ipcRenderer.invoke("reminders:search", query),
  create: (remind) => ipcRenderer.invoke("reminders:create", remind),
  update: (currentName, remind) => ipcRenderer.invoke("reminders:update", currentName, remind),
  remove: (name) => ipcRenderer.invoke("reminders:remove", name),
  duplicate: (name) => ipcRenderer.invoke("reminders:duplicate", name),
  rename: (currentName, newName) => ipcRenderer.invoke("reminders:rename", currentName, newName),
  setActive: (name, isActive) => ipcRenderer.invoke("reminders:setActive", name, isActive),
  setTopLevel: (name, isTopLevel) => ipcRenderer.invoke("reminders:setTopLevel", name, isTopLevel),
  exportCsv: () => ipcRenderer.invoke("reminders:exportCsv"),
  exportPdf: () => ipcRenderer.invoke("reminders:exportPdf"),
  setNativeTheme: (mode) => ipcRenderer.invoke("app:setThemeSource", mode),
  setLanguage: (language) => ipcRenderer.invoke("app:setLanguage", language),
  onNewReminderRequested: (callback) => subscribe("menu:new-reminder", callback),
  onOpenPreferencesRequested: (callback) => subscribe("menu:open-preferences", callback),
  onRemindersChanged: (callback) => subscribe("reminders:changed", callback),
};

contextBridge.exposeInMainWorld("remindMe", bridge);
