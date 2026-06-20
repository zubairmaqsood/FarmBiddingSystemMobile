package com.example.farmbiddingsystem.models;

public class BuyerModel extends UserModel {
    private String documentPath;
    private String buyerType; // 'Individual', 'Wholesaler', etc.
    private String companyName;
    private String companyAddress;
    private String companyType;

    public BuyerModel(int userId, String userName, String email, String role,
                 String buyerType, String companyName) {

        super(userId, userName, email, role);

        this.buyerType = buyerType;
        this.companyName = companyName;
    }

    // Getters and Setters...
}
