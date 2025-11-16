package com.Eges411Team.UnifiedPatientManager.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

// import necessary repositories, DTOs, and entities
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRecordUpdateDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;



// service class for patient record related operations
// going to call repositories for Allergy, Medication, Prescription, MedicalHistory
// assemble a PatientRecordDTO to return to controllers
@Service
public class PatientRecordService {

    // these tags allow us to get from each of the respective repositories
    @Autowired
    private AllergyRepository allergyRepository;
    @Autowired
    private MedicationRepository medicationRepository;
    @Autowired
    private MedicalHistoryRepo medicalHistoryRepository;
    @Autowired
    private UserRepository userRepository;

    public PatientRecordDTO getPatientRecord(Long userID) {
        // need dependencies for repositories
        PatientRecordDTO patientRecord = new PatientRecordDTO();

        // obtains the user from the repository and throws an exception if user isn't found
        User user = userRepository.findById(userID)
        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userID));

        // getting here assumes that we didn't throw an exception, so no need for a check

        // Map all patient fields from User to the DTO (firstName, lastName, email, phoneNumber, address, dateOfBirth).
        patientRecord.setPatientId(user.getId());
        patientRecord.setFirstName(user.getFirstName());
        patientRecord.setLastName(user.getLastName());
        patientRecord.setEmail(user.getEmail());
        patientRecord.setPhoneNumber(user.getPhoneNumber());
        patientRecord.setAddress(user.getAddress());
        patientRecord.setDateOfBirth(user.getDateOfBirth().toLocalDate());
        patientRecord.setGender(user.getGender());


        // populate patientRecord with empty arraylists to be populated
        patientRecord.setAllergies(new ArrayList<>());
        patientRecord.setMedications(new ArrayList<>());
        patientRecord.setPrescriptions(new ArrayList<>());
        patientRecord.setMedicalHistory(new ArrayList<>());



        // get the allergies
        List<Allergy> allergies = allergyRepository.findAllByPatientId(userID);

        // convert Allergy entities to AllergyDTOs in a loop (if list is empty won't execute)
        for (Allergy allergy : allergies) {
            PatientRecordDTO.AllergyDTO allergyDTO = new PatientRecordDTO.AllergyDTO();
            allergyDTO.setAllergyId(allergy.getId());
            allergyDTO.setSubstance(allergy.getSubstance());
            allergyDTO.setReaction(allergy.getReaction());
            allergyDTO.setSeverity(allergy.getSeverity());
            patientRecord.getAllergies().add(allergyDTO);
        }
    
        // get the medications
        List<Medication> medications = medicationRepository.findAllByPatientId(userID);

        // convert Medication entities to MedicationDTOs in the PatientRecordDTO
        for (Medication medication : medications) {
            PatientRecordDTO.MedicationDTO medicationDTO = new PatientRecordDTO.MedicationDTO();
            medicationDTO.setMedicationId(medication.getId());
            medicationDTO.setDrugName(medication.getDrugName());
            medicationDTO.setDose(medication.getDose());
            medicationDTO.setFrequency(medication.getFrequency());
            patientRecord.getMedications().add(medicationDTO);
        }


        // get the medical history
        List<MedicalHistory> medicalHistories = medicalHistoryRepository.findAllByPatientId(userID);
        
        // convert MedicalHistory entities to MedicalHistoryDTOs
        for (MedicalHistory history : medicalHistories) {
            PatientRecordDTO.MedicalHistoryDTO historyDTO = new PatientRecordDTO.MedicalHistoryDTO();
            historyDTO.setId(history.getId());
            historyDTO.setDoctorId(history.getDoctorId());
            historyDTO.setNotes(history.getDiagnosis());
            if (history.getStartDate() != null) {
                historyDTO.setStartDate(history.getStartDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            }
            patientRecord.getMedicalHistory().add(historyDTO);
        }

        return patientRecord;

    }

    public ResponseEntity<PatientRecordUpdateDTO> updatePatientRecord(Long userID, PatientRecordUpdateDTO recordDTO) {
        // Implementation to update patient record by userID
        return ResponseEntity.ok(recordDTO);
    }

}

