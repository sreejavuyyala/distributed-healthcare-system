import { useState } from 'react';
import { api } from '../api/client';
import { usePolling } from '../hooks/usePolling';
import { StatusBadge } from '../components/StatusBadge';

function formatDuration(start: string, end: string | null): string {
  if (!end) return '—';
  const ms = new Date(end).getTime() - new Date(start).getTime();
  return `${(ms / 1000).toFixed(1)}s`;
}

export function PipelineMonitoring() {
  const [running, setRunning] = useState(false);
  const feeds = usePolling(api.feedStatus, 4000);
  const executions = usePolling(() => api.feedExecutions(0, 20), 5000);

  async function runAll() {
    setRunning(true);
    try {
      await api.runAllFeeds();
    } finally {
      setTimeout(() => setRunning(false), 3000);
    }
  }

  return (
    <div>
      <div className="page-header row" style={{ justifyContent: 'space-between' }}>
        <div>
          <h1>Pipeline Monitoring</h1>
          <p>Each feed ingests, retries, and fails independently — the feed_execution audit trail below is the proof.</p>
        </div>
        <button className="btn btn-primary" onClick={runAll} disabled={running}>
          {running ? 'Starting…' : 'Run All Feeds'}
        </button>
      </div>

      <div className="card">
        <div className="card-title">Current Feed Status</div>
        <table>
          <thead>
            <tr>
              <th>Feed</th>
              <th>Status</th>
              <th>Last Run</th>
              <th>Records Processed</th>
              <th>Records Failed</th>
              <th>Retries</th>
              <th>Error</th>
            </tr>
          </thead>
          <tbody>
            {(feeds.data ?? []).map((f) => (
              <tr key={f.feedName}>
                <td style={{ textTransform: 'capitalize' }}>{f.feedName}</td>
                <td><StatusBadge status={f.status} /></td>
                <td>{new Date(f.lastRun).toLocaleString()}</td>
                <td>{f.recordsProcessed.toLocaleString()}</td>
                <td>{f.recordsFailed.toLocaleString()}</td>
                <td>{f.retryCount}</td>
                <td className="muted" style={{ maxWidth: 260, whiteSpace: 'normal' }}>{f.errorMessage ?? '—'}</td>
              </tr>
            ))}
            {feeds.data && feeds.data.length === 0 && (
              <tr><td colSpan={7}><div className="empty-state">No runs yet — click "Run All Feeds".</div></td></tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="card section-gap">
        <div className="card-title">Execution History (feed_execution audit trail)</div>
        <table>
          <thead>
            <tr>
              <th>Feed</th>
              <th>Batch ID</th>
              <th>Status</th>
              <th>Start</th>
              <th>Duration</th>
              <th>Processed</th>
              <th>Retries</th>
            </tr>
          </thead>
          <tbody>
            {(executions.data?.content ?? []).map((e) => (
              <tr key={e.executionId}>
                <td style={{ textTransform: 'capitalize' }}>{e.feedName}</td>
                <td className="muted" title={e.batchId}>{e.batchId.slice(0, 8)}…</td>
                <td><StatusBadge status={e.status} /></td>
                <td>{new Date(e.startTime).toLocaleTimeString()}</td>
                <td>{formatDuration(e.startTime, e.endTime)}</td>
                <td>{e.recordsProcessed.toLocaleString()}</td>
                <td>{e.retryCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}