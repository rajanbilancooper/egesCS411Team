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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Update Medical History
 * Use Case: Update patient medical history
 * Requirement: 3.1.1 - The system shall allow authorized users to update patient records
 * Technique: Equivalence Partitioning - Testing valid updates and error cases
 */
@ExtendWith(MockitoExtension.class)
class updateMedicalHistoryTest {

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    @Test
    void updateMedicalHistory_validUpdate_successfullyUpdates() throws Exception {
        // Arrange
        Long patientId = 1L;
        Long historyId = 10L;
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = sdf.parse("01/01/2023");
        Date endDate = sdf.parse("12/31/2023");

        MedicalHistory existingHistory = new MedicalHistory();
        existingHistory.setId(historyId);
        existingHistory.setPatientId(patientId);
        existingHistory.setDoctorId(5L);
        existingHistory.setDiagnosis("Type 2 Diabetes");
        existingHistory.setFrequency("Quarterly");
        existingHistory.setStartDate(startDate);
        existingHistory.setEndDate(endDate);

        MedicalHistory updateData = new MedicalHistory();
        updateData.setDiagnosis("Type 2 Diabetes Mellitus");
        updateData.setFrequency("Bi-annually");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(existingHistory));
        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MedicalHistory result = medicalHistoryService.updateMedicalHistory(patientId, historyId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals("Type 2 Diabetes Mellitus", result.getDiagnosis());
        assertEquals("Bi-annually", result.getFrequency());
        assertEquals(patientId, result.getPatientId());
        
        verify(medicalHistoryRepository, times(1)).save(any(MedicalHistory.class));
    }

    @Test
    void updateMedicalHistory_historyNotFound_throwsNotFoundException() {
        // Arrange
        Long patientId = 1L;
        Long nonExistentHistoryId = 999L;

        MedicalHistory updateData = new MedicalHistory();
        updateData.setDiagnosis("New diagnosis");

        when(medicalHistoryRepository.findById(nonExistentHistoryId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                medicalHistoryService.updateMedicalHistory(patientId, nonExistentHistoryId, updateData)
        );

        assertTrue(ex.getMessage().contains("not found"),
                "Expected error message about record not found but got: " + ex.getMessage());

        verify(medicalHistoryRepository, never()).save(any(MedicalHistory.class));
    }

    @Test
    void updateMedicalHistory_historyDoesNotBelongToPatient_throwsException() {
        // Arrange
        Long patientId = 1L;
        Long anotherPatientId = 2L;
        Long historyId = 20L;
        
        MedicalHistory historyFromAnotherPatient = new MedicalHistory();
        historyFromAnotherPatient.setId(historyId);
        historyFromAnotherPatient.setPatientId(anotherPatientId);
        historyFromAnotherPatient.setDiagnosis("Another patient's diagnosis");

        MedicalHistory updateData = new MedicalHistory();
        updateData.setDiagnosis("Trying to update");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(historyFromAnotherPatient));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                medicalHistoryService.updateMedicalHistory(patientId, historyId, updateData)
        );

        assertTrue(ex.getMessage().contains("does not belong"),
                "Expected error message about ownership but got: " + ex.getMessage());

        verify(medicalHistoryRepository, never()).save(any(MedicalHistory.class));
    }

    @Test
    void updateMedicalHistory_validDateRangeAtBoundary_successfullyUpdates() throws Exception {
        // Arrange - Valid case: end date equals start date
        Long patientId = 4L;
        Long historyId = 50L;
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date sameDate = sdf.parse("07/20/2024");

        MedicalHistory existingHistory = new MedicalHistory();
        existingHistory.setId(historyId);
        existingHistory.setPatientId(patientId);
        existingHistory.setStartDate(sameDate);

        MedicalHistory updateData = new MedicalHistory();
        updateData.setStartDate(sameDate);
        updateData.setEndDate(sameDate); // Same day is valid

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(existingHistory));
        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MedicalHistory result = medicalHistoryService.updateMedicalHistory(patientId, historyId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals(sameDate, result.getEndDate());
        
        verify(medicalHistoryRepository, times(1)).save(any(MedicalHistory.class));
    }
}
