import client from "./axiosClient";

export const medicalHistoryApi = {
  // /default/patient/{patientId}/medicalhistory
  getForPatient: (patientId) =>
    client.get(`/default/patient/${patientId}/medicalhistory`),

  addForPatient: (patientId, payload) =>
    client.post(`/default/patient/${patientId}/medicalhistory`, payload),

  updateEntry: (patientId, historyId, payload) =>
    client.put(`/default/patient/${patientId}/medicalhistory/${historyId}`, payload),

  deleteEntry: (patientId, historyId) =>
    client.delete(`/default/patient/${patientId}/medicalhistory/${historyId}`),
};