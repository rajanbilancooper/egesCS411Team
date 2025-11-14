package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final AllergyRepository allergyRepository;

    // Allergy → list of medications that should NOT be prescribed
    private static final Map<String, List<String>> ALLERGY_CONFLICTS = Map.of(
        // Antibiotic-related
        "Penicillin", List.of("Penicillin", "Amoxicillin", "Ampicillin", "Oxacillin", "Piperacillin"),
        "Cephalosporins", List.of("Cephalexin", "Ceftriaxone", "Cefuroxime"),
        "Sulfa", List.of("Sulfamethoxazole", "Trimethoprim-Sulfamethoxazole", "Sulfasalazine"),

        // Pain / NSAID related
        "NSAIDs", List.of("Ibuprofen", "Naproxen", "Aspirin", "Diclofenac", "Indomethacin"),
        "Aspirin", List.of("Aspirin", "Ibuprofen", "Naproxen"),

        // Opioid-related
        "Opioids", List.of("Morphine", "Codeine", "Oxycodone", "Hydrocodone", "Hydromorphone"),

        // Cardiovascular / statins / beta-blockers
        "StatinIntolerance", List.of("Atorvastatin", "Simvastatin", "Rosuvastatin", "Pravastatin"),
        "BetaBlockers", List.of("Metoprolol", "Atenolol", "Propranolol"),

        // Endocrine
        "ThyroidHormone", List.of("Levothyroxine", "Liothyronine"),
        "Insulin", List.of("Insulin Glargine", "Insulin Lispro", "Insulin Aspart")
    );

    // Medication ↔ medication interaction pairs
    private static final List<List<String>> MED_INTERACTIONS = List.of(
        // Anticoagulant + NSAID (demo: bleeding risk)
        List.of("Warfarin", "Aspirin"),
        List.of("Warfarin", "Ibuprofen"),
        List.of("Warfarin", "Naproxen"),

        // Two BP drugs (demo: duplicate therapy)
        List.of("Lisinopril", "Losartan"),
        List.of("Lisinopril", "Amlodipine"),

        // Sedatives + opioids (demo: CNS depression)
        List.of("Diazepam", "Oxycodone"),
        List.of("Gabapentin", "Oxycodone"),

        // SSRI + NSAID (demo: bleeding risk)
        List.of("Sertraline", "Ibuprofen"),
        List.of("Sertraline", "Aspirin"),

        // Statin + fibrate-style lipid drug (demo: muscle risk)
        List.of("Atorvastatin", "Gemfibrozil")
    );


    public MedicationService(MedicationRepository medicationRepository, AllergyRepository allergyRepository) {
        this.medicationRepository = medicationRepository;
        this.allergyRepository = allergyRepository;
    }

    /**
     * Checks:
     * 1) Each medication against patient's allergies (ALLERGY_CONFLICTS)
     * 2) Every pair of medications against MED_INTERACTIONS
     * Throws ResponseStatusException if any conflict is found.
     */
    private void checkForConflictsOrThrow(long patientId, List<Medication> meds) {
        // 1) Med vs allergies
        List<Allergy> allergies = allergyRepository.findAllByPatient_id(patientId);

        for (Medication med : meds) {
            // Only check prescriptions if you want
            if (!Boolean.TRUE.equals(med.getIs_perscription())) {
                continue;
            }

            String drugName = med.getDrug_name();
            if (drugName == null) continue;

            for (Allergy allergy : allergies) {
                String substance = allergy.getSubstance();
                if (substance == null) continue;

                List<String> badDrugs = ALLERGY_CONFLICTS.get(substance);
                if (badDrugs != null && badDrugs.stream()
                        .anyMatch(bad -> bad.equalsIgnoreCase(drugName))) {

                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Medication '" + drugName +
                        "' conflicts with allergy to '" + substance + "'"
                    );
                }
            }
        }

        // 2) Med vs med (drug–drug interactions)
        for (int i = 0; i < meds.size(); i++) {
            Medication m1 = meds.get(i);
            String d1 = m1.getDrug_name();
            if (d1 == null) continue;

            for (int j = i + 1; j < meds.size(); j++) {
                Medication m2 = meds.get(j);
                String d2 = m2.getDrug_name();
                if (d2 == null) continue;

                if (isBadInteraction(d1, d2)) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Medication interaction detected between '" +
                        d1 + "' and '" + d2 + "'"
                    );
                }
            }
        }
    }

    // Helper: checks if two drug names form a "bad pair" (ignoring order & case)
    private boolean isBadInteraction(String d1, String d2) {
        for (List<String> pair : MED_INTERACTIONS) {
            if (pair.size() != 2) continue;

            String a = pair.get(0);
            String b = pair.get(1);

            boolean match =
                (a.equalsIgnoreCase(d1) && b.equalsIgnoreCase(d2)) ||
                (a.equalsIgnoreCase(d2) && b.equalsIgnoreCase(d1));

            if (match) return true;
        }
        return false;
    }

    // GET /{patient_id}/medications
    public List<Medication> getMedicationsByPatientId(Long patientId) {
        return medicationRepository.findAllByPatient_id(patientId);
    }

    // POST /{patient_id}/providers/{provider_id}/medications
    // Replace all medications for this patient; assign provider (doctor) to each entry.
    public List<Medication> saveMedications(Long patientId, Long providerId, List<Medication> medications) {
        // Remove existing meds for this patient
        List<Medication> existing = medicationRepository.findAllByPatient_id(patientId);
        medicationRepository.deleteAll(existing);

        // Set correct foreign keys on incoming list
        for (Medication med : medications) {
            med.setPatient_id(patientId);
            med.setDoctor_id(providerId);
        }

        return medicationRepository.saveAll(medications);
    }

    // PUT /{patient_id}/providers/{provider_id}/medications/{medication_id}
    public Medication updateMedication(Long patientId, Long providerId, Long medicationId, Medication updated) {
        Medication existing = medicationRepository.findById(medicationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Medication not found with id: " + medicationId
            ));

        // Ensure medication belongs to this patient
        if (existing.getPatient_id() != patientId) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Medication does not belong to the specified patient"
            );
        }

        //Check conflicts for this updated med (wrapped in a list)
        checkForConflictsOrThrow(patientId, List.of(updated));

        // Ensure provider mapping
        existing.setDoctor_id(providerId);

        // Copy updatable fields (align these with your Medication entity)
        existing.setDrug_name(updated.getDrug_name());
        existing.setDose(updated.getDose());
        existing.setFrequency(updated.getFrequency());
        existing.setDuration(updated.getDuration());
        existing.setNotes(updated.getNotes());
        existing.setTimestamp(updated.getTimestamp());
        existing.setStatus(updated.getStatus());
        existing.setIs_perscription(updated.getIs_perscription());

        return medicationRepository.save(existing);
    }

    // GET /{patient_id}/medications/refresh
    // Currently same as getMedicationsByPatientId; hook external sync if needed.
    public List<Medication> refreshMedications(Long patientId) {
        return medicationRepository.findAllByPatient_id(patientId);
    }

    // DELETE /{patient_id}/medications/{medication_id}
    public void deleteMedication(Long patientId, Long medicationId) {
        Medication existing = medicationRepository.findById(medicationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Medication not found with id: " + medicationId
            ));

        if (existing.getPatient_id() != patientId) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Medication does not belong to the specified patient"
            );
        }

        medicationRepository.delete(existing);
    }
}
