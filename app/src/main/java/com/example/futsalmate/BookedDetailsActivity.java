package com.example.futsalmate;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;

public class BookedDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_details);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View btnDownload = findViewById(R.id.btnDownloadReceipt);
        View receiptContent = findViewById(R.id.receiptContent);

        if (btnDownload != null && receiptContent != null) {
            btnDownload.setOnClickListener(v -> {
                Bitmap bitmap = captureView(receiptContent);
                if (saveImageToGallery(bitmap)) {
                    Toast.makeText(this, "Receipt saved to Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save receipt", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Bitmap captureView(View view) {
        // Create a bitmap with the same dimensions as the view
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw the background color (matching the dashboard_bg)
        canvas.drawColor(Color.parseColor("#0A1E1A"));
        
        // Draw the view onto the canvas
        view.draw(canvas);
        return bitmap;
    }

    private boolean saveImageToGallery(Bitmap bitmap) {
        String fileName = "Receipt_" + System.currentTimeMillis() + ".jpg";
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FutsalMate");
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                java.io.File image = new java.io.File(imagesDir, fileName);
                fos = new java.io.FileOutputStream(image);
            }
            
            boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return saved;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
