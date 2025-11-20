package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AllergyService {

    private final AllergyRepository allergyRepository;

    public AllergyService(AllergyRepository allergyRepository) {
        this.allergyRepository = allergyRepository;
    }

    // GET /{patient_id}/allergies
    public List<Allergy> getAllergiesByPatientId(Long patientId) {
        return allergyRepository.findAllByPatientId(patientId);
    }

    // POST /{patient_id}/allergies
    // Replace all allergies for this patient with the provided list
    public List<Allergy> saveAllergies(Long patientId, List<Allergy> allergies) {
        // delete existing
        List<Allergy> existing = allergyRepository.findAllByPatientId(patientId);
        allergyRepository.deleteAll(existing);

        // set patient_id for each new allergy
        for (Allergy allergy : allergies) {
            allergy.setPatientId(patientId);
        }

        return allergyRepository.saveAll(allergies);
    }

    // POST single allergy (add without deleting existing)
    public Allergy addSingleAllergy(Long patientId, Allergy allergy) {
        // Check for duplicate (same substance, case-insensitive)
        List<Allergy> existing = allergyRepository.findAllByPatientId(patientId);
        for (Allergy ex : existing) {
            if (ex.getSubstance() != null && allergy.getSubstance() != null
                && ex.getSubstance().trim().equalsIgnoreCase(allergy.getSubstance().trim())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Allergy to '" + allergy.getSubstance() + "' already exists for this patient"
                );
            }
        }

        allergy.setPatientId(patientId);
        return allergyRepository.save(allergy);
    }

    // PUT /{patient_id}/allergies/{allergy_id}
    public Allergy updateAllergy(Long patientId, Long allergyId, Allergy updated) {
        Allergy existing = allergyRepository.findById(allergyId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Allergy not found with id: " + allergyId
            ));

        // ensure this allergy belongs to the given patient
        if (existing.getPatientId() == null || !existing.getPatientId().equals(patientId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Allergy does not belong to the specified patient"
            );
        }

        // update allowed fields
        existing.setSubstance(updated.getSubstance());
        existing.setReaction(updated.getReaction());
        existing.setSeverity(updated.getSeverity());

        return allergyRepository.save(existing);
    }

    // GET /{patient_id}/allergies/refresh
    // Currently same as getAllergies; hook external sync here if needed
    public List<Allergy> refreshAllergies(Long patientId) {
        return allergyRepository.findAllByPatientId(patientId);
    }

    // DELETE /{patient_id}/allergies/{allergy_id}
    public void deleteAllergy(Long patientId, Long allergyId) {
        Optional<Allergy> existingOpt = allergyRepository.findById(allergyId);

        Allergy existing = existingOpt.orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Allergy not found with id: " + allergyId
            )
        );

        if (existing.getPatientId() == null || !existing.getPatientId().equals(patientId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Allergy does not belong to the specified patient"
            );
        }

        allergyRepository.delete(existing);
    }
}
