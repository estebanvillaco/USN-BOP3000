function PriceSummary({ totalCost }) {
  return (
    <div className="card">
      <h3>Prisoversikt</h3>
      <p><strong>Estimert total kostnad:</strong> ~{totalCost} kr</p>
      <p style={{ fontSize: "0.85rem", color: "#666", marginTop: "0.4rem" }}>
        Prisene er estimater basert på hovedigrediensen per måltid. Faktisk kostnad kan variere.
      </p>
    </div>
  );
}

export default PriceSummary;