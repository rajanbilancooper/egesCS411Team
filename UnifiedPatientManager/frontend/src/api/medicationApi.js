import client from "./axiosClient";

export const medicationApi = {
  // /default/patient/{patientId}/medications
  getForPatient: (patientId) =>
    client.get(`/default/patient/${patientId}/medications`),

  addForPatient: (patientId, payload) =>
    client.post(`/default/patient/${patientId}/medications`, payload),

  updateMedication: (patientId, medId, payload) =>
    client.put(`/default/patient/${patientId}/medications/${medId}`, payload),

  deleteMedication: (patientId, medId) =>
    client.delete(`/default/patient/${patientId}/medications/${medId}`),
};