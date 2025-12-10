package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.medicalHistoryTests;

import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.services.MedicalHistoryService;
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
 * Test Case: Delete Medical History
 * Use Case: Delete patient medical history
 * Requirement: 3.1.1 - The system shall allow authorized users to delete patient records
 * Technique: Equivalence Partitioning - Testing valid deletion and error cases
 */
@ExtendWith(MockitoExtension.class)
class deleteMedicalHistoryTest {

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    @Test
    void deleteMedicalHistory_validHistoryAndPatient_successfullyDeletes() {
        // Arrange
        Long patientId = 1L;
        Long historyId = 10L;
        
        MedicalHistory historyToDelete = new MedicalHistory();
        historyToDelete.setId(historyId);
        historyToDelete.setPatientId(patientId);
        historyToDelete.setDiagnosis("Diagnosis to delete");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(historyToDelete));

        // Act
        assertDoesNotThrow(() -> medicalHistoryService.deleteMedicalHistory(patientId, historyId));

        // Assert
        verify(medicalHistoryRepository, times(1)).findById(historyId);
        verify(medicalHistoryRepository, times(1)).delete(historyToDelete);
    }

    @Test
    void deleteMedicalHistory_historyNotFound_throwsNotFoundException() {
        // Arrange
        Long patientId = 1L;
        Long nonExistentHistoryId = 999L;

        when(medicalHistoryRepository.findById(nonExistentHistoryId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                medicalHistoryService.deleteMedicalHistory(patientId, nonExistentHistoryId)
        );

        assertTrue(ex.getMessage().contains("not found"),
                "Expected error message about record not found but got: " + ex.getMessage());

        verify(medicalHistoryRepository, times(1)).findById(nonExistentHistoryId);
        verify(medicalHistoryRepository, never()).delete(any(MedicalHistory.class));
    }

    @Test
    void deleteMedicalHistory_historyDoesNotBelongToPatient_throwsException() {
        // Arrange
        Long patientId = 1L;
        Long anotherPatientId = 2L;
        Long historyId = 20L;
        
        MedicalHistory historyFromAnotherPatient = new MedicalHistory();
        historyFromAnotherPatient.setId(historyId);
        historyFromAnotherPatient.setPatientId(anotherPatientId);
        historyFromAnotherPatient.setDiagnosis("Another patient's diagnosis");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(historyFromAnotherPatient));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                medicalHistoryService.deleteMedicalHistory(patientId, historyId)
        );

        assertTrue(ex.getMessage().contains("does not belong"),
                "Expected error message about ownership but got: " + ex.getMessage());

        verify(medicalHistoryRepository, times(1)).findById(historyId);
        verify(medicalHistoryRepository, never()).delete(any(MedicalHistory.class));
    }

    @Test
    void deleteMedicalHistory_historyPatientIdNull_throwsException() {
        // Arrange
        Long patientId = 1L;
        Long historyId = 30L;
        
        MedicalHistory historyWithNullPatientId = new MedicalHistory();
        historyWithNullPatientId.setId(historyId);
        historyWithNullPatientId.setPatientId(null); // Null patient ID
        historyWithNullPatientId.setDiagnosis("Orphaned history");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(historyWithNullPatientId));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                medicalHistoryService.deleteMedicalHistory(patientId, historyId)
        );

        assertTrue(ex.getMessage().contains("does not belong"),
                "Expected error message but got: " + ex.getMessage());

        verify(medicalHistoryRepository, times(1)).findById(historyId);
        verify(medicalHistoryRepository, never()).delete(any(MedicalHistory.class));
    }

    @Test
    void deleteMedicalHistory_multipleHistoriesForPatient_deletesOnlySpecific() {
        // Arrange
        Long patientId = 2L;
        Long historyIdToDelete = 40L;
        
        MedicalHistory historyToDelete = new MedicalHistory();
        historyToDelete.setId(historyIdToDelete);
        historyToDelete.setPatientId(patientId);
        historyToDelete.setDiagnosis("Delete this diagnosis");

        when(medicalHistoryRepository.findById(historyIdToDelete)).thenReturn(Optional.of(historyToDelete));

        // Act
        assertDoesNotThrow(() -> medicalHistoryService.deleteMedicalHistory(patientId, historyIdToDelete));

        // Assert - Verify only the specific history was deleted
        verify(medicalHistoryRepository, times(1)).delete(historyToDelete);
        verify(medicalHistoryRepository, times(1)).findById(historyIdToDelete);
    }

    @Test
    void deleteMedicalHistory_wrongPatientIdProvided_doesNotDelete() {
        // Arrange
        Long correctPatientId = 3L;
        Long wrongPatientId = 99L;
        Long historyId = 50L;
        
        MedicalHistory history = new MedicalHistory();
        history.setId(historyId);
        history.setPatientId(correctPatientId); // Belongs to correct patient
        history.setDiagnosis("Some diagnosis");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(history));

        // Act & Assert - Attempting to delete with wrong patient ID
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                medicalHistoryService.deleteMedicalHistory(wrongPatientId, historyId)
        );

        assertTrue(ex.getMessage().contains("does not belong"));
        verify(medicalHistoryRepository, never()).delete(any(MedicalHistory.class));
    }
}
