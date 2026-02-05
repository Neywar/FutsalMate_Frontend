package com.example.futsalmate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.CourtsResponse;
import com.example.futsalmate.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorDashboardFragment extends Fragment {

    private static final String TAG = "VendorDashboardFragment";
    private TextView tvCurrentDate, tvCurrentTime;
    private TextView tvVendorName;
    private TextView tvCourtName;
    private TextView tvStartsIn;
    private TextView tvBookedByName;
    private TextView tvTimeSlotValue;
    private TextView tvTodayBookingsValue;
    private TextView tvActiveUsersValue;
    private TokenManager tokenManager;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            updateDateTime();
            timeHandler.postDelayed(this, 1000); // Update every second
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View view = null;
        try {
            view = inflater.inflate(R.layout.fragment_vendor_dashboard, container, false);
            
            tokenManager = new TokenManager(requireContext());

            tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
            tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
            tvVendorName = view.findViewById(R.id.tvVendorName);
            tvCourtName = view.findViewById(R.id.tvCourtName);
            tvStartsIn = view.findViewById(R.id.tvStartsIn);
            tvBookedByName = view.findViewById(R.id.tvBookedByName);
            tvTimeSlotValue = view.findViewById(R.id.tvTimeSlotValue);
            tvTodayBookingsValue = view.findViewById(R.id.tvTodayBookingsValue);
            tvActiveUsersValue = view.findViewById(R.id.tvActiveUsersValue);

            // Next Scheduled Game - View All
            View tvViewAll = view.findViewById(R.id.tvViewAll);
            if (tvViewAll != null) {
                tvViewAll.setOnClickListener(v -> {
                    if (getActivity() instanceof VendorMainActivity) {
                        ((VendorMainActivity) getActivity()).switchToBookings();
                    }
                });
            }

            // Check In Button -> Switches to Bookings Fragment
            View btnCheckIn = view.findViewById(R.id.btnCheckIn);
            if (btnCheckIn != null) {
                btnCheckIn.setOnClickListener(v -> {
                    if (getActivity() instanceof VendorMainActivity) {
                        ((VendorMainActivity) getActivity()).switchToBookings();
                    }
                });
            }

            // Notification Button
            View btnNotification = view.findViewById(R.id.btnNotification);
            if (btnNotification != null) {
                btnNotification.setOnClickListener(v -> {
                    Toast.makeText(getActivity(), "No new notifications", Toast.LENGTH_SHORT).show();
                });
            }
            
            String cachedName = tokenManager.getVendorName();
            if (cachedName != null && tvVendorName != null) {
                tvVendorName.setText(cachedName);
            }

            // Start updating time
            updateDateTime();
            loadCourtsSummary();
            
        } catch (Exception e) {
            Log.e(TAG, "Error inflating or setting up fragment", e);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        timeHandler.post(timeRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timeRunnable);
    }

    private void updateDateTime() {
        if (tvCurrentDate != null && tvCurrentTime != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            
            Date now = new Date();
            tvCurrentDate.setText(dateFormat.format(now));
            tvCurrentTime.setText(timeFormat.format(now));
        }
    }

    private void loadCourtsSummary() {
        String token = tokenManager.getAuthHeader();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Missing auth token for dashboard summary");
            return;
        }

        RetrofitClient.getInstance().getApiService().viewVendorCourts(token)
                .enqueue(new Callback<ApiResponse<CourtsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CourtsResponse>> call, Response<ApiResponse<CourtsResponse>> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                            CourtsResponse courtsResponse = response.body().getData();
                            List<Court> courts = courtsResponse != null ? courtsResponse.getCourts() : null;

                            int totalCourts = courts != null ? courts.size() : 0;
                            int activeCourts = 0;
                            if (courts != null) {
                                for (Court court : courts) {
                                    if (court != null && "active".equalsIgnoreCase(court.getStatus())) {
                                        activeCourts++;
                                    }
                                }
                            }

                            if (tvTodayBookingsValue != null) {
                                tvTodayBookingsValue.setText(String.valueOf(totalCourts));
                            }
                            if (tvActiveUsersValue != null) {
                                tvActiveUsersValue.setText(String.valueOf(activeCourts));
                            }

                            if (tvCourtName != null) {
                                tvCourtName.setText(totalCourts > 0 && courts != null && courts.get(0) != null && courts.get(0).getCourtName() != null
                                        ? courts.get(0).getCourtName()
                                        : "No courts yet");
                            }
                            if (tvStartsIn != null) {
                                tvStartsIn.setText(totalCourts > 0 ? "No upcoming bookings" : "No courts yet");
                            }
                            if (tvBookedByName != null) {
                                tvBookedByName.setText("-");
                            }
                            if (tvTimeSlotValue != null) {
                                tvTimeSlotValue.setText("-");
                            }
                        } else {
                            Log.w(TAG, "Failed to load dashboard courts: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CourtsResponse>> call, Throwable t) {
                        Log.e(TAG, "Dashboard courts request failed", t);
                    }
                });
    }
}
