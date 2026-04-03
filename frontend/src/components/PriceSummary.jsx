function PriceSummary({ totalCost }) {
  return (
    <div className="card">
      <h3>Prisoversikt</h3>
      <p><strong>Total kostnad:</strong> {totalCost} kr</p>
    </div>
  );
}

export default PriceSummary;