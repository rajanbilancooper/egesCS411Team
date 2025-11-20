import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { patientApi } from "./api/patientApi";
import NotesPanel from "./NotesPanel";
import PrescriptionPanel from "./PrescriptionPanel";
import ApiConnectivityBadge from "./ApiConnectivityBadge";

export default function PatientDashboardPage() {
  const navigate = useNavigate();
  const { id: routeId } = useParams();
  const patientId = routeId ? Number(routeId) : null;

  const [patient, setPatient] = useState(null);
  const [activeTab, setActiveTab] = useState("basic");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const handleTabClick = (tab) => {
    setActiveTab(tab);
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
        <div style={{ marginLeft: "auto" }}>
          <button className="upm-tab" onClick={() => setActiveTab("record")}>View Patient Record</button>
        </div>
      </header>

      {/* 2. MAIN AREA */}
      <main className="upm-main">
        {/* SEARCH BAR */}
        <div className="upm-search-row">
          <input
            className="upm-search-input"
            placeholder="Search or Enter Patient Name"
          />
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
            className={`upm-tab ${activeTab === "vaccines" ? "upm-tab-active" : ""}`}
            onClick={() => handleTabClick("vaccines")}
          >
            Vaccine record
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
                    {patient.fullName || "Rajan Bilan-Cooper"}
                  </h2>
                  <div className="upm-patient-meta">
                    <p>
                      <strong>DOB:</strong>{" "}
                      {patient.dateOfBirth || "1/1/98"}
                    </p>
                    <p>
                      <strong>Gender:</strong> {patient.gender || "MALE"}
                    </p>
                    <p>
                      <strong>Contact:</strong>{" "}
                      {patient.contact || "408-382-3049"}
                    </p>
                  </div>
                </div>
              </div>

              <div className="upm-basic-info">
                <h3>Basic Information</h3>
                <div className="upm-divider" />
                <div className="upm-basic-grid">
                  <p>
                    <strong>Height:</strong> {patient.height || "6'4"}
                  </p>
                  <p>
                    <strong>Weight:</strong> {patient.weight || "180 lbs"}
                  </p>
                </div>
                <p>
                  <strong>Insurance:</strong>{" "}
                  {patient.insurance || "Cigna"}
                </p>
                <p>
                  <strong>Allergies:</strong>{" "}
                  {patient.allergiesSummary ||
                    "Peanuts, Penicillin, Grass"}
                </p>
                <p>
                  <strong>Current medication:</strong>{" "}
                  {patient.currentMedicationSummary || "N/A"}
                </p>
              </div>
            </section>

            <section className="upm-card upm-card-right">
              <div className="upm-section-block">
                <h3>
                  Last Appointment (
                  {patient.lastAppointmentDate || "09/22/25"})
                </h3>
                <div className="upm-divider" />
                <p>
                  <strong>Chief Complaint:</strong>{" "}
                  {patient.lastChiefComplaint || "Knee pain after running"}
                </p>
                <p>
                  <strong>Diagnosis/Assessment:</strong>{" "}
                  {patient.lastDiagnosis || "Likely mild tendonitis"}
                </p>
                <p>
                  <strong>Next-Steps:</strong>{" "}
                  {patient.lastNextSteps ||
                    "Rest, ice, avoid running for 1–2 weeks, consider physical therapy if not improved"}
                </p>
              </div>

              <div className="upm-section-block">
                <h3>Important Medical History</h3>
                <div className="upm-divider" />
                <p>
                  {patient.importantHistory ||
                    "Asthma (childhood, mild, controlled)\nFamily history of diabetes"}
                </p>
              </div>
            </section>
          </div>
        )}

        {/* RECORD VIEW — aggregated DTO details */}
        {activeTab === "record" && (
          <div className="upm-card" style={{ marginTop: 16 }}>
            <h3>Patient Record</h3>
            <div className="upm-divider" />
            <p><strong>Patient ID:</strong> {patient.patientId ?? patient.id}</p>
            <p><strong>Name:</strong> {patient.firstName || patient.fullName} {patient.lastName}</p>
            <p><strong>Email:</strong> {patient.email}</p>
            <p><strong>Phone:</strong> {patient.phoneNumber}</p>
            <p><strong>Address:</strong> {patient.address}</p>
            <p><strong>Date of Birth:</strong> {patient.dateOfBirth ? new Date(patient.dateOfBirth).toLocaleString() : ""}</p>
            <p><strong>Gender:</strong> {patient.gender}</p>
            <p><strong>Height:</strong> {patient.height || "—"}</p>
            <p><strong>Weight:</strong> {patient.weight || "—"}</p>
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
        )}

        {/* NOTES VIEW — new two-column editor/history */}
        {activeTab === "notes" && (
            <NotesPanel patientId={patient.id || patient.patientId || patientId || 1} />
        )}

        {/* other tabs can be placeholders for now */}
        {activeTab === "prescriptions" && (
          <PrescriptionPanel patientId={patient.id || patient.patientId || patientId || 1} />
        )}
        {activeTab === "vaccines" && (
          <div className="upm-card" style={{ marginTop: "16px" }}>
            Vaccine Record coming soon…
          </div>
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
      </main>
    </div>
  );
}
