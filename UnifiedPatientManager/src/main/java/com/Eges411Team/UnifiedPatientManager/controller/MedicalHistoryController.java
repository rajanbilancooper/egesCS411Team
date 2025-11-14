package com.Eges411Team.UnifiedPatientManager.controller;

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

@RestController
@RequestMapping("/default/patient")
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    public MedicalHistoryController(MedicalHistoryService medicalHistoryService) {
        this.medicalHistoryService = medicalHistoryService;
    }

    // 🩺 Get all medical history records for a patient
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
    @Operation(summary = "Create or replace a patient's medical history records")
    public ResponseEntity<List<MedicalHistory>> createOrUpdate(
        @RequestBody List<MedicalHistory> historyList,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId
    ) {
        List<MedicalHistory> saved = medicalHistoryService.saveMedicalHistory(patientId, historyList);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update a specific medical history record
    @PutMapping("/{patient_id}/medicalhistory/{history_id}")
    @Operation(summary = "Update a specific medical history record for a patient")
    public ResponseEntity<MedicalHistory> update(
        @RequestBody MedicalHistory medicalHistory,
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

    // Refresh a patient’s medical history list
    @GetMapping("/{patient_id}/medicalhistory/refresh")
    @Operation(summary = "Refresh a patient's medical history list")
    public ResponseEntity<List<MedicalHistory>> refresh(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId
    ) {
        List<MedicalHistory> refreshed = medicalHistoryService.refreshMedicalHistory(patientId);
        return ResponseEntity.ok(refreshed);
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