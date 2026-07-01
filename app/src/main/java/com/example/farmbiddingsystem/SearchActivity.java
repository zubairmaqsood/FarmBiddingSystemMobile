package com.example.farmbiddingsystem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmbiddingsystem.adapters.SearchAdapter;
import com.example.farmbiddingsystem.models.AuctionModel;
import com.example.farmbiddingsystem.utils.AuctionDataHolder; // Import the bridge
import com.example.farmbiddingsystem.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private TextInputLayout tilSearchBox;
    private TextInputEditText etRealSearch;
    private RecyclerView recyclerSearchResults;
    private TextView tvSearchStatus;

    private SearchAdapter searchAdapter;

    private final Handler tickerHandler = new Handler(android.os.Looper.getMainLooper());
    private Runnable tickerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 1. Find the views
        tilSearchBox = findViewById(R.id.tilSearchBox);
        etRealSearch = findViewById(R.id.etRealSearch);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        tvSearchStatus = findViewById(R.id.tvSearchStatus);

        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this,2));

        SearchAdapter.OnAuctionClickListener clickListener = new SearchAdapter.OnAuctionClickListener() {
            @Override
            public void onBidClick(AuctionModel auction) {
                SharedPrefManager prefManager = new SharedPrefManager(SearchActivity.this);
                String token = prefManager.getToken();
                String role = prefManager.getRole();

                // Optional extra UI check (even though PHP will block it, this prevents the form from opening)
                if ("farmer".equalsIgnoreCase(role)) {
                    Toast.makeText(SearchActivity.this, "Farmers cannot place bids.", Toast.LENGTH_SHORT).show();
                    return;
                }
                BidForm bidSheet = new BidForm();
                Bundle bundle = new Bundle();
                bundle.putString("Auction Title", "Crop Name: " + auction.getAucTitle());
                bundle.putString("Auction Price", "Current Highest Bid: Rs " + auction.getCurrentPrice());
                bundle.putInt("Auction ID", auction.getAucId());
                bundle.putString("Token", token);
                bidSheet.setArguments(bundle);
                bidSheet.show(getSupportFragmentManager(), "BidForm");
            }

            @Override
            public void onViewDetailsClick(AuctionModel auction) {
                Intent intent = new Intent(SearchActivity.this, ViewAuction.class);
                // PASS THE ID SO THE API CAN FETCH THE FULL DETAILS
                intent.putExtra("auc_id", auction.getAucId());
                // Pass enough info to show the image/title instantly
                intent.putExtra("title", auction.getAucTitle());
                intent.putExtra("image_url", auction.getImagePath());
                intent.putExtra("end_time", auction.getEndTime());
                startActivity(intent);
            }
        };

        // 2. GRAB THE DATA INSTANTLY FROM THE BRIDGE (Zero API calls)
        List<AuctionModel> arrivedAuctions = AuctionDataHolder.getInstance().getMasterList();

        searchAdapter = new SearchAdapter(arrivedAuctions, clickListener);
        recyclerSearchResults.setAdapter(searchAdapter);

        // If the vault is empty (maybe the user opened search before homepage loaded), let them know
        if (arrivedAuctions.isEmpty()) {
            tvSearchStatus.setVisibility(View.VISIBLE);
            tvSearchStatus.setText("No live auctions available to search.");
        } else {
            tvSearchStatus.setVisibility(View.GONE);
        }

        // 3. UI Flow Magic (Back Arrow & Keyboard)
        tilSearchBox.setStartIconOnClickListener(v -> finish());

        etRealSearch.requestFocus();
        etRealSearch.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etRealSearch, InputMethodManager.SHOW_IMPLICIT);
        });

        // 4. INSTANT LOCAL FILTERING
        etRealSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                searchAdapter.filter(query);

                if (searchAdapter.getItemCount() == 0 && !query.isEmpty()) {
                    tvSearchStatus.setVisibility(View.VISIBLE);
                    tvSearchStatus.setText("No crops found matching '" + query + "'");
                } else if (arrivedAuctions.isEmpty()) {
                    tvSearchStatus.setVisibility(View.VISIBLE);
                    tvSearchStatus.setText("No live auctions available to search.");
                } else {
                    tvSearchStatus.setVisibility(View.GONE);
                }
            }
        });

        tickerRunnable = new Runnable() {
            @Override
            public void run() {
                if (searchAdapter != null && searchAdapter.getItemCount() > 0) {
                    // Send the "TICK_UPDATE" payload to every visible item on the screen
                    searchAdapter.notifyItemRangeChanged(0, searchAdapter.getItemCount(), "TICK_UPDATE");
                }
                // Loop this again in 1000 milliseconds (1 second)
                tickerHandler.postDelayed(this, 1000);
            }
        };
        // Start the heartbeat!
        tickerHandler.post(tickerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etRealSearch.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Kill the heartbeat when the activity dies so it doesn't drain the battery
        if (tickerRunnable != null) {
            tickerHandler.removeCallbacks(tickerRunnable);
        }
    }
}