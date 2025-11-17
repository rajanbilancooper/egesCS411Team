import { api } from "./client";

export const NotesAPI = {
  list(patientId) {
    return api.get(`/default/patient/${patientId}/notes`);
  },
  create(patientId, noteRequestDTO) {
    // noteRequestDTO = { doctor_id, note_type, content, timestamp }
    return api.post(`/default/patient/${patientId}/notes`, noteRequestDTO);
  },
  update(patientId, noteId, body) {
    return api.put(`/default/patient/${patientId}/notes/${noteId}`, body);
  },
  refresh(patientId) {
    return api.get(`/default/patient/${patientId}/notes/refresh`);
  },
  remove(patientId, noteId) {
    return api.delete(`/default/patient/${patientId}/notes/${noteId}`);
  },
};