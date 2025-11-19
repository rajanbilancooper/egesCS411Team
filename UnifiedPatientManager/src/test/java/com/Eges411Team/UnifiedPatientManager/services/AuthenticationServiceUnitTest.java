package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.LoginRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.LoginResponse;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceUnitTest {

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

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(10L);
        user.setUsername("dr_smith_123");
        user.setPassword("hashed");
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
    }

    @Test
    void authenticate_success_sendsOtpAndReturnsBasicResponse() {
        LoginRequest req = new LoginRequest("dr_smith_123", "GreatPasswordUsage", false);
        when(userRepository.findByUsername("dr_smith_123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("GreatPasswordUsage", "hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> (User) inv.getArgument(0));

        LoginResponse response = authenticationService.authenticate(req, "127.0.0.1");

        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getUsername()).isEqualTo("dr_smith_123");
        assertThat(response.getToken()).isNull(); // Token only after OTP verification
        verify(otpService).generateAndSendOtp(user);
    }

    @Test
    void authenticate_userNotFound_throws() {
        LoginRequest req = new LoginRequest("missing_user", "pw", false);
        when(userRepository.findByUsername("missing_user")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(req, "127.0.0.1"));
        verify(passwordEncoder, never()).matches(any(), any());
        verifyNoInteractions(otpService);
    }

    @Test
    void authenticate_badPassword_incrementsFailedAttemptsAndThrows() {
        LoginRequest req = new LoginRequest("dr_smith_123", "WrongPassword", false);
        when(userRepository.findByUsername("dr_smith_123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> (User) inv.getArgument(0));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
            authenticationService.authenticate(req, "127.0.0.1")
        );
        assertThat(ex.getMessage()).contains("Invalid username or password");
        verify(userRepository).save(user); // failed attempts incremented
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        verifyNoInteractions(otpService);
    }

    @Test
    void authenticate_lockedUser_throwsLockedException() {
        user.setIsLocked(true);
        LoginRequest req = new LoginRequest("dr_smith_123", "GreatPasswordUsage", false);
        when(userRepository.findByUsername("dr_smith_123")).thenReturn(Optional.of(user));

        assertThrows(org.springframework.security.authentication.LockedException.class, () ->
            authenticationService.authenticate(req, "127.0.0.1")
        );
        verify(passwordEncoder, never()).matches(any(), any());
        verifyNoInteractions(otpService);
    }
}
