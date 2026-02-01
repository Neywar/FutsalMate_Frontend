package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class CourtDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_details);

        // Header Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Book Now button logic
        MaterialButton btnBookNow = findViewById(R.id.btnBookNow);
        if (btnBookNow != null) {
            btnBookNow.setOnClickListener(v -> {
                // Navigate to SelectTimeslotActivity when Book Now is clicked
                Intent intent = new Intent(CourtDetailsActivity.this, SelectTimeslotActivity.class);
                startActivity(intent);
            });
        }
    }
}
