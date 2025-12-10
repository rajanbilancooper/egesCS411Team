package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.otpTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.OtpVerificationRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.LoginResponse;
import com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.InvalidOtpException;
import com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.OtpExpiredException;
import com.Eges411Team.UnifiedPatientManager.entity.OtpToken;
import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.OtpTokenRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import com.Eges411Team.UnifiedPatientManager.services.EmailService;
import com.Eges411Team.UnifiedPatientManager.services.JwtTokenProvider;
import com.Eges411Team.UnifiedPatientManager.services.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceComprehensiveTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSessionRepository userSessionRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private OtpService otpService;

    private User testUser;
    private OtpToken testOtpToken;
    private OtpVerificationRequest verificationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.PATIENT);

        testOtpToken = new OtpToken();
        testOtpToken.setOtpId(1L);
        testOtpToken.setUser(testUser);
        testOtpToken.setOtpCode("hashedotp123");
        testOtpToken.setCreatedAt(LocalDateTime.now());
        testOtpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        testOtpToken.setUsed(false);
        testOtpToken.setAttemptCount(0);

        verificationRequest = new OtpVerificationRequest();
        verificationRequest.setUsername("testuser");
        verificationRequest.setOtpCode("123456");
        verificationRequest.setIpAddress("192.168.1.1");
    }

    // ===== generateAndSendOtp Tests =====
    @Test
    void generateAndSendOtp_validUser_successfullyGeneratesAndSends() {
        doNothing().when(otpTokenRepository).invalidateUnusedOtps(anyLong());
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(testOtpToken);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        assertDoesNotThrow(() -> otpService.generateAndSendOtp(testUser));

        verify(otpTokenRepository).invalidateUnusedOtps(anyLong());
        verify(otpTokenRepository).save(any(OtpToken.class));
        verify(emailService).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void generateAndSendOtp_nullEmail_throwsIllegalStateException() {
        testUser.setEmail(null);
        doNothing().when(otpTokenRepository).invalidateUnusedOtps(anyLong());
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(testOtpToken);

        assertThrows(IllegalStateException.class, () -> otpService.generateAndSendOtp(testUser));
    }

    @Test
    void generateAndSendOtp_blankEmail_throwsIllegalStateException() {
        testUser.setEmail("   ");
        doNothing().when(otpTokenRepository).invalidateUnusedOtps(anyLong());
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(testOtpToken);

        assertThrows(IllegalStateException.class, () -> otpService.generateAndSendOtp(testUser));
    }

    @Test
    void generateAndSendOtp_emailSendFails_otpStillPersisted() {
        doNothing().when(otpTokenRepository).invalidateUnusedOtps(anyLong());
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(testOtpToken);
        doThrow(new MailException("SMTP failed") {}).when(emailService).sendOtpEmail(anyString(), anyString());

        // Should NOT throw - transaction should not rollback for MailException
        assertDoesNotThrow(() -> otpService.generateAndSendOtp(testUser));

        verify(otpTokenRepository).save(any(OtpToken.class));
        verify(emailService).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void generateAndSendOtp_generatesUniqueOtpCodes() {
        doNothing().when(otpTokenRepository).invalidateUnusedOtps(anyLong());
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(testOtpToken);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        otpService.generateAndSendOtp(testUser);
        otpService.generateAndSendOtp(testUser);

        verify(otpTokenRepository, times(2)).save(any(OtpToken.class));
    }

    // ===== verifyAndCompleteLogin Tests =====
    @Test
    void verifyAndCompleteLogin_validOtp_successfullyCompletesLogin() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwttoken123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userSessionRepository.save(any())).thenReturn(null);

        LoginResponse response = otpService.verifyAndCompleteLogin(verificationRequest);

        assertNotNull(response);
        assertEquals("jwttoken123", response.getToken());
        assertEquals("testuser", response.getUsername());
        verify(otpTokenRepository, times(1)).save(any(OtpToken.class));
        verify(userSessionRepository, times(1)).save(any());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void verifyAndCompleteLogin_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> otpService.verifyAndCompleteLogin(verificationRequest));
    }

    @Test
    void verifyAndCompleteLogin_noValidOtp_throwsInvalidOtpException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.empty());

        assertThrows(InvalidOtpException.class, () -> otpService.verifyAndCompleteLogin(verificationRequest));
    }

    @Test
    void verifyAndCompleteLogin_otpExpired_throwsOtpExpiredException() {
        testOtpToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));

        assertThrows(OtpExpiredException.class, () -> otpService.verifyAndCompleteLogin(verificationRequest));
    }

    @Test
    void verifyAndCompleteLogin_otpAlreadyUsed_throwsInvalidOtpException() {
        testOtpToken.setUsed(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));

        assertThrows(InvalidOtpException.class, () -> otpService.verifyAndCompleteLogin(verificationRequest));
    }

    @Test
    void verifyAndCompleteLogin_invalidOtpCode_incrementsAttemptCount() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidOtpException.class, () -> otpService.verifyAndCompleteLogin(verificationRequest));

        assertEquals(1, testOtpToken.getAttemptCount());
        verify(otpTokenRepository, times(1)).save(testOtpToken);
    }

    @Test
    void verifyAndCompleteLogin_invalidOtpCode_multipleAttempts() {
        testOtpToken.setAttemptCount(0);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // First failed attempt
        assertThrows(InvalidOtpException.class, () -> otpService.verifyAndCompleteLogin(verificationRequest));
        assertEquals(1, testOtpToken.getAttemptCount());

        // Second failed attempt
        assertThrows(InvalidOtpException.class, () -> otpService.verifyAndCompleteLogin(verificationRequest));
        assertEquals(2, testOtpToken.getAttemptCount());
    }

    @Test
    void verifyAndCompleteLogin_validOtp_marksAsUsed() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwttoken123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userSessionRepository.save(any())).thenReturn(null);

        otpService.verifyAndCompleteLogin(verificationRequest);

        assertTrue(testOtpToken.isUsed());
        verify(otpTokenRepository, times(1)).save(testOtpToken);
    }

    @Test
    void verifyAndCompleteLogin_validOtp_createsUserSession() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwttoken123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userSessionRepository.save(any())).thenReturn(null);

        LoginResponse response = otpService.verifyAndCompleteLogin(verificationRequest);

        assertNotNull(response);
        verify(userSessionRepository, times(1)).save(any());
    }

    @Test
    void verifyAndCompleteLogin_validOtp_updatesLastLoginTime() {
        LocalDateTime beforeLogin = LocalDateTime.now();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwttoken123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userSessionRepository.save(any())).thenReturn(null);

        otpService.verifyAndCompleteLogin(verificationRequest);

        assertNotNull(testUser.getLastLoginTime());
        assertTrue(testUser.getLastLoginTime().isAfter(beforeLogin));
    }

    @Test
    void verifyAndCompleteLogin_validOtp_returnsCorrectTokenInfo() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwttoken123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userSessionRepository.save(any())).thenReturn(null);

        LoginResponse response = otpService.verifyAndCompleteLogin(verificationRequest);

        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(1L, response.getUserId());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
    }

    @Test
    void verifyAndCompleteLogin_otpWithinExpiryWindow_succeeds() {
        // OTP expires at now + 1 second
        testOtpToken.setExpiresAt(LocalDateTime.now().plusSeconds(1));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwttoken123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userSessionRepository.save(any())).thenReturn(null);

        assertDoesNotThrow(() -> otpService.verifyAndCompleteLogin(verificationRequest));
    }

    @Test
    void verifyAndCompleteLogin_zeroAttemptCount_incrementsOnFail() {
        testOtpToken.setAttemptCount(0);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidOtpException.class, 
            () -> otpService.verifyAndCompleteLogin(verificationRequest));
        
        assertEquals(1, testOtpToken.getAttemptCount());
    }

    @Test
    void verifyAndCompleteLogin_twoFailedAttempts_incrementsCounter() {
        testOtpToken.setAttemptCount(2);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(testOtpToken));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidOtpException.class, 
            () -> otpService.verifyAndCompleteLogin(verificationRequest));
        
        assertEquals(3, testOtpToken.getAttemptCount());
    }
}
