package com.example.farmbiddingsystem;

import android.os.Bundle;
import android.view.MenuItem; // Make sure to add this import
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull; // Make sure to add this import
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView; // Make sure to add this import

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Load the HomeFragment by default when the app first opens
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // 2. Find the Bottom Bar in your layout
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // 3. Make it listen for clicks!
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // 4. Check which button was clicked using the IDs from your bottom_nav_menu.xml
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_bids) {
                    // This line is now active!
                    selectedFragment = new BidsFragment();
                } else if (itemId == R.id.nav_profile) {
                     selectedFragment = new ProfileFragment();
                }

                // 5. Swap the fragment!
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true; // Return true to tell Android to highlight the button
                }

                return false;
            }
        });
    }
}