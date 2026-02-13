package com.example.futsalmate;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.ViewBookingResponse;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private TextView tvConfirmationMessage;
    private ImageView ivCourtCover;
    private TextView tvCourtName;
    private TextView tvStatus;
    private TextView tvPitchInfo;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        tokenManager = new TokenManager(this);

        // Header Navigation
        ImageView btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        ivCourtCover = findViewById(R.id.ivCourtCover);
        tvCourtName = findViewById(R.id.tvCourtName);
        tvStatus = findViewById(R.id.tvStatus);
        tvPitchInfo = findViewById(R.id.tvPitchInfo);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);

        bindConfirmationMessage(null);

        // View My Bookings button logic
        MaterialButton btnViewBookings = findViewById(R.id.btnViewBookings);
        if (btnViewBookings != null) {
            btnViewBookings.setOnClickListener(v -> {
                Intent intent = new Intent(ConfirmationActivity.this, MainActivity.class);
                intent.putExtra("TARGET_FRAGMENT", "BOOKINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Optionally populating summary details
        populateBookingDetails();
    }

    private void populateBookingDetails() {
        int bookingId = getIntent().getIntExtra("BOOKING_ID", -1);
        if (bookingId <= 0) {
            return;
        }

        String token = tokenManager.getAuthHeader();
        if (token == null) {
            return;
        }

        RetrofitClient.getInstance().getApiService()
                .viewBookingDetail(token, bookingId)
                .enqueue(new Callback<ViewBookingResponse>() {
                    @Override
                    public void onResponse(Call<ViewBookingResponse> call, Response<ViewBookingResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getBooking() == null) {
                            return;
                        }
                        applyBookingToViews(response.body().getBooking());
                    }

                    @Override
                    public void onFailure(Call<ViewBookingResponse> call, Throwable t) {
                        // Keep the screen visible; fields will remain empty
                    }
                });
    }

    private void applyBookingToViews(Booking booking) {
        if (booking == null) {
            return;
        }

        Court court = booking.getCourt();

        bindConfirmationMessage(!TextUtils.isEmpty(booking.getCustomerName())
                ? booking.getCustomerName()
                : tokenManager != null ? tokenManager.getUserName() : null);

        if (tvCourtName != null) {
            tvCourtName.setText(court != null ? safeText(court.getCourtName()) : "");
        }
        if (tvLocation != null) {
            tvLocation.setText(court != null ? safeText(court.getLocation()) : "");
        }
        if (tvDate != null) {
            tvDate.setText(formatDate(booking.getDate()));
        }
        if (tvTime != null) {
            String start = booking.getStartTime();
            String end = booking.getEndTime();
            tvTime.setText(!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end) ? formatTimeRange(start, end) : "");
        }
        if (tvPitchInfo != null) {
            String price = court != null ? court.getPrice() : null;
            if (!TextUtils.isEmpty(price)) {
                tvPitchInfo.setText("Rs. " + price + " / hour");
            } else if (!TextUtils.isEmpty(booking.getPayment())) {
                tvPitchInfo.setText("Payment: " + booking.getPayment());
            } else {
                tvPitchInfo.setText("");
            }
        }

        if (tvStatus != null) {
            String status = booking.getStatus();
            if (TextUtils.isEmpty(status)) {
                tvStatus.setText("");
                tvStatus.setVisibility(TextView.GONE);
            } else {
                tvStatus.setVisibility(TextView.VISIBLE);
                tvStatus.setText(status.trim().toUpperCase(Locale.getDefault()));
                applyStatusTint(tvStatus, status);
            }
        }

        if (ivCourtCover != null) {
            String rawImage = court != null ? court.getImage() : null;
            String url = resolveFirstImageUrl(rawImage);
            if (TextUtils.isEmpty(url)) {
                ivCourtCover.setImageDrawable(null);
            } else {
                Glide.with(this)
                        .load(url)
                        .centerCrop()
                        .into(ivCourtCover);
            }
        }
    }

    private String formatTimeRange(String start, String end) {
        String startText = formatLocalTime(start);
        String endText = formatLocalTime(end);
        if (startText.isEmpty() && endText.isEmpty()) {
            return "";
        }
        if (startText.isEmpty()) {
            return endText;
        }
        if (endText.isEmpty()) {
            return startText;
        }
        return startText + " - " + endText;
    }

    private String formatLocalTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "";
        }
        String trimmed = time.trim();
        int dotIndex = trimmed.indexOf('.');
        if (dotIndex > 0) {
            trimmed = trimmed.substring(0, dotIndex);
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()),
                DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalTime parsed = LocalTime.parse(trimmed, formatter);
                return parsed.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
            } catch (Exception ignored) {
            }
        }
        return trimmed;
    }

    private void bindConfirmationMessage(String name) {
        if (tvConfirmationMessage == null) {
            return;
        }
        String fallbackName = tokenManager != null ? tokenManager.getUserName() : null;
        String finalName = !TextUtils.isEmpty(name) ? name : fallbackName;
        if (!TextUtils.isEmpty(finalName)) {
            String first = finalName.trim().split("\\s+")[0];
            tvConfirmationMessage.setText("Get your gear ready, " + first + "! You're all set to hit the pitch.");
        } else {
            tvConfirmationMessage.setText("You're all set to hit the pitch.");
        }
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }

    private String formatDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }
        String trimmed = raw.trim();
        try {
            LocalDate parsed = LocalDate.parse(trimmed);
            return parsed.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault()));
        } catch (Exception ignored) {
            return trimmed;
        }
    }

    private void applyStatusTint(TextView tv, String status) {
        String upper = status != null ? status.trim().toUpperCase(Locale.US) : "";
        int bg = Color.parseColor("#10B981"); // green default
        if (upper.contains("PEND")) {
            bg = Color.parseColor("#FACC15"); // yellow
        } else if (upper.contains("REJ") || upper.contains("CANC")) {
            bg = Color.parseColor("#EF4444"); // red
        } else if (upper.contains("CONF") || upper.contains("APPROV")) {
            bg = Color.parseColor("#10B981"); // green
        }
        tv.setBackgroundTintList(ColorStateList.valueOf(bg));
    }

    private String resolveFirstImageUrl(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String trimmed = raw.trim();
        String path = trimmed;

        if (trimmed.startsWith("[")) {
            try {
                JSONArray array = new JSONArray(trimmed);
                for (int i = 0; i < array.length(); i++) {
                    String candidate = array.optString(i, null);
                    if (candidate != null && !candidate.trim().isEmpty()) {
                        path = candidate.trim();
                        break;
                    }
                }
            } catch (JSONException ignored) {
                // fall back to raw string
            }
        }

        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String cleaned = path.trim().replace("\"", "");
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned;
        }
        // Normalize as absolute URL
        String base = "https://futsalmateapp.sameem.in.net/";
        Uri uri = Uri.parse(base + cleaned.replaceFirst("^/+", ""));
        return uri.toString();
    }
}
