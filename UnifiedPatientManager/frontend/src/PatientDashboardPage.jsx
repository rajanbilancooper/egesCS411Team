import React, { useEffect, useState } from "react";
import { patientApi } from "./api/patientApi";
import NotesPanel from "./NotesPanel"; // <-- make sure this path is right
import ApiConnectivityBadge from "./ApiConnectivityBadge";

export default function PatientDashboardPage() {
  const [patient, setPatient] = useState(null);
  const [activeTab, setActiveTab] = useState("basic");

  useEffect(() => {
    const load = async () => {
      // using mocked API for now
      const res = await patientApi.getPatientById(1);
      setPatient(res.data);
    };
    load();
  }, []);

  if (!patient) {
    return <div style={{ padding: "2rem" }}>Loading...</div>;
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
            onClick={() => setActiveTab("basic")}
          >
            Basic Information
          </button>

          <button
            className={`upm-tab ${activeTab === "prescriptions" ? "upm-tab-active" : ""}`}
            onClick={() => setActiveTab("prescriptions")}
          >
            Prescription History
          </button>

          <button
            className={`upm-tab ${activeTab === "vaccines" ? "upm-tab-active" : ""}`}
            onClick={() => setActiveTab("vaccines")}
          >
            Vaccine record
          </button>

          <button
            className={`upm-tab ${activeTab === "appointments" ? "upm-tab-active" : ""}`}
            onClick={() => setActiveTab("appointments")}
          >
            Appointment history
          </button>

          <button
            className={`upm-tab ${activeTab === "notes" ? "upm-tab-active" : ""}`}
            onClick={() => setActiveTab("notes")}
          >
            Notes
          </button>

          <button
            className={`upm-tab ${activeTab === "history" ? "upm-tab-active" : ""}`}
            onClick={() => setActiveTab("history")}
          >
            Patient History
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

        {/* NOTES VIEW — new two-column editor/history */}
        {activeTab === "notes" && (
          <NotesPanel patientId={patient.id || 1} />
        )}

        {/* other tabs can be placeholders for now */}
        {activeTab === "prescriptions" && (
          <div className="upm-card" style={{ marginTop: "16px" }}>
            Prescription History coming soon…
          </div>
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