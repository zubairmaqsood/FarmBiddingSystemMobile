package com.example.farmbiddingsystem;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager2;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Find the Carousel in the XML
        viewPager2 = view.findViewById(R.id.bannerCarousel);

        // 2. Set up the "fake" images (you will replace these with real URLs or drawables later)
        List<Integer> images = Arrays.asList(
                R.drawable.main_pic1,
                R.drawable.main_pic2,
                R.drawable.main_pic3,
                R.drawable.main_pic4
        );

        // 3. Attach the Adapter
        viewPager2.setAdapter(new BannerAdapter(images));

        // 4. Make it auto-scroll every 3 seconds!
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000); // 3000ms = 3 seconds
            }
        });

        return view;
    }

    // This is the Runnable that tells the carousel to move forward 1 page
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager2.getAdapter() != null) {
                int nextItem = viewPager2.getCurrentItem() + 1;
                if (nextItem >= viewPager2.getAdapter().getItemCount()) {
                    nextItem = 0; // Loop back to the first image
                }
                viewPager2.setCurrentItem(nextItem, true); // true = smooth scroll animation
            }
        }
    };

    // =================================================================
    // THE ADAPTER: This inner class takes your list of images and puts them into the item_banner.xml
    // =================================================================
    class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
        private final List<Integer> imageList;

        BannerAdapter(List<Integer> imageList) {
            this.imageList = imageList;
        }

        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new BannerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            // Glide automatically resizes the image and loads it in the background!
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(imageList.get(position))
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageList.size();
        }

        class BannerViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            BannerViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imgBanner);
            }
        }
    }
}