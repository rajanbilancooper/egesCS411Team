import client from "./axiosClient";

export const patientApi = {
  // /default/patient/search?name=...
  searchByName: (name) =>
    client.get("/default/patient/search", { params: { name } }),

  // /default/patient/{id}
  getById: (id) =>
    client.get(`/default/patient/${id}`),

  create: (payload) =>
    client.post("/default/patient", payload),

  update: (id, payload) =>
    client.put(`/default/patient/${id}`, payload),

  delete: (id) =>
    client.delete(`/default/patient/${id}`),
};