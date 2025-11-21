package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.medicationConflictTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PrescriptionResultResponse;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
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
class prescriptionAllergyConflictTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private MedicationService medicationService;

    @Test
    void createSinglePrescription_allergyConflict_detectedAndNotSaved() {
        Long patientId = 3L;
        Long providerId = 10L;

        // Patient allergies: Penicillin, Amoxicillin
        Allergy a1 = new Allergy();
        a1.setPatientId(patientId);
        a1.setSubstance("Penicillin");
        Allergy a2 = new Allergy();
        a2.setPatientId(patientId);
        a2.setSubstance("Amoxicillin");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(List.of(a1, a2));
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of());

        Medication med = new Medication();
        med.setDrugName("Penicillin");
        med.setDose("500 mg");
        med.setFrequency("3x/day");
        med.setDuration("1 week");
        med.setRoute("oral");
        med.setIsPerscription(true);

        // Act - no override requested
        PrescriptionResultResponse result = medicationService.createSinglePrescription(patientId, providerId, med, false, null);

        // Assert - conflict detected, message present, and nothing saved
        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertNotNull(result.getConflictMessages());
        assertFalse(result.getConflictMessages().isEmpty());
        assertTrue(result.getConflictMessages().stream().anyMatch(msg -> msg.contains("Allergy conflict") && msg.toLowerCase().contains("penicillin")));
        assertNull(result.getPrescription());

        verify(medicationRepository, never()).save(any(Medication.class));
    }
}
