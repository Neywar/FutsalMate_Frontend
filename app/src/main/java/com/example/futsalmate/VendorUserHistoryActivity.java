package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class VendorUserHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_user_history);

        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnContactUser = findViewById(R.id.btnContactUser);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnContactUser != null) {
            btnContactUser.setOnClickListener(v -> {
                Intent intent = new Intent(VendorUserHistoryActivity.this, VendorUserDetailsActivity.class);
                startActivity(intent);
            });
        }
    }
}
