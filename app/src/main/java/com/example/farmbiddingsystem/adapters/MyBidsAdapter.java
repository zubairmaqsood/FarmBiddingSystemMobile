package com.example.farmbiddingsystem.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.farmbiddingsystem.R;
import com.example.farmbiddingsystem.ViewAuction;
import com.example.farmbiddingsystem.models.AuctionModel;
import java.util.List;

public class MyBidsAdapter extends RecyclerView.Adapter<MyBidsAdapter.ViewHolder> {

    private List<AuctionModel> auctionList;
    private String userRole; // "buyer" or "farmer"
    private OnStatusClickListener listener;

    // Interface to send clicks back to the Fragment
    public interface OnStatusClickListener {
        void onActionClick(AuctionModel auction);
    }

    public MyBidsAdapter(List<AuctionModel> auctionList, String userRole, OnStatusClickListener listener) {
        this.auctionList = auctionList;
        this.userRole = userRole;
        this.listener = listener;
    }

    public void updateList(List<AuctionModel> newList) {
        this.auctionList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auction_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuctionModel auction = auctionList.get(position);

        // ── Universal Data ──
        holder.tvTitle.setText(auction.getAucTitle());
        holder.tvQty.setText("Qty: " + auction.getAucQty() + " kg");
        holder.tvEndTime.setText("Ends: " + auction.getEndTime());
        holder.tvHighestBid.setText("Highest Bid: Rs " + auction.getHighestBid());
        holder.tvStatus.setText(auction.getAucStatus());

        // ✅ FIX: Load the actual crop image (was previously unbound)
        Glide.with(holder.itemView.getContext())
                .load(auction.getImagePath())
                .into(holder.imgCrop);

        // ── Status Badge Color ──
        if ("expired".equalsIgnoreCase(auction.getAucStatus())) {
            holder.tvStatus.setTextColor(0xFFD32F2F); // Red
        } else {
            holder.tvStatus.setTextColor(0xFF388E3C); // Green
        }

        // ── ROLE-BASED UI LOGIC ──
        if ("buyer".equalsIgnoreCase(userRole)) {
            // What the Buyer sees: Keep the button visible
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.tvDynamicInfo.setText("My Bid: Rs " + auction.getMyBid());
            holder.btnAction.setText("Update Bid");

            // Click listener for Buyer action
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActionClick(auction);
                }
            });
        } else {
            // What the Farmer sees: Completely remove the action button from the layout flow
            holder.btnAction.setVisibility(View.GONE);
            holder.tvDynamicInfo.setText("Total Bids Placed: " + auction.getBidCount());
        }

        // ── View Details Button (Available to both roles) ──
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ViewAuction.class);

            // FIX: Changed "auction_id" to "auc_id" to match exactly what ViewAuction.java reads!
            intent.putExtra("auc_id", auction.getAucId());

            // Optional: Pass these to let ViewAuction load image & title instantly without layout shifts
            intent.putExtra("title", auction.getAucTitle());
            intent.putExtra("image_url", auction.getImagePath());
            intent.putExtra("end_time", auction.getEndTime());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return auctionList == null ? 0 : auctionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCrop;
        TextView tvTitle, tvQty, tvHighestBid, tvDynamicInfo, tvEndTime, tvStatus;
        Button btnViewDetails, btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCrop = itemView.findViewById(R.id.imgCrop);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvHighestBid = itemView.findViewById(R.id.tvHighestBid);
            tvDynamicInfo = itemView.findViewById(R.id.tvDynamicInfo);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}