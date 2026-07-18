import type { LogEntry } from '../types';

interface Props { logs: LogEntry[]; }

export default function LogTable({ logs }: Props) {
  if (!logs.length) return <p style={{ color: '#718096' }}>No results.</p>;

  return (
    <table className="log-table">
      <thead>
        <tr>
          <th>Time</th>
          <th>Level</th>
          <th>Source</th>
          <th>Message</th>
        </tr>
      </thead>
      <tbody>
        {logs.map((l) => (
          <tr key={l.id}>
            <td style={{ whiteSpace: 'nowrap', color: '#718096' }}>
              {new Date(l.timestamp).toLocaleString()}
            </td>
            <td>
              <span className={`badge badge-${l.level}`}>{l.level}</span>
            </td>
            <td style={{ color: '#a0aec0' }}>{l.source}</td>
            <td>{l.message}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
