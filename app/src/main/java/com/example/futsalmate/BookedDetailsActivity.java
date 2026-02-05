package com.example.futsalmate;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.ViewBookingResponse;
import com.example.futsalmate.utils.TokenManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import java.io.OutputStream;

public class BookedDetailsActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private int bookingId = -1;
    private Booking currentBooking;
    private TextView tvVenueName;
    private TextView tvCourtInfo;
    private TextView tvBookingDateValue;
    private TextView tvBookingTimeValue;
    private TextView tvBookingStatusTag;
    private TextView tvBookingRef;
    private TextView tvBookingPrice;
    private View btnEditBooking;
    private View btnDeleteBooking;
    private String sourceFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_details);

        tokenManager = new TokenManager(this);
        bookingId = getIntent().getIntExtra("booking_id", -1);
        sourceFilter = getIntent().getStringExtra("booking_filter");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View btnDownload = findViewById(R.id.btnDownloadReceipt);
        View receiptContent = findViewById(R.id.receiptContent);

        tvVenueName = findViewById(R.id.tvVenueName);
        tvCourtInfo = findViewById(R.id.tvCourtInfo);
        tvBookingDateValue = findViewById(R.id.tvBookingDateValue);
        tvBookingTimeValue = findViewById(R.id.tvBookingTimeValue);
        tvBookingStatusTag = findViewById(R.id.tvBookingStatusTag);
        tvBookingPrice = findViewById(R.id.tvBookingPrice);

        btnEditBooking = findViewById(R.id.btnEditBooking);
        btnDeleteBooking = findViewById(R.id.btnDeleteBooking);

        if (btnEditBooking != null) {
            btnEditBooking.setOnClickListener(v -> openEditBooking());
        }
        if (btnDeleteBooking != null) {
            btnDeleteBooking.setOnClickListener(v -> confirmDeleteBooking());
        }

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

        bindFromExtras();
        if ("CURRENT".equalsIgnoreCase(sourceFilter) || "PAST".equalsIgnoreCase(sourceFilter)) {
            disableActions();
        }
        loadBookingDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookingDetails();
    }

    private void loadBookingDetails() {
        if (bookingId <= 0) {
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.getInstance().getApiService().viewBookingDetail(token, bookingId)
                .enqueue(new retrofit2.Callback<ViewBookingResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<ViewBookingResponse> call, retrofit2.Response<ViewBookingResponse> response) {
                        if (response.code() == 403) {
                            Toast.makeText(BookedDetailsActivity.this, "Unauthorized booking", Toast.LENGTH_SHORT).show();
                            disableActions();
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null || response.body().getBooking() == null) {
                            Toast.makeText(BookedDetailsActivity.this, "Failed to load booking", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        currentBooking = response.body().getBooking();
                        bindBooking(currentBooking);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ViewBookingResponse> call, Throwable t) {
                        Toast.makeText(BookedDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindFromExtras() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String courtName = intent.getStringExtra("court_name");
        String courtLocation = intent.getStringExtra("court_location");
        String price = intent.getStringExtra("court_price");
        String date = intent.getStringExtra("booking_date");
        String start = intent.getStringExtra("booking_start");
        String end = intent.getStringExtra("booking_end");
        String status = intent.getStringExtra("booking_status");
        String paymentStatus = intent.getStringExtra("booking_payment_status");

        if (tvVenueName != null && courtName != null) {
            tvVenueName.setText(courtName);
        }
        if (tvCourtInfo != null && courtLocation != null) {
            tvCourtInfo.setText(courtLocation);
        }
        if (tvBookingDateValue != null && date != null) {
            tvBookingDateValue.setText(formatDate(date));
        }
        if (tvBookingTimeValue != null && (start != null || end != null)) {
            tvBookingTimeValue.setText(formatTimeRange(start, end));
        }
        if (tvBookingRef != null && bookingId > 0) {
            tvBookingRef.setText("Ref: #BK-" + bookingId);
        }
        if (tvBookingPrice != null && price != null) {
            tvBookingPrice.setText("Rs." + price);
        }
        if (status != null || paymentStatus != null) {
            bindStatusTag(status, paymentStatus);
        }
        updateActionsForBooking(date, start, end, status);
    }

    private void disableActions() {
        if (btnEditBooking != null) {
            btnEditBooking.setEnabled(false);
            btnEditBooking.setAlpha(0.6f);
        }
        if (btnDeleteBooking != null) {
            btnDeleteBooking.setEnabled(false);
            btnDeleteBooking.setAlpha(0.6f);
        }
    }

    private void enableActions() {
        if (btnEditBooking != null) {
            btnEditBooking.setEnabled(true);
            btnEditBooking.setAlpha(1f);
        }
        if (btnDeleteBooking != null) {
            btnDeleteBooking.setEnabled(true);
            btnDeleteBooking.setAlpha(1f);
        }
    }

    private void bindBooking(Booking booking) {
        if (booking == null) {
            return;
        }
        String courtName = booking.getCourt() != null ? booking.getCourt().getCourtName() : "Court";
        if (tvVenueName != null) {
            tvVenueName.setText(courtName);
        }
        if (tvCourtInfo != null) {
            String location = booking.getCourt() != null ? booking.getCourt().getLocation() : "";
            tvCourtInfo.setText(location != null && !location.isEmpty() ? location : "Court booking");
        }
        if (tvBookingDateValue != null) {
            tvBookingDateValue.setText(formatDate(booking.getDate()));
        }
        if (tvBookingTimeValue != null) {
            tvBookingTimeValue.setText(formatTimeRange(booking.getStartTime(), booking.getEndTime()));
        }
        if (tvBookingRef != null) {
            tvBookingRef.setText("Ref: #BK-" + booking.getId());
        }
        if (tvBookingPrice != null) {
            String price = booking.getCourt() != null && booking.getCourt().getPrice() != null
                    ? booking.getCourt().getPrice()
                    : "0";
            tvBookingPrice.setText("Rs." + price);
        }
        bindStatusTag(booking.getStatus(), booking.getPaymentStatus());
        updateActionsForBooking(booking.getDate(), booking.getStartTime(), booking.getEndTime(), booking.getStatus());
    }

    private void updateActionsForBooking(String date, String start, String end, String status) {
        if (isPastOrCurrent(date, start, end) || isWithinTwoHoursBeforeStart(date, start) || isFinalStatus(status)) {
            disableActions();
        } else {
            enableActions();
        }
    }

    private boolean isFinalStatus(String status) {
        if (status == null) return false;
        return "Cancelled".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status);
    }

    private boolean isPastOrCurrent(String date, String start, String end) {
        LocalDate bookingDate = parseDate(date);
        LocalTime startTime = parseTime(start);
        LocalTime endTime = parseTime(end);
        if (bookingDate == null || startTime == null || endTime == null) {
            return true;
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (bookingDate.isBefore(today)) {
            return true;
        }
        if (bookingDate.isAfter(today)) {
            return false;
        }
        return !startTime.isAfter(now);
    }

    private boolean isWithinTwoHoursBeforeStart(String date, String start) {
        LocalDate bookingDate = parseDate(date);
        LocalTime startTime = parseTime(start);
        if (bookingDate == null || startTime == null) {
            return true;
        }
        LocalDateTime bookingStart = LocalDateTime.of(bookingDate, startTime);
        LocalDateTime cutoff = bookingStart.minusHours(2);
        return !LocalDateTime.now().isBefore(cutoff);
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.trim().isEmpty()) return null;
        String trimmed = time.trim();
        int dotIndex = trimmed.indexOf('.');
        if (dotIndex > 0) {
            trimmed = trimmed.substring(0, dotIndex);
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("H:mm")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(trimmed, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void bindStatusTag(String status, String paymentStatus) {
        if (tvBookingStatusTag == null) return;
        String label = status != null ? status : "Pending";
        int bgColor = R.color.status_pending;
        int fgColor = R.color.action_yellow;
        if ("Confirmed".equalsIgnoreCase(status) || "Paid".equalsIgnoreCase(paymentStatus)) {
            label = "PAID";
            bgColor = R.color.status_paid;
            fgColor = R.color.bright_green;
        } else if ("Cancelled".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
            label = status.toUpperCase(Locale.getDefault());
            bgColor = R.color.status_cancelled;
            fgColor = R.color.text_grey;
        } else if ("Pending".equalsIgnoreCase(status)) {
            label = "PENDING";
            bgColor = R.color.status_pending;
            fgColor = R.color.action_yellow;
        }
        tvBookingStatusTag.setText(label);
        tvBookingStatusTag.setBackgroundTintList(ContextCompat.getColorStateList(this, bgColor));
        tvBookingStatusTag.setTextColor(ContextCompat.getColor(this, fgColor));
    }

    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) return "";
        try {
            LocalDate parsed = LocalDate.parse(date.trim());
            return parsed.format(DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.getDefault()));
        } catch (Exception ignored) {
            return date;
        }
    }

    private String formatTimeRange(String start, String end) {
        String s = formatLocalTime(start);
        String e = formatLocalTime(end);
        if (s.isEmpty() && e.isEmpty()) return "";
        return s + " - " + e;
    }

    private String formatLocalTime(String time) {
        if (time == null || time.trim().isEmpty()) return "";
        String trimmed = time.trim();
        int dotIndex = trimmed.indexOf('.');
        if (dotIndex > 0) {
            trimmed = trimmed.substring(0, dotIndex);
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("H:mm")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalTime parsed = LocalTime.parse(trimmed, formatter);
                return parsed.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
            } catch (Exception ignored) {
            }
        }
        return trimmed;
    }

    private void openEditBooking() {
        if (currentBooking == null) {
            Toast.makeText(this, "Booking details are not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Edit booking")
                .setMessage("Do you want to edit this booking?")
                .setPositiveButton("Edit", (dialog, which) -> launchEditBooking())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void launchEditBooking() {
        Intent intent = new Intent(this, EditTimeslotActivity.class);
        intent.putExtra("booking_id", currentBooking.getId());
        if (currentBooking.getCourt() != null) {
            intent.putExtra("court_id", currentBooking.getCourt().getId());
            intent.putExtra("court_name", currentBooking.getCourt().getCourtName());
            intent.putExtra("court_price", currentBooking.getCourt().getPrice());
            intent.putExtra("opening_time", currentBooking.getCourt().getOpeningTime());
            intent.putExtra("closing_time", currentBooking.getCourt().getClosingTime());
        }
        intent.putExtra("date", currentBooking.getDate());
        intent.putExtra("start_time", currentBooking.getStartTime());
        intent.putExtra("end_time", currentBooking.getEndTime());
        startActivity(intent);
    }

    private void confirmDeleteBooking() {
        if (bookingId <= 0) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete booking")
                .setMessage("Are you sure you want to delete this booking? This will cancel the booking.")
                .setPositiveButton("Delete", (dialog, which) -> deleteBooking())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBooking() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.getInstance().getApiService().cancelBooking(token, bookingId)
                .enqueue(new retrofit2.Callback<ViewBookingResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<ViewBookingResponse> call, retrofit2.Response<ViewBookingResponse> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(BookedDetailsActivity.this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(BookedDetailsActivity.this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ViewBookingResponse> call, Throwable t) {
                        Toast.makeText(BookedDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
