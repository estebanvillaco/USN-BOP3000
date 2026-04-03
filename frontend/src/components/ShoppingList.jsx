function ShoppingList({ items }) {
  return (
    <div className="card">
      <h3>Handleliste</h3>
      <ul>
        {items.map((item, index) => (
          <li key={index}>{item}</li>
        ))}
      </ul>
    </div>
  );
}

export default ShoppingList;