package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    // GET /{patient_id}/medications
    public List<Medication> getMedicationsByPatientId(int patientId) {
        return medicationRepository.findAllByPatient_id(patientId);
    }

    // POST /{patient_id}/providers/{provider_id}/medications
    // Replace all medications for this patient; assign provider (doctor) to each entry.
    public List<Medication> saveMedications(int patientId, int providerId, List<Medication> medications) {
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
    public Medication updateMedication(int patientId, int providerId, int medicationId, Medication updated) {
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
    public List<Medication> refreshMedications(int patientId) {
        return medicationRepository.findAllByPatient_id(patientId);
    }

    // DELETE /{patient_id}/medications/{medication_id}
    public void deleteMedication(int patientId, int medicationId) {
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
