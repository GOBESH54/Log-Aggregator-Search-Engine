import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function Navbar() {
  const { token, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <nav>
      <span className="brand">📋 Log Aggregator</span>
      {token && (
        <>
          <Link to="/">Search</Link>
          <Link to="/ingest">Ingest</Link>
          <Link to="/semantic">Semantic</Link>
          <Link to="/analyze">AI Analyze</Link>
          <Link to="/anomaly">Anomaly</Link>
          <button onClick={handleLogout} className="danger" style={{ marginLeft: 'auto' }}>
            Logout
          </button>
        </>
      )}
    </nav>
  );
}
