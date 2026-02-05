package com.example.futsalmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.BookCourtRequest;
import com.example.futsalmate.api.models.BookCourtResponse;
import com.example.futsalmate.api.models.BookPaymentInfo;
import com.example.futsalmate.api.models.BookedTimesResponse;
import com.example.futsalmate.api.models.CourtDetail;
import com.example.futsalmate.api.models.CourtDetailBooking;
import com.example.futsalmate.api.models.CourtDetailResponse;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectTimeslotActivity extends AppCompatActivity {

    private TextView tvCourtHeaderInfo;
    private TextView tvMonthLabel;
    private TextView tvSelectedInfo;
    private EditText etBookingNotes;
    private ChipGroup cgTimeSlots;
    private MaterialCardView cardEsewa;
    private MaterialCardView cardCash;

    private TokenManager tokenManager;
    private LocalDate selectedDate = LocalDate.now();
    private String selectedStartTime;
    private String selectedEndTime;
    private String selectedPayment = "eSewa";
    private int courtId = -1;
    private int courtPrice = 0;
    private String openingTime;
    private String closingTime;
    private List<CourtDetailBooking> bookedSlots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_timeslot);

        tokenManager = new TokenManager(this);

        tvCourtHeaderInfo = findViewById(R.id.tvCourtHeaderInfo);
        tvMonthLabel = findViewById(R.id.tvMonthLabel);
        tvSelectedInfo = findViewById(R.id.tvSelectedInfo);
        etBookingNotes = findViewById(R.id.etBookingNotes);
        cgTimeSlots = findViewById(R.id.cgTimeSlots);
        cardEsewa = findViewById(R.id.cardEsewa);
        cardCash = findViewById(R.id.cardCash);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        readIntentExtras();
        updateDateLabel();
        updateSelectionInfo();
        setupTimeSlotSelection();
        loadCourtOperatingHours();
        setupPaymentSelection();

        findViewById(R.id.btnContinue).setOnClickListener(v -> attemptBooking());
        if (tvMonthLabel != null) {
            tvMonthLabel.setOnClickListener(v -> showDatePicker());
        }
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        courtId = intent.getIntExtra("court_id", -1);
        String courtName = intent.getStringExtra("court_name");
        String price = intent.getStringExtra("court_price");
        openingTime = intent.getStringExtra("opening_time");
        closingTime = intent.getStringExtra("closing_time");

        if (courtName != null && tvCourtHeaderInfo != null) {
            tvCourtHeaderInfo.setText(courtName);
        }
        if (price != null) {
            try {
                courtPrice = Integer.parseInt(price);
            } catch (NumberFormatException ignored) {
                courtPrice = 0;
            }
        }
    }

    private void setupTimeSlotSelection() {
        if (cgTimeSlots == null) {
            return;
        }
        cgTimeSlots.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                selectedStartTime = null;
                selectedEndTime = null;
                updateSelectionInfo();
                return;
            }
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                parseTimeRange(chip.getText().toString());
                updateSelectionInfo();
            }
        });
    }

    private void populateTimeSlots() {
        if (cgTimeSlots == null) {
            return;
        }
        cgTimeSlots.removeAllViews();
        selectedStartTime = null;
        selectedEndTime = null;
        updateSelectionInfo();

        LocalTime open = parseLocalTime(openingTime);
        LocalTime close = parseLocalTime(closingTime);

        if (open == null || close == null || !open.isBefore(close)) {
            Toast.makeText(this, "Operating hours are not available for this court.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        LocalTime cursor = open;
        while (cursor.isBefore(close)) {
            LocalTime next = cursor.plusHours(1);
            if (next.isAfter(close)) {
                break;
            }
            boolean isToday = selectedDate != null && selectedDate.equals(today);
            boolean isPastSlot = isToday && !cursor.isAfter(nowTime);
            boolean isBookedSlot = isSlotBooked(cursor, next);
            String label = formatSlot(cursor, next);
            Chip chip = createTimeChip(label);
            if (isPastSlot) {
                chip.setEnabled(false);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setTextColor(getColor(R.color.text_grey));
                chip.setChipBackgroundColorResource(R.color.card_bg_light);
            } else if (isBookedSlot) {
                chip.setEnabled(false);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setTextColor(getColor(R.color.white));
                chip.setChipBackgroundColorResource(R.color.logout_red);
            }
            cgTimeSlots.addView(chip);
            cursor = next;
        }
    }

    private void loadCourtOperatingHours() {
        if (openingTime != null && closingTime != null) {
            fetchBookedTimes(selectedDate != null ? selectedDate : LocalDate.now());
            return;
        }
        if (courtId <= 0) {
            Toast.makeText(this, "Missing court details", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = tokenManager.getAuthHeader();
        if (token != null) {
            RetrofitClient.getInstance().getApiService().showCourtDetail(token, courtId)
                    .enqueue(new Callback<CourtDetailResponse>() {
                        @Override
                        public void onResponse(Call<CourtDetailResponse> call, Response<CourtDetailResponse> response) {
                            if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                                Toast.makeText(SelectTimeslotActivity.this, "Failed to load operating hours", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            applyOperatingHours(response.body().getData());
                        }

                        @Override
                        public void onFailure(Call<CourtDetailResponse> call, Throwable t) {
                            Toast.makeText(SelectTimeslotActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return;
        }

        RetrofitClient.getInstance().getApiService().showCourtDetailPublic(courtId)
                .enqueue(new Callback<CourtDetailResponse>() {
                    @Override
                    public void onResponse(Call<CourtDetailResponse> call, Response<CourtDetailResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            Toast.makeText(SelectTimeslotActivity.this, "Failed to load operating hours", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        applyOperatingHours(response.body().getData());
                    }

                    @Override
                    public void onFailure(Call<CourtDetailResponse> call, Throwable t) {
                        Toast.makeText(SelectTimeslotActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyOperatingHours(CourtDetail detail) {
        if (detail == null) {
            Toast.makeText(this, "Operating hours are not available for this court.", Toast.LENGTH_SHORT).show();
            return;
        }
        openingTime = detail.getOpeningTime();
        closingTime = detail.getClosingTime();
        bookedSlots = new ArrayList<>();
        if (tvCourtHeaderInfo != null && detail.getCourtName() != null) {
            tvCourtHeaderInfo.setText(detail.getCourtName());
        }
        fetchBookedTimes(selectedDate != null ? selectedDate : LocalDate.now());
    }

    private void fetchBookedTimes(LocalDate date) {
        if (courtId <= 0) {
            populateTimeSlots();
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            populateTimeSlots();
            return;
        }

        String dateParam = date != null ? date.toString() : LocalDate.now().toString();
        RetrofitClient.getInstance().getApiService().getBookedTimes(token, courtId, dateParam)
                .enqueue(new Callback<BookedTimesResponse>() {
                    @Override
                    public void onResponse(Call<BookedTimesResponse> call, Response<BookedTimesResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(SelectTimeslotActivity.this, "Failed to load booked times", Toast.LENGTH_SHORT).show();
                            bookedSlots = new ArrayList<>();
                            populateTimeSlots();
                            return;
                        }
                        bookedSlots = response.body().getBookedTimes() != null
                                ? response.body().getBookedTimes()
                                : new ArrayList<>();
                        populateTimeSlots();
                    }

                    @Override
                    public void onFailure(Call<BookedTimesResponse> call, Throwable t) {
                        Toast.makeText(SelectTimeslotActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        bookedSlots = new ArrayList<>();
                        populateTimeSlots();
                    }
                });
    }

    private boolean isSlotBooked(LocalTime slotStart, LocalTime slotEnd) {
        if (bookedSlots == null || bookedSlots.isEmpty()) {
            return false;
        }
        for (CourtDetailBooking booking : bookedSlots) {
            if (booking == null) {
                continue;
            }
            LocalTime bookedStart = parseLocalTime(booking.getStartTime());
            LocalTime bookedEnd = parseLocalTime(booking.getEndTime());
            if (bookedStart == null || bookedEnd == null) {
                continue;
            }
            boolean overlaps = bookedStart.isBefore(slotEnd) && bookedEnd.isAfter(slotStart);
            if (overlaps) {
                return true;
            }
        }
        return false;
    }

    private Chip createTimeChip(String label) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setChipBackgroundColorResource(R.color.card_bg);
        chip.setTextColor(getColor(R.color.white));
        chip.setChipStrokeWidth(0f);
        return chip;
    }

    private String formatSlot(LocalTime start, LocalTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
        return start.format(formatter) + " - " + end.format(formatter);
    }

    private LocalTime parseLocalTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return null;
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
                return LocalTime.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }

    private void setupPaymentSelection() {
        if (cardEsewa != null) {
            cardEsewa.setOnClickListener(v -> setSelectedPayment("eSewa"));
        }
        if (cardCash != null) {
            cardCash.setOnClickListener(v -> setSelectedPayment("Cash"));
        }
        setSelectedPayment(selectedPayment);
    }

    private void setSelectedPayment(String payment) {
        selectedPayment = payment;
        boolean isEsewa = "eSewa".equalsIgnoreCase(payment);
        if (cardEsewa != null) {
            cardEsewa.setStrokeColor(getColor(isEsewa ? R.color.bright_green : R.color.card_bg_light));
            cardEsewa.setStrokeWidth(2);
        }
        if (cardCash != null) {
            cardCash.setStrokeColor(getColor(isEsewa ? R.color.card_bg_light : R.color.bright_green));
            cardCash.setStrokeWidth(2);
        }
        updateSelectionInfo();
    }

    private void showDatePicker() {
        LocalDate date = selectedDate != null ? selectedDate : LocalDate.now();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateDateLabel();
                    updateSelectionInfo();
                    fetchBookedTimes(selectedDate);
                },
                date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth()
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void updateDateLabel() {
        if (tvMonthLabel == null || selectedDate == null) {
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault());
        tvMonthLabel.setText(selectedDate.format(formatter));
    }

    private void parseTimeRange(String range) {
        if (range == null) {
            selectedStartTime = null;
            selectedEndTime = null;
            return;
        }
        String[] parts = range.split("-");
        if (parts.length >= 2) {
            selectedStartTime = parts[0].trim();
            selectedEndTime = parts[1].trim();
        }
    }

    private void updateSelectionInfo() {
        if (tvSelectedInfo == null) {
            return;
        }
        String timeRange = selectedStartTime != null && selectedEndTime != null
                ? selectedStartTime + " - " + selectedEndTime
                : "Select time";

        int total = calculateTotalPrice();
        String dateText = selectedDate != null ? selectedDate.toString() : "";
        tvSelectedInfo.setText("Selected: " + dateText + " • " + timeRange + " — Rs." + total);
    }

    private int calculateTotalPrice() {
        if (courtPrice <= 0 || selectedStartTime == null || selectedEndTime == null) {
            return courtPrice;
        }
        try {
            LocalTime start = LocalTime.parse(selectedStartTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end = LocalTime.parse(selectedEndTime, DateTimeFormatter.ofPattern("HH:mm"));
            long minutes = Duration.between(start, end).toMinutes();
            if (minutes <= 0) {
                return courtPrice;
            }
            double hours = minutes / 60.0;
            return (int) Math.round(courtPrice * hours);
        } catch (Exception e) {
            return courtPrice;
        }
    }

    private void attemptBooking() {
        if (courtId <= 0) {
            Toast.makeText(this, "Missing court details", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedStartTime == null || selectedEndTime == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = etBookingNotes != null ? etBookingNotes.getText().toString().trim() : null;
        String date = selectedDate != null ? selectedDate.toString() : LocalDate.now().toString();

        BookCourtRequest request = new BookCourtRequest(
                date,
            normalizeTime(selectedStartTime),
            normalizeTime(selectedEndTime),
                notes,
                courtId,
                selectedPayment
        );

        RetrofitClient.getInstance().getApiService().bookCourt(token, request)
                .enqueue(new Callback<BookCourtResponse>() {
                    @Override
                    public void onResponse(Call<BookCourtResponse> call, Response<BookCourtResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            String message = extractErrorMessage(response);
                            Toast.makeText(SelectTimeslotActivity.this, message, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        handleBookingResponse(response.body());
                    }

                    @Override
                    public void onFailure(Call<BookCourtResponse> call, Throwable t) {
                        Toast.makeText(SelectTimeslotActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleBookingResponse(BookCourtResponse response) {
        if (response == null) {
            Toast.makeText(this, "Booking failed", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("Cash".equalsIgnoreCase(selectedPayment) || response.getPayment() == null) {
            Toast.makeText(this, response.getMessage() != null ? response.getMessage() : "Booking confirmed", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ConfirmationActivity.class));
            return;
        }

        BookPaymentInfo payment = response.getPayment();
        if (payment == null || payment.getPaymentUrl() == null) {
            Toast.makeText(this, "Payment info missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EsewaPaymentActivity.class);
        intent.putExtra("payment_url", payment.getPaymentUrl());
        intent.putExtra("amount", payment.getAmount() != null ? payment.getAmount() : 0);
        intent.putExtra("tax_amount", payment.getTaxAmount() != null ? payment.getTaxAmount() : 0);
        intent.putExtra("total_amount", payment.getTotalAmount() != null ? payment.getTotalAmount() : 0);
        intent.putExtra("transaction_uuid", payment.getTransactionUuid());
        intent.putExtra("product_code", payment.getProductCode());
        intent.putExtra("product_service_charge", payment.getProductServiceCharge() != null ? payment.getProductServiceCharge() : 0);
        intent.putExtra("product_delivery_charge", payment.getProductDeliveryCharge() != null ? payment.getProductDeliveryCharge() : 0);
        intent.putExtra("success_url", payment.getSuccessUrl());
        intent.putExtra("failure_url", payment.getFailureUrl());
        intent.putExtra("signed_field_names", payment.getSignedFieldNames());
        intent.putExtra("signature", payment.getSignature());
        startActivity(intent);
    }

    private String normalizeTime(String time) {
        if (time == null) {
            return null;
        }
        LocalTime localTime = parseLocalTime(time);
        if (localTime == null) {
            return time.trim();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault());
        return localTime.format(formatter);
    }

    private String extractErrorMessage(Response<?> response) {
        if (response == null) {
            return "Booking failed";
        }
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                JSONObject json = new JSONObject(body);
                if (json.has("message")) {
                    return json.getString("message");
                }
            }
        } catch (Exception ignored) {
            // ignored
        }
        return "Booking failed (" + response.code() + ")";
    }
}
