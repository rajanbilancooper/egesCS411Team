package com.Eges411Team.UnifiedPatientManager.DTOs.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for returning a patient's note")
public class NoteResponseDTO {

    @Schema(example = "1", description = "Unique identifier for the note record")
    private Long id;

    @Schema(example = "3", description = "ID of the patient associated with this note")
    private Long patientId;

    @Schema(example = "5", description = "ID of the doctor who authored the note")
    private Long doctorId;

    @Schema(example = "Progress", description = "Type of note (e.g., Progress, SOAP, Summary)")
    private String noteType;

    @Schema(example = "Patient reports improved mobility and reduced pain.", description = "Text content of the note")
    private String content;

    @Schema(example = "2025-11-12T14:30:00", description = "Time the note was created or last updated")
    private LocalDateTime timestamp;
}
