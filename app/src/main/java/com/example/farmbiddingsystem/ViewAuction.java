package com.example.farmbiddingsystem;

import android.os.Bundle;
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

        detailTimer.setText("02:12:45");

        detailDesc.setText(
                "Fresh farm onions available for bidding. "
                        + "These onions are organic and freshly harvested "
                        + "from the farm with premium quality."
        );

        detailBidCount.setText("Total Bids: 12");

        // Set Image
        detailImg.setImageResource(R.drawable.onion);
    }
}