import { NavLink, Route, Routes } from 'react-router-dom';
import { Overview } from './pages/Overview';
import { PipelineMonitoring } from './pages/PipelineMonitoring';
import { Analytics } from './pages/Analytics';
import { PatientSearch } from './pages/PatientSearch';
import { FailureDemo } from './pages/FailureDemo';

const NAV_ITEMS = [
  { to: '/', label: 'Overview', end: true },
  { to: '/pipeline', label: 'Pipeline Monitoring' },
  { to: '/failure-demo', label: 'Failure Simulation' },
  { to: '/analytics', label: 'Analytics' },
  { to: '/patients', label: 'Patient Search' },
];

export default function App() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          Healthcare Data Platform
          <small>Distributed ingestion &amp; analytics demo</small>
        </div>
        <nav>
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          Synthetic data only — no real PHI.
          <br />
          Academic / portfolio demonstration.
        </div>
      </aside>
      <main className="main">
        <Routes>
          <Route path="/" element={<Overview />} />
          <Route path="/pipeline" element={<PipelineMonitoring />} />
          <Route path="/failure-demo" element={<FailureDemo />} />
          <Route path="/analytics" element={<Analytics />} />
          <Route path="/patients" element={<PatientSearch />} />
        </Routes>
      </main>
    </div>
  );
}