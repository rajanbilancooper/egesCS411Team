package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientRegistrationRequest {

    @JsonProperty("username")
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @JsonProperty("password")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password; // plain inbound; will be hashed before persistence

    @JsonProperty("firstName")
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name max 50 characters")
    private String firstName;

    @JsonProperty("lastName")
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name max 50 characters")
    private String lastName;

    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @JsonProperty("phoneNumber")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,25}$", message = "Phone number format invalid")
    private String phoneNumber;

    @JsonProperty("address")
    @NotBlank(message = "Address is required")
    @Size(max = 100, message = "Address max 100 characters")
    private String address;

    @JsonProperty("gender")
    @NotBlank(message = "Gender is required")
    private String gender;

    // incoming date string (e.g. 2025-11-19) should be converted to LocalDateTime in controller
    @JsonProperty("dateOfBirth")
    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth; // keep as String for flexible parsing

    @JsonProperty("allergies")
    private List<AllergyRequest> allergies; // optional
}
