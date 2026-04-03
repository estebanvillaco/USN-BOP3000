import mockMealPlan from "../data/MockMealPlan";
import MealPlanCard from "../components/MealPlanCard";
import ShoppingList from "../components/ShoppingList";
import PriceSummary from "../components/PriceSummary";

function ResultsPage() {
  return (
    <div className="page">
      <h2>Resultater</h2>

      <div className="meal-grid">
        {mockMealPlan.meals.map((meal) => (
          <MealPlanCard key={meal.id} meal={meal} />
        ))}
      </div>

      <ShoppingList items={mockMealPlan.shoppingList} />
      <PriceSummary totalCost={mockMealPlan.totalCost} />
    </div>
  );
}

export default ResultsPage;