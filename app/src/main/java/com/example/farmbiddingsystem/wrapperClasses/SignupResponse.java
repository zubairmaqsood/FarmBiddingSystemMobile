package com.example.farmbiddingsystem.wrapperClasses;

public class SignupResponse {
    private boolean success;
    private String message;
    private String error; // For catching PHP exceptions
    private String token;
    private String role;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getError() { return error; }
    public String getToken() { return token; }
    public String getRole() { return role; }
}