import { useEffect, useState } from "react";

function scaleAmount(amount, scale) {
  if (!amount) return amount;
  const match = amount.match(/^(\d+(?:\/\d+)?(?:\.\d+)?)\s*(.*)$/);
  if (!match) return amount;
  const [, numPart, unit] = match;
  const num = numPart.includes("/")
    ? parseFloat(numPart.split("/")[0]) / parseFloat(numPart.split("/")[1])
    : parseFloat(numPart);
  const scaled = num * scale;
  const formatted =
    scaled % 1 === 0 ? scaled : parseFloat(scaled.toFixed(1));
  return unit ? `${formatted} ${unit}` : `${formatted}`;
}

function RecipeModal({ meal, onClose, recipeCache }) {
  const [recipe, setRecipe] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [servings, setServings] = useState(4);

  useEffect(() => {
    if (!meal) return;
    const cached = recipeCache?.current?.[meal.id];
    if (cached) {
      setRecipe(cached);
      setServings(meal.requestedServings ?? cached.defaultServings);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    fetch(`http://localhost:8080/api/meal/${meal.id}/recipe`)
      .then((res) => {
        if (!res.ok) throw new Error("Kunne ikke hente oppskrift");
        return res.json();
      })
      .then((data) => {
        if (recipeCache) recipeCache.current[meal.id] = data;
        setRecipe(data);
        setServings(meal.requestedServings ?? data.defaultServings);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, [meal]);

  if (!meal) return null;

  const scale = recipe ? servings / recipe.defaultServings : 1;
  const scaledPrice = meal.price * scale;

  return (
    <div style={styles.overlay} onClick={onClose}>
      <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
        <button style={styles.closeBtn} onClick={onClose}>✕</button>

        <h2 style={styles.title}>{meal.name}</h2>
        <p style={styles.dayBadge}>{meal.day}</p>

        {loading && <p style={styles.info}>Laster oppskrift…</p>}
        {error && <p style={styles.errorText}>{error}</p>}

        {recipe && (
          <>
            <div style={styles.servingsRow}>
              <label style={styles.servingsLabel}>Antall personer:</label>
              <input
                type="number"
                min={1}
                max={20}
                value={servings}
                onChange={(e) =>
                  setServings(Math.max(1, parseInt(e.target.value) || 1))
                }
                style={styles.servingsInput}
              />
            </div>

            {servings !== meal.requestedServings && (
              <p style={styles.servingsNote}>
                Handlelisten er beregnet for {meal.requestedServings} porsjon{meal.requestedServings !== 1 ? "er" : ""}.
              </p>
            )}

            <p style={styles.priceEstimate}>
              Estimert pris for {servings} person{servings !== 1 ? "er" : ""}:{" "}
              <strong>{scaledPrice.toFixed(2)} kr</strong>
            </p>

            <section style={styles.section}>
              <h3 style={styles.sectionTitle}>Ingredienser</h3>
              <ul style={styles.ingredientList}>
                {recipe.ingredients.map((ing, i) => (
                  <li key={i} style={styles.ingredientItem}>
                    <span style={styles.ingName}>{ing.name}</span>
                    <span style={styles.ingAmount}>
                      {scaleAmount(ing.amount, scale)}
                    </span>
                  </li>
                ))}
              </ul>
            </section>

            <section style={styles.section}>
              <h3 style={styles.sectionTitle}>Fremgangsmåte</h3>
              <ol style={styles.stepList}>
                {recipe.steps.map((step) => (
                  <li key={step.stepNumber} style={styles.stepItem}>
                    {step.instruction}
                  </li>
                ))}
              </ol>
            </section>
          </>
        )}
      </div>
    </div>
  );
}

const styles = {
  overlay: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.5)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 1000,
    padding: "1rem",
  },
  modal: {
    background: "white",
    borderRadius: "12px",
    padding: "2rem",
    maxWidth: "600px",
    width: "100%",
    maxHeight: "90vh",
    overflowY: "auto",
    position: "relative",
    boxShadow: "0 8px 32px rgba(0,0,0,0.2)",
  },
  closeBtn: {
    position: "absolute",
    top: "1rem",
    right: "1rem",
    background: "#eee",
    color: "#333",
    border: "none",
    borderRadius: "50%",
    width: "32px",
    height: "32px",
    cursor: "pointer",
    padding: 0,
    fontSize: "1rem",
    lineHeight: "32px",
    textAlign: "center",
  },
  title: {
    fontSize: "1.4rem",
    color: "#2c3e50",
    marginBottom: "0.25rem",
    paddingRight: "2rem",
  },
  dayBadge: {
    display: "inline-block",
    background: "#27ae60",
    color: "white",
    borderRadius: "12px",
    padding: "2px 10px",
    fontSize: "0.8rem",
    marginBottom: "1.25rem",
  },
  servingsRow: {
    display: "flex",
    alignItems: "center",
    gap: "0.75rem",
    marginBottom: "0.75rem",
  },
  servingsLabel: {
    fontWeight: "600",
    color: "#2c3e50",
  },
  servingsInput: {
    width: "70px",
    padding: "0.4rem 0.6rem",
    border: "1px solid #ccc",
    borderRadius: "6px",
    fontSize: "1rem",
  },
  servingsNote: {
    color: "#e67e22",
    fontSize: "0.85rem",
    marginBottom: "0.5rem",
  },
  priceEstimate: {
    color: "#555",
    marginBottom: "1.25rem",
    fontSize: "0.95rem",
  },
  section: {
    marginBottom: "1.5rem",
  },
  sectionTitle: {
    fontSize: "1.05rem",
    color: "#2c3e50",
    borderBottom: "2px solid #27ae60",
    paddingBottom: "0.3rem",
    marginBottom: "0.75rem",
  },
  ingredientList: {
    listStyle: "none",
    padding: 0,
    display: "flex",
    flexDirection: "column",
    gap: "0.4rem",
  },
  ingredientItem: {
    display: "flex",
    justifyContent: "space-between",
    padding: "0.35rem 0",
    borderBottom: "1px solid #f0f0f0",
    fontSize: "0.92rem",
  },
  ingName: {
    color: "#333",
  },
  ingAmount: {
    color: "#27ae60",
    fontWeight: "600",
  },
  stepList: {
    paddingLeft: "1.25rem",
    display: "flex",
    flexDirection: "column",
    gap: "0.6rem",
  },
  stepItem: {
    color: "#444",
    lineHeight: "1.5",
    fontSize: "0.92rem",
  },
  info: {
    color: "#888",
    fontStyle: "italic",
  },
  errorText: {
    color: "#c0392b",
  },
};

export default RecipeModal;
