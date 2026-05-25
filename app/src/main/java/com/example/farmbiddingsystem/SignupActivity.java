package com.example.farmbiddingsystem;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    // Declare all UI elements
    Spinner spinnerUserType, spinnerBuyerType, spinnerCompanyType;
    LinearLayout layoutBuyer, layoutFarmer;
    Button btnSignup;

    // General EditTexts
    EditText etFullName, etCnic, etEmail, etPhone, etPassword, etConfirmPassword;

    // Buyer EditTexts
    EditText etCompanyName, etCompanyAddress;

    // Farmer EditTexts
    EditText etFarmLocation, etFarmSize, etCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Layouts and Button
        layoutBuyer = findViewById(R.id.layoutBuyer);
        layoutFarmer = findViewById(R.id.layoutFarmer);
        btnSignup = findViewById(R.id.btnSignup);

        // Initialize Spinners
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

        // Setup Spinners
        setupSpinners();

        // Handle Layout Visibility Based on User Type
        spinnerUserType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerUserType.getSelectedItem().toString();

                if (selected.equals("Buyer")) {
                    layoutBuyer.setVisibility(View.VISIBLE);
                    layoutFarmer.setVisibility(View.GONE);
                } else if (selected.equals("Farmer")) {
                    layoutFarmer.setVisibility(View.VISIBLE);
                    layoutBuyer.setVisibility(View.GONE);
                } else {
                    layoutBuyer.setVisibility(View.GONE);
                    layoutFarmer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Trigger Validation on Button Click
        btnSignup.setOnClickListener(v -> {
            if (validateSignupForm()) {
                // If validation returns true, proceed with backend signup logic here
                Toast.makeText(SignupActivity.this, "Validation Passed! Signup Successful.", Toast.LENGTH_LONG).show();
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateSignupForm()) {
                    Toast.makeText(SignupActivity.this, "Validation Successful!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSpinners() {
        // User Type Spinner
        String[] userTypes = {"Select User Type", "Buyer", "Farmer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userTypes);
        spinnerUserType.setAdapter(adapter);

        // Buyer Type Spinner
        String[] buyerTypes = {"Select Buyer Type", "Individual", "Wholesaler", "Retailer", "Exporter"};
        ArrayAdapter<String> buyerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, buyerTypes);
        spinnerBuyerType.setAdapter(buyerAdapter);

        // Company Type Spinner
        String[] companyTypes = {"Select Company Type", "Private Limited", "Public Limited", "Sole Proprietorship", "Partnership"};
        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, companyTypes);
        spinnerCompanyType.setAdapter(companyAdapter);
    }

    private boolean validateSignupForm() {
        String fullName = etFullName.getText().toString().trim();
        String cnic = etCnic.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        String userType = spinnerUserType.getSelectedItem() != null ? spinnerUserType.getSelectedItem().toString() : "";

        if (userType.equals("Select User Type") || userType.isEmpty()) {
            Toast.makeText(this, "Please select a User Type (Buyer or Farmer)", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fullName.isEmpty()) {
            etFullName.setError("Full Name is required");
            etFullName.requestFocus();
            return false;
        }

        if (cnic.isEmpty() || cnic.length() != 13) {
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

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        // --- 2. Conditional Validations based on User Type ---

        switch (userType) {
            case "Buyer":
                String buyerType = spinnerBuyerType.getSelectedItem().toString();
                String companyName = etCompanyName.getText().toString().trim();
                String companyAddress = etCompanyAddress.getText().toString().trim();
                String companyType = spinnerCompanyType.getSelectedItem().toString();

                if (buyerType.equals("Select Buyer Type")) {
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

                if (companyType.equals("Select Company Type")) {
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
}