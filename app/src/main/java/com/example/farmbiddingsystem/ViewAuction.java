package com.example.farmbiddingsystem;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViewAuction extends AppCompatActivity {

    ImageView detailImg, btnBack;

    TextView detailStatus,
            detailTitle,
            detailFarmer,
            detailLocation,
            detailPhone,
            detailQty,
            detailBidderName,
            detailHighestBid,
            detailBasePrice,
            detailTimer,
            detailDesc,
            detailBidCount;

    Button mainBidBtn;

    // --- NEW: Timer Engine Variables ---
    // We will set this dynamically when you connect the API, but here is a dummy date for now
    private String dbEndTime = "2026-06-25 12:00:00";
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_auction);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        detailImg = findViewById(R.id.detailImg);

        detailStatus = findViewById(R.id.detailStatus);
        detailTitle = findViewById(R.id.detailTitle);
        detailFarmer = findViewById(R.id.detailFarmer);
        detailLocation = findViewById(R.id.detailLocation);
        detailPhone = findViewById(R.id.detailPhone);
        detailQty = findViewById(R.id.detailQty);
        detailBidderName = findViewById(R.id.detailBidderName);
        detailHighestBid = findViewById(R.id.detailHighestBid);
        detailBasePrice = findViewById(R.id.detailBasePrice);
        detailTimer = findViewById(R.id.detailTimer);
        detailDesc = findViewById(R.id.detailDesc);
        detailBidCount = findViewById(R.id.detailBidCount);

        mainBidBtn = findViewById(R.id.mainBidBtn);

        // Set Data
        loadAuctionData();

        // --- NEW: Start the Ticker! ---
        timerHandler.post(timerRunnable);

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Place Bid Button
        mainBidBtn.setOnClickListener(v -> {
            Toast.makeText(
                    ViewAuction.this,
                    "Bid Placed Successfully",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void loadAuctionData() {
        detailStatus.setText("ACTIVE");
        detailTitle.setText("Fresh Onion");
        detailFarmer.setText("Sold by: Ali");
        detailLocation.setText("Location: Lahore");
        detailPhone.setText("Phone: 03001234567");
        detailQty.setText("Quantity: 100 KG");
        detailBidderName.setText("Highest Bidder: Ahmed");
        detailHighestBid.setText("Rs 5000");
        detailBasePrice.setText("Rs 3000");

        // I removed your static "02:12:45" text here because the Handler will now update it every second!

        detailDesc.setText(
                "Fresh farm onions available for bidding. "
                        + "These onions are organic and freshly harvested "
                        + "from the farm with premium quality."
        );
        detailBidCount.setText("Total Bids: 12");
        detailImg.setImageResource(R.drawable.onion);
    }

    // ==========================================
    // --- The Timer Logic Section ---
    // ==========================================

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateCountdownText();
            timerHandler.postDelayed(this, 1000); // Loop every 1 second
        }
    };

    private void updateCountdownText() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        try {
            java.util.Date endDate = sdf.parse(dbEndTime);
            if (endDate != null) {
                long diff = endDate.getTime() - System.currentTimeMillis();

                if (diff <= 0) {
                    detailTimer.setText("Auction Ended");
                    detailTimer.setTextColor(android.graphics.Color.GRAY);
                    timerHandler.removeCallbacks(timerRunnable); // Stop ticking
                } else {
                    long seconds = (diff / 1000) % 60;
                    long minutes = (diff / (1000 * 60)) % 60;
                    long hours = (diff / (1000 * 60 * 60)) % 24;
                    long days = diff / (1000 * 60 * 60 * 24);

                    StringBuilder sb = new StringBuilder("⏰ ");
                    if (days > 0) sb.append(days).append("d ");
                    if (hours > 0) sb.append(hours).append("h ");
                    sb.append(minutes).append("m ").append(seconds).append("s");

                    detailTimer.setText(sb.toString().trim());

                    // Color logic: Red if under 3 hours, Green otherwise
                    if (diff <= 3 * 60 * 60 * 1000) {
                        detailTimer.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                    } else {
                        detailTimer.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
                    }
                }
            }
        } catch (Exception e) {
            detailTimer.setText("Time Unknown");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Crucial: Stop the loop when the user leaves this page
        timerHandler.removeCallbacks(timerRunnable);
    }
}