package com.Eges411Team.UnifiedPatientManager.DTOs.responses;

public class LoginResponse {
    
    private String token; //JWT Token Field
    private String tokenType = "Bearer"; //Token type field with default value
    private Long expiresIn; //Token expiration time field

    private Long userId; //User ID field
    private String username; //Username field
    private String role; //User role field
    private String firstName; //User first name field
    private String lastName; //User last name field

    //Constructor for LoginResponse class
    public LoginResponse() {} //Default constructor
    public LoginResponse(String token, Long expiresIn, Long userId, String username, String role, String firstName, String lastName) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.username = username;   
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    //Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


}

