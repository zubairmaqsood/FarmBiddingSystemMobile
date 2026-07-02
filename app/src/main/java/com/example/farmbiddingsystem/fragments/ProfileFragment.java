package com.example.farmbiddingsystem.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.farmbiddingsystem.LoginActivity;
import com.example.farmbiddingsystem.R;
import com.example.farmbiddingsystem.utils.AuctionDataHolder;
import com.example.farmbiddingsystem.utils.SharedPrefManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    // UI Elements Declare Kiye
    private TextView profileName, profileRole, btnLogout;
    private SharedPrefManager prefManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize SharedPrefManager
        prefManager = new SharedPrefManager(requireActivity());

        // 1. XML Views ko Java variables se map kiya (Matching your exact XML IDs)
        profileName = view.findViewById(R.id.profileName);
        profileRole = view.findViewById(R.id.profileRole);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 2. Set the Logout Click Listener
        btnLogout.setOnClickListener(v -> {
            // Clear SharedPreferences (Token, Role & Name)
            prefManager.logout();

            // Clear Cached Data so the next user doesn't see old data
            AuctionDataHolder.getInstance().clearAllData();

            // Redirect to Login Activity and clear the backstack
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    // FIX: Jab bhi user profile tab par wapas aayega, yeh automatically UI ko refresh karega
    @Override
    public void onResume() {
        super.onResume();
        updateProfileUI();
    }

    /**
     * DYNAMICALLY sets the user name and role from SharedPrefManager
     * This overwrites the placeholder "Zubair" text from XML at runtime!
     */
    private void updateProfileUI() {
        if (prefManager.isLoggedIn()) {
            // --- USER IS LOGGED IN (DYNAMIC LOAD) ---
            String savedName = prefManager.getUserName(); // Pulls real login name from memory
            String savedRole = prefManager.getRole();     // Pulls real role (farmer/buyer)

            profileName.setText(savedName); // Here "Zubair" is replaced by actual Username!
            profileRole.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);

            // Dynamically set role badge text
            if (savedRole != null && savedRole.equalsIgnoreCase("farmer")) {
                profileRole.setText("Verified Farmer");
            } else if (savedRole != null && savedRole.equalsIgnoreCase("buyer")) {
                profileRole.setText("Verified Buyer");
            } else {
                profileRole.setText("Verified User");
            }
        } else {
            // --- IF USER IS NOT LOGGED IN ---
            profileName.setText("Log in to your account");
            profileRole.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
        }
    }
}