package com.example.futsalmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.VendorBookingsData;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorUserHistoryActivity extends AppCompatActivity {

    private static final String PREFS_BLOCKED = "vendor_blocked_users";

    private TextView tvUserName;
    private TextView tvSummary;
    private LinearLayout historyRowsContainer;
    private TextView tvEmptyHistory;
    private MaterialButton btnBlockUser;

    private int customerId = -1;
    @Nullable private String customerPhone;
    private boolean isBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_user_history);

        ImageView btnBack = findViewById(R.id.btnBack);
        de.hdodenhof.circleimageview.CircleImageView ivUserAvatar = findViewById(R.id.ivUserAvatar);
        btnBlockUser = findViewById(R.id.btnContactUser);
        tvUserName = findViewById(R.id.tvUserName);
        historyRowsContainer = findViewById(R.id.historyRowsContainer);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Read customer info from Intent
        Intent src = getIntent();
        String name = null;
        int totalBookings = 0;
        String totalSpent = null;
        if (src != null) {
            name = src.getStringExtra("customer_name");
            customerId = src.getIntExtra("customer_id", -1);
            customerPhone = src.getStringExtra("customer_phone");
            totalBookings = src.getIntExtra("total_bookings", 0);
            totalSpent = src.getStringExtra("total_spent");
        }

        if (tvUserName != null && name != null && !name.trim().isEmpty()) {
            tvUserName.setText(name);
        }

        if (ivUserAvatar != null) {
            String photo = src != null ? src.getStringExtra("customer_profile_photo") : null;
            if (photo != null && !photo.trim().isEmpty()) {
                String url = resolveImageUrl(photo);
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .centerCrop()
                        .into(ivUserAvatar);
            } else {
                ivUserAvatar.setImageResource(R.drawable.ic_person);
            }
        }

        tvSummary = findViewById(R.id.tvSummary);
        if (tvSummary != null) {
            String bookingsLabel = totalBookings > 0 ? totalBookings + " Total Matches" : "No matches yet";
            String spentLabel = (totalSpent != null && !totalSpent.trim().isEmpty())
                    ? " • Spent Rs. " + totalSpent
                    : "";
            tvSummary.setText(bookingsLabel + spentLabel);
        }

        TextView tvMemberSince = findViewById(R.id.tvMemberSince);
        if (tvMemberSince != null) {
            String joinedAt = src != null ? src.getStringExtra("email_verified_at") : null;
            String label = formatMemberSince(joinedAt);
            tvMemberSince.setText(label);
        }

        // Load blocked state and update button UI
        isBlocked = isUserBlocked();
        updateBlockButton();

        if (btnBlockUser != null) {
            btnBlockUser.setOnClickListener(v -> {
                toggleBlockState();
            });
        }

        loadHistory();
    }

    private void loadHistory() {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApiService().vendorCourtBookings(token)
                .enqueue(new Callback<ApiResponse<VendorBookingsData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<VendorBookingsData>> call, Response<ApiResponse<VendorBookingsData>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            Toast.makeText(VendorUserHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                            renderHistory(new ArrayList<>());
                            return;
                        }

                        VendorBookingsData data = response.body().getData();
                        List<Booking> all = data.getBookings();
                        List<Booking> filtered = filterBookingsForCustomer(all);
                        renderHistory(filtered);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<VendorBookingsData>> call, Throwable t) {
                        Toast.makeText(VendorUserHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        renderHistory(new ArrayList<>());
                    }
                });
    }

    private List<Booking> filterBookingsForCustomer(List<Booking> all) {
        List<Booking> out = new ArrayList<>();
        if (all == null) return out;
        for (Booking b : all) {
            if (b == null) continue;
            boolean match = false;
            if (b.getUser() != null && customerId > 0 && b.getUser().getId() == customerId) {
                match = true;
            } else if (customerPhone != null && !customerPhone.trim().isEmpty()
                    && customerPhone.equalsIgnoreCase(b.getCustomerPhone())) {
                match = true;
            }
            if (match) {
                out.add(b);
            }
        }
        return out;
    }

    private void renderHistory(List<Booking> bookings) {
        if (historyRowsContainer == null) return;
        historyRowsContainer.removeAllViews();

        if (tvEmptyHistory != null) {
            tvEmptyHistory.setVisibility(bookings == null || bookings.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (bookings == null || bookings.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Booking booking : bookings) {
            View row = inflater.inflate(R.layout.item_vendor_booking_history_row, historyRowsContainer, false);

            TextView tvDateTime = row.findViewById(R.id.tvRowDateTime);
            TextView tvCourt = row.findViewById(R.id.tvRowCourt);
            TextView tvAmount = row.findViewById(R.id.tvRowAmount);
            TextView tvMethod = row.findViewById(R.id.tvRowMethod);
            TextView tvStatus = row.findViewById(R.id.tvRowStatus);

            String dateLabel = formatDate(booking.getDate());
            String timeLabel = formatTimeRange(booking.getStartTime(), booking.getEndTime());
            tvDateTime.setText(dateLabel + "\n" + timeLabel);

            String courtName = booking.getCourt() != null ? booking.getCourt().getCourtName() : "-";
            tvCourt.setText(courtName);

            String price = booking.getCourt() != null ? booking.getCourt().getPrice() : null;
            tvAmount.setText("Rs." + (price != null ? price : "0"));

            String method = booking.getPayment() != null ? booking.getPayment() : "-";
            tvMethod.setText(method.toUpperCase(Locale.US));

            String status = booking.getPaymentStatus() != null ? booking.getPaymentStatus() : booking.getStatus();
            status = status != null ? status.toUpperCase(Locale.US) : "PENDING";
            tvStatus.setText(status);
            applyStatusStyle(tvStatus, status);

            historyRowsContainer.addView(row);
        }
    }

    private String formatDate(@Nullable String date) {
        if (date == null || date.trim().isEmpty()) return "-";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("MMM dd", Locale.US);
            Date d = in.parse(date.trim());
            return d != null ? out.format(d) : date;
        } catch (Exception e) {
            return date;
        }
    }

    private String formatTimeRange(@Nullable String start, @Nullable String end) {
        String s = formatTime(start);
        String e = formatTime(end);
        if (s.isEmpty() && e.isEmpty()) return "-";
        return s + " - " + e;
    }

    private String formatTime(@Nullable String time) {
        if (time == null || time.trim().isEmpty()) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("HH:mm:ss", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("hh:mm a", Locale.US);
            Date d = in.parse(time.trim());
            return d != null ? out.format(d) : time;
        } catch (Exception e) {
            return time;
        }
    }

    private void applyStatusStyle(TextView tvStatus, String status) {
        if (status.contains("PAID") || status.contains("CONFIRMED")) {
            tvStatus.setBackgroundResource(R.drawable.bg_vendor_button);
            tvStatus.setTextColor(getResources().getColor(R.color.black));
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_vendor_card);
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#000000")));
            tvStatus.setTextColor(android.graphics.Color.parseColor("#FACC15"));
        }
    }

    private String resolveImageUrl(String image) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }
        String cleaned = image.trim();
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned;
        }
        return "https://futsalmateapp.sameem.in.net/" + cleaned.replaceFirst("^/+", "");
    }

    private String formatMemberSince(@Nullable String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "User since -";
        }
        String[] patterns = new String[] {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.US);
                Date parsed = in.parse(raw.trim());
                if (parsed != null) {
                    SimpleDateFormat out = new SimpleDateFormat("MMMM yyyy", Locale.US);
                    return "User since " + out.format(parsed);
                }
            } catch (Exception ignored) {
            }
        }
        return "User since " + raw;
    }

    private boolean isUserBlocked() {
        SharedPreferences prefs = getSharedPreferences(PREFS_BLOCKED, MODE_PRIVATE);
        if (customerId > 0) {
            return prefs.getBoolean("user_id_" + customerId, false);
        }
        if (customerPhone != null && !customerPhone.trim().isEmpty()) {
            return prefs.getBoolean("phone_" + customerPhone, false);
        }
        return false;
    }

    private void setUserBlocked(boolean blocked) {
        SharedPreferences prefs = getSharedPreferences(PREFS_BLOCKED, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (customerId > 0) {
            editor.putBoolean("user_id_" + customerId, blocked);
        }
        if (customerPhone != null && !customerPhone.trim().isEmpty()) {
            editor.putBoolean("phone_" + customerPhone, blocked);
        }
        editor.apply();
    }

    private void updateBlockButton() {
        if (btnBlockUser == null) return;
        if (isBlocked) {
            btnBlockUser.setText("Unblock User");
            btnBlockUser.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#374151")));
            btnBlockUser.setTextColor(getResources().getColor(R.color.white));
        } else {
            btnBlockUser.setText("Block User");
            btnBlockUser.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FACC15")));
            btnBlockUser.setTextColor(android.graphics.Color.parseColor("#000000"));
        }
    }

    private void toggleBlockState() {
        isBlocked = !isBlocked;
        setUserBlocked(isBlocked);
        updateBlockButton();
        Toast.makeText(this, isBlocked ? "User blocked for future bookings" : "User unblocked", Toast.LENGTH_SHORT).show();
    }
}
