package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MedicalHistoryService {

    private final MedicalHistoryRepo medicalHistoryRepository;

    public MedicalHistoryService(MedicalHistoryRepo medicalHistoryRepository) {
        this.medicalHistoryRepository = medicalHistoryRepository;
    }

    // GET /{patient_id}/medicalhistory
    public List<MedicalHistory> getMedicalHistoryByPatientId(Long patientId) {
        return medicalHistoryRepository.findAllByPatientId(patientId);
    }

    // POST /{patient_id}/medicalhistory
    // Replace all medical-history records for this patient with the provided list
    public List<MedicalHistory> saveMedicalHistory(Long patientId, List<MedicalHistory> historyList) {
        // delete existing for the patient
        List<MedicalHistory> existing = medicalHistoryRepository.findAllByPatientId(patientId);
        medicalHistoryRepository.deleteAll(existing);

        // enforce ownership
        for (MedicalHistory mh : historyList) {
            mh.setPatientId(patientId);
        }

        return medicalHistoryRepository.saveAll(historyList);
    }

    // PUT /{patient_id}/medicalhistory/{history_id}
    public MedicalHistory updateMedicalHistory(Long patientId, Long historyId, MedicalHistory updated) {
        MedicalHistory existing = medicalHistoryRepository.findById(historyId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Medical history record not found with id: " + historyId
            ));

        // ensure this record belongs to the given patient
        if (existing.getPatientId() == null || !existing.getPatientId().equals(patientId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Medical history record does not belong to the specified patient"
            );
        }

        // update allowed fields
        existing.setDoctorId(updated.getDoctorId());
        existing.setDiagnosis(updated.getDiagnosis());
        existing.setFrequency(updated.getFrequency());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());

        return medicalHistoryRepository.save(existing);
    }

    // GET /{patient_id}/medicalhistory/refresh
    // Currently same as get; hook external sync here if needed
    public List<MedicalHistory> refreshMedicalHistory(Long patientId) {
        return medicalHistoryRepository.findAllByPatientId(patientId);
    }

    // DELETE /{patient_id}/medicalhistory/{history_id}
    public void deleteMedicalHistory(Long patientId, Long historyId) {
        Optional<MedicalHistory> existingOpt = medicalHistoryRepository.findById(historyId);

        MedicalHistory existing = existingOpt.orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Medical history record not found with id: " + historyId
            )
        );

        if (existing.getPatientId() == null || !existing.getPatientId().equals(patientId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Medical history record does not belong to the specified patient"
            );
        }

        medicalHistoryRepository.delete(existing);
    }

    // saving a single record
    public MedicalHistory saveMedicalHistory(MedicalHistory entity) {
        if (entity.getStartDate() != null && entity.getEndDate() != null
                && entity.getEndDate().before(entity.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "end_date must be after or equal to start_date");
        }

        return medicalHistoryRepository.save(entity);
    }
}
