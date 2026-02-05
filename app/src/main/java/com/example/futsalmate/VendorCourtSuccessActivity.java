package com.example.futsalmate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class VendorCourtSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_court_success);

        ImageView btnClose = findViewById(R.id.btnClose);
        MaterialButton btnGoDashboard = findViewById(R.id.btnGoDashboard);
        MaterialButton btnAddAnother = findViewById(R.id.btnAddAnother);
        TextView tvPublishedName = findViewById(R.id.tvPublishedName);
        TextView tvPublishedPrice = findViewById(R.id.tvPublishedPrice);
        ImageView ivCourtPreview = findViewById(R.id.ivCourtPreview);
        
        // Get court data from intent
        String courtName = getIntent().getStringExtra("COURT_NAME");
        String courtPrice = getIntent().getStringExtra("COURT_PRICE");
        String imageUrl = getIntent().getStringExtra("COURT_IMAGE_URL");
        boolean hasLocalImage = getIntent().getBooleanExtra("HAS_LOCAL_IMAGE", false);
        String localImagePath = getIntent().getStringExtra("LOCAL_IMAGE_PATH");
        
        Log.d("CourtSuccess", "Court name: " + courtName);
        Log.d("CourtSuccess", "Image URL: " + imageUrl);
        Log.d("CourtSuccess", "Has local image: " + hasLocalImage);
        Log.d("CourtSuccess", "Local path: " + localImagePath);
        
        // Display court data
        if (courtName != null && !courtName.isEmpty()) {
            tvPublishedName.setText(courtName);
        }
        
        if (courtPrice != null && !courtPrice.isEmpty()) {
            tvPublishedPrice.setText("Rs. " + courtPrice + " / hr");
        }
        
        // Load image - priority: URL > Local file > Default
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("CourtSuccess", "Loading from URL: " + imageUrl);
            loadImageFromUrl(imageUrl, ivCourtPreview);
        } else if (hasLocalImage && localImagePath != null) {
            Log.d("CourtSuccess", "Loading from local file");
            loadImageFromFile(localImagePath, ivCourtPreview);
        } else {
            Log.d("CourtSuccess", "No image available");
        }

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
    
    private void loadImageFromUrl(String urlString, ImageView imageView) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                InputStream inputStream = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                runOnUiThread(() -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        Log.d("CourtSuccess", "Image loaded from URL successfully");
                    } else {
                        Log.e("CourtSuccess", "Failed to decode bitmap from URL");
                    }
                });
            } catch (Exception e) {
                Log.e("CourtSuccess", "Error loading image from URL", e);
                e.printStackTrace();
            }
        }).start();
    }
    
    private void loadImageFromFile(String filePath, ImageView imageView) {
        new Thread(() -> {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            Log.d("CourtSuccess", "Image loaded from file successfully");
                        } else {
                            Log.e("CourtSuccess", "Failed to decode bitmap from file");
                        }
                    });
                } else {
                    Log.e("CourtSuccess", "File does not exist: " + filePath);
                }
            } catch (Exception e) {
                Log.e("CourtSuccess", "Error loading image from file", e);
                e.printStackTrace();
            }
        }).start();
    }
}
