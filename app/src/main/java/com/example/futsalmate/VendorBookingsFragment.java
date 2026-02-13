package com.example.futsalmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.example.futsalmate.api.models.ViewBookingResponse;
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

    @Override
    public void onResume() {
        super.onResume();
        // Refresh bookings whenever we return to this fragment so that
        // payment_status (and other fields) reflect the latest changes
        // made from VendorBookingDetailsActivity.
        loadVendorBookings();
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
        if (!isAdded() || filtersContainer == null) return;
        filtersContainer.removeAllViews();

        addFilterChip("Today");
        addFilterChip("Upcoming");
        addFilterChip("All");
        addFilterChip("Manual");
    }

    private void addFilterChip(String label) {
        if (!isAdded()) return;
        android.content.Context ctx = getContext();
        if (ctx == null) return;

        TextView chip = new TextView(ctx);
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
        if (!isAdded() || filtersContainer == null) return;
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        for (int i = 0; i < filtersContainer.getChildCount(); i++) {
            View child = filtersContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                boolean selected = chip.getText().toString().equals(selectedDateFilter);
                if (selected) {
                    chip.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_vendor_button));
                    chip.setBackgroundTintList(null);
                    chip.setTextColor(Color.parseColor("#000000"));
                } else {
                    chip.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_vendor_card));
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

            // Manual filter: show only bookings where user_id is null
            if ("Manual".equals(selectedDateFilter)) {
                if (booking.getUser() != null) {
                    continue;
                }
            } else {
                // For other filters, apply date filtering
                if (!matchesDateFilter(booking.getDate(), selectedDate)) {
                    continue;
                }
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

        // Manual filter is handled separately in applyFilters()
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
            View bookingStatusContainer = row.findViewById(R.id.bookingStatusContainer);
            ImageView ivBookingStatus = row.findViewById(R.id.ivBookingStatus);
            TextView tvPaymentStatus = row.findViewById(R.id.tvBookingPaymentStatus);
            ImageButton btnBookingActions = row.findViewById(R.id.btnBookingActions);

            tvTime.setText(formatDateTimeRange(booking));
            tvUser.setText(getBookingName(booking));
            tvPhone.setText(getBookingPhone(booking));
            tvCourt.setText(booking.getCourt() != null ? booking.getCourt().getCourtName() : "-");

            applyBookingStatusIcon(bookingStatusContainer, ivBookingStatus, booking != null ? booking.getStatus() : null);

            if (tvPaymentStatus != null) {
                String paymentStatusText = getPaymentStatusText(booking);
                tvPaymentStatus.setText(paymentStatusText);
                applyStatusStyle(tvPaymentStatus, paymentStatusText);
            }

            if (btnBookingActions != null) {
                boolean showActions = shouldShowActions(booking);
                btnBookingActions.setVisibility(showActions ? View.VISIBLE : View.INVISIBLE);
                if (showActions) {
                    btnBookingActions.setOnClickListener(v -> showBookingActionMenu(v, booking));
                } else {
                    btnBookingActions.setOnClickListener(null);
                }
            }

            row.setOnClickListener(v -> openBookingDetails(booking));

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

    private void openBookingDetails(Booking booking) {
        if (booking == null || getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), VendorBookingDetailsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("booking_date", booking.getDate());
        intent.putExtra("booking_start", booking.getStartTime());
        intent.putExtra("booking_end", booking.getEndTime());
        intent.putExtra("booking_status", booking.getStatus());
        intent.putExtra("booking_payment_status", booking.getPaymentStatus());
        intent.putExtra("booking_payment_method", booking.getPayment());
        intent.putExtra("booking_notes", booking.getNotes());
        intent.putExtra("customer_name", getBookingName(booking));
        intent.putExtra("customer_phone", getBookingPhone(booking));
        if (booking.getCourt() != null) {
            intent.putExtra("court_name", booking.getCourt().getCourtName());
            intent.putExtra("court_location", booking.getCourt().getLocation());
            intent.putExtra("court_price", booking.getCourt().getPrice());
            intent.putExtra("court_image", booking.getCourt().getImage());
        }
        startActivity(intent);
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

    private String getBookingStatusText(Booking booking) {
        if (booking.getStatus() != null && !booking.getStatus().trim().isEmpty()) {
            return booking.getStatus().toUpperCase(Locale.US);
        }
        return "PENDING";
    }

    private void applyBookingStatusIcon(@Nullable View container, @Nullable ImageView icon, @Nullable String rawStatus) {
        if (container == null || icon == null) return;

        String status = rawStatus != null ? rawStatus.trim().toUpperCase(Locale.US) : "PENDING";
        if (status.isEmpty()) status = "PENDING";

        int iconRes = R.drawable.ic_hourglass;
        int bgColor = Color.parseColor("#40FACC15"); // default: pending (yellow, 25% alpha)
        int fgColor = Color.parseColor("#FACC15");

        if (status.contains("CONFIRM")) {
            iconRes = R.drawable.ic_check;
            bgColor = Color.parseColor("#4022C55E");
            fgColor = Color.parseColor("#22C55E");
        } else if (status.contains("PEND")) {
            iconRes = R.drawable.ic_hourglass;
            bgColor = Color.parseColor("#40FACC15");
            fgColor = Color.parseColor("#FACC15");
        } else if (status.contains("REJECT")) {
            iconRes = R.drawable.ic_close;
            bgColor = Color.parseColor("#40EF4444");
            fgColor = Color.parseColor("#EF4444");
        } else if (status.contains("CANCEL")) {
            iconRes = R.drawable.ic_close;
            bgColor = Color.parseColor("#401E293B");
            fgColor = requireContext().getColor(R.color.text_grey);
        }

        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(fgColor));
        container.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        icon.setContentDescription("Booking status: " + status);
    }

    private String getPaymentStatusText(Booking booking) {
        if (booking.getPaymentStatus() != null && !booking.getPaymentStatus().trim().isEmpty()) {
            return booking.getPaymentStatus().toUpperCase(Locale.US);
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
                vendorApproveBooking(booking);
                return true;
            } else if (item.getItemId() == R.id.action_reject) {
                vendorRejectBooking(booking);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void vendorApproveBooking(Booking booking) {
        if (booking == null || booking.getId() <= 0) {
            Toast.makeText(getContext(), "Invalid booking", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getContext(), "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.getInstance().getApiService().vendorApproveBooking(token, booking.getId())
                .enqueue(new Callback<ViewBookingResponse>() {
                    @Override
                    public void onResponse(Call<ViewBookingResponse> call, Response<ViewBookingResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(), response.body() != null ? response.body().getMessage() : "Failed to approve booking", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ViewBookingResponse body = response.body();
                        if ("success".equalsIgnoreCase(body.getStatus()) && body.getBooking() != null) {
                            int idx = indexOfBookingById(body.getBooking().getId());
                            if (idx >= 0) {
                                allBookings.set(idx, body.getBooking());
                            }
                            applyFilters();
                            Toast.makeText(getContext(), "Booking approved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), body.getMessage() != null ? body.getMessage() : "Failed to approve", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ViewBookingResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void vendorRejectBooking(Booking booking) {
        if (booking == null || booking.getId() <= 0) {
            Toast.makeText(getContext(), "Invalid booking", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getContext(), "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.getInstance().getApiService().vendorRejectBooking(token, booking.getId())
                .enqueue(new Callback<ViewBookingResponse>() {
                    @Override
                    public void onResponse(Call<ViewBookingResponse> call, Response<ViewBookingResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(), response.body() != null ? response.body().getMessage() : "Failed to reject booking", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ViewBookingResponse body = response.body();
                        if ("success".equalsIgnoreCase(body.getStatus()) && body.getBooking() != null) {
                            int idx = indexOfBookingById(body.getBooking().getId());
                            if (idx >= 0) {
                                allBookings.set(idx, body.getBooking());
                            }
                            applyFilters();
                            Toast.makeText(getContext(), "Booking rejected", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), body.getMessage() != null ? body.getMessage() : "Failed to reject", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ViewBookingResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int indexOfBookingById(int id) {
        for (int i = 0; i < allBookings.size(); i++) {
            if (allBookings.get(i).getId() == id) return i;
        }
        return -1;
    }

    private void applyStatusStyle(TextView tvStatus, String status) {
        if (status == null) {
            status = "";
        }
        String upper = status.toUpperCase(Locale.US);

        // PAID -> green, UNPAID -> red, PENDING/others -> yellow
        if (upper.contains("PAID") && !upper.contains("UNPAID")) {
            // PAID
            tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4010B981")));
            tvStatus.setTextColor(Color.parseColor("#10B981"));
        } else if (upper.contains("UNPAID")) {
            // UNPAID
            tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#40EF4444")));
            tvStatus.setTextColor(Color.parseColor("#EF4444"));
        } else {
            // PENDING or anything else -> yellow
            tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#40FACC15")));
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

    private String formatDateTimeRange(Booking booking) {
        if (booking == null) {
            return "-";
        }

        String date = booking.getDate();
        String timeRange = formatTimeRange(booking.getStartTime(), booking.getEndTime());

        if (date == null || date.isEmpty()) {
            return timeRange != null ? timeRange : "-";
        }

        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            java.text.SimpleDateFormat output = new java.text.SimpleDateFormat("MMM dd", Locale.US);
            java.util.Date parsed = input.parse(date);
            String formattedDate = parsed != null ? output.format(parsed) : date;
            return formattedDate + " • " + timeRange;
        } catch (Exception e) {
            return date + " • " + timeRange;
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
