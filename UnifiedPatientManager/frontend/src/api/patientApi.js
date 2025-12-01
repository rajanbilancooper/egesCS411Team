// src/api/patientApi.js
import client from "./axiosClient";

/**
 * patientApi
 * Wrapper around all backend endpoints under /api/patients/...
 *
 * Usage example (from a React component):
 *   import { patientApi } from "./api/patientApi";
 *
 *   const res = await patientApi.getPatientById(1);
 *   console.log(res.data);
 */
export const patientApi = {
  // GET /api/patients/searchMany?query=... (partial matches on first or last name)
  searchByName: (query) =>
    client.get("/api/patients/searchMany", {
      params: { query },
    }),

  // GET /api/patients/search?fullName=... (exact full name match: first + last)
  searchByFullName: (fullName) =>
    client.get("/api/patients/search", {
      params: { fullName },
    }),

  // GET /api/patients/{id}
  getPatientById: (id) =>
    client.get(`/api/patients/${id}`),

  // POST /api/patients/
  createPatient: (payload) =>
    client.post("/api/patients/", payload),

  // PUT /api/patients/{id}
  updatePatient: (id, payload) =>
    client.put(`/api/patients/${id}`, payload),

  // DELETE /api/patients/{id}
  deletePatient: (id) =>
    client.delete(`/api/patients/${id}`),

  // ---- Medical history, allergies, vaccines ----

  // GET /api/patients/{patientId}/medicalhistory
  getMedicalHistory: (patientId) =>
    client.get(`/api/patients/${patientId}/medicalhistory`),
  
  // POST /api/patients/{patientId}/medicalhistory
  createMedicalHistory: (patientId, payload) =>
    client.post(`/api/patients/${patientId}/medicalhistory`, payload),

  // GET /api/patients/{patientId}/allergies
  getAllergies: (patientId) =>
    client.get(`/api/patients/${patientId}/allergies`),

  // POST /api/patients/{patientId}/allergies/add (single allergy without deleting existing)
  createAllergy: (patientId, payload) =>
    client.post(`/api/patients/${patientId}/allergies/add`, payload),

  // DELETE /api/patients/{patientId}/allergies/{allergyId}
  deleteAllergy: (patientId, allergyId) =>
    client.delete(`/api/patients/${patientId}/allergies/${allergyId}`),

  // GET /api/patients/{patientId}/vaccines
  getVaccines: (patientId) =>
    client.get(`/api/patients/${patientId}/vaccines`),

  // ---- Notes ----

  // GET /api/patients/{patientId}/notes
  getNotes: (patientId) =>
    client.get(`/api/patients/${patientId}/notes`),

  // POST /api/patients/{patientId}/notes
  createNote: (patientId, payload) =>
    client.post(`/api/patients/${patientId}/notes`, payload),

  // Multipart variant: @RequestPart("note") JSON + optional @RequestPart("attachment") file
  createNoteMultipart: (patientId, noteDto, file) => {
    const form = new FormData();
    // append JSON as Blob so backend parses as application/json
    const noteBlob = new Blob([JSON.stringify(noteDto)], { type: "application/json" });
    form.append("note", noteBlob);
    if (file) {
      form.append("attachment", file, file.name);
    }
    return client.post(`/api/patients/${patientId}/notes`, form, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  // PUT /api/patients/{patientId}/notes/{noteId}
  updateNote: (patientId, noteId, payload) =>
    client.put(`/api/patients/${patientId}/notes/${noteId}`, payload),

  // DELETE /api/patients/{patientId}/notes/{noteId}
  deleteNote: (patientId, noteId) =>
    client.delete(`/api/patients/${patientId}/notes/${noteId}`),

  // GET /api/patients/{patientId}/notes/{noteId}/download (returns blob)
  downloadNoteAttachment: (patientId, noteId) =>
    client.get(`/api/patients/${patientId}/notes/${noteId}/download`, {
      responseType: "blob",
    }),

  // ---- Medications / Prescriptions ----

  // GET /api/patients/{patientId}/medications
  getMedications: (patientId) =>
    client.get(`/api/patients/${patientId}/medications`),

  // POST /api/patients/{patientId}/providers/{providerId}/prescriptions
  // Creates a single prescription with conflict check
  // payload: { drug_name, dose, frequency, duration, route, notes, override?, override_justification? }
  createPrescription: (patientId, providerId, payload) =>
    client.post(`/api/patients/${patientId}/providers/${providerId}/prescriptions`, payload),

  // PUT /api/patients/{patientId}/providers/{providerId}/medications/{medicationId}
  updateMedication: (patientId, providerId, medicationId, payload) =>
    client.put(`/api/patients/${patientId}/providers/${providerId}/medications/${medicationId}`, payload),

  // DELETE /api/patients/{patientId}/medications/{medicationId}
  deleteMedication: (patientId, medicationId) =>
    client.delete(`/api/patients/${patientId}/medications/${medicationId}`),
};
