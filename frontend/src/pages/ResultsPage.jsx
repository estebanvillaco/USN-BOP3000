<<<<<<< HEAD
import { useLocation } from "react-router-dom";
import mockMealPlan from "../data/MockMealPlan";
=======
import { useLocation, useNavigate } from "react-router-dom";
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
import MealPlanCard from "../components/MealPlanCard";
import ShoppingList from "../components/ShoppingList";
import PriceSummary from "../components/PriceSummary";

function ResultsPage() {
  const location = useLocation();
<<<<<<< HEAD
  const mealPlan = location.state?.mealPlan ?? mockMealPlan;
=======
  const navigate = useNavigate();
  const mealPlan = location.state?.mealPlan;

  if (!mealPlan) {
    return (
      <div className="page">
        <h2>Resultater</h2>
        <p>Ingen måltidsplan funnet. <button onClick={() => navigate("/planner")}>Gå tilbake</button></p>
      </div>
    );
  }
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2

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
<<<<<<< HEAD
      <PriceSummary totalCost={mealPlan.totalCost} />
=======
      <PriceSummary totalCost={mealPlan.totalCost} budget={mealPlan.budget} />
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
    </div>
  );
}

export default ResultsPage;
