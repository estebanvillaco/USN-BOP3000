function MealPlanCard({ meal, onClick }) {
  return (
    <div
      className="card"
      onClick={onClick}
      style={{ cursor: "pointer", transition: "transform 0.1s, box-shadow 0.1s" }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = "translateY(-2px)";
        e.currentTarget.style.boxShadow = "0 4px 16px rgba(39,174,96,0.2)";
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = "";
        e.currentTarget.style.boxShadow = "";
      }}
    >
      <h3>{meal.day}</h3>
      <p><strong>Måltid:</strong> {meal.name}</p>
      <p><strong>Butikk:</strong> {meal.store}</p>
      <p><strong>Pris:</strong> {meal.price.toFixed(2)} kr</p>
      <p style={{ fontSize: "0.8rem", color: "#27ae60", marginTop: "0.5rem", marginBottom: 0 }}>
        Klikk for oppskrift →
      </p>
    </div>
  );
}

export default MealPlanCard;
