package com.example.farmbiddingsystem; // Make sure this matches your package!

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BidsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bids, container, false);

        // 1. Find the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.bidsRecyclerView);

        // 2. Set it to a 2-Column Grid
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 3. Attach our Adapter (We are telling it to generate 6 dummy items for now)
        recyclerView.setAdapter(new BidsAdapter(6));

        return view;
    }

    // =====================================================================
    // THE ADAPTER: This loops through data and builds the UI cards
    // =====================================================================
    class BidsAdapter extends RecyclerView.Adapter<BidsAdapter.BidViewHolder> {

        private final int itemCount;

        BidsAdapter(int itemCount) {
            this.itemCount = itemCount;
        }

        @NonNull
        @Override
        public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // REUSING your existing item_auction.xml blueprint!
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auction, parent, false);
            return new BidViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
            // Later, when you add IDs to the TextViews in item_auction.xml,
            // you will use this space to change the text to "Rice", "Wheat", etc.
        }

        @Override
        public int getItemCount() {
            return itemCount; // Generates exactly 6 cards
        }

        class BidViewHolder extends RecyclerView.ViewHolder {
            BidViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}