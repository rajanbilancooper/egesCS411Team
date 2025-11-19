package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientRecordServiceUnitTest {

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

    @Test
    void getPatientRecordByFullName_notFound_throws() {
        String fullName = "John Smith";
        when(userRepository.findByFirstNameAndLastName("John", "Smith")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            patientRecordService.getPatientRecordByFullName(fullName)
        );

        assertThat(ex.getMessage()).contains("User not found");
        verify(userRepository).findByFirstNameAndLastName("John", "Smith");
        verifyNoInteractions(allergyRepository, medicationRepository, medicalHistoryRepo);
    }

    @Test
    void getPatientRecordByFullName_found_returnsDTO() {
        String fullName = "Jane Doe";
        User user = new User();
        user.setId(55L);
        user.setFirstName("Jane");
        user.setLastName("Doe");

        when(userRepository.findByFirstNameAndLastName("Jane", "Doe")).thenReturn(Optional.of(user));
        when(userRepository.findById(55L)).thenReturn(Optional.of(user));
        // downstream repositories return empty lists
        when(allergyRepository.findAllByPatientId(55L)).thenReturn(java.util.List.of());
        when(medicationRepository.findAllByPatientId(55L)).thenReturn(java.util.List.of());
        when(medicalHistoryRepo.findAllByPatientId(55L)).thenReturn(java.util.List.of());

        var dto = patientRecordService.getPatientRecordByFullName(fullName);
        assertThat(dto.getPatientId()).isEqualTo(55L);
        assertThat(dto.getFirstName()).isEqualTo("Jane");
        assertThat(dto.getLastName()).isEqualTo("Doe");
    }
}
