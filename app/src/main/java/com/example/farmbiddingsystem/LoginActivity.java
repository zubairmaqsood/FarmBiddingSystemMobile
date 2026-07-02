package com.example.farmbiddingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmbiddingsystem.network.ApiClient;
import com.example.farmbiddingsystem.network.ApiService;
import com.example.farmbiddingsystem.utils.SharedPrefManager; // Import statement added
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private ApiService apiService;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiClient.getClient().create(ApiService.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView txtSignUp = findViewById(R.id.txtSignUp);

        // Button click action
        btnLogin.setOnClickListener(v -> {
            // FIX: User ke button dabate hi naya text read karein!
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();

            if (validateLoginForm(email, password)) {
                login(email, password);
                Toast.makeText(LoginActivity.this, "Logging in...", Toast.LENGTH_SHORT).show();
            }
        });

        txtSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateLoginForm(String email, String password) {
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

    private void login(String email, String password) {
        apiService.loginUser(email, password).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseData = response.body();

                    if (responseData.containsKey("success") && (boolean) responseData.get("success")) {
                        String token = (String) responseData.get("token");
                        String role = (String) responseData.get("role");
                        String userName = (String) responseData.get("user_name");

                        // FIX: Puraane manual logic ki jagah aapki naye SharedPrefManager ko call kiya
                        SharedPrefManager prefManager = new SharedPrefManager(LoginActivity.this);
                        prefManager.saveUser(token, role, userName);

                        // Success hone par form clear karein
                        clearForm();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String error = (String) responseData.get("error");
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        Toast.makeText(LoginActivity.this, "Server Error: " + errorJson, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Unknown server error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, Throwable t) {
                Log.e("API_FAILURE", "Connection error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Server unreachable.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}