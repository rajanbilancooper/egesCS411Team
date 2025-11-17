package com.Eges411Team.UnifiedPatientManager.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllergyResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("patient_id")
    private Long patientId;

    @JsonProperty("reaction")
    private String reaction;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("substance")
    private String substance;
}
