import { useState } from 'react';
import { logsApi } from '../api/client';
import type { LogEntry } from '../types';
import LogTable from '../components/LogTable';

export default function SemanticSearchPage() {
  const [query, setQuery] = useState('');
  const [topK, setTopK] = useState(20);
  const [results, setResults] = useState<LogEntry[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const search = async () => {
    if (!query.trim()) return;
    setError('');
    setLoading(true);
    try {
      const { data } = await logsApi.semanticSearch({
        semanticQuery: query,
        semanticTopK: topK,
        semanticSearch: true,
      });
      setResults(data);
    } catch (e: unknown) {
      setError((e as Error).message ?? 'Semantic search failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>Semantic Search</h1>
      <div className="card">
        <div className="form-row">
          <input
            placeholder="Natural language query (e.g. database connection errors)"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && search()}
            style={{ flex: 3 }}
          />
          <input
            type="number"
            placeholder="Top K"
            value={topK}
            min={1}
            max={100}
            onChange={(e) => setTopK(Number(e.target.value))}
            style={{ flex: 0.5, minWidth: 80 }}
          />
          <button onClick={search} disabled={loading || !query.trim()}>
            {loading ? 'Searching…' : 'Search'}
          </button>
        </div>
        {error && <p className="error-msg">{error}</p>}
      </div>

      <div className="card">
        <LogTable logs={results} />
      </div>
    </div>
  );
}
