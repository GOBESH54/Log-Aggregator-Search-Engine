import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import SearchPage from './pages/SearchPage';
import IngestPage from './pages/IngestPage';
import SemanticSearchPage from './pages/SemanticSearchPage';
import AnalyzePage from './pages/AnalyzePage';
import AnomalyPage from './pages/AnomalyPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<ProtectedRoute><SearchPage /></ProtectedRoute>} />
          <Route path="/ingest" element={<ProtectedRoute><IngestPage /></ProtectedRoute>} />
          <Route path="/semantic" element={<ProtectedRoute><SemanticSearchPage /></ProtectedRoute>} />
          <Route path="/analyze" element={<ProtectedRoute><AnalyzePage /></ProtectedRoute>} />
          <Route path="/anomaly" element={<ProtectedRoute><AnomalyPage /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
