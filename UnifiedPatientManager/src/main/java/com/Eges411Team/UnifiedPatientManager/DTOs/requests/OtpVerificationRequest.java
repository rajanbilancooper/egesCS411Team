package com.Eges411Team.UnifiedPatientManager.DTOs.requests;
import jakarta.validation.constraints.NotBlank; //Needed to ensure that certain fields are not left blank
import jakarta.validation.constraints.Pattern; //Needed to validate things like emails and phones which follow a specific pattern 


public class OtpVerificationRequest {
    //Fields for OTP verification request
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits") //Exactly six digits 
    private String otpCode;

    private String ipAddress;

    // Constructors
    public OtpVerificationRequest() {}

    public OtpVerificationRequest(String username, String otpCode, String ipAddress) {
        this.username = username;
        this.otpCode = otpCode;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    

}
