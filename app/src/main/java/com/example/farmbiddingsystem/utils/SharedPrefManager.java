package com.example.farmbiddingsystem.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "farm_bidding_prefs";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save data when user signs up or logs in
    public void saveUser(String token, String role) {
        editor.putString("TOKEN", token);
        editor.putString("ROLE", role);
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean("IS_LOGGED_IN", false);
    }

    // Get the Token for API headers later
    public String getToken() {
        return sharedPreferences.getString("TOKEN", null);
    }

    // Get the Role to show/hide UI elements
    public String getRole() {
        return sharedPreferences.getString("ROLE", null);
    }

    // Call this when the user clicks "Logout"
    public void logout() {
        editor.clear();
        editor.apply();
    }
}