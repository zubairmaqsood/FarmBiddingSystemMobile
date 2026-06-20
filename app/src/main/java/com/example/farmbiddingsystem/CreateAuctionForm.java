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
    private Bitmap selectedImageBitmap = null;

    private QwenAiHelper qwenAiHelper;

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

        qwenAiHelper = new QwenAiHelper();

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

        if (btnAiDescribe != null) {
            btnAiDescribe.setOnClickListener(v -> generateAiDescription());
        }

        if (btnAiSuggestPrice != null) {
            btnAiSuggestPrice.setOnClickListener(v -> suggestAiPrice());
        }

        btnCreateAuction.setOnClickListener(v -> validateAndSubmit());
    }

    private void generateAiDescription() {
        if (selectedImageBitmap == null) {
            Toast.makeText(this, "Please capture or select an image first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = spinnerCategory.getText().toString().trim();
        String title = aucTitle.getText().toString().trim();

        if (category.isEmpty() || category.equals("Crop Category *") || title.isEmpty()) {
            Toast.makeText(this, "Please select Category and Item Name so AI knows context!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAiDescribe.setEnabled(false);
        btnAiDescribe.setText("✨ Analyzing Image...");
        Toast.makeText(this, "Analyzing image quality...", Toast.LENGTH_SHORT).show();

        qwenAiHelper.analyzeCropImage(selectedImageBitmap, category, title, new QwenAiHelper.AiCallback() {
            @Override
            public void onSuccess(String description, String qualityGrade, double ignoredPrice) {
                btnAiDescribe.setEnabled(true);
                btnAiDescribe.setText("✨ Generate with AI");

                String finalDescriptionText = "Estimated Quality: " + qualityGrade + "\n\n" + description;
                aucDesc.setText(finalDescriptionText);
                Toast.makeText(CreateAuctionForm.this, "Visual AI Description & Quality Generated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                btnAiDescribe.setEnabled(true);
                btnAiDescribe.setText("✨ Generate with AI");
                Toast.makeText(CreateAuctionForm.this, "AI Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void suggestAiPrice() {
        String category = spinnerCategory.getText().toString().trim();
        String title = aucTitle.getText().toString().trim();
        String qtyKgStr = aucQty.getText().toString().trim(); // We now expect this to be in KG
        String descriptionText = aucDesc.getText().toString().trim();

        if (title.isEmpty() || qtyKgStr.isEmpty()) {
            Toast.makeText(this, "Please enter Item Name and Quantity (KG) first", Toast.LENGTH_SHORT).show();
            if (qtyKgStr.isEmpty()) aucQty.requestFocus();
            return;
        }

        // --- CONVERT KG TO MUNS ---
        double qtyKg = 0;
        try {
            qtyKg = Double.parseDouble(qtyKgStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for KG", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1 Mun = 40 KG
        double qtyMuns = qtyKg / 40.0;

        // Format to 2 decimal places to avoid messy numbers (e.g., 2.5 Muns)
        String finalMunsStr = String.format(Locale.US, "%.2f", qtyMuns);

        btnAiSuggestPrice.setEnabled(false);
        btnAiSuggestPrice.setText("Calculating Price...");
        Toast.makeText(this, "Converting KG to Muns and estimating price...", Toast.LENGTH_SHORT).show();

        // Pass the converted Muns to the AI
        qwenAiHelper.suggestPriceText(category, title, finalMunsStr, descriptionText, new QwenAiHelper.AiCallback() {
            @Override
            public void onSuccess(String ignoredDesc, String ignoredQuality, double suggestedPrice) {
                runOnUiThread(() -> {
                    btnAiSuggestPrice.setEnabled(true);
                    btnAiSuggestPrice.setText("✨ Suggest Price with AI");

                    if (suggestedPrice > 0) {
                        basePrice.setText(String.valueOf((int) suggestedPrice));
                        Toast.makeText(CreateAuctionForm.this, "Price for " + finalMunsStr + " Muns applied!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CreateAuctionForm.this, "Could not determine price. Enter manually.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    btnAiSuggestPrice.setEnabled(true);
                    btnAiSuggestPrice.setText("✨ Suggest Price with AI");
                    Toast.makeText(CreateAuctionForm.this, "Price Error: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

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
        selectedImageBitmap = uriToBitmap(selectedImageUri);
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