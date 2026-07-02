package com.example.farmbiddingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmbiddingsystem.network.ApiClient;
import com.example.farmbiddingsystem.network.ApiService;
import com.example.farmbiddingsystem.utils.SharedPrefManager;
import com.example.farmbiddingsystem.wrapperClasses.SignupResponse;

public class SignupActivity extends AppCompatActivity {

    // Declare all UI elements
    private AutoCompleteTextView spinnerUserType, spinnerBuyerType, spinnerCompanyType;
    private com.google.android.material.card.MaterialCardView cardBuyer, cardFarmer;
    private com.google.android.material.button.MaterialButton btnSignup;

    // General EditTexts
    private EditText etFullName, etCnic, etEmail, etPhone, etPassword, etConfirmPassword;

    // Buyer EditTexts
    private EditText etCompanyName, etCompanyAddress;

    // Farmer EditTexts
    private EditText etFarmLocation, etFarmSize, etCity;

    private TextView backToLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Layouts and Button
        cardBuyer = findViewById(R.id.cardBuyer);
        cardFarmer = findViewById(R.id.cardFarmer);
        btnSignup = findViewById(R.id.btnSignup);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        spinnerBuyerType = findViewById(R.id.spinnerBuyerType);
        spinnerCompanyType = findViewById(R.id.spinnerCompanyType);

        // Initialize General EditTexts
        etFullName = findViewById(R.id.etFullName);
        etCnic = findViewById(R.id.etCnic);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Initialize Buyer EditTexts
        etCompanyName = findViewById(R.id.etCompanyName);
        etCompanyAddress = findViewById(R.id.etCompanyAddress);

        // Initialize Farmer EditTexts
        etFarmLocation = findViewById(R.id.etFarmLocation);
        etFarmSize = findViewById(R.id.etFarmSize);
        etCity = findViewById(R.id.etCity);

        // Back to login button
        backToLoginBtn = findViewById(R.id.txtGoToLogin);

        // Setup Spinners
        setupSpinners();

        // Handle Layout Visibility Based on User Type
        spinnerUserType.setOnItemClickListener((parent, view, position, id) -> {
            String selected = spinnerUserType.getText().toString();
            if (selected.equals("Buyer")) {
                cardBuyer.setVisibility(View.VISIBLE);
                cardFarmer.setVisibility(View.GONE);
            } else if (selected.equals("Farmer")) {
                cardFarmer.setVisibility(View.VISIBLE);
                cardBuyer.setVisibility(View.GONE);
            } else {
                cardBuyer.setVisibility(View.GONE);
                cardFarmer.setVisibility(View.GONE);
            }
        });

