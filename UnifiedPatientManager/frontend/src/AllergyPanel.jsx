import React, { useEffect, useState } from "react";
import { patientApi } from "./api/patientApi";
import { ALLERGY_SUBSTANCES } from "./constants/medicalOptions";

export default function AllergyPanel({ patientId, onAllergyChange }) {
  const [allergies, setAllergies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Form state
  const [showForm, setShowForm] = useState(false);
  const [substance, setSubstance] = useState("");
  const [otherSubstance, setOtherSubstance] = useState("");
  const [reaction, setReaction] = useState("");
  const [severity, setSeverity] = useState("Low");

  const loadAllergies = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await patientApi.getAllergies(patientId);
      setAllergies(res.data || []);
    } catch (err) {
      console.error("Failed to load allergies", err);
      setError(err?.response?.data?.message || "Failed to load allergies");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllergies();
  }, [patientId]);

  const resetForm = () => {
    setSubstance("");
    setOtherSubstance("");
    setReaction("");
    setSeverity("Low");
    setShowForm(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // If user selected "Other", require the otherSubstance text
    const finalSubstance = substance === "Other" ? otherSubstance : substance;
    if (!finalSubstance || finalSubstance.trim() === "") {
      alert("Please select or enter an allergy substance.");
      return;
    }

    const payload = {
      substance: finalSubstance,
      reaction,
      severity,
    };

    try {
      await patientApi.createAllergy(patientId, payload);
      alert("Allergy added successfully!");
      resetForm();
      loadAllergies();
      if (onAllergyChange) onAllergyChange(); // Refresh parent patient data
    } catch (err) {
      console.error("Failed to create allergy", err);
      const msg = err?.response?.data?.message || "Failed to create allergy";
      alert(msg);
    }
  };

  const handleDelete = async (allergyId) => {
    if (!window.confirm("Delete this allergy?")) return;
    try {
      await patientApi.deleteAllergy(patientId, allergyId);
      alert("Allergy deleted");
      loadAllergies();
      if (onAllergyChange) onAllergyChange(); // Refresh parent patient data
    } catch (err) {
      console.error("Failed to delete allergy", err);
      alert(err?.response?.data?.message || "Failed to delete allergy");
    }
  };

  if (loading) {
    return <div style={{ padding: "1rem" }}>Loading allergies...</div>;
  }

  if (error) {
    return <div style={{ padding: "1rem", color: "#c00" }}>Error: {error}</div>;
  }

  return (
    <div style={{ padding: "1rem" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
        <h3>Allergies</h3>
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
          {showForm ? "Cancel" : "+ Add Allergy"}
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
          <h4>New Allergy</h4>
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Substance *</label>
              <select
                value={substance}
                onChange={(e) => setSubstance(e.target.value)}
                required
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              >
                <option value="">-- Select substance --</option>
                {ALLERGY_SUBSTANCES.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
                <option value="Other">Other (specify)</option>
              </select>
              {substance === "Other" && (
                <input
                  type="text"
                  value={otherSubstance}
                  onChange={(e) => setOtherSubstance(e.target.value)}
                  placeholder="Specify other substance, e.g., Peanuts"
                  style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc", marginTop: "0.5rem" }}
                />
              )}
            </div>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Reaction</label>
              <input
                type="text"
                value={reaction}
                onChange={(e) => setReaction(e.target.value)}
                placeholder="e.g., Rash, Hives, Anaphylaxis"
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              />
            </div>
            <div style={{ marginBottom: "0.5rem" }}>
              <label style={{ display: "block", fontWeight: "bold" }}>Severity *</label>
              <select
                value={severity}
                onChange={(e) => setSeverity(e.target.value)}
                required
                style={{ width: "100%", padding: "0.5rem", borderRadius: "4px", border: "1px solid #ccc" }}
              >
                <option value="Low">Low</option>
                <option value="Medium">Medium</option>
                <option value="High">High</option>
                <option value="Critical">Critical</option>
              </select>
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
              Add Allergy
            </button>
          </form>
        </div>
      )}

      <div>
        <h4>Current Allergies</h4>
        {allergies.length === 0 ? (
          <p>No allergies on record.</p>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse", marginTop: "0.5rem" }}>
            <thead>
              <tr style={{ background: "#f0f0f0", borderBottom: "2px solid #ccc" }}>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Substance</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Reaction</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Severity</th>
                <th style={{ padding: "0.5rem", textAlign: "left" }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {allergies.map((allergy) => (
                <tr key={allergy.id} style={{ borderBottom: "1px solid #ddd" }}>
                  <td style={{ padding: "0.5rem" }}>{allergy.substance}</td>
                  <td style={{ padding: "0.5rem" }}>{allergy.reaction || "—"}</td>
                  <td style={{ padding: "0.5rem" }}>
                    <span
                      style={{
                        padding: "0.25rem 0.5rem",
                        borderRadius: "4px",
                        fontSize: "0.85rem",
                        fontWeight: "bold",
                        background:
                          allergy.severity === "Critical"
                            ? "#dc3545"
                            : allergy.severity === "High"
                            ? "#fd7e14"
                            : allergy.severity === "Medium"
                            ? "#ffc107"
                            : "#28a745",
                        color: allergy.severity === "Low" ? "#000" : "#fff",
                      }}
                    >
                      {allergy.severity || "Low"}
                    </span>
                  </td>
                  <td style={{ padding: "0.5rem" }}>
                    <button
                      onClick={() => handleDelete(allergy.id)}
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
