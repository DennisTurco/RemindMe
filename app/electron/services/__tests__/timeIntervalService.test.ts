import { describe, expect, it } from "vitest";
import {
  getNextExecutionBasedOnMethod,
  getNextExecutionByTimeInterval,
  getNextExecutionByTimeIntervalFromSpecificTime,
  isTimeRangeValid,
} from "../timeIntervalService";

describe("timeIntervalService", () => {
  it("getNextExecutionByTimeInterval adds days/hours/minutes to now", () => {
    const now = new Date(2024, 0, 1, 10, 0, 0);
    const result = getNextExecutionByTimeInterval({ days: 1, hours: 2, minutes: 30 }, now);
    expect(result).toEqual(new Date(2024, 0, 2, 12, 30, 0));
  });

  it("getNextExecutionByTimeInterval returns null without an interval", () => {
    expect(getNextExecutionByTimeInterval(null)).toBeNull();
  });

  it("getNextExecutionByTimeIntervalFromSpecificTime postpones by a day if already passed", () => {
    const now = new Date(2024, 0, 1, 23, 0, 0);
    const result = getNextExecutionByTimeIntervalFromSpecificTime({ days: 0, hours: 0, minutes: 0 }, "08:00:00", now);
    expect(result).toEqual(new Date(2024, 0, 2, 8, 0, 0));
  });

  it("getNextExecutionByTimeIntervalFromSpecificTime keeps today if still in the future", () => {
    const now = new Date(2024, 0, 1, 6, 0, 0);
    const result = getNextExecutionByTimeIntervalFromSpecificTime({ days: 0, hours: 0, minutes: 0 }, "08:00:00", now);
    expect(result).toEqual(new Date(2024, 0, 1, 8, 0, 0));
  });

  it("isTimeRangeValid requires start before end", () => {
    expect(isTimeRangeValid({ start: "08:00:00", end: "20:00:00" })).toBe(true);
    expect(isTimeRangeValid({ start: "20:00:00", end: "08:00:00" })).toBe(false);
    expect(isTimeRangeValid(null)).toBe(false);
  });

  it("getNextExecutionBasedOnMethod uses time-range start for CUSTOM_TIME_RANGE", () => {
    const now = new Date(2024, 0, 1, 6, 0, 0);
    const result = getNextExecutionBasedOnMethod(
      "CUSTOM_TIME_RANGE",
      { start: "08:00:00", end: "20:00:00" },
      { days: 0, hours: 0, minutes: 0 },
      now,
    );
    expect(result).toEqual(new Date(2024, 0, 1, 8, 0, 0));
  });

  it("getNextExecutionBasedOnMethod uses today at range start for ONE_TIME_PER_DAY", () => {
    const now = new Date(2024, 0, 1, 6, 0, 0);
    const result = getNextExecutionBasedOnMethod("ONE_TIME_PER_DAY", { start: "08:00:00", end: "20:00:00" }, null, now);
    expect(result).toEqual(new Date(2024, 0, 1, 8, 0, 0));
  });

  it("getNextExecutionBasedOnMethod falls back to plain interval for PC_STARTUP", () => {
    const now = new Date(2024, 0, 1, 6, 0, 0);
    const result = getNextExecutionBasedOnMethod("PC_STARTUP", null, { days: 0, hours: 1, minutes: 0 }, now);
    expect(result).toEqual(new Date(2024, 0, 1, 7, 0, 0));
  });
});
