package com.example.farmbiddingsystem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmbiddingsystem.adapters.AuctionAdapter;
import com.example.farmbiddingsystem.models.AuctionModel;
import com.example.farmbiddingsystem.network.ApiClient;
import com.example.farmbiddingsystem.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private TextInputLayout tilSearchBox;
    private TextInputEditText etRealSearch;
    private RecyclerView recyclerSearchResults;
    private TextView tvSearchStatus;

    private AuctionAdapter searchAdapter;

    // --- DEBOUNCING TOOLS ---
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private Call<List<AuctionModel>> currentApiCall; // Used to cancel old requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        // 1. Find the views
        tilSearchBox = findViewById(R.id.tilSearchBox);
        etRealSearch = findViewById(R.id.etRealSearch);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        tvSearchStatus = findViewById(R.id.tvSearchStatus);

        // 2. Setup the RecyclerView & Adapter
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));

        AuctionAdapter.OnAuctionClickListener clickListener = new AuctionAdapter.OnAuctionClickListener() {
            @Override
            public void onBidClick(AuctionModel auction) {
                BidForm bidSheet = new BidForm();
                Bundle bundle = new Bundle();
                bundle.putString("Auction Title", "Crop Name: " + auction.getAucTitle());
                bundle.putString("Auction Price", "Current Highest Bid: Rs " + auction.getCurrentPrice());
                bidSheet.setArguments(bundle);
                bidSheet.show(getSupportFragmentManager(), "BidForm");
            }

            @Override
            public void onViewDetailsClick(AuctionModel auction) {
                Intent intent = new Intent(SearchActivity.this, ViewAuction.class);
                startActivity(intent);
            }
        };

        searchAdapter = new AuctionAdapter(new ArrayList<>(), clickListener);
        recyclerSearchResults.setAdapter(searchAdapter);

        // 3. UI Flow Magic (Back Arrow & Keyboard)
        tilSearchBox.setStartIconOnClickListener(v -> finish());

        etRealSearch.requestFocus();
        etRealSearch.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etRealSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // 4. THE DEBOUNCED SEARCH LOGIC
        etRealSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                // Step A: Cancel the previous timer if the user keeps typing
                if (debounceRunnable != null) {
                    debounceHandler.removeCallbacks(debounceRunnable);
                }

                // Step B: Define what happens when the timer finally finishes
                debounceRunnable = () -> {
                    if (query.isEmpty()) {
                        clearSearch("Start typing to search...");
                    } else {
                        performSearch(query);
                    }
                };

                // Step C: Start a 500-millisecond countdown timer
                debounceHandler.postDelayed(debounceRunnable, 500);
            }
        });
    }

    private void performSearch(String query) {
        // 1. Cancel any slow, older API calls that might still be flying through the internet
        if (currentApiCall != null && !currentApiCall.isExecuted()) {
            currentApiCall.cancel();
        }

        // 2. Show Loading State
        recyclerSearchResults.setVisibility(View.GONE);
        tvSearchStatus.setVisibility(View.VISIBLE);
        tvSearchStatus.setText("Searching for '" + query + "'...");

        // 3. Fire the API Request
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        currentApiCall = apiService.searchAuctions(query);

        currentApiCall.enqueue(new Callback<List<AuctionModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<AuctionModel>> call, @NonNull Response<List<AuctionModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AuctionModel> results = response.body();

                    if (results.isEmpty()) {
                        clearSearch("No crops found matching '" + query + "'");
                    } else {
                        // Success! Show the list.
                        tvSearchStatus.setVisibility(View.GONE);
                        recyclerSearchResults.setVisibility(View.VISIBLE);
                        searchAdapter.updateList(results);
                    }
                } else {
                    clearSearch("Server error. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AuctionModel>> call, @NonNull Throwable t) {
                // If the user typed a new letter and we explicitly cancelled this call, ignore the failure
                if (!call.isCanceled()) {
                    Log.e("SEARCH_API", "Error: " + t.getMessage());
                    clearSearch("Network error. Check connection.");
                }
            }
        });
    }

    // Helper method to reset the UI safely
    private void clearSearch(String message) {
        searchAdapter.updateList(new ArrayList<>());
        recyclerSearchResults.setVisibility(View.GONE);
        tvSearchStatus.setVisibility(View.VISIBLE);
        tvSearchStatus.setText(message);
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etRealSearch.getWindowToken(), 0);
        }
    }
}