package loginTests;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests OTP failure and resend flow:
 * - User submits incorrect OTP
 * - User clicks resend (new OTP is issued)
 * - User attempts to use old OTP (should be rejected)
 * - Old OTP is marked used/invalid, no session is created
 */
public class LoginOtpResendTest {

    @Test
    public void incorrectThenResend_thenOldOtpRejected_andNoSessionCreated() {
        // Arrange
        OtpTokenRepository otpTokenRepository = Mockito.mock(OtpTokenRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserSessionRepository userSessionRepository = Mockito.mock(UserSessionRepository.class);
        EmailService emailService = Mockito.mock(EmailService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);

        OtpService otpService = new OtpService(
                otpTokenRepository,
                userRepository,
                userSessionRepository,
                emailService,
                passwordEncoder,
                jwtTokenProvider
        );

        User user = new User();
        user.setId(1L);
        user.setUsername("rajanbilancooper");
        user.setEmail("rajan@example.com");
        Mockito.when(userRepository.findByUsername("rajanbilancooper")).thenReturn(Optional.of(user));

        // Prepare two OTP tokens: original (token1) and resent (token2)
        AtomicReference<OtpToken> token1Ref = new AtomicReference<>();
        AtomicReference<OtpToken> token2Ref = new AtomicReference<>();

        // Make passwordEncoder.encode return two different hashed values for successive generates
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("hashed1", "hashed2");

        // Save behavior: first save returns token1, second save returns token2; also capture refs
        Mockito.when(otpTokenRepository.save(Mockito.any(OtpToken.class))).thenAnswer(inv -> {
            OtpToken t = inv.getArgument(0);
            if (token1Ref.get() == null) {
                token1Ref.set(t);
                return t;
            } else {
                token2Ref.set(t);
                return t;
            }
        });

        // invalidateUnusedOtps should mark the previous token as used
        Mockito.doAnswer(inv -> {
            if (token1Ref.get() != null) token1Ref.get().setUsed(true);
            return null;
        }).when(otpTokenRepository).invalidateUnusedOtps(Mockito.anyLong());

        // findFirstBy... should return the most recent unused token (token1 if not used, else token2)
        Mockito.when(otpTokenRepository.findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                Mockito.anyLong(), Mockito.any(LocalDateTime.class)))
                .thenAnswer(inv -> {
                    if (token1Ref.get() != null && !token1Ref.get().isUsed()) return Optional.of(token1Ref.get());
                    if (token2Ref.get() != null && !token2Ref.get().isUsed()) return Optional.of(token2Ref.get());
                    return Optional.empty();
                });

        // Setup matches: define oldCorrect=111111, newCorrect=222222
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.eq("hashed1"))).thenAnswer(inv -> {
            String raw = inv.getArgument(0);
            return "111111".equals(raw);
        });
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.eq("hashed2"))).thenAnswer(inv -> {
            String raw = inv.getArgument(0);
            return "222222".equals(raw);
        });

        // Act: generate first OTP (initial send)
        otpService.generateAndSendOtp(user);
        OtpToken token1 = token1Ref.get();
        assertTrue(token1 != null && !token1.isUsed());

        // User enters incorrect OTP (not 111111)
        OtpVerificationRequest wrongReq = new OtpVerificationRequest("rajanbilancooper", "000000", "192.0.2.1");
        assertThrows(com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.InvalidOtpException.class,
                () -> otpService.verifyAndCompleteLogin(wrongReq));
        // token1 attemptCount should increment
        assertEquals(1, token1.getAttemptCount());

        // User clicks resend -> generate new OTP; invalidateUnusedOtps will mark token1 used
        otpService.generateAndSendOtp(user);
        OtpToken token2 = token2Ref.get();
        assertTrue(token1.isUsed(), "Old token should be marked used after resend");
        assertTrue(token2 != null && !token2.isUsed());

        // User attempts to use the old (original) correct OTP value (111111)
        OtpVerificationRequest oldOtpReq = new OtpVerificationRequest("rajanbilancooper", "111111", "192.0.2.1");
        // Because token1 is now used, the service will pick token2; matching will fail and throw InvalidOtpException
        assertThrows(com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.InvalidOtpException.class,
                () -> otpService.verifyAndCompleteLogin(oldOtpReq));

        // token2 attemptCount should have incremented
        assertEquals(1, token2.getAttemptCount());

        // No user session should be created
        Mockito.verify(userSessionRepository, Mockito.never()).save(Mockito.any());
    }
}
