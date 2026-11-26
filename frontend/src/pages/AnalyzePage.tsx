import { useEffect, useState, useRef } from 'react';
import { streamAnalysis } from '../api/analysis';

export default function AnalyzePage() {
  const [searchText, setSearchText] = useState('');
  const [prompt, setPrompt] = useState('');
  const [stream, setStream] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const abortRef = useRef<AbortController | null>(null);
  useEffect(() => () => abortRef.current?.abort(), []);

  const analyze = async () => {
    setError('');
    setStream('');
    setLoading(true);

    abortRef.current?.abort();
    const ctrl = new AbortController();
    abortRef.current = ctrl;

    try {
      await streamAnalysis(searchText, prompt, ctrl.signal, (chunk) => {
        setStream((text) => text + chunk);
      });
    } catch (e: unknown) {
      if (!ctrl.signal.aborted) {
        setError(e instanceof Error ? e.message : 'Analysis failed');
      }
    } finally {
      if (abortRef.current === ctrl) {
        abortRef.current = null;
        setLoading(false);
      }
    }
  };

  const stop = () => abortRef.current?.abort();

  return (
    <div className="page">
      <h1>AI Log Analysis</h1>
      <div className="card">
        <textarea
          aria-label="Analysis question"
          placeholder="What would you like to know about your stored logs?"
          maxLength={2000}
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          style={{ minHeight: 140 }}
        />
        <div className="form-row" style={{ marginTop: '0.75rem' }}>
          <input
            aria-label="Log search text"
            placeholder="Optional search text (e.g. connection timeout)"
            maxLength={300}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
          />
          <button onClick={analyze} disabled={loading}>
            {loading ? 'Analyzing…' : 'Analyze'}
          </button>
          {loading && (
            <button className="danger" onClick={stop}>Stop</button>
          )}
        </div>
        {error && <p className="error-msg">{error}</p>}
      </div>

      {stream && (
        <div className="card">
          <h2>Analysis Result</h2>
          <div className="stream-box">{stream}</div>
        </div>
      )}
    </div>
  );
}
