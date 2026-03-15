import { Link } from "react-router-dom";

function Navbar() {
  return (
    <nav className="navbar">
      <h1>Meal Planner</h1>
      <div className="nav-links">
        <Link to="/">Hjem</Link>
        <Link to="/planner">Planlegger</Link>
        <Link to="/results">Resultater</Link>
      </div>
    </nav>
  );
}

export default Navbar;