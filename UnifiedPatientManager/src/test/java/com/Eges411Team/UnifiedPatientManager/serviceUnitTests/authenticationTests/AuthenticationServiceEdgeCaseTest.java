package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.authenticationTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.LoginRequest;
import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.entity.UserSession;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.OtpTokenRepository;
import com.Eges411Team.UnifiedPatientManager.services.AuthenticationService;
import com.Eges411Team.UnifiedPatientManager.services.OtpService;
import com.Eges411Team.UnifiedPatientManager.services.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService edge cases and branch coverage.
 * Focuses on untested branches: locked accounts, bad passwords, OTP generation failures.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceEdgeCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword");
        testUser.setRole(Role.PATIENT);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setFailedLoginAttempts(0);
        testUser.setIsLocked(false);
    }

    // ==================== Authenticate - Locked Account Test ====================
    @Test
    void authenticate_lockedAccount_throwsLockedException() {
        // Setup: User account is locked
        testUser.setIsLocked(true);
        
        LoginRequest loginRequest = new LoginRequest("testuser", "password", false);

        // Mock: User found but account is locked
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        // Execute & Verify: Should throw LockedException
        assertThrows(org.springframework.security.authentication.LockedException.class,
            () -> authenticationService.authenticate(loginRequest, "192.168.1.1"));

        // Verify: OTP should never be generated for locked accounts
        verify(otpService, never()).generateAndSendOtp(any());
    }

    // ==================== Authenticate - Bad Password Test ====================
    @Test
    void authenticate_wrongPassword_throwsBadCredentialsException() {
        // Setup: User exists but password is wrong
        testUser.setFailedLoginAttempts(0);
        
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongPassword", false);

        // Mock: User found, password doesn't match
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash()))
            .thenReturn(false);
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // Execute & Verify: Should throw BadCredentialsException
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
            () -> authenticationService.authenticate(loginRequest, "192.168.1.1"));

        // Verify: Failed login attempts should be incremented
        verify(userRepository).save(argThat(user -> user.getFailedLoginAttempts() == 1));
    }

    // ==================== Authenticate - Account Lock After Failed Attempts Test ====================
    @Test
    void authenticate_thirdFailedAttempt_locksAccount() {
        // Setup: User has already failed twice
        testUser.setFailedLoginAttempts(2);
        testUser.setIsLocked(false);
        
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongPassword", false);

        // Mock: User found, password doesn't match
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash()))
            .thenReturn(false);
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // Execute: Third failed attempt
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
            () -> authenticationService.authenticate(loginRequest, "192.168.1.1"));

        // Verify: Account should be locked after 3 attempts
        verify(userRepository).save(argThat(user -> 
            user.getFailedLoginAttempts() == 3 && user.getIsLocked()));
    }

    // ==================== Authenticate - OTP Generation Failure Test ====================
    @Test
    void authenticate_otpGenerationFails_rethrowsException() {
        // Setup: User exists, password is correct, but OTP generation fails
        testUser.setFailedLoginAttempts(0);
        
        LoginRequest loginRequest = new LoginRequest("testuser", "correctPassword", false);

        // Mock: User found, password matches
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctPassword", testUser.getPasswordHash()))
            .thenReturn(true);
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        
        // Mock: OTP generation throws exception
        doThrow(new RuntimeException("Email service unavailable"))
            .when(otpService).generateAndSendOtp(testUser);

        // Execute & Verify: Should rethrow the OTP exception
        assertThrows(RuntimeException.class,
            () -> authenticationService.authenticate(loginRequest, "192.168.1.1"));

        // Verify: Failed login attempts should still be reset before OTP call
        verify(userRepository).save(argThat(user -> user.getFailedLoginAttempts() == 0));
    }
    // ==================== Logout Tests ====================
    @Test
    void logout_validActiveSession_deactivatesSession() {
        // Setup: Create a session with a token
        String token = "Bearer validToken123";
        UserSession activeSession = new UserSession();
        activeSession.setId(1L);
        activeSession.setSessionToken("validToken123");
        activeSession.setIsActive(true);
        activeSession.setUser(testUser);

        // Mock: Session exists and is active
        when(userSessionRepository.findBySessionTokenAndActive("validToken123", true))
            .thenReturn(Optional.of(activeSession));
        when(userSessionRepository.save(any(UserSession.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // Execute: Logout
        assertDoesNotThrow(() -> authenticationService.logout(token));

        // Verify: Session was saved with isActive=false
        verify(userSessionRepository).save(argThat(session -> !session.getIsActive()));
    }

    @Test
    void logout_noActiveSession_throwsException() {
        // Setup: No session found for this token
        String token = "Bearer invalidToken";

        // Mock: No session found
        when(userSessionRepository.findBySessionTokenAndActive("invalidToken", true))
            .thenReturn(Optional.empty());

        // Execute & Verify: Should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> authenticationService.logout(token));
    }

    @Test
    void logout_sessionWithBearerPrefix_stripsPrefix() {
        // Setup: Token with "Bearer " prefix
        String tokenWithPrefix = "Bearer myToken123";
        UserSession activeSession = new UserSession();
        activeSession.setId(1L);
        activeSession.setSessionToken("myToken123");
        activeSession.setIsActive(true);
        activeSession.setUser(testUser);

        // Mock: Verify that prefix is stripped before lookup
        when(userSessionRepository.findBySessionTokenAndActive("myToken123", true))
            .thenReturn(Optional.of(activeSession));
        when(userSessionRepository.save(any(UserSession.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // Execute: Logout with bearer prefix
        assertDoesNotThrow(() -> authenticationService.logout(tokenWithPrefix));

        // Verify: Was called with the token WITHOUT the prefix
        verify(userSessionRepository).findBySessionTokenAndActive("myToken123", true);
    }

    // ==================== HandleFailedLogin - Lock Account Test ====================
    @Test
    void handleFailedLogin_thirdAttempt_locksAccount() {
        // The handleFailedLogin method is tested indirectly through authenticate
        // when we make 3 failed password attempts. Already covered by:
        // authenticate_thirdFailedAttempt_locksAccount test
    }
}
