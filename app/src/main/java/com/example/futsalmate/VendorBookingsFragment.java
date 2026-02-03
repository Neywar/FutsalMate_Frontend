package com.example.futsalmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class VendorBookingsFragment extends Fragment {

    private TextView tvSelectedDate;
    private final Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_bookings, container, false);

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        // Set current date by default
        updateDateLabel();

        // Back button -> Redirects to Dashboard
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof VendorMainActivity) {
                    ((VendorMainActivity) getActivity()).switchToDashboard();
                }
            });
        }

        // Calendar Filter button
        View btnCalendar = view.findViewById(R.id.btnCalendar);
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> showDatePicker());
        }

        // Manual Booking FAB
        View fabManualBooking = view.findViewById(R.id.fabManualBooking);
        if (fabManualBooking != null) {
            fabManualBooking.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), ManualBookingActivity.class));
            });
        }

        // Setup Edit/Delete for existing rows
        setupRowMenu(view.findViewById(R.id.rowBooking1), "Alex Rivera", "+1 234 567 890", "Court A");
        setupRowMenu(view.findViewById(R.id.rowBooking2), "Marcus J.", "+1 987 654 321", "Court B");

        return view;
    }

    private void setupRowMenu(View row, String userName, String phone, String court) {
        if (row == null) return;
        row.setOnClickListener(v -> {
            ContextThemeWrapper wrapper = new ContextThemeWrapper(getActivity(), R.style.DarkPopupMenuTheme);
            PopupMenu popup = new PopupMenu(wrapper, v);
            popup.getMenu().add("Edit Booking");
            popup.getMenu().add("Delete Booking");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Edit Booking")) {
                    Intent intent = new Intent(getActivity(), ManualBookingActivity.class);
                    intent.putExtra("EDIT_MODE", true);
                    intent.putExtra("USER_NAME", userName);
                    intent.putExtra("PHONE", phone);
                    intent.putExtra("COURT", court);
                    startActivity(intent);
                    return true;
                } else if (item.getTitle().equals("Delete Booking")) {
                    row.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Booking deleted", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
            Toast.makeText(getContext(), "Filtering bookings for: " + tvSelectedDate.getText(), Toast.LENGTH_SHORT).show();
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        if (tvSelectedDate != null) {
            tvSelectedDate.setText(sdf.format(calendar.getTime()).toUpperCase());
        }
    }
}
