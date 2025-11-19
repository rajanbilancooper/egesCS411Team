package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.OtpVerificationRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.LoginResponse;
import com.Eges411Team.UnifiedPatientManager.entity.OtpToken;
import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.entity.UserSession;
import com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.InvalidOtpException;
import com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.OtpExpiredException;
import com.Eges411Team.UnifiedPatientManager.repositories.OtpTokenRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceUnitTest {

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

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(5L);
        user.setUsername("dr_smith_123");
        user.setEmail("dr@example.com");
        user.setRole(Role.DOCTOR);
    }

    @Test
    void generateAndSendOtp_success_savesTokenAndEmailsCode() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(inv -> (OtpToken) inv.getArgument(0));

        otpService.generateAndSendOtp(user);

        verify(otpTokenRepository).invalidateUnusedOtps(5L);
        ArgumentCaptor<OtpToken> tokenCaptor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpTokenRepository).save(tokenCaptor.capture());
        OtpToken saved = tokenCaptor.getValue();
        assertThat(saved.getOtpCode()).isEqualTo("hashedOtp");
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getAttemptCount()).isZero();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(emailService).sendOtpEmail(eq("dr@example.com"), anyString());
    }

    @Test
    void generateAndSendOtp_missingEmail_throws() {
        user.setEmail(null);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(inv -> (OtpToken) inv.getArgument(0));
        assertThrows(IllegalStateException.class, () -> otpService.generateAndSendOtp(user));
        verify(emailService, never()).sendOtpEmail(any(), any());
    }

    private OtpToken buildValidToken() {
        OtpToken token = new OtpToken();
        token.setUser(user);
        token.setOtpCode("hashedOtp");
        token.setCreatedAt(LocalDateTime.now().minusSeconds(10));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);
        token.setAttemptCount(0);
        return token;
    }

    @Test
    void verifyAndCompleteLogin_success_marksUsedAndReturnsResponse() {
        OtpToken token = buildValidToken();
        OtpVerificationRequest req = new OtpVerificationRequest("dr_smith_123", "847293", "127.0.0.1");
        when(userRepository.findByUsername("dr_smith_123")).thenReturn(Optional.of(user));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(eq(5L), any(LocalDateTime.class)))
            .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("847293", "hashedOtp")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400L);
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(inv -> (OtpToken) inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> (User) inv.getArgument(0));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(inv -> (UserSession) inv.getArgument(0));

        LoginResponse resp = otpService.verifyAndCompleteLogin(req);
        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getUsername()).isEqualTo("dr_smith_123");
        assertThat(token.isUsed()).isTrue();
        verify(passwordEncoder).matches("847293", "hashedOtp");
    }

    @Test
    void verifyAndCompleteLogin_userNotFound_throws() {
        OtpVerificationRequest req = new OtpVerificationRequest("missing", "123456", "ip");
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> otpService.verifyAndCompleteLogin(req));
    }

    @Test
    void verifyAndCompleteLogin_noToken_throwsInvalidOtp() {
        OtpVerificationRequest req = new OtpVerificationRequest("dr_smith_123", "123456", "ip");
        when(userRepository.findByUsername("dr_smith_123")).thenReturn(Optional.of(user));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(eq(5L), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());
        assertThrows(InvalidOtpException.class, () -> otpService.verifyAndCompleteLogin(req));
    }

    @Test
    void verifyAndCompleteLogin_expiredToken_throws() {
        OtpToken token = buildValidToken();
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        OtpVerificationRequest req = new OtpVerificationRequest("dr_smith_123", "123456", "ip");
        when(userRepository.findByUsername("dr_smith_123")).thenReturn(Optional.of(user));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(eq(5L), any(LocalDateTime.class)))
            .thenReturn(Optional.of(token));
        assertThrows(OtpExpiredException.class, () -> otpService.verifyAndCompleteLogin(req));
    }

    @Test
    void verifyAndCompleteLogin_usedToken_throws() {
        OtpToken token = buildValidToken();
        token.setUsed(true);
        OtpVerificationRequest req = new OtpVerificationRequest("dr_smith_123", "123456", "ip");
        when(userRepository.findByUsername("dr_smith_123")).thenReturn(Optional.of(user));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(eq(5L), any(LocalDateTime.class)))
            .thenReturn(Optional.of(token));
        assertThrows(InvalidOtpException.class, () -> otpService.verifyAndCompleteLogin(req));
    }

    @Test
    void verifyAndCompleteLogin_invalidCode_incrementsAttemptAndThrows() {
        OtpToken token = buildValidToken();
        OtpVerificationRequest req = new OtpVerificationRequest("dr_smith_123", "000000", "ip");
        when(userRepository.findByUsername("dr_smith_123")).thenReturn(Optional.of(user));
        when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(eq(5L), any(LocalDateTime.class)))
            .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("000000", "hashedOtp")).thenReturn(false);
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(inv -> (OtpToken) inv.getArgument(0));

        InvalidOtpException ex = assertThrows(InvalidOtpException.class, () -> otpService.verifyAndCompleteLogin(req));
        assertThat(token.getAttemptCount()).isEqualTo(1);
        assertThat(ex.getMessage()).contains("Invalid OTP code");
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
