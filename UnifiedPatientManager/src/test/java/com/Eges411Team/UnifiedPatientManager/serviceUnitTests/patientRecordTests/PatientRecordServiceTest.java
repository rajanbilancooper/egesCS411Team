package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientRecordTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRecordUpdateDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.PatientRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientRecordServiceTest {

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

    private User testUser;
    private Allergy testAllergy;
    private Medication testMedication;
    private MedicalHistory testMedicalHistory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPhoneNumber("555-123-4567");
        testUser.setAddress("123 Main St");
        testUser.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));
        testUser.setGender("M");
        testUser.setHeight("180cm");
        testUser.setWeight("75kg");
        testUser.setUsername("johndoe");
        testUser.setPassword("hashedpass123");

        testAllergy = new Allergy();
        testAllergy.setId(1L);
        testAllergy.setPatientId(1L);
        testAllergy.setSubstance("Penicillin");
        testAllergy.setReaction("Rash");
        testAllergy.setSeverity("Moderate");

        testMedication = new Medication();
        testMedication.setId(1L);
        testMedication.setPatientId(1L);
        testMedication.setDrugName("Aspirin");
        testMedication.setDose("500mg");
        testMedication.setFrequency("Twice daily");

        testMedicalHistory = new MedicalHistory();
        testMedicalHistory.setId(1L);
        testMedicalHistory.setPatientId(1L);
        testMedicalHistory.setDoctorId(10L);
        testMedicalHistory.setDiagnosis("Hypertension");
        testMedicalHistory.setStartDate(new java.util.Date());
    }

    // ===== getPatientRecord Tests =====
    @Test
    void getPatientRecord_userExists_returnsPatientRecordDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(Arrays.asList(testAllergy));
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(Arrays.asList(testMedication));
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(Arrays.asList(testMedicalHistory));

        PatientRecordDTO result = patientRecordService.getPatientRecord(1L);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(1, result.getAllergies().size());
        assertEquals(1, result.getMedications().size());
        assertEquals(1, result.getMedicalHistory().size());
    }

    @Test
    void getPatientRecord_userNotFound_throwsRuntimeException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> patientRecordService.getPatientRecord(999L));
    }

    @Test
    void getPatientRecord_noAllergies_returnsEmptyAllergyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        PatientRecordDTO result = patientRecordService.getPatientRecord(1L);

        assertTrue(result.getAllergies().isEmpty());
        assertTrue(result.getMedications().isEmpty());
        assertTrue(result.getMedicalHistory().isEmpty());
    }

    @Test
    void getPatientRecord_multipleMedicalHistories_convertsAllDates() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(Arrays.asList(testMedicalHistory));

        PatientRecordDTO result = patientRecordService.getPatientRecord(1L);

        assertEquals(1, result.getMedicalHistory().size());
        assertEquals("Hypertension", result.getMedicalHistory().get(0).getNotes());
    }

    // ===== getPatientRecordByFullName Tests =====
    @Test
    void getPatientRecordByFullName_validFullName_returnsPatientRecord() {
        when(userRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        PatientRecordDTO result = patientRecordService.getPatientRecordByFullName("John Doe");

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }

    @Test
    void getPatientRecordByFullName_nullName_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> patientRecordService.getPatientRecordByFullName(null));
    }

    @Test
    void getPatientRecordByFullName_blankName_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> patientRecordService.getPatientRecordByFullName("   "));
    }

    @Test
    void getPatientRecordByFullName_onlyFirstName_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> patientRecordService.getPatientRecordByFullName("John"));
    }

    @Test
    void getPatientRecordByFullName_userNotFound_throwsRuntimeException() {
        when(userRepository.findByFirstNameAndLastName("Jane", "Smith")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> patientRecordService.getPatientRecordByFullName("Jane Smith"));
    }

    @Test
    void getPatientRecordByFullName_withMiddleName_ignoresMiddle() {
        when(userRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        PatientRecordDTO result = patientRecordService.getPatientRecordByFullName("John Michael Doe");

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    // ===== searchPatients Tests =====
    @Test
    void searchPatients_validQuery_returnsMatchingPatients() {
        List<User> mockUsers = Arrays.asList(testUser);
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("John", "John"))
            .thenReturn(mockUsers);

        List<PatientRecordDTO> results = patientRecordService.searchPatients("John");

        assertEquals(1, results.size());
        assertEquals("John", results.get(0).getFirstName());
    }

    @Test
    void searchPatients_nullQuery_returnsEmptyList() {
        List<PatientRecordDTO> results = patientRecordService.searchPatients(null);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchPatients_blankQuery_returnsEmptyList() {
        List<PatientRecordDTO> results = patientRecordService.searchPatients("   ");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchPatients_noMatches_returnsEmptyList() {
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Zzzz", "Zzzz"))
            .thenReturn(new ArrayList<>());

        List<PatientRecordDTO> results = patientRecordService.searchPatients("Zzzz");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchPatients_multipleMatches_returnsAll() {
        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("John");
        user2.setLastName("Smith");
        user2.setEmail("johnsmith@example.com");
        user2.setPhoneNumber("555-5678");

        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("John", "John"))
            .thenReturn(Arrays.asList(testUser, user2));

        List<PatientRecordDTO> results = patientRecordService.searchPatients("John");

        assertEquals(2, results.size());
    }

    // ===== createPatientRecord Tests =====
    @Test
    void createPatientRecord_validUser_createsAndReturnsRecord() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("John", "Doe", testUser.getDateOfBirth()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail("john@example.com")).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        List<Allergy> allergies = Arrays.asList(testAllergy);
        PatientRecordDTO result = patientRecordService.createPatientRecord(testUser, allergies);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userRepository).save(any(User.class));
        verify(allergyRepository).save(any(Allergy.class));
    }

    @Test
    void createPatientRecord_nullUser_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(null, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_nullUsername_throwsRuntimeException() {
        testUser.setUsername(null);
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_blankUsername_throwsRuntimeException() {
        testUser.setUsername("   ");
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_shortPassword_throwsRuntimeException() {
        testUser.setPassword("123");
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_nullFirstName_throwsRuntimeException() {
        testUser.setFirstName(null);
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_nullLastName_throwsRuntimeException() {
        testUser.setLastName(null);
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_nullEmail_throwsRuntimeException() {
        testUser.setEmail(null);
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_blankEmail_throwsRuntimeException() {
        testUser.setEmail("   ");
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_nullDateOfBirth_throwsRuntimeException() {
        testUser.setDateOfBirth(null);
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_futureDateOfBirth_throwsRuntimeException() {
        testUser.setDateOfBirth(LocalDateTime.now().plusYears(1));
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_nullPhoneNumber_throwsRuntimeException() {
        testUser.setPhoneNumber(null);
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_blankPhoneNumber_throwsRuntimeException() {
        testUser.setPhoneNumber("   ");
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_invalidPhoneNumber_throwsRuntimeException() {
        testUser.setPhoneNumber("12345");
        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_duplicateUsername_throwsRuntimeException() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_duplicateNameAndDob_throwsRuntimeException() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("John", "Doe", testUser.getDateOfBirth()))
            .thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_duplicateEmail_throwsRuntimeException() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("John", "Doe", testUser.getDateOfBirth()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail("john@example.com")).thenReturn(Arrays.asList(testUser));

        assertThrows(RuntimeException.class, () -> patientRecordService.createPatientRecord(testUser, new ArrayList<>()));
    }

    @Test
    void createPatientRecord_nullAllergies_createsUserWithoutAllergies() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("John", "Doe", testUser.getDateOfBirth()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail("john@example.com")).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        PatientRecordDTO result = patientRecordService.createPatientRecord(testUser, null);

        assertNotNull(result);
        assertTrue(result.getAllergies().isEmpty());
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    // ===== updatePatientRecord Tests =====
    @Test
    void updatePatientRecord_userNotFound_throwsRuntimeException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> patientRecordService.updatePatientRecord(999L, new PatientRecordUpdateDTO()));
    }

    @Test
    void updatePatientRecord_updateNameAndEmail_successfullyUpdates() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setFirstName("Jane");
        dto.setEmail("jane@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("Jane", "Doe", testUser.getDateOfBirth()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail("jane@example.com")).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void updatePatientRecord_duplicateNameAfterUpdate_throwsRuntimeException() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setFirstName("Jane");
        existingUser.setLastName("Doe");
        existingUser.setDateOfBirth(testUser.getDateOfBirth());

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setFirstName("Jane");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("Jane", "Doe", testUser.getDateOfBirth()))
            .thenReturn(Optional.of(existingUser));

        assertThrows(RuntimeException.class, () -> patientRecordService.updatePatientRecord(1L, dto));
    }

    @Test
    void updatePatientRecord_duplicateEmailAfterUpdate_throwsRuntimeException() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("jane@example.com");

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setEmail("jane@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findAllByEmail("jane@example.com")).thenReturn(Arrays.asList(otherUser));

        assertThrows(RuntimeException.class, () -> patientRecordService.updatePatientRecord(1L, dto));
    }

    @Test
    void updatePatientRecord_addAllergy_successfullyAdds() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        PatientRecordUpdateDTO.AllergyAction allergyAction = new PatientRecordUpdateDTO.AllergyAction();
        allergyAction.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.ADD);
        allergyAction.setSubstance("Peanuts");
        allergyAction.setReaction("Anaphylaxis");
        allergyAction.setSeverity("Severe");
        dto.setAllergyActions(Arrays.asList(allergyAction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(allergyRepository, times(2)).findAllByPatientId(1L);
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updatePatientRecord_updateAllergy_successfullyUpdates() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        PatientRecordUpdateDTO.AllergyAction allergyAction = new PatientRecordUpdateDTO.AllergyAction();
        allergyAction.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.UPDATE);
        allergyAction.setAllergyId(1L);
        allergyAction.setSubstance("Peanuts");
        allergyAction.setReaction("Swelling");
        allergyAction.setSeverity("Severe");
        dto.setAllergyActions(Arrays.asList(allergyAction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L))
            .thenReturn(Arrays.asList(testAllergy))
            .thenReturn(Arrays.asList(testAllergy));
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updatePatientRecord_updateNonExistentAllergy_createsNew() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        PatientRecordUpdateDTO.AllergyAction allergyAction = new PatientRecordUpdateDTO.AllergyAction();
        allergyAction.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.UPDATE);
        allergyAction.setAllergyId(999L);
        allergyAction.setSubstance("Shellfish");
        allergyAction.setReaction("Itching");
        allergyAction.setSeverity("Mild");
        dto.setAllergyActions(Arrays.asList(allergyAction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L))
            .thenReturn(new ArrayList<>())
            .thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updatePatientRecord_removeAllergy_successfullyRemoves() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        PatientRecordUpdateDTO.AllergyAction allergyAction = new PatientRecordUpdateDTO.AllergyAction();
        allergyAction.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.REMOVE);
        allergyAction.setAllergyId(1L);
        dto.setAllergyActions(Arrays.asList(allergyAction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(allergyRepository, times(1)).deleteById(1L);
    }

    @Test
    void updatePatientRecord_addMedication_successfullyAdds() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        PatientRecordUpdateDTO.MedicationAction medicationAction = new PatientRecordUpdateDTO.MedicationAction();
        medicationAction.setAction(PatientRecordUpdateDTO.MedicationAction.ActionType.ADD);
        medicationAction.setMedicationId(2L);
        medicationAction.setDrugName("Ibuprofen");
        medicationAction.setDosage("400mg");
        medicationAction.setFrequency("Every 6 hours");
        medicationAction.setDuration("1 week");
        medicationAction.setIsPrescription(false);
        dto.setMedicationActions(Arrays.asList(medicationAction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(medicationRepository, times(1)).save(any(Medication.class));
    }

    @Test
    void updatePatientRecord_updateMedication_successfullyUpdates() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        PatientRecordUpdateDTO.MedicationAction medicationAction = new PatientRecordUpdateDTO.MedicationAction();
        medicationAction.setAction(PatientRecordUpdateDTO.MedicationAction.ActionType.UPDATE);
        medicationAction.setMedicationId(1L);
        medicationAction.setDrugName("Aspirin 325mg");
        medicationAction.setDosage("325mg");
        medicationAction.setFrequency("Once daily");
        medicationAction.setDuration("2 weeks");
        medicationAction.setIsPrescription(true);
        dto.setMedicationActions(Arrays.asList(medicationAction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(Arrays.asList(testMedication));
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(medicationRepository, times(1)).save(any(Medication.class));
    }

    @Test
    void updatePatientRecord_removeMedication_successfullyRemoves() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        PatientRecordUpdateDTO.MedicationAction medicationAction = new PatientRecordUpdateDTO.MedicationAction();
        medicationAction.setAction(PatientRecordUpdateDTO.MedicationAction.ActionType.REMOVE);
        medicationAction.setMedicationId(1L);
        dto.setMedicationActions(Arrays.asList(medicationAction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(medicationRepository, times(1)).deleteById(1L);
    }

    @Test
    void updatePatientRecord_addMedicalNote_successfullyAdds() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setMedicalNote("Diagnosed with diabetes");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(medicalHistoryRepository, times(1)).save(any(MedicalHistory.class));
    }

    @Test
    void updatePatientRecord_blankMedicalNote_doesNotAdd() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setMedicalNote("   ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(medicalHistoryRepository, never()).save(any(MedicalHistory.class));
    }

    @Test
    void updatePatientRecord_allFields_successfullyUpdatesAll() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane.smith@example.com");
        dto.setPhoneNumber("555-9999");
        dto.setAddress("456 Oak Ave");
        dto.setHeight("165cm");
        dto.setWeight("65kg");
        dto.setMedicalNote("Updated diagnosis");

        PatientRecordUpdateDTO.AllergyAction allergyAction = new PatientRecordUpdateDTO.AllergyAction();
        allergyAction.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.ADD);
        allergyAction.setSubstance("Latex");
        allergyAction.setReaction("Contact dermatitis");
        allergyAction.setSeverity("Moderate");

        PatientRecordUpdateDTO.MedicationAction medicationAction = new PatientRecordUpdateDTO.MedicationAction();
        medicationAction.setAction(PatientRecordUpdateDTO.MedicationAction.ActionType.ADD);
        medicationAction.setMedicationId(2L);
        medicationAction.setDrugName("Metformin");
        medicationAction.setDosage("500mg");
        medicationAction.setFrequency("Twice daily");
        medicationAction.setDuration("Long term");
        medicationAction.setIsPrescription(true);

        dto.setAllergyActions(Arrays.asList(allergyAction));
        dto.setMedicationActions(Arrays.asList(medicationAction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("Jane", "Smith", testUser.getDateOfBirth()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail("jane.smith@example.com")).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L))
            .thenReturn(new ArrayList<>())
            .thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L))
            .thenReturn(new ArrayList<>())
            .thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L))
            .thenReturn(new ArrayList<>())
            .thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        verify(userRepository, times(1)).save(any(User.class));
        verify(allergyRepository, times(1)).save(any(Allergy.class));
        verify(medicationRepository, times(1)).save(any(Medication.class));
        verify(medicalHistoryRepository, times(1)).save(any(MedicalHistory.class));
    }

    @Test
    void updatePatientRecord_sameEmailAsCurrentUser_successfullyUpdates() {
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setEmail("john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(1L, dto);

        assertEquals(200, response.getStatusCode().value());
    }
}
