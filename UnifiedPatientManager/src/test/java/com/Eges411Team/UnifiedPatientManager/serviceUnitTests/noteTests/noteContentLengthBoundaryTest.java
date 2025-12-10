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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case 20: Note content exceeds maximum allowed note length
 * Use Case: Doctor Attaches physical notes to patient record
 * Requirement: 3.1.1 - The system shall allow doctors to attach notes (typed, audio, or image-based) 
 *              to patient records
 * Technique: Boundary Value Analysis – Testing the value beyond the upper valid boundary for content field
 */
@ExtendWith(MockitoExtension.class)
class noteContentLengthBoundaryTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    private static final int MAX_CONTENT_LENGTH = 500;

    @Test
    void saveSingleNote_contentExceedsMaxLength_throwsValidationException() {
        // Arrange - Build a Note with content exceeding 500 characters (501 chars)
        String contentTooLong = "A".repeat(501); // 501 characters (BVA: Max + 1)
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent(contentTooLong);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        // Act & Assert - expect ResponseStatusException for content too long
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.saveSingleNote(note)
        );

        assertTrue(ex.getReason().contains("Note content exceeds maximum length"),
                "Expected error message to contain 'Note content exceeds maximum length' but got: " + ex.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode(),
                "Expected HTTP status to be BAD_REQUEST");

        // Verify repository.save was NEVER called due to validation failure
        verify(noteRepository, never()).save(any(Note.class));

        // Verify patient existence was checked
        verify(userRepository, times(1)).findById(123L);
    }

    @Test
    void saveSingleNote_contentAtMaxLength_successfullySaves() {
        // Arrange - Build a Note with exactly 500 characters (BVA: Max boundary)
        String contentAtMax = "A".repeat(500); // Exactly 500 characters
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent(contentAtMax);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.TEXT);
        savedNote.setContent(contentAtMax);
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with content at maximum boundary
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals(500, result.getContent().length(), "Content should be exactly 500 characters");
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_contentJustBelowMax_successfullySaves() {
        // Arrange - Build a Note with 499 characters (BVA: Max - 1)
        String contentBelowMax = "A".repeat(499); // 499 characters
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent(contentBelowMax);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(2L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.TEXT);
        savedNote.setContent(contentBelowMax);
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with content just below maximum
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals(499, result.getContent().length(), "Content should be exactly 499 characters");
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_emptyContent_successfullySaves() {
        // Arrange - Build a Note with empty content (BVA: Min boundary)
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent(""); // Empty content (0 characters)
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(3L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.TEXT);
        savedNote.setContent("");
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with empty content
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals(0, result.getContent().length(), "Content should be empty (0 characters)");
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_contentFarExceedsMax_throwsValidationException() {
        // Arrange - Build a Note with content far exceeding maximum (1000 chars)
        String contentVeryLong = "A".repeat(1000); // 1000 characters
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent(contentVeryLong);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        // Act & Assert - expect ResponseStatusException for content far too long
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.saveSingleNote(note)
        );

        assertTrue(ex.getReason().contains("Note content exceeds maximum length"),
                "Expected error message to contain 'Note content exceeds maximum length'");

        // Verify repository.save was NEVER called
        verify(noteRepository, never()).save(any(Note.class));
    }
}
