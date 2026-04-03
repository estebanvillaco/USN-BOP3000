function MealPlanCard({ meal }) {
  return (
    <div className="card">
      <h3>{meal.day}</h3>
      <p><strong>Måltid:</strong> {meal.name}</p>
      <p><strong>Butikk:</strong> {meal.store}</p>
      <p><strong>Pris:</strong> {meal.price} kr</p>
      {meal.ingredients?.length > 0 && (
        <div style={{ marginTop: "0.5rem" }}>
          <strong>Ingredienser:</strong>
          <ul style={{ paddingLeft: "1.2rem", marginTop: "0.3rem" }}>
            {meal.ingredients.map((item, i) => (
              <li key={i}>
                {typeof item === "string"
                  ? item
                  : `${item.name} — ${item.price} kr (${item.store})`}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default MealPlanCard;