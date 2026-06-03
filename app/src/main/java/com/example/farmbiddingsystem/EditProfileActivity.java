package com.example.farmbiddingsystem;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextInputEditText etFullName, etPhone, etLocation;
    private MaterialButton btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Find Views
        btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // 2. Handle Custom Back Button
        btnBack.setOnClickListener(v -> {
            finish(); // Closes the activity and returns to the previous screen
        });

        // 3. Handle Save Button
        btnSaveProfile.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                // In the future, this is where you run an UPDATE query to your MySQL database
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close screen after saving
            }
        });
    }
}