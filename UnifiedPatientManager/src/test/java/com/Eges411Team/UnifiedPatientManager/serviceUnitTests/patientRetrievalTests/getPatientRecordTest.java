package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientRetrievalTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.PatientRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Get/Retrieve Patient Record
 * Tests PatientRecordService.getPatientRecord() and related methods
 */
@ExtendWith(MockitoExtension.class)
class getPatientRecordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @InjectMocks
    private PatientRecordService patientRecordService;

    @Test
    void getPatientRecord_validPatient_successfullyRetrievesRecord() {
        // Arrange - Create valid patient
        Long patientId = 123L;

        User patient = new User();
        patient.setId(patientId);
        patient.setFirstName("Rhea");
        patient.setLastName("Thakur");
        patient.setEmail("rhea@email.com");
        patient.setPhoneNumber("555-1234");
        patient.setAddress("123 Main St");
        patient.setDateOfBirth(LocalDateTime.of(1990, 5, 15, 0, 0));
        patient.setGender("Female");
        patient.setHeight("5'6\"");
        patient.setWeight("130 lbs");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        // Act
        PatientRecordDTO result = patientRecordService.getPatientRecord(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals("Rhea", result.getFirstName());
        assertEquals("Thakur", result.getLastName());
        assertEquals("rhea@email.com", result.getEmail());
        assertEquals("555-1234", result.getPhoneNumber());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("Female", result.getGender());
        assertEquals("5'6\"", result.getHeight());
        assertEquals("130 lbs", result.getWeight());
        assertNotNull(result.getAllergies());
        assertNotNull(result.getMedications());
        assertNotNull(result.getMedicalHistory());

        verify(userRepository, times(1)).findById(patientId);
    }

    @Test
    void getPatientRecord_patientNotFound_throwsException() {
        // Arrange - Test patient not found (orElseThrow branch)
        Long nonExistentPatientId = 999L;

        when(userRepository.findById(nonExistentPatientId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.getPatientRecord(nonExistentPatientId)
        );

        assertTrue(ex.getMessage().contains("User not found"));

        verify(userRepository, times(1)).findById(nonExistentPatientId);
        verify(allergyRepository, never()).findAllByPatientId(any());
    }

    @Test
    void getPatientRecord_patientWithAllergies_mapsAllergiesCorrectly() {
        // Arrange - Patient with allergies (tests allergy loop)
        Long patientId = 123L;

        User patient = new User();
        patient.setId(patientId);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("john@email.com");
        patient.setPhoneNumber("555-5678");
        patient.setAddress("456 Oak Ave");
        patient.setDateOfBirth(LocalDateTime.of(1985, 3, 10, 0, 0));
        patient.setGender("Male");

        Allergy allergy1 = new Allergy();
        allergy1.setId(1L);
        allergy1.setPatientId(patientId);
        allergy1.setSubstance("Peanuts");
        allergy1.setSeverity("HIGH");
        allergy1.setReaction("Anaphylaxis");

        Allergy allergy2 = new Allergy();
        allergy2.setId(2L);
        allergy2.setPatientId(patientId);
        allergy2.setSubstance("Shellfish");
        allergy2.setSeverity("MEDIUM");
        allergy2.setReaction("Hives");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(Arrays.asList(allergy1, allergy2));
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        // Act
        PatientRecordDTO result = patientRecordService.getPatientRecord(patientId);

        // Assert - Verify allergies were mapped
        assertNotNull(result.getAllergies());
        assertEquals(2, result.getAllergies().size());
        assertEquals("Peanuts", result.getAllergies().get(0).getSubstance());
        assertEquals("HIGH", result.getAllergies().get(0).getSeverity());
        assertEquals("Shellfish", result.getAllergies().get(1).getSubstance());

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getPatientRecord_patientWithMedications_mapsMedicationsCorrectly() {
        // Arrange - Patient with medications (tests medication loop)
        Long patientId = 123L;

        User patient = new User();
        patient.setId(patientId);
        patient.setFirstName("Jane");
        patient.setLastName("Smith");
        patient.setEmail("jane@email.com");
        patient.setPhoneNumber("555-9999");
        patient.setAddress("789 Pine St");
        patient.setDateOfBirth(LocalDateTime.of(1988, 7, 20, 0, 0));
        patient.setGender("Female");

        Medication med1 = new Medication();
        med1.setId(1L);
        med1.setPatientId(patientId);
        med1.setDrugName("Aspirin");
        med1.setDose("100mg");
        med1.setFrequency("Daily");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(Arrays.asList(med1));
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        // Act
        PatientRecordDTO result = patientRecordService.getPatientRecord(patientId);

        // Assert - Verify medications were mapped
        assertNotNull(result.getMedications());
        assertEquals(1, result.getMedications().size());
        assertEquals("Aspirin", result.getMedications().get(0).getDrugName());
        assertEquals("100mg", result.getMedications().get(0).getDose());

        verify(medicationRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getPatientRecord_patientWithMedicalHistory_mapsMedicalHistoryCorrectly() {
        // Arrange - Patient with medical history (tests medical history loop & date conversion)
        Long patientId = 123L;

        User patient = new User();
        patient.setId(patientId);
        patient.setFirstName("Bob");
        patient.setLastName("Johnson");
        patient.setEmail("bob@email.com");
        patient.setPhoneNumber("555-0000");
        patient.setAddress("321 Elm St");
        patient.setDateOfBirth(LocalDateTime.of(1970, 1, 1, 0, 0));
        patient.setGender("Male");

        MedicalHistory history1 = new MedicalHistory();
        history1.setId(1L);
        history1.setPatientId(patientId);
        history1.setDoctorId(456L);
        history1.setDiagnosis("High Blood Pressure");
        history1.setStartDate(Date.valueOf("2023-01-15"));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(Arrays.asList(history1));

        // Act
        PatientRecordDTO result = patientRecordService.getPatientRecord(patientId);

        // Assert - Verify medical history was mapped with date handling
        assertNotNull(result.getMedicalHistory());
        assertEquals(1, result.getMedicalHistory().size());
        assertEquals("High Blood Pressure", result.getMedicalHistory().get(0).getNotes());
        assertEquals(456L, result.getMedicalHistory().get(0).getDoctorId());
        assertNotNull(result.getMedicalHistory().get(0).getStartDate());

        verify(medicalHistoryRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getPatientRecord_patientWithCompleteData_returnsCompleteRecord() {
        // Arrange - Patient with allergies, medications, and medical history
        Long patientId = 123L;

        User patient = new User();
        patient.setId(patientId);
        patient.setFirstName("Alice");
        patient.setLastName("Brown");
        patient.setEmail("alice@email.com");
        patient.setPhoneNumber("555-1111");
        patient.setAddress("999 Maple St");
        patient.setDateOfBirth(LocalDateTime.of(1992, 11, 22, 0, 0));
        patient.setGender("Female");
        patient.setHeight("5'4\"");
        patient.setWeight("125 lbs");

        Allergy allergy = new Allergy();
        allergy.setId(1L);
        allergy.setPatientId(patientId);
        allergy.setSubstance("Dairy");
        allergy.setSeverity("MEDIUM");
        allergy.setReaction("Digestive upset");

        Medication medication = new Medication();
        medication.setId(1L);
        medication.setPatientId(patientId);
        medication.setDrugName("Ibuprofen");
        medication.setDose("200mg");
        medication.setFrequency("As needed");

        MedicalHistory history = new MedicalHistory();
        history.setId(1L);
        history.setPatientId(patientId);
        history.setDoctorId(789L);
        history.setDiagnosis("Migraine");
        history.setStartDate(Date.valueOf("2024-01-01"));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(Arrays.asList(allergy));
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(Arrays.asList(medication));
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(Arrays.asList(history));

        // Act
        PatientRecordDTO result = patientRecordService.getPatientRecord(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals("Alice", result.getFirstName());
        assertEquals(1, result.getAllergies().size());
        assertEquals(1, result.getMedications().size());
        assertEquals(1, result.getMedicalHistory().size());
        assertEquals("5'4\"", result.getHeight());

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
        verify(medicationRepository, times(1)).findAllByPatientId(patientId);
        verify(medicalHistoryRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getPatientRecord_medicalHistoryWithNullStartDate_handlesNull() {
        // Arrange - Test null startDate handling in medical history loop
        Long patientId = 123L;

        User patient = new User();
        patient.setId(patientId);
        patient.setFirstName("Charlie");
        patient.setLastName("Davis");
        patient.setEmail("charlie@email.com");
        patient.setPhoneNumber("555-2222");
        patient.setAddress("111 Cedar St");
        patient.setDateOfBirth(LocalDateTime.of(1995, 6, 30, 0, 0));
        patient.setGender("Male");

        MedicalHistory historyWithNullDate = new MedicalHistory();
        historyWithNullDate.setId(1L);
        historyWithNullDate.setPatientId(patientId);
        historyWithNullDate.setDoctorId(999L);
        historyWithNullDate.setDiagnosis("Flu");
        historyWithNullDate.setStartDate(null);  // null startDate

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(Arrays.asList(historyWithNullDate));

        // Act
        PatientRecordDTO result = patientRecordService.getPatientRecord(patientId);

        // Assert - Should not throw exception with null date
        assertNotNull(result);
        assertEquals(1, result.getMedicalHistory().size());
        assertEquals("Flu", result.getMedicalHistory().get(0).getNotes());

        verify(medicalHistoryRepository, times(1)).findAllByPatientId(patientId);
    }
}
