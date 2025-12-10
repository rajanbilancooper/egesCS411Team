package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.noteTests;

import com.Eges411Team.UnifiedPatientManager.entity.Note;
import com.Eges411Team.UnifiedPatientManager.entity.NoteType;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.NoteRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.NoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for NoteService covering all branches
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceComprehensiveTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    private static final int MAX_CONTENT_LENGTH = 500;
    private static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    // ==================== getNotesByPatientId ====================
    @Test
    void getNotesByPatientId_withNotes_returnsAll() {
        Long patientId = 1L;
        List<Note> notes = List.of(
            buildNote(1L, patientId, "Note 1"),
            buildNote(2L, patientId, "Note 2")
        );

        when(noteRepository.findAllByPatientId(patientId)).thenReturn(notes);

        List<Note> result = noteService.getNotesByPatientId(patientId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(noteRepository).findAllByPatientId(patientId);
    }

    @Test
    void getNotesByPatientId_noNotes_returnsEmpty() {
        Long patientId = 2L;

        when(noteRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        List<Note> result = noteService.getNotesByPatientId(patientId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== refreshNotes ====================
    @Test
    void refreshNotes_returnsLatestList() {
        Long patientId = 3L;
        List<Note> notes = List.of(buildNote(5L, patientId, "Refreshed note"));

        when(noteRepository.findAllByPatientId(patientId)).thenReturn(notes);

        List<Note> result = noteService.refreshNotes(patientId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(noteRepository).findAllByPatientId(patientId);
    }

    // ==================== saveNotes ====================
    @Test
    void saveNotes_deletesExistingAndSavesNew() {
        Long patientId = 4L;

        Note oldNote = buildNote(1L, patientId, "Old note");
        List<Note> existingNotes = List.of(oldNote);

        Note newNote1 = buildNote(null, null, "New note 1");
        Note newNote2 = buildNote(null, null, "New note 2");
        List<Note> newNotes = List.of(newNote1, newNote2);

        when(noteRepository.findAllByPatientId(patientId)).thenReturn(existingNotes);
        when(noteRepository.saveAll(anyList())).thenReturn(newNotes);

        List<Note> result = noteService.saveNotes(patientId, newNotes);

        verify(noteRepository).deleteAll(existingNotes);
        verify(noteRepository).saveAll(anyList());
        assertEquals(2, result.size());
    }

    @Test
    void saveNotes_emptyNewList_deletesExistingAndSavesNothing() {
        Long patientId = 5L;

        Note oldNote = buildNote(1L, patientId, "Old note");
        List<Note> existingNotes = List.of(oldNote);
        List<Note> emptyNotes = new ArrayList<>();

        when(noteRepository.findAllByPatientId(patientId)).thenReturn(existingNotes);
        when(noteRepository.saveAll(anyList())).thenReturn(emptyNotes);

        List<Note> result = noteService.saveNotes(patientId, emptyNotes);

        verify(noteRepository).deleteAll(existingNotes);
        assertTrue(result.isEmpty());
    }

    // ==================== updateNote ====================
    @Test
    void updateNote_notFound_throwsNotFound() {
        Long patientId = 6L;
        Long noteId = 999L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        Note updated = buildNote(null, null, "Updated");

        assertThrows(ResponseStatusException.class, () ->
            noteService.updateNote(patientId, noteId, updated)
        );

        verify(noteRepository).findById(noteId);
    }

    @Test
    void updateNote_wrongPatient_throwsBadRequest() {
        Long patientId = 7L;
        Long wrongPatientId = 99L;
        Long noteId = 10L;

        Note existing = buildNote(noteId, wrongPatientId, "Existing");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));

        Note updated = buildNote(null, null, "Updated");

        assertThrows(ResponseStatusException.class, () ->
            noteService.updateNote(patientId, noteId, updated)
        );

        verify(noteRepository).findById(noteId);
        verify(noteRepository, never()).save(any());
    }

    @Test
    void updateNote_patientIdNull_throwsBadRequest() {
        Long patientId = 8L;
        Long noteId = 11L;

        Note existing = buildNote(noteId, null, "Existing");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));

        Note updated = buildNote(null, null, "Updated");

        assertThrows(ResponseStatusException.class, () ->
            noteService.updateNote(patientId, noteId, updated)
        );

        verify(noteRepository, never()).save(any());
    }

    @Test
    void updateNote_validUpdateAllFields_successfullyUpdates() {
        Long patientId = 9L;
        Long noteId = 12L;

        Note existing = buildNote(noteId, patientId, "Original");
        existing.setNoteType(NoteType.TEXT);
        existing.setDoctorId(100L);

        Note updated = new Note();
        updated.setContent("Updated content");
        updated.setNoteType(NoteType.FILE);
        updated.setDoctorId(200L);
        updated.setTimestamp(LocalDateTime.now());
        updated.setAttachmentName("file.pdf");
        updated.setAttachmentData(new byte[]{1, 2, 3});

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.updateNote(patientId, noteId, updated);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        assertEquals(NoteType.FILE, result.getNoteType());
        assertEquals(200L, result.getDoctorId());
        verify(noteRepository).save(any());
    }

    // ==================== deleteNote ====================
    @Test
    void deleteNote_notFound_throwsNotFound() {
        Long patientId = 10L;
        Long noteId = 888L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            noteService.deleteNote(patientId, noteId)
        );

        verify(noteRepository, never()).delete(any());
    }

    @Test
    void deleteNote_wrongPatient_throwsBadRequest() {
        Long patientId = 11L;
        Long wrongPatientId = 88L;
        Long noteId = 13L;

        Note existing = buildNote(noteId, wrongPatientId, "Note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
            noteService.deleteNote(patientId, noteId)
        );

        verify(noteRepository, never()).delete(any());
    }

    @Test
    void deleteNote_patientIdNull_throwsBadRequest() {
        Long patientId = 12L;
        Long noteId = 14L;

        Note existing = buildNote(noteId, null, "Note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
            noteService.deleteNote(patientId, noteId)
        );

        verify(noteRepository, never()).delete(any());
    }

    @Test
    void deleteNote_validNote_successfullyDeletes() {
        Long patientId = 13L;
        Long noteId = 15L;

        Note existing = buildNote(noteId, patientId, "Note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));

        noteService.deleteNote(patientId, noteId);

        verify(noteRepository).delete(existing);
    }

    // ==================== saveSingleNote - Basic Validation ====================
    @Test
    void saveSingleNote_validTextNote_successfullySaves() {
        Note note = buildNote(null, 1L, "Valid text note");
        note.setNoteType(NoteType.TEXT);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.saveSingleNote(note);

        assertNotNull(result);
        verify(noteRepository).save(any());
    }

    @Test
    void saveSingleNote_nullPatientId_successfullySaves() {
        // Note with null patientId should skip patient validation
        Note note = new Note();
        note.setPatientId(null);
        note.setContent("Note without patient");
        note.setNoteType(NoteType.TEXT);

        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.saveSingleNote(note);

        assertNotNull(result);
        verify(userRepository, never()).findById(any());
        verify(noteRepository).save(any());
    }

    @Test
    void saveSingleNote_patientNotFound_throwsNotFound() {
        Note note = buildNote(null, 100L, "Note");
        note.setNoteType(NoteType.TEXT);

        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            noteService.saveSingleNote(note)
        );

        verify(noteRepository, never()).save(any());
    }

    // ==================== saveSingleNote - Content Length Validation ====================
    @Test
    void saveSingleNote_contentAtMaxLength_successfullySaves() {
        String contentAtMax = "a".repeat(MAX_CONTENT_LENGTH);

        Note note = buildNote(null, 1L, contentAtMax);
        note.setNoteType(NoteType.TEXT);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.saveSingleNote(note);

        assertNotNull(result);
        verify(noteRepository).save(any());
    }

    @Test
    void saveSingleNote_contentExceedsMaxLength_throwsValidationException() {
        String contentTooLong = "a".repeat(MAX_CONTENT_LENGTH + 1);

        Note note = buildNote(null, 1L, contentTooLong);
        note.setNoteType(NoteType.TEXT);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThrows(ResponseStatusException.class, () ->
            noteService.saveSingleNote(note)
        );

        verify(noteRepository, never()).save(any());
    }

    @Test
    void saveSingleNote_nullContent_successfullySaves() {
        Note note = buildNote(null, 1L, null);
        note.setNoteType(NoteType.TEXT);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.saveSingleNote(note);

        assertNotNull(result);
        verify(noteRepository).save(any());
    }

    // ==================== saveSingleNote - File Size Validation ====================
    @Test
    void saveSingleNote_fileAtMaxSize_successfullySaves() {
        byte[] fileDataAtMax = new byte[MAX_FILE_SIZE_BYTES];

        Note note = new Note();
        note.setPatientId(1L);
        note.setNoteType(NoteType.FILE);
        note.setAttachmentName("file.pdf");
        note.setAttachmentData(fileDataAtMax);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.saveSingleNote(note);

        assertNotNull(result);
        verify(noteRepository).save(any());
    }

    @Test
    void saveSingleNote_fileExceedsMaxSize_throwsValidationException() {
        byte[] fileDataTooLarge = new byte[MAX_FILE_SIZE_BYTES + 1];

        Note note = new Note();
        note.setPatientId(1L);
        note.setNoteType(NoteType.FILE);
        note.setAttachmentName("file.pdf");
        note.setAttachmentData(fileDataTooLarge);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThrows(ResponseStatusException.class, () ->
            noteService.saveSingleNote(note)
        );

        verify(noteRepository, never()).save(any());
    }

    @Test
    void saveSingleNote_fileTypeButNoAttachmentData_successfullySaves() {
        // FILE type note without attachment data should pass validation
        Note note = new Note();
        note.setPatientId(1L);
        note.setNoteType(NoteType.FILE);
        note.setAttachmentName(null);
        note.setAttachmentData(null);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.saveSingleNote(note);

        assertNotNull(result);
        verify(noteRepository).save(any());
    }

    // ==================== saveSingleNote - File Format Validation ====================
    @Test
    void saveSingleNote_supportedFileFormat_successfullySaves() {
        String[] supportedFormats = {".pdf", ".doc", ".docx", ".png", ".jpg", ".jpeg", ".mp3", ".wav", ".m4a"};

        for (String format : supportedFormats) {
            Note note = new Note();
            note.setPatientId(1L);
            note.setNoteType(NoteType.FILE);
            note.setAttachmentName("file" + format);
            note.setAttachmentData(new byte[100]);

            User patient = new User();
            when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

            Note result = noteService.saveSingleNote(note);

            assertNotNull(result);
        }

        verify(noteRepository, times(supportedFormats.length)).save(any());
    }

    @Test
    void saveSingleNote_unsupportedFileFormat_throwsValidationException() {
        Note note = new Note();
        note.setPatientId(1L);
        note.setNoteType(NoteType.FILE);
        note.setAttachmentName("file.exe");
        note.setAttachmentData(new byte[100]);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThrows(ResponseStatusException.class, () ->
            noteService.saveSingleNote(note)
        );

        verify(noteRepository, never()).save(any());
    }

    @Test
    void saveSingleNote_caseInsensitiveFileFormatDetection() {
        Note note = new Note();
        note.setPatientId(1L);
        note.setNoteType(NoteType.FILE);
        note.setAttachmentName("FILE.PDF");  // Uppercase
        note.setAttachmentData(new byte[100]);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.saveSingleNote(note);

        assertNotNull(result);
        verify(noteRepository).save(any());
    }

    @Test
    void saveSingleNote_fileTypeNoAttachmentName_successfullySaves() {
        // FILE type without attachmentName should skip format validation
        Note note = new Note();
        note.setPatientId(1L);
        note.setNoteType(NoteType.FILE);
        note.setAttachmentName(null);
        note.setAttachmentData(new byte[100]);

        User patient = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.saveSingleNote(note);

        assertNotNull(result);
        verify(noteRepository).save(any());
    }

    // ==================== getNoteForPatient ====================
    @Test
    void getNoteForPatient_notFound_throwsNotFound() {
        Long patientId = 14L;
        Long noteId = 777L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            noteService.getNoteForPatient(patientId, noteId)
        );
    }

    @Test
    void getNoteForPatient_wrongPatient_throwsBadRequest() {
        Long patientId = 15L;
        Long wrongPatientId = 77L;
        Long noteId = 16L;

        Note existing = buildNote(noteId, wrongPatientId, "Note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
            noteService.getNoteForPatient(patientId, noteId)
        );
    }

    @Test
    void getNoteForPatient_patientIdNull_throwsBadRequest() {
        Long patientId = 16L;
        Long noteId = 17L;

        Note existing = buildNote(noteId, null, "Note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
            noteService.getNoteForPatient(patientId, noteId)
        );
    }

    @Test
    void getNoteForPatient_validNote_returnsNote() {
        Long patientId = 17L;
        Long noteId = 18L;

        Note existing = buildNote(noteId, patientId, "Note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existing));

        Note result = noteService.getNoteForPatient(patientId, noteId);

        assertNotNull(result);
        assertEquals(noteId, result.getId());
        assertEquals(patientId, result.getPatientId());
    }

    // ==================== Helper Method ====================
    private Note buildNote(Long id, Long patientId, String content) {
        Note note = new Note();
        note.setId(id);
        note.setPatientId(patientId);
        note.setDoctorId(100L);
        note.setNoteType(NoteType.TEXT);
        note.setContent(content);
        note.setTimestamp(LocalDateTime.now());
        return note;
    }
}
