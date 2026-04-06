import { useLocation } from "react-router-dom";
import PreferenceForm from "../components/PreferenceForm";

const goalLabels = {
  healthy: "Sunn livsstil",
  weight_loss: "Gå ned i vekt",
  muscle_gain: "Bygge muskler"
};

function PlannerPage() {
  const location = useLocation();
  const goal = location.state?.goal ?? "healthy";

  return (
    <div className="page">
      <h2>Måltidsplanlegger</h2>
      <p>
        Mål: <strong>{goalLabels[goal] ?? goal}</strong> — Fyll inn preferanser og budsjett for å generere en måltidsplan.
      </p>
      <PreferenceForm goal={goal} />
    </div>
  );
}

export default PlannerPage;