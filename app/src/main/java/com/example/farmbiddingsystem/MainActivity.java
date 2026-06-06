package com.example.farmbiddingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; // Make sure to add this import
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull; // Make sure to add this import
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.farmbiddingsystemmobile.R;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Make sure to add this import
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigationView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // 1. Load the HomeFragment by default when the app first opens
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }


        // 2. Find the Bottom Bar in your layout
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        FloatingActionButton createAucBtn = findViewById(R.id.fabAddAuction);

        createAucBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateAuctionForm.class));
            }
        });

        // 3. Make it listen for clicks!
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // 4. Check which button was clicked using the IDs from your bottom_nav_menu.xml
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Show the FAB and load HomeFragment
                    createAucBtn.show();
                    loadFragment(new HomeFragment());
                    return true;

                } else if (itemId == R.id.nav_bids) {
                    // Hide the FAB and load BidsFragment
                    createAucBtn.hide();
                    loadFragment(new BidsFragment());
                    return true;

                } else if (itemId == R.id.nav_profile) {
                    // Hide the FAB and load ProfileFragment
                    createAucBtn.hide();
                    loadFragment(new ProfileFragment());
                    return true;
                }

                return false;
            }
        });


    }

    private void loadFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}