package com.example.farmbiddingsystem.models;

public class FarmerModel extends UserModel {
    private String registryFileName;
    private String city;
    private String farmLocation;
    private double farmSize;

    public FarmerModel(int userId, String userName, String email, String role,
                  String city, String farmLocation, double farmSize) {

        super(userId, userName, email, role);

        this.city = city;
        this.farmLocation = farmLocation;
        this.farmSize = farmSize;
    }

    // Getters and Setters for farm details...
}
