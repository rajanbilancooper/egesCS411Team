package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.medicationConflictTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PrescriptionResultResponse;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.services.MedicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class prescriptionInteractionConflictTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private MedicationService medicationService;

    @Test
    void createSinglePrescription_interactionConflict_detectedAndNotSaved() {
        Long patientId = 2L;
        Long providerId = 11L;

        // Patient currently has Warfarin prescribed
        Medication existing = new Medication();
        existing.setPatientId(patientId);
        existing.setDrugName("Warfarin");
        existing.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of(existing));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of());

        Medication med = new Medication();
        med.setDrugName("Aspirin");
        med.setDose("80 mg");
        med.setFrequency("BID");
        med.setDuration("2 weeks");
        med.setRoute("oral");
        med.setIsPerscription(true);

        // Act - no override requested
        PrescriptionResultResponse result = medicationService.createSinglePrescription(patientId, providerId, med, false, null);

        // Assert - conflict detected for interaction, message present, and nothing saved
        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertNotNull(result.getConflictMessages());
        assertFalse(result.getConflictMessages().isEmpty());
        assertTrue(result.getConflictMessages().stream().anyMatch(msg -> msg.toLowerCase().contains("interaction") && msg.toLowerCase().contains("warfarin") && msg.toLowerCase().contains("aspirin")));
        assertNull(result.getPrescription());

        verify(medicationRepository, never()).save(any(Medication.class));
    }
}
