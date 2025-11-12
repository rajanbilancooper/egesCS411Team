package com.Eges411Team.UnifiedPatientManager.entity;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "patient_record")
public class PatientRecord {

    // attributes

    // a patient can have many allergies
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Allergy> allergies;

    // and many medications
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Medication> medications;

    // and prescriptions
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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