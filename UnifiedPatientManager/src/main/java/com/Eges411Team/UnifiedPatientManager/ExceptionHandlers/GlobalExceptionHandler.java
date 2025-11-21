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
// removed unused import AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

//Lets us log errrors 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Lets us use lists
import java.util.stream.Collectors;

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

    // Handle generic IllegalArgumentException (e.g. missing email)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage()));
    }

    // Handle email sending errors gracefully (e.g. SMTP misconfigured during dev/test)
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorResponse> handleMailException(MailException ex) {
        logger.error("Mail error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("Failed to send OTP email. Please check server email configuration or delivery logs."));
    }

    // Handle bean validation failures (@Valid annotated DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        String combined = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
        if (combined.isBlank()) {
            combined = "Validation error";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(combined));
    }

    // Handle ResponseStatusException (propagate its message cleanly instead of generic fallback)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        logger.warn("ResponseStatusException: status={}, reason={}", ex.getStatusCode(), ex.getReason());
        String msg = ex.getReason();
        if (msg == null || msg.isBlank()) {
            msg = ex.getMessage();
        }
        return ResponseEntity.status(ex.getStatusCode())
            .body(new ErrorResponse(msg));
    }

    // Fallback: any uncaught exception -> 500 with its message (if present)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Log the full exception class and stack trace for diagnostics
        logger.error("Unhandled exception occurred: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);

        // Prefer the exception message, but if it's null/blank return the exception class name so frontend sees something actionable
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = ex.getClass().getSimpleName();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(msg));
    }

}
    