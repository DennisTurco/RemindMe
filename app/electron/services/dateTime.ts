/**
 * Local (zone-less) date-time helpers mirroring java.time.LocalDateTime /
 * LocalTime semantics, so persisted strings stay compatible with the values
 * the Java app used to write (e.g. "2024-06-01T14:30:00", "08:00:00").
 */

function pad(n: number, width = 2): string {
  return String(n).padStart(width, "0");
}

export function formatLocalDateTime(date: Date): string {
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  );
}

/** Parses an ISO-like local date-time string (no timezone) as local time. */
export function parseLocalDateTime(value: string): Date {
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/.exec(value);
  if (!match) {
    throw new Error(`Invalid local date-time: ${value}`);
  }
  const [, y, mo, d, h, mi, s] = match;
  return new Date(Number(y), Number(mo) - 1, Number(d), Number(h), Number(mi), s ? Number(s) : 0);
}

export function formatLocalTime(date: Date): string {
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

/** Combines a calendar date from `date` with a "HH:mm[:ss]" time-of-day string. */
export function combineDateAndTime(date: Date, timeOfDay: string): Date {
  const match = /^(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?$/.exec(timeOfDay);
  if (!match) {
    throw new Error(`Invalid time-of-day: ${timeOfDay}`);
  }
  const [, h, m, s] = match;
  const result = new Date(date);
  result.setHours(Number(h), Number(m), s ? Number(s) : 0, 0);
  return result;
}

export function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

/** Lexicographic compare works because both strings are zero-padded HH:mm:ss. */
export function isTimeOfDayBefore(a: string, b: string): boolean {
  return a < b;
}

export function timeOfDayInRange(value: string, start: string, end: string): boolean {
  return value >= start && value <= end;
}
