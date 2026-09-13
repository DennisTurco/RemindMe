export type IconName =
  | "ALERT"
  | "BOOK_CLOSED"
  | "BOOK"
  | "EYE_CLOSED"
  | "EYE"
  | "MAN_BEER"
  | "MAN_CALCULATOR"
  | "MAN_COMPUTER"
  | "MAN_JOGGING"
  | "MAN_SHOPPING"
  | "MAN_SLEEPING"
  | "MAN_WEARING_TIE"
  | "MAN_WITH_DIETARY"
  | "MAN_YOGA"
  | "MAN"
  | "MUSIC1"
  | "MUSIC2"
  | "PAUSE_CIRCLE"
  | "WARNING"
  | "WORK"
  | "MEME_BABY_YODA"
  | "MEME_DOGE"
  | "MEME_FACEPALM"
  | "MEME_HANDSOME_SQIDWARD"
  | "MEME_LEONARDO_DICAPRIO"
  | "MEME_POLITE_CAT"
  | "MEME_ROLL_SAFE"
  | "MEME_FINE_DOG"
  | "MEME_LOOK_MONKEY"
  | "MEME_OLD_MAN"
  | "MEME_WOMAN_YELLING"
  | "MEME_HOMER_SIMPSON";

export const DEFAULT_ICON: IconName = "ALERT";

export const ICON_PATHS: Record<IconName, string> = {
  ALERT: "res/img/remind/alert.svg",
  BOOK_CLOSED: "res/img/remind/book-closed.svg",
  BOOK: "res/img/remind/book.svg",
  EYE_CLOSED: "res/img/remind/eye-closed.svg",
  EYE: "res/img/remind/eye.svg",
  MAN_BEER: "res/img/remind/man-beer.svg",
  MAN_CALCULATOR: "res/img/remind/man-calculator.svg",
  MAN_COMPUTER: "res/img/remind/man-computer.svg",
  MAN_JOGGING: "res/img/remind/man-jogging.svg",
  MAN_SHOPPING: "res/img/remind/man-shopping.svg",
  MAN_SLEEPING: "res/img/remind/man-sleeping.svg",
  MAN_WEARING_TIE: "res/img/remind/man-wearing-tie.svg",
  MAN_WITH_DIETARY: "res/img/remind/man-with-dietary.svg",
  MAN_YOGA: "res/img/remind/man-yoga.svg",
  MAN: "res/img/remind/man.svg",
  MUSIC1: "res/img/remind/music1.svg",
  MUSIC2: "res/img/remind/music2.svg",
  PAUSE_CIRCLE: "res/img/remind/pause-circle.svg",
  WARNING: "res/img/remind/warning.svg",
  WORK: "res/img/remind/work.svg",
  MEME_BABY_YODA: "res/img/remind/meme_baby_yoda.svg",
  MEME_DOGE: "res/img/remind/meme_doge.svg",
  MEME_FACEPALM: "res/img/remind/meme_facepalm.svg",
  MEME_HANDSOME_SQIDWARD: "res/img/remind/meme_handsome_squidward.svg",
  MEME_LEONARDO_DICAPRIO: "res/img/remind/meme_leonardo_dicaprio_laughing.svg",
  MEME_POLITE_CAT: "res/img/remind/meme_polite_cat.svg",
  MEME_ROLL_SAFE: "res/img/remind/meme_roll_safe.svg",
  MEME_FINE_DOG: "res/img/remind/meme_this_is_fine_dog.svg",
  MEME_LOOK_MONKEY: "res/img/remind/meme_lookmonkey.svg",
  MEME_OLD_MAN: "res/img/remind/meme_old_man.svg",
  MEME_WOMAN_YELLING: "res/img/remind/meme_woman_yelling.svg",
  MEME_HOMER_SIMPSON: "res/img/remind/meme_homer_simpson.svg",
};

export type SoundName =
  | "NO_SOUND"
  | "SOUND1"
  | "SOUND2"
  | "SOUND3"
  | "SOUND4"
  | "SOUND5"
  | "SOUND8"
  | "SOUND9"
  | "SOUND11"
  | "SOUND12"
  | "MEME_UWU"
  | "MEME_BLUE_LOBSTER"
  | "MEME_FUS_RO_DAH"
  | "MEME_MANZ"
  | "MEME_METAL_PIPE"
  | "MEME_PERRO_SALCICCIA"
  | "MEME_SIUM"
  | "MEME_SPIN"
  | "MEME_TO_BE_CONTINUED";

export const DEFAULT_SOUND: SoundName = "NO_SOUND";

