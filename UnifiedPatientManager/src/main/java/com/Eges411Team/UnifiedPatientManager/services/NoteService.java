package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.Note;
import com.Eges411Team.UnifiedPatientManager.entity.NoteType;
import com.Eges411Team.UnifiedPatientManager.repositories.NoteRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import java.util.Arrays;
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
    private final UserRepository userRepository;
    
    // Supported file extensions
    private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList(
        ".pdf", ".doc", ".docx", ".png", ".jpg", ".jpeg", ".mp3", ".wav", ".m4a"
    );
    
    // Maximum content length for notes (matches database VARCHAR limit)
    private static final int MAX_CONTENT_LENGTH = 500;
    
    // Maximum file size for attachments (5 MB)
    private static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB in bytes

    public NoteService(NoteRepo noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    // GET /{patient_id}/notes
    public List<Note> getNotesByPatientId(Long patientId) {
        return noteRepository.findAllByPatientId(patientId);
    }

    // POST /{patient_id}/notes
    // Replace all notes for this patient with the provided list
    public List<Note> saveNotes(Long patientId, List<Note> notes) {
        // Delete existing notes for this patient
        List<Note> existing = noteRepository.findAllByPatientId(patientId);
        noteRepository.deleteAll(existing);

        // Ensure correct patientId
        for (Note note : notes) {
            note.setPatientId(patientId);
        }

        return noteRepository.saveAll(notes);
    }

    // PUT /{patient_id}/notes/{note_id}
    public Note updateNote(Long patientId, Long noteId, Note updated) {
        Note existing = noteRepository.findById(noteId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Note not found with id: " + noteId
            ));

        // Ensure note belongs to the correct patient
        if (existing.getPatientId() == null || !existing.getPatientId().equals(patientId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Note does not belong to the specified patient"
            );
        }

        // Update allowed fields
        existing.setDoctorId(updated.getDoctorId());
        existing.setNoteType(updated.getNoteType());
        existing.setContent(updated.getContent());
        existing.setTimestamp(updated.getTimestamp());
        existing.setAttachmentName(updated.getAttachmentName());
        existing.setAttachmentData(updated.getAttachmentData());

        return noteRepository.save(existing);
    }

    // GET /{patient_id}/notes/refresh
    // Currently same as get; could later sync with an external system
    public List<Note> refreshNotes(Long patientId) {
        return noteRepository.findAllByPatientId(patientId);
    }

    // DELETE /{patient_id}/notes/{note_id}
    public void deleteNote(Long patientId, Long noteId) {
        Optional<Note> existingOpt = noteRepository.findById(noteId);

        Note existing = existingOpt.orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Note not found with id: " + noteId
            )
        );

        if (existing.getPatientId() == null || !existing.getPatientId().equals(patientId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Note does not belong to the specified patient"
            );
        }

        noteRepository.delete(existing);
    }

    // save a single note
    public Note saveSingleNote(Note note) {
        // Validate that the patient exists
        if (note.getPatientId() != null) {
            userRepository.findById(note.getPatientId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient Not Found")
            );
        }
        
        // Validate content length for TEXT notes
        if (note.getContent() != null && note.getContent().length() > MAX_CONTENT_LENGTH) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Note content exceeds maximum length of " + MAX_CONTENT_LENGTH + " characters"
            );
        }
        
        // Validate file size for FILE type notes with attachment data
        if (note.getNoteType() == NoteType.FILE && note.getAttachmentData() != null) {
            if (note.getAttachmentData().length > MAX_FILE_SIZE_BYTES) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE_BYTES / 1024 / 1024) + " MB"
                );
            }
        }
        
        // Validate file format for FILE type notes with attachments
        if (note.getNoteType() == NoteType.FILE && note.getAttachmentName() != null) {
            String fileName = note.getAttachmentName().toLowerCase();
            boolean isSupported = SUPPORTED_FILE_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);
            
            if (!isSupported) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Unsupported file type. Supported formats: " + SUPPORTED_FILE_EXTENSIONS
                );
            }
        }
        
        return noteRepository.save(note);
    }

    // Retrieve single note ensuring it belongs to patient
    public Note getNoteForPatient(Long patientId, Long noteId) {
        Note existing = noteRepository.findById(noteId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found with id: " + noteId));
        if (existing.getPatientId() == null || !existing.getPatientId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note does not belong to the specified patient");
        }
        return existing;
    }
}
