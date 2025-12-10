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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case 18: Create Note for Non-Existent Patient
 * Use Case: Doctor Attaches physical notes to patient record
 * Requirement: 3.1.1 - The system shall allow nurses to update treatment notes, vitals, 
 *              and patient information during appointments or visits
 * Technique: Equivalence Partitioning – Testing the invalid input class for nonexistent patient IDs
 */
@ExtendWith(MockitoExtension.class)
class nonExistentPatientNoteTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void saveSingleNote_nonExistentPatient_throwsPatientNotFoundException() {
        // Arrange - Build a Note with non-existent patient ID as per test case
        Note note = new Note();
        note.setPatientId(99999L); // Non-existent patient ID
        note.setDoctorId(456L); // Doctor ID 456
        note.setNoteType(NoteType.TEXT);
        note.setContent("Patient reports mild headache and was advised to rest.");
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient does NOT exist (findById returns empty Optional)
        when(userRepository.findById(99999L)).thenReturn(Optional.empty());

        // Act & Assert - expect ResponseStatusException with "Patient Not Found" message
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.saveSingleNote(note)
        );

        assertEquals("Patient Not Found", ex.getReason(),
                "Expected error message to be 'Patient Not Found' but got: " + ex.getReason());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode(),
                "Expected HTTP status to be NOT_FOUND");

        // Verify repository.save was NEVER called due to validation failure
        verify(noteRepository, never()).save(any(Note.class));

        // Verify that userRepository.findById was called to validate patient existence
        verify(userRepository, times(1)).findById(99999L);
    }

    @Test
    void saveSingleNote_nonExistentPatient_noNoteCreated() {
        // Arrange - Non-existent patient scenario
        Note note = new Note();
        note.setPatientId(99999L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent("Patient reports mild headache and was advised to rest.");
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient does NOT exist
        when(userRepository.findById(99999L)).thenReturn(Optional.empty());

        // Act & Assert - Verify exception is thrown and note is not saved
        assertThrows(ResponseStatusException.class, () ->
                noteService.saveSingleNote(note)
        );

        // Verify NO note record is created
        verify(noteRepository, never()).save(any(Note.class));
    }
}
