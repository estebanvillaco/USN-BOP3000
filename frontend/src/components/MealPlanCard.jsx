function MealPlanCard({ meal }) {
  return (
    <div className="card">
      <h3>{meal.day}</h3>
      <p><strong>Måltid:</strong> {meal.name}</p>
      <p><strong>Butikk:</strong> {meal.store}</p>
      <p><strong>Estimert pris:</strong> ~{meal.price} kr</p>
    </div>
  );
}

export default MealPlanCard;
