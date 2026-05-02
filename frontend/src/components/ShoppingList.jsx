function ShoppingList({ items, meals }) {
  if (!items || items.length === 0) {
    return (
      <div className="card">
        <h3>Handleliste</h3>
        <p>Ingen varer å vise.</p>
      </div>
    );
  }

  // Group items by store
  const byStore = items.reduce((acc, item) => {
    const store = item.store || "Ukjent butikk";
    if (!acc[store]) acc[store] = [];
    acc[store].push(item);
    return acc;
  }, {});

  // Order stores by which appears first in the list (day order)
  const storeOrder = [];
  items.forEach((item) => {
    if (!storeOrder.includes(item.store)) storeOrder.push(item.store);
  });

  return (
    <div className="card">
      <h3>Handleliste</h3>
      <p style={{ color: "#555", marginBottom: "1rem" }}>
        {items.length} vare{items.length !== 1 ? "r" : ""} fra {storeOrder.length} butikk{storeOrder.length !== 1 ? "er" : ""}
      </p>

      {storeOrder.map((store) => (
        <div key={store} style={{ marginBottom: "1.5rem" }}>
          <h4 style={{
            borderBottom: "2px solid #e0e0e0",
            paddingBottom: "0.25rem",
            marginBottom: "0.75rem",
            color: "#333"
          }}>
            {store}
          </h4>
          <ul style={{ listStyle: "none", padding: 0 }}>
            {byStore[store].map((item, index) => (
              <li key={index} style={{ marginBottom: "0.75rem" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                  <div>
                    <span style={{ fontWeight: "bold" }}>{item.name}</span>
                    {" – "}
                    <span>{item.amount}</span>
                    {item.day && (
                      <span style={{
                        marginLeft: "0.5rem",
                        fontSize: "0.78em",
                        background: "#f0f0f0",
                        borderRadius: "4px",
                        padding: "1px 6px",
                        color: "#666"
                      }}>
                        {item.day}
                      </span>
                    )}
                    <br />
                    <span style={{ color: "#555", fontSize: "0.85em" }}>{item.productName}</span>
                  </div>
                  <span style={{ fontWeight: "bold", whiteSpace: "nowrap", marginLeft: "1rem" }}>
                    {item.price.toFixed(2)} kr
                  </span>
                </div>
              </li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  );
}

export default ShoppingList;
