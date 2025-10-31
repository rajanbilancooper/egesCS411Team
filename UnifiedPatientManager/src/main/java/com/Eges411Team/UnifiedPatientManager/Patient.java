package com.Eges411Team.UnifiedPatientManager;

// this allows for database mapping
import jakarta.persistence.*;

// use the built in Date class and List class
import java.util.Date;
import java.util.List;

// import the other classes needed for relationships
import com.Eges411Team.UnifiedPatientManager.PatientRecord;
import com.Eges411Team.UnifiedPatientManager.Allergy;
import com.Eges411Team.UnifiedPatientManager.Medication;
import com.Eges411Team.UnifiedPatientManager.Prescription;

// entity annotation to map this class to a database table
@Entity
// gives the database table a name 
@Table(name = "patients")
public class Patient {
    // fields

    // our primary key will be the patient ID
    // denotes this field as the primary key
    @Id
    // specifies that the value will be generated automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // we must give the column a name
    @Column(name = "patient_id")
    private Integer id;

    // SpringBoot automatically creates names for the other columns
    // the following are fields of a 'patient' with no relationships
    private String name;
    private Integer age;
    private String gender;
    private Date dateOfBirth;
    private String address;
    private String phoneNumber;

    // the following fields are relationships to other classes

    // a patient has one patient record
    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private PatientRecord patientRecord;

    // a patient can have many allergies
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Allergy> allergies;

    // and many medications
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Medication> medications;

    // and prescriptions
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Prescription> prescriptions;  


    // empty constructor required by JPA
    public Patient() {
        // no params
    }

    // create the Constructor
    public Patient(String name, Integer age, String gender, Date dateOfBirth, String address, String phoneNumber) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phoneNumber = phoneNumber;

        // need to validate inputs TODO

    }


    // ID getter and setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // name getter and setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // age getter and setter
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // gender getter and setter
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    // date of birth getter and setter
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // address getter and setter
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // phone number getter and setter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // getter and setter for patient record
    public PatientRecord getPatientRecord() {
        return patientRecord;
    }

    public void setPatientRecord(PatientRecord patientRecord) {
        this.patientRecord = patientRecord;
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
