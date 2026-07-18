import { useState } from 'react';
import { logsApi } from '../api/client';
import type { LogIngestRequest } from '../types';

const EMPTY: LogIngestRequest = { source: '', level: 'INFO', message: '', metadata: '' };

export default function IngestPage() {
  const [entries, setEntries] = useState<LogIngestRequest[]>([{ ...EMPTY }]);
  const [result, setResult] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const update = (i: number, field: keyof LogIngestRequest, value: string) => {
    setEntries((prev) => prev.map((e, idx) => (idx === i ? { ...e, [field]: value } : e)));
  };

  const addRow = () => setEntries((prev) => [...prev, { ...EMPTY }]);
  const removeRow = (i: number) => setEntries((prev) => prev.filter((_, idx) => idx !== i));

  const submit = async () => {
    setError('');
    setResult('');
    setLoading(true);
    try {
      const { data } = await logsApi.ingest(entries);
      setResult(data);
      setEntries([{ ...EMPTY }]);
    } catch (e: unknown) {
      setError((e as Error).message ?? 'Ingest failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>Ingest Logs</h1>
      {entries.map((entry, i) => (
        <div className="card" key={i}>
          <div className="form-row">
            <input
              placeholder="Source"
              value={entry.source}
              onChange={(e) => update(i, 'source', e.target.value)}
            />
            <select value={entry.level} onChange={(e) => update(i, 'level', e.target.value)}>
              {['ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE'].map((l) => (
                <option key={l}>{l}</option>
              ))}
            </select>
            {entries.length > 1 && (
              <button className="danger" onClick={() => removeRow(i)}>Remove</button>
            )}
          </div>
          <textarea
            placeholder="Message"
            value={entry.message}
            onChange={(e) => update(i, 'message', e.target.value)}
          />
          <textarea
            placeholder="Metadata (optional JSON)"
            value={entry.metadata}
            onChange={(e) => update(i, 'metadata', e.target.value)}
            style={{ marginTop: '0.5rem', minHeight: 50 }}
          />
        </div>
      ))}

      <div style={{ display: 'flex', gap: '0.75rem' }}>
        <button onClick={addRow}>+ Add Entry</button>
        <button className="success" onClick={submit} disabled={loading}>
          {loading ? 'Ingesting…' : 'Ingest'}
        </button>
      </div>

      {result && <p className="success-msg">{result}</p>}
      {error && <p className="error-msg">{error}</p>}
    </div>
  );
}
