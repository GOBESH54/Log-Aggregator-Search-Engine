import { useState } from 'react';
import { logsApi } from '../api/client';
import type { LogEntry } from '../types';
import LogTable from '../components/LogTable';

export default function SearchPage() {
  const [searchText, setSearchText] = useState('');
  const [source, setSource] = useState('');
  const [level, setLevel] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [page, setPage] = useState(0);
  const [size] = useState(50);
  const [results, setResults] = useState<LogEntry[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const search = async (p = 0) => {
    setError('');
    setLoading(true);
    try {
      const { data } = await logsApi.search({
        searchText: searchText || undefined,
        source: source || undefined,
        level: level || undefined,
        startTime: startTime || undefined,
        endTime: endTime || undefined,
        page: p,
        size,
      });
      setResults(data);
      setPage(p);
    } catch (e: unknown) {
      setError((e as Error).message ?? 'Search failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>Keyword Search</h1>
      <div className="card">
        <div className="form-row">
          <input placeholder="Search text" value={searchText} onChange={(e) => setSearchText(e.target.value)} />
          <input placeholder="Source" value={source} onChange={(e) => setSource(e.target.value)} />
          <select value={level} onChange={(e) => setLevel(e.target.value)}>
            <option value="">All levels</option>
            {['ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE'].map((l) => (
              <option key={l}>{l}</option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <input type="datetime-local" value={startTime} onChange={(e) => setStartTime(e.target.value)} />
          <input type="datetime-local" value={endTime} onChange={(e) => setEndTime(e.target.value)} />
          <button onClick={() => search(0)} disabled={loading}>
            {loading ? 'Searching…' : 'Search'}
          </button>
        </div>
        {error && <p className="error-msg">{error}</p>}
      </div>

      <div className="card">
        <LogTable logs={results} />
        {results.length > 0 && (
          <div className="pagination">
            <button onClick={() => search(page - 1)} disabled={page === 0 || loading}>← Prev</button>
            <span style={{ color: '#a0aec0' }}>Page {page + 1}</span>
            <button onClick={() => search(page + 1)} disabled={results.length < size || loading}>Next →</button>
          </div>
        )}
      </div>
    </div>
  );
}
