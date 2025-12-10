package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientCreationTests;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case 5: No Duplicate Usernames
 * Use Case: Storing Detailed Patient Information
 * Requirement: 3.1.1 - The system shall store patient information
 * Technique: Equivalence Partitioning – Testing invalid input class for creating an account that already exists
 */
@ExtendWith(MockitoExtension.class)
class noDuplicateUsernamesTest {

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
    void createPatientRecord_duplicateUsername_throwsExceptionAndDoesNotSave() {
        // Arrange - Pre-condition: A user already exists with the username "miguelocque"
        // Mock repository to return that the username already exists
        when(userRepository.existsByUsername("miguelocque")).thenReturn(true);

        // Build a new User with the duplicate username as per test case input
        User newUser = new User();
        newUser.setRole(Role.PATIENT);
        newUser.setPhoneNumber("617-222-2222");
        newUser.setFirstName("Miguel");
        newUser.setLastName("Ocque");
        newUser.setUsername("miguelocque"); // duplicate username
        newUser.setPassword("greatPassword!");
        newUser.setAddress("200 Bay State Rd, Boston MA 02215");
        newUser.setGender("MALE");
        newUser.setEmail("miguelsSecondEmail@gmail.com");
        newUser.setDateOfBirth(LocalDateTime.of(1995, 5, 15, 0, 0)); // valid past date
        newUser.setHeight("5'7\"");
        newUser.setWeight("125 lbs");

        // Act & Assert - expect RuntimeException with message "User already exists"
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.createPatientRecord(newUser, java.util.Collections.emptyList())
        );

        assertTrue(ex.getMessage().contains("User already exists"),
                "Expected error message to contain 'User already exists' but got: " + ex.getMessage());

        // Verify repository.save was never called due to validation failure
        verify(userRepository, never()).save(any(User.class));

        // Ensure input fields remain unchanged (user is prompted to create new username without losing information)
        assertEquals("617-222-2222", newUser.getPhoneNumber());
        assertEquals("Miguel", newUser.getFirstName());
        assertEquals("Ocque", newUser.getLastName());
        assertEquals("miguelocque", newUser.getUsername());
        assertEquals("greatPassword!", newUser.getPasswordHash());
        assertEquals("200 Bay State Rd, Boston MA 02215", newUser.getAddress());
        assertEquals("MALE", newUser.getGender());
        assertEquals("miguelsSecondEmail@gmail.com", newUser.getEmail());
        assertEquals("5'7\"", newUser.getHeight());
        assertEquals("125 lbs", newUser.getWeight());
        assertEquals(Role.PATIENT, newUser.getRole());
    }
}
