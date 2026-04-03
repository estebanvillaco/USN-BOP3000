import { Link } from "react-router-dom";

function HomePage() {
  return (
    <div className="page">
      <h2>Velkommen til Meal Planner</h2>
      <p>
        Dette systemet hjelper brukere med å planlegge måltider basert på
        preferanser, allergier, kostholdsrestriksjoner og budsjett.
      </p>
      <Link to="/planner" className="button-link">
        Start planlegging
      </Link>
      <div style={{ color: 'black', background: 'white' }}>Test: If you see this, the app is rendering.</div>
    </div>
  );
}

export default HomePage;