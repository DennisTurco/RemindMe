import { useEffect, useState } from "react";
import { HashRouter, Route, Routes } from "react-router-dom";
import { OnboardingWizard } from "./components/OnboardingWizard";
import { hasSeenOnboarding, markOnboardingSeen, onOnboardingTrigger } from "./lib/onboarding";
import { MainPage } from "./pages/MainPage";
import { ReminderPopupPage } from "./pages/ReminderPopupPage";

function MainAppShell() {
  const [showOnboarding, setShowOnboarding] = useState(() => !hasSeenOnboarding());

  useEffect(() => onOnboardingTrigger(() => setShowOnboarding(true)), []);

  function finishOnboarding() {
    markOnboardingSeen();
    setShowOnboarding(false);
  }

  return (
    <>
      <MainPage />
      {showOnboarding && <OnboardingWizard onFinish={finishOnboarding} />}
    </>
  );
}

export function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/popup/reminder" element={<ReminderPopupPage />} />
        <Route path="*" element={<MainAppShell />} />
      </Routes>
    </HashRouter>
  );
}
