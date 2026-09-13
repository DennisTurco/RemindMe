const STORAGE_KEY = "remindme-onboarding-seen";
const TRIGGER_EVENT = "remindme:show-onboarding";

export function hasSeenOnboarding(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY) === "true";
  } catch {
    return true;
  }
}

export function markOnboardingSeen(): void {
  try {
    localStorage.setItem(STORAGE_KEY, "true");
  } catch {
    // Ignore write failures (e.g. private browsing); the wizard just reappears next launch.
  }
}

/** Lets other UI (e.g. a "Show tutorial again" button in Preferences) reopen the wizard on demand. */
export function triggerOnboarding(): void {
  window.dispatchEvent(new Event(TRIGGER_EVENT));
}

export function onOnboardingTrigger(callback: () => void): () => void {
  window.addEventListener(TRIGGER_EVENT, callback);
  return () => window.removeEventListener(TRIGGER_EVENT, callback);
}
