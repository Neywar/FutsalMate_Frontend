package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BookedDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_details);

        // Find views
        TextView tvVenueName = findViewById(R.id.tvVenueName);
        TextView tvCourtInfo = findViewById(R.id.tvCourtInfo);
        ImageView ivCourtImage = findViewById(R.id.ivCourtImage);
        TextView tvBookingDateValue = findViewById(R.id.tvBookingDateValue);
        TextView tvBookingTimeValue = findViewById(R.id.tvBookingTimeValue);
        TextView tvBookingStatusTag = findViewById(R.id.tvBookingStatusTag);
        TextView tvBookingPrice = findViewById(R.id.tvBookingPrice);
        View btnEditBooking = findViewById(R.id.btnEditBooking);
        View btnDeleteBooking = findViewById(R.id.btnDeleteBooking);

        // Get data from intent
        String courtName = getExtraFirst("court_name");
        String courtLocation = getExtraFirst("court_location");
        String courtImage = getExtraFirst("court_image");
        String bookingDate = getExtraFirst("booking_date");
        String startTime = getExtraFirst("booking_start", "start_time");
        String endTime = getExtraFirst("booking_end", "end_time");
        String bookingStatus = getExtraFirst("booking_status", "status");
        String paymentStatus = getExtraFirst("booking_payment_status");
        String price = getExtraFirst("court_price");

        // Populate views
        tvVenueName.setText(safeText(courtName));
        tvCourtInfo.setText(safeText(courtLocation));
        tvBookingDateValue.setText(formatDate(bookingDate));
        tvBookingTimeValue.setText(formatTimeRange(startTime, endTime));

        String statusLabel = buildStatusLabel(bookingStatus, paymentStatus);
        tvBookingStatusTag.setText(statusLabel);
        applyStatusStyle(tvBookingStatusTag, statusLabel);

        tvBookingPrice.setText(formatPrice(price));

        String imageUrl = resolveImageUrl(courtImage);
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_court_one)
                .error(R.drawable.ic_court_one)
                .centerCrop()
                .into(ivCourtImage);
        } else {
            Glide.with(this)
                .load(R.drawable.ic_court_one)
                .centerCrop()
                .into(ivCourtImage);
        }

        boolean disableActions = isWithinTwoHours(bookingDate, startTime);
        int bookingId = getIntent().getIntExtra("booking_id", -1);

        if (btnEditBooking != null) {
            btnEditBooking.setEnabled(!disableActions && bookingId > 0);
            btnEditBooking.setAlpha(disableActions || bookingId <= 0 ? 0.5f : 1f);
            if (disableActions || bookingId <= 0) {
                btnEditBooking.setOnClickListener(v ->
                        Toast.makeText(this, "Cannot edit within 2 hours of start time", Toast.LENGTH_SHORT).show()
                );
            } else {
                btnEditBooking.setOnClickListener(v -> {
                    Intent intent = new Intent(BookedDetailsActivity.this, EditTimeslotActivity.class);
                    intent.putExtra("booking_id", bookingId);
                    intent.putExtra("court_id", getIntent().getIntExtra("court_id", -1));
                    intent.putExtra("court_name", courtName);
                    intent.putExtra("court_price", price);
                    intent.putExtra("opening_time", getIntent().getStringExtra("opening_time"));
                    intent.putExtra("closing_time", getIntent().getStringExtra("closing_time"));
                    intent.putExtra("date", bookingDate);
                    intent.putExtra("start_time", startTime);
                    intent.putExtra("end_time", endTime);
                    startActivity(intent);
                });
            }
        }

        if (btnDeleteBooking != null) {
            btnDeleteBooking.setEnabled(!disableActions && bookingId > 0);
            btnDeleteBooking.setAlpha(disableActions || bookingId <= 0 ? 0.5f : 1f);
            if (disableActions || bookingId <= 0) {
                btnDeleteBooking.setOnClickListener(v ->
                        Toast.makeText(this, "Cannot delete within 2 hours of start time", Toast.LENGTH_SHORT).show()
                );
            } else {
                btnDeleteBooking.setOnClickListener(v -> confirmDeleteBooking(bookingId));
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void confirmDeleteBooking(int bookingId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete booking")
                .setMessage("Are you sure you want to delete this booking? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteBooking(bookingId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBooking(int bookingId) {
        com.example.futsalmate.utils.TokenManager tokenManager = new com.example.futsalmate.utils.TokenManager(this);
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.futsalmate.api.RetrofitClient.getInstance()
                .getApiService()
                .cancelBooking(token, bookingId)
                .enqueue(new retrofit2.Callback<com.example.futsalmate.api.models.ViewBookingResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.futsalmate.api.models.ViewBookingResponse> call,
                                           retrofit2.Response<com.example.futsalmate.api.models.ViewBookingResponse> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(BookedDetailsActivity.this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(BookedDetailsActivity.this, "Booking deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.futsalmate.api.models.ViewBookingResponse> call, Throwable t) {
                        Toast.makeText(BookedDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getExtraFirst(String... keys) {
        if (keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            String value = getIntent().getStringExtra(key);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }

    private String formatDate(String date) {
        if (TextUtils.isEmpty(date)) {
            return "";
        }
        try {
            LocalDate parsed = LocalDate.parse(date.trim());
            return parsed.format(DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.getDefault()));
        } catch (Exception ignored) {
            return date;
        }
    }

    private String formatTimeRange(String start, String end) {
        String startText = formatLocalTime(start);
        String endText = formatLocalTime(end);
        if (TextUtils.isEmpty(startText) && TextUtils.isEmpty(endText)) {
            return "";
        }
        if (TextUtils.isEmpty(startText)) {
            return endText;
        }
        if (TextUtils.isEmpty(endText)) {
            return startText;
        }
        return startText + " - " + endText;
    }

    private String formatLocalTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "";
        }
        try {
            String trimmed = time.trim();
            int dotIndex = trimmed.indexOf('.');
            if (dotIndex > 0) {
                trimmed = trimmed.substring(0, dotIndex);
            }
            LocalTime localTime = LocalTime.parse(trimmed);
            return localTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
        } catch (Exception e) {
            return time; // Fallback to original time if parsing fails
        }
    }

    private LocalTime parseLocalTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return null;
        }
        try {
            String trimmed = time.trim();
            int dotIndex = trimmed.indexOf('.');
            if (dotIndex > 0) {
                trimmed = trimmed.substring(0, dotIndex);
            }
            return LocalTime.parse(trimmed);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isWithinTwoHours(String date, String startTime) {
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(startTime)) {
            return false;
        }
        try {
            LocalDate bookingDate = LocalDate.parse(date.trim());
            LocalTime bookingStart = parseLocalTime(startTime);
            if (bookingStart == null) {
                return false;
            }
            LocalDateTime startDateTime = LocalDateTime.of(bookingDate, bookingStart);
            Duration duration = Duration.between(LocalDateTime.now(), startDateTime);
            return duration.toMinutes() <= 120;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String buildStatusLabel(String bookingStatus, String paymentStatus) {
        if (!TextUtils.isEmpty(paymentStatus)) {
            return paymentStatus.trim().toUpperCase(Locale.getDefault());
        }
        if (!TextUtils.isEmpty(bookingStatus)) {
            return bookingStatus.trim().toUpperCase(Locale.getDefault());
        }
        return "PENDING";
    }

    private void applyStatusStyle(TextView tvStatus, String status) {
        if (tvStatus == null || status == null) {
            return;
        }
        String upper = status.toUpperCase(Locale.getDefault());
        if (upper.contains("PAID")) {
            tvStatus.setBackgroundTintList(getColorStateList(R.color.status_paid));
            tvStatus.setTextColor(getColor(R.color.bright_green));
        } else if (upper.contains("CONFIRMED")) {
            tvStatus.setBackgroundTintList(getColorStateList(R.color.status_paid));
            tvStatus.setTextColor(getColor(R.color.bright_green));
        } else if (upper.contains("CANCEL") || upper.contains("REJECT")) {
            tvStatus.setBackgroundTintList(getColorStateList(R.color.status_cancelled));
            tvStatus.setTextColor(getColor(R.color.text_grey));
        } else {
            tvStatus.setBackgroundTintList(getColorStateList(R.color.status_pending));
            tvStatus.setTextColor(getColor(R.color.action_yellow));
        }
    }

    private String formatPrice(String price) {
        if (TextUtils.isEmpty(price)) {
            return "Rs. -";
        }
        return "Rs." + price;
    }

    private String resolveImageUrl(String image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String cleaned = extractFirstImage(image);
        if (cleaned == null || cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned;
        }
        return "https://futsalmateapp.sameem.in.net/" + cleaned.replaceFirst("^/+", "");
    }

    private String extractFirstImage(String image) {
        String trimmed = image.trim();
        if (trimmed.startsWith("[")) {
            try {
                JSONArray array = new JSONArray(trimmed);
                if (array.length() > 0) {
                    return array.optString(0, null);
                }
                return null;
            } catch (JSONException e) {
                return null;
            }
        }
        return trimmed;
    }
}
