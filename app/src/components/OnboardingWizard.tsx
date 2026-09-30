import { useState } from "react";
import { useI18n } from "../lib/i18n";
import { MarkdownContent } from "./MarkdownContent";

interface Step {
  icon: string;
  titleKey: string;
  titleFallback: string;
  bodyKey: string;
  bodyFallback: string;
}

const STEPS: Step[] = [
  {
    icon: "🔔",
    titleKey: "Step1Title",
    titleFallback: "Welcome to RemindMe",
    bodyKey: "Step1Body",
    bodyFallback:
      "RemindMe helps you never forget what matters: create recurring reminders with your own icon, sound and description, and stay organized effortlessly.",
  },
  {
    icon: "➕",
    titleKey: "Step2Title",
    titleFallback: "Create your first reminder",
    bodyKey: "Step2Body",
    bodyFallback:
      "Press **+** in the top left to add one: choose a name, description (also in **Markdown**), an icon and a notification sound, and decide whether it should always stay on top.",
  },
  {
    icon: "⏱",
    titleKey: "Step3Title",
    titleFallback: "When it triggers",
    bodyKey: "Step3Body",
    bodyFallback:
      "The **execution method** decides the behavior: on PC startup every so often, within a custom time range, or once a day at a specific time. Set the repeat interval with the dedicated button.",
  },
  {
    icon: "🗂",
    titleKey: "Step4Title",
    titleFallback: "Manage the list",
    bodyKey: "Step4Body",
    bodyFallback:
      "Use the search bar to filter reminders. Right-click a row to quickly edit, duplicate, delete, rename or enable/disable a reminder.",
  },
  {
    icon: "⚙",
    titleKey: "Step5Title",
    titleFallback: "Export and customize",
    bodyKey: "Step5Body",
    bodyFallback:
      "Export the list as **CSV** or **PDF** from the toolbar, or the whole list as JSON from the **File** menu. In **Options > Preferences** you can change the language and switch between light and dark theme at any time.",
  },
];

export function OnboardingWizard({ onFinish }: { onFinish: () => void }) {
  const { t } = useI18n();
  const [index, setIndex] = useState(0);
  const step = STEPS[index];
  const isFirst = index === 0;
  const isLast = index === STEPS.length - 1;

  return (
    <div className="modal-overlay" onClick={onFinish}>
      <div className="modal onboarding-panel" onClick={(e) => e.stopPropagation()}>
        <button className="btn btn-ghost onboarding-skip" onClick={onFinish}>
          {t("Onboarding", "SkipButton", "Skip")}
        </button>

        <div className="onboarding-icon">{step.icon}</div>
        <h2 className="onboarding-title">{t("Onboarding", step.titleKey, step.titleFallback)}</h2>
        <MarkdownContent className="onboarding-body" text={t("Onboarding", step.bodyKey, step.bodyFallback)} />

        <div className="onboarding-dots">
          {STEPS.map((_, i) => (
            <span key={i} className={`onboarding-dot ${i === index ? "active" : ""}`} />
          ))}
        </div>

        <div className="modal-actions onboarding-actions">
          <button className="btn" onClick={() => setIndex((i) => i - 1)} style={{ visibility: isFirst ? "hidden" : "visible" }}>
            {t("Onboarding", "BackButton", "Back")}
          </button>
          {isLast ? (
            <button className="btn btn-primary" onClick={onFinish}>
              {t("Onboarding", "StartButton", "Start")}
            </button>
          ) : (
            <button className="btn btn-primary" onClick={() => setIndex((i) => i + 1)}>
              {t("Onboarding", "NextButton", "Next")}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
