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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Real-time test: generate OTP and wait real time for expiry (5 minutes + 1s), then verify
 * that verifying throws OtpExpiredException.
 *
 * WARNING: this test sleeps ~5 minutes. Run only when you expect long-running test.
 */
public class LoginOtpExpiryRealTimeTest {

    @Test
    public void realTimeFiveMinuteExpiry_shouldThrowOtpExpiredException() throws Exception {
        // Arrange - mock dependencies
        OtpTokenRepository otpTokenRepository = Mockito.mock(OtpTokenRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserSessionRepository userSessionRepository = Mockito.mock(UserSessionRepository.class);
        EmailService emailService = Mockito.mock(EmailService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);

        // Capture the saved OTP token so we can inspect its expiresAt later
        AtomicReference<OtpToken> savedOtpRef = new AtomicReference<>();
        Mockito.when(otpTokenRepository.save(Mockito.any(OtpToken.class))).thenAnswer(inv -> {
            OtpToken t = inv.getArgument(0);
            savedOtpRef.set(t);
            return t;
        });

        // Make password encoder return a hashed value when generating the OTP
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("hashed-otp");

        OtpService otpService = new OtpService(
                otpTokenRepository,
                userRepository,
                userSessionRepository,
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

        // Act step 1: generate OTP (this will set expiresAt = now + 5 minutes)
        otpService.generateAndSendOtp(user);

        // Ensure we captured a token
        OtpToken token = savedOtpRef.get();
        if (token == null) {
            throw new IllegalStateException("OTP token was not saved by mocked repository");
        }

        // Log-ish output for human during long sleep (so test runner shows progress)
        System.out.println("OTP generated; expiresAt=" + token.getExpiresAt() + ", now=" + LocalDateTime.now());

        // Wait 5 minutes + 1 second to ensure token is expired
        long sleepMs = 5L * 60L * 1000L + 1000L;
        Thread.sleep(sleepMs);

        System.out.println("Woke up; now=" + LocalDateTime.now());

        // Make repository return the saved token regardless of expire filter so the service's expiry check runs
        Mockito.when(
                otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                        Mockito.eq(user.getId()), Mockito.any(LocalDateTime.class))
        ).thenReturn(Optional.of(token));

        // Act & Assert: verifying now should raise OtpExpiredException
        OtpVerificationRequest request = new OtpVerificationRequest("rajanbilancooper", "123456", "192.0.2.1");

        assertThrows(com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.OtpExpiredException.class,
                () -> otpService.verifyAndCompleteLogin(request));
    }
}
