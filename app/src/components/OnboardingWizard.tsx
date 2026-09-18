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
    titleFallback: "Benvenuto in RemindMe",
    bodyKey: "Step1Body",
    bodyFallback:
      "RemindMe ti aiuta a non dimenticare le cose che contano: crea promemoria ricorrenti con un'icona, un suono e una descrizione tutta tua, e resta organizzato senza sforzo.",
  },
  {
    icon: "➕",
    titleKey: "Step2Title",
    titleFallback: "Crea il tuo primo promemoria",
    bodyKey: "Step2Body",
    bodyFallback:
      "Premi **+** in alto a sinistra per aggiungerne uno: scegli nome, descrizione (anche in **Markdown**), icona e suono di notifica, e decidi se deve restare sempre visibile in primo piano.",
  },
  {
    icon: "⏱",
    titleKey: "Step3Title",
    titleFallback: "Quando si attiva",
    bodyKey: "Step3Body",
    bodyFallback:
      "Il **metodo di esecuzione** decide il comportamento: all'avvio del PC ogni tot tempo, in una fascia oraria personalizzata, oppure una volta al giorno a un orario preciso. L'intervallo di ripetizione si imposta col pulsante dedicato.",
  },
  {
    icon: "🗂",
    titleKey: "Step4Title",
    titleFallback: "Gestisci l'elenco",
    bodyKey: "Step4Body",
    bodyFallback:
      "Usa la barra di ricerca per filtrare i promemoria. Clic destro su una riga per modificare, duplicare, eliminare, rinominare o attivare/disattivare rapidamente un promemoria.",
  },
  {
    icon: "⚙",
    titleKey: "Step5Title",
    titleFallback: "Esporta e personalizza",
    bodyKey: "Step5Body",
    bodyFallback:
      "Esporta l'elenco in **CSV** o **PDF** dalla toolbar, oppure l'intero elenco in JSON dal menu **File**. In **Opzioni > Preferenze** puoi cambiare lingua e passare dal tema chiaro a quello scuro in qualsiasi momento.",
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
        <button className="btn onboarding-skip" onClick={onFinish}>
          {t("Onboarding", "SkipButton", "Salta")}
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
            {t("Onboarding", "BackButton", "Indietro")}
          </button>
          {isLast ? (
            <button className="btn btn-primary" onClick={onFinish}>
              {t("Onboarding", "StartButton", "Inizia")}
            </button>
          ) : (
            <button className="btn btn-primary" onClick={() => setIndex((i) => i + 1)}>
              {t("Onboarding", "NextButton", "Avanti")}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
