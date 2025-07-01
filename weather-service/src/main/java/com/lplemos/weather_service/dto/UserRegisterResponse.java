package com.lplemos.weather_service.dto;

public class UserRegisterResponse {
    private String username;
    private String role;
    private String message;

    public UserRegisterResponse() {}

    public UserRegisterResponse(String username, String role, String message) {
        this.username = username;
        this.role = role;
        this.message = message;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
} 