package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientCreationTests;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Role;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: No Duplicate Email Addresses
 * Use Case: Storing Detailed Patient Information
 * Requirement: 3.1.1 - The system shall store patient information with unique email
 * Technique: Equivalence Partitioning – Testing invalid input class for email that already exists
 */
@ExtendWith(MockitoExtension.class)
class noDuplicateEmailTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AllergyRepository allergyRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private MedicalHistoryRepo medicalHistoryRepo;

    @InjectMocks
    private PatientRecordService patientRecordService;

    @Test
    void createPatientRecord_duplicateEmail_throwsExceptionAndDoesNotSave() {
        // Arrange - Pre-condition: A user already exists with the email "shared@gmail.com"
        // Mock repository to return that the email already exists
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("shared@gmail.com");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        when(userRepository.findAllByEmail("shared@gmail.com")).thenReturn(new ArrayList<>(Collections.singletonList(existingUser)));

        // Build a new User with the duplicate email as per test case input
        User newUser = new User();
        newUser.setRole(Role.PATIENT);
        newUser.setFirstName("Jane");
        newUser.setLastName("Smith");
        newUser.setDateOfBirth(LocalDateTime.of(1992, 3, 20, 0, 0));
        newUser.setGender("FEMALE");
        newUser.setEmail("shared@gmail.com"); // duplicate email
        newUser.setUsername("janesmith");
        newUser.setPassword("JanePassword123");
        newUser.setHeight("5'5\"");
        newUser.setWeight("120 lbs");
        newUser.setAddress("456 Oak Ave, Boston, MA 02116");
        newUser.setPhoneNumber("617-333-3333");

        // Act & Assert - expect RuntimeException with message about duplicate email
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.createPatientRecord(newUser, Collections.emptyList())
        );

        assertTrue(ex.getMessage().contains("already exists") && ex.getMessage().contains("email"),
                "Expected error message to contain 'already exists' and 'email' but got: " + ex.getMessage());

        // Verify repository.save was never called due to validation failure
        verify(userRepository, never()).save(any(User.class));

        // Ensure input fields remain unchanged (user is prompted to use different email without losing information)
        assertEquals("Jane", newUser.getFirstName());
        assertEquals("Smith", newUser.getLastName());
        assertEquals("shared@gmail.com", newUser.getEmail());
        assertEquals("janesmith", newUser.getUsername());
        assertEquals("617-333-3333", newUser.getPhoneNumber());
        assertEquals(Role.PATIENT, newUser.getRole());
    }

    @Test
    void createPatientRecord_duplicateEmailCaseInsensitive_throwsExceptionAndDoesNotSave() {
        // Arrange - Email comparison should be case-insensitive
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("Test@Gmail.com");

        when(userRepository.findAllByEmail("test@gmail.com")).thenReturn(new ArrayList<>(Collections.singletonList(existingUser)));

        User newUser = new User();
        newUser.setRole(Role.PATIENT);
        newUser.setFirstName("Bob");
        newUser.setLastName("Jones");
        newUser.setDateOfBirth(LocalDateTime.of(1985, 7, 10, 0, 0));
        newUser.setGender("MALE");
        newUser.setEmail("test@gmail.com"); // Different case, same email
        newUser.setUsername("bobjones");
        newUser.setPassword("BobPassword123");
        newUser.setHeight("5'11\"");
        newUser.setWeight("180 lbs");
        newUser.setAddress("789 Pine St, Boston, MA 02118");
        newUser.setPhoneNumber("617-444-4444");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.createPatientRecord(newUser, Collections.emptyList())
        );

        assertTrue(ex.getMessage().contains("already exists"),
                "Expected error message to contain 'already exists' but got: " + ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createPatientRecord_validEmailUnique_successfullyCreatesPatient() {
        // Arrange - Email does not exist in system
        when(userRepository.findAllByEmail("unique@gmail.com")).thenReturn(new ArrayList<>());
        
        User newUser = new User();
        newUser.setRole(Role.PATIENT);
        newUser.setFirstName("Alice");
        newUser.setLastName("Brown");
        newUser.setDateOfBirth(LocalDateTime.of(1990, 1, 15, 0, 0));
        newUser.setGender("FEMALE");
        newUser.setEmail("unique@gmail.com");
        newUser.setUsername("alicebrown");
        newUser.setPassword("AlicePass123");
        newUser.setHeight("5'4\"");
        newUser.setWeight("115 lbs");
        newUser.setAddress("321 Elm St, Boston, MA 02119");
        newUser.setPhoneNumber("617-555-5555");

        // Mock successful save
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(3L);
            return u;
        });

        // Act
        assertDoesNotThrow(() ->
                patientRecordService.createPatientRecord(newUser, Collections.emptyList())
        );

        // Verify save was called
        verify(userRepository, times(1)).save(any(User.class));
    }
}
