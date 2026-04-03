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
      <ul>
        {items.map((item, index) => (
          <li key={index}>{item}</li>
        ))}
      </ul>
    </div>
  );
}

export default ShoppingList;