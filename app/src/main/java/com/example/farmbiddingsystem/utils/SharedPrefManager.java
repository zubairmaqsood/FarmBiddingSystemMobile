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

    // FIX 1: Ab yeh method userName bhi accept karega aur save karega
    public void saveUser(String token, String role, String userName) {
        editor.putString("TOKEN", token);
        editor.putString("ROLE", role);
        editor.putString("USER_NAME", userName); // Naam memory me save ho gaya
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

    // FIX 2: Profile Fragment ke liye naam nikalne wala method add kiya
    public String getUserName() {
        return sharedPreferences.getString("USER_NAME", "User");
    }

    // Call this when the user clicks "Logout"
    public void logout() {
        editor.clear();
        editor.apply();
    }
}