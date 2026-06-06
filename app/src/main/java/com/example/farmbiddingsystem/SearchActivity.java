package com.example.farmbiddingsystem;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmbiddingsystemmobile.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SearchActivity extends AppCompatActivity {

    private TextInputLayout tilSearchBox;
    private TextInputEditText etRealSearch;
    private RecyclerView recyclerSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Find the views
        tilSearchBox = findViewById(R.id.tilSearchBox);
        etRealSearch = findViewById(R.id.etRealSearch);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);

        // 2. The "Back Arrow" Logic
        // When the user clicks the arrow inside the search bar, destroy this activity
        tilSearchBox.setStartIconOnClickListener(v -> {
            finish(); // Returns smoothly to the HomeFragment
        });

        // 3. THE KEYBOARD MAGIC
        // Request focus on the EditText immediately
        etRealSearch.requestFocus();

        // Tell the Android system to slide the keyboard up
        etRealSearch.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etRealSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // 4. (Optional for now) Listen to what the user types!
        etRealSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etRealSearch.getText().toString();
            // In the future, you will send this 'query' variable to your database
            // to fetch results and put them into the recyclerSearchResults!
            return false;
        });
    }

    // Safety check: if the user uses the phone's physical back swipe, hide the keyboard smoothly
    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etRealSearch.getWindowToken(), 0);
        }
    }
}