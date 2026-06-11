package com.example.farmbiddingsystem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.farmbiddingsystem.BidForm;
import com.example.farmbiddingsystem.R;
import com.example.farmbiddingsystem.SearchActivity;
import com.example.farmbiddingsystem.ViewAuction;
import com.example.farmbiddingsystem.adapters.AuctionAdapter;
import com.example.farmbiddingsystem.models.AuctionModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HomeFragment extends Fragment {

    // --- MASTER DATA & UI LISTS ---
    private List<AuctionModel> allAuctions = new ArrayList<>();

    // We don't store the regular/ending lists here anymore.
    // We generate them fresh every second and hand them to the Adapters via DiffUtil!
    private AuctionAdapter regularAdapter;
    private AuctionAdapter endingAdapter;

    // --- REAL-TIME TICKER ---
    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private View layoutEndingSoonHeader;
    private RecyclerView rvEnding;
    private RecyclerView rvLive;

    // --- CAROUSEL ---
    private ViewPager2 viewPager2;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. CAROUSEL SETUP
        viewPager2 = view.findViewById(R.id.bannerCarousel);
        List<Integer> images = Arrays.asList(
                R.drawable.main_pic1, R.drawable.main_pic2,
                R.drawable.main_pic3, R.drawable.main_pic4
        );
        viewPager2.setAdapter(new BannerAdapter(images));
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        // 2. UI INITIALIZATION
        rvLive = view.findViewById(R.id.rvLiveAuctions);
        rvEnding = view.findViewById(R.id.rvEndingAuctions);
        layoutEndingSoonHeader = view.findViewById(R.id.layoutEndingSoonHeader);

        // 3. LOAD MASTER DATA (Dummy Data - Use dates in the future so you can see it work!)
        allAuctions.clear();
        // Ends in exactly 24 Hours (June 12, 2:18 PM)
        allAuctions.add(new AuctionModel(1, 101, "Tomorrow's Wheat", "Safely in the Live section.", "",
                100.0, 1000.0, "Live", 0.0, 0,
                "2026-06-10 10:00:00", "2026-06-12 14:18:00", 0, "Farmer Ali"));

        // Ends in exactly 5 Hours (June 11, 7:18 PM)
        allAuctions.add(new AuctionModel(2, 102, "Evening Corn", "Safely in the Live section.", "",
                200.0, 2000.0, "Live", 2100.0, 301,
                "2026-06-10 10:00:00", "2026-06-11 19:18:00", 1, "Farmer Usman"));


        // ==========================================
        // CASE 2: ENDING SOON AUCTIONS (< 3 Hours)
        // ==========================================

        // Ends in exactly 2 Hours (June 11, 4:18 PM)
        allAuctions.add(new AuctionModel(3, 103, "Afternoon Rice", "Already in Ending Soon.", "",
                300.0, 3000.0, "Live", 3500.0, 302,
                "2026-06-10 10:00:00", "2026-06-11 16:18:00", 4, "Farmer Raza"));

        // Ends in exactly 5 Minutes (June 11, 2:23 PM) -> Quick expiration test!
        allAuctions.add(new AuctionModel(4, 104, "Expiring Onion", "Watch this expire and disappear!", "",
                400.0, 4000.0, "Live", 4000.0, 0,
                "2026-06-10 10:00:00", "2026-06-11 14:23:00", 0, "Farmer Tariq"));


        // ==========================================
        // CASE 3: THE SWAP TESTER (3 Hours + 10 Seconds)
        // ==========================================

        // Ends exactly at 5:33:00 PM today.
        // Since it is currently around 2:31 PM, this auction has about 3 hours and 2 minutes left.
        // It will start in the top "Live Auctions" list with a green timer.
        // Watch it tick down. The exact second it hits "03h 00m 00s",
        // it will magically jump to the bottom "Ending Soon" list and turn red!
        allAuctions.add(new AuctionModel(5, 105, "The Swap Test", "Watch this jump to the bottom!", "",
                500.0, 5000.0, "Live", 5500.0, 305,
                "2026-06-10 10:00:00", "2026-06-11 17:33:00", 2, "Farmer Ahmed"));

        // 4. COMMON CLICK LISTENER
        AuctionAdapter.OnAuctionClickListener commonClickListener = new AuctionAdapter.OnAuctionClickListener() {
            @Override
            public void onBidClick(AuctionModel auction) {
                BidForm bidSheet = new BidForm();
                Bundle bundle = new Bundle();
                bundle.putString("Auction Title", "Crop Name: " + auction.getAucTitle());
                bundle.putString("Auction Price", "Current Highest Bid: Rs " + auction.getCurrentPrice());
                bidSheet.setArguments(bundle);
                bidSheet.show(requireActivity().getSupportFragmentManager(), "BidForm");
            }

            @Override
            public void onViewDetailsClick(AuctionModel auction) {
                Intent intent = new Intent(requireActivity(), ViewAuction.class);
                startActivity(intent);
            }
        };

        // 5. ADAPTER SETUP (Start with empty lists, the Ticker will instantly fill them)
        regularAdapter = new AuctionAdapter(new ArrayList<>(), commonClickListener);
        rvLive.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLive.setAdapter(regularAdapter);

        endingAdapter = new AuctionAdapter(new ArrayList<>(), commonClickListener);
        rvEnding.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvEnding.setAdapter(endingAdapter);

        // 6. SEARCH BAR SETUP
        EditText editSearch = view.findViewById(R.id.editSearch);
        editSearch.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SearchActivity.class);
            startActivity(intent);
        });

        // 7. START REAL-TIME TICKER
        updateAuctionsRealTime();
        tickerHandler.postDelayed(tickerRunnable, 1000);

        return view;
    }

    // --- REAL-TIME LOOP LOGIC ---
    private final Runnable tickerRunnable = new Runnable() {
        @Override
        public void run() {
            updateAuctionsRealTime();
            tickerHandler.postDelayed(this, 1000);
        }
    };

    private void updateAuctionsRealTime() {
        long now = System.currentTimeMillis();
        long THREE_HOURS_MS = 3 * 60 * 60 * 1000;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

        // Create fresh lists for DiffUtil to compare against
        List<AuctionModel> newRegularList = new ArrayList<>();
        List<AuctionModel> newEndingList = new ArrayList<>();

        // Use Iterator to safely remove expired items from the master list while looping
        Iterator<AuctionModel> iterator = allAuctions.iterator();
        while (iterator.hasNext()) {
            AuctionModel auction = iterator.next();
            try {
                java.util.Date endDate = sdf.parse(auction.getEndTime());
                if (endDate != null) {
                    long diff = endDate.getTime() - now;
                    long diffInHours = diff / (1000 * 60 * 60);

                    if (diff <= 0) {
                        // EXPIRED: Remove completely
                        iterator.remove();
                    } else if (diff <= THREE_HOURS_MS) {
                        // ENDING SOON: Add to fresh ending list
                        newEndingList.add(auction);
                    } else {
                        // LIVE AUCTION: Add to fresh regular list
                        newRegularList.add(auction);
                    }
                }
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        // Send the fresh lists to the Adapters (They will handle the DiffUtil logic internally!)
        if (regularAdapter != null) {
            regularAdapter.updateList(newRegularList);
            regularAdapter.notifyItemRangeChanged(0, regularAdapter.getItemCount(), "TICK_UPDATE");
        }
        if (endingAdapter != null) {
            endingAdapter.updateList(newEndingList);
            endingAdapter.notifyItemRangeChanged(0, endingAdapter.getItemCount(), "TICK_UPDATE");
        }

        // Toggle UI Visibility based on the new ending list
        if (newEndingList.isEmpty()) {
            layoutEndingSoonHeader.setVisibility(View.GONE);
            rvEnding.setVisibility(View.GONE);
        } else {
            layoutEndingSoonHeader.setVisibility(View.VISIBLE);
            rvEnding.setVisibility(View.VISIBLE);
        }
    }

    // --- CLEANUP LOGIC ---
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
        tickerHandler.removeCallbacks(tickerRunnable); // Crucial to prevent battery drain
    }

    // --- CAROUSEL RUNNABLE ---
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager2.getAdapter() != null) {
                int nextItem = viewPager2.getCurrentItem() + 1;
                if (nextItem >= viewPager2.getAdapter().getItemCount()) {
                    nextItem = 0;
                }
                viewPager2.setCurrentItem(nextItem, true);
            }
        }
    };

    // --- CAROUSEL ADAPTER ---
    class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
        // [Keep your exact BannerAdapter code here...]
        private final List<Integer> imageList;
        BannerAdapter(List<Integer> imageList) { this.imageList = imageList; }
        @NonNull @Override public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new BannerViewHolder(view);
        }
        @Override public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext()).load(imageList.get(position)).into(holder.imageView);
        }
        @Override public int getItemCount() { return imageList.size(); }
        class BannerViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            BannerViewHolder(@NonNull View itemView) { super(itemView); imageView = itemView.findViewById(R.id.imgBanner); }
        }
    }
}