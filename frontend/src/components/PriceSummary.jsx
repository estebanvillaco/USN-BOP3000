function PriceSummary({ totalCost, budget }) {
  const remaining = budget - totalCost;
  const overBudget = remaining < 0;

  return (
    <div className="card">
      <h3>Prisoversikt</h3>
<<<<<<< HEAD
      <p><strong>Estimert total kostnad:</strong> ~{totalCost} kr</p>
      <p style={{ fontSize: "0.85rem", color: "#666", marginTop: "0.4rem" }}>
        Prisene er estimater basert på hovedigrediensen per måltid. Faktisk kostnad kan variere.
=======
      <p><strong>Budsjett:</strong> {budget} kr</p>
      <p><strong>Total kostnad:</strong> {totalCost} kr</p>
      <p style={{ color: overBudget ? "#e74c3c" : "#27ae60" }}>
        <strong>{overBudget ? "Over budsjett:" : "Gjenstående:"}</strong>{" "}
        {Math.abs(remaining).toFixed(2)} kr
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
      </p>
    </div>
  );
}

export default PriceSummary;