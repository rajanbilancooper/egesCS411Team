package com.Eges411Team.UnifiedPatientManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

// this will be a class for users in the system
import jakarta.persistence.*;

// to use a list of prescriptions
import java.util.List;


// base class for all users
@Entity

// one database table for all user types
@Table(name = "users")
public class User {
    // fields common to all users
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    // will be used as primary key
    private Long id;

    @Column(nullable = false, unique = true)
    // every user must have a unique username
    private String username;

    @JsonIgnore
    // password does not get returned in JSON responses
    @Column(name = "password_hash", nullable = false)
    // every user must have a password
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    // role of the user (DOCTOR, NURSE, PATIENT)
    @Column(nullable = false)
    // every user must have a role
    private Role role; 

    // only patients have a patientRecord
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    // avoids returning entire patient record in user JSON
    private PatientRecord patientRecord;

    // prescriptions this user authored (if doctor)
    @OneToMany(mappedBy = "doctor")
    private List<Prescription> prescriptionsAuthored;

    // prescriptions for this user (if patient)
    @OneToMany(mappedBy = "patient")
    private List<Prescription> prescriptionsReceived;


    // empty constructor required by JPA
    public User() {
        // no params
    }

    // constructor with parameters
    public User(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    public PatientRecord getPatientRecord() {
        return patientRecord;
    }
    public void setPatientRecord(PatientRecord patientRecord) {
        this.patientRecord = patientRecord;
    }
    public List<Prescription> getPrescriptionsAuthored() {
        return prescriptionsAuthored;
    }
    public void setPrescriptionsAuthored(List<Prescription> prescriptionsAuthored) {
        this.prescriptionsAuthored = prescriptionsAuthored;
    }
    public List<Prescription> getPrescriptionsReceived() {
        return prescriptionsReceived;
    }
    public void setPrescriptionsReceived(List<Prescription> prescriptionsReceived) {
        this.prescriptionsReceived = prescriptionsReceived;
    }
}