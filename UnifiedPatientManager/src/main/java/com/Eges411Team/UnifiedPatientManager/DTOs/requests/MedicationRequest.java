package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MedicationRequest {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("patient_id")
    private Integer patientId;

    @JsonProperty("doctor_id")
    private Integer doctorId;

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

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("is_perscription")
    private Boolean isPerscription;
}

