package com.Eges411Team.UnifiedPatientManager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRecordUpdateDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRegistrationRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.AllergyRequest;
import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.services.PatientRecordService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;

// controller for patient record related endpoints
@RestController
@RequestMapping("/api/patients")
public class PatientRecordController {
    // dependency injection of the patientRecord Service
    @Autowired
    PatientRecordService patientRecordService;

    @Autowired
    PasswordEncoder passwordEncoder;

    // need a method for getting the patientRecord
    @GetMapping("/{id}")
    public ResponseEntity<PatientRecordDTO> getPatientRecord(@PathVariable("id") Long patientID) {
        
        // gets the record using the service using the User ID
        return ResponseEntity.ok(patientRecordService.getPatientRecord(patientID));
    }

    // method for updating a patientRecord
    @PutMapping("/{id}")
    public ResponseEntity<PatientRecordDTO> updatePatientRecord(@PathVariable("id") Long patientID, @RequestBody PatientRecordUpdateDTO recordUpdateDTO) {

        // updates the record using the service using the ID and the DTO
        return patientRecordService.updatePatientRecord(patientID, recordUpdateDTO);
    }

    // method for getting a patient record by full name
    @GetMapping("/search")
    public ResponseEntity<PatientRecordDTO> getPatientRecordByName(@RequestParam String fullName) {
        
        // gets the record using the service using the full name
        return ResponseEntity.ok(patientRecordService.getPatientRecordByFullName(fullName));
    }

    // partial search returning list of patient records (basic info)
    @GetMapping("/searchMany")
    public ResponseEntity<List<PatientRecordDTO>> searchPatients(@RequestParam("query") String query) {
        List<PatientRecordDTO> list = patientRecordService.searchPatients(query);
        return ResponseEntity.ok(list);
    }

    // method to create a patient record (patient user + optional allergies)
    @PostMapping("/")
    public ResponseEntity<PatientRecordDTO> createPatientRecord(@Valid @RequestBody PatientRegistrationRequest request) {

        // Build User entity
        User userTemplate = new User();
        userTemplate.setUsername(request.getUsername());
        String rawPassword = (request.getPassword() == null || request.getPassword().isBlank()) ? "tempPass123" : request.getPassword();
        userTemplate.setPassword(passwordEncoder.encode(rawPassword));
        userTemplate.setRole(Role.PATIENT); // registering a patient
        userTemplate.setFirstName(request.getFirstName());
        userTemplate.setLastName(request.getLastName());
        userTemplate.setEmail(request.getEmail());
        userTemplate.setPhoneNumber(request.getPhoneNumber());
        userTemplate.setAddress(request.getAddress() == null ? "" : request.getAddress());
        userTemplate.setGender(request.getGender() == null ? "OTHER" : request.getGender());
        userTemplate.setHeight(request.getHeight());
        userTemplate.setWeight(request.getWeight());
        // Parse date string (YYYY-MM-DD) to LocalDateTime at start of day
        try {
            java.time.LocalDate dateOnly = java.time.LocalDate.parse(request.getDateOfBirth());
            userTemplate.setDateOfBirth(dateOnly.atStartOfDay());
        } catch (Exception ex) {
            throw new RuntimeException("Invalid date format; expected YYYY-MM-DD");
        }
        userTemplate.setCreationDate(java.time.LocalDateTime.now());
        userTemplate.setUpdateDate(java.time.LocalDateTime.now());
        userTemplate.setLastLoginTime(java.time.LocalDateTime.now());

        // Convert allergy requests to entity list
        List<Allergy> allergyEntities = new java.util.ArrayList<>();
        if (request.getAllergies() != null) {
            for (AllergyRequest ar : request.getAllergies()) {
                Allergy a = new Allergy();
                a.setSubstance(ar.getSubstance());
                a.setReaction(ar.getReaction());
                a.setSeverity(ar.getSeverity());
                allergyEntities.add(a);
            }
        }

        return ResponseEntity.ok(patientRecordService.createPatientRecord(userTemplate, allergyEntities));
    }

}