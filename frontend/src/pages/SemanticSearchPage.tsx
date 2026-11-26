import { useState } from 'react';
import { logsApi } from '../api/client';
import type { LogEntry } from '../types';
import LogTable from '../components/LogTable';

export default function SemanticSearchPage() {
  const [query, setQuery] = useState('');
  const [topK, setTopK] = useState('20');
  const [results, setResults] = useState<LogEntry[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const search = async () => {
    const limit = Number(topK);
    if (loading || !query.trim()) return;
    if (!Number.isInteger(limit) || limit < 1 || limit > 100) {
      setError('Enter a result limit between 1 and 100.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      const { data } = await logsApi.semanticSearch({
        semanticQuery: query,
        semanticTopK: limit,
        size: limit,
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
            aria-label="Semantic search query"
            maxLength={300}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && search()}
            style={{ flex: 3 }}
          />
          <input
            type="number"
            placeholder="Top K"
            aria-label="Result limit"
            value={topK}
            min={1}
            max={100}
            onChange={(e) => setTopK(e.target.value)}
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
