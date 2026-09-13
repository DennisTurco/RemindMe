// Kept in sync with electron/types.ts. Duplicated (not imported) so the
// renderer (Vite/bundler tsconfig) and the electron main process (CommonJS
// tsconfig) stay fully independent builds, matching the DailyPill layout.

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

export type ExecutionMethod = "PC_STARTUP" | "CUSTOM_TIME_RANGE" | "ONE_TIME_PER_DAY";

export interface TimeInterval {
  days: number;
  hours: number;
  minutes: number;
}

export interface TimeRange {
  start: string;
  end: string;
}

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
    isActive: true,
    isTopLevel: true,
    lastExecution: null,
    nextExecution: null,
    creationDate: null,
    lastUpdateDate: null,
    timeInterval: { days: 0, hours: 1, minutes: 0 },
    icon: "ALERT",
    sound: "NO_SOUND",
    executionMethod: "PC_STARTUP",
    timeRange: null,
    maxExecutionPerDay: 0,
  };
}
