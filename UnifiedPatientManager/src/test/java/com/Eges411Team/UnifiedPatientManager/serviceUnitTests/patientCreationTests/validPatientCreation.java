package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientCreationTests;

import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class validPatientCreation {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	void newAccountCreation_Patient_validInputs_createsAndSavesPatient() {
		// Arrange - build a User object matching the test case inputs
		User patient = new User();
		patient.setUsername("m.ocque");
		patient.setPassword("greatPassword!");
		patient.setRole(Role.PATIENT);
		patient.setFirstName("Miguel");
		patient.setLastName("Ocque");
		patient.setPhoneNumber("617-222-2222");
		patient.setAddress("200 Bay State Rd, Boston MA 02215");
		patient.setGender("MALE");
		patient.setEmail("miguelsemail@gmail.com");
		patient.setHeight("5'7\"");
		patient.setWeight("125 lbs");
		patient.setDateOfBirth(LocalDateTime.of(1995,1,1,0,0));
		patient.setCreationDate(LocalDateTime.now());
		patient.setUpdateDate(LocalDateTime.now());

		// Mock repository behavior to simulate DB assigning an ID on save
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User u = invocation.getArgument(0);
			u.setId(1L);
			return u;
		});

		// Act
		User saved = userService.saveUser(patient);

		// Assert - verify repository was called and returned user has an id
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository, times(1)).save(captor.capture());

		User captured = captor.getValue();
		assertEquals("m.ocque", captured.getUsername());
		assertEquals("Miguel", captured.getFirstName());
		assertEquals("Ocque", captured.getLastName());
		assertEquals("617-222-2222", captured.getPhoneNumber());
		assertEquals(Role.PATIENT, captured.getRole());
		assertEquals("miguelsemail@gmail.com", captured.getEmail());

		assertNotNull(saved);
		assertEquals(1L, saved.getId());
	}
}

