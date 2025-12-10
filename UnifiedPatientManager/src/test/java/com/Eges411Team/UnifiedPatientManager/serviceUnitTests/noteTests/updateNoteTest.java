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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Update Note
 * Use Case: Update patient notes
 * Requirement: 3.1.1 - The system shall allow users to update patient notes
 * Technique: Equivalence Partitioning - Testing valid updates and error cases
 */
@ExtendWith(MockitoExtension.class)
class updateNoteTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void updateNote_validNoteAndPatient_successfullyUpdatesNote() {
        // Arrange
        Long patientId = 1L;
        Long noteId = 50L;
        
        Note existingNote = new Note();
        existingNote.setId(noteId);
        existingNote.setPatientId(patientId);
        existingNote.setNoteType(NoteType.TEXT);
        existingNote.setContent("Original content");
        existingNote.setTimestamp(LocalDateTime.of(2025, 11, 10, 10, 0));

        Note updatedNote = new Note();
        updatedNote.setId(noteId);
        updatedNote.setPatientId(patientId);
        updatedNote.setNoteType(NoteType.TEXT);
        updatedNote.setContent("Updated content");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);

        // Act
        Note result = noteService.updateNote(patientId, noteId, updatedNote);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(noteId, result.getId());
        assertEquals("Updated content", result.getContent());
        
        verify(noteRepository, times(1)).findById(noteId);
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void updateNote_updateContentOnly_successfullyUpdates() {
        // Arrange
        Long patientId = 2L;
        Long noteId = 60L;
        
        Note existingNote = new Note();
        existingNote.setId(noteId);
        existingNote.setPatientId(patientId);
        existingNote.setContent("Old content");
        existingNote.setDoctorId(5L);

        Note updateData = new Note();
        updateData.setContent("New updated content");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Note result = noteService.updateNote(patientId, noteId, updateData);

        // Assert
        assertEquals("New updated content", result.getContent());
        assertEquals(patientId, result.getPatientId());
        
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void updateNote_noteNotFound_throwsNotFoundException() {
        // Arrange
        Long patientId = 1L;
        Long nonExistentNoteId = 999L;

        Note updateData = new Note();
        updateData.setContent("Update content");

        when(noteRepository.findById(nonExistentNoteId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.updateNote(patientId, nonExistentNoteId, updateData)
        );

        assertTrue(ex.getMessage().contains("Note not found") || ex.getMessage().contains("NOT_FOUND"),
                "Expected error message about note not found but got: " + ex.getMessage());

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_noteDoesNotBelongToPatient_throwsException() {
        // Arrange
        Long patientId = 1L;
        Long anotherPatientId = 3L;
        Long noteId = 70L;
        
        Note noteFromAnotherPatient = new Note();
        noteFromAnotherPatient.setId(noteId);
        noteFromAnotherPatient.setPatientId(anotherPatientId);
        noteFromAnotherPatient.setContent("Another patient's note");

        Note updateData = new Note();
        updateData.setContent("Trying to update");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(noteFromAnotherPatient));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.updateNote(patientId, noteId, updateData)
        );

        assertTrue(ex.getMessage().contains("does not belong") || ex.getMessage().contains("BAD_REQUEST"),
                "Expected error message about note not belonging to patient but got: " + ex.getMessage());

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_updateToFileType_successfullyUpdates() {
        // Arrange
        Long patientId = 3L;
        Long noteId = 90L;
        
        Note existingNote = new Note();
        existingNote.setId(noteId);
        existingNote.setPatientId(patientId);
        existingNote.setNoteType(NoteType.TEXT);
        existingNote.setContent("Text content");

        Note updateData = new Note();
        updateData.setNoteType(NoteType.FILE);
        updateData.setAttachmentName("document.pdf");
        updateData.setAttachmentData(new byte[] {1, 2, 3, 4, 5});

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Note result = noteService.updateNote(patientId, noteId, updateData);

        // Assert
        assertEquals(NoteType.FILE, result.getNoteType());
        assertEquals("document.pdf", result.getAttachmentName());
        
        verify(noteRepository, times(1)).save(any(Note.class));
    }
}
