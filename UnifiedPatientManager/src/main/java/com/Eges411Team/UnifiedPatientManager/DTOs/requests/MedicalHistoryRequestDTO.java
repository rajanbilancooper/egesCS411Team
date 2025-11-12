package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating a patient's medical history record")
public class MedicalHistoryRequestDTO {

    @NotNull(message = "Patient ID is required")
    @Schema(example = "3", description = "ID of the patient associated with this medical record")
    private Integer patient_id;

    @NotNull(message = "Doctor ID is required")
    @Schema(example = "5", description = "ID of the doctor who created or managed this record")
    private Integer doctor_id;

    @NotBlank(message = "Diagnosis cannot be blank")
    @Schema(example = "Asthma", description = "Primary diagnosis for this medical record")
    private String diagnosis;

    @NotBlank(message = "Frequency cannot be blank")
    @Schema(example = "Weekly", description = "Frequency of the diagnosis or treatment")
    private String frequency;

    @Schema(example = "2023-03-10", description = "Date when the diagnosis or treatment began")
    private Date start_date;

    @Schema(example = "2024-02-15", description = "Date when the diagnosis or treatment ended (if applicable)")
    private Date end_date;
}