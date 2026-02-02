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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VendorDashboardFragment extends Fragment {

    private static final String TAG = "VendorDashboardFragment";
    private TextView tvCurrentDate, tvCurrentTime;
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
            
            tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
            tvCurrentTime = view.findViewById(R.id.tvCurrentTime);

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
            
            // Start updating time
            updateDateTime();
            
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
}
