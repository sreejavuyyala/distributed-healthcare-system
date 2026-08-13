import { useState } from 'react';
import { api } from '../api/client';
import { usePolling } from '../hooks/usePolling';
import { StatusBadge } from '../components/StatusBadge';

const FEEDS = ['PATIENTS', 'ENCOUNTERS', 'DIAGNOSES', 'PROCEDURES', 'LABS'];

export function FailureDemo() {
  const [targetFeed, setTargetFeed] = useState('ENCOUNTERS');
  const [attempts, setAttempts] = useState(3);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const feeds = usePolling(api.feedStatus, 3000);

  async function simulate() {
    setBusy(true);
    setMessage(null);
    try {
      const res = await api.simulateFailure(targetFeed, attempts);
      setMessage(res.message);
    } catch (e) {
      setMessage((e as Error).message);
    } finally {
      setTimeout(() => setBusy(false), 3000);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Pipeline Failure Simulation</h1>
        <p>
          Force one feed to fail deterministically and watch the others keep succeeding — this is
          per-feed failure isolation, the core distributed-systems property of this platform.
        </p>
      </div>

      <div className="card">
        <div className="card-title">Configure a Simulated Failure</div>
        <div className="row" style={{ flexWrap: 'wrap', gap: 14 }}>
          <div style={{ minWidth: 200 }}>
            <label className="muted" style={{ fontSize: 12 }}>Feed to fail</label>
            <select className="input" value={targetFeed} onChange={(e) => setTargetFeed(e.target.value)}>
              {FEEDS.map((f) => (
                <option key={f} value={f}>{f}</option>
              ))}
            </select>
          </div>
          <div style={{ minWidth: 160 }}>
            <label className="muted" style={{ fontSize: 12 }}>Attempts to fail (3 = exhaust all retries)</label>
            <input
              className="input"
              type="number"
              min={1}
              max={5}
              value={attempts}
              onChange={(e) => setAttempts(Number(e.target.value))}
            />
          </div>
          <button className="btn btn-danger" onClick={simulate} disabled={busy} style={{ alignSelf: 'flex-end' }}>
            {busy ? 'Running…' : 'Trigger Failure & Run All Feeds'}
          </button>
        </div>
        {message && <div className="callout callout-info section-gap">{message}</div>}
      </div>

      <div className="card section-gap">
        <div className="card-title">Live Feed Status</div>
        <table>
          <thead>
            <tr>
              <th>Feed</th>
              <th>Status</th>
              <th>Records Processed</th>
              <th>Retry Count</th>
              <th>Error</th>
            </tr>
          </thead>
          <tbody>
            {(feeds.data ?? []).map((f) => (
              <tr key={f.feedName} style={f.feedName === targetFeed.toLowerCase() ? { background: '#fff8f6' } : undefined}>
                <td style={{ textTransform: 'capitalize', fontWeight: 600 }}>{f.feedName}</td>
                <td><StatusBadge status={f.status} /></td>
                <td>{f.recordsProcessed.toLocaleString()}</td>
                <td>{f.retryCount}</td>
                <td className="muted" style={{ maxWidth: 320, whiteSpace: 'normal' }}>{f.errorMessage ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <p className="muted section-gap" style={{ marginBottom: 0 }}>
          Expect: the targeted feed lands on <strong>FAILED</strong> after exhausting its retries, while every
          other feed still reports <strong>SUCCESS</strong> with its full record count. Clear the effect by
          re-running with a lower attempts value, or by calling{' '}
          <code>POST /api/feeds/simulate/{'{feed}'}/clear</code>.
        </p>
      </div>
    </div>
  );
}