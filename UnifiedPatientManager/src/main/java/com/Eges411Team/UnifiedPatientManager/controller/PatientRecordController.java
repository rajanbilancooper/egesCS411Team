package com.Eges411Team.UnifiedPatientManager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRecordUpdateDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.services.PatientRecordService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

// controller for patient record related endpoints
@RestController
@RequestMapping("/api/patients")
public class PatientRecordController {
    // dependency injection of the patientRecord Service
    @Autowired
    PatientRecordService patientRecordService;

    // need a method for getting the patientRecord
    @GetMapping("/{id}")
    public ResponseEntity<PatientRecordDTO> getPatientRecord(@PathVariable Long patientID) {
        
        // gets the record using the service using the User ID
        return ResponseEntity.ok(patientRecordService.getPatientRecord(patientID));
    }

    // method for updating a patientRecord
    @PutMapping("/{id}")
    public ResponseEntity<PatientRecordDTO> updatePatientRecord(@PathVariable Long patientID, @RequestBody PatientRecordUpdateDTO recordUpdateDTO) {

        // updates the record using the service using the ID and the DTO
        return patientRecordService.updatePatientRecord(patientID, recordUpdateDTO);
    }

    // method for getting a patient record by full name
    @GetMapping("/search")
    public ResponseEntity<PatientRecordDTO> getPatientRecordByName(@RequestParam String fullName) {
        
        // gets the record using the service using the full name
        return ResponseEntity.ok(patientRecordService.getPatientRecordByFullName(fullName));
    }

}