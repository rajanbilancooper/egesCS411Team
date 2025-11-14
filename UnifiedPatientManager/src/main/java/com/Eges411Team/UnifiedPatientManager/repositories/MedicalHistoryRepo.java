package com.Eges411Team.UnifiedPatientManager.repositories;

import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Date;

public interface MedicalHistoryRepo extends JpaRepository<MedicalHistory, Long> {

    // Fetch all medical history records for a given patient
    List<MedicalHistory> findAllByPatientId(Long patientId);

    // Fetch medical history by doctor
    List<MedicalHistory> findAllByDoctorId(Long doctorId);

    // Get active medical histories (no end_date yet)
    List<MedicalHistory> findAllByEndDateIsNull();

    // Filter by date range (e.g., all records between two dates)
    List<MedicalHistory> findAllByStartDateBetween(Date start, Date end);
}

