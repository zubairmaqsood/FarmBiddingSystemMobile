package com.example.farmbiddingsystem;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.farmbiddingsystem.network.ApiClient;
import com.example.farmbiddingsystem.network.ApiService;
import com.example.farmbiddingsystem.wrapperClasses.GenericResponse; // Ensure you have this wrapper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BidForm extends BottomSheetDialogFragment {

    private int auctionId;
    private String token;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bid_form, container, false);

        EditText etBidAmount = view.findViewById(R.id.etBidAmount);
        Button btnSubmitBid = view.findViewById(R.id.btnSubmitBid);
        TextView cropName = view.findViewById(R.id.cropName);
        TextView cropHighestBid = view.findViewById(R.id.cropHighestBid);

        if(getArguments() != null){
            String aucTitle = getArguments().getString("Auction Title");
            String aucHighestBid = getArguments().getString("Auction Price");
            auctionId = getArguments().getInt("Auction ID", -1);
            token = getArguments().getString("Token"); // Retrieve token

            cropName.setText(aucTitle);
            cropHighestBid.setText(aucHighestBid);
        }

        btnSubmitBid.setOnClickListener(v -> {
            String bidString = etBidAmount.getText().toString().trim();

            if (bidString.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            } else if (auctionId == -1 || token == null) {
                Toast.makeText(getContext(), "Authentication error. Please re-login.", Toast.LENGTH_SHORT).show();
            } else {
                btnSubmitBid.setEnabled(false);
                btnSubmitBid.setText("Submitting...");
                submitBidToServer(bidString, btnSubmitBid);
            }
        });

        return view;
    }

    private void submitBidToServer(String bidAmount, Button btnSubmitBid) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Pass "place_bid" as the action to match your PHP switch statement
        apiService.placeBid("Bearer " + token, "place_bid", auctionId, bidAmount).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse serverResponse = response.body();
                    if (serverResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Bid Placed Successfully!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed: " + serverResponse.getError(), Toast.LENGTH_LONG).show();
                        btnSubmitBid.setEnabled(true);
                        btnSubmitBid.setText("Submit Bid");
                    }
                } else {
                    Toast.makeText(getContext(), "Server rejected the bid.", Toast.LENGTH_LONG).show();
                    btnSubmitBid.setEnabled(true);
                    btnSubmitBid.setText("Submit Bid");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                Log.e("BID_FORM", "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network Error. Please try again.", Toast.LENGTH_LONG).show();
                btnSubmitBid.setEnabled(true);
                btnSubmitBid.setText("Submit Bid");
            }
        });
    }
}