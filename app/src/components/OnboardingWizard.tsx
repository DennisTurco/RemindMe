import { useState } from "react";
import type { ReactNode } from "react";

interface Step {
  icon: string;
  title: string;
  body: ReactNode;
}

const STEPS: Step[] = [
  {
    icon: "🔔",
    title: "Benvenuto in RemindMe",
    body: (
      <>
        RemindMe ti aiuta a non dimenticare le cose che contano: crea promemoria ricorrenti con
        un'icona, un suono e una descrizione tutta tua, e resta organizzato senza sforzo.
      </>
    ),
  },
  {
    icon: "➕",
    title: "Crea il tuo primo promemoria",
    body: (
      <>
        Premi <strong>+</strong> in alto a sinistra per aggiungerne uno: scegli nome, descrizione
        (anche in <strong>Markdown</strong>), icona e suono di notifica, e decidi se deve restare
        sempre visibile in primo piano.
      </>
    ),
  },
  {
    icon: "⏱",
    title: "Quando si attiva",
    body: (
      <>
        Il <strong>metodo di esecuzione</strong> decide il comportamento: all'avvio del PC ogni tot
        tempo, in una fascia oraria personalizzata, oppure una volta al giorno a un orario preciso.
        L'intervallo di ripetizione si imposta col pulsante dedicato.
      </>
    ),
  },
  {
    icon: "🗂",
    title: "Gestisci l'elenco",
    body: (
      <>
        Usa la barra di ricerca per filtrare i promemoria. Clic destro su una riga per modificare,
        duplicare, eliminare, rinominare o attivare/disattivare rapidamente un promemoria.
      </>
    ),
  },
  {
    icon: "⚙",
    title: "Esporta e personalizza",
    body: (
      <>
        Esporta l'elenco in <strong>CSV</strong> o <strong>PDF</strong> dalla toolbar, oppure l'intero
        elenco in JSON dal menu <strong>File</strong>. In <strong>Opzioni &gt; Preferenze</strong> puoi
        cambiare lingua e passare dal tema chiaro a quello scuro in qualsiasi momento.
      </>
    ),
  },
];

export function OnboardingWizard({ onFinish }: { onFinish: () => void }) {
  const [index, setIndex] = useState(0);
  const step = STEPS[index];
  const isFirst = index === 0;
  const isLast = index === STEPS.length - 1;

  return (
    <div className="modal-overlay" onClick={onFinish}>
      <div className="modal onboarding-panel" onClick={(e) => e.stopPropagation()}>
        <button className="btn onboarding-skip" onClick={onFinish}>
          Salta
        </button>

        <div className="onboarding-icon">{step.icon}</div>
        <h2 className="onboarding-title">{step.title}</h2>
        <p className="onboarding-body">{step.body}</p>

        <div className="onboarding-dots">
          {STEPS.map((_, i) => (
            <span key={i} className={`onboarding-dot ${i === index ? "active" : ""}`} />
          ))}
        </div>

        <div className="modal-actions onboarding-actions">
          <button className="btn" onClick={() => setIndex((i) => i - 1)} style={{ visibility: isFirst ? "hidden" : "visible" }}>
            Indietro
          </button>
          {isLast ? (
            <button className="btn btn-primary" onClick={onFinish}>
              Inizia
            </button>
          ) : (
            <button className="btn btn-primary" onClick={() => setIndex((i) => i + 1)}>
              Avanti
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
