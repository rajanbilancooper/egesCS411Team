import client from "./axiosClient";

export const allergyApi = {
  // /default/patient/{patientId}/allergies
  getForPatient: (patientId) =>
    client.get(`/default/patient/${patientId}/allergies`),

  addForPatient: (patientId, payload) =>
    client.post(`/default/patient/${patientId}/allergies`, payload),

  updateAllergy: (patientId, allergyId, payload) =>
    client.put(`/default/patient/${patientId}/allergies/${allergyId}`, payload),

  deleteAllergy: (patientId, allergyId) =>
    client.delete(`/default/patient/${patientId}/allergies/${allergyId}`),
};