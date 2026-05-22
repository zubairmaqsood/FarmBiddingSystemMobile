package com.example.farmbiddingsystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateAuctionForm extends AppCompatActivity {

    private ImageView backBtn, imagePreview;
    private AutoCompleteTextView spinnerCategory;
    private TextInputEditText aucTitle, aucDesc, basePrice, aucQty, endTime;
    private MaterialButton btnCamera, btnGallery, btnCreateAuction;

    private Uri selectedImageUri = null;

    // 1. GALLERY LAUNCHER
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    showImagePreview();
                }
            }
    );

    // 2. CAMERA LAUNCHER
    // Notice how this uses 'TakePicture' instead of 'StartActivityForResult'
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && selectedImageUri != null) {
                    showImagePreview();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_auction_form);

        // Find Views
        backBtn = findViewById(R.id.backBtn);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        aucTitle = findViewById(R.id.aucTitle);
        aucDesc = findViewById(R.id.aucDesc);
        basePrice = findViewById(R.id.basePrice);
        aucQty = findViewById(R.id.aucQty);
        endTime = findViewById(R.id.endTime);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        imagePreview = findViewById(R.id.imagePreview);
        btnCreateAuction = findViewById(R.id.btnCreateAuction);

        backBtn.setOnClickListener(v -> finish());
        endTime.setOnClickListener(v -> showDatePicker());

        // Setup Dropdown (Code omitted for brevity, keep your existing logic here!)
        String[] cropCategories = new String[]{"Wheat", "Rice (Basmati)", "Sugarcane", "Maize", "Vegetables (Mixed)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cropCategories);
        spinnerCategory.setAdapter(adapter);

        // --- BUTTON CLICKS ---

        // Open Gallery
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        // Open Camera
        btnCamera.setOnClickListener(v -> {
            selectedImageUri = createSecureFileUri();
            if (selectedImageUri != null) {
                cameraLauncher.launch(selectedImageUri);
            }
        });

        btnCreateAuction.setOnClickListener(v -> validateAndSubmit());
    }

    // --- NEW HELPER: Create a secure placeholder file for the camera ---
    private Uri createSecureFileUri() {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Return the secure URI using the FileProvider we made in the Manifest!
            return FileProvider.getUriForFile(this, "com.example.farmbiddingsystem.fileprovider", imageFile);

        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // --- HELPER: Display the image using Glide ---
    private void showImagePreview() {
        imagePreview.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(selectedImageUri)
                .into(imagePreview);
    }

    private void showDatePicker() {
        // ... Keep your existing DatePickerDialog code here ...
    }

    private void validateAndSubmit() {
        // ... Keep your existing validation code here ...
    }
}