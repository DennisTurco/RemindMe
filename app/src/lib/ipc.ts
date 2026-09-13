import type { Remind } from "./types";

export interface ExportResult {
  canceled: boolean;
  path?: string;
}

export interface RemindMeBridge {
  getAll(): Promise<Remind[]>;
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

declare global {
  interface Window {
    remindMe?: RemindMeBridge;
  }
}

/**
 * Falls back to an in-memory no-op bridge when running outside Electron
 * (e.g. `vite` alone, for quick UI-only preview in a plain browser tab).
 */
function createBrowserFallbackBridge(): RemindMeBridge {
  let reminds: Remind[] = [];
  return {
    getAll: async () => reminds,
    search: async (query) => reminds.filter((r) => r.name.toLowerCase().includes(query.toLowerCase())),
    create: async (remind) => {
      reminds = [...reminds, remind];
      return remind;
    },
    update: async (currentName, remind) => {
      reminds = reminds.map((r) => (r.name === currentName ? remind : r));
      return remind;
    },
    remove: async (name) => {
      reminds = reminds.filter((r) => r.name !== name);
    },
    duplicate: async () => null,
    rename: async () => {},
    setActive: async (name, isActive) => {
      reminds = reminds.map((r) => (r.name === name ? { ...r, isActive } : r));
    },
    setTopLevel: async (name, isTopLevel) => {
      reminds = reminds.map((r) => (r.name === name ? { ...r, isTopLevel } : r));
    },
    exportCsv: async () => ({ canceled: true }),
    exportPdf: async () => ({ canceled: true }),
    setNativeTheme: async () => {},
    setLanguage: async () => {},
    onNewReminderRequested: () => () => {},
    onOpenPreferencesRequested: () => () => {},
    onRemindersChanged: () => () => {},
  };
}

export const remindMe: RemindMeBridge = window.remindMe ?? createBrowserFallbackBridge();
