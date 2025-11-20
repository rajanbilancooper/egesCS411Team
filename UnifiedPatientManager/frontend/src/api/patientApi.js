// import client from "./axiosClient";

// export const patientApi = {
//   // /default/patient/search?name=...
//   searchByName: (name) =>
//     client.get("/default/patient/search", { params: { name } }),

//   // /default/patient/{id}
//   getById: (id) =>
//     client.get(`/default/patient/${id}`),

//   create: (payload) =>
//     client.post("/default/patient", payload),

//   update: (id, payload) =>
//     client.put(`/default/patient/${id}`, payload),

//   delete: (id) =>
//     client.delete(`/default/patient/${id}`),
// };

export const patientApi = {
  getPatientById: async (id) =>
    Promise.resolve({
      data: {
        id,
        fullName: "Mock Patient",
        dateOfBirth: "2000-01-01",
        gender: "Female",
        allergiesSummary: "Peanuts, Dust",
        currentMedicationSummary: "Ibuprofen 200mg",
      },
    }),

  getNotes: async (patientId) =>
    Promise.resolve({
      data: [
        { id: 1, content: "This is a mock note (no DB connected)." },
      ],
    }),

  createNote: async (patientId, payload) =>
    Promise.resolve({
      data: { id: 2, ...payload },
    }),

  getVaccines: async (patientId) =>
    Promise.resolve({
      data: [
        {
          id: 1,
          name: "COVID-19",
          date: "2024-01-01",
          monthsUntilInvalid: 12,
        },
      ],
    }),

  // same idea for medical history, allergies...
};