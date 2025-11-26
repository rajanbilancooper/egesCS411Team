package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @JsonProperty("username")
    @NotBlank(message = "Username is required")
    private String username;
}
