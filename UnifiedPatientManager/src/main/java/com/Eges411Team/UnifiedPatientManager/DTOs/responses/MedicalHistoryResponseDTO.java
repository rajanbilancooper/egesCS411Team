package com.Eges411Team.UnifiedPatientManager.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO representing a patient's medical history record")
public class MedicalHistoryResponseDTO {

    @Schema(example = "1", description = "Unique identifier for this medical history record")
    private Integer id;

    @Schema(example = "3", description = "ID of the patient associated with this medical record")
    private Integer patient_id;

    @Schema(example = "5", description = "ID of the doctor who authored this record")
    private Integer doctor_id;

    @Schema(example = "Asthma", description = "Primary diagnosis")
    private String diagnosis;

    @Schema(example = "Weekly", description = "Frequency of the diagnosis or checkups")
    private String frequency;

    @Schema(example = "2023-03-10", description = "Start date of the medical condition or treatment")
    private Date start_date;

    @Schema(example = "2024-02-15", description = "End date of the medical condition or treatment (if applicable)")
    private Date end_date;
}