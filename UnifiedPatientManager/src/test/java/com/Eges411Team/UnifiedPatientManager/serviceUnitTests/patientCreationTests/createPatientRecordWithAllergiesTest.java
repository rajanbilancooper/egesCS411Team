package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientCreationTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.PatientRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Create patient record with allergies
 * Use Case: Create new patient record with allergy data
 * Requirement: 3.1.1 - The system shall allow creating patients with allergies
 * Technique: Equivalence Partitioning - Testing allergy list variations
 */
@ExtendWith(MockitoExtension.class)
class createPatientRecordWithAllergiesTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private PatientRecordService patientRecordService;

    @Test
    void createPatientRecord_withNullAllergies_createsPatientWithNoAllergies() {
        // Arrange
        User user = createValidUser("johndoe", "John", "Doe");
        
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth(anyString(), anyString(), any()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail(anyString())).thenReturn(new ArrayList<>());
        
        User savedUser = createValidUser("johndoe", "John", "Doe");
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act - Pass null for allergies list
        PatientRecordDTO result = patientRecordService.createPatientRecord(user, null);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertNotNull(result.getAllergies());
        assertTrue(result.getAllergies().isEmpty());
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void createPatientRecord_withEmptyAllergiesList_createsPatientWithNoAllergies() {
        // Arrange
        User user = createValidUser("janedoe", "Jane", "Doe");
        
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth(anyString(), anyString(), any()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail(anyString())).thenReturn(new ArrayList<>());
        
        User savedUser = createValidUser("janedoe", "Jane", "Doe");
        savedUser.setId(2L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act - Pass empty list for allergies
        PatientRecordDTO result = patientRecordService.createPatientRecord(user, new ArrayList<>());

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getPatientId());
        assertNotNull(result.getAllergies());
        assertTrue(result.getAllergies().isEmpty());
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void createPatientRecord_withAllergiesList_createsPatientWithAllergies() {
        // Arrange
        User user = createValidUser("bobsmith", "Bob", "Smith");
        
        Allergy peanutAllergy = new Allergy();
        peanutAllergy.setSubstance("Peanuts");
        peanutAllergy.setReaction("Anaphylaxis");
        peanutAllergy.setSeverity("High");
        
        Allergy penicillinAllergy = new Allergy();
        penicillinAllergy.setSubstance("Penicillin");
        penicillinAllergy.setReaction("Rash");
        penicillinAllergy.setSeverity("Medium");
        
        List<Allergy> allergies = Arrays.asList(peanutAllergy, penicillinAllergy);
        
        when(userRepository.existsByUsername("bobsmith")).thenReturn(false);
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth(anyString(), anyString(), any()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail(anyString())).thenReturn(new ArrayList<>());
        
        User savedUser = createValidUser("bobsmith", "Bob", "Smith");
        savedUser.setId(3L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Mock allergy saves
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(invocation -> {
            Allergy saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId((long) (Math.random() * 1000));
            }
            return saved;
        });

        // Act
        PatientRecordDTO result = patientRecordService.createPatientRecord(user, allergies);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getPatientId());
        assertNotNull(result.getAllergies());
        assertEquals(2, result.getAllergies().size());
        
        // Verify allergies were saved with patient ID
        verify(allergyRepository, times(2)).save(argThat(allergy -> 
            allergy.getPatientId().equals(3L)
        ));
        
        // Verify allergy DTOs contain correct data
        PatientRecordDTO.AllergyDTO allergyDTO1 = result.getAllergies().get(0);
        assertEquals("Peanuts", allergyDTO1.getSubstance());
        assertEquals("Anaphylaxis", allergyDTO1.getReaction());
        assertEquals("High", allergyDTO1.getSeverity());
        
        PatientRecordDTO.AllergyDTO allergyDTO2 = result.getAllergies().get(1);
        assertEquals("Penicillin", allergyDTO2.getSubstance());
        assertEquals("Rash", allergyDTO2.getReaction());
        assertEquals("Medium", allergyDTO2.getSeverity());
    }

    @Test
    void createPatientRecord_withSingleAllergy_createsPatientCorrectly() {
        // Arrange
        User user = createValidUser("alicejones", "Alice", "Jones");
        
        Allergy latexAllergy = new Allergy();
        latexAllergy.setSubstance("Latex");
        latexAllergy.setReaction("Skin irritation");
        latexAllergy.setSeverity("Low");
        
        List<Allergy> allergies = Arrays.asList(latexAllergy);
        
        when(userRepository.existsByUsername("alicejones")).thenReturn(false);
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth(anyString(), anyString(), any()))
            .thenReturn(Optional.empty());
        when(userRepository.findAllByEmail(anyString())).thenReturn(new ArrayList<>());
        
        User savedUser = createValidUser("alicejones", "Alice", "Jones");
        savedUser.setId(4L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(invocation -> {
            Allergy saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        // Act
        PatientRecordDTO result = patientRecordService.createPatientRecord(user, allergies);

        // Assert
        assertNotNull(result);
        assertEquals(4L, result.getPatientId());
        assertEquals(1, result.getAllergies().size());
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    // Helper method to create a valid user
    private User createValidUser(String username, String firstName, String lastName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(username + "@email.com");
        user.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));
        user.setPhoneNumber("1234567890");
        return user;
    }
}
