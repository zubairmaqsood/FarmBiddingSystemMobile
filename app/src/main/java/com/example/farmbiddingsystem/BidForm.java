package com.example.farmbiddingsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BidForm extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bid_form, container, false);

        EditText etBidAmount = view.findViewById(R.id.etBidAmount);
        Button btnSubmitBid = view.findViewById(R.id.btnSubmitBid);

        btnSubmitBid.setOnClickListener(v -> {
            String bid = etBidAmount.getText().toString();

            if (bid.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Bid of Rs " + bid + " placed!", Toast.LENGTH_SHORT).show();


                dismiss(); // This slides the sheet back down and closes it!
            }
        });

        return view;
    }
}