// src/api/patientApi.js
import client from "./axiosClient";

/**
 * patientApi
 * Wrapper around all backend endpoints under /default/patient/...
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

  // GET /default/patient/{id}
  getPatientById: (id) =>
    client.get(`/default/patient/${id}`),

  // POST /default/patient
  createPatient: (payload) =>
    client.post("/default/patient", payload),

  // PUT /default/patient/{id}
  updatePatient: (id, payload) =>
    client.put(`/default/patient/${id}`, payload),

  // DELETE /default/patient/{id}
  deletePatient: (id) =>
    client.delete(`/default/patient/${id}`),

  // ---- Medical history, allergies, vaccines ----

  // GET /default/patient/{patientId}/medicalhistory
  getMedicalHistory: (patientId) =>
    client.get(`/default/patient/${patientId}/medicalhistory`),

  // GET /default/patient/{patientId}/allergies
  getAllergies: (patientId) =>
    client.get(`/default/patient/${patientId}/allergies`),

  // GET /default/patient/{patientId}/vaccines
  getVaccines: (patientId) =>
    client.get(`/default/patient/${patientId}/vaccines`),

  // ---- Notes ----

  // GET /default/patient/{patientId}/notes
  getNotes: (patientId) =>
    client.get(`/default/patient/${patientId}/notes`),

  // POST /default/patient/{patientId}/notes
  createNote: (patientId, payload) =>
    client.post(`/default/patient/${patientId}/notes`, payload),

  // PUT /default/patient/{patientId}/notes/{noteId}
  updateNote: (patientId, noteId, payload) =>
    client.put(`/default/patient/${patientId}/notes/${noteId}`, payload),

  // DELETE /default/patient/{patientId}/notes/{noteId}
  deleteNote: (patientId, noteId) =>
    client.delete(`/default/patient/${patientId}/notes/${noteId}`),
};
