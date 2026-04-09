function ShoppingList({ items }) {
  if (!items || items.length === 0) {
    return (
      <div className="card">
        <h3>Handleliste</h3>
        <p>Ingen varer å vise.</p>
      </div>
    );
  }
  return (
    <div className="card">
      <h3>Handleliste</h3>
      <p style={{ color: "#555", marginBottom: "0.5rem" }}>
        {items.length} vare{items.length !== 1 ? "r" : ""}
      </p>
      <ul style={{ listStyle: "none", padding: 0 }}>
        {items.map((item, index) => (
          <li key={index} style={{ marginBottom: "0.75rem" }}>
            <span style={{ fontWeight: "bold" }}>{item.name}</span>
            {" – "}
            {item.amount}
            {" – "}
            {item.price.toFixed(2)} kr
            <span style={{ color: "#888", fontSize: "0.85em" }}> ({item.store})</span>
            <br />
            <span style={{ color: "#555", fontSize: "0.85em" }}>{item.productName}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default ShoppingList;
