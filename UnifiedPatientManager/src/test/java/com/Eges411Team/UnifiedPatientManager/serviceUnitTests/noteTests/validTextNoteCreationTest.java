package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.noteTests;

import com.Eges411Team.UnifiedPatientManager.entity.Note;
import com.Eges411Team.UnifiedPatientManager.entity.NoteType;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.NoteRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.NoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case 17: Create text note with all valid fields
 * Use Case: Doctor Attaches physical notes to patient record
 * Requirement: 3.1.1 - The system shall allow doctors to attach notes (typed, audio, or image-based) 
 *              to patient records
 * Technique: Equivalence Partitioning – Testing the valid input class for note creation
 */
@ExtendWith(MockitoExtension.class)
class validTextNoteCreationTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void saveSingleNote_allValidFields_successfullyCreatesNote() {
        // Arrange - Build a Note with all valid fields as per test case
        LocalDateTime timestamp = LocalDateTime.now();
        
        Note note = new Note();
        note.setPatientId(123L); // Patient ID 123
        note.setDoctorId(456L); // Doctor ID 456
        note.setNoteType(NoteType.TEXT); // Note type is TEXT
        note.setContent("Patient reports mild headache and was advised to rest.");
        note.setTimestamp(timestamp); // Server-generated timestamp

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        // Mock the repository to return the saved note with an ID
        Note savedNote = new Note();
        savedNote.setId(1L); // Database-generated ID
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.TEXT);
        savedNote.setContent("Patient reports mild headache and was advised to rest.");
        savedNote.setTimestamp(timestamp);

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Call the service method to save the note
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify the note was successfully created with correct values
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Note should have an ID after saving (database-generated)");
        assertEquals(123L, result.getPatientId(), "Patient ID should be 123");
        assertEquals(456L, result.getDoctorId(), "Doctor ID should be 456");
        assertEquals(NoteType.TEXT, result.getNoteType(), "Note type should be TEXT");
        assertEquals("Patient reports mild headache and was advised to rest.", result.getContent(), 
                "Content should match the input");
        assertNotNull(result.getTimestamp(), "Timestamp should be set (server-generated)");

        // Verify repository.save was called exactly once
        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository, times(1)).save(captor.capture());

        // Verify the entity passed to save had the correct values
        Note capturedEntity = captor.getValue();
        assertEquals(123L, capturedEntity.getPatientId(), "Captured entity should have patient ID 123");
        assertEquals(456L, capturedEntity.getDoctorId(), "Captured entity should have doctor ID 456");
        assertEquals(NoteType.TEXT, capturedEntity.getNoteType(), 
                "Captured entity should have note type TEXT");
        assertEquals("Patient reports mild headache and was advised to rest.", 
                capturedEntity.getContent(), 
                "Captured entity should have correct content");
        assertNotNull(capturedEntity.getTimestamp(), 
                "Captured entity should have timestamp");
    }

    @Test
    void saveSingleNote_validTextNote_createsNoteWithTimestamp() {
        // Arrange - Valid TEXT note
        LocalDateTime beforeSave = LocalDateTime.now();
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent("Patient reports mild headache and was advised to rest.");
        note.setTimestamp(beforeSave);

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.TEXT);
        savedNote.setContent("Patient reports mild headache and was advised to rest.");
        savedNote.setTimestamp(beforeSave);

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save the note
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify note was created with a timestamp
        assertNotNull(result);
        assertNotNull(result.getTimestamp(), "Note should have a timestamp");
        assertTrue(result.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)), 
                "Timestamp should be recent (within last second)");
        
        // Verify the save was called
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_validFields_noExceptionThrown() {
        // Arrange - Valid note with all required fields
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent("Patient reports mild headache and was advised to rest.");
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.TEXT);
        savedNote.setContent("Patient reports mild headache and was advised to rest.");
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> {
            Note result = noteService.saveSingleNote(note);
            assertNotNull(result, "Result should not be null after successful save");
        });

        // Verify the save was called
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_validFileNote_successfullyCreatesFileNote() {
        // Arrange - Build a FILE note with attachment data
        LocalDateTime timestamp = LocalDateTime.now();
        byte[] fileData = "Sample file content for lab results PDF".getBytes();
        
        Note fileNote = new Note();
        fileNote.setPatientId(123L);
        fileNote.setDoctorId(456L);
        fileNote.setNoteType(NoteType.FILE);
        fileNote.setContent("Lab results from 2025-12-09"); // Optional description
        fileNote.setAttachmentName("lab-results.pdf");
        fileNote.setAttachmentData(fileData);
        fileNote.setTimestamp(timestamp);

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        // Mock the repository to return the saved file note with an ID
        Note savedFileNote = new Note();
        savedFileNote.setId(2L);
        savedFileNote.setPatientId(123L);
        savedFileNote.setDoctorId(456L);
        savedFileNote.setNoteType(NoteType.FILE);
        savedFileNote.setContent("Lab results from 2025-12-09");
        savedFileNote.setAttachmentName("lab-results.pdf");
        savedFileNote.setAttachmentData(fileData);
        savedFileNote.setTimestamp(timestamp);

        when(noteRepository.save(any(Note.class))).thenReturn(savedFileNote);

        // Act - Save the file note
        Note result = noteService.saveSingleNote(fileNote);

        // Assert - Verify the file note was created successfully
        assertNotNull(result, "Result should not be null");
        assertEquals(2L, result.getId(), "Note should have an ID after saving");
        assertEquals(123L, result.getPatientId(), "Patient ID should be 123");
        assertEquals(456L, result.getDoctorId(), "Doctor ID should be 456");
        assertEquals(NoteType.FILE, result.getNoteType(), "Note type should be FILE");
        assertEquals("lab-results.pdf", result.getAttachmentName(), 
                "Attachment name should be lab-results.pdf");
        assertNotNull(result.getAttachmentData(), "Attachment data should not be null");
        assertArrayEquals(fileData, result.getAttachmentData(), 
                "Attachment data should match the input file data");
        assertEquals("Lab results from 2025-12-09", result.getContent(), 
                "Content description should match");
        assertNotNull(result.getTimestamp(), "Timestamp should be set");

        // Verify repository.save was called
        verify(noteRepository, times(1)).save(any(Note.class));
    }
}
