package com.example.farmbiddingsystem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bids, container, false);

        recyclerView = view.findViewById(R.id.bidsRecyclerView);
        tvStatusMessage = view.findViewById(R.id.tvStatusMessage);
        myBidLogin = view.findViewById(R.id.my_bid_login_btn);

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
        MyBidsAdapter.OnStatusClickListener clickListener = (auction, userRole) -> {
            if ("buyer".equalsIgnoreCase(userRole)) {
                // Buyer clicked "Update Bid" -> Open the Bottom Sheet Form
                BidForm bidSheet = new BidForm();
                Bundle bundle = new Bundle();
                bundle.putString("Auction Title", "Crop Name: " + auction.getAucTitle());
                bundle.putString("Auction Price", "Current Highest Bid: Rs " + auction.getHighestBid());
                bidSheet.setArguments(bundle);
                bidSheet.show(requireActivity().getSupportFragmentManager(), "BidForm");
            } else {
                // Farmer clicked "View Details" -> Open Full View Activity
                Intent intent = new Intent(requireActivity(), ViewAuction.class);
                // intent.putExtra("auction_id", auction.getAucId());
                startActivity(intent);
            }
        };

        // Initialize our new specialized adapter
        myBidsAdapter = new MyBidsAdapter(new ArrayList<>(), role, clickListener);
        recyclerView.setAdapter(myBidsAdapter);

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

    private void showErrorState(String message, boolean showLoginButton) {
        recyclerView.setVisibility(View.GONE);
        tvStatusMessage.setVisibility(View.VISIBLE);
        tvStatusMessage.setText(message);

        // Only show the button if we explicitly tell it to!
        if (showLoginButton) {
            myBidLogin.setVisibility(View.VISIBLE);
        } else {
            myBidLogin.setVisibility(View.GONE);
        }
    }

    private void showDataState() {
        tvStatusMessage.setVisibility(View.GONE);
        myBidLogin.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}