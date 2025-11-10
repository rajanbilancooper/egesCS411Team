package com.Eges411Team.UnifiedPatientManager.ExceptionHandlers;
//MAYUBE NEED TO IMPORT OUR ERROR RESPONSE DTO HERE
//CALL IT ErrorResponse AND IMPORT FROM com.Eges411Team.UnifiedPatientManager.dto.ErrorResponse
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.ErrorResponse;

//Basic SpringBoot and Java Imports for Exception Handling (DONE FOR US)
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//Lets us log errrors 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Lets us use lists
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //Creates a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //Handle Spring Security BadCredentialsException
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Bad credentials attempt: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)  // 401
            .body(new ErrorResponse("Invalid username or password"));
    }

    //Handle Spring Security LockedException
     @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        logger.warn("Locked account access attempt: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)  // 403
            .body(new ErrorResponse("Your account has been locked. Please contact the administrator."));
    }

    // Handle Spring Security's DisabledException
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        logger.warn("Disabled account access attempt: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)  // 403
            .body(new ErrorResponse("Your account has been disabled. Please contact the administrator."));
    }

    // Handle Spring Security's UsernameNotFoundException
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        logger.warn("User not found: {}", ex.getMessage());
        
        // Return same message as BadCredentials for security (don't reveal if user exists)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)  // 401
            .body(new ErrorResponse("Invalid username or password"));
    }

      // Handle custom InvalidOtpException
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex) {
        logger.warn("Invalid OTP attempt: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)  // 400
            .body(new ErrorResponse(ex.getMessage()));
    }

    // Handle custom OtpExpiredException
    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpired(OtpExpiredException ex) {
        logger.warn("Expired OTP attempt: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)  // 400
            .body(new ErrorResponse("OTP has expired. Please request a new one."));
    }

}
    