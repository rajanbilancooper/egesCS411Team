package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MedicalHistoryService {

    private final MedicalHistoryRepo medicalHistoryRepository;

    public MedicalHistoryService(MedicalHistoryRepo medicalHistoryRepository) {
        this.medicalHistoryRepository = medicalHistoryRepository;
    }

    // GET /{patient_id}/medicalhistory
    public List<MedicalHistory> getMedicalHistoryByPatientId(int patientId) {
        return medicalHistoryRepository.findAllByPatient_id(patientId);
    }

    // POST /{patient_id}/medicalhistory
    // Replace all medical-history records for this patient with the provided list
    public List<MedicalHistory> saveMedicalHistory(int patientId, List<MedicalHistory> historyList) {
        // delete existing for the patient
        List<MedicalHistory> existing = medicalHistoryRepository.findAllByPatient_id(patientId);
        medicalHistoryRepository.deleteAll(existing);

        // enforce ownership
        for (MedicalHistory mh : historyList) {
            mh.setPatient_id(patientId);
        }

        return medicalHistoryRepository.saveAll(historyList);
    }

    // PUT /{patient_id}/medicalhistory/{history_id}
    public MedicalHistory updateMedicalHistory(int patientId, int historyId, MedicalHistory updated) {
        MedicalHistory existing = medicalHistoryRepository.findById(historyId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Medical history record not found with id: " + historyId
            ));

        // ensure this record belongs to the given patient
        if (existing.getPatient_id() != patientId) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Medical history record does not belong to the specified patient"
            );
        }

        // update allowed fields
        existing.setDoctor_id(updated.getDoctor_id());
        existing.setDiagnosis(updated.getDiagnosis());
        existing.setFrequency(updated.getFrequency());
        existing.setStart_date(updated.getStart_date());
        existing.setEnd_date(updated.getEnd_date());

        return medicalHistoryRepository.save(existing);
    }

    // GET /{patient_id}/medicalhistory/refresh
    // Currently same as get; hook external sync here if needed
    public List<MedicalHistory> refreshMedicalHistory(int patientId) {
        return medicalHistoryRepository.findAllByPatient_id(patientId);
    }

    // DELETE /{patient_id}/medicalhistory/{history_id}
    public void deleteMedicalHistory(int patientId, int historyId) {
        Optional<MedicalHistory> existingOpt = medicalHistoryRepository.findById(historyId);

        MedicalHistory existing = existingOpt.orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Medical history record not found with id: " + historyId
            )
        );

        if (existing.getPatient_id() != patientId) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Medical history record does not belong to the specified patient"
            );
        }

        medicalHistoryRepository.delete(existing);
    }
}