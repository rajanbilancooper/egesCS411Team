package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.medicationConflictTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PrescriptionResultResponse;
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
class prescriptionDuplicateTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private MedicationService medicationService;

    @Test
    void createSinglePrescription_duplicateDetected_doesNotSave() {
        Long patientId = 4L;
        Long providerId = 12L;

        // Existing prescription: Aspirin 80 mg
        Medication existing = new Medication();
        existing.setPatientId(patientId);
        existing.setDrugName("Aspirin");
        existing.setDose("80 mg");
        existing.setFrequency("BID");
        existing.setDuration("2 weeks");
        existing.setRoute("oral");
        existing.setIsPerscription(true);

        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(List.of(existing));

        // Attempt to add Aspirin 20 mg
        Medication med = new Medication();
        med.setDrugName("Aspirin");
        med.setDose("20 mg");
        med.setFrequency("BID");
        med.setDuration("1 week");
        med.setRoute("oral");
        med.setIsPerscription(true);

        PrescriptionResultResponse result = medicationService.createSinglePrescription(patientId, providerId, med, false, null);

        assertNotNull(result);
        assertTrue(result.isConflicts());
        assertNotNull(result.getConflictMessages());
        assertTrue(result.getConflictMessages().stream().anyMatch(m -> m.toLowerCase().contains("duplicate") && m.toLowerCase().contains("aspirin")));
        assertNull(result.getPrescription());

        verify(medicationRepository, never()).save(any(Medication.class));
    }
}
