package com.Eges411Team.UnifiedPatientManager.repositories;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    // Uses the Java field name "patient_id"
    List<Allergy> findAllByPatientId(Long patientId);
}
