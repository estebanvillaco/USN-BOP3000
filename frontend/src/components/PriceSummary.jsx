function PriceSummary({ totalCost, budget }) {
  const remaining = budget - totalCost;
  const overBudget = remaining < 0;

  return (
    <div className="card">
      <h3>Prisoversikt</h3>
      <p><strong>Budsjett:</strong> {budget} kr</p>
      <p><strong>Total kostnad:</strong> {totalCost} kr</p>
      <p style={{ color: overBudget ? "#e74c3c" : "#27ae60" }}>
        <strong>{overBudget ? "Over budsjett:" : "Gjenstående:"}</strong>{" "}
        {Math.abs(remaining).toFixed(2)} kr
      </p>
    </div>
  );
}

export default PriceSummary;