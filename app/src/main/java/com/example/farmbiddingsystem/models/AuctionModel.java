package com.example.farmbiddingsystem.models;

/**
 * AuctionModel — mirrors the auctions table + farmer name from users table.
 *
 * Database source:
 *   auctions  → auc_id, user_id, auc_title, auc_desc, image_path,
 *               auc_qty, base_price, auc_status, highest_bid,
 *               highest_bidder_id, start_time, end_time, bid_count
 *   users     → user_name (joined by auctions.user_id = users.user_id)
 *
 * Note: 'registry_file_name' from farmers table is not included here
 * because it is an admin/verification field, not displayed in the app UI.
 */
public class AuctionModel {

    // From auctions table
    private int    aucId;
    private int    userId;           // farmer's user_id (FK → farmers.user_id)
    private String aucTitle;
    private String aucDesc;
    private String imagePath;
    private double aucQty;
    private double basePrice;
    private String aucStatus;        // "Live" or "Expired"
    private double highestBid;
    private int    highestBidderId;  // FK → buyers.user_id (0 if no bids yet)
    private String startTime;        // DATETIME as String; format: "yyyy-MM-dd HH:mm:ss"
    private String endTime;          // DATETIME as String; format: "yyyy-MM-dd HH:mm:ss"
    private int    bidCount;

    // From users table (joined — farmer's display name)
    private String farmerName;       // users.user_name WHERE users.user_id = auctions.user_id


    // ─────────────────────────────────────────
    // Constructor — used when building from API/database response
    // ─────────────────────────────────────────
    public AuctionModel(int aucId, int userId, String aucTitle, String aucDesc,
                        String imagePath, double aucQty, double basePrice,
                        String aucStatus, double highestBid, int highestBidderId,
                        String startTime, String endTime, int bidCount,
                        String farmerName) {

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
    }

    // ─────────────────────────────────────────
    // Empty constructor — needed for JSON parsing libraries (Gson, Retrofit)
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


    // ─────────────────────────────────────────
    // Setters — needed when updating from live data (e.g. new bid placed)
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


    // ─────────────────────────────────────────
    // Helper — tells you if this auction is still live
    // ─────────────────────────────────────────
    public boolean isLive() {
        return "Live".equalsIgnoreCase(aucStatus);
    }

    // Helper — tells you if any bids have been placed
    public boolean hasBids() {
        return bidCount > 0;
    }

    // Helper — returns highest bid if exists, otherwise base price
    // Useful for displaying "current price" on the card
    public double getCurrentPrice() {
        return hasBids() ? highestBid : basePrice;
    }
}