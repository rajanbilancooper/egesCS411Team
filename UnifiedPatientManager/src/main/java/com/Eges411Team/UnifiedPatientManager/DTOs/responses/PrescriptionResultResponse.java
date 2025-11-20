package com.Eges411Team.UnifiedPatientManager.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrescriptionResultResponse {

    @JsonProperty("conflicts")
    private boolean conflicts; // true if any conflicts detected

    @JsonProperty("conflict_messages")
    private List<String> conflictMessages; // detailed conflict messages

    @JsonProperty("prescription")
    private MedicationResponse prescription; // populated only if saved (including override case)

    public PrescriptionResultResponse() {}

    public PrescriptionResultResponse(boolean conflicts, List<String> conflictMessages, MedicationResponse prescription) {
        this.conflicts = conflicts;
        this.conflictMessages = conflictMessages;
        this.prescription = prescription;
    }
}
