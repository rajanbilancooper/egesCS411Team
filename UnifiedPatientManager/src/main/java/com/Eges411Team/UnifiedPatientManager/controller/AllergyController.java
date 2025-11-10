package com.Eges411Team.UnifiedPatientManager.controller;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.services.AllergyService;
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
                            value = "[{\"id\":3,\"patient_id\":3,\"reaction\":\"GIintolerance\",\"severity\":\"HighRisk\",\"substance\":\"Pollen\"}]"
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<Allergy>> find(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId
    ) {
        List<Allergy> allergies = allergyService.getAllergiesByPatientId(patientId);
        return ResponseEntity.ok(allergies);
    }

    @PostMapping("/{patient_id}/allergies")
    @Operation(summary = "Create or replace a patient's allergy list")
    public ResponseEntity<List<Allergy>> createOrUpdate(
        @RequestBody List<Allergy> allergyList,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId
    ) {
        List<Allergy> saved = allergyService.saveAllergies(patientId, allergyList);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{patient_id}/allergies/{allergy_id}")
    @Operation(summary = "Update a specific allergy for a patient")
    public ResponseEntity<Allergy> update(
        @RequestBody Allergy allergy,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId,
        @PathVariable("allergy_id")
        @Parameter(example = "1")
        int allergyId
    ) {
        Allergy updated = allergyService.updateAllergy(patientId, allergyId, allergy);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{patient_id}/allergies/refresh")
    @Operation(summary = "Refresh a patient's allergy list")
    public ResponseEntity<List<Allergy>> refresh(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId
    ) {
        List<Allergy> refreshed = allergyService.refreshAllergies(patientId);
        return ResponseEntity.ok(refreshed);
    }

    @DeleteMapping("/{patient_id}/allergies/{allergy_id}")
    @Operation(summary = "Delete a specific allergy for a patient")
    public ResponseEntity<HttpStatus> delete(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        int patientId,
        @PathVariable("allergy_id")
        @Parameter(example = "1")
        int allergyId
    ) {
        allergyService.deleteAllergy(patientId, allergyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