        // Trigger Validation on Button Click
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateSignupForm()) {
                    performNetworkSignup(); // Call the API!
                }
            }
        });

        backToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });
    }

    private void performNetworkSignup() {
        // 1. Show Loading State
        btnSignup.setText("Creating Account...");
        btnSignup.setEnabled(false);

        // 2. Gather Data
        String fullName = etFullName.getText().toString().trim();
        String cnic = etCnic.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // **CRITICAL:** PHP expects 'buyer' or 'farmer' in lowercase!
        String userType = spinnerUserType.getText().toString().trim().toLowerCase();

        // Initialize optional fields as empty strings
        String buyerType = "", compName = "", compAddress = "", compType = "";
        String farmLoc = "", farmSize = "", city = "";

        if (userType.equals("buyer")) {
            buyerType = spinnerBuyerType.getText().toString().trim();
            compName = etCompanyName.getText().toString().trim();
            compAddress = etCompanyAddress.getText().toString().trim();
            compType = spinnerCompanyType.getText().toString().trim();
        } else if (userType.equals("farmer")) {
            farmLoc = etFarmLocation.getText().toString().trim();
            farmSize = etFarmSize.getText().toString().trim();
            city = etCity.getText().toString().trim();
        }

        // 3. Make API Call
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.registerUser(
                userType, fullName, cnic, email, phone, password,
                buyerType, compName, compAddress, compType,
                farmLoc, farmSize, city
        ).enqueue(new retrofit2.Callback<SignupResponse>() {
            @Override
            public void onResponse(retrofit2.Call<SignupResponse> call, retrofit2.Response<SignupResponse> response) {
                btnSignup.setText("Sign Up");
                btnSignup.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    SignupResponse res = response.body();

                    if (res.isSuccess()) {
                        // 4. Save to SharedPreferences!
                        SharedPrefManager prefManager = new SharedPrefManager(SignupActivity.this);

                        // FIX: Added 'fullName' as the third parameter to match SharedPrefManager
                        prefManager.saveUser(res.getToken(), res.getRole(), fullName);

                        Toast.makeText(SignupActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();

                        clearForm(); // Clean up form values safely

                        // 5. Send user to MainActivity
                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "Error: " + res.getError(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Server Error. Check Email/CNIC.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<SignupResponse> call, Throwable t) {
                btnSignup.setText("Sign Up");
                btnSignup.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSpinners() {
        String[] userTypes = {"Select User Type", "Buyer", "Farmer"};
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, userTypes);
        spinnerUserType.setAdapter(userAdapter);

        String[] buyerTypes = {"Select Buyer Type", "Individual", "Wholesaler", "Retailer", "Exporter"};
        ArrayAdapter<String> buyerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, buyerTypes);
        spinnerBuyerType.setAdapter(buyerAdapter);

        String[] companyTypes = {"Select Company Type", "Private Limited", "Public Limited",
                "Sole Proprietorship", "Partnership"};
        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, companyTypes);
        spinnerCompanyType.setAdapter(companyAdapter);
    }

    private boolean validateSignupForm() {
        String fullName = etFullName.getText().toString().trim();
        String cnic = etCnic.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        String userType = spinnerUserType.getText().toString().trim();

        if (userType.equals("Select User Type") || userType.isEmpty()) {
            Toast.makeText(this, "Please select a User Type (Buyer or Farmer)", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fullName.isEmpty()) {
            etFullName.setError("Full Name is required");
            etFullName.requestFocus();
            return false;
        }

        if (cnic.length() != 13) {
            etCnic.setError("Valid 13-digit CNIC is required");
            etCnic.requestFocus();
            return false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email address is required");
            etEmail.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        if (phone.length() != 11) {
            etPhone.setError("Phone number must be 11 digits");
            etPhone.requestFocus();
            return false;
        }

        if (!phone.startsWith("03")) {
            etPhone.setError("Phone number must start with 03");
            etPhone.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        switch (userType) {
            case "Buyer":
                String buyerType = spinnerBuyerType.getText().toString().trim();
                String companyName = etCompanyName.getText().toString().trim();
                String companyAddress = etCompanyAddress.getText().toString().trim();
                String companyType = spinnerCompanyType.getText().toString().trim();

                if (buyerType.isEmpty() || buyerType.equals("Select Buyer Type")) {
                    Toast.makeText(this, "Please select a Buyer Type", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (companyName.isEmpty()) {
                    etCompanyName.setError("Company Name is required");
                    etCompanyName.requestFocus();
                    return false;
                }

                if (companyAddress.isEmpty()) {
                    etCompanyAddress.setError("Company Address is required");
                    etCompanyAddress.requestFocus();
                    return false;
                }

                if (companyType.isEmpty() || companyType.equals("Select Company Type")) {
                    Toast.makeText(this, "Please select a Company Type", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;

            case "Farmer":
                String farmLocation = etFarmLocation.getText().toString().trim();
                String farmSize = etFarmSize.getText().toString().trim();
                String city = etCity.getText().toString().trim();

                if (farmLocation.isEmpty()) {
                    etFarmLocation.setError("Farm Location is required");
                    etFarmLocation.requestFocus();
                    return false;
                }

                if (farmSize.isEmpty()) {
                    etFarmSize.setError("Farm Size is required");
                    etFarmSize.requestFocus();
                    return false;
                }

                if (city.isEmpty()) {
                    etCity.setError("City is required");
                    etCity.requestFocus();
                    return false;
                }
                break;
        }

        return true;
    }

    private void clearForm() {
        etFullName.setText("");
        etCnic.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");

        etCompanyName.setText("");
        etCompanyAddress.setText("");

        etFarmLocation.setText("");
        etFarmSize.setText("");
        etCity.setText("");

        spinnerUserType.setText("", false);
        spinnerBuyerType.setText("", false);
        spinnerCompanyType.setText("", false);

        cardBuyer.setVisibility(View.GONE);
        cardFarmer.setVisibility(View.GONE);

        etFullName.clearFocus();
    }
}