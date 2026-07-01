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
import android.widget.TextView;

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
import com.example.farmbiddingsystem.network.ApiClient;
import com.example.farmbiddingsystem.network.ApiService;
import com.example.farmbiddingsystem.utils.AuctionDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    // --- MASTER DATA & UI LISTS ---
    private List<AuctionModel> allAuctions = new ArrayList<>();

    private View rootView; // Store the root view

    private AuctionAdapter regularAdapter;
    private AuctionAdapter endingAdapter;

    // --- REAL-TIME TICKER ---
    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private View layoutEndingSoonHeader;
    private View tvLiveHeader;
    private TextView tvEmptyState;
    private RecyclerView rvEnding;
    private RecyclerView rvLive;

    private boolean isInitialLoad = true;

    // --- CAROUSEL ---
    private ViewPager2 viewPager2;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. CAROUSEL SETUP
        viewPager2 = rootView.findViewById(R.id.bannerCarousel);
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
        viewPager2 = rootView.findViewById(R.id.bannerCarousel);
        rvLive = rootView.findViewById(R.id.rvLiveAuctions);
        rvEnding = rootView.findViewById(R.id.rvEndingAuctions);
        layoutEndingSoonHeader = rootView.findViewById(R.id.layoutEndingSoonHeader);
        tvLiveHeader = rootView.findViewById(R.id.tvLiveHeader);
        tvEmptyState = rootView.findViewById(R.id.tvEmptyState);

        // 3. COMMON CLICK LISTENER
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

                // Pass basic data for INSTANT loading
                intent.putExtra("auc_id", auction.getAucId());
                intent.putExtra("title", auction.getAucTitle());
                intent.putExtra("image_url", auction.getImagePath());
                intent.putExtra("end_time", auction.getEndTime());

                startActivity(intent);
            }
        };

        // 4. ADAPTER SETUP
        regularAdapter = new AuctionAdapter(new ArrayList<>(), commonClickListener);
        rvLive.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLive.setAdapter(regularAdapter);

        endingAdapter = new AuctionAdapter(new ArrayList<>(), commonClickListener);
        rvEnding.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvEnding.setAdapter(endingAdapter);

        // 5. SEARCH BAR SETUP
        EditText editSearch = rootView.findViewById(R.id.editSearch);
        editSearch.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SearchActivity.class);
            startActivity(intent);
        });

        // 6. FETCH REAL DATA FROM VERCEL
        updateAuctionsRealTime();
        List<AuctionModel> cachedAuctions = AuctionDataHolder.getInstance().getMasterList();

        if (cachedAuctions != null && !cachedAuctions.isEmpty()) {
            // Data is already in memory! Skip the API call.
            allAuctions = cachedAuctions;
            showDataState();
            updateAuctionsRealTime();
            tickerHandler.post(tickerRunnable); // Start the timer
        } else {
            // No data in memory, fetch from the server (runs only on first load)
            fetchLiveAuctions();
        }

        return rootView;
    }

    private void fetchLiveAuctions() {
        showLoadingState(); // <--- Call this FIRST

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getHomePageAuctions().enqueue(new Callback<List<AuctionModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<AuctionModel>> call, @NonNull Response<List<AuctionModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allAuctions = response.body();
                    AuctionDataHolder.getInstance().setMasterList(allAuctions);

                    showDataState(); // <--- Call this to clear the loading spinner
                    updateAuctionsRealTime();
                    tickerHandler.postDelayed(tickerRunnable, 1000);
                } else {
                    showErrorState("Server error. Please try again later.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AuctionModel>> call, @NonNull Throwable t) {
                showErrorState("Check your internet connection.");
            }
        });
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
        // STATE 1: Empty Master List (No auctions at all)
        if (allAuctions == null || allAuctions.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvLiveHeader.setVisibility(View.GONE);
            rvLive.setVisibility(View.GONE);
            layoutEndingSoonHeader.setVisibility(View.GONE);
            rvEnding.setVisibility(View.GONE);
            return;
        }

        // STATE 2: We have data, hide empty state text
        tvEmptyState.setVisibility(View.GONE);

        long now = System.currentTimeMillis();
        long THREE_HOURS_MS = 3 * 60 * 60 * 1000;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

        List<AuctionModel> newRegularList = new ArrayList<>();
        List<AuctionModel> newEndingList = new ArrayList<>();

        Iterator<AuctionModel> iterator = allAuctions.iterator();
        while (iterator.hasNext()) {
            AuctionModel auction = iterator.next();
            try {
                java.util.Date endDate = sdf.parse(auction.getEndTime());
                if (endDate != null) {
                    long diff = endDate.getTime() - now;

                    if (diff <= 0) {
                        iterator.remove(); // Remove expired
                    } else if (diff <= THREE_HOURS_MS) {
                        newEndingList.add(auction); // Ending Soon
                    } else {
                        newRegularList.add(auction); // Regular Live
                    }
                }
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        // Update Adapters
        if (regularAdapter != null) {
            regularAdapter.updateList(newRegularList);
            regularAdapter.notifyItemRangeChanged(0, regularAdapter.getItemCount(), "TICK_UPDATE");
        }
        if (endingAdapter != null) {
            endingAdapter.updateList(newEndingList);
            endingAdapter.notifyItemRangeChanged(0, endingAdapter.getItemCount(), "TICK_UPDATE");
        }

        // Toggle UI Visibility based on Ending Soon list
        if (newEndingList.isEmpty()) {
            layoutEndingSoonHeader.setVisibility(View.GONE);
            rvEnding.setVisibility(View.GONE);
        } else {
            layoutEndingSoonHeader.setVisibility(View.VISIBLE);
            rvEnding.setVisibility(View.VISIBLE);
        }

        // Toggle UI Visibility based on Regular Live list
        if (newRegularList.isEmpty()) {
            tvLiveHeader.setVisibility(View.GONE);
            rvLive.setVisibility(View.GONE);
        } else {
            tvLiveHeader.setVisibility(View.VISIBLE);
            rvLive.setVisibility(View.VISIBLE);
        }

        // Final check: If all auctions just expired on this exact tick
        if (allAuctions.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvLiveHeader.setVisibility(View.GONE);
            rvLive.setVisibility(View.GONE);
            layoutEndingSoonHeader.setVisibility(View.GONE);
            rvEnding.setVisibility(View.GONE);
        }
    }

    private void showLoadingState() {
        if (rootView == null) return; // Safety check

        rvLive.setVisibility(View.GONE);
        rvEnding.setVisibility(View.GONE);
        layoutEndingSoonHeader.setVisibility(View.GONE);
        tvLiveHeader.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        View loadingSpinner = rootView.findViewById(R.id.loadingSpinner);
        if (loadingSpinner != null) loadingSpinner.setVisibility(View.VISIBLE);
    }

    private void showDataState() {
        if (rootView == null) return;

        View loadingSpinner = rootView.findViewById(R.id.loadingSpinner);
        if (loadingSpinner != null) loadingSpinner.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        if (rootView == null) return;

        View loadingSpinner = rootView.findViewById(R.id.loadingSpinner);
        if (loadingSpinner != null) loadingSpinner.setVisibility(View.GONE);

        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText(message);

        rvLive.setVisibility(View.GONE);
        rvEnding.setVisibility(View.GONE);
        layoutEndingSoonHeader.setVisibility(View.GONE);
        tvLiveHeader.setVisibility(View.GONE);
    }
    // --- CLEANUP LOGIC ---
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
        tickerHandler.removeCallbacks(tickerRunnable);
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