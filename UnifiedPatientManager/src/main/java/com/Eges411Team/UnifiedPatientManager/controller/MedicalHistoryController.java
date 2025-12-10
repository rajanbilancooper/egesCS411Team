package com.Eges411Team.UnifiedPatientManager.controller;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.MedicalHistoryRequestDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.MedicalHistoryResponseDTO;
import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.services.MedicalHistoryService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/default/patient", "/api/patients"})
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    public MedicalHistoryController(MedicalHistoryService medicalHistoryService) {
        this.medicalHistoryService = medicalHistoryService;
    }

    // Get all medical history records for a patient
    @GetMapping("/{patient_id}/medicalhistory")
    @Operation(
        summary = "Get a patient's medical history list",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of medical history records for the given patient",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "MedicalHistoryList",
                            summary = "Retrieves a patient's medical history list.",
                            description = "Retrieves all medical history entries for a given patient.",
                            value = "[{\"id\":1,\"patient_id\":3,\"doctor_id\":5,\"diagnosis\":\"Asthma\",\"frequency\":\"Weekly\",\"start_date\":\"2022-10-01\",\"end_date\":\"2023-03-01\"}]"
                        )
                    }
                )
            )
        }
    )


    public ResponseEntity<List<MedicalHistory>> find(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId
    ) {

        List<MedicalHistory> records = medicalHistoryService.getMedicalHistoryByPatientId(patientId);
        return ResponseEntity.ok(records);
    }

    // Create or replace a patient's medical history records

    @PostMapping("/{patient_id}/medicalhistory")
    @Operation(summary = "Create a new medical history record for a patient")
    public ResponseEntity<MedicalHistoryResponseDTO> createMedicalHistory(
        @PathVariable("patient_id") 
        @Parameter(example = "3") 
        long patientId,
        @Valid @RequestBody MedicalHistoryRequestDTO MHrequestDTO) {

    // Create a new entity instance
    MedicalHistory medicalHistory = new MedicalHistory();

    // Set the entity fields from the request DTO
    medicalHistory.setPatientId(patientId); 
    medicalHistory.setDoctorId(MHrequestDTO.getDoctor_id());
    medicalHistory.setDiagnosis(MHrequestDTO.getDiagnosis());
    medicalHistory.setFrequency(MHrequestDTO.getFrequency());
    medicalHistory.setStartDate(MHrequestDTO.getStart_date());
    medicalHistory.setEndDate(MHrequestDTO.getEnd_date());
    // optional flag: whether to prompt for prescribing medication
    if (MHrequestDTO.getPrescribe_medication() != null) {
        medicalHistory.setPrescribeMedication(MHrequestDTO.getPrescribe_medication());
    } else {
        medicalHistory.setPrescribeMedication(false);
    }

    // Save the entity using your service
    MedicalHistory saved = medicalHistoryService.saveMedicalHistory(medicalHistory);

    // Convert the saved entity to a Response DTO
    MedicalHistoryResponseDTO responseDTO = new MedicalHistoryResponseDTO();
    responseDTO.setId(saved.getId() == null ? null : saved.getId().intValue());
    responseDTO.setPatient_id(saved.getPatientId() == null ? null : saved.getPatientId().intValue());
    responseDTO.setDoctor_id(saved.getDoctorId() == null ? null : saved.getDoctorId().intValue());
    responseDTO.setDiagnosis(saved.getDiagnosis());
    responseDTO.setFrequency(saved.getFrequency());
    responseDTO.setStart_date(saved.getStartDate());
    responseDTO.setEnd_date(saved.getEndDate());
    responseDTO.setPrescribe_medication(saved.getPrescribeMedication());

    // Return a created response with the saved record
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
}
    // Update a specific medical history record 
    @PutMapping("/{patient_id}/medicalhistory/{history_id}")
    @Operation(summary = "Update a specific medical history record for a patient")
    public ResponseEntity<MedicalHistory> update(
        @Valid @RequestBody MedicalHistory medicalHistory,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId,
        @PathVariable("history_id")
        @Parameter(example = "1")
        Long historyId
    ) {
        MedicalHistory updated = medicalHistoryService.updateMedicalHistory(patientId, historyId, medicalHistory);
        return ResponseEntity.ok(updated);
    }

    // Delete a specific medical history record
    @DeleteMapping("/{patient_id}/medicalhistory/{history_id}")
    @Operation(summary = "Delete a specific medical history record for a patient")
    public ResponseEntity<HttpStatus> delete(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId,
        @PathVariable("history_id")
        @Parameter(example = "1")
        Long historyId
    ) {
        medicalHistoryService.deleteMedicalHistory(patientId, historyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
