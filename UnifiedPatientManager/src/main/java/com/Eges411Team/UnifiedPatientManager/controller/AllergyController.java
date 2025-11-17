package com.Eges411Team.UnifiedPatientManager.controller;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.AllergyRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.AllergyResponse;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.DTOs.mappers.AllergyMapper;
import com.Eges411Team.UnifiedPatientManager.services.AllergyService;

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
public class AllergyController {

    private final AllergyService allergyService;

    public AllergyController(AllergyService allergyService) {
        this.allergyService = allergyService;
    }

    @GetMapping("/{patient_id}/allergies")
    @Operation(
        summary = "Get a patient's allergy list",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of allergies for the given patient",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "AllergyList",
                            summary = "Retrieves a patient's allergy list.",
                            description = "Retrieves a patient's allergy list.",
                            // same JSON structure, now coming from DTO instead of entity
                            value = "[{\"id\":3,\"patient_id\":3,\"reaction\":\"GIintolerance\",\"severity\":\"HighRisk\",\"substance\":\"Pollen\"}]"
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<AllergyResponse>> find(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId
    ) {
        List<Allergy> allergies = allergyService.getAllergiesByPatientId(patientId);

        List<AllergyResponse> response = allergies.stream()
            .map(AllergyMapper::toResponseDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{patient_id}/allergies")
    @Operation(summary = "Create or replace a patient's allergy list")
    public ResponseEntity<List<AllergyResponse>> createOrUpdate(
        @RequestBody List<AllergyRequest> allergyList,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId
    ) {
        // convert request DTOs -> entity list
        List<Allergy> entities = allergyList.stream()
            .map(AllergyMapper::toEntity)
            .collect(Collectors.toList());

        // service still works with entities
        List<Allergy> saved = allergyService.saveAllergies(patientId, entities);

        // convert back to response DTOs
        List<AllergyResponse> response = saved.stream()
            .map(AllergyMapper::toResponseDto)
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{patient_id}/allergies/{allergy_id}")
    @Operation(summary = "Update a specific allergy for a patient")
    public ResponseEntity<AllergyResponse> update(
        @RequestBody AllergyRequest allergyDto,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId,
        @PathVariable("allergy_id")
        @Parameter(example = "1")
        Long allergyId
    ) {
        // map DTO -> entity with updated fields
        Allergy updatedEntity = AllergyMapper.toEntity(allergyDto);

        Allergy updated = allergyService.updateAllergy(patientId, allergyId, updatedEntity);

        AllergyResponse response = AllergyMapper.toResponseDto(updated);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{patient_id}/allergies/refresh")
    @Operation(summary = "Refresh a patient's allergy list")
    public ResponseEntity<List<AllergyResponse>> refresh(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId
    ) {
        List<Allergy> refreshed = allergyService.refreshAllergies(patientId);

        List<AllergyResponse> response = refreshed.stream()
            .map(AllergyMapper::toResponseDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{patient_id}/allergies/{allergy_id}")
    @Operation(summary = "Delete a specific allergy for a patient")
    public ResponseEntity<HttpStatus> delete(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId,
        @PathVariable("allergy_id")
        @Parameter(example = "1")
        Long allergyId
    ) {
        allergyService.deleteAllergy(patientId, allergyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