export const SOUND_PATHS: Record<SoundName, string> = {
  NO_SOUND: "",
  SOUND1: "res/sounds/sound1.wav",
  SOUND2: "res/sounds/sound2.wav",
  SOUND3: "res/sounds/sound3.wav",
  SOUND4: "res/sounds/sound4.wav",
  SOUND5: "res/sounds/sound5.wav",
  SOUND8: "res/sounds/sound8.wav",
  SOUND9: "res/sounds/sound9.wav",
  SOUND11: "res/sounds/sound11.wav",
  SOUND12: "res/sounds/sound12.wav",
  MEME_UWU: "res/sounds/meme_uwu.wav",
  MEME_BLUE_LOBSTER: "res/sounds/meme_blue_lobster.wav",
  MEME_FUS_RO_DAH: "res/sounds/meme_fus_ro_dah.wav",
  MEME_MANZ: "res/sounds/meme_maanz.wav",
  MEME_METAL_PIPE: "res/sounds/meme_metal_pipe.wav",
  MEME_PERRO_SALCICCIA: "res/sounds/meme_perro_salciccia.wav",
  MEME_SIUM: "res/sounds/meme_sium.wav",
  MEME_SPIN: "res/sounds/meme_spin.wav",
  MEME_TO_BE_CONTINUED: "res/sounds/meme_to_be_continued.wav",
};

export type ExecutionMethod = "PC_STARTUP" | "CUSTOM_TIME_RANGE" | "ONE_TIME_PER_DAY";

export const DEFAULT_EXECUTION_METHOD: ExecutionMethod = "PC_STARTUP";

/** Lower value = higher priority, mirrors Java ExecutionMethod.executionMethodPriority(). */
export const EXECUTION_METHOD_PRIORITY: Record<ExecutionMethod, number> = {
  ONE_TIME_PER_DAY: 1,
  CUSTOM_TIME_RANGE: 2,
  PC_STARTUP: 3,
};

/** Mirrors the Java record TimeInterval(days, hours, minutes), serialized by Gson as the string "d.hh:mm". */
export interface TimeInterval {
  days: number;
  hours: number;
  minutes: number;
}

export function timeIntervalToString(interval: TimeInterval): string {
  return `${interval.days}.${interval.hours}:${interval.minutes}`;
}

export function timeIntervalFromString(value: string): TimeInterval {
  const match = /^(\d+)\.(\d{1,2}):(\d{1,2})$/.exec(value);
  if (!match) {
    throw new Error(`Invalid time interval format: ${value}`);
  }
  const [, days, hours, minutes] = match;
  return { days: Number(days), hours: Number(hours), minutes: Number(minutes) };
}

/**
 * Mirrors the Java record TimeRange(start, end), where start/end are LocalTime
 * serialized by Gson (via LocalTimeAdapter) as "HH:mm:ss" strings.
 */
export interface TimeRange {
  start: string;
  end: string;
}

/**
 * Mirrors remindme.Entities.Remind. Field names and types match Gson's default
 * serialization exactly, so existing remind_list*.json files load unchanged.
 * Dates are ISO-8601 local date-time strings (java.time.LocalDateTime#toString()).
 */
export interface Remind {
  name: string;
  description: string;
  remindCount: number;
  isActive: boolean;
  isTopLevel: boolean;
  lastExecution: string | null;
  nextExecution: string | null;
  creationDate: string | null;
  lastUpdateDate: string | null;
  timeInterval: TimeInterval | null;
  icon: IconName;
  sound: SoundName;
  executionMethod: ExecutionMethod;
  timeRange: TimeRange | null;
  maxExecutionPerDay: number;
}

export function createDefaultRemind(): Remind {
  return {
    name: "",
    description: "",
    remindCount: 0,
    isActive: false,
    isTopLevel: false,
    lastExecution: null,
    nextExecution: null,
    creationDate: null,
    lastUpdateDate: null,
    timeInterval: null,
    icon: DEFAULT_ICON,
    sound: DEFAULT_SOUND,
    executionMethod: DEFAULT_EXECUTION_METHOD,
    timeRange: null,
    maxExecutionPerDay: 0,
  };
}

export type LanguageCode = "ITA" | "ENG" | "DEU" | "ESP" | "FRA";

export const LANGUAGE_FILE_NAMES: Record<LanguageCode, string> = {
  ITA: "ita.json",
  ENG: "eng.json",
  DEU: "deu.json",
  ESP: "esp.json",
  FRA: "fra.json",
};

export interface RemindListPath {
  directory: string;
  file: string;
}

export type ThemeCode =
  | "INTELLIJ"
  | "DRACULA"
  | "CARBON"
  | "ARC_ORAGE"
  | "ARC_DARK_ORANGE"
  | "CYAN_LIGHT"
  | "NORD"
  | "HIGH_CONTRAST"
  | "SOLARIZED_DARK"
  | "SOLARIZED_LIGHT";

/** Human-readable names as persisted by Java's ThemesEnum#getThemeName(). */
export const THEME_NAMES: Record<ThemeCode, string> = {
  INTELLIJ: "Light",
  DRACULA: "Dark",
  CARBON: "Carbon",
  ARC_ORAGE: "Arc - Orange",
  ARC_DARK_ORANGE: "Arc Dark - Orange",
  CYAN_LIGHT: "Cyan light",
  NORD: "Nord",
  HIGH_CONTRAST: "High contrast",
  SOLARIZED_DARK: "Solarized dark",
  SOLARIZED_LIGHT: "Solarized light",
};

/**
 * Mirrors res/config/preferences.json. Language/Theme are persisted as the
 * human-readable strings Java's enums expose (getFileName()/getThemeName()),
 * not the enum constant names, so existing preferences.json files stay valid.
 */
export interface PreferencesFile {
  Language: string | null;
  Theme: string | null;
  RemindList: { Directory: string; File: string } | null;
}
