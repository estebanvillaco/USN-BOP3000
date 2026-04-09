function PriceSummary({ totalCost }) {
  return (
    <div className="card">
      <h3>Prisoversikt</h3>
      <p><strong>Total kostnad:</strong> {totalCost.toFixed(2)} kr</p>
      <p style={{ fontSize: "0.85rem", color: "#666", marginTop: "0.4rem" }}>
        Prisene er hentet direkte fra Kassalapp.
      </p>
    </div>
  );
}

export default PriceSummary;
