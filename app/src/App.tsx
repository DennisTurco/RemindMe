import { useEffect, useState } from "react";
import { OnboardingWizard } from "./components/OnboardingWizard";
import { hasSeenOnboarding, markOnboardingSeen, onOnboardingTrigger } from "./lib/onboarding";
import { MainPage } from "./pages/MainPage";

export function App() {
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
