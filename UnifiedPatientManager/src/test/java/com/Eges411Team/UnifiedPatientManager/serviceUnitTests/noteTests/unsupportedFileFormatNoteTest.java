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
 * Test Case 19: Unsupported file format rejected
 * Use Case: Doctor Attaches physical notes to patient record
 * Requirement: 3.1.1 - The system shall grant authorised users to upload and view supporting files 
 *              such as scanned physical documents, images, or audio notes in a patient's record
 * Technique: Equivalence Partitioning – file type partition (supported vs unsupported)
 */
@ExtendWith(MockitoExtension.class)
class unsupportedFileFormatNoteTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void saveSingleNote_unsupportedFileFormat_throwsUnsupportedFileTypeException() {
        // Arrange - Build a Note with unsupported file format (.gif)
        byte[] fileData = "GIF89a fake gif data".getBytes();
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Patient reports mild headache and was advised to rest.");
        note.setAttachmentName("notes.gif"); // Unsupported file format
        note.setAttachmentData(fileData);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        // Act & Assert - expect ResponseStatusException for unsupported file type
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                noteService.saveSingleNote(note)
        );

        assertTrue(ex.getReason().contains("Unsupported file type"),
                "Expected error message to contain 'Unsupported file type' but got: " + ex.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode(),
                "Expected HTTP status to be BAD_REQUEST");

        // Verify repository.save was NEVER called due to validation failure
        verify(noteRepository, never()).save(any(Note.class));

        // Verify patient existence was checked
        verify(userRepository, times(1)).findById(123L);
    }

    @Test
    void saveSingleNote_unsupportedFileFormat_noFileUploaded() {
        // Arrange - Unsupported file format
        byte[] fileData = "Fake executable data".getBytes();
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Patient reports mild headache and was advised to rest.");
        note.setAttachmentName("virus.exe"); // Unsupported file format
        note.setAttachmentData(fileData);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        // Act & Assert - Verify exception is thrown
        assertThrows(ResponseStatusException.class, () ->
                noteService.saveSingleNote(note)
        );

        // Verify NO file is uploaded and stored in the database
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void saveSingleNote_supportedPdfFormat_successfullySaves() {
        // Arrange - Supported PDF file format
        byte[] fileData = "PDF file content".getBytes();
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Patient lab results");
        note.setAttachmentName("results.pdf"); // Supported file format
        note.setAttachmentData(fileData);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.FILE);
        savedNote.setContent("Patient lab results");
        savedNote.setAttachmentName("results.pdf");
        savedNote.setAttachmentData(fileData);
        savedNote.setTimestamp(LocalDateTime.now());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with supported format
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals("results.pdf", result.getAttachmentName());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_supportedPngFormat_successfullySaves() {
        // Arrange - Supported PNG file format
        byte[] fileData = "PNG image data".getBytes();
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("X-ray image");
        note.setAttachmentName("xray.png"); // Supported file format
        note.setAttachmentData(fileData);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(2L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.FILE);
        savedNote.setContent("X-ray image");
        savedNote.setAttachmentName("xray.png");
        savedNote.setAttachmentData(fileData);

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with supported format
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals("xray.png", result.getAttachmentName());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void saveSingleNote_supportedJpgFormat_successfullySaves() {
        // Arrange - Supported JPG file format
        byte[] fileData = "JPG image data".getBytes();
        
        Note note = new Note();
        note.setPatientId(123L);
        note.setDoctorId(456L);
        note.setNoteType(NoteType.FILE);
        note.setContent("Patient photo");
        note.setAttachmentName("photo.jpg"); // Supported file format
        note.setAttachmentData(fileData);
        note.setTimestamp(LocalDateTime.now());

        // Mock that patient exists
        when(userRepository.findById(123L)).thenReturn(Optional.of(new User()));

        Note savedNote = new Note();
        savedNote.setId(3L);
        savedNote.setPatientId(123L);
        savedNote.setDoctorId(456L);
        savedNote.setNoteType(NoteType.FILE);
        savedNote.setContent("Patient photo");
        savedNote.setAttachmentName("photo.jpg");
        savedNote.setAttachmentData(fileData);

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // Act - Save note with supported format
        Note result = noteService.saveSingleNote(note);

        // Assert - Verify it was saved successfully
        assertNotNull(result);
        assertEquals("photo.jpg", result.getAttachmentName());
        verify(noteRepository, times(1)).save(any(Note.class));
    }
}
