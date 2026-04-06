import { useNavigate } from "react-router-dom";

const goals = [
  {
    id: "healthy",
    label: "Sunn livsstil",
    description: "Balanserte måltider med varierte råvarer for generell velvære.",
    icon: "🥗"
  },
  {
    id: "weight_loss",
    label: "Gå ned i vekt",
    description: "Lettere måltider med fokus på grønnsaker, suppe og magert protein.",
    icon: "⚖️"
  },
  {
    id: "muscle_gain",
    label: "Bygge muskler",
    description: "Proteinrike måltider med kylling, egg, fisk og kjøtt.",
    icon: "💪"
  }
];

function GoalPage() {
  const navigate = useNavigate();

  function handleSelect(goalId) {
    navigate("/planner", { state: { goal: goalId } });
  }

  return (
    <div className="page">
      <h2>Hva er målet ditt?</h2>
      <p>Velg et mål slik at vi kan tilpasse måltidsplanen din.</p>
      <div className="goal-grid">
        {goals.map((goal) => (
          <button
            key={goal.id}
            className="goal-card"
            onClick={() => handleSelect(goal.id)}
          >
            <span className="goal-icon">{goal.icon}</span>
            <h3>{goal.label}</h3>
            <p>{goal.description}</p>
          </button>
        ))}
      </div>
    </div>
  );
}

export default GoalPage;
