package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.loginTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.OtpVerificationRequest;
import com.Eges411Team.UnifiedPatientManager.entity.OtpToken;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.OtpTokenRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import com.Eges411Team.UnifiedPatientManager.services.EmailService;
import com.Eges411Team.UnifiedPatientManager.services.JwtTokenProvider;
import com.Eges411Team.UnifiedPatientManager.services.OtpService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Boundary test: attempt to verify OTP at the 5 minute expiry boundary (elapsed = 5 minutes)
 */
public class LoginOtpExpiryBoundaryTest {

    @Test
    public void verifyingOtpAtFiveMinuteBoundary_shouldThrowOtpExpiredException() {
        // Arrange - mock dependencies
        OtpTokenRepository otpTokenRepository = Mockito.mock(OtpTokenRepository.class);
    UserRepository userRepository = Mockito.mock(UserRepository.class);
        EmailService emailService = Mockito.mock(EmailService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);

    OtpService otpService = new OtpService(
        otpTokenRepository,
        userRepository,
        Mockito.mock(UserSessionRepository.class),
        emailService,
        passwordEncoder,
        jwtTokenProvider
    );

        // Create user fixture
        User user = new User();
        user.setId(1L);
        user.setUsername("rajanbilancooper");
        user.setEmail("rajan@example.com");

        Mockito.when(userRepository.findByUsername("rajanbilancooper")).thenReturn(Optional.of(user));

        // Create an OTP token that expired exactly 5 minutes ago (boundary)
    LocalDateTime now = LocalDateTime.now();
        OtpToken otpToken = new OtpToken();
        otpToken.setUser(user);
        otpToken.setOtpCode("hashed-otp");
        otpToken.setCreatedAt(now.minusMinutes(6));
        otpToken.setExpiresAt(now.minusMinutes(5)); // expired exactly 5 minutes ago
        otpToken.setUsed(false);

        Mockito.when(
                otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                        Mockito.eq(user.getId()), Mockito.any(LocalDateTime.class))
        ).thenReturn(Optional.of(otpToken));

        // Act - prepare request and assert expired exception is thrown
        OtpVerificationRequest request = new OtpVerificationRequest("rajanbilancooper", "123456", "192.0.2.1");

        assertThrows(com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.OtpExpiredException.class,
                () -> otpService.verifyAndCompleteLogin(request));
    }

    // no helper needed
}
