import type { ExecutionMethod, IconName, SoundName } from "./types";

export interface CatalogOption<T extends string> {
  value: T;
  label: string;
  path: string;
}

/** Mirrors remindme.Enums.IconsEnum (name, path), in the same declaration order. */
export const ICON_OPTIONS: CatalogOption<IconName>[] = [
  { value: "ALERT", label: "Alert", path: "/img/remind/alert.svg" },
  { value: "BOOK_CLOSED", label: "Book closed", path: "/img/remind/book-closed.svg" },
  { value: "BOOK", label: "Book", path: "/img/remind/book.svg" },
  { value: "EYE_CLOSED", label: "Eye closed", path: "/img/remind/eye-closed.svg" },
  { value: "EYE", label: "Eye", path: "/img/remind/eye.svg" },
  { value: "MAN_BEER", label: "Man with a beer", path: "/img/remind/man-beer.svg" },
  { value: "MAN_CALCULATOR", label: "Man with a calculator", path: "/img/remind/man-calculator.svg" },
  { value: "MAN_COMPUTER", label: "Man with a computer", path: "/img/remind/man-computer.svg" },
  { value: "MAN_JOGGING", label: "Man doing jogging", path: "/img/remind/man-jogging.svg" },
  { value: "MAN_SHOPPING", label: "Man doing shopping", path: "/img/remind/man-shopping.svg" },
  { value: "MAN_SLEEPING", label: "Man sleeping", path: "/img/remind/man-sleeping.svg" },
  { value: "MAN_WEARING_TIE", label: "Man wearing a tie", path: "/img/remind/man-wearing-tie.svg" },
  { value: "MAN_WITH_DIETARY", label: "Man with diet", path: "/img/remind/man-with-dietary.svg" },
  { value: "MAN_YOGA", label: "Man doing yoga", path: "/img/remind/man-yoga.svg" },
  { value: "MAN", label: "Man", path: "/img/remind/man.svg" },
  { value: "MUSIC1", label: "Music 1", path: "/img/remind/music1.svg" },
  { value: "MUSIC2", label: "Music 2", path: "/img/remind/music2.svg" },
  { value: "PAUSE_CIRCLE", label: "Pause circle", path: "/img/remind/pause-circle.svg" },
  { value: "WARNING", label: "Warning", path: "/img/remind/warning.svg" },
  { value: "WORK", label: "Work", path: "/img/remind/work.svg" },
  { value: "MEME_BABY_YODA", label: "Meme - Baby Yoda", path: "/img/remind/meme_baby_yoda.svg" },
  { value: "MEME_DOGE", label: "Meme - Doge", path: "/img/remind/meme_doge.svg" },
  { value: "MEME_FACEPALM", label: "Meme - Facepalm", path: "/img/remind/meme_facepalm.svg" },
  {
    value: "MEME_HANDSOME_SQIDWARD",
    label: "Meme - Handsome Squidward",
    path: "/img/remind/meme_handsome_squidward.svg",
  },
  {
    value: "MEME_LEONARDO_DICAPRIO",
    label: "Meme - Leonardo Dicaprio",
    path: "/img/remind/meme_leonardo_dicaprio_laughing.svg",
  },
  { value: "MEME_POLITE_CAT", label: "Meme - Polite Cat", path: "/img/remind/meme_polite_cat.svg" },
  { value: "MEME_ROLL_SAFE", label: "Meme - Roll Safe", path: "/img/remind/meme_roll_safe.svg" },
  { value: "MEME_FINE_DOG", label: "Meme - Fine Dog", path: "/img/remind/meme_this_is_fine_dog.svg" },
  { value: "MEME_LOOK_MONKEY", label: "Meme - Look Monkey", path: "/img/remind/meme_lookmonkey.svg" },
  { value: "MEME_OLD_MAN", label: "Meme - Old Man", path: "/img/remind/meme_old_man.svg" },
  { value: "MEME_WOMAN_YELLING", label: "Meme - Woman Yelling", path: "/img/remind/meme_woman_yelling.svg" },
  { value: "MEME_HOMER_SIMPSON", label: "Meme - Homer Simpson", path: "/img/remind/meme_homer_simpson.svg" },
];

