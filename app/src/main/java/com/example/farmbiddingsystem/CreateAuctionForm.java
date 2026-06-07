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

    // UI Buttons including our two new AI tools
    private MaterialButton btnCamera, btnGallery, btnCreateAuction, btnAiDescribe, btnAiSuggestPrice;

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

        // Find AI Buttons
        btnAiDescribe = findViewById(R.id.btnAiDescribe);
        btnAiSuggestPrice = findViewById(R.id.btnAiSuggestPrice);

        // Set up basic click listeners
        backBtn.setOnClickListener(v -> finish());
        endTime.setOnClickListener(v -> showDatePicker());

        // Setup Dropdown for Crop Categories
        String[] cropCategories = new String[]{"Wheat", "Rice (Basmati)", "Sugarcane", "Maize", "Vegetables (Mixed)", "Fruits", "Pulses"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cropCategories);
        spinnerCategory.setAdapter(adapter);

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

        // AI Feature 1: Description Generation
        if (btnAiDescribe != null) {
            btnAiDescribe.setOnClickListener(v -> generateAiDescription());
        }

        // AI Feature 2: Price Suggestion
        if (btnAiSuggestPrice != null) {
            btnAiSuggestPrice.setOnClickListener(v -> suggestAiPrice());
        }

        // Final Submission
        btnCreateAuction.setOnClickListener(v -> validateAndSubmit());
    }

    // =====================================================================
    // GEMINI AI INTEGRATION 1: Description Generator
    // =====================================================================
    private void generateAiDescription() {
        String category = spinnerCategory.getText().toString().trim();
        String title = aucTitle.getText().toString().trim();

        // Validation: Ensure the farmer has filled out a title and category first
        if (category.isEmpty() || category.equals("Crop Category *")) {
            Toast.makeText(this, "Please select a Crop Category first", Toast.LENGTH_SHORT).show();
            spinnerCategory.requestFocus();
            return;
        }

        if (title.isEmpty()) {
            aucTitle.setError("Please enter an Item Name first");
            aucTitle.requestFocus();
            return;
        }

        Toast.makeText(this, "Generating description with Gemini AI...", Toast.LENGTH_SHORT).show();
        btnAiDescribe.setEnabled(false);

        String aiPrompt = "Act as an expert agricultural business marketer. Create a highly professional, " +
                "appealing market description for a wholesale listing of " + title + " belonging to the category of " + category + ". " +
                "Highlight factors like freshness, high farming quality, and good harvest standards. " +
                "Keep the response professional, clean, and exactly 2 to 3 sentences long without markdown formatting or bullet points.";

        GeminiAI gemini = new GeminiAI();
        gemini.generateText(aiPrompt, new GeminiAI.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    btnAiDescribe.setEnabled(true);
                    if (aucDesc != null) {
                        aucDesc.setText(response);
                        Toast.makeText(CreateAuctionForm.this, "AI Description Generated!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnAiDescribe.setEnabled(true);
                    Toast.makeText(CreateAuctionForm.this, "AI Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // =====================================================================
    // GEMINI AI INTEGRATION 2: Market Price Estimator
    // =====================================================================
    private void suggestAiPrice() {
        String category = spinnerCategory.getText().toString().trim();
        String title = aucTitle.getText().toString().trim();
        String qtyStr = aucQty.getText().toString().trim();

        // Ensure the farmer provided the item name and quantity before asking AI
        if (title.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Please enter Item Name and Quantity first", Toast.LENGTH_SHORT).show();
            if (title.isEmpty()) aucTitle.requestFocus();
            else aucQty.requestFocus();
            return;
        }

        btnAiSuggestPrice.setEnabled(false);
        Toast.makeText(this, "Analyzing live market rates...", Toast.LENGTH_SHORT).show();

        // We ask Gemini to act as an agricultural financial analyst in Pakistan
        String aiPrompt = "Act as an expert agricultural market analyst in Pakistan. " +
                "A farmer is selling " + qtyStr + " KG of " + title + " in the " + category + " category. " +
                "Calculate a realistic and highly competitive total starting base price in PKR (Rs) for this entire quantity. " +
                "Reply with ONLY the raw integer number representing the total total. Do not include commas, the word 'Rs', or any other text.";

        GeminiAI gemini = new GeminiAI();
        gemini.generateText(aiPrompt, new GeminiAI.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    btnAiSuggestPrice.setEnabled(true);

                    // The AI might accidentally include text or commas, so we clean it to strictly numbers
                    String cleanNumber = response.replaceAll("[^0-9]", "");

                    if (!cleanNumber.isEmpty()) {
                        basePrice.setText(cleanNumber);
                        Toast.makeText(CreateAuctionForm.this, "Best market price applied!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CreateAuctionForm.this, "Could not determine price. Please enter manually.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnAiSuggestPrice.setEnabled(true);
                    Toast.makeText(CreateAuctionForm.this, "Market AI Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // --- HELPER: Create a secure placeholder file for the camera ---
    private Uri createSecureFileUri() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

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
        // Implement your DatePickerDialog here
        // Example: Calendar logic to pop up a date dialog and set text to endTime
    }

    private void validateAndSubmit() {
        // Implement your validation and database (Firebase/MySQL) submission here
        Toast.makeText(this, "Auction Created Successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}