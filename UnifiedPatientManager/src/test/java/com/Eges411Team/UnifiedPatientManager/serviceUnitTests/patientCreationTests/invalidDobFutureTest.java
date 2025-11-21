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

@ExtendWith(MockitoExtension.class)
class invalidDobFutureTest {

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
    void createPatientRecord_dobInFuture_throwsExceptionAndDoesNotSave() {
        // Arrange - build a User with a DOB in the future as per test case
        User user = new User();
        user.setRole(Role.PATIENT);
        user.setFirstName("Mike");
        user.setLastName("London");
        user.setDateOfBirth(LocalDateTime.of(2030, 6, 24, 0, 0)); // future date
        user.setGender("MALE");
        user.setEmail("mikesemail@gmail.com");
        user.setUsername("mikeiscool");
        user.setPassword("MikeLondon123");
        user.setHeight("6'7\"");
        user.setWeight("220 lbs");
        user.setAddress("665 Commonwealth Ave, Boston, MA 02215");
        user.setPhoneNumber("617-222-2222");

        // Act & Assert - expect RuntimeException for future DOB
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.createPatientRecord(user, java.util.Collections.emptyList())
        );

        assertTrue(ex.getMessage().contains("Date of birth cannot be in the future"));

        // Verify repository.save was never called due to validation failure
        verify(userRepository, never()).save(any(User.class));

        // Ensure other fields remain unchanged (inputs preserved)
        assertEquals("Mike", user.getFirstName());
        assertEquals("London", user.getLastName());
        assertEquals("mikesemail@gmail.com", user.getEmail());
        assertEquals("mikeiscool", user.getUsername());
    }
}
