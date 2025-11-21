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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class invalidPhoneNumberTest {

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
    void createPatientRecord_invalidPhone_throwsExceptionAndDoesNotSave() {
        // Arrange - build a User with invalid phone number as per test case
        User user = new User();
        user.setRole(Role.PATIENT);
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setDateOfBirth(LocalDateTime.of(2000, 6, 24, 0, 0));
        user.setGender("MALE");
        user.setEmail("johnsemail@gmail.com");
        user.setUsername("johniscool");
        user.setPassword("JohnJohnson123");
        user.setHeight("6'6\"");
        user.setWeight("200 lbs");
        user.setAddress("1 White House, Washington DC, USA, 01111");
        user.setPhoneNumber("24536"); // invalid

        // No repository stubbing needed because validation fails before duplicate check

        // Act & Assert - expect RuntimeException for invalid phone format
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.createPatientRecord(user, Collections.emptyList())
        );

        assertTrue(ex.getMessage().contains("Phone number must be 10 digits"));

        // Verify repository.save was never called due to validation failure
        verify(userRepository, never()).save(any(User.class));

        // Ensure other fields remain unchanged (inputs preserved)
        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("johnsemail@gmail.com", user.getEmail());
        assertEquals("johniscool", user.getUsername());
    }
}
