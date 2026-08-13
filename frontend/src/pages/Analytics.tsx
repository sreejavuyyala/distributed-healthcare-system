import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { api } from '../api/client';
import { usePolling } from '../hooks/usePolling';

const PRIMARY = '#0e6f6a';
const SECONDARY = '#c0392b';

export function Analytics() {
  const trend = usePolling(() => api.encounterTrend(18), 30000);
  const diagnoses = usePolling(() => api.diagnosisFrequency(10), 30000);
  const facilities = usePolling(api.facilityActivity, 30000);
  const providers = usePolling(() => api.providerWorkload(8), 30000);

  return (
    <div>
      <div className="page-header">
        <h1>Clinical Analytics</h1>
        <p>Aggregate rollups computed with indexed SQL over the partitioned encounters table.</p>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="card-title">Encounters Over Time</div>
          {trend.data && trend.data.length > 0 ? (
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={trend.data}>
                <CartesianGrid strokeDasharray="3 3" stroke="#eef2f2" />
                <XAxis dataKey="period" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Line type="monotone" dataKey="encounterCount" stroke={PRIMARY} strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-state">No data yet.</div>
          )}
        </div>

        <div className="card">
          <div className="card-title">Top Diagnoses</div>
          {diagnoses.data && diagnoses.data.length > 0 ? (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={diagnoses.data} layout="vertical" margin={{ left: 60 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#eef2f2" />
                <XAxis type="number" tick={{ fontSize: 11 }} />
                <YAxis type="category" dataKey="diagnosisCode" tick={{ fontSize: 11 }} width={70} />
                <Tooltip formatter={(v: number, _n, entry) => [v, entry.payload.diagnosisDescription]} />
                <Bar dataKey="count" fill={PRIMARY} radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-state">No data yet.</div>
          )}
        </div>

        <div className="card">
          <div className="card-title">Encounters by Facility</div>
          {facilities.data && facilities.data.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={facilities.data} margin={{ bottom: 60 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#eef2f2" />
                <XAxis dataKey="facilityName" tick={{ fontSize: 10 }} angle={-30} textAnchor="end" interval={0} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="encounterCount" fill={SECONDARY} radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-state">No data yet.</div>
          )}
        </div>

        <div className="card">
          <div className="card-title">Provider Workload</div>
          {providers.data && providers.data.length > 0 ? (
            <table>
              <thead>
                <tr><th>Provider</th><th>Specialty</th><th>Encounters</th></tr>
              </thead>
              <tbody>
                {providers.data.map((p) => (
                  <tr key={p.providerName}>
                    <td>{p.providerName}</td>
                    <td className="muted">{p.specialty ?? '—'}</td>
                    <td>{p.encounterCount.toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="empty-state">No data yet.</div>
          )}
        </div>
      </div>
    </div>
  );
}