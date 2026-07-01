package com.example.farmbiddingsystem;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.farmbiddingsystem.models.AuctionDetailsResponse;
import com.example.farmbiddingsystem.network.ApiClient;
import com.example.farmbiddingsystem.network.ApiService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAuction extends AppCompatActivity {

    private TextView detailTitle, detailDesc, detailQty, detailHighestBid;
    private TextView detailBasePrice, detailTimer, detailBidCount, detailStatus;
    private TextView detailFarmer, detailLocation, detailPhone, detailBidderName;
    private ImageView detailImg, btnBack;
    private Button mainBidBtn;

    private int auctionId;
    private String endTimeString;
    private String currentHighestBid = "0"; // Store to pass to BidForm
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_auction);

        initViews();

        detailFarmer.setVisibility(View.GONE);
        detailLocation.setVisibility(View.GONE);
        detailPhone.setVisibility(View.GONE);
        detailPhone.setVisibility(View.GONE);
        detailBidderName.setVisibility(View.GONE);

        // 1. INSTANT LOAD: Load basic data from Intent immediately
        auctionId = getIntent().getIntExtra("auc_id", -1);
        String initialTitle = getIntent().getStringExtra("title");
        String initialImage = getIntent().getStringExtra("image_url");
        endTimeString = getIntent().getStringExtra("end_time");

        detailTitle.setText(initialTitle);
        if (initialImage != null && !initialImage.isEmpty()) {
            Glide.with(this).load(initialImage).placeholder(R.drawable.onion).into(detailImg);
        }

        // Start timer using intent data while API loads
        if (endTimeString != null) {
            timerHandler.post(timerRunnable);
        }

        btnBack.setOnClickListener(v -> finish());
        mainBidBtn.setOnClickListener(v -> openBidForm());

        // 2. FETCH DETAILS: Call API to get Farmer, Phone, and live bids
        if (auctionId != -1) {
            fetchFullAuctionDetails();
        } else {
            Toast.makeText(this, "Error: Auction ID missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        detailImg = findViewById(R.id.detailImg);
        detailStatus = findViewById(R.id.detailStatus);
        detailTitle = findViewById(R.id.detailTitle);
        detailQty = findViewById(R.id.detailQty);
        detailHighestBid = findViewById(R.id.detailHighestBid);
        detailBasePrice = findViewById(R.id.detailBasePrice);
        detailTimer = findViewById(R.id.detailTimer);
        detailDesc = findViewById(R.id.detailDesc);
        detailBidCount = findViewById(R.id.detailBidCount);
        detailFarmer = findViewById(R.id.detailFarmer);
        detailLocation = findViewById(R.id.detailLocation);
        detailPhone = findViewById(R.id.detailPhone);
        detailBidderName = findViewById(R.id.detailBidderName);
        mainBidBtn = findViewById(R.id.mainBidBtn);
    }

    private void fetchFullAuctionDetails() {
        detailStatus.setText("Loading...");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAuctionDetails(auctionId).enqueue(new Callback<AuctionDetailsResponse>() {
            @Override
            public void onResponse(Call<AuctionDetailsResponse> call, Response<AuctionDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuctionDetailsResponse details = response.body();

                    detailDesc.setText(details.getAucDesc());
                    detailQty.setText("Quantity: " + details.getAucQty() + " KG");
                    detailBasePrice.setText("Rs " + details.getBasePrice());
                    detailBidCount.setText("Total Bids: " + details.getBidCount());
                    detailHighestBid.setText("Rs " + details.getHighestBid());

                    // --- NEW STATUS COLOR CODE GOES RIGHT HERE ---
                    detailStatus.setText(details.getAucStatus());
                    if ("Live".equalsIgnoreCase(details.getAucStatus())) {
                        detailStatus.setBackgroundColor(Color.parseColor("#2E7D32")); // Green background
                        detailStatus.setTextColor(Color.WHITE); // White text
                    } else {
                        detailStatus.setBackgroundColor(Color.parseColor("#FBC02D")); // Yellow background
                        detailStatus.setTextColor(Color.BLACK); // Black text
                    }
                    // ---------------------------------------------

                    detailFarmer.setText("Sold by: " + details.getFarmerName());
                    detailFarmer.setVisibility(View.VISIBLE);

                    detailLocation.setText("Location: " + details.getCity() + ", " + details.getFarmLocation());
                    detailLocation.setVisibility(View.VISIBLE);

                    detailPhone.setText("Phone: " + details.getPhone());
                    detailPhone.setVisibility(View.VISIBLE);

                    if (details.getHighestBidderName() != null) {
                        detailBidderName.setText("Highest Bidder: " + details.getHighestBidderName());
                        detailBidderName.setVisibility(View.VISIBLE);
                    }
                    else{
                        detailBidderName.setText("No Higest Bidder yet.");
                        detailBidderName.setVisibility(View.VISIBLE);
                    }
                    // Update timer target just in case it was extended/changed
                    endTimeString = details.getEndTime();
                }
            }

            @Override
            public void onFailure(Call<AuctionDetailsResponse> call, Throwable t) {
                Log.e("VIEW_AUCTION", "Failed to fetch details: " + t.getMessage());
                Toast.makeText(ViewAuction.this, "Failed to load full details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openBidForm() {
        BidForm bidSheet = new BidForm();
        Bundle bundle = new Bundle();
        bundle.putString("Auction Title", "Crop Name: " + detailTitle.getText().toString());
        bundle.putString("Auction Price", "Current Highest Bid: Rs " + currentHighestBid);

        // Pass the actual ID to the BidForm so the PHP script knows WHICH auction to update!
        bundle.putInt("Auction ID", auctionId);

        bidSheet.setArguments(bundle);
        bidSheet.show(getSupportFragmentManager(), "BidForm");
    }

    // --- REAL-TIME TICKER LOGIC ---
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimerUI();
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void updateTimerUI() {
        if (endTimeString == null || endTimeString.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date endDate = sdf.parse(endTimeString);
            if (endDate != null) {
                long diff = endDate.getTime() - System.currentTimeMillis();

                if (diff <= 0) {
                    // Item has expired: Update all UI to reflect the end state
                    detailTimer.setText("Expired");
                    detailTimer.setTextColor(Color.parseColor("#757575")); // Grey
                    detailStatus.setText("EXPIRED"); // Hardcode expired here since the timer detected it
                    detailStatus.setBackgroundColor(Color.parseColor("#E0E0E0")); // Grey background
                    mainBidBtn.setEnabled(false);
                    mainBidBtn.setText("Auction Ended");
                    mainBidBtn.setBackgroundColor(Color.parseColor("#9E9E9E"));
                    timerHandler.removeCallbacks(timerRunnable);
                } else {
                    // Item is still live: ONLY update the timer text and color
                    detailTimer.setText(formatTime(diff));

                    if (diff <= 3 * 60 * 60 * 1000) {
                        detailTimer.setTextColor(Color.parseColor("#D32F2F")); // Red if ending soon
                    } else {
                        detailTimer.setTextColor(Color.parseColor("#2E7D32")); // Green if live
                    }

                    // We REMOVED the detailStatus.setText("ACTIVE") line from here.
                    // Now, whatever your fetchFullAuctionDetails() sets (e.g., "Live", "Pending") will stay on the screen!
                }
            }
        } catch (Exception e) {
            detailTimer.setText("Invalid Date");
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private String formatTime(long ms) {
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000 * 60)) % 60;
        long hours = (ms / (1000 * 60 * 60)) % 24;
        long days = ms / (1000 * 60 * 60 * 24);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m ").append(seconds).append("s");

        return sb.toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}