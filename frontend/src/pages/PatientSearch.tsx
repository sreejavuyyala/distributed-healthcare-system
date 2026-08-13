import { useState } from 'react';
import { api } from '../api/client';
import type { Diagnosis, Encounter, Lab, Patient, Procedure } from '../types';

interface PatientBundle {
  patient: Patient;
  encounters: Encounter[];
  diagnoses: Diagnosis[];
  procedures: Procedure[];
  labs: Lab[];
}

export function PatientSearch() {
  const [query, setQuery] = useState('PAT-0000001');
  const [bundle, setBundle] = useState<PatientBundle | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function search() {
    const id = query.trim();
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const [patient, encounters, diagnoses, procedures, labs] = await Promise.all([
        api.patient(id),
        api.patientEncounters(id),
        api.patientDiagnoses(id),
        api.patientProcedures(id),
        api.patientLabs(id),
      ]);
      setBundle({
        patient,
        encounters: encounters.content,
        diagnoses: diagnoses.content,
        procedures: procedures.content,
        labs: labs.content,
      });
    } catch (e) {
      setBundle(null);
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Patient Explorer</h1>
        <p>Search a synthetic patient ID (e.g. PAT-0000001) to view their full clinical history.</p>
      </div>

      <div className="card">
        <div className="row">
          <input
            className="input"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && search()}
            placeholder="Patient ID, e.g. PAT-0000001"
          />
          <button className="btn btn-primary" onClick={search} disabled={loading}>
            {loading ? 'Searching…' : 'Search'}
          </button>
        </div>
        {error && <div className="error-banner section-gap">{error}</div>}
      </div>

      {bundle && (
        <>
          <div className="card section-gap">
            <div className="card-title">Demographics</div>
            <table>
              <tbody>
                <tr><td>Patient ID</td><td>{bundle.patient.patientId}</td></tr>
                <tr><td>Name</td><td>{bundle.patient.firstName} {bundle.patient.lastName}</td></tr>
                <tr><td>Date of Birth</td><td>{bundle.patient.dateOfBirth}</td></tr>
                <tr><td>Gender</td><td>{bundle.patient.gender ?? '—'}</td></tr>
                <tr><td>Zip Code</td><td>{bundle.patient.zipCode ?? '—'}</td></tr>
              </tbody>
            </table>
          </div>

          <div className="grid grid-2 section-gap">
            <div className="card">
              <div className="card-title">Encounters ({bundle.encounters.length})</div>
              <table>
                <thead><tr><th>Date</th><th>Type</th><th>Department</th><th>Facility</th></tr></thead>
                <tbody>
                  {bundle.encounters.map((e) => (
                    <tr key={e.encounterId}>
                      <td>{e.encounterDate}</td>
                      <td>{e.encounterType ?? '—'}</td>
                      <td>{e.department ?? '—'}</td>
                      <td className="muted">{e.facilityName ?? '—'}</td>
                    </tr>
                  ))}
                  {bundle.encounters.length === 0 && <tr><td colSpan={4}><div className="empty-state">None</div></td></tr>}
                </tbody>
              </table>
            </div>

            <div className="card">
              <div className="card-title">Diagnoses ({bundle.diagnoses.length})</div>
              <table>
                <thead><tr><th>Code</th><th>Description</th></tr></thead>
                <tbody>
                  {bundle.diagnoses.map((d) => (
                    <tr key={d.diagnosisId}>
                      <td>{d.diagnosisCode}</td>
                      <td className="muted">{d.diagnosisDescription ?? '—'}</td>
                    </tr>
                  ))}
                  {bundle.diagnoses.length === 0 && <tr><td colSpan={2}><div className="empty-state">None</div></td></tr>}
                </tbody>
              </table>
            </div>

            <div className="card">
              <div className="card-title">Procedures ({bundle.procedures.length})</div>
              <table>
                <thead><tr><th>Date</th><th>Code</th><th>Description</th></tr></thead>
                <tbody>
                  {bundle.procedures.map((p) => (
                    <tr key={p.procedureId}>
                      <td>{p.procedureDate}</td>
                      <td>{p.procedureCode}</td>
                      <td className="muted">{p.procedureDescription ?? '—'}</td>
                    </tr>
                  ))}
                  {bundle.procedures.length === 0 && <tr><td colSpan={3}><div className="empty-state">None</div></td></tr>}
                </tbody>
              </table>
            </div>

            <div className="card">
              <div className="card-title">Lab Results ({bundle.labs.length})</div>
              <table>
                <thead><tr><th>Collected</th><th>Test</th><th>Result</th><th>Range</th></tr></thead>
                <tbody>
                  {bundle.labs.map((l) => (
                    <tr key={l.labId}>
                      <td>{new Date(l.collectedAt).toLocaleDateString()}</td>
                      <td>{l.testName}</td>
                      <td>{l.testResult ?? '—'}</td>
                      <td className="muted">{l.referenceRange ?? '—'}</td>
                    </tr>
                  ))}
                  {bundle.labs.length === 0 && <tr><td colSpan={4}><div className="empty-state">None</div></td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
}