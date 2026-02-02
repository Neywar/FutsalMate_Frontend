package com.example.futsalmate;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class EditCourtActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_court);

        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnUpdateCourt = findViewById(R.id.btnUpdateCourt);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnUpdateCourt != null) {
            btnUpdateCourt.setOnClickListener(v -> {
                // Logic to update the court would go here
                finish();
            });
        }
    }
}
