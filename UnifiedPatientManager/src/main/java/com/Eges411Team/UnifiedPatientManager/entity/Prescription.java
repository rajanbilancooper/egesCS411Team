package com.Eges411Team.UnifiedPatientManager.entity;

// import 
import jakarta.persistence.*;

// entity annotation to map this class to a database table
@Entity
// gives the database table a name
@Table(name = "prescriptions")
public class Prescription {

    // attributes

    // the prescription ID will be our primary key
    @Id
    // specifies that the value will be generated automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // we must give the column an explicit name
    @Column(name = "prescription_id")
    private Integer prescriptionID;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String prescribingDoctorID;
    private boolean isActive; // indicates if the prescription is currently active
    private Integer patientID; // foreign key to link to Patient

    // constructor for Prescription class
    public Prescription(String medicationName, String dosage, String frequency, String prescribingDoctorID, boolean isActive, Integer patientID) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.prescribingDoctorID = prescribingDoctorID;
        this.isActive = isActive;
        this.patientID = patientID;
    }

    // empty constructor required by JPA
    public Prescription() {
        // no params
    }

    // dont need patientID setter -- handled by JPA relationship??? TODO confirm with team
    public Integer getPatientID() {
        return patientID;
    }

    // getters and setters for the ID
    public Integer getPrescriptionID() {
        return prescriptionID;  
    }
    // do we need a setter for ID -- auto-generated? Must check with team TODO
    public void setPrescriptionID(Integer prescriptionID) {
        this.prescriptionID = prescriptionID;
    }

    // getter and setter for medicationName
    public String getMedicationName() {
        return medicationName;
    }
    // do we need a setter for medicationName -- usually doesn't change? Must check with team TODO
    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    // getter and setter for dosage
    public String getDosage() {
        return dosage; 
    }
    // do we need a setter for dosage -- usually doesn't change? Must check with team TODO
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    // getter and setter for frequency
    public String getFrequency() {
        return frequency;
    }
    // we CAN change the frequency of a patient's prescription if the doctor updates it
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    // getter and setter for prescribingDoctorID
    public String getPrescribingDoctorID() {
        return prescribingDoctorID;
    }
    // we CAN change the prescribing doctor if needed -- could be different specialist
    public void setPrescribingDoctorID(String prescribingDoctorID) {
        this.prescribingDoctorID = prescribingDoctorID;
    }

    // getter and setter for isActive
    public boolean getIsActive() {
        return isActive;
    }
    // we CAN change the active status of a prescription if needed -- e.g., if discontinued/stopped or restarted
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

}