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
 * Test Case 6: Password with Too Few Characters
 * Use Case: Storing Detailed Patient Information
 * Requirement: 3.1.3 - The system shall require users to authenticate using a username and password
 * Technique: Robustness Testing – Testing Min - boundary for password length
 */
@ExtendWith(MockitoExtension.class)
class PasswordTooShortTest {

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
    void createPatientRecord_passwordTooShort_throwsExceptionAndDoesNotSave() {
        // Arrange - Build a User with password that's too short as per test case
        User user = new User();
        user.setRole(Role.PATIENT);
        user.setPhoneNumber("617-222-2222");
        user.setFirstName("Christian");
        user.setLastName("Poli");
        user.setUsername("cpoli");
        user.setPassword("1"); // Min - (too short, only 1 character)
        user.setAddress("214 Bay State Rd, Boston MA 02215");
        user.setGender("MALE");
        user.setEmail("christian@gmail.com");
        user.setDateOfBirth(LocalDateTime.of(1998, 3, 10, 0, 0)); // valid past date
        user.setHeight("5'9\"");
        user.setWeight("150 lbs");

        // Act & Assert - expect RuntimeException for password being too short
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.createPatientRecord(user, java.util.Collections.emptyList())
        );

        assertTrue(ex.getMessage().contains("Password Length must be longer than 6 characters"),
                "Expected error message to contain 'Password Length must be longer than 6 characters' but got: " + ex.getMessage());

        // Verify repository.save was never called due to validation failure
        verify(userRepository, never()).save(any(User.class));

        // Ensure input fields remain unchanged (user is prompted to enter new password without losing information)
        assertEquals("617-222-2222", user.getPhoneNumber());
        assertEquals("Christian", user.getFirstName());
        assertEquals("Poli", user.getLastName());
        assertEquals("cpoli", user.getUsername());
        assertEquals("1", user.getPasswordHash());
        assertEquals("214 Bay State Rd, Boston MA 02215", user.getAddress());
        assertEquals("MALE", user.getGender());
        assertEquals("christian@gmail.com", user.getEmail());
        assertEquals("5'9\"", user.getHeight());
        assertEquals("150 lbs", user.getWeight());
        assertEquals(Role.PATIENT, user.getRole());
    }
}
