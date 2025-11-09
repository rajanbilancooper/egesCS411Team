//Package declaration for the ErrorResponse class
package com.Eges411Team.UnifiedPatientManager.DTOs.responses;

//Import lists and dateTime
import java.time.LocalDateTime;
import java.util.List;


public class ErrorResponse {

    private String message; //Error MEssage Field 
    private LocalDateTime timestamp; //Timeof error occurrence
    private List<String> errors; //Detailed list of the errors 

    //Constructor for ErrorResponse class
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    //Overloaded constructor to include error details
    public ErrorResponse(String message, List<String> errors) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    //Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    int a = 1;

}
