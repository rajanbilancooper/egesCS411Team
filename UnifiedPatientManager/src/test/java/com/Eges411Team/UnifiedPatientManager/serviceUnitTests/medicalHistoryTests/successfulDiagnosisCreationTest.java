package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.medicalHistoryTests;

import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.services.MedicalHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case 16: Successful Creation of Diagnosis in Medical History
 * Use Case: Storing detailed patient information
 * Requirement: 3.1.1 - The system shall allow authorized users to create, view, update, and edit 
 *              patient records, including diagnoses and full medical history entries
 * Technique: Equivalence Partitioning - "all required fields present" vs "missing required fields"
 */
@ExtendWith(MockitoExtension.class)
class successfulDiagnosisCreationTest {

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    @Test
    void saveMedicalHistory_allRequiredFieldsPresent_successfullySavesDiagnosis() throws Exception {
        // Arrange - Build a MedicalHistory with all required fields as per test case
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = sdf.parse("11/21/2025");
        Date endDate = sdf.parse("11/21/2026");

        MedicalHistory diagnosis = new MedicalHistory();
        diagnosis.setPatientId(3L); // Patient ID 3 exists
        diagnosis.setDoctorId(15L); // Doctor ID 15
        diagnosis.setDiagnosis("Type 2 Diabetes Mellitus");
        diagnosis.setFrequency("Follow-up every 3 months");
        diagnosis.setStartDate(startDate);
        diagnosis.setEndDate(endDate);
        diagnosis.setPrescribeMedication(false); // "Also prescribe a medication?" is unchecked

        // Mock the repository to return the saved entity with an ID
        MedicalHistory savedDiagnosis = new MedicalHistory();
        savedDiagnosis.setId(1L);
        savedDiagnosis.setPatientId(3L);
        savedDiagnosis.setDoctorId(15L);
        savedDiagnosis.setDiagnosis("Type 2 Diabetes Mellitus");
        savedDiagnosis.setFrequency("Follow-up every 3 months");
        savedDiagnosis.setStartDate(startDate);
        savedDiagnosis.setEndDate(endDate);
        savedDiagnosis.setPrescribeMedication(false);

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenReturn(savedDiagnosis);

        // Act - Call the service method to save the diagnosis
        MedicalHistory result = medicalHistoryService.saveMedicalHistory(diagnosis);

        // Assert - Verify the diagnosis was saved with correct values
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Diagnosis should have an ID after saving");
        assertEquals(3L, result.getPatientId(), "Patient ID should be 3");
        assertEquals(15L, result.getDoctorId(), "Doctor ID should be 15");
        assertEquals("Type 2 Diabetes Mellitus", result.getDiagnosis(), 
                "Diagnosis should be 'Type 2 Diabetes Mellitus'");
        assertEquals("Follow-up every 3 months", result.getFrequency(), 
                "Frequency should be 'Follow-up every 3 months'");
        assertEquals(startDate, result.getStartDate(), "Start date should be 11/21/2025");
        assertEquals(endDate, result.getEndDate(), "End date should be 11/21/2026");
        assertEquals(false, result.getPrescribeMedication(), 
                "Prescribe medication should be false (unchecked)");

        // Verify repository.save was called exactly once
        ArgumentCaptor<MedicalHistory> captor = ArgumentCaptor.forClass(MedicalHistory.class);
        verify(medicalHistoryRepository, times(1)).save(captor.capture());

        // Verify the entity passed to save had the correct values
        MedicalHistory capturedEntity = captor.getValue();
        assertEquals(3L, capturedEntity.getPatientId(), "Captured entity should have patient ID 3");
        assertEquals(15L, capturedEntity.getDoctorId(), "Captured entity should have doctor ID 15");
        assertEquals("Type 2 Diabetes Mellitus", capturedEntity.getDiagnosis(), 
                "Captured entity should have correct diagnosis");
        assertEquals("Follow-up every 3 months", capturedEntity.getFrequency(), 
                "Captured entity should have correct frequency");
        assertEquals(startDate, capturedEntity.getStartDate(), 
                "Captured entity should have correct start date");
        assertEquals(endDate, capturedEntity.getEndDate(), 
                "Captured entity should have correct end date");
        assertEquals(false, capturedEntity.getPrescribeMedication(), 
                "Captured entity should have prescribe medication as false");
    }

    @Test
    void saveMedicalHistory_validDatesAndRequiredFields_noExceptionThrown() throws Exception {
        // Arrange - Valid diagnosis with end date after start date
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = sdf.parse("11/21/2025");
        Date endDate = sdf.parse("11/21/2026");

        MedicalHistory diagnosis = new MedicalHistory();
        diagnosis.setPatientId(3L);
        diagnosis.setDoctorId(15L);
        diagnosis.setDiagnosis("Type 2 Diabetes Mellitus");
        diagnosis.setFrequency("Follow-up every 3 months");
        diagnosis.setStartDate(startDate);
        diagnosis.setEndDate(endDate);
        diagnosis.setPrescribeMedication(false);

        MedicalHistory savedDiagnosis = new MedicalHistory();
        savedDiagnosis.setId(1L);
        savedDiagnosis.setPatientId(3L);
        savedDiagnosis.setDoctorId(15L);
        savedDiagnosis.setDiagnosis("Type 2 Diabetes Mellitus");
        savedDiagnosis.setFrequency("Follow-up every 3 months");
        savedDiagnosis.setStartDate(startDate);
        savedDiagnosis.setEndDate(endDate);
        savedDiagnosis.setPrescribeMedication(false);

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenReturn(savedDiagnosis);

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> {
            MedicalHistory result = medicalHistoryService.saveMedicalHistory(diagnosis);
            assertNotNull(result);
        });

        // Verify the save was called
        verify(medicalHistoryRepository, times(1)).save(any(MedicalHistory.class));
    }
}
