package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.loginTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.LoginRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.OtpVerificationRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.LoginResponse;
import com.Eges411Team.UnifiedPatientManager.entity.OtpToken;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.OtpTokenRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import com.Eges411Team.UnifiedPatientManager.services.AuthenticationService;
import com.Eges411Team.UnifiedPatientManager.services.JwtTokenProvider;
import com.Eges411Team.UnifiedPatientManager.services.OtpService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional test: successful login with valid username/password and OTP.
 */
public class SuccessfulLoginWith2FATest {

    @Test
    public void successfulLogin_withValidOtp_shouldReturnToken_andMarkOtpUsed() {
        // Arrange - mocks for AuthenticationService
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserSessionRepository userSessionRepository = Mockito.mock(UserSessionRepository.class);
        OtpService otpServiceMock = Mockito.mock(OtpService.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        PasswordEncoder authPasswordEncoder = Mockito.mock(PasswordEncoder.class);
        OtpTokenRepository otpTokenRepository = Mockito.mock(OtpTokenRepository.class);

        AuthenticationService authService = new AuthenticationService(
                userRepository,
                userSessionRepository,
                otpServiceMock,
                jwtTokenProvider,
                authPasswordEncoder
                ,otpTokenRepository
        );

        // Fixture user in DB
        User user = new User();
        user.setId(1L);
        user.setUsername("rajanbilancooper");
        user.setPassword("storedHash");
        user.setEmail("rajan@example.com");
        // set a valid role to avoid NPE when building response
        user.setRole(com.Eges411Team.UnifiedPatientManager.entity.Role.PATIENT);
        user.setIsLocked(false);

        Mockito.when(userRepository.findByUsername("rajanbilancooper")).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Auth password encoder should accept the correct password
        Mockito.when(authPasswordEncoder.matches("rajanpassword123", "storedHash")).thenReturn(true);

        // OTP generation should be invoked but we mock it to avoid external email sending
        Mockito.doNothing().when(otpServiceMock).generateAndSendOtp(Mockito.eq(user));

        // Act - trigger authentication which should call generateAndSendOtp
        LoginRequest loginReq = new LoginRequest("rajanbilancooper", "rajanpassword123", false);
        LoginResponse preOtpResponse = authService.authenticate(loginReq, "192.0.2.1");

        assertNotNull(preOtpResponse);
        assertEquals(user.getId(), preOtpResponse.getUserId());
        assertEquals(user.getUsername(), preOtpResponse.getUsername());

        // Now prepare OtpService (real instance) to verify OTP
        otpTokenRepository = Mockito.mock(OtpTokenRepository.class);
        PasswordEncoder otpPasswordEncoder = Mockito.mock(PasswordEncoder.class);
        JwtTokenProvider jwtProviderForOtp = Mockito.mock(JwtTokenProvider.class);

        // Create an OTP token that is valid
        OtpToken otpToken = new OtpToken();
        otpToken.setUser(user);
        otpToken.setOtpCode("hashed-otp");
        otpToken.setCreatedAt(LocalDateTime.now());
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpToken.setUsed(false);

        Mockito.when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                Mockito.eq(user.getId()), Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(otpToken));

        Mockito.when(otpTokenRepository.save(Mockito.any(OtpToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Make password encoder match the provided OTP
        Mockito.when(otpPasswordEncoder.matches("123456", "hashed-otp")).thenReturn(true);

        // Make JWT provider produce a token
        Mockito.when(jwtProviderForOtp.generateToken(Mockito.eq(user))).thenReturn("token-abc-123");
        Mockito.when(jwtProviderForOtp.getExpirationTime()).thenReturn(86400L);

        OtpService otpService = new OtpService(
                otpTokenRepository,
                userRepository,
                userSessionRepository,
                Mockito.mock(com.Eges411Team.UnifiedPatientManager.services.EmailService.class),
                otpPasswordEncoder,
                jwtProviderForOtp
        );

        // Act - verify the OTP and complete login
        OtpVerificationRequest otpReq = new OtpVerificationRequest("rajanbilancooper", "123456", "192.0.2.1");
        LoginResponse finalResponse = otpService.verifyAndCompleteLogin(otpReq);

        // Assert - token present and OTP marked used
        assertNotNull(finalResponse.getToken());
        assertEquals("token-abc-123", finalResponse.getToken());
        assertEquals("Bearer", finalResponse.getTokenType());
        assertTrue(otpToken.isUsed(), "OTP should be marked used after successful verification");

        // Verify a session was created (save called)
        Mockito.verify(userSessionRepository, Mockito.atLeastOnce()).save(Mockito.any());
    }
}
