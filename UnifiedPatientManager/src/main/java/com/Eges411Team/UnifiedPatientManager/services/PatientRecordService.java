package com.Eges411Team.UnifiedPatientManager.services;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

// import necessary repositories and DTOs
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO.AllergyDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRecordUpdateDTO;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;

import java.util.List;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;

// service class for patient record related operations
// going to call repositories for Allergy, Medication, Prescription, MedicalHistory
// assemble a PatientRecordDTO to return to controllers
@Service
public class PatientRecordService {

    @Autowired
    private AllergyRepository allergyRepository;
    @Autowired
    private MedicationRepository medicationRepository;
    @Autowired
    private MedicalHistoryRepo medicalHistoryRepository;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<PatientRecordDTO> getPatientRecord(Long userID) {
        // need dependencies for repositories
        PatientRecordDTO patientRecord = new PatientRecordDTO();
        // populate patientRecord with data from repositories

        // get the allergies
        List<Allergy> allergies = allergyRepository.findAllByPatient_id(userID);

        // convert Allergy entities to AllergyDTOs in a loop
        for (Allergy allergy : allergies) {
            AllergyDTO allergyDTO = new AllergyDTO();
            allergyDTO.setAllergyId(allergy.getId());
            allergyDTO.setReaction(allergy.getReaction());
            allergyDTO.setSeverity(allergy.getSeverity());
            patientRecord.getAllergies().add(allergyDTO);
        }
        
        // get the medications
        List<Medication> medications = medicationRepository.findAllByPatient_id(userID);

        // convert Medication entities to MedicationDTOs in the PatientRecordDTO
        for (Medication medication : medications) {
            PatientRecordDTO.MedicationDTO medicationDTO = new PatientRecordDTO.MedicationDTO();
            medicationDTO.setMedicationId(medication.getId());
            medicationDTO.setDrugName(medication.getDrug_name());
            medicationDTO.setDose(medication.getDose());
            medicationDTO.setFrequency(medication.getFrequency());
            patientRecord.getMedications().add(medicationDTO);
        }


        // get the medical history
        List<MedicalHistory> medicalHistories = medicalHistoryRepository.findAllByPatient_id(userID);
        
        
        userRepository.findById(userID).ifPresent(user -> patientRecord.setPatientId(user.getId()));
        

    }

    public ResponseEntity<PatientRecordUpdateDTO> updatePatientRecord(Long userID, PatientRecordUpdateDTO recordDTO) {
        // Implementation to update patient record by userID
        return ResponseEntity.ok(recordDTO);
    }

}

