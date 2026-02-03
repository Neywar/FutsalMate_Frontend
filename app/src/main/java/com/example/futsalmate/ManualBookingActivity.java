package com.example.futsalmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ManualBookingActivity extends AppCompatActivity {

    private EditText etCustomerName, etCustomerPhone;
    private AutoCompleteTextView spinnerCourt;
    private TextView tvBookingDate, tvStartTime, tvEndTime;
    private MaterialButton btnConfirmBooking;
    private Calendar calendar = Calendar.getInstance();
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_booking);

        // Initialize views
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        spinnerCourt = findViewById(R.id.spinnerCourt);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        TextView tvTitle = findViewById(R.id.tvTitle);

        // Setup Court Spinner
        String[] courts = {"Court A", "Court B", "Court C"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courts);
        spinnerCourt.setAdapter(adapter);

        // Check if editing
        if (getIntent().getBooleanExtra("EDIT_MODE", false)) {
            isEditMode = true;
            tvTitle.setText("Edit Booking");
            btnConfirmBooking.setText("UPDATE BOOKING");
            
            // Populate with dummy data
            etCustomerName.setText(getIntent().getStringExtra("USER_NAME"));
            etCustomerPhone.setText(getIntent().getStringExtra("PHONE"));
            spinnerCourt.setText(getIntent().getStringExtra("COURT"), false);
            tvBookingDate.setText("Oct 24, 2023");
            tvStartTime.setText("09:00");
            tvEndTime.setText("10:00");
        }

        // Click Listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvBookingDate.setOnClickListener(v -> showDatePicker());
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        btnConfirmBooking.setOnClickListener(v -> {
            if (validateFields()) {
                String message = isEditMode ? "Booking Updated Successfully" : "Manual Booking Confirmed";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            targetView.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, 12, 0, true).show();
    }

    private boolean validateFields() {
        if (etCustomerName.getText().toString().trim().isEmpty()) {
            etCustomerName.setError("Required");
            return false;
        }
        if (tvBookingDate.getText().toString().equals("Select Date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
