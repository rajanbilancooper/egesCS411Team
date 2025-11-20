package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientRegistrationRequest {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password; // plain for now; service hashes if implemented

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("address")
    private String address;

    @JsonProperty("gender")
    private String gender;

    // incoming date string (e.g. 2025-11-19) should be converted to LocalDateTime in controller
    @JsonProperty("dateOfBirth")
    private String dateOfBirth; // keep as String for flexible parsing

    @JsonProperty("allergies")
    private List<AllergyRequest> allergies; // optional
}
