// src/main/java/com/bd/dto/LoginRequest.java
package com.bd.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username; // accepts either numeric id or holderName

    @NotBlank(message = "Password is required")
    private String password; // accepted but not checked

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}