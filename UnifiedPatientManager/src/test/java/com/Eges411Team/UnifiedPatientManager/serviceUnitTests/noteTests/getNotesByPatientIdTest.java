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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Get Notes for Patient
 * Use Case: Retrieve patient notes
 * Requirement: 3.1.1 - The system shall allow users to retrieve patient notes
 * Technique: Equivalence Partitioning and Boundary Testing
 */
@ExtendWith(MockitoExtension.class)
class getNotesByPatientIdTest {

    @Mock
    private NoteRepo noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void getNotesByPatientId_validPatientWithNotes_returnsNotes() {
        // Arrange
        Long patientId = 1L;
        
        Note note1 = new Note();
        note1.setId(1L);
        note1.setPatientId(patientId);
        note1.setDoctorId(5L);
        note1.setNoteType(NoteType.TEXT);
        note1.setContent("Patient presented with fever");
        note1.setTimestamp(LocalDateTime.of(2025, 11, 15, 10, 30));

        Note note2 = new Note();
        note2.setId(2L);
        note2.setPatientId(patientId);
        note2.setDoctorId(5L);
        note2.setNoteType(NoteType.TEXT);
        note2.setContent("Follow-up visit scheduled");
        note2.setTimestamp(LocalDateTime.of(2025, 11, 16, 14, 0));

        when(noteRepository.findAllByPatientId(patientId))
                .thenReturn(Arrays.asList(note1, note2));

        // Act
        List<Note> result = noteService.getNotesByPatientId(patientId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 notes");
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Patient presented with fever", result.get(0).getContent());
        
        verify(noteRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getNotesByPatientId_validPatientWithoutNotes_returnsEmptyList() {
        // Arrange
        Long patientId = 3L;
        
        when(noteRepository.findAllByPatientId(patientId))
                .thenReturn(new ArrayList<>());

        // Act
        List<Note> result = noteService.getNotesByPatientId(patientId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty for patient with no notes");
        
        verify(noteRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getNotesByPatientId_singleNote_returnsListWithSingleNote() {
        // Arrange
        Long patientId = 2L;
        
        Note note = new Note();
        note.setId(5L);
        note.setPatientId(patientId);
        note.setNoteType(NoteType.FILE);
        note.setAttachmentName("lab_results.pdf");
        note.setTimestamp(LocalDateTime.of(2025, 11, 12, 9, 15));

        when(noteRepository.findAllByPatientId(patientId))
                .thenReturn(Arrays.asList(note));

        // Act
        List<Note> result = noteService.getNotesByPatientId(patientId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals("lab_results.pdf", result.get(0).getAttachmentName());
        
        verify(noteRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getNotesByPatientId_multipleNotes_returnsAllInChronologicalOrder() {
        // Arrange
        Long patientId = 4L;
        
        Note note1 = new Note();
        note1.setId(10L);
        note1.setPatientId(patientId);
        note1.setContent("First note");
        note1.setTimestamp(LocalDateTime.of(2025, 11, 10, 8, 0));

        Note note2 = new Note();
        note2.setId(11L);
        note2.setPatientId(patientId);
        note2.setContent("Second note");
        note2.setTimestamp(LocalDateTime.of(2025, 11, 11, 9, 30));

        Note note3 = new Note();
        note3.setId(12L);
        note3.setPatientId(patientId);
        note3.setContent("Third note");
        note3.setTimestamp(LocalDateTime.of(2025, 11, 12, 11, 0));

        when(noteRepository.findAllByPatientId(patientId))
                .thenReturn(Arrays.asList(note1, note2, note3));

        // Act
        List<Note> result = noteService.getNotesByPatientId(patientId);

        // Assert
        assertEquals(3, result.size());
        assertEquals("First note", result.get(0).getContent());
        assertEquals("Second note", result.get(1).getContent());
        assertEquals("Third note", result.get(2).getContent());
        
        verify(noteRepository, times(1)).findAllByPatientId(patientId);
    }
}
