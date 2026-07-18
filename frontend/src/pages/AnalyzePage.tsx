import { useState, useRef } from 'react';

export default function AnalyzePage() {
  const [logsText, setLogsText] = useState('');
  const [query, setQuery] = useState('');
  const [stream, setStream] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  const analyze = async () => {
    const logs = logsText.split('\n').map((l) => l.trim()).filter(Boolean);
    if (!logs.length) return;

    setError('');
    setStream('');
    setLoading(true);

    abortRef.current?.abort();
    const ctrl = new AbortController();
    abortRef.current = ctrl;

    const token = localStorage.getItem('jwt');

    try {
      const res = await fetch('/api/logs/analyze/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ logs, query: query || undefined }),
        signal: ctrl.signal,
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const reader = res.body!.getReader();
      const decoder = new TextDecoder();

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        const chunk = decoder.decode(value);
        // SSE format: "data: ...\n\n"
        const lines = chunk.split('\n');
        for (const line of lines) {
          if (line.startsWith('data:')) {
            setStream((prev) => prev + line.slice(5));
          }
        }
      }
    } catch (e: unknown) {
      if ((e as Error).name !== 'AbortError') {
        setError((e as Error).message ?? 'Analysis failed');
      }
    } finally {
      setLoading(false);
    }
  };

  const stop = () => { abortRef.current?.abort(); setLoading(false); };

  return (
    <div className="page">
      <h1>AI Log Analysis</h1>
      <div className="card">
        <textarea
          placeholder="Paste log lines here (one per line)"
          value={logsText}
          onChange={(e) => setLogsText(e.target.value)}
          style={{ minHeight: 140 }}
        />
        <div className="form-row" style={{ marginTop: '0.75rem' }}>
          <input
            placeholder="Optional query / focus (e.g. why did the service crash?)"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <button onClick={analyze} disabled={loading || !logsText.trim()}>
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
