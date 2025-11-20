package com.Eges411Team.UnifiedPatientManager.services;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.LoginResponse;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.OtpVerificationRequest;
import com.Eges411Team.UnifiedPatientManager.entity.OtpToken;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.entity.UserSession;
import com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.InvalidOtpException;
import com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.OtpExpiredException;
import com.Eges411Team.UnifiedPatientManager.repositories.OtpTokenRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final EmailService EmailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    

    // Constructor for dependencies
    public OtpService(OtpTokenRepository otpTokenRepository,
                     UserRepository userRepository,
                     UserSessionRepository userSessionRepository,
                     EmailService EmailService,
                     PasswordEncoder passwordEncoder,
                     JwtTokenProvider jwtTokenProvider) {
        this.otpTokenRepository = otpTokenRepository;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.EmailService = EmailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //LETS CREATE A METHOD TO GENERATE AND SEND OTP
    // Ensure OTP save is not rolled back if email sending fails; this helps local testing
    @Transactional(noRollbackFor = MailException.class)
    public void generateAndSendOtp(User user) {
        // Step 1: Invalidate any existing unused OTPs for this user/type
        // This ensures only ONE valid OTP exists at a time
        otpTokenRepository.invalidateUnusedOtps(user.getId());

        // Step 2: Generate random 6-digit code
    String otpCode = generateRandomCode();

        // Step 3: Hash the OTP before storing in database for security
        String hashedOtp = passwordEncoder.encode(otpCode);

        // Step 4: Create OTP token entity
        OtpToken otpToken = new OtpToken();
        otpToken.setUser(user);
        otpToken.setOtpCode(hashedOtp);  // Store hashed version
        otpToken.setCreatedAt(LocalDateTime.now());
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));  // Expires in 5 minutes
        otpToken.setUsed(false);
        otpToken.setAttemptCount(0);

        // Step 5: Save OTP token to database
    otpTokenRepository.save(otpToken);

        // Validate email exists before attempting send
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalStateException("User does not have an email address configured");
        }

        // Log OTP for local development/testing (remove or lower log level in production)
        logger.info("Generated OTP for user {}: {}", user.getUsername(), otpCode);
        // Actually attempt to send the OTP via Email but do not roll back the db save on failure
        try {
            EmailService.sendOtpEmail(user.getEmail(), otpCode);
        } catch (MailException me) {
            // Log but do not rethrow so that the OTP stays persisted and can be used for testing
            logger.error("Failed to send OTP email to {}: {}", user.getEmail(), me.getMessage());
        }

    }
    //Now they have the OTP, they inpyt it... so need to verify its legit 
    @Transactional
    public LoginResponse verifyAndCompleteLogin(OtpVerificationRequest request) {
        
        
       
        
        //1: Find user by username
        User user = userRepository.findByUsername(request.getUsername())
           .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUsername()));

        //2: Retrieve the most recent valid OTP for this user
            OtpToken otpToken = otpTokenRepository
                .findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(user.getId(), LocalDateTime.now())
                .orElseThrow(() -> new InvalidOtpException("No valid OTP found for this user"));

        //3: Check if OTP is expired
        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired");
        }

        //4: Check iof thje OTP has already been used 
        if (otpToken.isUsed()) {
            throw new InvalidOtpException("OTP has already been used");
        }

        //5: Verify the provided OTP code matches the stored hashed code
        if (!passwordEncoder.matches(request.getOtpCode(), otpToken.getOtpCode())) {
            // Increment attempt count
            otpToken.setAttemptCount(otpToken.getAttemptCount() + 1);
            otpTokenRepository.save(otpToken);
            throw new InvalidOtpException("Invalid OTP code. Attempts remaining: " + 
                                        (3 - otpToken.getAttemptCount()));
        }

        //6: Mark OTP as used
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        //7: Complete login and generate JWT token
        String token = jwtTokenProvider.generateToken(user);
        Long expiresIn = jwtTokenProvider.getExpirationTime();

        // 8 Create session record in database
        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionToken(token);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        session.setIpAddress(request.getIpAddress());
        session.setIsActive(true);
        session.setLastActivityTime(LocalDateTime.now());
        userSessionRepository.save(session);
        
        //9: Update user's last login date
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        //10: Build response with token and user info
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        return response;
    }
        //MAYBE FIGURE OUT HOW TO RESEND OTPs IF TIME PREVAILS 

        //Make One Time Password using SecureRandom
        private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); //6 digit code
        return String.valueOf(code);
    }


}
