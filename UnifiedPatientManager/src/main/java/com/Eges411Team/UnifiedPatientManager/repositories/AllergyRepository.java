package com.Eges411Team.UnifiedPatientManager.repositories;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AllergyRepository extends JpaRepository<Allergy, Integer> {

    // Uses the Java field name "patient_id"
    List<Allergy> findAllByPatient_id(int patient_id);
}
