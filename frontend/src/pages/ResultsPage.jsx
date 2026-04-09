import { useLocation, useNavigate } from "react-router-dom";
import MealPlanCard from "../components/MealPlanCard";
import ShoppingList from "../components/ShoppingList";
import PriceSummary from "../components/PriceSummary";

function ResultsPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const mealPlan = location.state?.mealPlan;

  if (!mealPlan) {
    return (
      <div className="page">
        <h2>Ingen måltidsplan funnet</h2>
        <p>Gå tilbake og generer en plan først.</p>
        <button onClick={() => navigate("/planner")}>Tilbake til planlegger</button>
      </div>
    );
  }

  return (
    <div className="page">
      <h2>Resultater</h2>

      {mealPlan.meals.length === 0 ? (
        <p>Ingen måltider funnet innenfor budsjettet. Prøv å øke budsjettet eller endre preferansene.</p>
      ) : (
        <div className="meal-grid">
          {mealPlan.meals.map((meal) => (
            <MealPlanCard key={meal.id} meal={meal} />
          ))}
        </div>
      )}

      <ShoppingList items={mealPlan.shoppingList} />
      <PriceSummary totalCost={mealPlan.totalCost} />
    </div>
  );
}

export default ResultsPage;
