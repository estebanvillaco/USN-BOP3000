import { useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import MealPlanCard from "../components/MealPlanCard";
import ShoppingList from "../components/ShoppingList";
import PriceSummary from "../components/PriceSummary";
import RecipeModal from "../components/RecipeModal";

function ResultsPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const mealPlan = location.state?.mealPlan
    ?? JSON.parse(sessionStorage.getItem("lastMealPlan") ?? "null");
  const [selectedMeal, setSelectedMeal] = useState(null);
  const recipeCache = useRef({});

  if (!mealPlan) {
    return (
      <div className="page">
        <h2>Ingen måltidsplan funnet</h2>
        <p>Gå tilbake og generer en plan først.</p>
        <button onClick={() => navigate("/planner")}>Tilbake til planlegger</button>
      </div>
    );
  }

  const warnings = mealPlan.warnings ?? [];

  return (
    <div className="page">
      <h2>Resultater</h2>

      {warnings.length > 0 && (
        <div style={{
          background: "#fff3cd",
          border: "1px solid #ffc107",
          borderRadius: "8px",
          padding: "0.75rem 1rem",
          marginBottom: "1rem",
        }}>
          <strong>Noen ingredienser ble ikke funnet:</strong>
          <ul style={{ margin: "0.4rem 0 0 1rem", padding: 0 }}>
            {warnings.map((w, i) => <li key={i} style={{ fontSize: "0.9rem" }}>{w}</li>)}
          </ul>
        </div>
      )}

      {mealPlan.meals.length === 0 ? (
        <p>Ingen måltider funnet innenfor budsjettet. Prøv å øke budsjettet eller endre preferansene.</p>
      ) : (
        <>
          <p style={{ color: "#666", fontSize: "0.9rem", marginBottom: "0.5rem" }}>
            Klikk på et måltid for å se oppskriften og justere antall personer.
          </p>
          <div className="meal-grid">
            {mealPlan.meals.map((meal) => (
              <MealPlanCard
                key={meal.id}
                meal={meal}
                onClick={() => setSelectedMeal(meal)}
              />
            ))}
          </div>
        </>
      )}

      <ShoppingList items={mealPlan.shoppingList} />
      <PriceSummary totalCost={mealPlan.totalCost} />

      {selectedMeal && (
        <RecipeModal
          meal={selectedMeal}
          onClose={() => setSelectedMeal(null)}
          recipeCache={recipeCache}
        />
      )}
    </div>
  );
}

export default ResultsPage;
