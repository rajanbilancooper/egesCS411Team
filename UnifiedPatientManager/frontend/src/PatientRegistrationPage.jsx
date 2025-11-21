import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import client from "./api/axiosClient";
import { ALLERGY_SUBSTANCES } from "./constants/medicalOptions";
import "./PatientRegistrationPage.css";

// NOTE: Backend create endpoint currently defined at @PostMapping("/api/patients/")
// with two @RequestBody params (User, List<Allergy>) which is invalid in Spring.
// This page sends a single JSON object with user fields + optional allergies array.
// Backend may need adjustment to accept a composite DTO. For now, attempt sending user only.

export default function PatientRegistrationPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    password: "", // allow password creation now that DTO supports it
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    address: "",
    dateOfBirth: "",
    gender: "MALE",
    height: "",
    weight: "",
  });
  const [allergies, setAllergies] = useState([]);
  const [allergyDraft, setAllergyDraft] = useState({ substance: "", reaction: "", severity: "Low" });
  const [otherAllergy, setOtherAllergy] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [successId, setSuccessId] = useState(null);

  const updateField = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const addAllergy = (e) => {
    e.preventDefault();
    const finalSubstance = allergyDraft.substance === "Other" ? otherAllergy : allergyDraft.substance;
    if (!finalSubstance || finalSubstance.trim() === "") {
      return;
    }
    setAllergies((list) => [...list, { substance: finalSubstance, reaction: allergyDraft.reaction, severity: allergyDraft.severity || "Low" }]);
    setAllergyDraft({ substance: "", reaction: "", severity: "Low" });
    setOtherAllergy("");
  };

  const removeAllergy = (idx) => {
    setAllergies((list) => list.filter((_, i) => i !== idx));
  };

  const validate = () => {
    if (!form.username.trim()) return "Username required";
    if (!form.password.trim() || form.password.length < 6) return "Password (min 6 chars) required";
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
      const payload = { ...form, allergies: allergies };
      const res = await client.post("/api/patients/", payload);
      const pid = res.data.patientId;
      setSuccessId(pid);
      // immediate redirect after short delay for user feedback
      setTimeout(() => {
        navigate(`/patients/${pid}`);
      }, 600);
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Registration failed");
    }
    setSubmitting(false);
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
              <span>Password *</span>
              <input name="password" type="password" value={form.password} onChange={updateField} />
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
            <label className="reg-field">
              <span>Height</span>
              <input name="height" value={form.height} onChange={updateField} placeholder="e.g., 5'10&quot; or 178cm" />
            </label>
            <label className="reg-field">
              <span>Weight</span>
              <input name="weight" value={form.weight} onChange={updateField} placeholder="e.g., 180 lbs or 82 kg" />
            </label>
          </div>

          <div className="reg-section">
            <h3>Allergies (optional)</h3>
            <div className="reg-allergy-row">
              <select value={allergyDraft.substance} onChange={(e)=>setAllergyDraft(a=>({...a, substance:e.target.value}))}>
                <option value="">-- Select substance --</option>
                {ALLERGY_SUBSTANCES.map(s=> (
                  <option key={s} value={s}>{s}</option>
                ))}
                <option value="Other">Other (specify)</option>
              </select>
              {allergyDraft.substance === "Other" && (
                <input placeholder="Specify other substance" value={otherAllergy} onChange={(e)=>setOtherAllergy(e.target.value)} />
              )}
              <input placeholder="Reaction" value={allergyDraft.reaction} onChange={(e)=>setAllergyDraft(a=>({...a, reaction:e.target.value}))} />
              <select value={allergyDraft.severity} onChange={(e)=>setAllergyDraft(a=>({...a, severity:e.target.value}))}>
                <option value="Low">Low</option>
                <option value="Medium">Medium</option>
                <option value="High">High</option>
                <option value="Critical">Critical</option>
              </select>
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
