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
  const [latestNote, setLatestNote] = useState(null);
  const [latestDiagnosis, setLatestDiagnosis] = useState(null);
  const [overviewLoading, setOverviewLoading] = useState(false);
  const [overviewError, setOverviewError] = useState(null);

  const handleTabClick = (tab) => {
    setActiveTab(tab);
  };

  const refreshPatientData = async () => {
    if (!patientId || Number.isNaN(patientId)) return;
    try {
      const res = await patientApi.getPatientById(patientId);
      setPatient(res.data);
      // Also refresh latest note & diagnosis when record refreshes
      await loadOverviewData(patientId);
    } catch (err) {
      console.error("Failed to refresh patient data", err);
    }
  };

  const pickLatestByTimestamp = (items, timestampField) => {
    if (!Array.isArray(items) || items.length === 0) return null;
    // Prefer records with a real timestamp; if none have one, fall back to highest id or array order
    const withTs = items.filter(it => !!it[timestampField]);
    if (withTs.length > 0) {
      return withTs.slice().sort((a,b) => new Date(b[timestampField]).getTime() - new Date(a[timestampField]).getTime())[0];
    }
    // Fallback: try by id descending
    const withId = items.filter(it => typeof it.id === 'number');
    if (withId.length > 0) {
      return withId.slice().sort((a,b) => b.id - a.id)[0];
    }
    return items[0];
  };

  const loadOverviewData = async (pid) => {
    setOverviewLoading(true);
    setOverviewError(null);
    try {
      // Fetch notes & medical history in parallel
      const [notesRes, historyRes] = await Promise.allSettled([
        patientApi.getNotes(pid),
        patientApi.getMedicalHistory(pid)
      ]);

      if (notesRes.status === 'fulfilled') {
        const notesArr = Array.isArray(notesRes.value.data) ? notesRes.value.data : [];
        // latest note by timestamp (or fallback to first)
        const latest = pickLatestByTimestamp(notesArr, 'timestamp') || notesArr[0] || null;
        setLatestNote(latest);
      } else {
        console.warn('Failed to load notes for overview', notesRes.reason);
      }

      if (historyRes.status === 'fulfilled') {
        const raw = historyRes.value.data;
        const histArr = Array.isArray(raw) ? raw : [];
        // Strategy: prefer highest id (creation order) rather than date-only startDate (no time component)
        // If startDate present for all, still rely on id to break ties.
        let latestHist = null;
        if (histArr.length > 0) {
          latestHist = histArr.slice().sort((a,b) => {
            const idA = typeof a.id === 'number' ? a.id : -1;
            const idB = typeof b.id === 'number' ? b.id : -1;
            return idB - idA; // descending by id
          })[0];
        }
        // Normalize field for rendering: ensure 'diagnosis' exists even if backend changed naming
        if (latestHist && !latestHist.diagnosis && latestHist.notes) {
          latestHist.diagnosis = latestHist.notes;
        }
        setLatestDiagnosis(latestHist);
      } else {
        console.warn('Failed to load medical history for overview', historyRes.reason);
      }
    } catch (e) {
      console.error('Overview load error', e);
      setOverviewError(e?.message || 'Failed to load overview data');
    } finally {
      setOverviewLoading(false);
    }
  };

  // Exposed wrapper used by child panels to refresh latest overview fields without full patient reload
  const refreshOverviewOnly = async () => {
    if (!patientId) return;
    await loadOverviewData(patientId);
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
        await loadOverviewData(patientId);
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
    if (!searchTerm || searchTerm.trim().length < 1) {
      setSearchResults([]);
      setShowResults(false);
      setSearchError(null);
      return;
    }

    let timer = setTimeout(async () => {
      setSearchLoading(true);
      setSearchError(null);
      const term = searchTerm.trim();
      try {
        const tasks = [];
        tasks.push(patientApi.searchByName(term));
        const wantFull = term.includes(" ");
        if (wantFull) tasks.push(patientApi.searchByFullName(term));

        const settled = await Promise.allSettled(tasks);
        const partialRes = settled[0].status === 'fulfilled' ? settled[0].value : null;
        const fullRes = (wantFull && settled[1] && settled[1].status === 'fulfilled') ? settled[1].value : null;
        const fullErr = (wantFull && settled[1] && settled[1].status === 'rejected') ? settled[1].reason : null;

        const partialData = partialRes && Array.isArray(partialRes.data) ? partialRes.data : [];
        let fullData = [];
        if (fullRes) {
          if (Array.isArray(fullRes.data)) fullData = fullRes.data; else if (fullRes.data) fullData = [fullRes.data];
        }

        const mergedMap = new Map();
        [...partialData, ...fullData].forEach(p => {
            const pid = p.patientId ?? p.id;
            if (!mergedMap.has(pid)) mergedMap.set(pid, p);
        });
        const list = Array.from(mergedMap.values()).map(p => ({
          id: p.patientId ?? p.id,
          name: (p.firstName ? `${p.firstName} ${p.lastName || ""}`.trim() : (p.username || "Unknown")),
          exact: fullData.some(fd => (fd.patientId ?? fd.id) === (p.patientId ?? p.id))
        }));
        list.sort((a,b) => (a.exact === b.exact ? a.name.localeCompare(b.name) : a.exact ? -1 : 1));

        setSearchResults(list);
        // Show results if we have any or if exact search errored while partial gave none (so we can display hint)
        setShowResults(list.length > 0 || !!fullErr);
        if (fullErr && list.length === 0) {
          // Specific message for no exact match
          setSearchError('No exact full-name match');
        }
      } catch (outer) {
        console.error('Search failed (outer)', outer);
        setSearchError(outer?.response?.data?.message || outer?.message || 'Search failed');
        setSearchResults([]);
        setShowResults(true);
      } finally {
        setSearchLoading(false);
      }
    }, 300); // allow both requests to settle

    return () => clearTimeout(timer);
  }, [searchTerm]);

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleSelectPatient = (id) => {
    const numericId = Number(id);
    if (Number.isNaN(numericId)) {
      console.warn("Selected patient id is not numeric", id);
      return;
    }
    setShowResults(false);
    setSearchTerm("");
    navigate(`/patients/${numericId}`);
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
                    background: r.exact ? "#eef" : "#fff",
                    cursor: "pointer",
                    borderBottom: "1px solid #eee"
                  }}
                  className="upm-search-result-item"
                >
                  {r.name} <span style={{ opacity: 0.6 }}>#{r.id}</span>{" "}
                  {r.exact && <span style={{ color: "#336", fontSize: 12 }}> (exact)</span>}
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
                {overviewLoading && <p>Loading overview…</p>}
                {overviewError && <p style={{ color: '#c00' }}>Overview error: {overviewError}</p>}
                {!overviewLoading && !overviewError && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div>
                      <strong>Most Recent Note:</strong>
                      {latestNote ? (
                        latestNote.noteType === 'FILE' || latestNote.noteType === 'FILE' ? (
                          <div style={{ marginTop: 4 }}>
                            Attachment: {latestNote.attachmentName || 'attachment.bin'}
                            {latestNote.timestamp && (
                              <div style={{ fontSize: 12, opacity: 0.7 }}>
                                {new Date(latestNote.timestamp).toLocaleString()}
                              </div>
                            )}
                          </div>
                        ) : (
                          <div style={{ marginTop: 4 }}>
                            {latestNote.content && latestNote.content.length > 160
                              ? latestNote.content.slice(0,160) + '…'
                              : (latestNote.content || '—')}
                            {latestNote.timestamp && (
                              <div style={{ fontSize: 12, opacity: 0.7 }}>
                                {new Date(latestNote.timestamp).toLocaleString()}
                              </div>
                            )}
                          </div>
                        )
                      ) : (
                        <span style={{ marginLeft: 6, opacity: 0.6 }}>No notes yet</span>
                      )}
                    </div>
                    <div>
                      <strong>Most Recent Diagnosis:</strong>
                      {latestDiagnosis ? (
                        <div style={{ marginTop: 4 }}>
                          {(latestDiagnosis.diagnosis && latestDiagnosis.diagnosis.length > 160)
                            ? latestDiagnosis.diagnosis.slice(0,160) + '…'
                            : (latestDiagnosis.diagnosis || '—')}
                          {latestDiagnosis.startDate && (
                            <div style={{ fontSize: 12, opacity: 0.7 }}>
                              {new Date(latestDiagnosis.startDate).toLocaleDateString()}
                            </div>
                          )}
                        </div>
                      ) : (
                        <span style={{ marginLeft: 6, opacity: 0.6 }}>No diagnoses yet</span>
                      )}
                    </div>
                  </div>
                )}
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
            <NotesPanel
              patientId={patient.id || patient.patientId || patientId || 1}
              onNotesChanged={refreshOverviewOnly}
            />
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
          <MedicalHistoryPanel
            patientId={patient.id || patient.patientId || patientId || 1}
            onChange={refreshPatientData}
            onPrescribeRequested={() => setActiveTab('prescriptions')}
          />
        )}
      </main>
    </div>
  );
}
