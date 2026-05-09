package com.example.farmbiddingsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class SignupActivity extends AppCompatActivity {

    Spinner spinnerUserType, spinnerBuyerType, spinnerCompanyType;
    LinearLayout layoutBuyer, layoutFarmer;
    Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        spinnerUserType = findViewById(R.id.spinnerUserType);
        spinnerBuyerType = findViewById(R.id.spinnerBuyerType);
        spinnerCompanyType = findViewById(R.id.spinnerCompanyType);

        layoutBuyer = findViewById(R.id.layoutBuyer);
        layoutFarmer = findViewById(R.id.layoutFarmer);

        btnSignup = findViewById(R.id.btnSignup);

        // User Type Spinner
        String[] userTypes = {"Select User Type", "Buyer", "Farmer"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                userTypes
        );

        spinnerUserType.setAdapter(adapter);

        // Buyer Type Spinner
        String[] buyerTypes = {
                "Select Buyer Type",
                "Individual",
                "Wholesaler",
                "Retailer",
                "Exporter"
        };

        ArrayAdapter<String> buyerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                buyerTypes
        );

        spinnerBuyerType.setAdapter(buyerAdapter);

        // Company Type Spinner
        String[] companyTypes = {
                "Select Company Type",
                "Private Limited",
                "Public Limited",
                "Sole Proprietorship",
                "Partnership"
        };

        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                companyTypes
        );

        spinnerCompanyType.setAdapter(companyAdapter);

        // Show Hide Layouts

        spinnerUserType.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id) {

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

                    }
                });

        btnSignup.setOnClickListener(v ->
                Toast.makeText(
                        SignupActivity.this,
                        "Signup Successful",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }
}