/** Mirrors remindme.Enums.SoundsEnum. Sound6/7/10 are intentionally absent, same as the Java enum. */
export const SOUND_OPTIONS: CatalogOption<SoundName>[] = [
  { value: "NO_SOUND", label: "No Sound", path: "" },
  { value: "SOUND1", label: "Sound 1", path: "/sounds/sound1.wav" },
  { value: "SOUND2", label: "Sound 2", path: "/sounds/sound2.wav" },
  { value: "SOUND3", label: "Sound 3", path: "/sounds/sound3.wav" },
  { value: "SOUND4", label: "Sound 4", path: "/sounds/sound4.wav" },
  { value: "SOUND5", label: "Sound 5", path: "/sounds/sound5.wav" },
  { value: "SOUND8", label: "Sound 8", path: "/sounds/sound8.wav" },
  { value: "SOUND9", label: "Sound 9", path: "/sounds/sound9.wav" },
  { value: "SOUND11", label: "Sound 11", path: "/sounds/sound11.wav" },
  { value: "SOUND12", label: "Sound 12", path: "/sounds/sound12.wav" },
  { value: "MEME_UWU", label: "Meme - Uwu", path: "/sounds/meme_uwu.wav" },
  { value: "MEME_BLUE_LOBSTER", label: "Meme - Blue Lobster", path: "/sounds/meme_blue_lobster.wav" },
  { value: "MEME_FUS_RO_DAH", label: "Meme - Fus Ro Dah", path: "/sounds/meme_fus_ro_dah.wav" },
  { value: "MEME_MANZ", label: "Meme - Manz", path: "/sounds/meme_maanz.wav" },
  { value: "MEME_METAL_PIPE", label: "Meme - Metal Pipe", path: "/sounds/meme_metal_pipe.wav" },
  { value: "MEME_PERRO_SALCICCIA", label: "Meme - Perro Salciccia", path: "/sounds/meme_perro_salciccia.wav" },
  { value: "MEME_SIUM", label: "Meme - Sium", path: "/sounds/meme_sium.wav" },
  { value: "MEME_SPIN", label: "Meme - Spin", path: "/sounds/meme_spin.wav" },
  { value: "MEME_TO_BE_CONTINUED", label: "Meme - To Be Continued", path: "/sounds/meme_to_be_continued.wav" },
];

export const EXECUTION_METHOD_OPTIONS: { value: ExecutionMethod; label: string }[] = [
  { value: "PC_STARTUP", label: "Pc Startup" },
  { value: "CUSTOM_TIME_RANGE", label: "Custom Time Range" },
  { value: "ONE_TIME_PER_DAY", label: "One Time Per Day" },
];

/** Translation key (under the "ExecutionMethod" category) + Italian fallback for each execution method. */
export const EXECUTION_METHOD_TRANSLATION: Record<ExecutionMethod, { key: string; fallback: string }> = {
  PC_STARTUP: { key: "PcStartup", fallback: "Avvio PC" },
  CUSTOM_TIME_RANGE: { key: "CustomTimeRange", fallback: "Intervallo di tempo personalizzato" },
  ONE_TIME_PER_DAY: { key: "OneTimePerDay", fallback: "Una volta al giorno" },
};

const ICON_PATH_BY_NAME = new Map(ICON_OPTIONS.map((o) => [o.value, o.path]));
const SOUND_PATH_BY_NAME = new Map(SOUND_OPTIONS.map((o) => [o.value, o.path]));

export function iconPath(icon: IconName): string {
  return ICON_PATH_BY_NAME.get(icon) ?? ICON_PATH_BY_NAME.get("ALERT")!;
}

export function soundPath(sound: SoundName): string {
  return SOUND_PATH_BY_NAME.get(sound) ?? "";
}

export function timeIntervalToString(interval: { days: number; hours: number; minutes: number } | null): string {
  if (!interval) return "-";
  return `${interval.days}.${interval.hours}:${interval.minutes}`;
}
