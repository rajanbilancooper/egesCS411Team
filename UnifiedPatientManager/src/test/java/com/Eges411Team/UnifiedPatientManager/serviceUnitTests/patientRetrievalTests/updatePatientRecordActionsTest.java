package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientRetrievalTests;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Additional tests for PatientRecordService.updatePatientRecord focusing on
 * allergy actions, medication actions, and medical notes branches.
 */
@ExtendWith(MockitoExtension.class)
class updatePatientRecordActionsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private PatientRecordService patientRecordService;

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@doe.com");
        user.setPhoneNumber("555-1234");
        user.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));
        return user;
    }

    @Test
    void updatePatientRecord_nullAllergyActions_skipsAllergyChanges() {
        Long patientId = 10L;
        User user = buildUser(patientId);
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setAllergyActions(null);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(allergyRepository, never()).save(any(Allergy.class));
        verify(allergyRepository, never()).deleteById(any());
    }

    @Test
    void updatePatientRecord_addAllergy_savesWithPatientId() {
        Long patientId = 11L;
        User user = buildUser(patientId);

        PatientRecordUpdateDTO.AllergyAction add = new PatientRecordUpdateDTO.AllergyAction();
        add.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.ADD);
        add.setSubstance("Peanuts");
        add.setSeverity("High");
        add.setReaction("Anaphylaxis");

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setAllergyActions(List.of(add));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(allergyRepository).save(argThat(a ->
            patientId.equals(a.getPatientId()) &&
            "Peanuts".equals(a.getSubstance()) &&
            "High".equals(a.getSeverity()) &&
            "Anaphylaxis".equals(a.getReaction())
        ));
    }

    @Test
    void updatePatientRecord_updateExistingAllergy_updatesAndSaves() {
        Long patientId = 12L;
        User user = buildUser(patientId);

        Allergy existing = new Allergy();
        existing.setId(5L);
        existing.setPatientId(patientId);
        existing.setSubstance("Dust");
        existing.setSeverity("Low");
        existing.setReaction("Sneezing");

        PatientRecordUpdateDTO.AllergyAction update = new PatientRecordUpdateDTO.AllergyAction();
        update.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.UPDATE);
        update.setAllergyId(5L);
        update.setSubstance("Dust mites");
        update.setSeverity("Medium");
        update.setReaction("Congestion");

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setAllergyActions(List.of(update));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(existing));
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(allergyRepository).save(argThat(a ->
            a.getId().equals(5L) &&
            "Dust mites".equals(a.getSubstance()) &&
            "Medium".equals(a.getSeverity()) &&
            "Congestion".equals(a.getReaction())
        ));
    }

    @Test
    void updatePatientRecord_updateMissingAllergy_createsNewWithGivenId() {
        Long patientId = 13L;
        User user = buildUser(patientId);

        PatientRecordUpdateDTO.AllergyAction update = new PatientRecordUpdateDTO.AllergyAction();
        update.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.UPDATE);
        update.setAllergyId(99L);
        update.setSubstance("Pollen");
        update.setSeverity("Low");
        update.setReaction("Itchy eyes");

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setAllergyActions(List.of(update));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(allergyRepository).save(argThat(a ->
            a.getId().equals(99L) &&
            "Pollen".equals(a.getSubstance())
        ));
    }

    @Test
    void updatePatientRecord_removeAllergy_deletesById() {
        Long patientId = 14L;
        User user = buildUser(patientId);

        PatientRecordUpdateDTO.AllergyAction remove = new PatientRecordUpdateDTO.AllergyAction();
        remove.setAction(PatientRecordUpdateDTO.AllergyAction.ActionType.REMOVE);
        remove.setAllergyId(7L);

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setAllergyActions(List.of(remove));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(allergyRepository).deleteById(7L);
    }

    @Test
    void updatePatientRecord_nullMedicationActions_skipsMedicationChanges() {
        Long patientId = 20L;
        User user = buildUser(patientId);
        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setMedicationActions(null);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(medicationRepository, never()).save(any(Medication.class));
        verify(medicationRepository, never()).deleteById(any());
    }

    @Test
    void updatePatientRecord_addMedication_savesMedication() {
        Long patientId = 21L;
        User user = buildUser(patientId);

        PatientRecordUpdateDTO.MedicationAction add = new PatientRecordUpdateDTO.MedicationAction();
        add.setAction(PatientRecordUpdateDTO.MedicationAction.ActionType.ADD);
        add.setMedicationId(50L);
        add.setDrugName("Ibuprofen");
        add.setDosage("200mg");
        add.setDuration("5 days");
        add.setFrequency("BID");
        add.setIsPrescription(true);

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setMedicationActions(List.of(add));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(medicationRepository).save(argThat(m ->
            "Ibuprofen".equals(m.getDrugName()) &&
            "200mg".equals(m.getDose()) &&
            "BID".equals(m.getFrequency()) &&
            m.getIsPerscription()
        ));
    }

    @Test
    void updatePatientRecord_updateExistingMedication_updatesFields() {
        Long patientId = 22L;
        User user = buildUser(patientId);

        Medication existing = new Medication();
        existing.setId(60L);
        existing.setPatientId(patientId);
        existing.setDrugName("Tylenol");
        existing.setDose("100mg");
        existing.setFrequency("TID");
        existing.setDuration("3 days");
        existing.setIsPerscription(false);

        PatientRecordUpdateDTO.MedicationAction update = new PatientRecordUpdateDTO.MedicationAction();
        update.setAction(PatientRecordUpdateDTO.MedicationAction.ActionType.UPDATE);
        update.setMedicationId(60L);
        update.setDrugName("Tylenol");
        update.setDosage("500mg");
        update.setDuration("7 days");
        update.setFrequency("QID");
        update.setIsPrescription(true);

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setMedicationActions(List.of(update));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of(existing));
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(medicationRepository).save(argThat(m ->
            m.getId().equals(60L) &&
            "500mg".equals(m.getDose()) &&
            "QID".equals(m.getFrequency()) &&
            m.getIsPerscription()
        ));
    }

    @Test
    void updatePatientRecord_updateMissingMedication_createsNew() {
        Long patientId = 23L;
        User user = buildUser(patientId);

        PatientRecordUpdateDTO.MedicationAction update = new PatientRecordUpdateDTO.MedicationAction();
        update.setAction(PatientRecordUpdateDTO.MedicationAction.ActionType.UPDATE);
        update.setMedicationId(77L);
        update.setDrugName("Amoxicillin");
        update.setDosage("250mg");
        update.setDuration("10 days");
        update.setFrequency("BID");
        update.setIsPrescription(true);

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setMedicationActions(List.of(update));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(medicationRepository).save(argThat(m ->
            m.getId().equals(77L) &&
            "Amoxicillin".equals(m.getDrugName())
        ));
    }

    @Test
    void updatePatientRecord_removeMedication_deletesById() {
        Long patientId = 24L;
        User user = buildUser(patientId);

        PatientRecordUpdateDTO.MedicationAction remove = new PatientRecordUpdateDTO.MedicationAction();
        remove.setAction(PatientRecordUpdateDTO.MedicationAction.ActionType.REMOVE);
        remove.setMedicationId(88L);

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setMedicationActions(List.of(remove));

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(medicationRepository).deleteById(88L);
    }

    @Test
    void updatePatientRecord_medicalNoteNullOrBlank_skipsSave() {
        Long patientId = 30L;
        User user = buildUser(patientId);

        PatientRecordUpdateDTO dtoNull = new PatientRecordUpdateDTO();
        dtoNull.setMedicalNote(null);

        PatientRecordUpdateDTO dtoBlank = new PatientRecordUpdateDTO();
        dtoBlank.setMedicalNote("   ");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dtoNull);
        patientRecordService.updatePatientRecord(patientId, dtoBlank);

        verify(medicalHistoryRepository, never()).save(any(MedicalHistory.class));
    }

    @Test
    void updatePatientRecord_medicalNoteProvided_createsMedicalHistory() {
        Long patientId = 31L;
        User user = buildUser(patientId);

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setMedicalNote("Patient reports mild headache");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        patientRecordService.updatePatientRecord(patientId, dto);

        verify(medicalHistoryRepository).save(argThat(mh ->
            patientId.equals(mh.getPatientId()) &&
            "Patient reports mild headache".equals(mh.getDiagnosis())
        ));
    }

    @Test
    void updatePatientRecord_returnsUpdatedPatientRecordDTO() {
        Long patientId = 32L;
        User user = buildUser(patientId);
        user.setAddress("Old Address");

        PatientRecordUpdateDTO dto = new PatientRecordUpdateDTO();
        dto.setAddress("New Address");
        dto.setAllergyActions(new ArrayList<>()); // ensure path with empty list
        dto.setMedicationActions(new ArrayList<>());

        when(userRepository.findById(patientId)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(patientId, dto);

        assertNotNull(response.getBody());
        assertEquals("New Address", response.getBody().getAddress());
        assertEquals("John", response.getBody().getFirstName());
    }
}
