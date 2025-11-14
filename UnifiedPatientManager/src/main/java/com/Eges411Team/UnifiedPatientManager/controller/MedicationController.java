package com.Eges411Team.UnifiedPatientManager.controller;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.MedicationRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.MedicationResponse;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.DTOs.mappers.MedicationMapper;
import com.Eges411Team.UnifiedPatientManager.services.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/default/patient")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping("/{patient_id}/medications")
    @Operation(
        summary = "Get a patient's medication list",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of medications for the given patient",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "MedicationList",
                            summary = "Retrieves a patient's medication list.",
                            description = "Sample medication list for a patient.",
                            value = "[" +
                                "{\"id\":1,\"doctor_id\":10,\"patient_id\":3,\"drug_name\":\"Amoxicillin\",\"dose\":\"500mg\",\"frequency\":\"BID\",\"duration\":\"7 days\",\"notes\":\"Take with food\",\"status\":true,\"is_perscription\":true}" +
                                "]"
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<MedicationResponse>> find(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId
    ) {
        List<Medication> medications = medicationService.getMedicationsByPatientId(patientId);

        List<MedicationResponse> response = medications.stream()
            .map(MedicationMapper::toResponseDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{patient_id}/providers/{provider_id}/medications")
    @Operation(summary = "Create or replace a patient's medication list for a given provider")
    public ResponseEntity<List<MedicationResponse>> createOrUpdate(
        @RequestBody List<MedicationRequest> medicationList,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId,
        @PathVariable("provider_id")
        @Parameter(example = "10")
        int providerId
    ) {
        // DTOs from UI -> entities
        List<Medication> entities = medicationList.stream()
            .map(MedicationMapper::toEntity)
            .collect(Collectors.toList());

        // service still works with entities
        List<Medication> saved = medicationService.saveMedications(patientId, providerId, entities);

        // entities -> response DTOs
        List<MedicationResponse> response = saved.stream()
            .map(MedicationMapper::toResponseDto)
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{patient_id}/providers/{provider_id}/medications/{medication_id}")
    @Operation(summary = "Update a specific medication for a patient and provider")
    public ResponseEntity<MedicationResponse> update(
        @RequestBody MedicationRequest medicationDto,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId,
        @PathVariable("provider_id")
        @Parameter(example = "10")
        int providerId,
        @PathVariable("medication_id")
        @Parameter(example = "1")
        int medicationId
    ) {
        Medication updatedEntity = MedicationMapper.toEntity(medicationDto);

        Medication updated = medicationService.updateMedication(
            patientId,
            providerId,
            medicationId,
            updatedEntity
        );

        MedicationResponse response = MedicationMapper.toResponseDto(updated);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{patient_id}/medications/refresh")
    @Operation(summary = "Refresh a patient's medication list")
    public ResponseEntity<List<MedicationResponse>> refresh(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId
    ) {
        List<Medication> refreshed = medicationService.refreshMedications(patientId);

        List<MedicationResponse> response = refreshed.stream()
            .map(MedicationMapper::toResponseDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{patient_id}/medications/{medication_id}")
    @Operation(summary = "Delete a specific medication for a patient")
    public ResponseEntity<HttpStatus> delete(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId,
        @PathVariable("medication_id")
        @Parameter(example = "1")
        int medicationId
    ) {
        medicationService.deleteMedication(patientId, medicationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
