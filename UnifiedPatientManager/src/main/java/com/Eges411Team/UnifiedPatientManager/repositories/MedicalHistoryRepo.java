package com.Eges411Team.UnifiedPatientManager.repositories;

import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Date;

public interface MedicalHistoryRepo extends JpaRepository<MedicalHistory, Integer> {

    // Fetch all medical history records for a given patient
    List<MedicalHistory> findAllByPatient_id(Long patient_id);

    // Fetch medical history by doctor
    List<MedicalHistory> findAllByDoctor_id(Long doctor_id);

    // Get active medical histories (no end_date yet)
    List<MedicalHistory> findAllByEnd_dateIsNull();

    // Filter by date range (e.g., all records between two dates)
    List<MedicalHistory> findAllByStart_dateBetween(Date start, Date end);
}

