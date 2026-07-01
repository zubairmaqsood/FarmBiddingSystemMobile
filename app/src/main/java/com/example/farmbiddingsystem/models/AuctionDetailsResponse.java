package com.example.farmbiddingsystem.models;

import com.google.gson.annotations.SerializedName;

public class AuctionDetailsResponse {
    @SerializedName("auc_title") private String aucTitle;
    @SerializedName("auc_desc") private String aucDesc;
    @SerializedName("image_path") private String imagePath;
    @SerializedName("auc_qty") private String aucQty;
    @SerializedName("base_price") private String basePrice;
    @SerializedName("auc_status") private String aucStatus;
    @SerializedName("highest_bid") private String highestBid;
    @SerializedName("end_time") private String endTime;
    @SerializedName("bid_count") private String bidCount;
    @SerializedName("city") private String city;
    @SerializedName("farm_location") private String farmLocation;
    @SerializedName("farmer_name") private String farmerName;
    @SerializedName("ph_no") private String phone;
    @SerializedName("highest_bidder_name") private String highestBidderName;
    @SerializedName("error") private String error;

    // Getters
    public String getAucTitle() { return aucTitle; }
    public String getAucDesc() { return aucDesc; }
    public String getImagePath() { return imagePath; }
    public String getAucQty() { return aucQty; }
    public String getBasePrice() { return basePrice; }
    public String getAucStatus() { return aucStatus; }
    public String getHighestBid() { return highestBid; }
    public String getEndTime() { return endTime; }
    public String getBidCount() { return bidCount; }
    public String getCity() { return city; }
    public String getFarmLocation() { return farmLocation; }
    public String getFarmerName() { return farmerName; }
    public String getPhone() { return phone; }
    public String getHighestBidderName() { return highestBidderName; }
    public String getError() { return error; }
}