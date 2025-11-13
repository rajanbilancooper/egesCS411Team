package com.Eges411Team.UnifiedPatientManager.entity;

import java.util.List;

import jakarta.persistence.*;

/**
 * DEPRECATED: PatientRecord should NOT be a JPA entity.
 * Use PatientRecordDTO (in DTOs/responses) and PatientRecordService instead.
 * 
 * The @OneToMany mappedBy annotations were INCORRECT because child entities 
 * (Allergy, Medication, Prescription) use primitive patient_id fields, not object references.
 * These have been removed. Use PatientRecordService.getPatientRecord(patientId) to assemble records.
 */
@Entity
@Table(name = "patient_record")
@Deprecated(since = "1.0", forRemoval = true)
public class PatientRecord {

    // DEPRECATED: Removed incorrect @OneToMany(mappedBy = "patient") annotations.
    // Child entities do NOT have a "patient" field; they use patient_id (int).
    // Use PatientRecordService.getPatientRecord(patientId) instead.
    
    private List<Allergy> allergies;

    private List<Medication> medications;

    private List<Prescription> prescriptions; 


    public PatientRecord() {
        // no params
    }

    // getter and setter for allergies
    public List<Allergy> getAllergies() {
        return allergies;
    }
    public void setAllergies(List<Allergy> allergies) {
        this.allergies = allergies;
    }

    // getter and setter for medications
    public List<Medication> getMedications() {
        return medications;
    }
    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }
    // getter and setter for prescriptions
    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }
    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }
}