package com.example.futsalmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.UpdatePaymentStatusRequest;
import com.example.futsalmate.api.models.ViewBookingResponse;
import com.example.futsalmate.utils.TokenManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorBookingDetailsActivity extends AppCompatActivity {

    private static final String[] PAYMENT_STATUS_VALUES = {"Pending", "Paid", "Unpaid"};
    private int bookingId = -1;
    private String currentPaymentStatus = "";
    private boolean spinnerInitialized = false;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_booking_details);

        tokenManager = new TokenManager(this);
        bookingId = getIntent().getIntExtra("booking_id", -1);

        ImageView ivCourtImage = findViewById(R.id.ivCourtImage);
        TextView tvCourtName = findViewById(R.id.tvCourtName);
        TextView tvCourtLocation = findViewById(R.id.tvCourtLocation);
        TextView tvBookingTimeValue = findViewById(R.id.tvBookingTimeValue);
        TextView tvBookingDateValue = findViewById(R.id.tvBookingDateValue);
        TextView tvBookingStatusTag = findViewById(R.id.tvBookingStatusTag);
        TextView tvBookingStatusValue = findViewById(R.id.tvBookingStatusValue);
        TextView tvCustomerName = findViewById(R.id.tvCustomerName);
        TextView tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        TextView tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        Spinner spinnerPaymentStatus = findViewById(R.id.spinnerPaymentStatus);
        TextView tvBookingPrice = findViewById(R.id.tvBookingPrice);
        TextView tvBookingNotes = findViewById(R.id.tvBookingNotes);

        String courtName = getStringExtra("court_name");
        String courtLocation = getStringExtra("court_location");
        String courtImage = getStringExtra("court_image");
        String bookingDate = getStringExtra("booking_date");
        String startTime = getStringExtra("booking_start");
        String endTime = getStringExtra("booking_end");
        String bookingStatus = getStringExtra("booking_status");
        String paymentStatus = getStringExtra("booking_payment_status");
        currentPaymentStatus = normalizePaymentStatusForApi(paymentStatus);
        String paymentMethod = getStringExtra("booking_payment_method");
        String bookingNotes = getStringExtra("booking_notes");
        String customerName = getStringExtra("customer_name");
        String customerPhone = getStringExtra("customer_phone");
        String price = getStringExtra("court_price");

        tvCourtName.setText(safeText(courtName, "Court"));
        tvCourtLocation.setText(safeText(courtLocation, ""));
        tvBookingTimeValue.setText(formatTimeRange(startTime, endTime));
        tvBookingDateValue.setText(formatDate(bookingDate));
        String bookingStatusDisplay = safeText(toUpper(bookingStatus), "PENDING");
        tvBookingStatusTag.setText(bookingStatusDisplay);
        if (tvBookingStatusValue != null) {
            tvBookingStatusValue.setText(safeText(toUpper(bookingStatus), "PENDING"));
        }
        tvCustomerName.setText(safeText(customerName, "Unknown"));
        tvCustomerPhone.setText(safeText(customerPhone, "-"));
        tvPaymentMethod.setText(safeText(paymentMethod, "-"));
        tvBookingPrice.setText(formatPrice(price));
        if (tvBookingNotes != null) {
            tvBookingNotes.setText(safeText(bookingNotes, "—"));
        }

        // Payment status dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment_status_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentStatus.setAdapter(adapter);
        int initialPosition = positionForPaymentStatus(currentPaymentStatus);
        spinnerPaymentStatus.setSelection(initialPosition >= 0 ? initialPosition : 0);
        spinnerInitialized = true;

        spinnerPaymentStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnerInitialized || bookingId <= 0) return;
                String selected = PAYMENT_STATUS_VALUES[position];
                if (selected.equals(currentPaymentStatus)) return;
                updatePaymentStatus(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private String getStringExtra(String key) {
        String value = getIntent().getStringExtra(key);
        return value != null ? value : "";
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private String toUpper(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.US);
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

    private String formatPrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            return "Rs. 0";
        }
        return "Rs. " + price.trim();
    }

    private String resolveImageUrl(String image) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }
        String trimmed = image.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://futsalmateapp.sameem.in.net/" + trimmed.replaceFirst("^/+", "");
    }

    /** Map API payment_status to dropdown value (Pending, Paid, Unpaid). */
    private String normalizePaymentStatusForApi(String status) {
        if (status == null) return "Pending";
        String s = status.trim();
        if (s.equalsIgnoreCase("Paid")) return "Paid";
        if (s.equalsIgnoreCase("Unpaid") || s.equalsIgnoreCase("Failed")) return "Unpaid";
        return "Pending";
    }

    private int positionForPaymentStatus(String status) {
        for (int i = 0; i < PAYMENT_STATUS_VALUES.length; i++) {
            if (PAYMENT_STATUS_VALUES[i].equals(status)) return i;
        }
        return 0;
    }

    private void updatePaymentStatus(String paymentStatus) {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.getInstance().getApiService()
                .vendorUpdatePaymentStatus(token, bookingId, new UpdatePaymentStatusRequest(paymentStatus))
                .enqueue(new Callback<ViewBookingResponse>() {
                    @Override
                    public void onResponse(Call<ViewBookingResponse> call, Response<ViewBookingResponse> response) {
                        if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                            currentPaymentStatus = paymentStatus;
                            Toast.makeText(VendorBookingDetailsActivity.this, "Payment status updated.", Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = response.body() != null ? response.body().getMessage() : "Failed to update";
                            Toast.makeText(VendorBookingDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
                            spinnerInitialized = false;
                            ((Spinner) findViewById(R.id.spinnerPaymentStatus)).setSelection(positionForPaymentStatus(currentPaymentStatus));
                            spinnerInitialized = true;
                        }
                    }

                    @Override
                    public void onFailure(Call<ViewBookingResponse> call, Throwable t) {
                        Toast.makeText(VendorBookingDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        spinnerInitialized = false;
                        ((Spinner) findViewById(R.id.spinnerPaymentStatus)).setSelection(positionForPaymentStatus(currentPaymentStatus));
                        spinnerInitialized = true;
                    }
                });
    }
}
