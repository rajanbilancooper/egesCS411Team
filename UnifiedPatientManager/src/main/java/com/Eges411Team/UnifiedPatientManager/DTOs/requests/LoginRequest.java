package com.Eges411Team.UnifiedPatientManager.DTOs.requests;

//Imports NotBlank annotation from Jakarta Validation to ensure fields are not empty
import jakarta.validation.constraints.NotBlank; 


public class LoginRequest {
    @NotBlank(message = "Username is required") //Ensures username is not blank
    private String username;

    @NotBlank(message = "Password is required") //Ensures password is not blank
    private String password;

    private boolean rememberMe; //Field to indicate if the user wants to be remembered  

    //Constructors 
    public LoginRequest() {} //Default constructor

    //Parameterized constructor
    public LoginRequest(String username, String password, boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }

     // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
