package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MedicationRequest {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("patient_id")
    private Long patientId;

    @JsonProperty("doctor_id")
    private Long doctorId;

    @JsonProperty("drug_name")
    private String drugName;

    @JsonProperty("dose")
    private String dose;

    @JsonProperty("frequency")
    private String frequency;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("route")
    private String route;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("is_perscription")
    private Boolean isPerscription;

    @JsonProperty("override")
    private Boolean override; // request override if conflicts

    @JsonProperty("override_justification")
    private String overrideJustification; // reason provided by doctor
}

