import PreferenceForm from "../components/PreferenceForm";

function PlannerPage() {
  return (
    <div className="page">
      <h2>Måltidsplanlegger</h2>
      <p>Fyll inn preferanser og budsjett for å generere en enkel måltidsplan.</p>
      <PreferenceForm />
    </div>
  );
}

export default PlannerPage;