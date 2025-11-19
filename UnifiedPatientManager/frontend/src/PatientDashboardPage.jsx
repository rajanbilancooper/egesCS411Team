import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { patientApi } from "../api/patientApi";
import NotesPanel from "../components/NotesPanel";

export default function PatientDashboardPage() {
  const { id } = useParams(); // patientId from /patients/:id
  const [patient, setPatient] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await patientApi.getPatientById(id);
        setPatient(res.data);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  if (loading) return <div>Loading...</div>;
  if (!patient) return <div>Patient not found</div>;

  return (
    <div className="patient-page">
      <header className="topbar">
        <h1>Unified Patient Manager</h1>
      </header>

      <main className="patient-layout">
        <section className="patient-left">
          <div className="card">
            <h2>{patient.fullName}</h2>
            <p>DOB: {patient.dateOfBirth}</p>
            <p>Gender: {patient.gender}</p>
          </div>
          {/* later you can add: basic info, allergies, etc. */}
        </section>

        <section className="patient-right">
          <NotesPanel patientId={id} />
        </section>
      </main>
    </div>
  );
}