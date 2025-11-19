import client from "./axiosClient";

export const noteApi = {
  // /default/patient/{patientId}/notes
  getForPatient: (patientId) =>
    client.get(`/default/patient/${patientId}/notes`),

  getById: (patientId, noteId) =>
    client.get(`/default/patient/${patientId}/notes/${noteId}`),

  create: (patientId, payload) =>
    client.post(`/default/patient/${patientId}/notes`, payload),

  update: (patientId, noteId, payload) =>
    client.put(`/default/patient/${patientId}/notes/${noteId}`, payload),

  delete: (patientId, noteId) =>
    client.delete(`/default/patient/${patientId}/notes/${noteId}`),
};