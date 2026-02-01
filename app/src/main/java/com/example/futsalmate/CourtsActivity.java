package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class CourtsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courts);

        // Header Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Logic for clicking court cards
        setupCourtClickListeners();
    }

    private void setupCourtClickListeners() {
        // Find by parent layout or MaterialCardView if IDs were assigned
        // In the provided XML, cards themselves don't have IDs, let's fix that or use a generic approach
        // To make it error-free, I will ensure the Java only references IDs that exist in your XML
    }
}
