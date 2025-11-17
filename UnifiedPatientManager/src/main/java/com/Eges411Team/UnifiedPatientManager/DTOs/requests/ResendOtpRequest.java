package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

import jakarta.validation.constraints.NotBlank;

public class ResendOtpRequest {
    @NotBlank(message = "Username is required")
    private String username;

    public ResendOtpRequest() {}

    public ResendOtpRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
