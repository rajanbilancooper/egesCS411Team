package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test Case #8: New Account Creation - Patient
 * Goal: Validate the system successfully creates a new patient account with valid inputs
 */
@ExtendWith(MockitoExtension.class)
class PatientAccountCreationUnitTest {

    @Mock
    private AllergyRepository allergyRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private MedicalHistoryRepo medicalHistoryRepo;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientRecordService patientRecordService;

    private User newPatientTemplate;

    @BeforeEach
    void setUp() {
        newPatientTemplate = new User();
        newPatientTemplate.setUsername("m.ocque");
        newPatientTemplate.setPassword("greatPassword!");
        newPatientTemplate.setFirstName("Miguel");
        newPatientTemplate.setLastName("Ocque");
        newPatientTemplate.setPhoneNumber("617-222-2222");
        newPatientTemplate.setEmail("miguel.ocque@example.com");
        newPatientTemplate.setRole(Role.PATIENT);
        newPatientTemplate.setGender("Male");
        newPatientTemplate.setDateOfBirth(LocalDateTime.of(1995, 3, 15, 0, 0));
        newPatientTemplate.setAddress("123 Main St, Boston MA, 02115");
        newPatientTemplate.setCreationDate(LocalDateTime.now());
        newPatientTemplate.setUpdateDate(LocalDateTime.now());
    }

    @Test
    void createPatientAccount_validInputs_successfullyCreatesAccount() {
        // Mock: no duplicate patient exists
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth(
            "Miguel", "Ocque", LocalDateTime.of(1995, 3, 15, 0, 0))
        ).thenReturn(Optional.empty());

        // Mock: repository save returns persisted user with generated ID
        User persistedUser = new User();
        persistedUser.setId(1001L);
        persistedUser.setUsername(newPatientTemplate.getUsername());
        persistedUser.setFirstName(newPatientTemplate.getFirstName());
        persistedUser.setLastName(newPatientTemplate.getLastName());
        persistedUser.setPhoneNumber(newPatientTemplate.getPhoneNumber());
        persistedUser.setEmail(newPatientTemplate.getEmail());
        persistedUser.setRole(newPatientTemplate.getRole());
        persistedUser.setGender(newPatientTemplate.getGender());
        persistedUser.setDateOfBirth(newPatientTemplate.getDateOfBirth());
        persistedUser.setAddress(newPatientTemplate.getAddress());

        when(userRepository.save(newPatientTemplate)).thenReturn(persistedUser);

        // Execute: create patient account
        PatientRecordDTO result = patientRecordService.createPatientRecord(newPatientTemplate, List.of());

        // Expected Results: Validate patient account fields
        assertThat(result.getPatientId()).isEqualTo(1001L);
        assertThat(result.getFirstName()).isEqualTo("Miguel");
        assertThat(result.getLastName()).isEqualTo("Ocque");
        assertThat(result.getPhoneNumber()).isEqualTo("617-222-2222");
        assertThat(result.getEmail()).isEqualTo("miguel.ocque@example.com");
        assertThat(result.getGender()).isEqualTo("Male");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDateTime.of(1995, 3, 15, 0, 0));
        assertThat(result.getAddress()).isEqualTo("123 Main St, Boston MA, 02115");

        // Verify: user was saved to repository
        verify(userRepository).save(newPatientTemplate);
        
        // Verify: duplicate check was performed
        verify(userRepository).findByFirstNameAndLastNameAndDateOfBirth(
            "Miguel", "Ocque", LocalDateTime.of(1995, 3, 15, 0, 0)
        );
    }

    @Test
    void createPatientAccount_missingUsername_throwsException() {
        newPatientTemplate.setUsername(null);

        try {
            patientRecordService.createPatientRecord(newPatientTemplate, List.of());
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("Username is required");
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void createPatientAccount_missingPhoneNumber_throwsException() {
        newPatientTemplate.setPhoneNumber(null);

        try {
            patientRecordService.createPatientRecord(newPatientTemplate, List.of());
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("Phone number is required");
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void createPatientAccount_invalidPhoneFormat_throwsException() {
        newPatientTemplate.setPhoneNumber("12345"); // Only 5 digits

        try {
            patientRecordService.createPatientRecord(newPatientTemplate, List.of());
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("Phone number must be 10 digits");
            verify(userRepository, never()).save(any());
        }
    }
}
