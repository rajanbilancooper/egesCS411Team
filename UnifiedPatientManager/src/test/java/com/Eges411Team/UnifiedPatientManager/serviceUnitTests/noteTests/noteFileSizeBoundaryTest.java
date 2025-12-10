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
 * Test Case 21: File size testing (max file size)
 * Use Case: Doctor Attaches physical notes to patient record
 * Requirement: 3.1.1 - The system shall allow doctors to attach notes (typed, audio, or image-based) 
 *              to patient records
 * Technique: Boundary Value Testing – size limit boundary (5 MB)
 */
@ExtendWith(MockitoExtension.class)
class noteFileSizeBoundaryTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    private static final int MAX_FILE_SIZE_MB = 5;
    private static final int MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024; // 5 MB in bytes

    @Test
    void saveSingleNote_fileSizeExactlyAtMax_successfullySaves() {
        // Arrange - Create a file of exactly 5 MB (BVA: Max boundary)
        byte[] fileDataAtMax = new byte[MAX_FILE_SIZE_BYTES]; // Exactly 5 MB
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Image within size limit");
        note.setAttachmentName("scan.png");
        note.setAttachmentData(fileDataAtMax);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.FILE);
        savedNote.setContent("Image within size limit");
        savedNote.setAttachmentName("scan.png");
        savedNote.setAttachmentData(fileDataAtMax);
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with file at maximum size
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals(MAX_FILE_SIZE_BYTES, result.getAttachmentData().length, 
                "File size should be exactly 5 MB");
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_fileSizeExceedsMax_throwsValidationException() {
        // Arrange - Create a file larger than 5 MB (BVA: Max + 1)
        byte[] fileDataTooLarge = new byte[MAX_FILE_SIZE_BYTES + 1]; // 5 MB + 1 byte
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Image too large");
        note.setAttachmentName("large-scan.png");
        note.setAttachmentData(fileDataTooLarge);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        // Act & Assert - expect ResponseStatusException for file too large
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.saveSingleNote(note)
        );

        assertTrue(ex.getReason().contains("File size exceeds maximum"),
                "Expected error message to contain 'File size exceeds maximum' but got: " + ex.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode(),
                "Expected HTTP status to be BAD_REQUEST");

        // Verify repository.save was NEVER called due to validation failure
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void saveSingleNote_fileSizeJustBelowMax_successfullySaves() {
        // Arrange - Create a file just below 5 MB (BVA: Max - 1)
        byte[] fileDataBelowMax = new byte[MAX_FILE_SIZE_BYTES - 1]; // 5 MB - 1 byte
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Image just below limit");
        note.setAttachmentName("xray.jpg");
        note.setAttachmentData(fileDataBelowMax);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(2L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.FILE);
        savedNote.setContent("Image just below limit");
        savedNote.setAttachmentName("xray.jpg");
        savedNote.setAttachmentData(fileDataBelowMax);
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with file just below maximum
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals(MAX_FILE_SIZE_BYTES - 1, result.getAttachmentData().length,
                "File size should be 5 MB - 1 byte");
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_smallFile_successfullySaves() {
        // Arrange - Create a small file (BVA: Min boundary)
        byte[] smallFile = new byte[1024]; // 1 KB
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Small image file");
        note.setAttachmentName("thumbnail.png");
        note.setAttachmentData(smallFile);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(3L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.FILE);
        savedNote.setContent("Small image file");
        savedNote.setAttachmentName("thumbnail.png");
        savedNote.setAttachmentData(smallFile);
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with small file
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals(1024, result.getAttachmentData().length, "File size should be 1 KB");
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_fileSizeFarExceedsMax_throwsValidationException() {
        // Arrange - Create a file far exceeding 5 MB (10 MB)
        byte[] fileDataVeryLarge = new byte[10 * 1024 * 1024]; // 10 MB
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Image way too large");
        note.setAttachmentName("huge-scan.png");
        note.setAttachmentData(fileDataVeryLarge);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        // Act & Assert - expect ResponseStatusException for file way too large
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.saveSingleNote(note)
        );

        assertTrue(ex.getReason().contains("File size exceeds maximum"),
                "Expected error message to contain 'File size exceeds maximum'");

        // Verify repository.save was NEVER called
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void saveSingleNote_textNoteWithNoFile_noFileSizeValidation() {
        // Arrange - TEXT note without file attachment (file size validation should be skipped)
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.TEXT);
        note.setContent("This is a text note without any file");
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(4L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.TEXT);
        savedNote.setContent("This is a text note without any file");
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save TEXT note (no file size check should occur)
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully (no file size validation for TEXT notes)
        assertNotNull(result);
        assertNull(result.getAttachmentData(), "TEXT note should have no attachment data");
        verify(noteRepository, times(1)).save(any(Note.class));
    }
}
