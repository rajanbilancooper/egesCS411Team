package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

// DTO for updating patient records
public class PatientRecordUpdateRequestDTO {

    // field for patient ID
    private Long patientId;

    // first name field
    private String firstName;

    // last name field
    private String lastName;

    // email field
    private String email;

    // 

    // getter and setter for patientId
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    // getter and setter for firstName
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // getter and setter for lastName
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // getter and setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}