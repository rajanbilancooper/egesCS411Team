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
 * Test Case: Update Allergy
 * Tests AllergyService.updateAllergy() method for all branches:
 * - Successful update (allergy found, belongs to patient)
 * - Allergy not found (orElseThrow)
 * - PatientId is null
 * - PatientId doesn't match (wrong patient)
 */
@ExtendWith(MockitoExtension.class)
class updateAllergyTest {

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private AllergyService allergyService;

    @Test
    void updateAllergy_validUpdateWithAllFields_successfullyUpdatesAllergy() {
        // Arrange - Update all fields of an allergy
        Long patientId = 123L;
        Long allergyId = 1L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Peanuts");
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        Allergy updatedAllergy = new Allergy();
        updatedAllergy.setSubstance("Tree Nuts");
        updatedAllergy.setSeverity("MEDIUM");
        updatedAllergy.setReaction("Swelling");

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(invocation -> {
            Allergy saved = invocation.getArgument(0);
            return saved;
        });

        // Act - Update the allergy
        Allergy result = allergyService.updateAllergy(patientId, allergyId, updatedAllergy);

        // Assert - Verify all fields were updated
        assertNotNull(result);
        assertEquals("Tree Nuts", result.getSubstance(), "Substance should be updated");
        assertEquals("MEDIUM", result.getSeverity(), "Severity should be updated");
        assertEquals("Swelling", result.getReaction(), "Reaction should be updated");
        assertEquals(patientId, result.getPatientId(), "PatientId should remain the same");

        verify(allergyRepository, times(1)).findById(allergyId);
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_updateSubstanceOnly_successfullyUpdates() {
        // Arrange - Test partial update (only substance)
        Long patientId = 123L;
        Long allergyId = 2L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Peanuts");
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        Allergy updatedAllergy = new Allergy();
        updatedAllergy.setSubstance("Shellfish");
        updatedAllergy.setSeverity("HIGH");  // Same
        updatedAllergy.setReaction("Anaphylaxis");  // Same

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Allergy result = allergyService.updateAllergy(patientId, allergyId, updatedAllergy);

        // Assert
        assertNotNull(result);
        assertEquals("Shellfish", result.getSubstance());
        assertEquals("HIGH", result.getSeverity());
        assertEquals("Anaphylaxis", result.getReaction());

        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_allergyNotFound_throwsNotFoundException() {
        // Arrange - Test allergy ID doesn't exist (orElseThrow branch)
        Long patientId = 123L;
        Long nonExistentAllergyId = 999L;

        Allergy updatedAllergy = new Allergy();
        updatedAllergy.setSubstance("Updated");
        updatedAllergy.setSeverity("LOW");
        updatedAllergy.setReaction("Mild");

        when(allergyRepository.findById(nonExistentAllergyId)).thenReturn(Optional.empty());

        // Act & Assert - Expect ResponseStatusException
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                allergyService.updateAllergy(patientId, nonExistentAllergyId, updatedAllergy)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Allergy not found"));

        verify(allergyRepository, times(1)).findById(nonExistentAllergyId);
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_allergyPatientIdIsNull_throwsBadRequestException() {
        // Arrange - Allergy's patientId is null (edge case)
        Long patientId = 123L;
        Long allergyId = 3L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(null);  // PatientId is null
        existingAllergy.setSubstance("Peanuts");
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        Allergy updatedAllergy = new Allergy();
        updatedAllergy.setSubstance("Updated");
        updatedAllergy.setSeverity("LOW");
        updatedAllergy.setReaction("Mild");

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));

        // Act & Assert - Expect BAD_REQUEST because patientId is null
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                allergyService.updateAllergy(patientId, allergyId, updatedAllergy)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("does not belong to the specified patient"));

        verify(allergyRepository, times(1)).findById(allergyId);
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_allergyBelongsToDifferentPatient_throwsBadRequestException() {
        // Arrange - Allergy belongs to different patient
        Long requestingPatientId = 123L;
        Long actualAllergyPatientId = 456L;  // Different patient
        Long allergyId = 4L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(actualAllergyPatientId);  // Belongs to different patient
        existingAllergy.setSubstance("Peanuts");
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        Allergy updatedAllergy = new Allergy();
        updatedAllergy.setSubstance("Updated");
        updatedAllergy.setSeverity("LOW");
        updatedAllergy.setReaction("Mild");

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));

        // Act & Assert - Expect BAD_REQUEST because wrong patient
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                allergyService.updateAllergy(requestingPatientId, allergyId, updatedAllergy)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("does not belong to the specified patient"));

        verify(allergyRepository, times(1)).findById(allergyId);
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_updateSeverityOnly_successfullyUpdates() {
        // Arrange - Test updating only severity (branches: all setters called)
        Long patientId = 123L;
        Long allergyId = 5L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Penicillin");
        existingAllergy.setSeverity("LOW");
        existingAllergy.setReaction("Rash");

        Allergy updatedAllergy = new Allergy();
        updatedAllergy.setSubstance("Penicillin");  // Same
        updatedAllergy.setSeverity("HIGH");  // Updated
        updatedAllergy.setReaction("Rash");  // Same

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Allergy result = allergyService.updateAllergy(patientId, allergyId, updatedAllergy);

        // Assert
        assertNotNull(result);
        assertEquals("Penicillin", result.getSubstance());
        assertEquals("HIGH", result.getSeverity());
        assertEquals("Rash", result.getReaction());

        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_updateReactionOnly_successfullyUpdates() {
        // Arrange - Test updating only reaction
        Long patientId = 123L;
        Long allergyId = 6L;

        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(allergyId);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Dairy");
        existingAllergy.setSeverity("MEDIUM");
        existingAllergy.setReaction("Digestive upset");

        Allergy updatedAllergy = new Allergy();
        updatedAllergy.setSubstance("Dairy");  // Same
        updatedAllergy.setSeverity("MEDIUM");  // Same
        updatedAllergy.setReaction("Severe cramping");  // Updated

        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(existingAllergy));
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Allergy result = allergyService.updateAllergy(patientId, allergyId, updatedAllergy);

        // Assert
        assertNotNull(result);
        assertEquals("Dairy", result.getSubstance());
        assertEquals("MEDIUM", result.getSeverity());
        assertEquals("Severe cramping", result.getReaction());

        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }
}
