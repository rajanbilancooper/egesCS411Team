package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.allergyTests;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.services.AllergyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Delete Allergy
 * Tests AllergyService.deleteAllergy() method for all branches:
 * - Successful delete (allergy found, belongs to patient)
 * - Allergy not found (orElseThrow)
 * - PatientId is null
 * - PatientId doesn't match (wrong patient)
 */
@ExtendWith(MockitoExtension.class)
class deleteAllergyTest {

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private AllergyService allergyService;

    @Test
    void deleteAllergy_validAllergyBelongsToPatient_successfullyDeletes() {
        // Arrange - Delete allergy that belongs to patient
        Long patientId = 123L;
        Long allergyId = 1L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Peanuts");
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));

        // Act - Delete the allergy
        allergyService.deleteAllergy(patientId, allergyId);

        // Assert - Verify delete was called
        verify(allergyRepository, times(1)).findById(allergyId);
        verify(allergyRepository, times(1)).delete(existingAllergy);
    }

    @Test
    void deleteAllergy_allergyNotFound_throwsNotFoundException() {
        // Arrange - Test allergy ID doesn't exist (orElseThrow branch)
        Long patientId = 123L;
        Long nonExistentAllergyId = 999L;

        when(allergyRepository.findById(nonExistentAllergyId)).thenReturn(Optional.empty());

        // Act & Assert - Expect ResponseStatusException
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                allergyService.deleteAllergy(patientId, nonExistentAllergyId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Allergy not found"));

        verify(allergyRepository, times(1)).findById(nonExistentAllergyId);
        verify(allergyRepository, never()).delete(any(Allergy.class));
    }

    @Test
    void deleteAllergy_allergyPatientIdIsNull_throwsBadRequestException() {
        // Arrange - Allergy's patientId is null (edge case)
        Long patientId = 123L;
        Long allergyId = 2L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(null);  // PatientId is null
        existingAllergy.setSubstance("Shellfish");
        existingAllergy.setSeverity("MEDIUM");
        existingAllergy.setReaction("Hives");

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));

        // Act & Assert - Expect BAD_REQUEST because patientId is null
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                allergyService.deleteAllergy(patientId, allergyId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("does not belong to the specified patient"));

        verify(allergyRepository, times(1)).findById(allergyId);
        verify(allergyRepository, never()).delete(any(Allergy.class));
    }

    @Test
    void deleteAllergy_allergyBelongsToDifferentPatient_throwsBadRequestException() {
        // Arrange - Allergy belongs to different patient
        Long requestingPatientId = 123L;
        Long actualAllergyPatientId = 456L;  // Different patient
        Long allergyId = 3L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(actualAllergyPatientId);  // Belongs to different patient
        existingAllergy.setSubstance("Dairy");
        existingAllergy.setSeverity("LOW");
        existingAllergy.setReaction("Sneezing");

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));

        // Act & Assert - Expect BAD_REQUEST because wrong patient
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                allergyService.deleteAllergy(requestingPatientId, allergyId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("does not belong to the specified patient"));

        verify(allergyRepository, times(1)).findById(allergyId);
        verify(allergyRepository, never()).delete(any(Allergy.class));
    }

    @Test
    void deleteAllergy_multiplePatients_deletesOnlyCorrectAllergy() {
        // Arrange - Verify correct allergy is deleted even with multiple patients
        Long patientId1 = 123L;
        Long patientId2 = 456L;
        Long allergyIdForPatient1 = 1L;

        Allergy allergyForPatient1 = new Allergy();
        allergyForPatient1.setId(allergyIdForPatient1);
        allergyForPatient1.setPatientId(patientId1);
        allergyForPatient1.setSubstance("Peanuts");
        allergyForPatient1.setSeverity("HIGH");
        allergyForPatient1.setReaction("Anaphylaxis");

        when(allergyRepository.findById(allergyIdForPatient1)).thenReturn(Optional.of(allergyForPatient1));

        // Act - Patient 1 deletes their allergy
        allergyService.deleteAllergy(patientId1, allergyIdForPatient1);

        // Assert - Verify correct allergy was deleted
        verify(allergyRepository, times(1)).delete(allergyForPatient1);
    }

    @Test
    void deleteAllergy_patientIdMatchesExactly_successfullyDeletes() {
        // Arrange - Verify patientId exact match is required
        Long patientId = 789L;
        Long allergyId = 10L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Latex");
        existingAllergy.setSeverity("MEDIUM");
        existingAllergy.setReaction("Skin irritation");

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));

        // Act - Delete with exact patientId match
        allergyService.deleteAllergy(patientId, allergyId);

        // Assert - Verify delete was called
        verify(allergyRepository, times(1)).delete(existingAllergy);
    }
}
