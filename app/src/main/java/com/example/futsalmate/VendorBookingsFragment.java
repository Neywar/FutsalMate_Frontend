package com.example.futsalmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.VendorBookingsData;
import com.example.futsalmate.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class VendorBookingsFragment extends Fragment {

    private TextView tvSelectedDate;
    private TextView tvBookingsCount;
    private TextView tvTodayTotalAmount;
    private TextView tvEmptyBookings;
    private EditText etSearchBookings;
    private LinearLayout bookingsList;
    private LinearLayout filtersContainer;
    private TokenManager tokenManager;
    private final List<Booking> allBookings = new ArrayList<>();
    private String selectedDateFilter = "Today";
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_bookings, container, false);

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvBookingsCount = view.findViewById(R.id.tvBookingsCount);
        tvTodayTotalAmount = view.findViewById(R.id.tvTodayTotalAmount);
        tvEmptyBookings = view.findViewById(R.id.tvEmptyBookings);
        etSearchBookings = view.findViewById(R.id.etSearchBookings);
        bookingsList = view.findViewById(R.id.bookingsList);
        filtersContainer = view.findViewById(R.id.filtersContainer);
        tokenManager = new TokenManager(requireContext());

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

        if (etSearchBookings != null) {
            etSearchBookings.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        loadVendorBookings();

        return view;
    }

    private void loadVendorBookings() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getContext(), "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApiService().vendorCourtBookings(token)
                .enqueue(new Callback<ApiResponse<VendorBookingsData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<VendorBookingsData>> call, Response<ApiResponse<VendorBookingsData>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            Toast.makeText(getContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        VendorBookingsData data = response.body().getData();
                        allBookings.clear();
                        if (data.getBookings() != null) {
                            allBookings.addAll(data.getBookings());
                        }

                        buildDateFilters();
                        applyFilters();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<VendorBookingsData>> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
            applyFilters();
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

    private void buildDateFilters() {
        if (filtersContainer == null) return;
        filtersContainer.removeAllViews();

        addFilterChip("Today");
        addFilterChip("Upcoming");
        addFilterChip("All");
    }

    private void addFilterChip(String label) {
        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setPadding(40, 20, 40, 20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(24);
        chip.setLayoutParams(params);
        chip.setTextSize(12);
        chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
        chip.setOnClickListener(v -> {
            selectedDateFilter = label;
            refreshFilterStyles();
            applyFilters();
        });

        filtersContainer.addView(chip);
        refreshFilterStyles();
    }

    private void refreshFilterStyles() {
        if (filtersContainer == null) return;
        for (int i = 0; i < filtersContainer.getChildCount(); i++) {
            View child = filtersContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                boolean selected = chip.getText().toString().equals(selectedDateFilter);
                if (selected) {
                    chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_vendor_button));
                    chip.setBackgroundTintList(null);
                    chip.setTextColor(Color.parseColor("#000000"));
                } else {
                    chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_vendor_card));
                    chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1F2937")));
                    chip.setTextColor(Color.parseColor("#9CA3AF"));
                }
            }
        }
    }

    private void applyFilters() {
        if (bookingsList == null) return;

        String selectedDate = apiDateFormat.format(calendar.getTime());
        String query = etSearchBookings != null ? etSearchBookings.getText().toString().trim().toLowerCase(Locale.US) : "";

        List<Booking> filtered = new ArrayList<>();
        for (Booking booking : allBookings) {
            if (booking.getDate() == null) {
                continue;
            }

            if (!matchesDateFilter(booking.getDate(), selectedDate)) {
                continue;
            }

            if (!query.isEmpty()) {
                String name = getBookingName(booking).toLowerCase(Locale.US);
                String phone = getBookingPhone(booking).toLowerCase(Locale.US);
                if (!name.contains(query) && !phone.contains(query)) {
                    continue;
                }
            }

            filtered.add(booking);
        }

        renderBookings(filtered);
    }

    private boolean matchesDateFilter(String bookingDate, String todayDate) {
        if ("All".equals(selectedDateFilter)) {
            return true;
        }

        if ("Today".equals(selectedDateFilter)) {
            return bookingDate.equals(todayDate);
        }

        if ("Upcoming".equals(selectedDateFilter)) {
            try {
                java.util.Date booking = apiDateFormat.parse(bookingDate);
                java.util.Date today = apiDateFormat.parse(todayDate);
                if (booking == null || today == null) {
                    return false;
                }
                return booking.after(today);
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private void renderBookings(List<Booking> bookings) {
        bookingsList.removeAllViews();

        if (tvEmptyBookings != null) {
            tvEmptyBookings.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (tvBookingsCount != null) {
            tvBookingsCount.setText(bookings.size() + " Bookings");
        }
        if (tvTodayTotalAmount != null) {
            double total = calculateTotalEarnings(bookings);
            tvTodayTotalAmount.setText(String.format(Locale.US, "Rs.%.2f", total));
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            View row = inflater.inflate(R.layout.item_vendor_booking, bookingsList, false);

            TextView tvTime = row.findViewById(R.id.tvBookingTime);
            TextView tvUser = row.findViewById(R.id.tvBookingUser);
            TextView tvPhone = row.findViewById(R.id.tvBookingPhone);
            TextView tvCourt = row.findViewById(R.id.tvBookingCourt);
            TextView tvStatus = row.findViewById(R.id.tvBookingStatus);
            ImageButton btnBookingActions = row.findViewById(R.id.btnBookingActions);

            tvTime.setText(formatTimeRange(booking.getStartTime(), booking.getEndTime()));
            tvUser.setText(getBookingName(booking));
            tvPhone.setText(getBookingPhone(booking));
            tvCourt.setText(booking.getCourt() != null ? booking.getCourt().getCourtName() : "-");

            String statusText = getStatusText(booking);
            tvStatus.setText(statusText);
            applyStatusStyle(tvStatus, statusText);

            if (btnBookingActions != null) {
                boolean showActions = shouldShowActions(booking);
                btnBookingActions.setVisibility(showActions ? View.VISIBLE : View.INVISIBLE);
                if (showActions) {
                    btnBookingActions.setOnClickListener(v -> showBookingActionMenu(v, booking));
                } else {
                    btnBookingActions.setOnClickListener(null);
                }
            }

            bookingsList.addView(row);

            if (i < bookings.size() - 1) {
                View divider = new View(requireContext());
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                ));
                divider.setBackgroundColor(Color.parseColor("#1F2937"));
                bookingsList.addView(divider);
            }
        }
    }

    private String getBookingName(Booking booking) {
        if (booking.getUser() != null && booking.getUser().getFullName() != null) {
            return booking.getUser().getFullName();
        }
        if (booking.getCustomerName() != null) {
            return booking.getCustomerName();
        }
        return "Unknown";
    }

    private String getBookingPhone(Booking booking) {
        if (booking.getUser() != null && booking.getUser().getPhone() != null) {
            return booking.getUser().getPhone();
        }
        if (booking.getCustomerPhone() != null) {
            return booking.getCustomerPhone();
        }
        return "-";
    }

    private String getStatusText(Booking booking) {
        if (booking.getPaymentStatus() != null && !booking.getPaymentStatus().isEmpty()) {
            return booking.getPaymentStatus().toUpperCase(Locale.US);
        }
        if (booking.getStatus() != null) {
            return booking.getStatus().toUpperCase(Locale.US);
        }
        return "PENDING";
    }

    private boolean shouldShowActions(Booking booking) {
        String paymentStatus = booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "";
        String paymentMethod = booking.getPayment() != null ? booking.getPayment() : "";
        return paymentStatus.equalsIgnoreCase("pending") || paymentMethod.equalsIgnoreCase("cash");
    }

    private void showBookingActionMenu(View anchor, Booking booking) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.vendor_booking_action_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_approve) {
                booking.setPaymentStatus("PAID");
                booking.setStatus("CONFIRMED");
                applyFilters();
                Toast.makeText(getContext(), "Booking approved", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.action_reject) {
                booking.setPaymentStatus("REJECTED");
                booking.setStatus("REJECTED");
                applyFilters();
                Toast.makeText(getContext(), "Booking rejected", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void applyStatusStyle(TextView tvStatus, String status) {
        if (status.contains("PAID")) {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4010B981")));
            tvStatus.setTextColor(Color.parseColor("#10B981"));
        } else if (status.contains("CONFIRMED")) {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4022C55E")));
            tvStatus.setTextColor(Color.parseColor("#22C55E"));
        } else if (status.contains("REJECT")) {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#40EF4444")));
            tvStatus.setTextColor(Color.parseColor("#EF4444"));
        } else {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#40FACC15")));
            tvStatus.setTextColor(Color.parseColor("#FACC15"));
        }
    }

    private double calculateTotalEarnings(List<Booking> bookings) {
        double total = 0.0;
        for (Booking booking : bookings) {
            if (booking.getCourt() != null && booking.getCourt().getPrice() != null) {
                total += parseAmount(booking.getCourt().getPrice());
            }
        }
        return total;
    }

    private double parseAmount(String amount) {
        if (amount == null) {
            return 0.0;
        }
        String cleaned = amount.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String formatTimeRange(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return "-";
        }
        return formatTime(startTime) + " - " + formatTime(endTime);
    }

    private String formatTime(String time24) {
        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("HH:mm:ss", Locale.US);
            java.text.SimpleDateFormat output = new java.text.SimpleDateFormat("hh:mm a", Locale.US);
            return output.format(input.parse(time24));
        } catch (Exception e) {
            return time24;
        }
    }
}
