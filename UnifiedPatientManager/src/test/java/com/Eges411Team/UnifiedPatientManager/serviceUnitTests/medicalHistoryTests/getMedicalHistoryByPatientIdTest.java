package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.medicalHistoryTests;

import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.services.MedicalHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Get Medical History by Patient ID
 * Use Case: Retrieve patient medical history
 * Requirement: 3.1.1 - The system shall allow authorized users to view patient records
 * Technique: Equivalence Partitioning and Boundary Testing
 */
@ExtendWith(MockitoExtension.class)
class getMedicalHistoryByPatientIdTest {

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    @Test
    void getMedicalHistoryByPatientId_validPatientWithHistory_returnsHistory() {
        // Arrange
        Long patientId = 1L;
        
        MedicalHistory history1 = new MedicalHistory();
        history1.setId(1L);
        history1.setPatientId(patientId);
        history1.setDoctorId(5L);
        history1.setDiagnosis("Type 2 Diabetes");
        history1.setFrequency("Quarterly checkup");

        MedicalHistory history2 = new MedicalHistory();
        history2.setId(2L);
        history2.setPatientId(patientId);
        history2.setDoctorId(6L);
        history2.setDiagnosis("Hypertension");
        history2.setFrequency("Monthly check");

        when(medicalHistoryRepository.findAllByPatientId(patientId))
                .thenReturn(Arrays.asList(history1, history2));

        // Act
        List<MedicalHistory> result = medicalHistoryService.getMedicalHistoryByPatientId(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Type 2 Diabetes", result.get(0).getDiagnosis());
        assertEquals("Hypertension", result.get(1).getDiagnosis());
        
        verify(medicalHistoryRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getMedicalHistoryByPatientId_validPatientWithoutHistory_returnsEmptyList() {
        // Arrange
        Long patientId = 3L;
        
        when(medicalHistoryRepository.findAllByPatientId(patientId))
                .thenReturn(new ArrayList<>());

        // Act
        List<MedicalHistory> result = medicalHistoryService.getMedicalHistoryByPatientId(patientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(medicalHistoryRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getMedicalHistoryByPatientId_singleHistoryRecord_returnsList() {
        // Arrange
        Long patientId = 2L;
        
        MedicalHistory history = new MedicalHistory();
        history.setId(10L);
        history.setPatientId(patientId);
        history.setDiagnosis("Asthma");

        when(medicalHistoryRepository.findAllByPatientId(patientId))
                .thenReturn(Arrays.asList(history));

        // Act
        List<MedicalHistory> result = medicalHistoryService.getMedicalHistoryByPatientId(patientId);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Asthma", result.get(0).getDiagnosis());
        
        verify(medicalHistoryRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getMedicalHistoryByPatientId_multipleRecords_returnsAll() {
        // Arrange
        Long patientId = 4L;
        
        MedicalHistory h1 = new MedicalHistory();
        h1.setId(20L);
        h1.setPatientId(patientId);
        h1.setDiagnosis("Diagnosis 1");

        MedicalHistory h2 = new MedicalHistory();
        h2.setId(21L);
        h2.setPatientId(patientId);
        h2.setDiagnosis("Diagnosis 2");

        MedicalHistory h3 = new MedicalHistory();
        h3.setId(22L);
        h3.setPatientId(patientId);
        h3.setDiagnosis("Diagnosis 3");

        when(medicalHistoryRepository.findAllByPatientId(patientId))
                .thenReturn(Arrays.asList(h1, h2, h3));

        // Act
        List<MedicalHistory> result = medicalHistoryService.getMedicalHistoryByPatientId(patientId);

        // Assert
        assertEquals(3, result.size());
        
        verify(medicalHistoryRepository, times(1)).findAllByPatientId(patientId);
    }
}
