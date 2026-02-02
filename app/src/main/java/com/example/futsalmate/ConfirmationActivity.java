package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // Header Navigation
        ImageView btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // View My Bookings button logic
        MaterialButton btnViewBookings = findViewById(R.id.btnViewBookings);
        if (btnViewBookings != null) {
            btnViewBookings.setOnClickListener(v -> {
                Intent intent = new Intent(ConfirmationActivity.this, MainActivity.class);
                intent.putExtra("TARGET_FRAGMENT", "BOOKINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Optionally populating summary details
        populateBookingDetails();
    }

    private void populateBookingDetails() {
        // Here you would normally set texts based on passed intent extras
        TextView tvCourtName = findViewById(R.id.tvCourtName);
        if (tvCourtName != null) {
            // tvCourtName.setText(getIntent().getStringExtra("COURT_NAME"));
        }
    }
}
