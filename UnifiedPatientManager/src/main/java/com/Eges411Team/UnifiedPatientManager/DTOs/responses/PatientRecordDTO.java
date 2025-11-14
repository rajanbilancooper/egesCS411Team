package com.Eges411Team.UnifiedPatientManager.DTOs.responses;
// imports
import java.util.List;
import java.util.Set;


import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;


// a patient record DTO to encapsulate all relevant patient record information
// omits the need for a patientRecord entity
public class PatientRecordDTO {
    
    // field for patient ID
    private Long patientId;

    // first name field
    private String firstName;

    // last name field
    private String lastName;

    // email field
    private String email;

    // field for list of allergies
    private Set<Allergy> allergies;

    // field for list of medications
    private List<Medication> medications;

}
