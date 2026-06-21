package com.example.farmbiddingsystem.wrapperClasses;

public class GenericResponse {

    private boolean success;
    private String message;
    private String error;

    // --- GETTERS ---

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}