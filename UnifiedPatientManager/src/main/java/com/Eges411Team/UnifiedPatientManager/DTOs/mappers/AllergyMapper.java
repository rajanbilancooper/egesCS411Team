package com.Eges411Team.UnifiedPatientManager.DTOs.mappers;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.AllergyRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.AllergyResponse;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;

public class AllergyMapper {

    // Convert Request DTO -> Entity
    public static Allergy toEntity(AllergyRequest dto) {
        if (dto == null) return null;

        Allergy allergy = new Allergy();

        // Only set id/patient_id if included in request
        if (dto.getId() != null) {
            allergy.setId(dto.getId());
        }
        if (dto.getPatientId() != null) {
            allergy.setPatient_id(dto.getPatientId());
        }

        allergy.setReaction(dto.getReaction());
        allergy.setSeverity(dto.getSeverity());
        allergy.setSubstance(dto.getSubstance());

        return allergy;
    }

    // Convert Entity -> Response DTO
    public static AllergyResponse toResponseDto(Allergy entity) {
        if (entity == null) return null;

        AllergyResponse dto = new AllergyResponse();

        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatient_id());
        dto.setReaction(entity.getReaction());
        dto.setSeverity(entity.getSeverity());
        dto.setSubstance(entity.getSubstance());

        return dto;
    }
}

