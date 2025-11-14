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
import com.Eges411Team.UnifiedPatientManager.services.JwtTokenProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
//ASK RHEA IF NEED TRANSACTIONAL
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    

    // Constructor for dependencies
    public OtpService(OtpTokenRepository otpTokenRepository,
                     UserRepository userRepository,
                     UserSessionRepository userSessionRepository,
                     SmsService smsService,
                     PasswordEncoder passwordEncoder,
                     JwtTokenProvider jwtTokenProvider) {
        this.otpTokenRepository = otpTokenRepository;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.smsService = smsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //LETS CREATE A METHOD TO GENERATE AND SEND OTP
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

        //Actually send the OTP via SMS
        smsService.sendOtpSms(user.getPhoneNumber(), otpCode); 

    }
    //Now they have the OTP, they inpyt it... so need to verify its legit 
    public LoginResponse verifyAndCompleteLogin(OtpVerificationRequest request) {
        
        //1: Find user by username
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUsername()));

        //2: Retrieve the most recent valid OTP for this user
         OtpToken otpToken = otpTokenRepository
            .findValidOtp(user.getId())
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

        //Make One Time Passwword 
        private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); //6 digit code, needs to be at least 100000
        return String.valueOf(code);
    }


}
