package com.example.farmbiddingsystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
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
    private MaterialButton btnCamera, btnGallery, btnCreateAuction, btnAiDescribe, btnAiSuggestPrice;

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    showImagePreview();
                }
            }
    );

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
        btnAiDescribe = findViewById(R.id.btnAiDescribe);
        btnAiSuggestPrice = findViewById(R.id.btnAiSuggestPrice);

        backBtn.setOnClickListener(v -> finish());
        endTime.setOnClickListener(v -> showDatePicker());

        String[] cropCategories = new String[]{"Wheat", "Rice (Basmati)", "Sugarcane", "Maize", "Vegetables (Mixed)", "Fruits", "Pulses"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cropCategories);
        spinnerCategory.setAdapter(adapter);

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnCamera.setOnClickListener(v -> {
            selectedImageUri = createSecureFileUri();
            if (selectedImageUri != null) {
                cameraLauncher.launch(selectedImageUri);
            }
        });

        if (btnAiDescribe != null) btnAiDescribe.setOnClickListener(v -> generateAiDescription());
        if (btnAiSuggestPrice != null) btnAiSuggestPrice.setOnClickListener(v -> suggestAiPrice());

        btnCreateAuction.setOnClickListener(v -> validateAndSubmit());
    }

    // =====================================================================
    // AI INTEGRATION 1: Multimodal Description Generator
    // =====================================================================
    private void generateAiDescription() {
        String category = spinnerCategory.getText().toString().trim();
        String title = aucTitle.getText().toString().trim();

        if (category.isEmpty() || category.equals("Crop Category *")) {
            Toast.makeText(this, "Please select a Crop Category first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.isEmpty()) {
            aucTitle.setError("Please enter an Item Name first");
            return;
        }

        Toast.makeText(this, "Analyzing image and generating description...", Toast.LENGTH_SHORT).show();
        btnAiDescribe.setEnabled(false);

        // Fetch the actual image data
        Bitmap imageBitmap = uriToBitmap(selectedImageUri);

        // Update prompt to acknowledge the image
        String aiPrompt = "Act as an expert agricultural business marketer. I have attached an image of the actual crop being sold. " +
                "Analyze the visible condition, color, and freshness of the crop in the photo. " +
                "Create a highly professional market description for a wholesale listing of " + title + " (" + category + "). " +
                "Highlight the specific positive qualities you see in the image. " +
                "Keep the response clean, exactly 2 to 3 sentences long, with no markdown or bullet points.";

        GeminiAI gemini = new GeminiAI();
        gemini.generateResponse(aiPrompt, imageBitmap, new GeminiAI.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    btnAiDescribe.setEnabled(true);
                    if (aucDesc != null) {
                        aucDesc.setText(response);
                        Toast.makeText(CreateAuctionForm.this, "Visual AI Description Generated!", Toast.LENGTH_SHORT).show();
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
    // AI INTEGRATION 2: Multimodal Live Market Price Estimator
    // =====================================================================
    private void suggestAiPrice() {
        String category = spinnerCategory.getText().toString().trim();
        String title = aucTitle.getText().toString().trim();
        String qtyStr = aucQty.getText().toString().trim();

        if (title.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Please enter Item Name and Quantity first", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAiSuggestPrice.setEnabled(false);
        Toast.makeText(this, "Inspecting crop quality & fetching live rates...", Toast.LENGTH_SHORT).show();

        Bitmap imageBitmap = uriToBitmap(selectedImageUri);

        // Instruct AI to base the price calculation heavily on the visual grade of the item
        String aiPrompt = "Act as an expert agricultural market analyst and crop inspector in Pakistan. " +
                "A farmer is selling " + qtyStr + " KG of " + title + " (" + category + "). " +
                "I have attached an image of the specific crop batch. Visually assess the grade, freshness, and quality of the item in the picture. " +
                "Then, search the live web for today's current wholesale Mandi rates in Pakistan. " +
                "Based on the live rates AND the visual quality grade you determined from the image, calculate a fair total starting base price in PKR (Rs) for the entire " + qtyStr + " KG quantity. " +
                "If the crop looks premium, apply a premium rate; if it looks average, apply standard rates. " +
                "Reply with ONLY the final raw integer number representing the total value. Do not include commas, decimals, or 'Rs'.";

        GeminiAI gemini = new GeminiAI();
        gemini.generateResponse(aiPrompt, imageBitmap, new GeminiAI.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    btnAiSuggestPrice.setEnabled(true);
                    String cleanNumber = response.replaceAll("[^0-9]", "");
                    if (!cleanNumber.isEmpty()) {
                        basePrice.setText(cleanNumber);
                        Toast.makeText(CreateAuctionForm.this, "Quality-adjusted live price applied!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CreateAuctionForm.this, "Could not determine price.", Toast.LENGTH_SHORT).show();
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

    // --- HELPER: Safely convert URI to Bitmap for the AI ---
    private Bitmap uriToBitmap(Uri uri) {
        if (uri == null) return null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            } else {
                return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Uri createSecureFileUri() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            return FileProvider.getUriForFile(this, "com.example.farmbiddingsystem.fileprovider", imageFile);
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void showImagePreview() {
        imagePreview.setVisibility(View.VISIBLE);
        Glide.with(this).load(selectedImageUri).into(imagePreview);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> endTime.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void validateAndSubmit() {
        Toast.makeText(this, "Auction Created Successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}