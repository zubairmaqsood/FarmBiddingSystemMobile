package com.example.farmbiddingsystem.models;

import com.google.gson.annotations.SerializedName;

/**
 * AuctionModel — mirrors the auctions table + farmer name from users table.
 *
 * This single model handles responses from both homepage.php and myBid.php!
 * If an API doesn't send a specific field (like my_bid), Gson safely ignores it.
 */
public class AuctionModel {

    // From auctions table
    @SerializedName("auc_id")
    private int aucId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("auc_title")
    private String aucTitle;

    @SerializedName("auc_desc")
    private String aucDesc;

    @SerializedName("image_path")
    private String imagePath;

    @SerializedName("auc_qty")
    private double aucQty;

    @SerializedName("base_price")
    private double basePrice;

    @SerializedName("auc_status")
    private String aucStatus;

    @SerializedName("highest_bid")
    private double highestBid;

    @SerializedName("highest_bidder_id")
    private int highestBidderId;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("bid_count")
    private int bidCount;

    // From users table (joined — farmer's display name)
    @SerializedName("farmer_name")
    private String farmerName;

    // --- NEW: Specifically for the Buyer's "My Bids" screen ---
    @SerializedName("my_bid")
    private double myBid;


    // ─────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────
    public AuctionModel(int aucId, int userId, String aucTitle, String aucDesc,
                        String imagePath, double aucQty, double basePrice,
                        String aucStatus, double highestBid, int highestBidderId,
                        String startTime, String endTime, int bidCount,
                        String farmerName, double myBid) {

        this.aucId           = aucId;
        this.userId          = userId;
        this.aucTitle        = aucTitle;
        this.aucDesc         = aucDesc;
        this.imagePath       = imagePath;
        this.aucQty          = aucQty;
        this.basePrice       = basePrice;
        this.aucStatus       = aucStatus;
        this.highestBid      = highestBid;
        this.highestBidderId = highestBidderId;
        this.startTime       = startTime;
        this.endTime         = endTime;
        this.bidCount        = bidCount;
        this.farmerName      = farmerName;
        this.myBid           = myBid;
    }

    // ─────────────────────────────────────────
    // Empty constructor — needed for Gson / Retrofit
    // ─────────────────────────────────────────
    public AuctionModel() {}


    // ─────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────
    public int    getAucId()           { return aucId; }
    public int    getUserId()          { return userId; }
    public String getAucTitle()        { return aucTitle; }
    public String getAucDesc()         { return aucDesc; }
    public String getImagePath()       { return imagePath; }
    public double getAucQty()          { return aucQty; }
    public double getBasePrice()       { return basePrice; }
    public String getAucStatus()       { return aucStatus; }
    public double getHighestBid()      { return highestBid; }
    public int    getHighestBidderId() { return highestBidderId; }
    public String getStartTime()       { return startTime; }
    public String getEndTime()         { return endTime; }
    public int    getBidCount()        { return bidCount; }
    public String getFarmerName()      { return farmerName; }
    public double getMyBid()           { return myBid; } // New Getter!


    // ─────────────────────────────────────────
    // Setters
    // ─────────────────────────────────────────
    public void setAucId(int aucId)                   { this.aucId = aucId; }
    public void setUserId(int userId)                 { this.userId = userId; }
    public void setAucTitle(String aucTitle)          { this.aucTitle = aucTitle; }
    public void setAucDesc(String aucDesc)            { this.aucDesc = aucDesc; }
    public void setImagePath(String imagePath)        { this.imagePath = imagePath; }
    public void setAucQty(double aucQty)              { this.aucQty = aucQty; }
    public void setBasePrice(double basePrice)        { this.basePrice = basePrice; }
    public void setAucStatus(String aucStatus)        { this.aucStatus = aucStatus; }
    public void setHighestBid(double highestBid)      { this.highestBid = highestBid; }
    public void setHighestBidderId(int id)            { this.highestBidderId = id; }
    public void setStartTime(String startTime)        { this.startTime = startTime; }
    public void setEndTime(String endTime)            { this.endTime = endTime; }
    public void setBidCount(int bidCount)             { this.bidCount = bidCount; }
    public void setFarmerName(String farmerName)      { this.farmerName = farmerName; }
    public void setMyBid(double myBid)                { this.myBid = myBid; } // New Setter!


    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────
    public boolean isLive() {
        return "Live".equalsIgnoreCase(aucStatus);
    }

    public boolean hasBids() {
        return bidCount > 0;
    }

    public double getCurrentPrice() {
        return hasBids() ? highestBid : basePrice;
    }
}