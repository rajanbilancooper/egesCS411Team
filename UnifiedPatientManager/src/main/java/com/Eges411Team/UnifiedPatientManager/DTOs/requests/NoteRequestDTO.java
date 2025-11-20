package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.sql.Blob;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating a patient's note")
public class NoteRequestDTO {

    // @NotNull(message = "Patient ID is required")
    // @Schema(example = "3", description = "ID of the patient associated with this note")
    // private Long patient_id;

    @NotNull(message = "Doctor ID is required")
    @Schema(example = "5", description = "ID of the doctor who wrote the note")
    private Long doctor_id;

    @NotBlank(message = "Note type cannot be blank")
    @Schema(example = "Progress", description = "Type of note (e.g., Progress, SOAP, Summary)")
    private Blob note_type;

    @NotBlank(message = "Note content cannot be blank")
    @Schema(example = "Patient reports improved mobility and reduced pain.", description = "Main content or body of the note")
    private Blob content;

    @Schema(example = "2025-11-12T14:30:00", description = "Timestamp when the note was created or updated")
    private LocalDateTime timestamp;
}