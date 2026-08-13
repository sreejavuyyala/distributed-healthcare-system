import { api } from '../api/client';
import { usePolling } from '../hooks/usePolling';
import { StatCard } from '../components/StatCard';
import { StatusBadge } from '../components/StatusBadge';
import { Link } from 'react-router-dom';

export function Overview() {
  const overview = usePolling(api.overview, 15000);
  const metrics = usePolling(api.metrics, 15000);
  const feeds = usePolling(api.feedStatus, 5000);

  return (
    <div>
      <div className="page-header">
        <h1>Platform Overview</h1>
        <p>Live counts from the PostgreSQL analytics layer — synthetic data only.</p>
      </div>

      {overview.error && <div className="error-banner">Could not load overview: {overview.error}</div>}

      <div className="grid grid-stats">
        <StatCard label="Patients" value={overview.data?.totalPatients.toLocaleString() ?? '—'} />
        <StatCard label="Encounters" value={overview.data?.totalEncounters.toLocaleString() ?? '—'} />
        <StatCard label="Diagnoses" value={overview.data?.totalDiagnoses.toLocaleString() ?? '—'} />
        <StatCard label="Procedures" value={overview.data?.totalProcedures.toLocaleString() ?? '—'} />
        <StatCard label="Lab Results" value={overview.data?.totalLabs.toLocaleString() ?? '—'} />
      </div>

      <div className="grid grid-2 section-gap">
        <div className="card">
          <div className="card-title">Platform Metrics</div>
          {metrics.data ? (
            <table>
              <tbody>
                <tr>
                  <td>Total records processed</td>
                  <td>{metrics.data.recordsProcessed.toLocaleString()}</td>
                </tr>
                <tr>
                  <td>Successful feed runs</td>
                  <td>{metrics.data.successfulFeeds}</td>
                </tr>
                <tr>
                  <td>Failed feed runs</td>
                  <td>{metrics.data.failedFeeds}</td>
                </tr>
                <tr>
                  <td>Avg. ingestion processing time</td>
                  <td>{metrics.data.averageProcessingTimeMs.toFixed(1)} ms</td>
                </tr>
                <tr>
                  <td>Avg. analytics query latency (live probe)</td>
                  <td>{metrics.data.averageQueryLatencyMs.toFixed(2)} ms</td>
                </tr>
              </tbody>
            </table>
          ) : (
            <div className="empty-state">Loading…</div>
          )}
        </div>

        <div className="card">
          <div className="card-title">Pipeline Health (latest run per feed)</div>
          {feeds.data && feeds.data.length > 0 ? (
            <table>
              <thead>
                <tr>
                  <th>Feed</th>
                  <th>Status</th>
                  <th>Records</th>
                  <th>Retries</th>
                </tr>
              </thead>
              <tbody>
                {feeds.data.map((f) => (
                  <tr key={f.feedName}>
                    <td style={{ textTransform: 'capitalize' }}>{f.feedName}</td>
                    <td>
                      <StatusBadge status={f.status} />
                    </td>
                    <td>{f.recordsProcessed.toLocaleString()}</td>
                    <td>{f.retryCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="empty-state">
              No ingestion runs yet. Trigger one from{' '}
              <Link to="/pipeline">Pipeline Monitoring</Link>.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}