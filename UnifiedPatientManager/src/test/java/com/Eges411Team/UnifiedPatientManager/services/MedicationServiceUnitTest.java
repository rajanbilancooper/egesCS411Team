package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Medication;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceUnitTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private MedicationService medicationService;

    private Medication existing;

    @BeforeEach
    void setUp() {
        existing = new Medication();
        existing.setId(10L);
        existing.setPatientId(1L);
        existing.setDoctorId(77L);
        existing.setDrugName("OldName");
        existing.setIsPerscription(true);
        existing.setTimestamp(LocalDateTime.now());
    }

    @Test
    void updateMedication_noConflict_succeeds() {
        // Given patient has a Penicillin allergy; updating Ibuprofen (not in Penicillin conflict list)
        Allergy penicillinAllergy = new Allergy();
        penicillinAllergy.setId(200L);
        penicillinAllergy.setPatientId(1L);
        penicillinAllergy.setSubstance("Penicillin");

        Medication updated = new Medication();
        updated.setId(10L); // same id passed in
        updated.setDrugName("Ibuprofen");
        updated.setIsPerscription(true);
        updated.setDose("200mg");
        updated.setFrequency("BID");
        updated.setDuration("5d");

        when(medicationRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(List.of(penicillinAllergy));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> (Medication) inv.getArgument(0));

        // When
        Medication result = medicationService.updateMedication(1L, 99L, 10L, updated);

        // Then
        assertThat(result.getDrugName()).isEqualTo("Ibuprofen");
        assertThat(result.getDoctorId()).isEqualTo(99L);

        ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
        verify(medicationRepository).save(captor.capture());
        Medication saved = captor.getValue();
        assertThat(saved.getDrugName()).isEqualTo("Ibuprofen");
        verify(allergyRepository).findAllByPatientId(1L);
    }

    @Test
    void updateMedication_conflictWithAllergy_throws() {
        // Given Penicillin allergy; prescribing Amoxicillin should conflict
        Allergy penicillinAllergy = new Allergy();
        penicillinAllergy.setId(201L);
        penicillinAllergy.setPatientId(1L);
        penicillinAllergy.setSubstance("Penicillin");

        Medication updated = new Medication();
        updated.setId(10L);
        updated.setDrugName("Amoxicillin");
        updated.setIsPerscription(true);

        when(medicationRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(List.of(penicillinAllergy));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            medicationService.updateMedication(1L, 99L, 10L, updated)
        );

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        assertThat(ex.getReason()).contains("conflicts with allergy");
        // Intentionally not verifying save() to avoid nullness warning; absence of save is implicit since exception thrown.
    }

    @Test
    void updateMedication_notFound_throws() {
        when(medicationRepository.findById(10L)).thenReturn(Optional.empty());

        Medication updated = new Medication();
        updated.setId(10L);
        updated.setDrugName("Ibuprofen");
        updated.setIsPerscription(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            medicationService.updateMedication(1L, 99L, 10L, updated)
        );

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        verify(allergyRepository, never()).findAllByPatientId(any());
    }

    @Test
    void getMedicationsByPatientId_delegatesToRepository() {
        Medication m = new Medication();
        m.setId(300L);
        m.setPatientId(5L);
        m.setDrugName("TestDrug");

        when(medicationRepository.findAllByPatientId(5L)).thenReturn(List.of(m));

        List<Medication> result = medicationService.getMedicationsByPatientId(5L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDrugName()).isEqualTo("TestDrug");
        verify(medicationRepository).findAllByPatientId(5L);
    }
}
