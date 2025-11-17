package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.Note;
import com.Eges411Team.UnifiedPatientManager.repositories.NoteRepo;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NoteService {

    private final NoteRepo noteRepository;

    public NoteService(NoteRepo noteRepository) {
        this.noteRepository = noteRepository;
    }

    // GET /{patient_id}/notes
    public List<Note> getNotesByPatientId(int patientId) {
        return noteRepository.findAllByPatient_id(patientId);
    }

    // POST /{patient_id}/notes
    // Replace all notes for this patient with the provided list
    public List<Note> saveNotes(int patientId, List<Note> notes) {
        // Delete existing notes for this patient
        List<Note> existing = noteRepository.findAllByPatient_id(patientId);
        noteRepository.deleteAll(existing);
 
        // Ensure correct patient_id
        for (Note note : notes) {
            note.setPatient_id(patientId);
        }

        return noteRepository.saveAll(notes);
    }

    // PUT /{patient_id}/notes/{note_id}
    public Note updateNote(int patientId, int noteId, Note updated) {
        Note existing = noteRepository.findById(noteId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Note not found with id: " + noteId
            ));

        // Ensure note belongs to the correct patient
        if (existing.getPatient_id() != patientId) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Note does not belong to the specified patient"
            );
        }

        // Update allowed fields
        existing.setDoctor_id(updated.getDoctor_id());
        existing.setNote_type(updated.getNote_type());
        existing.setContent(updated.getContent());
        existing.setTimestamp(updated.getTimestamp());

        return noteRepository.save(existing);
    }

    // GET /{patient_id}/notes/refresh
    // Currently same as get; could later sync with an external system
    public List<Note> refreshNotes(int patientId) {
        return noteRepository.findAllByPatient_id(patientId);
    }

    // DELETE /{patient_id}/notes/{note_id}
    public void deleteNote(int patientId, int noteId) {
        Optional<Note> existingOpt = noteRepository.findById(noteId);

        Note existing = existingOpt.orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Note not found with id: " + noteId
            )
        );

        if (existing.getPatient_id() != patientId) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Note does not belong to the specified patient"
            );
        }

        noteRepository.delete(existing);
    }

    // save a single note
    public Note saveSingleNote(Note note) {
    return noteRepository.save(note);
}
}
