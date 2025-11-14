package com.Eges411Team.UnifiedPatientManager.DTOs.responses;
// imports
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;

// a patient record DTO to encapsulate all relevant patient record information
// omits the need for a patientRecord entity
public class PatientRecordDTO {
    
    // field for patient ID
    private Long patientId;

    // only a getter for patientId to make it read-only
    public Long getPatientId() {
        return patientId;
    }

    // first name field
    private String firstName;

    // getter and setter for firstName
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // last name field
    private String lastName;

    // getter and setter for lastName
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // email field
    private String email;

    // getter and setter for email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // phone number field
    private String phoneNumber;

    // getter and setter for phoneNumber
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // address field
    private String address;

    // getter and setter for address
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    // date of birth field
    private LocalDate dateOfBirth;

    // getter and setter for dateOfBirth
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // gender field
    private String gender;

    // getter and setter for gender
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    // we need individual nested DTOs for allergies, medications, prescriptions
    private List<AllergyDTO> allergies;

    // nested DTO
    public static class AllergyDTO {
        private Integer allergyId;    // so doctor can reference it for update/remove
        private String substance;     // what the patient is allergic to
        private String severity;      // low/medium/high/critical
        private String reaction;      // what happens: rash, swelling, etc

        // getters and setters
        public Integer getAllergyId() {
            return allergyId;
        }
        public void setAllergyId(Integer allergyId) {
            this.allergyId = allergyId;
        }
        public String getSubstance() {
            return substance;
        }
        public void setSubstance(String substance) {
            this.substance = substance;
        }
        public String getSeverity() {
            return severity;
        }
        public void setSeverity(String severity) {
            this.severity = severity;
        }
        public String getReaction() {
            return reaction;
        }
        public void setReaction(String reaction) {
            this.reaction = reaction;
        }
    }

    // getter and setter for allergies
    public List<AllergyDTO> getAllergies() {
        return allergies;
    }
    public void setAllergies(List<AllergyDTO> allergies) {
        this.allergies = allergies;
    }

    private List<MedicationDTO> medications;

    // medication DTO nested class
    public static class MedicationDTO {
        private Integer medicationId; // so doctor can reference it for update/remove
        private String drugName;      // ibuprofen, lisinopril, etc
        private String dose;          // 500mg, 10mg, etc
        private String frequency;     // once daily, twice daily, etc
        private String duration;      // 30 days, ongoing, etc

        // getters and setters
        public Integer getMedicationId() {
            return medicationId;
        }
        public void setMedicationId(Integer medicationId) {
            this.medicationId = medicationId;
        }
        public String getDrugName() {
            return drugName;
        }
        public void setDrugName(String drugName) {
            this.drugName = drugName;
        }
        public String getDose() {
            return dose;
        }
        public void setDose(String dose) {
            this.dose = dose;
        }
        public String getFrequency() {
            return frequency;
        }
        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }
        public String getDuration() {
            return duration;
        }
        public void setDuration(String duration) {
            this.duration = duration;
        }
    }

    // getter and setter for medications
    public List<MedicationDTO> getMedications() {
        return medications;
    }
    public void setMedications(List<MedicationDTO> medications) {
        this.medications = medications;
    }

    private List<PrescriptionDTO> prescriptions;

    // nested PrescriptionDTO class
    public static class PrescriptionDTO {
        private Integer id;           // identifies the prescription
        private Integer doctorId;     // which doctor issued it
        private String medicationName;// the medication prescribed
        private LocalDateTime dateIssued; // when it was issued

        // getters and setters
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public Integer getDoctorId() {
            return doctorId;
        }
        public void setDoctorId(Integer doctorId) {
            this.doctorId = doctorId;
        }
        public String getMedicationName() {
            return medicationName;
        }
        public void setMedicationName(String medicationName) {
            this.medicationName = medicationName;
        }
        public LocalDateTime getDateIssued() {
            return dateIssued;
        }
        public void setDateIssued(LocalDateTime dateIssued) {
            this.dateIssued = dateIssued;
        }
    }

    // getter and setter for prescriptions
    public List<PrescriptionDTO> getPrescriptions() {
        return prescriptions;
    }
    public void setPrescriptions(List<PrescriptionDTO> prescriptions) {
        this.prescriptions = prescriptions;
    }

    private List<MedicalHistoryDTO> medicalHistory;

    // nested MedicalHistoryDTO class
    public static class MedicalHistoryDTO {
        private Integer id;           // identifies the history entry
        private Integer doctorId;     // which doctor made this entry
        private String notes;         // what the doctor observed/wrote
        private LocalDateTime startDate; // when this entry was created

        // getters and setters
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public Integer getDoctorId() {
            return doctorId;
        }
        public void setDoctorId(Integer doctorId) {
            this.doctorId = doctorId;
        }
        public String getNotes() {
            return notes;
        }
        public void setNotes(String notes) {
            this.notes = notes;
        }
        public LocalDateTime getStartDate() {
            return startDate;
        }
        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }
    }

    // getter and setter for medicalHistory
    public List<MedicalHistoryDTO> getMedicalHistory() {
        return medicalHistory;
    }
    public void setMedicalHistory(List<MedicalHistoryDTO> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
    
}