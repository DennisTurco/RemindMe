import type { ExecutionMethod, TimeInterval, TimeRange } from "../types";
import { addDays, combineDateAndTime, isTimeOfDayBefore } from "./dateTime";

/** Mirrors remindme.Services.TimeIntervalService. */

export function isTimeRangeValid(range: TimeRange | null): boolean {
  return range !== null && isTimeOfDayBefore(range.start, range.end);
}

export function getNextExecutionByTimeInterval(interval: TimeInterval | null, now: Date = new Date()): Date | null {
  if (!interval) return null;
  const result = new Date(now);
  result.setDate(result.getDate() + interval.days);
  result.setHours(result.getHours() + interval.hours);
  result.setMinutes(result.getMinutes() + interval.minutes);
  return result;
}

export function getNextExecutionByTimeIntervalFromSpecificTime(
  interval: TimeInterval | null,
  timeFrom: string | null,
  now: Date = new Date(),
): Date | null {
  if (!interval || !timeFrom) return null;

  let baseTime = combineDateAndTime(now, timeFrom);
  baseTime.setDate(baseTime.getDate() + interval.days);
  baseTime.setHours(baseTime.getHours() + interval.hours);
  baseTime.setMinutes(baseTime.getMinutes() + interval.minutes);

  if (baseTime < now) {
    baseTime = addDays(baseTime, 1);
  }

  return baseTime;
}

export function getNextExecutionBasedOnMethod(
  method: ExecutionMethod,
  range: TimeRange | null,
  interval: TimeInterval | null,
  now: Date = new Date(),
): Date | null {
  if (method === "CUSTOM_TIME_RANGE" && isTimeRangeValid(range)) {
    return getNextExecutionByTimeIntervalFromSpecificTime(interval, range!.start, now);
  }
  if (method === "ONE_TIME_PER_DAY" && range) {
    return combineDateAndTime(now, range.start);
  }
  return getNextExecutionByTimeInterval(interval, now);
}
