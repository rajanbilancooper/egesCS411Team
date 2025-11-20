import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { patientApi } from "./api/patientApi";
import NotesPanel from "./NotesPanel";
import PrescriptionPanel from "./PrescriptionPanel";
import AllergyPanel from "./AllergyPanel";
import MedicalHistoryPanel from "./MedicalHistoryPanel";
import ApiConnectivityBadge from "./ApiConnectivityBadge";

export default function PatientDashboardPage() {
  const navigate = useNavigate();
  const { id: routeId } = useParams();
  const patientId = routeId ? Number(routeId) : null;

  const [patient, setPatient] = useState(null);
  const [activeTab, setActiveTab] = useState("basic");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // Search state
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState(null);
  const [showResults, setShowResults] = useState(false);
  const [searchDebounceId, setSearchDebounceId] = useState(null);
  const [isEditingRecord, setIsEditingRecord] = useState(false);
  const [editedPatient, setEditedPatient] = useState(null);
  const [saveError, setSaveError] = useState(null);
  const [saving, setSaving] = useState(false);

  const handleTabClick = (tab) => {
    setActiveTab(tab);
  };

  const refreshPatientData = async () => {
    if (!patientId || Number.isNaN(patientId)) return;
    try {
      const res = await patientApi.getPatientById(patientId);
      setPatient(res.data);
    } catch (err) {
      console.error("Failed to refresh patient data", err);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      // No token => force re-auth
      navigate("/login", { replace: true });
      return;
    }
    if (!patientId || Number.isNaN(patientId)) {
      setError("No patient id supplied in route.");
      setLoading(false);
      return;
    }
    const load = async () => {
      setLoading(true);
      try {
        const res = await patientApi.getPatientById(patientId);
        setPatient(res.data);
        setError(null);
      } catch (err) {
        console.error("Failed to load patient", err);
        const msg = err?.response?.data?.message || err?.message || "Failed to fetch patient";
        setError(msg);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [patientId, navigate]);

  // Perform debounced search when searchTerm changes
  useEffect(() => {
    if (!searchTerm || searchTerm.trim().length < 2) {
      setSearchResults([]);
      setShowResults(false);
      setSearchError(null);
      return;
    }
    if (searchDebounceId) {
      clearTimeout(searchDebounceId);
    }
    const id = setTimeout(async () => {
      setSearchLoading(true);
      setSearchError(null);
      try {
        const res = await patientApi.searchByName(searchTerm.trim());
        const data = res.data;
        let list = [];
        if (Array.isArray(data)) {
          list = data.map(p => ({
            id: p.patientId ?? p.id,
            name: p.fullName || (p.firstName ? `${p.firstName} ${p.lastName || ""}`.trim() : (p.username || "Unknown"))
          }));
        } else if (data && typeof data === 'object') {
          list = [{
            id: data.patientId ?? data.id,
            name: data.fullName || (data.firstName ? `${data.firstName} ${data.lastName || ""}`.trim() : (data.username || "Unknown"))
          }];
        }
        setSearchResults(list);
        setShowResults(true);
      } catch (err) {
        console.error("Search failed", err);
        setSearchError(err?.response?.data?.message || err?.message || "Search failed");
        setSearchResults([]);
        setShowResults(true);
      } finally {
        setSearchLoading(false);
      }
    }, 300); // 300ms debounce
    setSearchDebounceId(id);
  }, [searchTerm]);

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleSelectPatient = (id) => {
    setShowResults(false);
    setSearchTerm("");
    navigate(`/patients/${id}`);
  };

  const startEditRecord = () => {
    setEditedPatient({ ...patient });
    setIsEditingRecord(true);
    setSaveError(null);
  };

  const cancelEditRecord = () => {
    setEditedPatient(null);
    setIsEditingRecord(false);
    setSaveError(null);
  };

  const handleEditChange = (field, value) => {
    setEditedPatient((prev) => ({ ...(prev || {}), [field]: value }));
  };

  const saveEditedRecord = async () => {
    if (!patientId) return;
    setSaving(true);
    setSaveError(null);
    try {
      // call backend update
      const payload = { ...editedPatient };
      await patientApi.updatePatient(patientId, payload);
      // refresh local patient data from server
      await refreshPatientData();
      setIsEditingRecord(false);
      setEditedPatient(null);
    } catch (err) {
      console.error("Failed to save patient", err);
      setSaveError(err?.response?.data?.message || err?.message || "Failed to save patient");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div style={{ padding: "2rem" }}>Loading patient...</div>;
  }

  if (error) {
    return (
      <div style={{ padding: "2rem" }}>
        <h2 style={{ color: "#c00" }}>Unable to load patient</h2>
        <p>{error}</p>
        <button onClick={() => navigate("/login")}>Back to Login</button>
      </div>
    );
  }

  if (!patient) {
    return (
      <div style={{ padding: "2rem" }}>
        <p>No patient data returned.</p>
        <button onClick={() => navigate(`/patients/${patientId || 1}`)}>Retry</button>
      </div>
    );
  }

  return (
    <div className="upm-root">
      <header className="upm-header">
        <div className="upm-logo" />
        <span className="upm-header-title">Unified Patient Manager</span>
        <ApiConnectivityBadge />
        
      </header>

      {/* 2. MAIN AREA */}
      <main className="upm-main">
        {/* SEARCH BAR */}
        <div className="upm-search-row" style={{ position: "relative" }}>
          <input
            className="upm-search-input"
            placeholder="Search or Enter Patient Name"
            value={searchTerm}
            onChange={handleSearchChange}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                if (searchResults.length > 0) {
                  e.preventDefault();
                  handleSelectPatient(searchResults[0].id);
                }
              }
            }}
            onFocus={() => { if (searchResults.length > 0) setShowResults(true); }}
            onBlur={() => {
              // slight delay so click can register
              setTimeout(() => setShowResults(false), 150);
            }}
          />
          {showResults && (
            <div className="upm-search-results" style={{ position: "absolute", top: "100%", left: 0, right: 0, background: "#fff", border: "1px solid #ddd", borderRadius: 4, zIndex: 20, maxHeight: 240, overflowY: "auto" }}>
              {searchLoading && <div style={{ padding: "0.5rem" }}>Searching...</div>}
              {!searchLoading && searchError && <div style={{ padding: "0.5rem", color: "#c00" }}>{searchError}</div>}
              {!searchLoading && !searchError && searchResults.length === 0 && (
                <div style={{ padding: "0.5rem" }}>No matches</div>
              )}
              {!searchLoading && !searchError && searchResults.map(r => (
                <button
                  key={r.id}
                  onMouseDown={(e) => { e.preventDefault(); handleSelectPatient(r.id); }}
                  style={{
                    display: "block",
                    width: "100%",
                    textAlign: "left",
                    padding: "0.5rem 0.75rem",
                    border: "none",
                    background: "#fff",
                    cursor: "pointer",
                    borderBottom: "1px solid #eee"
                  }}
                  className="upm-search-result-item"
                >
                  {r.name} <span style={{ opacity: 0.6 }}>#{r.id}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* 2b. TABS ROW */}
        <div className="upm-tabs">
          <button
            className={`upm-tab ${activeTab === "basic" ? "upm-tab-active" : ""}`}
            onClick={() => handleTabClick("basic")}
          >
            Basic Information
          </button>

          <button
            className={`upm-tab ${activeTab === "prescriptions" ? "upm-tab-active" : ""}`}
            onClick={() => handleTabClick("prescriptions")}
          >
            Prescription History
          </button>

          <button
            className={`upm-tab ${activeTab === "allergies" ? "upm-tab-active" : ""}`}
            onClick={() => handleTabClick("allergies")}
          >
            Allergies
          </button>

          <button
            className={`upm-tab ${activeTab === "appointments" ? "upm-tab-active" : ""}`}
            onClick={() => handleTabClick("appointments")}
          >
            Appointment history
          </button>

          <button
            className={`upm-tab ${activeTab === "notes" ? "upm-tab-active" : ""}`}
            onClick={() => { console.log("switch tab -> notes"); handleTabClick("notes"); }}
          >
            Notes
          </button>

          <button
            className={`upm-tab ${activeTab === "history" ? "upm-tab-active" : ""}`}
            onClick={() => handleTabClick("history")}
          >
            Patient History
          </button>

          <button
            className={`upm-tab ${activeTab === "diagnoses" ? "upm-tab-active" : ""}`}
            onClick={() => handleTabClick("diagnoses")}
          >
            Diagnoses
          </button>

          <button
            className={`upm-tab ${activeTab === "record" ? "upm-tab-active" : ""}`}
            onClick={() => handleTabClick("record")}
          >
            Patient Record
          </button>
        </div>

        {/* 3. CONTENT UNDER TABS */}

        {/* BASIC INFO VIEW — use your existing two-card layout here */}
        {activeTab === "basic" && (
          <div className="upm-layout">
            <section className="upm-card upm-card-left">
              <div className="upm-patient-top">
                <div className="upm-avatar-circle" />
                <div className="upm-patient-main">
                  <h2 className="upm-patient-name">
                    {(patient.firstName || "").trim()} {(patient.lastName || "").trim()}
                  </h2>
                  <div className="upm-patient-meta">
                    <p>
                      <strong>DOB:</strong>{" "}
                      {patient.dateOfBirth ? new Date(patient.dateOfBirth).toLocaleDateString() : "—"}
                    </p>
                    <p>
                      <strong>Gender:</strong> {patient.gender || "—"}
                    </p>
                    <p>
                      <strong>Contact:</strong>{" "}
                      {patient.phoneNumber || "—"}
                    </p>
                    <p>
                      <strong>Address:</strong>{" "}
                      {patient.address || "—"}
                    </p>
                  </div>
                </div>
              </div>

              <div className="upm-basic-info">
                <h3>Basic Information</h3>
                <div className="upm-divider" />
                <div className="upm-basic-grid">
                  <p>
                    <strong>Height:</strong> {patient.height || "—"}
                  </p>
                  <p>
                    <strong>Weight:</strong> {patient.weight || "—"}
                  </p>
                </div>
                <p>
                  <strong>Allergies:</strong>{" "}
                  {Array.isArray(patient.allergies) && patient.allergies.length > 0
                    ? patient.allergies.map(a => a.substance).join(", ")
                    : "None recorded"}
                </p>
                <p>
                  <strong>Current Medications:</strong>{" "}
                  {Array.isArray(patient.medications) && patient.medications.length > 0
                    ? patient.medications.map(m => m.drugName).join(", ")
                    : "None recorded"}
                </p>
              </div>
            </section>

            <section className="upm-card upm-card-right">
              <div className="upm-section-block">
                <h3>Overview</h3>
                <div className="upm-divider" />
                <p style={{ whiteSpace: 'pre-line', opacity: 0.85 }}>
                  This panel will later show appointment summaries and key history.
                </p>
              </div>
              <div className="upm-section-block">
                <h3>Summary Counts</h3>
                <div className="upm-divider" />
                <p><strong>Allergies:</strong> {Array.isArray(patient.allergies) ? patient.allergies.length : 0}</p>
                <p><strong>Medications:</strong> {Array.isArray(patient.medications) ? patient.medications.length : 0}</p>
              </div>
            </section>
          </div>
        )}

        {/* RECORD VIEW — aggregated DTO details */}
        {activeTab === "record" && (
          <div className="upm-card" style={{ marginTop: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <h3 style={{ margin: 0 }}>Patient Record</h3>
              {!isEditingRecord ? (
                <div>
                  <button className="upm-tab" onClick={startEditRecord}>Edit</button>
                </div>
              ) : (
                <div>
                  <button className="upm-tab" onClick={saveEditedRecord} disabled={saving}>{saving ? 'Saving...' : 'Save'}</button>
                  <button className="upm-tab" onClick={cancelEditRecord} style={{ marginLeft: 8 }} disabled={saving}>Cancel</button>
                </div>
              )}
            </div>

            <div className="upm-divider" />

            <div style={{ marginTop: 8 }}>
              <p><strong>Patient ID:</strong> {patient.patientId ?? patient.id}</p>

              <p>
                <strong>Name:</strong>{' '}
                {!isEditingRecord ? (
                  <>{patient.firstName || patient.fullName} {patient.lastName}</>
                ) : (
                  <span>
                    <input type="text" value={editedPatient?.firstName || ''} onChange={(e) => handleEditChange('firstName', e.target.value)} placeholder="First name" />
                    {' '}
                    <input type="text" value={editedPatient?.lastName || ''} onChange={(e) => handleEditChange('lastName', e.target.value)} placeholder="Last name" />
                  </span>
                )}
              </p>

              <p>
                <strong>Email:</strong>{' '}
                {!isEditingRecord ? (
                  patient.email
                ) : (
                  <input type="email" value={editedPatient?.email || ''} onChange={(e) => handleEditChange('email', e.target.value)} />
                )}
              </p>

              <p>
                <strong>Phone:</strong>{' '}
                {!isEditingRecord ? (
                  patient.phoneNumber
                ) : (
                  <input type="text" value={editedPatient?.phoneNumber || ''} onChange={(e) => handleEditChange('phoneNumber', e.target.value)} />
                )}
              </p>

              <p>
                <strong>Address:</strong>{' '}
                {!isEditingRecord ? (
                  patient.address
                ) : (
                  <input type="text" value={editedPatient?.address || ''} onChange={(e) => handleEditChange('address', e.target.value)} style={{ width: '60%' }} />
                )}
              </p>

              <p>
                <strong>Date of Birth:</strong>{' '}
                {!isEditingRecord ? (
                  (patient.dateOfBirth ? new Date(patient.dateOfBirth).toLocaleString() : "")
                ) : (
                  <input type="date" value={editedPatient?.dateOfBirth ? new Date(editedPatient.dateOfBirth).toISOString().slice(0,10) : ''} onChange={(e) => handleEditChange('dateOfBirth', e.target.value)} />
                )}
              </p>

              <p>
                <strong>Gender:</strong>{' '}
                {!isEditingRecord ? (
                  patient.gender
                ) : (
                  <select value={editedPatient?.gender || ''} onChange={(e) => handleEditChange('gender', e.target.value)}>
                    <option value="">--</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                )}
              </p>

              <p>
                <strong>Height:</strong>{' '}
                {!isEditingRecord ? (
                  patient.height || '—'
                ) : (
                  <input type="text" value={editedPatient?.height || ''} onChange={(e) => handleEditChange('height', e.target.value)} />
                )}
              </p>

              <p>
                <strong>Weight:</strong>{' '}
                {!isEditingRecord ? (
                  patient.weight || '—'
                ) : (
                  <input type="text" value={editedPatient?.weight || ''} onChange={(e) => handleEditChange('weight', e.target.value)} />
                )}
              </p>

              {saveError && <p style={{ color: '#c00' }}>{saveError}</p>}

              <div className="upm-divider" />
              <p><strong>Allergies:</strong></p>
              <ul>
                {(patient.allergies || []).map((a) => (
                  <li key={a.allergyId}>{a.substance} {a.severity ? `(${a.severity})` : ""}</li>
                ))}
              </ul>
              <p><strong>Medications:</strong></p>
              <ul>
                {(patient.medications || []).map((m) => (
                  <li key={m.medicationId}>{m.drugName} {m.dose ? `– ${m.dose}` : ""} {m.frequency || ""}</li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {/* NOTES VIEW — new two-column editor/history */}
        {activeTab === "notes" && (
            <NotesPanel patientId={patient.id || patient.patientId || patientId || 1} />
        )}

        {/* other tabs can be placeholders for now */}
        {activeTab === "prescriptions" && (
          <PrescriptionPanel patientId={patient.id || patient.patientId || patientId || 1} />
        )}
        {activeTab === "allergies" && (
          <AllergyPanel patientId={patient.id || patient.patientId || patientId || 1} onAllergyChange={refreshPatientData} />
        )}
        {activeTab === "appointments" && (
          <div className="upm-card" style={{ marginTop: "16px" }}>
            Appointment History coming soon…
          </div>
        )}
        {activeTab === "history" && (
          <div className="upm-card" style={{ marginTop: "16px" }}>
            Patient History coming soon…
          </div>
        )}
        {activeTab === "diagnoses" && (
          <MedicalHistoryPanel patientId={patient.id || patient.patientId || patientId || 1} onChange={refreshPatientData} />
        )}
      </main>
    </div>
  );
}
