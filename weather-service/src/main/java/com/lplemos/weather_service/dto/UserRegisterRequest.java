package com.lplemos.weather_service.dto;

import com.lplemos.weather_service.model.UserRole;

public class UserRegisterRequest {
    private String username;
    private String password;
    private UserRole role;

    public UserRegisterRequest() {}

    public UserRegisterRequest(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
} 