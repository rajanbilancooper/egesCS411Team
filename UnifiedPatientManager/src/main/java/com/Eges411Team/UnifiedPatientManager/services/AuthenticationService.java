package com.Eges411Team.UnifiedPatientManager.services;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.LoginRequest;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.LoginResponse;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.entity.UserSession;
//import com.Eges411Team.UnifiedPatientManager.ExceptionHandlers.InvalidOtpException;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import com.Eges411Team.UnifiedPatientManager.services.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
//ASK RHEA IF NEED TRANSACTIONAL
public class AuthenticationService {

    //Dependencies
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    //NOT MADE YET private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    //Constructor for dependencies
    public AuthenticationService(UserRepository userRepository,
                                UserSessionRepository userSessionRepository,
                                OtpService otpService,
                                JwtTokenProvider jwtTokenProvider,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.otpService = otpService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }
    

    //WHAT IS THIS SERVIVC DOING? IT CHECKS IF THE USER EXISTS BY USING THEIR USERNAME AND PASSWORD
    //IF THEY EXIST, AN OTP IS SENT FOR VERIFICATION WOOP WOOP 

    public LoginResponse authenticate(LoginRequest request, String ipAddress) {
        
        // Step 1: Find user by username
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUsername()));
        
        // Step 2: Verify password
        // Compare plain text password with hashed password in database
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Password is wrong - handle failed login
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid username or password");
        }
        
        // Step 3: Password is correct - reset failed login attempts
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Step 4: Generate OTP and send to user
        otpService.generateAndSendOtp(user);
    }

    //NOW THEY ARE PASSED 2FA, they NEED A JWT TOKEN TO ACCESS RESOURCES
     private LoginResponse completeLogin(User user, String ipAddress) {
        
        // Step 1: Generate JWT token
        String token = jwtTokenProvider.generateToken(user);
        Long expiresIn = jwtTokenProvider.getExpirationTime();  // 24 hours
        
        // Step 2: Create session record in database
        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionToken(token);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        session.setIpAddress(ipAddress);  // Track where login occurred
        session.setIsActive(true);
        session.setLastActivityTime(LocalDateTime.now());
        
        userSessionRepository.save(session);
        
        // Step 3: Update user's last login timestamp
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        
        // Step 4: Build response with token and user info
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
    
    return response;
    }

    //GREAT, IF THEY HAVE GOTTEN HERE, THEY DO NOT HAVE ACCESS 
    //SO... INCREASE FAILED LOGIN ATTEMPTS? AND LOCK THEM OUT 

    private void handleFailedLogin(User user) {
        // Increment failed login counter
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        // Lock account if failed attempts reach 3
        if (user.getFailedLoginAttempts() >= 3) {
            user.setIsLocked(true);
        }
        
        userRepository.save(user);
        
    }
    public void logout(String token) {
        // Remove "Bearer " prefix if present
        String tokenWithoutBearer = token.replace("Bearer ", "");
        
        // Find active session with this token
        UserSession session = userSessionRepository
            .findBySessionTokenAndActive(tokenWithoutBearer, true)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or inactive session token"));
        // Deactivate the session (user is logged out)
        session.setIsActive(false);
        userSessionRepository.save(session);
    }
}   

