package com.example.futsalmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class VendorUserDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_user_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        TextView tvDetailPhone = findViewById(R.id.tvDetailPhone);
        MaterialButton btnCallUser = findViewById(R.id.btnCallUser);
        MaterialButton btnMessageUser = findViewById(R.id.btnMessageUser);

        // Voice Call functionality
        if (btnCallUser != null && tvDetailPhone != null) {
            btnCallUser.setOnClickListener(v -> {
                String phoneNumber = tvDetailPhone.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            });
        }

        // Send Message functionality
        if (btnMessageUser != null && tvDetailPhone != null) {
            btnMessageUser.setOnClickListener(v -> {
                String phoneNumber = tvDetailPhone.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + phoneNumber));
                startActivity(intent);
            });
        }
    }
}
