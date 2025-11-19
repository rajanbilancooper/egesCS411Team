package com.Eges411Team.UnifiedPatientManager;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.services.MedicationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

/**
 * Test Case #1: Valid Prescription Addition with No Conflicts
 * Goal: Ensure a prescription (Amoxicillin) is accepted when the patient has no conflicting allergies.
 * Note: Current implementation performs conflict checking in updateMedication, not saveMedications.
 */
@SpringBootTest
@Transactional
public class MedicationServiceIntegrationTest {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private AllergyRepository allergyRepository;

    @Test
    void validPrescriptionNoAllergyConflicts() {
        Long patientId = 100L;
        Long doctorId = 200L;

        // Pre-condition setup: existing medication record (will be updated to prescription Amoxicillin)
        Medication existing = new Medication();
        existing.setPatientId(patientId);
        existing.setDoctorId(doctorId);
        existing.setDrugName("Placeholder");
        existing.setDose("0 mg");
        existing.setFrequency("0x/day");
        existing.setDuration("0 days");
        existing.setNotes("initial");
        existing.setTimestamp(LocalDateTime.now());
        existing.setStatus(true);
        existing.setIsPerscription(true);
        medicationRepository.save(existing);

        // Patient has an allergy that does NOT conflict with Amoxicillin (e.g., NSAIDs)
        Allergy nonConflictingAllergy = new Allergy();
        nonConflictingAllergy.setId(10L);
        nonConflictingAllergy.setPatientId(patientId);
        nonConflictingAllergy.setReaction("Swelling");
        nonConflictingAllergy.setSeverity("MILD");
        nonConflictingAllergy.setSubstance("NSAIDs"); // Map does not relate NSAIDs to Amoxicillin
        allergyRepository.save(nonConflictingAllergy);

        // Prepare updated prescription data (Amoxicillin)
        Medication updated = new Medication();
        updated.setDrugName("Amoxicillin");
        updated.setDose("500 mg");
        updated.setFrequency("2x/day");
        updated.setDuration("7 days");
        updated.setNotes("Take with food");
        updated.setTimestamp(LocalDateTime.now());
        updated.setStatus(true);
        updated.setIsPerscription(true);

        // Execute: should NOT throw a conflict exception
        Medication result;
        try {
            result = medicationService.updateMedication(patientId, doctorId, existing.getId(),updated);
        } catch (ResponseStatusException ex) {
            Assertions.fail("Unexpected conflict detected: " + ex.getReason());
            return; // unreachable after fail, but keeps compiler happy
        }

        // Expected Results
        Assertions.assertEquals("Amoxicillin", result.getDrugName());
        Assertions.assertEquals("500 mg", result.getDose());
        Assertions.assertEquals("2x/day", result.getFrequency());
        Assertions.assertEquals("7 days", result.getDuration());
        Assertions.assertTrue(Boolean.TRUE.equals(result.getIsPerscription()));

        // Verify persisted state
        Medication persisted = medicationRepository.findById(existing.getId()).orElseThrow();
        Assertions.assertEquals("Amoxicillin", persisted.getDrugName());
    }
}
