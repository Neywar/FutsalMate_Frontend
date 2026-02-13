package com.example.futsalmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

        de.hdodenhof.circleimageview.CircleImageView ivUserDetailAvatar = findViewById(R.id.ivUserDetailAvatar);
        TextView tvUserDetailName = findViewById(R.id.tvUserDetailName);
        TextView tvDetailPhone = findViewById(R.id.tvDetailPhone);
        TextView tvDetailEmail = findViewById(R.id.tvDetailEmail);
        TextView tvUserDetailVerification = findViewById(R.id.tvUserDetailVerification);
        TextView tvInsightLeftValue = findViewById(R.id.tvInsightLeftValue);
        TextView tvInsightRightValue = findViewById(R.id.tvInsightRightValue);

        MaterialButton btnCallUser = findViewById(R.id.btnCallUser);
        MaterialButton btnMessageUser = findViewById(R.id.btnMessageUser);

        // Bind data passed from VendorUsersFragment/VendorUserHistoryActivity
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("customer_name");
            String phone = intent.getStringExtra("customer_phone");
            String email = intent.getStringExtra("customer_email");
            int totalBookings = intent.getIntExtra("total_bookings", 0);
            String totalSpent = intent.getStringExtra("total_spent");
            boolean isVerified = intent.getBooleanExtra("is_email_verified", false);
            String emailVerifiedAtStr = intent.getStringExtra("email_verified_at");
            String profilePhoto = intent.getStringExtra("customer_profile_photo");
            if (!isVerified && emailVerifiedAtStr != null && !emailVerifiedAtStr.trim().isEmpty()) {
                isVerified = true;
            }

            if (ivUserDetailAvatar != null) {
                if (profilePhoto != null && !profilePhoto.trim().isEmpty()) {
                    String url = resolveImageUrl(profilePhoto);
                    Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .centerCrop()
                            .into(ivUserDetailAvatar);
                } else {
                    ivUserDetailAvatar.setImageResource(R.drawable.ic_person);
                }
            }

            if (tvUserDetailName != null && name != null && !name.isEmpty()) {
                tvUserDetailName.setText(name);
            }
            if (tvDetailPhone != null && phone != null && !phone.isEmpty()) {
                tvDetailPhone.setText(phone);
            }
            if (tvDetailEmail != null && email != null && !email.isEmpty()) {
                tvDetailEmail.setText(email);
            }
            if (tvInsightLeftValue != null) {
                tvInsightLeftValue.setText(String.valueOf(totalBookings));
            }
            if (tvInsightRightValue != null) {
                tvInsightRightValue.setText("Rs. " + (totalSpent != null && !totalSpent.isEmpty() ? totalSpent : "0.00"));
            }

            if (tvUserDetailVerification != null) {
                if (isVerified) {
                    tvUserDetailVerification.setText("Verified Player");
                    tvUserDetailVerification.setTextColor(android.graphics.Color.parseColor("#FACC15"));
                } else {
                    tvUserDetailVerification.setText("Unverified Player");
                    tvUserDetailVerification.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
                }
            }
        }

        // Voice Call functionality
        if (btnCallUser != null && tvDetailPhone != null) {
            btnCallUser.setOnClickListener(v -> {
                String phoneNumber = tvDetailPhone.getText().toString().trim();
                if (phoneNumber.isEmpty() || "—".equals(phoneNumber)) {
                    return;
                }
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(dialIntent);
            });
        }

        // Send Message functionality
        if (btnMessageUser != null && tvDetailPhone != null) {
            btnMessageUser.setOnClickListener(v -> {
                String phoneNumber = tvDetailPhone.getText().toString().trim();
                if (phoneNumber.isEmpty() || "—".equals(phoneNumber)) {
                    return;
                }
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
                startActivity(smsIntent);
            });
        }
    }

    private String resolveImageUrl(String image) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }
        String cleaned = image.trim();
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned;
        }
        return "https://futsalmateapp.sameem.in.net/" + cleaned.replaceFirst("^/+", "");
    }
}
