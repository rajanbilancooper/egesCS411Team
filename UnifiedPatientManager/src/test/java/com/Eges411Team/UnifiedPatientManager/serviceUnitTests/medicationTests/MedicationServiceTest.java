package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.medicationTests;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.services.MedicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for MedicationService.
 * Covers all public methods and key branches: retrieval, saving, updating, deleting,
 * refresh, and conflict detection (allergy/interaction).
 */
@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private MedicationService medicationService;

    // Helper: builds a medication with common fields
    private Medication buildMedication(Long id, Long patientId, String drugName, Boolean isPrescription) {
        Medication med = new Medication();
        med.setId(id);
        med.setPatientId(patientId);
        med.setDoctorId(100L);
        med.setDrugName(drugName);
        med.setDose("500mg");
        med.setFrequency("BID");
        med.setDuration("7 days");
        med.setRoute("oral");
        med.setIsPerscription(isPrescription);
        med.setTimestamp(LocalDateTime.now());
        med.setStatus(true);
        return med;
    }

    // Helper: builds an allergy with a substance
    private Allergy buildAllergy(Long patientId, String substance) {
        Allergy allergy = new Allergy();
        allergy.setPatientId(patientId);
        allergy.setSubstance(substance);
        allergy.setSeverity("High");
        allergy.setReaction("Anaphylaxis");
        return allergy;
    }

    // ==================== getMedicationsByPatientId ====================
    @Test
    void getMedicationsByPatientId_withMedications_returnsAll() {
        Long patientId = 1L;
        List<Medication> meds = List.of(
            buildMedication(1L, patientId, "Aspirin", true),
            buildMedication(2L, patientId, "Ibuprofen", false)
        );

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(meds);

        List<Medication> result = medicationService.getMedicationsByPatientId(patientId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Aspirin", result.get(0).getDrugName());
        assertEquals("Ibuprofen", result.get(1).getDrugName());
        verify(medicationRepository).findAllByPatientId(patientId);
    }

    @Test
    void getMedicationsByPatientId_noMedications_returnsEmpty() {
        Long patientId = 2L;

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        List<Medication> result = medicationService.getMedicationsByPatientId(patientId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(medicationRepository).findAllByPatientId(patientId);
    }

    // ==================== refreshMedications ====================
    @Test
    void refreshMedications_returnsLatestList() {
        Long patientId = 3L;
        List<Medication> meds = List.of(buildMedication(5L, patientId, "Lisinopril", true));

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(meds);

        List<Medication> result = medicationService.refreshMedications(patientId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lisinopril", result.get(0).getDrugName());
        verify(medicationRepository).findAllByPatientId(patientId);
    }

    // ==================== saveMedications ====================
    @Test
    void saveMedications_deletesExistingAndSavesNew() {
        Long patientId = 4L;
        Long providerId = 10L;

        Medication oldMed = buildMedication(1L, patientId, "OldDrug", true);
        List<Medication> existingMeds = List.of(oldMed);

        Medication newMed1 = new Medication();
        newMed1.setDrugName("Aspirin");
        newMed1.setDose("100mg");
        newMed1.setFrequency("BID");
        newMed1.setDuration("5 days");

        Medication newMed2 = new Medication();
        newMed2.setDrugName("Ibuprofen");
        newMed2.setDose("200mg");
        newMed2.setFrequency("TID");
        newMed2.setDuration("3 days");

        List<Medication> newMeds = List.of(newMed1, newMed2);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(existingMeds);
        when(medicationRepository.saveAll(anyList())).thenReturn(newMeds);

        List<Medication> result = medicationService.saveMedications(patientId, providerId, newMeds);

        verify(medicationRepository).deleteAll(existingMeds);
        verify(medicationRepository).saveAll(anyList());
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void saveMedications_emptyNewList_deletesExistingAndSavesNothing() {
        Long patientId = 5L;
        Long providerId = 11L;

        Medication oldMed = buildMedication(1L, patientId, "OldDrug", true);
        List<Medication> existingMeds = List.of(oldMed);
        List<Medication> emptyNewMeds = new ArrayList<>();

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(existingMeds);
        when(medicationRepository.saveAll(anyList())).thenReturn(emptyNewMeds);

        List<Medication> result = medicationService.saveMedications(patientId, providerId, emptyNewMeds);

        verify(medicationRepository).deleteAll(existingMeds);
        assertTrue(result.isEmpty());
    }

    // ==================== updateMedication ====================
    @Test
    void updateMedication_notFound_throwsNotFound() {
        Long patientId = 6L;
        Long providerId = 12L;
        Long medicationId = 999L;

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.empty());

        Medication updated = buildMedication(null, null, "Aspirin", true);

        assertThrows(ResponseStatusException.class, () ->
            medicationService.updateMedication(patientId, providerId, medicationId, updated)
        );

        verify(medicationRepository).findById(medicationId);
    }

    @Test
    void updateMedication_wrongPatient_throwsBadRequest() {
        Long patientId = 7L;
        Long wrongPatientId = 99L;
        Long providerId = 13L;
        Long medicationId = 10L;

        Medication existing = buildMedication(medicationId, wrongPatientId, "OldDrug", true);

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));

        Medication updated = buildMedication(null, null, "Aspirin", true);

        assertThrows(ResponseStatusException.class, () ->
            medicationService.updateMedication(patientId, providerId, medicationId, updated)
        );

        verify(medicationRepository).findById(medicationId);
    }

    @Test
    void updateMedication_existingMedication_updatesFieldsAndSaves() {
        Long patientId = 8L;
        Long providerId = 14L;
        Long medicationId = 11L;

        Medication existing = buildMedication(medicationId, patientId, "Aspirin", true);

        Medication updated = new Medication();
        updated.setDrugName("Ibuprofen");
        updated.setDose("400mg");
        updated.setFrequency("QID");
        updated.setDuration("10 days");
        updated.setNotes("Updated notes");
        updated.setRoute("rectal");
        updated.setStatus(false);
        updated.setIsPerscription(false);
        updated.setTimestamp(LocalDateTime.now());

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenReturn(existing);

        Medication result = medicationService.updateMedication(patientId, providerId, medicationId, updated);

        assertNotNull(result);
        verify(medicationRepository).save(argThat(med ->
            "Ibuprofen".equals(med.getDrugName()) &&
            "400mg".equals(med.getDose()) &&
            "QID".equals(med.getFrequency()) &&
            "10 days".equals(med.getDuration()) &&
            "Updated notes".equals(med.getNotes()) &&
            "rectal".equals(med.getRoute()) &&
            !med.getStatus() &&
            !med.getIsPerscription() &&
            providerId.equals(med.getDoctorId())
        ));
    }

    // ==================== deleteMedication ====================
    @Test
    void deleteMedication_notFound_throwsNotFound() {
        Long patientId = 9L;
        Long medicationId = 888L;

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            medicationService.deleteMedication(patientId, medicationId)
        );

        verify(medicationRepository).findById(medicationId);
        verify(medicationRepository, never()).delete(any());
    }

    @Test
    void deleteMedication_wrongPatient_throwsBadRequest() {
        Long patientId = 10L;
        Long wrongPatientId = 88L;
        Long medicationId = 12L;

        Medication existing = buildMedication(medicationId, wrongPatientId, "Aspirin", true);

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
            medicationService.deleteMedication(patientId, medicationId)
        );

        verify(medicationRepository, never()).delete(any());
    }

    @Test
    void deleteMedication_patientIdNull_throwsBadRequest() {
        Long patientId = 11L;
        Long medicationId = 13L;

        Medication existing = buildMedication(medicationId, null, "Aspirin", true);

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
            medicationService.deleteMedication(patientId, medicationId)
        );

        verify(medicationRepository, never()).delete(any());
    }

    @Test
    void deleteMedication_validMedication_deletesSuccessfully() {
        Long patientId = 12L;
        Long medicationId = 14L;

        Medication existing = buildMedication(medicationId, patientId, "Aspirin", true);

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));

        medicationService.deleteMedication(patientId, medicationId);

        verify(medicationRepository).delete(existing);
    }

    // ==================== createSinglePrescription - Allergy Conflicts ====================
    @Test
    void createSinglePrescription_allergyConflictNoOverride_notSaved() {
        Long patientId = 17L;
        Long providerId = 19L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication amoxicillin = new Medication();
        amoxicillin.setDrugName("Amoxicillin");
        amoxicillin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(penicillinAllergy));

        var result = medicationService.createSinglePrescription(patientId, providerId, amoxicillin, false, null);

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertTrue(result.getConflictMessages().stream().anyMatch(msg -> msg.toLowerCase().contains("allergy")));
        assertNull(result.getPrescription());
        verify(medicationRepository, never()).save(any(Medication.class));
    }

    @Test
    void createSinglePrescription_allergyConflictWithOverride_saved() {
        Long patientId = 18L;
        Long providerId = 20L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication amoxicillin = new Medication();
        amoxicillin.setDrugName("Amoxicillin");
        amoxicillin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(penicillinAllergy));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, providerId, amoxicillin, true, "Patient insists");

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertNotNull(result.getPrescription());
        verify(medicationRepository).save(argThat(m ->
            m.getConflictFlag() != null && m.getConflictFlag() &&
            "Patient insists".equals(m.getOverrideJustification())
        ));
    }

    // ==================== createSinglePrescription - Interaction Conflicts ====================
    @Test
    void createSinglePrescription_interactionConflictNoOverride_notSaved() {
        Long patientId = 19L;
        Long providerId = 21L;

        Medication warfarin = buildMedication(20L, patientId, "Warfarin", true);

        Medication aspirin = new Medication();
        aspirin.setDrugName("Aspirin");
        aspirin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of(warfarin));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        var result = medicationService.createSinglePrescription(patientId, providerId, aspirin, false, null);

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertTrue(result.getConflictMessages().stream().anyMatch(msg -> msg.toLowerCase().contains("interaction")));
        assertNull(result.getPrescription());
        verify(medicationRepository, never()).save(any(Medication.class));
    }

    @Test
    void createSinglePrescription_interactionConflictWithOverride_saved() {
        Long patientId = 20L;
        Long providerId = 22L;

        Medication warfarin = buildMedication(21L, patientId, "Warfarin", true);

        Medication aspirin = new Medication();
        aspirin.setDrugName("Aspirin");
        aspirin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of(warfarin));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, providerId, aspirin, true, "Doctor approved");

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertNotNull(result.getPrescription());
        verify(medicationRepository).save(argThat(m ->
            m.getConflictFlag() != null && m.getConflictFlag() &&
            "Doctor approved".equals(m.getOverrideJustification())
        ));
    }

    // ==================== createSinglePrescription - Duplicate Medication ====================
    @Test
    void createSinglePrescription_duplicateMedicationNoOverride_notSaved() {
        Long patientId = 21L;
        Long providerId = 23L;

        Medication existingAspirin = buildMedication(22L, patientId, "Aspirin", true);

        Medication newAspirin = new Medication();
        newAspirin.setDrugName("Aspirin");
        newAspirin.setDose("100mg");
        newAspirin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of(existingAspirin));

        var result = medicationService.createSinglePrescription(patientId, providerId, newAspirin, false, null);

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertTrue(result.getConflictMessages().stream().anyMatch(msg -> msg.toLowerCase().contains("duplicate")));
        assertNull(result.getPrescription());
        verify(medicationRepository, never()).save(any(Medication.class));
    }

    // ==================== createSinglePrescription - No Conflicts ====================
    @Test
    void createSinglePrescription_noConflicts_saved() {
        Long patientId = 23L;
        Long providerId = 25L;

        Medication ibuprofen = new Medication();
        ibuprofen.setDrugName("Ibuprofen");
        ibuprofen.setDose("200mg");
        ibuprofen.setIsPerscription(false);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, providerId, ibuprofen, false, null);

        assertNotNull(result);
        assertFalse(result.isConflicts());
        assertTrue(result.getConflictMessages().isEmpty());
        assertNotNull(result.getPrescription());
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void createSinglePrescription_setsPatientAndDoctorIds() {
        Long patientId = 24L;
        Long providerId = 26L;

        Medication med = new Medication();
        med.setDrugName("Lisinopril");
        med.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        medicationService.createSinglePrescription(patientId, providerId, med, false, null);

        verify(medicationRepository).save(argThat(m ->
            patientId.equals(m.getPatientId()) &&
            providerId.equals(m.getDoctorId())
        ));
    }

    // ==================== createSinglePrescription - Non-Prescription & Blank Names ====================
    @Test
    void createSinglePrescription_nonPrescription_noConflictCheck() {
        Long patientId = 14L;
        Long providerId = 16L;

        Medication nonPrescription = new Medication();
        nonPrescription.setDrugName("Aspirin");
        nonPrescription.setIsPerscription(false);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenReturn(nonPrescription);

        var result = medicationService.createSinglePrescription(patientId, providerId, nonPrescription, false, null);

        assertNotNull(result);
        assertFalse(result.isConflicts());
        assertTrue(result.getConflictMessages().isEmpty());
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void createSinglePrescription_blankDrugName_noConflictCheck() {
        Long patientId = 15L;
        Long providerId = 17L;

        Medication blankDrug = new Medication();
        blankDrug.setDrugName("   ");
        blankDrug.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenReturn(blankDrug);

        var result = medicationService.createSinglePrescription(patientId, providerId, blankDrug, false, null);

        assertNotNull(result);
        assertFalse(result.isConflicts());
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void createSinglePrescription_nullDrugName_noConflictCheck() {
        Long patientId = 16L;
        Long providerId = 18L;

        Medication nullDrug = new Medication();
        nullDrug.setDrugName(null);
        nullDrug.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenReturn(nullDrug);

        var result = medicationService.createSinglePrescription(patientId, providerId, nullDrug, false, null);

        assertNotNull(result);
        assertFalse(result.isConflicts());
        verify(medicationRepository).save(any(Medication.class));
    }

    // ==================== updateMedication - Allergy Conflict ====================
    @Test
    void updateMedication_withAllergyConflict_throwsBadRequest() {
        Long patientId = 25L;
        Long providerId = 27L;
        Long medicationId = 30L;

        Medication existing = buildMedication(medicationId, patientId, "Aspirin", true);

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication updated = new Medication();
        updated.setDrugName("Amoxicillin");
        updated.setIsPerscription(true);

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(penicillinAllergy));

        assertThrows(ResponseStatusException.class, () ->
            medicationService.updateMedication(patientId, providerId, medicationId, updated)
        );

        verify(medicationRepository).findById(medicationId);
        verify(allergyRepository).findAllByPatientId(patientId);
        verify(medicationRepository, never()).save(any());
    }

    @Test
    void updateMedication_successfulUpdateWithoutConflicts() {
        Long patientId = 26L;
        Long providerId = 28L;
        Long medicationId = 31L;

        Medication existing = buildMedication(medicationId, patientId, "Aspirin", true);

        Medication updated = new Medication();
        updated.setDrugName("Ibuprofen");
        updated.setIsPerscription(true);
        updated.setDose("200mg");

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenReturn(existing);

        Medication result = medicationService.updateMedication(patientId, providerId, medicationId, updated);

        assertNotNull(result);
        verify(medicationRepository).save(any());
    }

    @Test
    void updateMedication_nullDrugNameInUpdate_succeeds() {
        Long patientId = 27L;
        Long providerId = 29L;
        Long medicationId = 32L;

        Medication existing = buildMedication(medicationId, patientId, "Aspirin", true);

        Medication updated = new Medication();
        updated.setDrugName(null);
        updated.setIsPerscription(true);

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenReturn(existing);

        Medication result = medicationService.updateMedication(patientId, providerId, medicationId, updated);

        assertNotNull(result);
        verify(medicationRepository).save(any());
    }

    // ==================== Multi-Medication Interaction Tests ====================
    @Test
    void checkForConflictsOrThrow_multipleAllergyConflicts() {
        Long patientId = 28L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");
        Allergy nsaidAllergy = buildAllergy(patientId, "NSAIDs");

        Medication amoxicillin = buildMedication(40L, patientId, "Amoxicillin", true);

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(penicillinAllergy, nsaidAllergy));

        // When updating with Amoxicillin (conflicts with Penicillin allergy)
        when(medicationRepository.findById(40L)).thenReturn(Optional.of(amoxicillin));

        Medication updated = new Medication();
        updated.setDrugName("Amoxicillin");
        updated.setIsPerscription(true);

        assertThrows(ResponseStatusException.class, () ->
            medicationService.updateMedication(patientId, 100L, 40L, updated)
        );
    }

    @Test
    void checkForConflictsOrThrow_nullSubstanceInAllergy() {
        Long patientId = 29L;

        Allergy nullSubstanceAllergy = new Allergy();
        nullSubstanceAllergy.setPatientId(patientId);
        nullSubstanceAllergy.setSubstance(null);

        Medication aspirin = buildMedication(42L, patientId, "Aspirin", true);

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(nullSubstanceAllergy));

        when(medicationRepository.findById(42L)).thenReturn(Optional.of(aspirin));

        Medication updated = new Medication();
        updated.setDrugName("Aspirin");
        updated.setIsPerscription(true);

        // Should not throw because null substance is skipped
        when(medicationRepository.save(any(Medication.class))).thenReturn(aspirin);

        Medication result = medicationService.updateMedication(patientId, 100L, 42L, updated);
        assertNotNull(result);
    }

    @Test
    void checkForConflictsOrThrow_caseInsensitiveAllergyMatch() {
        Long patientId = 30L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication amoxicillin = buildMedication(43L, patientId, "Aspirin", true);

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(penicillinAllergy));

        when(medicationRepository.findById(43L)).thenReturn(Optional.of(amoxicillin));

        Medication updated = new Medication();
        updated.setDrugName("AMOXICILLIN");  // Uppercase
        updated.setIsPerscription(true);

        assertThrows(ResponseStatusException.class, () ->
            medicationService.updateMedication(patientId, 100L, 43L, updated)
        );
    }

    @Test
    void checkForConflictsOrThrow_nonPrescriptionNotChecked() {
        Long patientId = 31L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication aspirin = buildMedication(44L, patientId, "Aspirin", true);

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(penicillinAllergy));

        when(medicationRepository.findById(44L)).thenReturn(Optional.of(aspirin));

        Medication updated = new Medication();
        updated.setDrugName("Amoxicillin");
        updated.setIsPerscription(false);  // Not a prescription - shouldn't be checked

        when(medicationRepository.save(any(Medication.class))).thenReturn(aspirin);

        Medication result = medicationService.updateMedication(patientId, 100L, 44L, updated);
        assertNotNull(result);
        verify(medicationRepository).save(any());
    }

    @Test
    void checkForConflictsOrThrow_interactionWithinSameList() {
        // Tests that interactions within checkForConflictsOrThrow are detected with multiple meds
        Long patientId = 32L;

        Medication warfarin = buildMedication(45L, patientId, "Warfarin", true);

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(new ArrayList<>());

        when(medicationRepository.findById(45L)).thenReturn(Optional.of(warfarin));

        // Try to update to Aspirin - but checkForConflictsOrThrow doesn't check interactions
        // with existing meds, only within the passed list
        Medication updated = new Medication();
        updated.setDrugName("Aspirin");
        updated.setIsPerscription(true);

        when(medicationRepository.save(any(Medication.class))).thenReturn(warfarin);

        // This should succeed because checkForConflictsOrThrow only checks
        // interactions within the single med list (which can't have interaction with itself)
        Medication result = medicationService.updateMedication(patientId, 100L, 45L, updated);
        assertNotNull(result);
    }

    @Test
    void collectConflicts_detects_allergyConflict() {
        Long patientId = 33L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(penicillinAllergy));
        when(medicationRepository.findAllByPatientId(patientId))
            .thenReturn(new ArrayList<>());

        Medication amoxicillin = new Medication();
        amoxicillin.setDrugName("Amoxicillin");
        amoxicillin.setIsPerscription(true);

        var result = medicationService.createSinglePrescription(patientId, 100L, amoxicillin, false, null);

        assertTrue(result.isConflicts());
        assertTrue(result.getConflictMessages().stream().anyMatch(m -> m.contains("Allergy")));
    }

    @Test
    void collectConflicts_detects_interactionWithExistingMed() {
        Long patientId = 34L;

        Medication warfarin = buildMedication(50L, patientId, "Warfarin", true);

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(warfarin));

        Medication aspirin = new Medication();
        aspirin.setDrugName("Aspirin");
        aspirin.setIsPerscription(true);

        var result = medicationService.createSinglePrescription(patientId, 100L, aspirin, false, null);

        assertTrue(result.isConflicts());
        assertTrue(result.getConflictMessages().stream().anyMatch(m -> m.contains("Interaction")));
    }

    @Test
    void collectConflicts_multipleConflicts() {
        Long patientId = 35L;

        Allergy nsaidAllergy = buildAllergy(patientId, "NSAIDs");
        Medication warfarin = buildMedication(51L, patientId, "Warfarin", true);

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(nsaidAllergy));
        when(medicationRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(warfarin));

        // Aspirin conflicts with both NSAIDs allergy and Warfarin interaction
        Medication aspirin = new Medication();
        aspirin.setDrugName("Aspirin");
        aspirin.setIsPerscription(true);

        var result = medicationService.createSinglePrescription(patientId, 100L, aspirin, false, null);

        assertTrue(result.isConflicts());
        assertTrue(result.getConflictMessages().size() >= 2);
    }

    @Test
    void isBadInteraction_reversedOrder() {
        Long patientId = 36L;

        Medication aspirin = buildMedication(52L, patientId, "Aspirin", true);

        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(aspirin));

        // Warfarin should detect interaction with Aspirin (reversed order in list)
        Medication warfarin = new Medication();
        warfarin.setDrugName("Warfarin");
        warfarin.setIsPerscription(true);

        var result = medicationService.createSinglePrescription(patientId, 100L, warfarin, false, null);

        assertTrue(result.isConflicts());
    }

    @Test
    void updateMedication_nullNullableMedicationField_succeeds() {
        Long patientId = 34L;
        Long providerId = 35L;
        Long medicationId = 47L;

        Medication existing = buildMedication(medicationId, patientId, "Aspirin", true);

        Medication updated = new Medication();
        updated.setDrugName("Ibuprofen");
        updated.setNotes(null);
        updated.setRoute(null);
        updated.setIsPerscription(true);

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenReturn(existing);

        Medication result = medicationService.updateMedication(patientId, providerId, medicationId, updated);

        assertNotNull(result);
        verify(medicationRepository).save(any());
    }

    @Test
    void createSinglePrescription_duplicateWithOverrideAndAllergyConflict() {
        Long patientId = 35L;
        Long providerId = 36L;

        Medication existingAspirin = buildMedication(48L, patientId, "Aspirin", true);

        Allergy nsaidAllergy = buildAllergy(patientId, "NSAIDs");

        Medication newAspirin = new Medication();
        newAspirin.setDrugName("Aspirin");
        newAspirin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(existingAspirin));
        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(nsaidAllergy));
        when(medicationRepository.save(any(Medication.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, providerId, newAspirin, true, "Patient approved");

        assertNotNull(result);
        assertTrue(result.isConflicts());
        verify(medicationRepository).save(any());
    }

    @Test
    void checkForConflictsOrThrow_nullDrugNameSkipped() {
        Long patientId = 36L;

        Medication medWithNullDrug = buildMedication(49L, patientId, null, true);

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findById(49L)).thenReturn(Optional.of(medWithNullDrug));

        Medication updated = new Medication();
        updated.setDrugName("Aspirin");
        updated.setIsPerscription(true);

        when(medicationRepository.save(any(Medication.class))).thenReturn(medWithNullDrug);

        // Should succeed because null drug names are skipped in conflict checking
        Medication result = medicationService.updateMedication(patientId, 100L, 49L, updated);
        assertNotNull(result);
    }

    // ==================== Additional Branch Coverage Tests ====================
    @Test
    void createSinglePrescription_noConflictsWithoutDuplicate_returnsWithoutFlag() {
        Long patientId = 37L;
        Long providerId = 38L;

        Medication newMed = new Medication();
        newMed.setDrugName("Metformin");
        newMed.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, providerId, newMed, false, null);

        assertNotNull(result);
        assertFalse(result.isConflicts());
        assertTrue(result.getConflictMessages().isEmpty());
        assertNotNull(result.getPrescription());
    }

    @Test
    void createSinglePrescription_duplicateWithoutOverride_returnsFalseWithConflict() {
        Long patientId = 38L;
        Long providerId = 39L;

        Medication existingMed = buildMedication(50L, patientId, "Aspirin", true);

        Medication newMed = new Medication();
        newMed.setDrugName("Aspirin");
        newMed.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of(existingMed));

        var result = medicationService.createSinglePrescription(patientId, providerId, newMed, false, null);

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertNull(result.getPrescription());
        verify(medicationRepository, never()).save(any());
    }

    @Test
    void createSinglePrescription_nullDrugName_noConflicts() {
        Long patientId = 39L;
        Long providerId = 40L;

        Medication newMed = new Medication();
        newMed.setDrugName(null);
        newMed.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, providerId, newMed, false, null);

        assertNotNull(result);
        assertFalse(result.isConflicts());
        verify(medicationRepository).save(any());
    }

    @Test
    void createSinglePrescription_allergyConflictWithoutOverride_returnsFalse() {
        Long patientId = 40L;
        Long providerId = 41L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication newMed = new Medication();
        newMed.setDrugName("Ampicillin");
        newMed.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(penicillinAllergy));

        var result = medicationService.createSinglePrescription(patientId, providerId, newMed, false, null);

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertNull(result.getPrescription());
        verify(medicationRepository, never()).save(any());
    }

    @Test
    void createSinglePrescription_allergyConflictWithOverride_savesWithFlag() {
        Long patientId = 41L;
        Long providerId = 42L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication newMed = new Medication();
        newMed.setDrugName("Ampicillin");
        newMed.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(penicillinAllergy));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, providerId, newMed, true, "Patient insists");

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertNotNull(result.getPrescription());
        verify(medicationRepository).save(argThat(m ->
            m.getConflictFlag() != null && m.getConflictFlag() &&
            "Patient insists".equals(m.getOverrideJustification())
        ));
    }

    @Test
    void createSinglePrescription_allergyConflictNoOverrideJustification_returnsFalse() {
        Long patientId = 42L;
        Long providerId = 43L;

        Allergy nsaidAllergy = buildAllergy(patientId, "NSAIDs");

        Medication newMed = new Medication();
        newMed.setDrugName("Naproxen");
        newMed.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(nsaidAllergy));

        var result = medicationService.createSinglePrescription(patientId, providerId, newMed, false, null);

        assertTrue(result.isConflicts());
        assertFalse(result.getConflictMessages().isEmpty());
    }

    @Test
    void saveMedications_withMultipleMeds_setsPatientAndDoctorIds() {
        Long patientId = 43L;
        Long providerId = 44L;

        Medication med1 = new Medication();
        med1.setDrugName("Med1");
        Medication med2 = new Medication();
        med2.setDrugName("Med2");

        List<Medication> existingMeds = new ArrayList<>();
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(existingMeds);
        when(medicationRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Medication> savedMeds = inv.getArgument(0);
            return savedMeds;
        });

        List<Medication> result = medicationService.saveMedications(patientId, providerId, List.of(med1, med2));

        assertEquals(2, result.size());
        for (Medication med : result) {
            assertEquals(patientId, med.getPatientId());
            assertEquals(providerId, med.getDoctorId());
        }
    }

    @Test
    void collectConflicts_allergyExistsNoDrugMatch_returnsEmpty() {
        Long patientId = 44L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication newMed = new Medication();
        newMed.setDrugName("Lisinopril");  // Not in penicillin conflict list
        newMed.setIsPerscription(true);

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(penicillinAllergy));
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        var result = medicationService.createSinglePrescription(patientId, 100L, newMed, false, null);

        assertFalse(result.isConflicts());
        assertTrue(result.getConflictMessages().isEmpty());
    }

    @Test
    void collectConflicts_interactionExistsWithExisting_returnsConflict() {
        Long patientId = 45L;

        Medication warfarin = buildMedication(60L, patientId, "Warfarin", true);

        Medication aspirin = new Medication();
        aspirin.setDrugName("Aspirin");
        aspirin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of(warfarin));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        var result = medicationService.createSinglePrescription(patientId, 100L, aspirin, false, null);

        assertTrue(result.isConflicts());
        assertTrue(result.getConflictMessages().stream().anyMatch(m -> m.contains("Interaction")));
    }

    @Test
    void collectConflicts_blankDrugName_returnsEmpty() {
        Long patientId = 46L;

        Medication newMed = new Medication();
        newMed.setDrugName("   ");  // Blank string
        newMed.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, 100L, newMed, false, null);

        assertFalse(result.isConflicts());
        verify(medicationRepository).save(any());
    }

    @Test
    void deleteMedication_medicationNotFound_throwsNotFound() {
        Long patientId = 47L;
        Long medicationId = 888L;

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            medicationService.deleteMedication(patientId, medicationId)
        );

        verify(medicationRepository, never()).delete(any());
    }

    @Test
    void saveMedications_emptyExistingMeds_savesAll() {
        Long patientId = 48L;
        Long providerId = 49L;

        Medication med1 = new Medication();
        med1.setDrugName("Med1");

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Medication> result = medicationService.saveMedications(patientId, providerId, List.of(med1));

        assertEquals(1, result.size());
        verify(medicationRepository).deleteAll(any());
    }

    @Test
    void updateMedication_changesAllFields_successfullySaves() {
        Long patientId = 49L;
        Long providerId = 50L;
        Long medicationId = 62L;

        Medication existing = buildMedication(medicationId, patientId, "OldDrug", true);

        Medication updated = new Medication();
        updated.setDrugName("NewDrug");
        updated.setDose("1000mg");
        updated.setFrequency("TID");
        updated.setDuration("14 days");
        updated.setNotes("Updated notes");
        updated.setRoute("IV");
        updated.setStatus(false);
        updated.setIsPerscription(false);
        updated.setTimestamp(LocalDateTime.now());

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        Medication result = medicationService.updateMedication(patientId, providerId, medicationId, updated);

        assertNotNull(result);
        assertEquals("NewDrug", result.getDrugName());
        assertEquals(false, result.getIsPerscription());
        assertEquals("IV", result.getRoute());
        assertEquals(providerId, result.getDoctorId());
    }

    @Test
    void refreshMedications_returnsSameAsMedicationsByPatientId() {
        Long patientId = 50L;
        List<Medication> meds = List.of(buildMedication(1L, patientId, "Drug1", true));

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(meds);

        List<Medication> result = medicationService.refreshMedications(patientId);

        assertEquals(meds.size(), result.size());
        verify(medicationRepository).findAllByPatientId(patientId);
    }

    // ==================== Additional Edge Cases for Higher Branch Coverage ====================
    @Test
    void collectConflicts_multipleAllergies_noConflictWithAny() {
        Long patientId = 51L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");
        Allergy nsaidAllergy = buildAllergy(patientId, "NSAIDs");

        Medication lisinopril = new Medication();
        lisinopril.setDrugName("Lisinopril");
        lisinopril.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(penicillinAllergy, nsaidAllergy));

        var result = medicationService.createSinglePrescription(patientId, 100L, lisinopril, false, null);

        assertFalse(result.isConflicts());
        assertTrue(result.getConflictMessages().isEmpty());
    }

    @Test
    void collectConflicts_existingMedsWithNullDrugName_skipped() {
        Long patientId = 52L;

        Medication medWithNullDrug = new Medication();
        medWithNullDrug.setDrugName(null);
        medWithNullDrug.setIsPerscription(true);

        Medication aspirin = new Medication();
        aspirin.setDrugName("Aspirin");
        aspirin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(medWithNullDrug));
        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(new ArrayList<>());

        var result = medicationService.createSinglePrescription(patientId, 100L, aspirin, false, null);

        assertFalse(result.isConflicts());
    }

    @Test
    void collectConflicts_allergyWithNullSubstance_skipped() {
        Long patientId = 53L;

        Allergy nullSubstanceAllergy = new Allergy();
        nullSubstanceAllergy.setPatientId(patientId);
        nullSubstanceAllergy.setSubstance(null);

        Medication aspirin = new Medication();
        aspirin.setDrugName("Aspirin");
        aspirin.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(nullSubstanceAllergy));

        var result = medicationService.createSinglePrescription(patientId, 100L, aspirin, false, null);

        assertFalse(result.isConflicts());
    }

    @Test
    void saveMedications_withEmptyList_deletesAndSaves() {
        Long patientId = 54L;
        Long providerId = 55L;

        Medication oldMed = buildMedication(1L, patientId, "OldDrug", true);

        when(medicationRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(oldMed));
        when(medicationRepository.saveAll(anyList()))
            .thenReturn(new ArrayList<>());

        List<Medication> result = medicationService.saveMedications(patientId, providerId, new ArrayList<>());

        assertTrue(result.isEmpty());
        verify(medicationRepository).deleteAll(any());
    }

    @Test
    void updateMedication_withAllergyConflict_throwsException() {
        Long patientId = 55L;
        Long providerId = 56L;
        Long medicationId = 70L;

        Medication existing = buildMedication(medicationId, patientId, "SafeDrug", true);

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication updated = new Medication();
        updated.setDrugName("Amoxicillin");
        updated.setIsPerscription(true);

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(penicillinAllergy));

        assertThrows(ResponseStatusException.class, () ->
            medicationService.updateMedication(patientId, providerId, medicationId, updated)
        );

        verify(medicationRepository, never()).save(any());
    }

    @Test
    void createSinglePrescription_nonPrescriptionMed_skipsConflictCheck() {
        Long patientId = 56L;
        Long providerId = 57L;

        Medication amoxicillin = new Medication();
        amoxicillin.setDrugName("Amoxicillin");
        amoxicillin.setIsPerscription(false);  // NOT a prescription

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = medicationService.createSinglePrescription(patientId, providerId, amoxicillin, false, null);

        // Should save successfully even with allergy because it's not a prescription
        assertFalse(result.isConflicts());
        verify(medicationRepository).save(any());
    }

    @Test
    void checkForConflictsOrThrow_nonPrescriptionMeds_skipped() {
        Long patientId = 57L;
        Long medicationId = 71L;

        Allergy penicillinAllergy = buildAllergy(patientId, "Penicillin");

        Medication existing = buildMedication(medicationId, patientId, "Amoxicillin", true);

        Medication updated = new Medication();
        updated.setDrugName("Aspirin");
        updated.setIsPerscription(false);  // NOT a prescription

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(patientId))
            .thenReturn(List.of(penicillinAllergy));
        when(medicationRepository.save(any(Medication.class))).thenReturn(existing);

        Medication result = medicationService.updateMedication(patientId, 100L, medicationId, updated);

        assertNotNull(result);
        verify(medicationRepository).save(any());
    }

    @Test
    void saveMedications_preservesAllOtherFields() {
        Long patientId = 58L;
        Long providerId = 59L;

        Medication med = new Medication();
        med.setDrugName("TestDrug");
        med.setDose("100mg");
        med.setFrequency("BID");
        med.setDuration("30 days");
        med.setRoute("oral");
        med.setStatus(true);
        med.setNotes("Test notes");
        med.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Medication> meds = inv.getArgument(0);
            // Verify all fields are preserved
            for (Medication m : meds) {
                assertEquals("TestDrug", m.getDrugName());
                assertEquals("100mg", m.getDose());
                assertEquals("BID", m.getFrequency());
                assertEquals("30 days", m.getDuration());
                assertEquals("oral", m.getRoute());
                assertEquals(true, m.getStatus());
                assertEquals("Test notes", m.getNotes());
                assertEquals(true, m.getIsPerscription());
                assertEquals(patientId, m.getPatientId());
                assertEquals(providerId, m.getDoctorId());
            }
            return meds;
        });

        List<Medication> result = medicationService.saveMedications(patientId, providerId, List.of(med));

        assertEquals(1, result.size());
    }
}

