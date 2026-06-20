package com.example.farmbiddingsystem.models;

public class UserModel {
    private int userId;
    private String userName;
    private String email;
    private String password;
    private String phNo;
    private String role; // 'admin', 'farmer', 'buyer'
    private String cnic;


    public UserModel(int userId, String userName, String email, String role) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.role = role;
    }

    // Example Getter
    public String getUserName() { return userName; }
}
