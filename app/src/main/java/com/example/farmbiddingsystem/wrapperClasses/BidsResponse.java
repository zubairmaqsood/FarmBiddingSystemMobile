package com.example.farmbiddingsystem.wrapperClasses;

import com.example.farmbiddingsystem.models.AuctionModel;

import java.util.List;

public class BidsResponse {
    private boolean success;
    private List<AuctionModel> data; // Matches the "data" array from PHP!
    private String error;

    // Getters
    public boolean isSuccess() { return success; }
    public List<AuctionModel> getData() { return data; }
    public String getError() { return error; }
}
