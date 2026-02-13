package com.example.futsalmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.BookedTimesResponse;
import com.example.futsalmate.api.models.CourtDetail;
import com.example.futsalmate.api.models.CourtDetailBooking;
import com.example.futsalmate.api.models.CourtDetailResponse;
import com.example.futsalmate.api.models.EditBookingRequest;
import com.example.futsalmate.api.models.ViewBookingResponse;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTimeslotActivity extends AppCompatActivity {

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
    private int courtId = -1;
    private int courtPrice = 0;
    private String openingTime;
    private String closingTime;
    private int bookingId = -1;
    private String originalDate;
    private String originalStart;
    private String originalEnd;
    private List<CourtDetailBooking> bookedSlots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_timeslot);

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

        if (etBookingNotes != null) {
            etBookingNotes.setEnabled(false);
            etBookingNotes.setVisibility(View.GONE);
        }
        if (cardEsewa != null) {
            cardEsewa.setVisibility(View.GONE);
        }
        if (cardCash != null) {
            cardCash.setVisibility(View.GONE);
        }
        View notesLabel = findViewById(R.id.tvBookingNotesLabel);
        if (notesLabel != null) {
            notesLabel.setVisibility(View.GONE);
        }
        View paymentLabel = findViewById(R.id.tvPaymentMethodLabel);
        if (paymentLabel != null) {
            paymentLabel.setVisibility(View.GONE);
        }

        findViewById(R.id.btnContinue).setOnClickListener(v -> confirmUpdateBooking());
        if (tvMonthLabel != null) {
            tvMonthLabel.setOnClickListener(v -> showDatePicker());
        }
    }

    private void confirmUpdateBooking() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Update booking")
                .setMessage("Do you want to update this booking? You cannot update the booking before 2 hours of your game start time.")
                .setPositiveButton("Update", (dialog, which) -> attemptUpdate())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        bookingId = intent.getIntExtra("booking_id", -1);
        courtId = intent.getIntExtra("court_id", -1);
        String courtName = intent.getStringExtra("court_name");
        String price = intent.getStringExtra("court_price");
        openingTime = intent.getStringExtra("opening_time");
        closingTime = intent.getStringExtra("closing_time");
        originalDate = intent.getStringExtra("date");
        originalStart = intent.getStringExtra("start_time");
        originalEnd = intent.getStringExtra("end_time");

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
        if (originalDate != null) {
            try {
                selectedDate = LocalDate.parse(originalDate);
            } catch (Exception ignored) {
            }
        }
        selectedStartTime = normalizeDisplayTime(originalStart);
        selectedEndTime = normalizeDisplayTime(originalEnd);
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
            boolean isOriginalSlot = isOriginalSlot(cursor, next);
            String label = formatSlot(cursor, next);
            Chip chip = createTimeChip(label);

            if (isOriginalSlot) {
                chip.setChecked(true);
                selectedStartTime = formatLocalTime(cursor);
                selectedEndTime = formatLocalTime(next);
            }

            if (isPastSlot) {
                chip.setEnabled(false);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setTextColor(getColor(R.color.text_grey));
                chip.setChipBackgroundColorResource(R.color.card_bg_light);
            } else if (isBookedSlot && !isOriginalSlot) {
                chip.setEnabled(false);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setTextColor(getColor(R.color.white));
                chip.setChipBackgroundColorResource(R.color.logout_red);
            }
            cgTimeSlots.addView(chip);
            cursor = next;
        }
        updateSelectionInfo();
    }

    private boolean isOriginalSlot(LocalTime slotStart, LocalTime slotEnd) {
        if (originalDate == null || originalStart == null || originalEnd == null || selectedDate == null) {
            return false;
        }
        try {
            LocalDate original = LocalDate.parse(originalDate);
            if (!selectedDate.equals(original)) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
        LocalTime originalStartTime = parseLocalTime(originalStart);
        LocalTime originalEndTime = parseLocalTime(originalEnd);
        return originalStartTime != null && originalEndTime != null
                && originalStartTime.equals(slotStart)
                && originalEndTime.equals(slotEnd);
    }

    private void loadCourtOperatingHours() {
        // We now rely on openingTime/closingTime passed via Intent (from booking details).
        // If they are missing, we can't safely construct slots, so just warn and return.
        if (openingTime == null || closingTime == null) {
            Toast.makeText(this, "Missing court operating hours.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Only fetch booked times for the selected date; no need to call /court-detail.
        fetchBookedTimes(selectedDate != null ? selectedDate : LocalDate.now());
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
                            Toast.makeText(EditTimeslotActivity.this, "Failed to load booked times", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(EditTimeslotActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
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
            }
        }
        return null;
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
            LocalTime start = parseLocalTime(selectedStartTime);
            LocalTime end = parseLocalTime(selectedEndTime);
            if (start == null || end == null) {
                return courtPrice;
            }
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

    private void attemptUpdate() {
        if (bookingId <= 0) {
            Toast.makeText(this, "Missing booking details", Toast.LENGTH_SHORT).show();
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

        String date = selectedDate != null ? selectedDate.toString() : LocalDate.now().toString();
        EditBookingRequest request = new EditBookingRequest(
                date,
                normalizeTime(selectedStartTime),
                normalizeTime(selectedEndTime)
        );

        RetrofitClient.getInstance().getApiService().editBooking(token, bookingId, request)
                .enqueue(new Callback<ViewBookingResponse>() {
                    @Override
                    public void onResponse(Call<ViewBookingResponse> call, Response<ViewBookingResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(EditTimeslotActivity.this, "Failed to update booking", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(EditTimeslotActivity.this, "Booking updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Call<ViewBookingResponse> call, Throwable t) {
                        Toast.makeText(EditTimeslotActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

    private String normalizeDisplayTime(String time) {
        LocalTime localTime = parseLocalTime(time);
        if (localTime == null) {
            return time;
        }
        return localTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
    }

    private String formatLocalTime(LocalTime time) {
        if (time == null) return "";
        return time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
    }
}
