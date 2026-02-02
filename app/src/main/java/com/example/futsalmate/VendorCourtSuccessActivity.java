package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class VendorCourtSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_court_success);

        ImageView btnClose = findViewById(R.id.btnClose);
        MaterialButton btnGoDashboard = findViewById(R.id.btnGoDashboard);
        MaterialButton btnAddAnother = findViewById(R.id.btnAddAnother);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        if (btnGoDashboard != null) {
            btnGoDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(VendorCourtSuccessActivity.this, VendorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (btnAddAnother != null) {
            btnAddAnother.setOnClickListener(v -> {
                startActivity(new Intent(VendorCourtSuccessActivity.this, AddCourtActivity.class));
                finish();
            });
        }
    }
}
