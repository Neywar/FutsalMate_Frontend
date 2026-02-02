package com.example.futsalmate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class BookedDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_details);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
