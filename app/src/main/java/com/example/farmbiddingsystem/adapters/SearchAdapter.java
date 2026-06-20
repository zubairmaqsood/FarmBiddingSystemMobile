package com.example.farmbiddingsystem.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmbiddingsystem.R;
import com.example.farmbiddingsystem.models.AuctionModel;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<AuctionModel> masterList; // The full downloaded list
    private List<AuctionModel> currentDisplayList; // What the search is showing
    private OnAuctionClickListener listener;

    public interface OnAuctionClickListener {
        void onBidClick(AuctionModel auction);
        void onViewDetailsClick(AuctionModel auction);
    }

    public SearchAdapter(List<AuctionModel> masterList, OnAuctionClickListener listener) {
        this.masterList = masterList;
        this.currentDisplayList = new ArrayList<>(masterList);
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return currentDisplayList.size();
    }

    // --- SMOOTH SEARCH FILTER ---
    public void filter(String text) {
        List<AuctionModel> newFilteredList = new ArrayList<>();

        if (text.isEmpty()) {
            newFilteredList.addAll(masterList);
        } else {
            text = text.toLowerCase().trim();
            for (AuctionModel item : masterList) {
                if (item.getAucTitle().toLowerCase().contains(text)) {
                    newFilteredList.add(item);
                }
            }
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return currentDisplayList.size(); }

            @Override
            public int getNewListSize() { return newFilteredList.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return currentDisplayList.get(oldPos).getAucId() == newFilteredList.get(newPos).getAucId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return currentDisplayList.get(oldPos).getCurrentPrice() == newFilteredList.get(newPos).getCurrentPrice();
            }
        });

        currentDisplayList.clear();
        currentDisplayList.addAll(newFilteredList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_auction, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuctionModel auction = currentDisplayList.get(position);

        holder.title.setText(auction.getAucTitle());
        holder.price.setText("Rs " + auction.getCurrentPrice());
        holder.quantity.setText(auction.getAucQty() + " KG");
        holder.bidsPlaced.setText(auction.getBidCount() + " Bids Placed");

        updateTimerText(holder, auction);

        holder.bidBtn.setOnClickListener(v -> {
            if (listener != null) listener.onBidClick(auction);
        });

        holder.viewDetailsBtn.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetailsClick(auction);
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0).equals("TICK_UPDATE")) {
            AuctionModel auction = currentDisplayList.get(position);
            updateTimerText(holder, auction);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    private void updateTimerText(ViewHolder holder, AuctionModel auction) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        try {
            java.util.Date endDate = sdf.parse(auction.getEndTime());
            if (endDate != null) {
                long diff = endDate.getTime() - System.currentTimeMillis();
                holder.timeLeft.setText(formatTime(diff));

                if (diff > 0 && diff <= 3 * 60 * 60 * 1000) {
                    holder.timeLeft.setTextColor(Color.parseColor("#D32F2F"));
                } else if (diff > 0) {
                    holder.timeLeft.setTextColor(Color.parseColor("#2E7D32"));
                } else {
                    holder.timeLeft.setTextColor(Color.parseColor("#757575"));
                }
            }
        } catch (Exception e) {
            holder.timeLeft.setText("Invalid Date");
        }
    }

    private String formatTime(long ms) {
        if (ms <= 0) return "Expired";
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, quantity, bidsPlaced, timeLeft;
        Button bidBtn, viewDetailsBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.auctionTitle);
            price = itemView.findViewById(R.id.auctionPrice);
            quantity = itemView.findViewById(R.id.auctionQty);
            bidsPlaced = itemView.findViewById(R.id.auctionBidsPlaced);
            timeLeft = itemView.findViewById(R.id.auctionTimeLeft);
            bidBtn = itemView.findViewById(R.id.btnBidNow);
            viewDetailsBtn = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}