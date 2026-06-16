package com.example.farmbiddingsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmbiddingsystem.network.ApiClient;
import com.example.farmbiddingsystem.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Declare UI elements
    private TextInputEditText etEmail, etPassword;
    private ApiService apiService;
    private String email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiClient.getClient().create(ApiService.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView txtSignUp = findViewById(R.id.txtSignUp);


        btnLogin.setOnClickListener(v -> {
            if (validateLoginForm(email,password)) {
                login(email,password);
                Toast.makeText(LoginActivity.this, "Validation Passed! Logging in...", Toast.LENGTH_SHORT).show();
                clearForm();
            }
        });

        txtSignUp.setOnClickListener(v -> {
            // Navigate the user to the SignupActivity
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateLoginForm(String email,String password) {

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() > 20) {
            etPassword.setError("Password must not exceed 20 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        etEmail.setText("");
        etPassword.setText("");
        etEmail.clearFocus();
    }

    private void login(String email,String password){
        apiService.loginUser(email,password).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseData = response.body();

                    // PHP keys map directly to your Java Map keys here!
                    if (responseData.containsKey("success") && (boolean) responseData.get("success")) {
                        String token = (String) responseData.get("token");
                        String role = (String) responseData.get("role");
                        String userName = (String) responseData.get("user_name");

                        // Save credentials to SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putString("jwt_token", token)
                                .putString("user_role", role)
                                .putString("user_name", userName)
                                .apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        // This handles if php sent an error back under a successful HTTP 200 state
                        String error = (String) responseData.get("error");
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // This handles HTTP 500 (Server Error) or 404
                    try {
                        // This is how you parse the error sent by your PHP catch block!
                        String errorJson = response.errorBody().string();
                        // Use Gson to turn that string back into a Map/Object
                        Toast.makeText(LoginActivity.this, "Server Error: " + errorJson, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Unknown server error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            // This runs when server is not reached at all
            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, Throwable t) {
                Log.e("API_FAILURE", "Connection error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Server unreachable.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}