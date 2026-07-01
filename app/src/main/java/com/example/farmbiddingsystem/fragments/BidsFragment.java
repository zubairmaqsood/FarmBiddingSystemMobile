package com.example.farmbiddingsystem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmbiddingsystem.BidForm;
import com.example.farmbiddingsystem.LoginActivity;
import com.example.farmbiddingsystem.R;
import com.example.farmbiddingsystem.ViewAuction;
import com.example.farmbiddingsystem.adapters.MyBidsAdapter;
import com.example.farmbiddingsystem.models.AuctionModel;
import com.example.farmbiddingsystem.utils.AuctionDataHolder;
import com.example.farmbiddingsystem.wrapperClasses.BidsResponse;
import com.example.farmbiddingsystem.network.ApiClient;
import com.example.farmbiddingsystem.network.ApiService;
import com.example.farmbiddingsystem.utils.SharedPrefManager; // <-- Import the Manager!

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BidsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvStatusMessage;
    private MyBidsAdapter myBidsAdapter;
    private Button myBidLogin;
    private ProgressBar loadingSpinner;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bids, container, false);

        recyclerView = view.findViewById(R.id.bidsRecyclerView);
        tvStatusMessage = view.findViewById(R.id.tvStatusMessage);
        myBidLogin = view.findViewById(R.id.my_bid_login_btn);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);

        myBidLogin.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), LoginActivity.class));
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchMyBids();

        return view;
    }

    private void fetchMyBids() {
        // 1. Initialize the Manager
        SharedPrefManager prefManager = new SharedPrefManager(requireActivity());

        // 2. Check if logged in the clean way
        if (!prefManager.isLoggedIn() || prefManager.getToken() == null) {
            // Pass TRUE to show the login button
            showErrorState("Please login to view your dashboard.", true);
            return;
        }

        // 3. Grab the Token and Role easily without worrying about keys!
        String token = prefManager.getToken();
        String role = prefManager.getRole() != null ? prefManager.getRole() : "buyer"; // Fallback just in case

        // Setup the Click Listener
        // Setup the Click Listener
        MyBidsAdapter.OnStatusClickListener clickListener = (auction) -> {
                // Buyer clicked "Update Bid"

            if ("farmer".equalsIgnoreCase(role)) {
                Toast.makeText(getContext(), "Farmers cannot place bids.", Toast.LENGTH_SHORT).show();
                return;
            }
                BidForm bidSheet = new BidForm();
                Bundle bundle = new Bundle();
                bundle.putString("Auction Title", "Crop Name: " + auction.getAucTitle());
                bundle.putString("Auction Price", "Current Highest Bid: Rs " + auction.getHighestBid());
                // CRITICAL: Pass the ID here so the BidForm knows which auction to update!
                bundle.putInt("Auction ID", auction.getAucId());
                bundle.putString("Token", token);
                bidSheet.setArguments(bundle);
                bidSheet.show(requireActivity().getSupportFragmentManager(), "BidForm");
        };

        // Initialize our new specialized adapter
        myBidsAdapter = new MyBidsAdapter(new ArrayList<>(), role, clickListener);
        recyclerView.setAdapter(myBidsAdapter);

        List<AuctionModel> cachedBids = AuctionDataHolder.getInstance().getMyBidsList();
        if (cachedBids != null && !cachedBids.isEmpty()) {
            // Load instantly from memory without showing loading spinner
            showDataState();
            myBidsAdapter.updateList(cachedBids);
            return; // STOP EXECUTION HERE, DO NOT CALL API
        }

        showLoadingState();

        // Fetch Data from Vercel using the secure token
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getMyBids("Bearer " + token).enqueue(new Callback<BidsResponse>() {
            @Override
            public void onResponse(@NonNull Call<BidsResponse> call, @NonNull Response<BidsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BidsResponse serverResponse = response.body();

                    if (serverResponse.isSuccess()) {
                        List<AuctionModel> myBids = serverResponse.getData();
                        if (myBids == null || myBids.isEmpty()) {
                            showErrorState("You have no active listings or bids yet.",false);
                        } else {
                            showDataState();
                            myBidsAdapter.updateList(myBids);
                            // SAVE TO CACHE FOR NEXT TIME
                            AuctionDataHolder.getInstance().setMyBidsList(myBids);
                        }
                    } else {
                        showErrorState(serverResponse.getError(),false);
                    }
                } else {
                    showErrorState("Failed to load data. Please try again.",false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BidsResponse> call, @NonNull Throwable t) {
                Log.e("BIDS_API", "Network Error: " + t.getMessage());
                showErrorState("Server unreachable. Check your internet connection.",false);
            }
        });
    }

    private void showLoadingState() {
        recyclerView.setVisibility(View.GONE);
        tvStatusMessage.setVisibility(View.GONE);
        myBidLogin.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.VISIBLE);
    }

    private void showErrorState(String message, boolean showLoginButton) {
        loadingSpinner.setVisibility(View.GONE); // Hide spinner
        recyclerView.setVisibility(View.GONE);
        tvStatusMessage.setVisibility(View.VISIBLE);
        tvStatusMessage.setText(message);
        myBidLogin.setVisibility(showLoginButton ? View.VISIBLE : View.GONE);
    }

    private void showDataState() {
        loadingSpinner.setVisibility(View.GONE); // Hide spinner
        tvStatusMessage.setVisibility(View.GONE);
        myBidLogin.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}