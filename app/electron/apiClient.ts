import type { Remind } from "./types";

/**
 * Fetch wrapper for the Java backend (remindme.Api.ApiServer), consumed from
 * the Electron main process. Mirrors DailyPill's frontend/src/lib/api.ts
 * pattern, but lives main-process-side here since file dialogs (import/
 * export) must run there anyway.
 */
const BASE_URL = "http://localhost:8765";

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`RemindMe API ${path} failed (${res.status}): ${text}`);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  const contentType = res.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return (await res.json()) as T;
  }
  return (await res.text()) as unknown as T;
}

export const apiClient = {
  async health(): Promise<boolean> {
    try {
      const res = await fetch(`${BASE_URL}/health`);
      return res.ok;
    } catch {
      return false;
    }
  },

  getAll: () => request<Remind[]>("/reminders"),

  async getByName(name: string): Promise<Remind | null> {
    try {
      return await request<Remind>(`/reminders/${encodeURIComponent(name)}`);
    } catch {
      return null;
    }
  },

  search: (query: string) => request<Remind[]>(`/reminders/search?q=${encodeURIComponent(query)}`),

  /** Reminders due right now; the backend also marks them as shown as a side effect (see ApiServer /reminders/due). */
  getDue: () => request<Remind[]>("/reminders/due"),

  create: (remind: Remind) => request<Remind>("/reminders", { method: "POST", body: JSON.stringify(remind) }),

  update: (currentName: string, remind: Remind) =>
    request<Remind>(`/reminders/${encodeURIComponent(currentName)}`, { method: "PUT", body: JSON.stringify(remind) }),

  remove: (name: string) => request<void>(`/reminders/${encodeURIComponent(name)}`, { method: "DELETE" }),

  duplicate: (name: string) => request<Remind | null>(`/reminders/${encodeURIComponent(name)}/duplicate`, { method: "POST" }),

  rename: (currentName: string, newName: string) =>
    request<void>(`/reminders/${encodeURIComponent(currentName)}/rename`, {
      method: "POST",
      body: JSON.stringify({ newName }),
    }),

  setActive: (name: string, value: boolean) =>
    request<void>(`/reminders/${encodeURIComponent(name)}/active`, { method: "POST", body: JSON.stringify({ value }) }),

  setTopLevel: (name: string, value: boolean) =>
    request<void>(`/reminders/${encodeURIComponent(name)}/topLevel`, { method: "POST", body: JSON.stringify({ value }) }),

  exportCsv: () => request<string>("/export/csv"),

  async exportPdf(): Promise<Buffer> {
    const res = await fetch(`${BASE_URL}/export/pdf`);
    if (!res.ok) {
      throw new Error(`RemindMe API /export/pdf failed (${res.status})`);
    }
    return Buffer.from(await res.arrayBuffer());
  },

  exportJson: () => request<Remind[]>("/export/json"),

  importJson: (reminds: Remind[]) => request<void>("/import/json", { method: "POST", body: JSON.stringify(reminds) }),
};
