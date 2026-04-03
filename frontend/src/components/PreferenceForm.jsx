import { useState } from "react";
import { useNavigate } from "react-router-dom";

function PreferenceForm() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    budget: "",
    days: "7",
    dietType: "None",
    allergies: "",
    preferences: ""
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  function handleChange(event) {
    setFormData({
      ...formData,
      [event.target.name]: event.target.value
    });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await fetch("http://localhost:8080/api/meal-plan", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData)
      });

      if (!response.ok) throw new Error("Kunne ikke generere måltidsplan");

      const mealPlan = await response.json();
      navigate("/results", { state: { mealPlan } });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="form-card" onSubmit={handleSubmit}>
      <label>Budsjett (kr)</label>
      <input
        type="number"
        name="budget"
        value={formData.budget}
        onChange={handleChange}
        placeholder="F.eks. 500"
        required
      />

      <label>Antall dager</label>
      <select name="days" value={formData.days} onChange={handleChange}>
        <option value="3">3 dager</option>
        <option value="5">5 dager</option>
        <option value="7">7 dager</option>
      </select>

      <label>Kostholdstype</label>
      <select name="dietType" value={formData.dietType} onChange={handleChange}>
        <option value="None">Ingen</option>
        <option value="Vegetarian">Vegetar</option>
        <option value="Vegan">Vegan</option>
        <option value="Lactose Free">Laktosefri</option>
        <option value="Gluten Free">Glutenfri</option>
      </select>

      <label>Allergier</label>
      <input
        type="text"
        name="allergies"
        value={formData.allergies}
        onChange={handleChange}
        placeholder="F.eks. nøtter, melk"
      />

      <label>Matpreferanser</label>
      <input
        type="text"
        name="preferences"
        value={formData.preferences}
        onChange={handleChange}
        placeholder="F.eks. pasta, kylling, fisk"
      />

      {error && <p style={{ color: "red" }}>{error}</p>}
      <button type="submit" disabled={loading}>
        {loading ? "Genererer..." : "Generer måltidsplan"}
      </button>
    </form>
  );
}

export default PreferenceForm;