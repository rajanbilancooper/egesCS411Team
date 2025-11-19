package com.Eges411Team.UnifiedPatientManager.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRecordUpdateDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
// import necessary repositories, DTOs, and entities
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;

import jakarta.transaction.Transactional;



// service class for patient record related operations
// going to call repositories for Allergy, Medication, Prescription, MedicalHistory
// assemble a PatientRecordDTO to return to controllers

// TODO: find how to go about incorporating DoctorID into the medication entities
@Service
@Transactional
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
        patientRecord.setDateOfBirth(user.getDateOfBirth());
        patientRecord.setGender(user.getGender());


        // populate patientRecord with empty arraylists to be populated
        patientRecord.setAllergies(new ArrayList<>());
        patientRecord.setMedications(new ArrayList<>());
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

    // New lookup by full name for unit testing not-found scenario
    public PatientRecordDTO getPatientRecordByFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Name must be provided");
        }
        String[] parts = fullName.trim().split(" ");
        if (parts.length < 2) {
            throw new RuntimeException("Full name must include first and last name");
        }
        String first = parts[0];
        String last = parts[parts.length - 1]; // allow middle names ignored

        User user = userRepository.findByFirstNameAndLastName(first, last)
            .orElseThrow(() -> new RuntimeException("User not found with name: " + fullName));

        // Reuse existing logic by ID (double fetch acceptable for simplicity)
        return getPatientRecord(user.getId());
    }

    // Creation method to support patient record creation
    public PatientRecordDTO createPatientRecord(User userTemplate, List<Allergy> allergiesInput) {
        if (userTemplate == null) {
            throw new RuntimeException("User template cannot be null");
        }
        if (userTemplate.getUsername() == null || userTemplate.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (userTemplate.getFirstName() == null || userTemplate.getLastName() == null) {
            throw new RuntimeException("First and last name are required");
        }
        if (userTemplate.getEmail() == null || userTemplate.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (userTemplate.getDateOfBirth() == null) {
            throw new RuntimeException("Date of birth is required");
        }

        // Duplicate check: same first name, last name, and date of birth
        boolean duplicateExists = userRepository
            .findByFirstNameAndLastNameAndDateOfBirth(
                userTemplate.getFirstName(),
                userTemplate.getLastName(),
                userTemplate.getDateOfBirth()
            ).isPresent();
        if (duplicateExists) {
            throw new RuntimeException("Patient already exists");
        }

        // Persist User (ID generated by DB)
        User savedUser = userRepository.save(userTemplate);

        // Persist allergies if provided
        if (allergiesInput != null) {
            for (Allergy allergy : allergiesInput) {
                allergy.setPatientId(savedUser.getId());
                allergyRepository.save(allergy);
            }
        }

        // Build the PatientRecordDTO to return to the controller
        PatientRecordDTO patientRecord = new PatientRecordDTO();
        patientRecord.setPatientId(savedUser.getId());
        patientRecord.setFirstName(savedUser.getFirstName());
        patientRecord.setLastName(savedUser.getLastName());
        patientRecord.setEmail(savedUser.getEmail());
        patientRecord.setPhoneNumber(savedUser.getPhoneNumber());
        patientRecord.setAddress(savedUser.getAddress());
        patientRecord.setDateOfBirth(savedUser.getDateOfBirth());
        patientRecord.setGender(savedUser.getGender());
        patientRecord.setAllergies(new java.util.ArrayList<>());
        patientRecord.setMedications(new java.util.ArrayList<>());
        patientRecord.setMedicalHistory(new java.util.ArrayList<>());

        // get the allergies list and loop through each one and create the DTOs for them
        if (allergiesInput != null) {
            for (Allergy allergy : allergiesInput) {
                PatientRecordDTO.AllergyDTO currentAllergyDTO = new PatientRecordDTO.AllergyDTO();
                currentAllergyDTO.setAllergyId(allergy.getId());
                currentAllergyDTO.setSubstance(allergy.getSubstance());
                currentAllergyDTO.setReaction(allergy.getReaction());
                currentAllergyDTO.setSeverity(allergy.getSeverity());
                patientRecord.getAllergies().add(currentAllergyDTO);
            }
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

            // we should get the medications for the user similar to allergy
            List<Medication> existingMedications = medicationRepository.findAllByPatientId(userID);

            // then similarly create a hashmap with the ID's as keys and the medication as the value
            HashMap<Long, Medication> medicationsMap = new HashMap<>();

            // loop through and add them all to the map
            for (Medication medication : existingMedications) {
                medicationsMap.putIfAbsent(medication.getId(), medication);
            }

            for (PatientRecordUpdateDTO.MedicationAction action : recordDTO.getMedicationActions()) {
                switch (action.getAction()) {
                    case ADD:
                        // create a new medication objec
                        Medication newMedication = new Medication();
                        newMedication.setDose(action.getDosage());
                        newMedication.setDuration(action.getDuration());
                        newMedication.setId(action.getMedicationId());
                        newMedication.setDrugName(action.getDrugName());
                        newMedication.setFrequency(action.getFrequency());
                        newMedication.setIsPerscription(action.getIsPrescription());

                        // save it to the repository
                        medicationRepository.save(newMedication);
                        break;


                    case UPDATE:
                    // logic for updating a medication
                        
                    // get the medication ID of the current thing we're updating
                    Long currMedicationID = action.getMedicationId();

                    // get it from the map if it exists, if not create a new object for it
                    if (medicationsMap.containsKey(currMedicationID)) {
                        Medication currMedication = medicationsMap.get(currMedicationID);

                        // set the fields from the action
                        currMedication.setDose(action.getDosage());
                        currMedication.setDrugName(action.getDrugName());
                        currMedication.setDuration(action.getDuration());
                        currMedication.setFrequency(action.getFrequency());
                        currMedication.setIsPerscription(action.getIsPrescription());
                        currMedication.setId(action.getMedicationId());
                        currMedication.setPatientId(userID);

                        // save to database
                        medicationRepository.save(currMedication);
                    }
                    else {
                        // here we create a new medication object and populate it
                        Medication anotherNewMedication = new Medication();
                        
                        // set fields from action
                        anotherNewMedication.setDose(action.getDosage());
                        anotherNewMedication.setDrugName(action.getDrugName());
                        anotherNewMedication.setDuration(action.getDuration());
                        anotherNewMedication.setFrequency(action.getFrequency());
                        anotherNewMedication.setIsPerscription(action.getIsPrescription());
                        anotherNewMedication.setId(action.getMedicationId());
                        anotherNewMedication.setPatientId(userID);

                        //save to database
                        medicationRepository.save(anotherNewMedication);
                    }

                        break;


                    case REMOVE:
                    // logic for removing a medication

                        medicationRepository.deleteById(action.getMedicationId());
                        break;


                    default:
                        break;
                }
            }
        }

        // ** MEDICAL HISTORY ** 
        if (recordDTO.getMedicalNote() != null && !recordDTO.getMedicalNote().trim().isEmpty()) {

            // create a new medical history
            MedicalHistory medicalHistory = new MedicalHistory();
            
            // link the medical history to the current patient
            medicalHistory.setPatientId(userID);
            medicalHistory.setDiagnosis(recordDTO.getMedicalNote());
            medicalHistory.setStartDate(new java.util.Date()); // Current timestamp

            // save to repo
            medicalHistoryRepository.save(medicalHistory);
        }


        // finally, create a patientRecordDTO to return
        PatientRecordDTO patientRecord = new PatientRecordDTO();

        // update the necessary fields in patientRecord
        patientRecord.setFirstName(user.getFirstName());
        patientRecord.setLastName(user.getLastName());
        patientRecord.setAddress(user.getAddress());

        // populate patientRecord with empty arraylists to be populated
        patientRecord.setAllergies(new ArrayList<>());
        patientRecord.setMedications(new ArrayList<>());
        patientRecord.setMedicalHistory(new ArrayList<>());

        // get the list of allergies from the repo and set the nested DTO fields
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

        // get the list of medications from the repo and set the nested DTO fields
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

        // get the list of medical history from the repo and set the nested DTO fields
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

        patientRecord.setDateOfBirth(user.getDateOfBirth());
        patientRecord.setGender(user.getGender());
        patientRecord.setEmail(user.getEmail());
        patientRecord.setPhoneNumber(user.getPhoneNumber());
        
        // return the patient Record
        return ResponseEntity.ok(patientRecord);

    }

}

