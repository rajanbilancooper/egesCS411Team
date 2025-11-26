package com.Eges411Team.UnifiedPatientManager.controller;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.LoginRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.OtpVerificationRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.ResendOtpRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.ForgotPasswordRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PasswordResetRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.LoginResponse;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.AuthenticationService;
import com.Eges411Team.UnifiedPatientManager.services.OtpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final OtpService otpService;
    private final UserRepository userRepository;

    public AuthenticationController(AuthenticationService authenticationService,
                                    OtpService otpService,
                                    UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.otpService = otpService;
        this.userRepository = userRepository;
    }

    // Step 1: Username + password -> send OTP
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        LoginResponse response = authenticationService.authenticate(request, ip);
        return ResponseEntity.ok(response);
    }

    // Step 2: Verify OTP -> return JWT + user info
    @PostMapping("/verify")
    public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request,
                                                   HttpServletRequest httpRequest) {
        // Set IP address from request
        request.setIpAddress(httpRequest.getRemoteAddr());
        LoginResponse response = otpService.verifyAndCompleteLogin(request);
        return ResponseEntity.ok(response);
    }

    // Resend OTP for username
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = userOpt.get();
        // Use existing service to generate and send OTP; it will validate email presence
        otpService.generateAndSendOtp(user);
        return ResponseEntity.accepted().body("OTP resent");
    }

    // Logout by token (expects Authorization header)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Authorization header");
        }
        authenticationService.logout(authorization);
        return ResponseEntity.ok().body("Logged out");
    }

    // Forgot password - Step 1: Send OTP to user's email
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.initiateForgotPassword(request.getUsername());
        return ResponseEntity.ok().body("Password reset OTP sent to your email");
    }

    // Password reset - Step 2: Verify OTP and set new password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authenticationService.resetPassword(
            request.getUsername(),
            request.getOtpCode(),
            request.getNewPassword()
        );
        return ResponseEntity.ok().body("Password reset successful");
    }

    

}


