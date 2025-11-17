package com.Eges411Team.UnifiedPatientManager.DTOs.mappers;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.MedicationRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.MedicationResponse;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;

public class MedicationMapper {

    // DTO from UI -> Entity for service/repo
    public static Medication toEntity(MedicationRequest dto) {
        if (dto == null) return null;

        Medication med = new Medication();

        if (dto.getId() != null) {
            med.setId(dto.getId());
        }
        if (dto.getPatientId() != null) {
            med.setPatientId(dto.getPatientId());
        }
        if (dto.getDoctorId() != null) {
            med.setDoctorId(dto.getDoctorId());
        }

    med.setDrugName(dto.getDrugName());
        med.setDose(dto.getDose());
        med.setFrequency(dto.getFrequency());
        med.setDuration(dto.getDuration());
        med.setNotes(dto.getNotes());
        med.setTimestamp(dto.getTimestamp());
        med.setStatus(dto.getStatus());
    med.setIsPerscription(dto.getIsPerscription());

        return med;
    }

    // Entity from DB -> DTO for UI
    public static MedicationResponse toResponseDto(Medication entity) {
        if (entity == null) return null;

        MedicationResponse dto = new MedicationResponse();

        dto.setId(entity.getId());
    dto.setPatientId(entity.getPatientId());
    dto.setDoctorId(entity.getDoctorId());
    dto.setDrugName(entity.getDrugName());
        dto.setDose(entity.getDose());
        dto.setFrequency(entity.getFrequency());
        dto.setDuration(entity.getDuration());
        dto.setNotes(entity.getNotes());
        dto.setTimestamp(entity.getTimestamp());
        dto.setStatus(entity.getStatus());
    dto.setIsPerscription(entity.getIsPerscription());

        return dto;
    }
}

