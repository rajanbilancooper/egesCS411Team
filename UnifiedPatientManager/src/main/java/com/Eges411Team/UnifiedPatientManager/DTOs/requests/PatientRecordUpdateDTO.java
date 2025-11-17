package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import java.util.List;

// DTO for updating patient records
public class PatientRecordUpdateDTO {

    // dont need patientId here since it's in the URL path - obtained by controller

    // fields that the doctor will actually update
    private String phoneNumber;
    private String address;

    // allergy updates
    private List<AllergyAction> allergyActions;

    // medication updates
    private List<MedicationAction> medicationActions;

    // doctor's observation/note (creates a new MedicalHistory entry)
    private String medicalNote;

    // allergy actions - create a nested class
    public static class AllergyAction {
        public enum ActionType { ADD, REMOVE, UPDATE }
        // what was done
        private ActionType action;
        private Integer allergyId; // for REMOVE or UPDATE
        private String substance;    // for ADD or UPDATE

        // what did the doctor specify for the severity of the allergy
        private String severity;     // for ADD or UPDATE

        // what happenss when exposed to the allergen
        private String reaction;     // for ADD or UPDATE

        // getters and setters
        public ActionType getAction() {
            return action;
        }
        public void setAction(ActionType action) {
            this.action = action;
        }
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

    // medication actions - create a nested class (sibling to AllergyAction, not nested inside it)
    public static class MedicationAction {
        public enum ActionType { ADD, REMOVE, UPDATE }
        // what was done
        private ActionType action;
        private Integer medicationId; // for REMOVE or UPDATE
        private String drugName;          // for ADD or UPDATE
        private String dosage;        // for ADD or UPDATE
        private String frequency;     // for ADD or UPDATE
        private String duration;      // for ADD or UPDATE

        // getters and setters
        public ActionType getAction() {
            return action;
        }
        public void setAction(ActionType action) {
            this.action = action;
        }
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
        public String getDosage() {
            return dosage;
        }
        public void setDosage(String dosage) {
            this.dosage = dosage;
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

    // getters and setters for main DTO fields
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public List<AllergyAction> getAllergyActions() {
        return allergyActions;
    }
    public void setAllergyActions(List<AllergyAction> allergyActions) {
        this.allergyActions = allergyActions;
    }
    public List<MedicationAction> getMedicationActions() {
        return medicationActions;
    }
    public void setMedicationActions(List<MedicationAction> medicationActions) {
        this.medicationActions = medicationActions;
    }
    public String getMedicalNote() {
        return medicalNote;
    }
    public void setMedicalNote(String medicalNote) {
        this.medicalNote = medicalNote;
    }
}