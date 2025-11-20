import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import client from "./api/axiosClient";
import "./PatientRegistrationPage.css";

// NOTE: Backend create endpoint currently defined at @PostMapping("/api/patients/")
// with two @RequestBody params (User, List<Allergy>) which is invalid in Spring.
// This page sends a single JSON object with user fields + optional allergies array.
// Backend may need adjustment to accept a composite DTO. For now, attempt sending user only.

export default function PatientRegistrationPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    address: "",
    dateOfBirth: "",
    gender: "MALE",
  });
  const [allergies, setAllergies] = useState([]);
  const [allergyDraft, setAllergyDraft] = useState({ substance: "", reaction: "", severity: "" });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [successId, setSuccessId] = useState(null);

  const updateField = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const addAllergy = (e) => {
    e.preventDefault();
    if (!allergyDraft.substance) return;
    setAllergies((list) => [...list, { ...allergyDraft }]);
    setAllergyDraft({ substance: "", reaction: "", severity: "" });
  };

  const removeAllergy = (idx) => {
    setAllergies((list) => list.filter((_, i) => i !== idx));
  };

  const validate = () => {
    if (!form.username.trim()) return "Username required";
    if (!form.firstName.trim() || !form.lastName.trim()) return "First and last name required";
    if (!form.email.match(/^[^@]+@[^@]+\.[^@]+$/)) return "Valid email required";
    if (!form.phoneNumber.replace(/[^0-9]/g, "").match(/^\d{10}$/)) return "Phone must be 10 digits";
    if (!form.dateOfBirth) return "Date of birth required";
    return null;
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccessId(null);
    const v = validate();
    if (v) { setError(v); return; }
    setSubmitting(true);
    try {
      // Attempt sending just user first (most likely to work with current controller)
      const payload = { ...form };
      const res = await client.post("/api/patients/", payload);
      // Expect PatientRecordDTO back; capture patientId
      setSuccessId(res.data.patientId);
    } catch (err) {
      // Retry with composite structure if first attempt fails (e.g., expecting both)
      try {
        const composite = { user: { ...form }, allergies: allergies };
        const res2 = await client.post("/api/patients/", composite);
        setSuccessId(res2.data.patientId);
      } catch (inner) {
        setError(inner.response?.data?.message || inner.message || "Registration failed");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="reg-root">
      <header className="reg-header">
        <div className="reg-logo-circle" />
        <span className="reg-title">Unified Patient Manager</span>
        <button className="reg-header-login" onClick={() => navigate("/login")}>Login</button>
      </header>
      <main className="reg-main">
        <form className="reg-card" onSubmit={onSubmit}>
          <h2 className="reg-card-title">Patient Registration</h2>
          <div className="reg-grid">
            <label className="reg-field">
              <span>Username *</span>
              <input name="username" value={form.username} onChange={updateField} />
            </label>
            <label className="reg-field">
              <span>First Name *</span>
              <input name="firstName" value={form.firstName} onChange={updateField} />
            </label>
            <label className="reg-field">
              <span>Last Name *</span>
              <input name="lastName" value={form.lastName} onChange={updateField} />
            </label>
            <label className="reg-field">
              <span>Email *</span>
              <input name="email" type="email" value={form.email} onChange={updateField} />
            </label>
            <label className="reg-field">
              <span>Phone *</span>
              <input name="phoneNumber" value={form.phoneNumber} onChange={updateField} placeholder="123-456-7890" />
            </label>
            <label className="reg-field">
              <span>Address</span>
              <input name="address" value={form.address} onChange={updateField} />
            </label>
            <label className="reg-field">
              <span>Date of Birth *</span>
              <input name="dateOfBirth" type="date" value={form.dateOfBirth} onChange={updateField} />
            </label>
            <label className="reg-field">
              <span>Gender</span>
              <select name="gender" value={form.gender} onChange={updateField}>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </label>
          </div>

          <div className="reg-section">
            <h3>Allergies (optional)</h3>
            <div className="reg-allergy-row">
              <input placeholder="Substance" value={allergyDraft.substance} onChange={(e)=>setAllergyDraft(a=>({...a, substance:e.target.value}))} />
              <input placeholder="Reaction" value={allergyDraft.reaction} onChange={(e)=>setAllergyDraft(a=>({...a, reaction:e.target.value}))} />
              <input placeholder="Severity" value={allergyDraft.severity} onChange={(e)=>setAllergyDraft(a=>({...a, severity:e.target.value}))} />
              <button type="button" className="reg-add-btn" onClick={addAllergy}>Add</button>
            </div>
            {allergies.length > 0 && (
              <ul className="reg-allergy-list">
                {allergies.map((a,i)=>(
                  <li key={i}>
                    <span>{a.substance} | {a.reaction} | {a.severity}</span>
                    <button type="button" onClick={()=>removeAllergy(i)}>✕</button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {error && <p className="reg-error">{error}</p>}
          {successId && (
            <p className="reg-success">Successfully registered patient #{successId}. <button type="button" onClick={()=>navigate(`/patients/${successId}`)}>Go to Dashboard</button></p>
          )}

          <div className="reg-actions">
            <button type="submit" className="reg-submit" disabled={submitting}>{submitting?"Submitting...":"Register"}</button>
            <button type="button" className="reg-cancel" onClick={()=>navigate("/login")}>Cancel</button>
          </div>
          <p className="reg-hint">Fields marked * are required.</p>
        </form>
      </main>
    </div>
  );
}
