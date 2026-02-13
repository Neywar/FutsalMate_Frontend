package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.VendorCustomer;
import com.example.futsalmate.api.models.VendorCustomerStats;
import com.example.futsalmate.api.models.VendorCustomersResponse;
import com.example.futsalmate.api.models.VendorBookingsData;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorUsersFragment extends Fragment {

    private TokenManager tokenManager;
    private LinearLayout topCustomersContainer;
    private LinearLayout recentActivityContainer;
    private TextView tvEmptyTopCustomers;
    private TextView tvEmptyRecentActivity;
    private EditText etSearchCustomers;

    private final List<VendorCustomer> customers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_users, container, false);

        tokenManager = new TokenManager(requireContext());

        topCustomersContainer = view.findViewById(R.id.topCustomersContainer);
        recentActivityContainer = view.findViewById(R.id.recentActivityContainer);
        tvEmptyTopCustomers = view.findViewById(R.id.tvEmptyTopCustomers);
        tvEmptyRecentActivity = view.findViewById(R.id.tvEmptyRecentActivity);
        etSearchCustomers = view.findViewById(R.id.etSearchCustomers);

        // Back button -> Redirects to Dashboard
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof VendorMainActivity) {
                    ((VendorMainActivity) getActivity()).switchToDashboard();
                }
            });
        }

        // Search filter
        if (etSearchCustomers != null) {
            etSearchCustomers.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    renderTopCustomers();
                    renderRecentActivity();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewVendorCustomers();
    }

    private void viewVendorCustomers() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getContext(), "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApiService().viewVendorCustomers(token)
                .enqueue(new Callback<ApiResponse<VendorCustomersResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<VendorCustomersResponse>> call, Response<ApiResponse<VendorCustomersResponse>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            Toast.makeText(getContext(), "Failed to load customers", Toast.LENGTH_SHORT).show();
                            loadCustomersFromBookings();
                            return;
                        }

                        VendorCustomersResponse data = response.body().getData();
                        customers.clear();
                        if (data.getCustomers() != null) {
                            customers.addAll(data.getCustomers());
                        }

                        renderTopCustomers();
                        renderRecentActivity();

                        if (customers.isEmpty()) {
                            loadCustomersFromBookings();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<VendorCustomersResponse>> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        loadCustomersFromBookings();
                    }
                });
    }

    private void loadCustomersFromBookings() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            return;
        }

        RetrofitClient.getInstance().getApiService().vendorCourtBookings(token)
                .enqueue(new Callback<ApiResponse<VendorBookingsData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<VendorBookingsData>> call, Response<ApiResponse<VendorBookingsData>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            return;
                        }

                        VendorBookingsData data = response.body().getData();
                        List<Booking> bookings = data.getBookings();
                        if (bookings == null || bookings.isEmpty()) {
                            return;
                        }

                        Map<String, VendorCustomer> uniqueCustomers = new HashMap<>();
                        Map<String, VendorCustomerStats> statsMap = new HashMap<>();

                        for (Booking booking : bookings) {
                            User user = booking.getUser();
                            String key = getCustomerKey(booking);
                            if (key == null) {
                                continue;
                            }

                            VendorCustomer customer = uniqueCustomers.get(key);
                            if (customer == null) {
                                customer = new VendorCustomer();
                                if (user != null) {
                                    customer.setId(user.getId());
                                    customer.setFullName(user.getFullName());
                                    customer.setEmail(user.getEmail());
                                    customer.setPhone(user.getPhone());
                                    customer.setProfilePhoto(user.getProfilePhotoUrl());
                                    customer.setEmailVerifiedAt(user.getEmailVerifiedAt());
                                } else {
                                    customer.setFullName(booking.getCustomerName());
                                    customer.setPhone(booking.getCustomerPhone());
                                }
                                uniqueCustomers.put(key, customer);
                            }

                            VendorCustomerStats stats = statsMap.get(key);
                            if (stats == null) {
                                stats = new VendorCustomerStats();
                                statsMap.put(key, stats);
                            }

                            stats.setTotalBookings(stats.getTotalBookings() + 1);
                            if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("confirmed")) {
                                stats.setConfirmedBookings(stats.getConfirmedBookings() + 1);
                            }

                            if (booking.getPaymentStatus() != null && booking.getPaymentStatus().equalsIgnoreCase("paid")) {
                                String price = booking.getCourt() != null ? booking.getCourt().getPrice() : null;
                                double total = parseAmount(stats.getTotalSpent());
                                total += parseAmount(price);
                                stats.setTotalSpent(String.format(Locale.US, "%.2f", total));
                            }

                            if (booking.getDate() != null) {
                                String last = stats.getLastBookingDate();
                                if (last == null || booking.getDate().compareTo(last) > 0) {
                                    stats.setLastBookingDate(booking.getDate());
                                }
                            }
                        }

                        customers.clear();
                        for (Map.Entry<String, VendorCustomer> entry : uniqueCustomers.entrySet()) {
                            VendorCustomer customer = entry.getValue();
                            VendorCustomerStats stats = statsMap.get(entry.getKey());
                            customer.setStatistics(stats);
                            customers.add(customer);
                        }

                        renderTopCustomers();
                        renderRecentActivity();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<VendorBookingsData>> call, Throwable t) {
                    }
                });
    }

    private String getCustomerKey(Booking booking) {
        if (booking.getUser() != null) {
            return "user:" + booking.getUser().getId();
        }
        if (booking.getCustomerPhone() != null && !booking.getCustomerPhone().isEmpty()) {
            return "phone:" + booking.getCustomerPhone();
        }
        if (booking.getCustomerName() != null && !booking.getCustomerName().isEmpty()) {
            return "name:" + booking.getCustomerName();
        }
        return null;
    }

    private List<VendorCustomer> getFilteredCustomers() {
        String query = etSearchCustomers != null ? etSearchCustomers.getText().toString().trim().toLowerCase(Locale.US) : "";
        if (query.isEmpty()) return new ArrayList<>(customers);
        List<VendorCustomer> out = new ArrayList<>();
        for (VendorCustomer c : customers) {
            if (getCustomerName(c).toLowerCase(Locale.US).contains(query)) out.add(c);
        }
        return out;
    }

    private void renderTopCustomers() {
        if (topCustomersContainer == null) return;
        topCustomersContainer.removeAllViews();
        List<VendorCustomer> filtered = getFilteredCustomers();

        if (tvEmptyTopCustomers != null) {
            tvEmptyTopCustomers.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < filtered.size(); i++) {
            VendorCustomer customer = filtered.get(i);
            View item = inflater.inflate(R.layout.item_vendor_top_customer, topCustomersContainer, false);
            TextView nameView = item.findViewById(R.id.tvTopCustomerName);
            de.hdodenhof.circleimageview.CircleImageView avatar = item.findViewById(R.id.ivTopCustomer);
            nameView.setText(getCustomerName(customer));
            boolean highlight = (i == 0);
            nameView.setTextColor(getResources().getColor(highlight ? R.color.white : R.color.gray_text));
            nameView.setTypeface(nameView.getTypeface(), highlight ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            if (avatar != null) {
                String photo = getCustomerProfilePhoto(customer);
                if (photo != null && !photo.trim().isEmpty()) {
                    String url = resolveImageUrl(photo);
                    com.bumptech.glide.Glide.with(avatar.getContext())
                            .load(url)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .centerCrop()
                            .into(avatar);
                } else {
                    avatar.setImageResource(R.drawable.ic_person);
                }
                if (highlight) {
                    avatar.setBorderColor(android.graphics.Color.parseColor("#FACC15"));
                    avatar.setBorderWidth(2);
                }
            }
            final int index = customers.indexOf(customer);
            item.setOnClickListener(v -> openCustomerHistory(index));
            topCustomersContainer.addView(item);
        }
    }

    private void renderRecentActivity() {
        if (recentActivityContainer == null) return;
        recentActivityContainer.removeAllViews();
        List<VendorCustomer> filtered = getFilteredCustomers();

        if (tvEmptyRecentActivity != null) {
            tvEmptyRecentActivity.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < filtered.size(); i++) {
            VendorCustomer customer = filtered.get(i);
            final int index = customers.indexOf(customer);
            View card = inflater.inflate(R.layout.item_vendor_recent_activity, recentActivityContainer, false);

            de.hdodenhof.circleimageview.CircleImageView ivUser = card.findViewById(R.id.ivUser);
            TextView tvUserName = card.findViewById(R.id.tvUserName);
            TextView tvPitch = card.findViewById(R.id.tvPitch);
            TextView tvDate = card.findViewById(R.id.tvDate);
            TextView tvStatus = card.findViewById(R.id.tvStatus);

            VendorCustomerStats stats = customer.getStatistics();
            tvUserName.setText(getCustomerName(customer));

            if (ivUser != null) {
                String photo = getCustomerProfilePhoto(customer);
                if (photo != null && !photo.trim().isEmpty()) {
                    String url = resolveImageUrl(photo);
                    com.bumptech.glide.Glide.with(ivUser.getContext())
                            .load(url)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .centerCrop()
                            .into(ivUser);
                } else {
                    ivUser.setImageResource(R.drawable.ic_person);
                }
            }

            String totalSpent = stats != null ? stats.getTotalSpent() : null;
            String totalBookings = stats != null ? String.valueOf(stats.getTotalBookings()) : "0";
            tvPitch.setText("Total spent: Rs. " + (totalSpent != null ? totalSpent : "0.00") + " • Bookings: " + totalBookings);

            String lastDate = stats != null ? stats.getLastBookingDate() : null;
            tvDate.setText(formatDateLabel(lastDate));

            boolean paid = stats != null && (stats.getConfirmedBookings() > 0 || isPositiveAmount(totalSpent));
            applyStatusBadgeStyle(tvStatus, paid);

            // Whole card opens user details; "View History" opens history screen.
            card.setOnClickListener(v -> openCustomerDetails(index));
            card.findViewById(R.id.btnViewHistory).setOnClickListener(v -> openCustomerHistory(index));
            recentActivityContainer.addView(card);
        }
    }

    private void openCustomerDetails(int index) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), VendorUserDetailsActivity.class);
        if (index >= 0 && index < customers.size()) {
            VendorCustomer customer = customers.get(index);
            VendorCustomerStats stats = customer.getStatistics();
            intent.putExtra("customer_id", customer.getId());
            intent.putExtra("customer_name", getCustomerName(customer));
            intent.putExtra("customer_phone", customer.getPhone());
            intent.putExtra("customer_email", customer.getEmail());
            intent.putExtra("customer_profile_photo", getCustomerProfilePhoto(customer));
            boolean emailVerified = customer.getEmailVerifiedAt() != null && !customer.getEmailVerifiedAt().trim().isEmpty();
            intent.putExtra("is_email_verified", emailVerified);
            if (customer.getEmailVerifiedAt() != null) {
                intent.putExtra("email_verified_at", customer.getEmailVerifiedAt());
            }
            if (stats != null) {
                intent.putExtra("total_bookings", stats.getTotalBookings());
                intent.putExtra("confirmed_bookings", stats.getConfirmedBookings());
                intent.putExtra("total_spent", stats.getTotalSpent());
                intent.putExtra("last_booking_date", stats.getLastBookingDate());
            }
        }
        startActivity(intent);
    }

    private String getCustomerName(VendorCustomer customer) {
        if (customer.getFullName() != null && !customer.getFullName().isEmpty()) {
            return customer.getFullName();
        }
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            return customer.getEmail();
        }
        if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
            return customer.getPhone();
        }
        return "Unknown";
    }

    private String formatDateLabel(String date) {
        if (date == null || date.isEmpty()) {
            return "No bookings yet";
        }
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date parsed = input.parse(date);
            return parsed != null ? output.format(parsed) : date;
        } catch (Exception e) {
            return date;
        }
    }

    private boolean isPositiveAmount(String amount) {
        if (amount == null) return false;
        String cleaned = amount.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) return false;
        try {
            return Double.parseDouble(cleaned) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
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

    private void applyStatusBadgeStyle(TextView statusView, boolean paid) {
        if (statusView == null) return;
        if (paid) {
            statusView.setBackgroundResource(R.drawable.bg_vendor_button);
            statusView.setTextColor(getResources().getColor(R.color.black));
            statusView.setText("PAID");
        } else {
            statusView.setBackgroundResource(R.drawable.bg_vendor_card);
            statusView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#000000")));
            statusView.setTextColor(android.graphics.Color.parseColor("#FACC15"));
            statusView.setText("PENDING");
        }
    }

    private String getCustomerProfilePhoto(VendorCustomer customer) {
        if (customer == null) {
            return null;
        }
        String photo = customer.getProfilePhoto();
        return (photo != null && !photo.trim().isEmpty()) ? photo : null;
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

    private void openCustomerHistory(int index) {
        Intent intent = new Intent(getActivity(), VendorUserHistoryActivity.class);
        if (index >= 0 && index < customers.size()) {
            VendorCustomer customer = customers.get(index);
            VendorCustomerStats stats = customer.getStatistics();
            intent.putExtra("customer_id", customer.getId());
            intent.putExtra("customer_name", getCustomerName(customer));
            intent.putExtra("customer_phone", customer.getPhone());
            intent.putExtra("customer_email", customer.getEmail());
            intent.putExtra("customer_profile_photo", getCustomerProfilePhoto(customer));
            intent.putExtra("is_email_verified",
                    customer.getEmailVerifiedAt() != null && !customer.getEmailVerifiedAt().trim().isEmpty());
            if (customer.getEmailVerifiedAt() != null) {
                intent.putExtra("email_verified_at", customer.getEmailVerifiedAt());
            }
            if (stats != null) {
                intent.putExtra("total_bookings", stats.getTotalBookings());
                intent.putExtra("total_spent", stats.getTotalSpent());
            }
        }
        startActivity(intent);
    }
}
