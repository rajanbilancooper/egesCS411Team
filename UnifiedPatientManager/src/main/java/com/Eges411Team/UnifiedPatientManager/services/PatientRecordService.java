package com.Eges411Team.UnifiedPatientManager.services;

import java.util.ArrayList;
import java.util.HashMap;
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

    public ResponseEntity<PatientRecordDTO> updatePatientRecord(Long userID, PatientRecordUpdateDTO recordDTO) {
        // Implementation to update patient record by userID

        // firstly obtain the user or throw an exception if they don't exist
        User user = userRepository.findById(userID).orElseThrow(() 
            -> new RuntimeException("User not found with ID: " + userID));
        
        // we want to save the information from the  PatientRecordUpdateDTO that maps to the user being updated
        
        // set new phone number and address and save them
        // we need null checks to determine if the recordDTO has these fields as null or not
        if (recordDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(recordDTO.getPhoneNumber());
        }
        if (recordDTO.getAddress() != null) {
            user.setAddress(recordDTO.getAddress());
        }
        // save the user at this point
        userRepository.save(user);

        // now we must go through the allergies, medication and medical history (they each have an enum with ADD, DELETE, UPDATE)

        // ** ALLERGIES **
        // first see if the DTO contains allergy actions made
        if (recordDTO.getAllergyActions() != null) {
            
            // first we can get all of the patient's allergies from the repository
            List<Allergy> existingAllergies = allergyRepository.findAllByPatientId(userID);

            // make the list into a hashSet for O(1) lookups in the upcoming loop
            HashMap<Long, Allergy> existingAllergiesMap = new HashMap<>();
            for (Allergy allergy : existingAllergies) {
                existingAllergiesMap.putIfAbsent(allergy.getId(), allergy);
            }

            // loop through each action and determine what to do based on the action enum
            for (PatientRecordUpdateDTO.AllergyAction action : recordDTO.getAllergyActions()) {
                switch (action.getAction()) {
                    case ADD:
                        // create a new allergy object and populate it
                        Allergy newAllergy = new Allergy();
                        newAllergy.setPatientId(userID);
                        newAllergy.setReaction(action.getReaction());
                        newAllergy.setSeverity(action.getSeverity());
                        newAllergy.setSubstance(action.getSubstance());

                        // save it to the repository
                        allergyRepository.save(newAllergy);
                        break;

                    case UPDATE:
                        // get the current allergy from the map
                        Long currAllergyId = action.getAllergyId();
                        
                        // get the current allergy using that allergyid if it exists
                        if (existingAllergiesMap.get(currAllergyId) != null) {
                            Allergy currentAllergy = existingAllergiesMap.get(currAllergyId);

                            // then we can reset the fields in the current allergy and save it
                            currentAllergy.setReaction(action.getReaction());
                            currentAllergy.setSeverity(action.getSeverity());
                            currentAllergy.setSubstance(action.getSubstance());

                            // save
                            allergyRepository.save(currentAllergy);
                        }
                        else {
                            // this is the case where they're trying to update a non-existing allergy, which is just
                            // adding a new allergy -- fix logic here and save
                            Allergy anotherNewAllergy = new Allergy();
                            anotherNewAllergy.setPatientId(userID);
                            anotherNewAllergy.setId(currAllergyId);
                            anotherNewAllergy.setReaction(action.getReaction());
                            anotherNewAllergy.setSeverity(action.getSeverity());
                            anotherNewAllergy.setSubstance(action.getSubstance());

                            // save to repo
                            allergyRepository.save(anotherNewAllergy);
                        }
                        
                        break;

                    case REMOVE:
                        // remove the allergy by its ID
                        allergyRepository.deleteById(action.getAllergyId());
                        break;
                }
            }
        }

        
        // ** MEDICATIONS **
        // check if DTO contains changes made to medications
        if (recordDTO.getMedicationActions() != null) {
            for (PatientRecordUpdateDTO.MedicationAction action : recordDTO.getMedicationActions()) {
                switch (action.getAction()) {
                    case ADD:
                    // logic for adding a medication
                        break;


                    case UPDATE:
                    // logic for updating a medication
                        break;


                    case REMOVE:
                    // logic for removing a medication
                        break;


                    default:
                        break;
                }
            }
        }

        // ** MEDICAL HISTORY ** 
        // we have a 'medicalNote' as an attribute -- treat as a new Medical History -- TODO


        // finally, create a patientRecordDTO to return
        PatientRecordDTO patientRecord = new PatientRecordDTO();

        // update the necessary fields in patientRecord
        



        return ResponseEntity.ok(patientRecord);

    }

}

