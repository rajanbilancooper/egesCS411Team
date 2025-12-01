package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private Long doctorId;

    @NotNull(message = "Note type is required")
    @Schema(example = "TEXT", description = "Type of note: TEXT or FILE")
    private com.Eges411Team.UnifiedPatientManager.entity.NoteType noteType;

    @Schema(example = "Patient reports improved mobility and reduced pain.", description = "Main content or body of the note. Required for TEXT notes; ignored for FILE notes.")
    private String content;

    @Schema(example = "2025-11-12T14:30:00", description = "Timestamp when the note was created or updated")
    private LocalDateTime timestamp;

    @Schema(example = "lab-results.pdf", description = "Optional custom attachment file name to override the uploaded file's original name for FILE notes.")
    private String attachmentName;
}