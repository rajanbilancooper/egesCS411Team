package com.Eges411Team.UnifiedPatientManager.ExceptionHandlers;

//Imports AuthenticationException from Spring Security for handling authentication errors
import org.springframework.security.core.AuthenticationException;

public class InvalidOtpException extends AuthenticationException{
    //Constructor with message
    public InvalidOtpException(String message) {
        super(message);
    }
    //Constructor with message AND cause
    public InvalidOtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
