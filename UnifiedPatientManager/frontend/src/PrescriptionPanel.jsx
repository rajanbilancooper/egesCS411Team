import React, { useEffect, useState } from "react";
import { patientApi } from "./api/patientApi";
import { PRESCRIPTION_MEDICATIONS } from "./constants/medicalOptions";

export default function PrescriptionPanel({ patientId }) {
  const [medications, setMedications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Form state
  const [showForm, setShowForm] = useState(false);
  const [drugName, setDrugName] = useState("");
  const [dose, setDose] = useState("");
  const [frequency, setFrequency] = useState("");
  const [duration, setDuration] = useState("");
  const [route, setRoute] = useState("oral");
  const [notes, setNotes] = useState("");
  
  // Conflict handling
  const [conflicts, setConflicts] = useState([]);
  const [showConflicts, setShowConflicts] = useState(false);
  const [overrideJustification, setOverrideJustification] = useState("");
  const [pendingPrescription, setPendingPrescription] = useState(null);

  const providerId = 1; // Hardcoded for demo; in real app get from auth context

  const loadMedications = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await patientApi.getMedications(patientId);
      setMedications(res.data || []);
    } catch (err) {
      console.error("Failed to load medications", err);
      setError(err?.response?.data?.message || "Failed to load medications");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMedications();
  }, [patientId]);

  const resetForm = () => {
    setDrugName("");
    setDose("");
    setFrequency("");
    setDuration("");
    setRoute("oral");
    setNotes("");
    setConflicts([]);
    setShowConflicts(false);
    setOverrideJustification("");
    setPendingPrescription(null);
    setShowForm(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    // Prevent adding an obvious duplicate prescription: same drug, dose, and frequency
    try {
      const normalizedDrug = (drugName || "").toString().trim().toLowerCase();

      const isDuplicate = medications.some((m) => {
        const mDrug = (m.drug_name || "").toString().trim().toLowerCase();
        return mDrug === normalizedDrug;
      });

      if (isDuplicate) {
        alert("A prescription for this drug already exists.\nIf you meant to add a different dosage/frequency, please edit or delete the existing prescription first.");
        return;
      }
    } catch (dupCheckErr) {
      // Non-fatal: if duplicate check fails for any reason, allow submission to proceed
      console.warn("Duplicate check failed, proceeding:", dupCheckErr);
    }

    const payload = {
      drug_name: drugName,
      dose,
      frequency,
      duration,
      route,
      notes,
      is_perscription: true,
      status: true,
      override: false,
    };

    setPendingPrescription(payload);

    try {
      const res = await patientApi.createPrescription(patientId, providerId, payload);
      const result = res.data;

      if (result.conflicts && result.conflict_messages && result.conflict_messages.length > 0) {
        // Conflicts detected
        setConflicts(result.conflict_messages);
        setShowConflicts(true);
      } else if (result.prescription) {
        // Success
        alert("Prescription added successfully!");
        resetForm();
        loadMedications();
      } else {
        // Unexpected response
        alert("Unexpected response from server");
      }
    } catch (err) {
      console.error("Failed to create prescription", err);
      alert(err?.response?.data?.message || "Failed to create prescription");
    }
  };

  const handleOverride = async () => {
    if (!overrideJustification.trim()) {
      alert("Please provide a justification for overriding the conflict.");
      return;
    }

    const payload = {
      ...pendingPrescription,
      override: true,
      override_justification: overrideJustification,
    };

    try {
      const res = await patientApi.createPrescription(patientId, providerId, payload);
      const result = res.data;

      if (result.prescription) {
        alert("Prescription added with override!");
        resetForm();
        loadMedications();
      } else {
        alert("Failed to override conflicts");
      }
    } catch (err) {
      console.error("Failed to override prescription", err);
      alert(err?.response?.data?.message || "Failed to override prescription");
    }
  };

  const handleDelete = async (medicationId) => {
    if (!window.confirm("Delete this medication?")) return;
    try {
      await patientApi.deleteMedication(patientId, medicationId);
      alert("Medication deleted");
      loadMedications();
    } catch (err) {
      console.error("Failed to delete medication", err);
      alert(err?.response?.data?.message || "Failed to delete medication");
    }
  };

  if (loading) {
    return <div style={{ padding: "1rem" }}>Loading prescriptions...</div>;
  }

  if (error) {
    return <div style={{ padding: "1rem", color: "#c00" }}>Error: {error}</div>;
  }

  return (
    <div style={{ padding: "1rem" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
        <h3>Prescriptions</h3>
        <button
          onClick={() => setShowForm(!showForm)}
          style={{
            padding: "0.5rem 1rem",
            background: "#007bff",
            color: "white",
            border: "none",
            borderRadius: "4px",
            cursor: "pointer",
          }}
        >
          {showForm ? "Cancel" : "+ Add Prescription"}
        </button>
      </div>

      {showForm && (
        <div
          style={{
            border: "1px solid #ddd",
            borderRadius: "8px",
            padding: "1rem",
            marginBottom: "1rem",
            background: "#f9f9f9",
          }}
        >
          <h4>New Prescription</h4>
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Drug Name *</label>
              <select
                value={drugName}
                onChange={(e) => setDrugName(e.target.value)}
                required
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              >
                <option value="">-- Select medication --</option>
                {PRESCRIPTION_MEDICATIONS.map((m) => (
                  <option key={m} value={m}>{m}</option>
                ))}
              </select>
            </div>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Dose *</label>
              <input
                type="text"
                value={dose}
                onChange={(e) => setDose(e.target.value)}
                required
                placeholder="e.g., 500mg"
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              />
            </div>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Frequency *</label>
              <input
                type="text"
                value={frequency}
                onChange={(e) => setFrequency(e.target.value)}
                required
                placeholder="e.g., BID, TID, QID"
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              />
            </div>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Duration *</label>
              <input
                type="text"
                value={duration}
                onChange={(e) => setDuration(e.target.value)}
                required
                placeholder="e.g., 7 days, 2 weeks"
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              />
            </div>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Route *</label>
              <select
                value={route}
                onChange={(e) => setRoute(e.target.value)}
                required
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              >
                <option value="oral">Oral</option>
                <option value="IV">IV</option>
                <option value="IM">IM</option>
                <option value="topical">Topical</option>
                <option value="sublingual">Sublingual</option>
                <option value="inhaled">Inhaled</option>
              </select>
            </div>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Notes</label>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={3}
                placeholder="e.g., Take with food"
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              />
            </div>
            <button
              type="submit"
              style={{
                padding: "0.5rem 1rem",
                background: "#28a745",
                color: "white",
                border: "none",
                borderRadius: "4px",
                cursor: "pointer",
              }}
            >
              Check & Submit
            </button>
          </form>

          {showConflicts && conflicts.length > 0 && (
            <div
              style={{
                marginTop: "1rem",
                padding: "1rem",
                background: "#fff3cd",
                border: "1px solid #ffc107",
                borderRadius: "4px",
              }}
            >
              <h4 style={{ color: "#856404" }}>⚠️ Conflicts Detected</h4>
              <ul style={{ margin: "0.5rem 0", paddingLeft: "1.5rem" }}>
                {conflicts.map((c, i) => (
                  <li key={i} style={{ color: "#856404" }}>
                    {c}
                  </li>
                ))}
              </ul>
              <div style={{ marginTop: "1rem" }}>
                <label style={{ display: "block", fontWeight: "bold" }}>Override Justification *</label>
                <textarea
                  value={overrideJustification}
                  onChange={(e) => setOverrideJustification(e.target.value)}
                  rows={3}
                  placeholder="Explain why you are overriding this conflict..."
                  style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
                />
              </div>
              <div style={{ marginTop: "1rem", display: "flex", gap: "0.5rem" }}>
                <button
                  onClick={handleOverride}
                  style={{
                    padding: "0.5rem 1rem",
                    background: "#dc3545",
                    color: "white",
                    border: "none",
                    borderRadius: "4px",
                    cursor: "pointer",
                  }}
                >
                  Override & Save
                </button>
                <button
                  onClick={resetForm}
                  style={{
                    padding: "0.5rem 1rem",
                    background: "#6c757d",
                    color: "white",
                    border: "none",
                    borderRadius: "4px",
                    cursor: "pointer",
                  }}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      <div>
        <h4>Current Medications</h4>
        {medications.length === 0 ? (
          <p>No medications on record.</p>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse", marginTop: "0.5rem" }}>
            <thead>
              <tr style={{ background: "#f0f0f0", borderBottom: "2px solid #ccc" }}>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Drug</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Dose</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Frequency</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Duration</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Route</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Notes</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Conflict</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {medications.map((med) => (
                <tr key={med.id} style={{ borderBottom: "1px solid #ddd" }}>
                  <td style={{ padding: "0.5rem" }}>{med.drug_name}</td>
                  <td style={{ padding: "0.5rem" }}>{med.dose}</td>
                  <td style={{ padding: "0.5rem" }}>{med.frequency}</td>
                  <td style={{ padding: "0.5rem" }}>{med.duration}</td>
                  <td style={{ padding: "0.5rem" }}>{med.route || "—"}</td>
                  <td style={{ padding: "0.5rem" }}>{med.notes || "—"}</td>
                  <td style={{ padding: "0.5rem" }}>
                    {med.conflict_flag ? (
                      <span style={{ color: "#dc3545", fontWeight: "bold" }}>
                        ⚠️ {med.conflict_details || "Conflict"}
                      </span>
                    ) : (
                      "—"
                    )}
                  </td>
                  <td style={{ padding: "0.5rem" }}>
                    <button
                      onClick={() => handleDelete(med.id)}
                      style={{
                        padding: "0.25rem 0.5rem",
                        background: "#dc3545",
                        color: "white",
                        border: "none",
                        borderRadius: "4px",
                        cursor: "pointer",
                        fontSize: "0.85rem",
                      }}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
