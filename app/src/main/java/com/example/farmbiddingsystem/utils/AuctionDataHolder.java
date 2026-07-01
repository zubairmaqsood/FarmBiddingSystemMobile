package com.example.farmbiddingsystem.utils;

import com.example.farmbiddingsystem.models.AuctionModel;
import java.util.ArrayList;
import java.util.List;

public class AuctionDataHolder {
    private static AuctionDataHolder instance;

    // Store data for both screens
    private List<AuctionModel> masterList = new ArrayList<>();
    private List<AuctionModel> myBidsList = new ArrayList<>();

    private AuctionDataHolder() {}

    public static synchronized AuctionDataHolder getInstance() {
        if (instance == null) {
            instance = new AuctionDataHolder();
        }
        return instance;
    }

    // --- Home Screen Data ---
    public List<AuctionModel> getMasterList() {
        return masterList;
    }

    public void setMasterList(List<AuctionModel> masterList) {
        this.masterList = masterList;
    }

    // --- My Bids Data ---
    public List<AuctionModel> getMyBidsList() {
        return myBidsList;
    }

    public void setMyBidsList(List<AuctionModel> myBidsList) {
        this.myBidsList = myBidsList;
    }

    // Call this when the user logs out!
    public void clearAllData() {
        masterList.clear();
        myBidsList.clear();
    }
}