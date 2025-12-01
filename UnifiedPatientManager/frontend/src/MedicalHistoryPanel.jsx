import React, { useEffect, useState } from "react";
import { patientApi } from "./api/patientApi";

export default function MedicalHistoryPanel({ patientId, onChange, onPrescribeRequested }) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // form state
  const [doctorId, setDoctorId] = useState("");
  const [diagnosis, setDiagnosis] = useState("");
  const [frequency, setFrequency] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [prescribeMedication, setPrescribeMedication] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);

  const load = async () => {
    if (!patientId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await patientApi.getMedicalHistory(patientId);
      setRecords(res.data || []);
    } catch (err) {
      console.error("Failed to load medical history", err);
      setError(err?.response?.data?.message || err?.message || "Failed to load medical history");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [patientId]);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSaveError(null);
    try {
      const payload = {
        // note: controller accepts patient id from URL; patient_id is optional in DTO
        doctor_id: doctorId ? Number(doctorId) : null,
        diagnosis,
        frequency,
        start_date: startDate || null,
        end_date: endDate || null,
        prescribe_medication: prescribeMedication === true,
      };
      const res = await patientApi.createMedicalHistory(patientId, payload);
      // reload list
      await load();
      // clear form
      setDoctorId("");
      setDiagnosis("");
      setFrequency("");
      setStartDate("");
      setEndDate("");
      // notify parent if provided
      if (onChange) onChange();
      // if user requested prescribing a medication, inform parent to switch to prescriptions
      if (prescribeMedication && typeof onPrescribeRequested === 'function') {
        onPrescribeRequested();
      }
    } catch (err) {
      console.error("Failed to create medical history", err);
      setSaveError(err?.response?.data?.message || err?.message || "Failed to save record");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="upm-card" style={{ marginTop: 16 }}>
      <h3 style={{ marginTop: 0 }}>Diagnoses / Medical History</h3>
      <div className="upm-divider" />
      <form onSubmit={handleCreate} style={{ marginBottom: 12 }}>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
          <input placeholder="Doctor ID (required)" value={doctorId} onChange={(e) => setDoctorId(e.target.value)} style={{ width: 140 }} required />
          <input placeholder="Diagnosis (required)" value={diagnosis} onChange={(e) => setDiagnosis(e.target.value)} style={{ width: 240 }} required />
          <input placeholder="Frequency (required)" value={frequency} onChange={(e) => setFrequency(e.target.value)} style={{ width: 140 }} required />
          <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            Start
            <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
          </label>
          <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            End
            <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
          </label>
          <label style={{ display: 'flex', alignItems: 'center', gap: 6, marginLeft: 8 }}>
            <input type="checkbox" checked={prescribeMedication} onChange={(e) => setPrescribeMedication(e.target.checked)} />
            <span style={{ fontSize: 13 }}>Also prescribe a medication?</span>
          </label>
          <button className="upm-tab" type="submit" disabled={saving}>{saving ? 'Saving...' : 'Add Diagnosis'}</button>
        </div>
        {saveError && <div style={{ color: '#c00', marginTop: 6 }}>{saveError}</div>}
      </form>

      <div>
        {loading && <div>Loading records...</div>}
        {!loading && error && <div style={{ color: '#c00' }}>{error}</div>}
        {!loading && !error && records.length === 0 && <div>No diagnoses recorded.</div>}
        {!loading && !error && records.length > 0 && (
          <ul>
            {records.map(r => (
              <li key={r.id || r.historyId || JSON.stringify(r)}>
                <strong>{r.diagnosis}</strong> {r.frequency ? `– ${r.frequency}` : ''} {r.startDate ? `(${new Date(r.startDate).toLocaleDateString()}` : ''}{r.endDate ? ` to ${new Date(r.endDate).toLocaleDateString()})` : (r.startDate ? ')' : '')}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
