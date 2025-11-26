package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {

    @JsonProperty("username")
    @NotBlank(message = "Username is required")
    private String username;

    @JsonProperty("otpCode")
    @NotBlank(message = "OTP code is required")
    private String otpCode;

    @JsonProperty("newPassword")
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
