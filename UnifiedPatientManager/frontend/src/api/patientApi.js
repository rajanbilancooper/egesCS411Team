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
  // GET /default/patient/search?name=...
  searchByName: (name) =>
    client.get("/default/patient/search", {
      params: { name },
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

  // GET /api/patients/{patientId}/allergies
  getAllergies: (patientId) =>
    client.get(`/api/patients/${patientId}/allergies`),

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

  // PUT /api/patients/{patientId}/notes/{noteId}
  updateNote: (patientId, noteId, payload) =>
    client.put(`/api/patients/${patientId}/notes/${noteId}`, payload),

  // DELETE /api/patients/{patientId}/notes/{noteId}
  deleteNote: (patientId, noteId) =>
    client.delete(`/api/patients/${patientId}/notes/${noteId}`),
};
