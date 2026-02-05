package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
    private TextView tvTopCustomerName1;
    private TextView tvTopCustomerName2;
    private TextView tvTopCustomerName3;
    private TextView tvTopCustomerName4;

    private View customer1;
    private View customer2;
    private View customer3;
    private View customer4;

    private View cardRecent1;
    private View cardRecent2;
    private TextView tvUserName1;
    private TextView tvUserName2;
    private TextView tvPitch1;
    private TextView tvPitch2;
    private TextView tvDate1;
    private TextView tvDate2;
    private TextView tvStatus1;
    private TextView tvStatus2;

    private final List<VendorCustomer> customers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_users, container, false);

        tokenManager = new TokenManager(requireContext());

        customer1 = view.findViewById(R.id.customer1);
        customer2 = view.findViewById(R.id.customer2);
        customer3 = view.findViewById(R.id.customer3);
        customer4 = view.findViewById(R.id.customer4);

        tvTopCustomerName1 = view.findViewById(R.id.tvTopCustomerName1);
        tvTopCustomerName2 = view.findViewById(R.id.tvTopCustomerName2);
        tvTopCustomerName3 = view.findViewById(R.id.tvTopCustomerName3);
        tvTopCustomerName4 = view.findViewById(R.id.tvTopCustomerName4);

        cardRecent1 = view.findViewById(R.id.cardRecent1);
        cardRecent2 = view.findViewById(R.id.cardRecent2);
        tvUserName1 = view.findViewById(R.id.tvUserName1);
        tvUserName2 = view.findViewById(R.id.tvUserName2);
        tvPitch1 = view.findViewById(R.id.tvPitch1);
        tvPitch2 = view.findViewById(R.id.tvPitch2);
        tvDate1 = view.findViewById(R.id.tvDate1);
        tvDate2 = view.findViewById(R.id.tvDate2);
        tvStatus1 = view.findViewById(R.id.tvStatus1);
        tvStatus2 = view.findViewById(R.id.tvStatus2);

        // Back button -> Redirects to Dashboard
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof VendorMainActivity) {
                    ((VendorMainActivity) getActivity()).switchToDashboard();
                }
            });
        }

        // Click listener for "View History" on Recent Activity Card 1
        view.findViewById(R.id.btnViewHistory1).setOnClickListener(v -> openCustomerHistory(0));

        // Click listener for "View History" on Recent Activity Card 2
        view.findViewById(R.id.btnViewHistory2).setOnClickListener(v -> openCustomerHistory(1));

        // "See all" top customers
        view.findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Showing all customers", Toast.LENGTH_SHORT).show();
        });

        // Filter button
        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Opening filters", Toast.LENGTH_SHORT).show();
        });

        viewVendorCustomers();

        return view;
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

    private void renderTopCustomers() {
        updateTopCustomerSlot(customer1, tvTopCustomerName1, 0, true);
        updateTopCustomerSlot(customer2, tvTopCustomerName2, 1, false);
        updateTopCustomerSlot(customer3, tvTopCustomerName3, 2, false);
        updateTopCustomerSlot(customer4, tvTopCustomerName4, 3, false);
    }

    private void updateTopCustomerSlot(View slot, TextView nameView, int index, boolean highlight) {
        if (slot == null || nameView == null) return;
        slot.setVisibility(View.VISIBLE);
        if (index < customers.size()) {
            VendorCustomer customer = customers.get(index);
            nameView.setText(getCustomerName(customer));
            nameView.setTextColor(getResources().getColor(highlight ? R.color.white : R.color.gray_text));
            nameView.setTypeface(nameView.getTypeface(), highlight ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    private void renderRecentActivity() {
        bindRecentCard(cardRecent1, tvUserName1, tvPitch1, tvDate1, tvStatus1, 0);
        bindRecentCard(cardRecent2, tvUserName2, tvPitch2, tvDate2, tvStatus2, 1);
    }

    private void bindRecentCard(View card, TextView nameView, TextView pitchView, TextView dateView, TextView statusView, int index) {
        if (card == null || nameView == null || pitchView == null || dateView == null || statusView == null) return;
        card.setVisibility(View.VISIBLE);
        if (index < customers.size()) {
            VendorCustomer customer = customers.get(index);
            VendorCustomerStats stats = customer.getStatistics();
            nameView.setText(getCustomerName(customer));

            String totalSpent = stats != null ? stats.getTotalSpent() : null;
            String totalBookings = stats != null ? String.valueOf(stats.getTotalBookings()) : "0";
            pitchView.setText("Total spent: Rs. " + (totalSpent != null ? totalSpent : "0.00") + " • Bookings: " + totalBookings);

            String lastDate = stats != null ? stats.getLastBookingDate() : null;
            dateView.setText(formatDateLabel(lastDate));

            boolean paid = stats != null && (stats.getConfirmedBookings() > 0 || isPositiveAmount(totalSpent));
            applyStatusBadgeStyle(statusView, paid);
        }
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

    private void openCustomerHistory(int index) {
        Intent intent = new Intent(getActivity(), VendorUserHistoryActivity.class);
        if (index < customers.size()) {
            VendorCustomer customer = customers.get(index);
            intent.putExtra("customer_id", customer.getId());
            intent.putExtra("customer_name", getCustomerName(customer));
        }
        startActivity(intent);
    }
}
