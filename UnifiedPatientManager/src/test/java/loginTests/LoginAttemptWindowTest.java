package loginTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.LoginRequest;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import com.Eges411Team.UnifiedPatientManager.services.AuthenticationService;
import com.Eges411Team.UnifiedPatientManager.services.JwtTokenProvider;
import com.Eges411Team.UnifiedPatientManager.services.OtpService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case: User inputs four invalid sign in attempts within the span of five minutes.
 *
 * The test simulates repeated wrong-password attempts from the same IP and asserts the
 * account becomes locked (rejected) after repeated failures.
 */
public class LoginAttemptWindowTest {

    @Test
    public void fourInvalidAttemptsWithinWindow_shouldLockAccount() {
        // Arrange - mock dependencies
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserSessionRepository userSessionRepository = Mockito.mock(UserSessionRepository.class);
        OtpService otpService = Mockito.mock(OtpService.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        AuthenticationService auth = new AuthenticationService(
                userRepository,
                userSessionRepository,
                otpService,
                jwtTokenProvider,
                passwordEncoder
        );

        // Create a registered user fixture
        User user = new User();
        user.setId(1L);
        user.setUsername("rajanbilancooper");
    user.setPassword("hashed");
        user.setFailedLoginAttempts(0);
        user.setIsLocked(false);

        Mockito.when(userRepository.findByUsername("rajanbilancooper")).thenReturn(Optional.of(user));
        // Save should return the passed user and allow the in-memory instance to be updated
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Always treat the provided passwords as incorrect
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        String ip = "192.0.2.1"; // sample source IP (same for all attempts)
        String[] attemptedPasswords = {"rajanpassword1", "rajanpassword2", "rajanpassword3", "rajanpassword4"};

        // Act & Assert
        for (int i = 0; i < attemptedPasswords.length; i++) {
            LoginRequest req = new LoginRequest("rajanbilancooper", attemptedPasswords[i], false);

            if (i < 2) {
                // first two attempts: bad credentials expected, account still not locked
                assertThrows(BadCredentialsException.class, () -> auth.authenticate(req, ip), "Expected BadCredentialsException on attempt " + (i + 1));
                assertFalse(user.getIsLocked(), "User should not be locked yet after attempt " + (i + 1));
            } else if (i == 2) {
                // third attempt: still throws BadCredentialsException, but implementation sets lock when attempts >= 3
                assertThrows(BadCredentialsException.class, () -> auth.authenticate(req, ip), "Expected BadCredentialsException on attempt " + (i + 1));
                assertTrue(user.getIsLocked(), "User should be locked after third failed attempt (implementation threshold)");
            } else {
                // fourth attempt: account already locked, should throw LockedException immediately
                assertThrows(LockedException.class, () -> auth.authenticate(req, ip), "Expected LockedException on attempt " + (i + 1));
                assertTrue(user.getIsLocked(), "User should remain locked after further attempts");
            }
        }
    }
}
