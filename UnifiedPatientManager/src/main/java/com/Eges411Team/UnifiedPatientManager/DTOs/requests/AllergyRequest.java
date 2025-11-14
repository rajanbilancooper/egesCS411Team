package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllergyRequest {

    // optional: let client send id if needed (e.g. for updates)
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("patient_id")
    private Integer patientId;

    @JsonProperty("reaction")
    private String reaction;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("substance")
    private String substance;
}