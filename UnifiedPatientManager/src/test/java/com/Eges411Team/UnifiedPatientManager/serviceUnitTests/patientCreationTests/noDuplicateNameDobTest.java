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
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: No Duplicate Name + DOB Combination
 * Use Case: Storing Detailed Patient Information
 * Requirement: 3.1.1 - The system shall store patient information and prevent duplicate records
 * Technique: Equivalence Partitioning – Testing invalid input class for duplicate name+DOB
 */
@ExtendWith(MockitoExtension.class)
class noDuplicateNameDobTest {

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
    void createPatientRecord_duplicateNameAndDob_throwsExceptionAndDoesNotSave() {
        // Arrange - Pre-condition: A user already exists with name "Michael Johnson" born "1985-04-12"
        User existingPatient = new User();
        existingPatient.setId(1L);
        existingPatient.setFirstName("Michael");
        existingPatient.setLastName("Johnson");
        existingPatient.setDateOfBirth(LocalDateTime.of(1985, 4, 12, 0, 0));
        existingPatient.setEmail("michael.j@gmail.com");
        existingPatient.setUsername("michaelj");

        // Mock repository to return the duplicate when checking name+DOB
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("Michael", "Johnson", LocalDateTime.of(1985, 4, 12, 0, 0)))
                .thenReturn(Optional.of(existingPatient));

        // Build a new User with the same name and DOB as per test case input
        User newUser = new User();
        newUser.setRole(Role.PATIENT);
        newUser.setFirstName("Michael");
        newUser.setLastName("Johnson");
        newUser.setDateOfBirth(LocalDateTime.of(1985, 4, 12, 0, 0)); // same DOB as existing
        newUser.setGender("MALE");
        newUser.setEmail("michael.johnson.new@gmail.com"); // different email
        newUser.setUsername("michaeljohns2");
        newUser.setPassword("MichaelPass123");
        newUser.setHeight("5'10\"");
        newUser.setWeight("175 lbs");
        newUser.setAddress("999 Main St, Boston, MA 02111");
        newUser.setPhoneNumber("617-666-6666");

        // Act & Assert - expect RuntimeException with message about duplicate record
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.createPatientRecord(newUser, Collections.emptyList())
        );

        assertTrue(ex.getMessage().contains("already exists") && ex.getMessage().contains("name"),
                "Expected error message to contain 'already exists' and 'name' but got: " + ex.getMessage());

        // Verify repository.save was never called due to duplicate validation failure
        verify(userRepository, never()).save(any(User.class));

        // Ensure input fields remain unchanged
        assertEquals("Michael", newUser.getFirstName());
        assertEquals("Johnson", newUser.getLastName());
        assertEquals(LocalDateTime.of(1985, 4, 12, 0, 0), newUser.getDateOfBirth());
        assertEquals("michael.johnson.new@gmail.com", newUser.getEmail());
    }

    @Test
    void createPatientRecord_sameNameDifferentDob_successfullyCreatesPatient() {
        // Arrange - Same name but different DOB should be allowed
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("Sarah", "Davis", LocalDateTime.of(1995, 6, 22, 0, 0)))
                .thenReturn(Optional.empty()); // No duplicate found
        when(userRepository.findAllByEmail("sarah.davis@gmail.com"))
                .thenReturn(Collections.emptyList()); // Email is unique

        User newUser = new User();
        newUser.setRole(Role.PATIENT);
        newUser.setFirstName("Sarah");
        newUser.setLastName("Davis");
        newUser.setDateOfBirth(LocalDateTime.of(1995, 6, 22, 0, 0));
        newUser.setGender("FEMALE");
        newUser.setEmail("sarah.davis@gmail.com");
        newUser.setUsername("sarahdavis");
        newUser.setPassword("SarahPass123");
        newUser.setHeight("5'6\"");
        newUser.setWeight("125 lbs");
        newUser.setAddress("555 Cedar St, Boston, MA 02115");
        newUser.setPhoneNumber("617-777-7777");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        // Act
        assertDoesNotThrow(() ->
                patientRecordService.createPatientRecord(newUser, Collections.emptyList())
        );

        // Verify save was called
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createPatientRecord_sameDobDifferentName_successfullyCreatesPatient() {
        // Arrange - Same DOB but different name should be allowed
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("Jennifer", "Wilson", LocalDateTime.of(1988, 9, 5, 0, 0)))
                .thenReturn(Optional.empty()); // No duplicate found
        when(userRepository.findAllByEmail("jennifer.w@gmail.com"))
                .thenReturn(Collections.emptyList()); // Email is unique

        User newUser = new User();
        newUser.setRole(Role.PATIENT);
        newUser.setFirstName("Jennifer");
        newUser.setLastName("Wilson");
        newUser.setDateOfBirth(LocalDateTime.of(1988, 9, 5, 0, 0));
        newUser.setGender("FEMALE");
        newUser.setEmail("jennifer.w@gmail.com");
        newUser.setUsername("jenniferwilson");
        newUser.setPassword("JenniPass123");
        newUser.setHeight("5'5\"");
        newUser.setWeight("130 lbs");
        newUser.setAddress("666 Birch Ln, Boston, MA 02114");
        newUser.setPhoneNumber("617-888-8888");

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
