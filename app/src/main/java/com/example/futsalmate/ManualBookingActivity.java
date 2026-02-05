package com.example.futsalmate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.CourtsResponse;
import com.example.futsalmate.api.models.ManualBookingRequest;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManualBookingActivity extends AppCompatActivity {

    private static final String TAG = "ManualBooking";
    private EditText etCustomerName, etCustomerPhone, etNotes;
    private AutoCompleteTextView spinnerCourt, spinnerPaymentMethod;
    private TextView tvBookingDate, tvStartTime, tvEndTime;
    private MaterialButton btnConfirmBooking;
    private ProgressBar progressBar;
    private Calendar calendar = Calendar.getInstance();
    private TokenManager tokenManager;
    
    private List<Court> vendorCourts = new ArrayList<>();
    private Map<String, Integer> courtNameToIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_booking);

        tokenManager = new TokenManager(this);

        // Initialize views
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        etNotes = findViewById(R.id.etNotes);
        spinnerCourt = findViewById(R.id.spinnerCourt);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        progressBar = findViewById(R.id.progressBar);
        TextView tvTitle = findViewById(R.id.tvTitle);

        // Setup Payment Method Spinner
        String[] paymentMethods = getResources().getStringArray(R.array.payment_methods_array);
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paymentMethods);
        spinnerPaymentMethod.setAdapter(paymentAdapter);

        // Load vendor courts
        loadVendorCourts();

        // Click Listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvBookingDate.setOnClickListener(v -> showDatePicker());
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        btnConfirmBooking.setOnClickListener(v -> {
            if (validateFields()) {
                submitManualBooking();
            }
        });
    }

    private void loadVendorCourts() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RetrofitClient.getInstance().getApiService().viewVendorCourts("Bearer " + token)
                .enqueue(new Callback<ApiResponse<CourtsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CourtsResponse>> call, Response<ApiResponse<CourtsResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<CourtsResponse> apiResponse = response.body();
                            if ("success".equals(apiResponse.getStatus()) && apiResponse.getData() != null) {
                                vendorCourts = apiResponse.getData().getCourts();
                                setupCourtSpinner();
                            }
                        } else {
                            Log.e(TAG, "Failed to load courts: " + response.code());
                            Toast.makeText(ManualBookingActivity.this, "Failed to load courts", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CourtsResponse>> call, Throwable t) {
                        Log.e(TAG, "Error loading courts: " + t.getMessage());
                        Toast.makeText(ManualBookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupCourtSpinner() {
        List<String> courtNames = new ArrayList<>();
        courtNameToIdMap.clear();
        
        for (Court court : vendorCourts) {
            String courtName = court.getCourtName();
            courtNames.add(courtName);
            courtNameToIdMap.put(courtName, court.getId());
        }
        
        ArrayAdapter<String> courtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courtNames);
        spinnerCourt.setAdapter(courtAdapter);
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            tvBookingDate.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(TextView targetView) {
        NumberPicker hourPicker = new NumberPicker(this);
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setValue(12);

        NumberPicker amPmPicker = new NumberPicker(this);
        amPmPicker.setMinValue(0);
        amPmPicker.setMaxValue(1);
        amPmPicker.setDisplayedValues(new String[] {"AM", "PM"});
        amPmPicker.setValue(0);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        layout.addView(hourPicker);
        layout.addView(amPmPicker);

        new AlertDialog.Builder(this)
                .setTitle("Select Time")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    int hour = hourPicker.getValue();
                    String amPm = amPmPicker.getValue() == 0 ? "AM" : "PM";
                    targetView.setText(String.format(Locale.getDefault(), "%d %s", hour, amPm));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitManualBooking() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnConfirmBooking.setEnabled(false);

        // Get form data
        String customerName = etCustomerName.getText().toString().trim();
        String customerPhone = etCustomerPhone.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String courtName = spinnerCourt.getText().toString();
        String payment = spinnerPaymentMethod.getText().toString();

        // Get court ID
        Integer courtId = courtNameToIdMap.get(courtName);
        if (courtId == null) {
            Toast.makeText(this, "Please select a valid court", Toast.LENGTH_SHORT).show();
            btnConfirmBooking.setEnabled(true);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }

        // Format date as yyyy-MM-dd
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String date = apiDateFormat.format(calendar.getTime());

        String startTime = tvStartTime.getText().toString();
        String endTime = tvEndTime.getText().toString();

        // Create request
        ManualBookingRequest request = new ManualBookingRequest(
                date, startTime, endTime, notes,
                customerName, customerPhone, courtId, payment
        );

        String token = tokenManager.getToken();
        RetrofitClient.getInstance().getApiService().manualBookCourt("Bearer " + token, request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        btnConfirmBooking.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(ManualBookingActivity.this, "Manual Booking Confirmed", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String errorMsg = "Failed to create booking";
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
                            }
                            Toast.makeText(ManualBookingActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        btnConfirmBooking.setEnabled(true);
                        Log.e(TAG, "Error: " + t.getMessage());
                        Toast.makeText(ManualBookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateFields() {
        if (etCustomerName.getText().toString().trim().isEmpty()) {
            etCustomerName.setError("Customer name is required");
            return false;
        }
        if (etCustomerPhone.getText().toString().trim().isEmpty()) {
            etCustomerPhone.setError("Customer phone is required");
            return false;
        }
        if (spinnerCourt.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a court", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerPaymentMethod.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select payment method", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvBookingDate.getText().toString().equals("Select Date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvStartTime.getText().toString().equals("Start Time")) {
            Toast.makeText(this, "Please select start time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvEndTime.getText().toString().equals("End Time")) {
            Toast.makeText(this, "Please select end time", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
