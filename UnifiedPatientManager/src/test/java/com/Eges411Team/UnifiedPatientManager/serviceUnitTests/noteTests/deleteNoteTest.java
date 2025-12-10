package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.noteTests;

import com.Eges411Team.UnifiedPatientManager.entity.Note;
import com.Eges411Team.UnifiedPatientManager.entity.NoteType;
import com.Eges411Team.UnifiedPatientManager.repositories.NoteRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.NoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Delete Note
 * Use Case: Delete patient notes
 * Requirement: 3.1.1 - The system shall allow users to delete patient notes
 * Technique: Equivalence Partitioning - Testing valid deletion and error cases
 */
@ExtendWith(MockitoExtension.class)
class deleteNoteTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void deleteNote_validNoteAndPatient_successfullyDeletesNote() {
        // Arrange
        Long patientId = 1L;
        Long noteId = 10L;
        
        Note noteToDelete = new Note();
        noteToDelete.setId(noteId);
        noteToDelete.setPatientId(patientId);
        noteToDelete.setNoteType(NoteType.TEXT);
        noteToDelete.setContent("Note to be deleted");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(noteToDelete));

        // Act
        assertDoesNotThrow(() -> noteService.deleteNote(patientId, noteId));

        // Assert
        verify(noteRepository, times(1)).findById(noteId);
        verify(noteRepository, times(1)).delete(noteToDelete);
    }

    @Test
    void deleteNote_noteNotFound_throwsNotFoundException() {
        // Arrange
        Long patientId = 1L;
        Long nonExistentNoteId = 999L;

        when(noteRepository.findById(nonExistentNoteId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.deleteNote(patientId, nonExistentNoteId)
        );

        assertTrue(ex.getMessage().contains("Note not found") || ex.getMessage().contains("NOT_FOUND"),
                "Expected error message about note not found but got: " + ex.getMessage());

        verify(noteRepository, times(1)).findById(nonExistentNoteId);
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void deleteNote_noteDoesNotBelongToPatient_throwsException() {
        // Arrange
        Long patientId = 1L;
        Long anotherPatientId = 2L;
        Long noteId = 20L;
        
        Note noteFromAnotherPatient = new Note();
        noteFromAnotherPatient.setId(noteId);
        noteFromAnotherPatient.setPatientId(anotherPatientId); // Different patient
        noteFromAnotherPatient.setContent("Another patient's note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(noteFromAnotherPatient));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.deleteNote(patientId, noteId)
        );

        assertTrue(ex.getMessage().contains("does not belong") || ex.getMessage().contains("BAD_REQUEST"),
                "Expected error message about note not belonging to patient but got: " + ex.getMessage());

        verify(noteRepository, times(1)).findById(noteId);
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void deleteNote_notePatientIdNull_throwsException() {
        // Arrange
        Long patientId = 1L;
        Long noteId = 30L;
        
        Note noteWithNullPatientId = new Note();
        noteWithNullPatientId.setId(noteId);
        noteWithNullPatientId.setPatientId(null); // Null patient ID
        noteWithNullPatientId.setContent("Orphaned note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(noteWithNullPatientId));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.deleteNote(patientId, noteId)
        );

        assertTrue(ex.getMessage().contains("does not belong"),
                "Expected error message but got: " + ex.getMessage());

        verify(noteRepository, times(1)).findById(noteId);
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void deleteNote_multipleNotesForPatient_deletesOnlySpecificNote() {
        // Arrange
        Long patientId = 3L;
        Long noteIdToDelete = 40L;
        
        Note noteToDelete = new Note();
        noteToDelete.setId(noteIdToDelete);
        noteToDelete.setPatientId(patientId);
        noteToDelete.setContent("Delete this note");

        when(noteRepository.findById(noteIdToDelete)).thenReturn(Optional.of(noteToDelete));

        // Act
        assertDoesNotThrow(() -> noteService.deleteNote(patientId, noteIdToDelete));

        // Assert - Verify only the specific note was deleted
        verify(noteRepository, times(1)).delete(noteToDelete);
        verify(noteRepository, times(1)).findById(noteIdToDelete);
    }
}
