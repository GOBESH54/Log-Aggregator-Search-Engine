import { useState } from 'react';
import { logsApi } from '../api/client';

export default function AnomalyPage() {
  const [result, setResult] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const run = async () => {
    setError('');
    setResult('');
    setLoading(true);
    try {
      const { data } = await logsApi.runAnomalyScan();
      setResult(data);
    } catch (e: unknown) {
      setError((e as Error).message ?? 'Scan failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>Anomaly Detection</h1>
      <div className="card">
        <p style={{ color: '#a0aec0', marginBottom: '1rem' }}>
          Triggers an immediate AI-powered anomaly scan over the last hour of logs.
          This normally runs on a scheduled cron job.
        </p>
        <button onClick={run} disabled={loading}>
          {loading ? 'Running scan…' : '▶ Run Anomaly Scan Now'}
        </button>
        {result && <p className="success-msg" style={{ marginTop: '0.75rem' }}>✓ {result}</p>}
        {error && <p className="error-msg">{error}</p>}
      </div>
    </div>
  );
}
