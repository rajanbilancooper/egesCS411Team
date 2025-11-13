package com.Eges411Team.UnifiedPatientManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

// this will be a class for users in the system
import jakarta.persistence.*;

// to use a list of prescriptions
import java.util.List;

// to use a set of allergies
import java.util.Set;


// base class for all users
@Entity

// one database table for all user types
@Table(name = "user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
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
    @Column(name = "password", nullable = false)
    // every user must have a password
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    // role of the user (DOCTOR, NURSE, PATIENT)
    @Column(name = "role", nullable = false, length = 20)
    // every user must have a role
    private Role role; 

    @Column(name = "first_name", length = 50)
    // every user must have a first name
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    // every user must have a last name
    private String lastName;
     
    @Column(name = "phone_number", length = 20)
    // every user must have a phone number
    private String phoneNumber;
   
    @Column(name = "failed_login_attempts")
    //every user has a count of failed login attempts
    private int failedLoginAttempts = 0;
    
    @Column(name = "gender", length = 10)
    //every user has a gender 
    private String gender;

    @Column(name = "date_of_birth")
    //every user has a date of birth
    private String dateOfBirth;
    
    @Column(name = "address", length = 100)
    //every user has an address
    private String address;

    @Column(name = "email", length = 50)
    //every user has an email
    private String email;

    @Column(name = "creation_date")
    //every user has a creation date
    private String creationDate;

    @Column(name = "update_date")
    //every user has an update date
    private String updateDate;


    // omitting patient record relationship - deprecated entity - means that patient records
    // should be handled via PatientRecordService and PatientRecordDTO, NOT via JPA entity mapping.

    // ** relationships -- necessary because without them JPA/Hibernate will not create the foreign key constraints in the database **

    // prescriptions for this user (if patient)
    // NOTE: These mappedBy annotations are INCORRECT - child entities use primitive patient_id fields, not "patient" object refs.
    // They are marked @Transient to prevent JPA mapping errors. Use repositories directly instead.
    @Transient
    private List<Prescription> prescriptionsReceived;

    // allergies for this user (if patient) 
    // NOTE: @OneToMany(mappedBy = "patient") was INCORRECT; Allergy uses patient_id int field.
    // Marked @Transient. Use AllergyRepository.findAllByPatient_id() instead.
    @Transient
    private Set<Allergy> allergies;

    // medical history for this user (if patient)
    // NOTE: @OneToOne(mappedBy = "patient") was INCORRECT; MedicalHistory uses patient_id int field.
    // Marked @Transient. Use MedicalHistoryRepo.findAllByPatient_id() instead.
    @Transient
    private MedicalHistory medicalHistory;

    // medications for this user (if patient)
    // NOTE: @OneToMany(mappedBy = "patient") was INCORRECT; Medication uses patient_id int field.
    // Marked @Transient. Use MedicationRepository.findAllByPatient_id() instead.
    @Transient
    private List<Medication> medications;


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

    // when setting password, hash it before storing
    public void setPassword(String password) {
        // TODO - implement password hashing
        this.passwordHash = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;  
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
    
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    public List<Prescription> getPrescriptionsReceived() {
        return prescriptionsReceived;
    }
    public void setPrescriptionsReceived(List<Prescription> prescriptionsReceived) {
        this.prescriptionsReceived = prescriptionsReceived;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
    public String getUpdateDate() {
        return updateDate;
    }   
    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}      
