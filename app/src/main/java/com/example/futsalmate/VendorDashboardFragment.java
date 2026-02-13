package com.example.futsalmate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.CourtsResponse;
import com.example.futsalmate.api.models.VendorBookingsData;
import com.example.futsalmate.api.models.VendorDashboardResponse;
import com.example.futsalmate.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private View cardNextScheduledGame;
    private TextView tvCourtName;
    private TextView tvStartsIn;
    private TextView tvBookedByName;
    private TextView tvTimeSlotValue;
    private LinearLayout courtStatusContainer;
    private TextView tvTodayBookingsValue;
    private TextView tvActiveUsersValue;
    private TextView tvSlotsLeft;
    private TextView tvEarningsValue;
    private TextView tvHoursValue;
    private TextView tvUtilLabel;
    private TextView tvUtilValue;
    private TextView tvUtilValueDelta;
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
            cardNextScheduledGame = view.findViewById(R.id.cardNextScheduledGame);
            tvCourtName = view.findViewById(R.id.tvCourtName);
            tvStartsIn = view.findViewById(R.id.tvStartsIn);
            tvBookedByName = view.findViewById(R.id.tvBookedByName);
            tvTimeSlotValue = view.findViewById(R.id.tvTimeSlotValue);
            courtStatusContainer = view.findViewById(R.id.courtStatusContainer);
            tvTodayBookingsValue = view.findViewById(R.id.tvTodayBookingsValue);
            tvActiveUsersValue = view.findViewById(R.id.tvActiveUsersValue);
            tvSlotsLeft = view.findViewById(R.id.tvSlotsLeft);
            tvEarningsValue = view.findViewById(R.id.tvEarningsValue);
            tvHoursValue = view.findViewById(R.id.tvHoursValue);

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

            // Notification Button -> open Notifications Activity
            View btnNotification = view.findViewById(R.id.btnNotification);
            if (btnNotification != null) {
                btnNotification.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(getActivity(), VendorNotificationsActivity.class);
                    startActivity(intent);
                });
            }
            
            String cachedName = tokenManager.getVendorName();
            if (cachedName != null && tvVendorName != null) {
                tvVendorName.setText(cachedName);
            }

            // Start updating time
            updateDateTime();
            loadVendorName();
            loadDashboardData();
            
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

    private void loadVendorName() {
        String token = tokenManager.getAuthHeader();
        if (token == null || token.isEmpty()) return;
        String cached = tokenManager.getVendorName();
        if (cached != null && tvVendorName != null) {
            tvVendorName.setText(cached);
        }
        RetrofitClient.getInstance().getApiService().vendorDashboard(token)
                .enqueue(new Callback<ApiResponse<VendorDashboardResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<VendorDashboardResponse>> call, Response<ApiResponse<VendorDashboardResponse>> response) {
                        if (!isAdded() || tvVendorName == null) return;
                        if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                            VendorDashboardResponse data = response.body().getData();
                            if (data != null && data.getVendor() != null && data.getVendor().getName() != null) {
                                tvVendorName.setText(data.getVendor().getName());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<VendorDashboardResponse>> call, Throwable t) {
                        Log.w(TAG, "Vendor dashboard name failed", t);
                    }
                });
    }

    private void loadDashboardData() {
        String token = tokenManager.getAuthHeader();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Missing auth token for dashboard");
            return;
        }

        RetrofitClient.getInstance().getApiService().viewVendorCourts(token)
                .enqueue(new Callback<ApiResponse<CourtsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CourtsResponse>> call, Response<ApiResponse<CourtsResponse>> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null || !"success".equalsIgnoreCase(response.body().getStatus())) {
                            bindEmptyDashboard(null, null);
                            return;
                        }
                        List<Court> courts = response.body().getData() != null ? response.body().getData().getCourts() : null;
                        loadBookingsAndBind(token, courts);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CourtsResponse>> call, Throwable t) {
                        Log.e(TAG, "Dashboard courts failed", t);
                        if (isAdded()) bindEmptyDashboard(null, null);
                    }
                });
    }

    private void loadBookingsAndBind(String token, List<Court> courts) {
        RetrofitClient.getInstance().getApiService().vendorCourtBookings(token)
                .enqueue(new Callback<ApiResponse<VendorBookingsData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<VendorBookingsData>> call, Response<ApiResponse<VendorBookingsData>> response) {
                        if (!isAdded()) return;
                        List<Booking> bookings = null;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            bookings = response.body().getData().getBookings();
                        }
                        bindDashboardData(courts, bookings != null ? bookings : new ArrayList<>());
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<VendorBookingsData>> call, Throwable t) {
                        Log.e(TAG, "Dashboard bookings failed", t);
                        if (isAdded()) bindDashboardData(courts, new ArrayList<>());
                    }
                });
    }

    private void bindEmptyDashboard(List<Court> courts, List<Booking> bookings) {
        bindDashboardData(courts != null ? courts : new ArrayList<>(), bookings != null ? bookings : new ArrayList<>());
    }

    private void bindDashboardData(List<Court> courts, List<Booking> bookings) {
        SimpleDateFormat apiDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat apiTime = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String todayStr = apiDate.format(new Date());
        long nowMs = System.currentTimeMillis();

        // Next upcoming booking (today or future, Confirmed or Pending)
        Booking nextBooking = null;
        for (Booking b : bookings) {
            if (b == null || b.getDate() == null || b.getStartTime() == null || b.getEndTime() == null) continue;
            if (!"Confirmed".equalsIgnoreCase(b.getStatus()) && !"Pending".equalsIgnoreCase(b.getStatus())) continue;
            try {
                java.util.Date bookDate = apiDate.parse(b.getDate());
                java.util.Date startDt = apiTime.parse(b.getStartTime());
                if (bookDate == null || startDt == null) continue;
                Calendar cal = Calendar.getInstance();
                cal.setTime(bookDate);
                int year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH), day = cal.get(Calendar.DAY_OF_MONTH);
                cal.setTime(startDt);
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                if (cal.getTimeInMillis() >= nowMs) {
                    if (nextBooking == null || cal.getTimeInMillis() < getBookingStartMs(apiDate, apiTime, nextBooking)) {
                        nextBooking = b;
                    }
                }
            } catch (Exception ignore) {}
        }

        if (nextBooking != null && cardNextScheduledGame != null && tvCourtName != null && tvStartsIn != null && tvBookedByName != null && tvTimeSlotValue != null) {
            cardNextScheduledGame.setVisibility(View.VISIBLE);
            tvCourtName.setText(nextBooking.getCourt() != null ? nextBooking.getCourt().getCourtName() : "Court");
            tvBookedByName.setText(bookingDisplayName(nextBooking));
            tvTimeSlotValue.setText(formatTimeRange(nextBooking.getStartTime(), nextBooking.getEndTime()));
            tvStartsIn.setText(formatStartsIn(nextBooking.getDate(), nextBooking.getStartTime(), apiDate, apiTime));
        } else if (cardNextScheduledGame != null) {
            cardNextScheduledGame.setVisibility(View.GONE);
        }

        // Today's count
        int todayCount = 0;
        for (Booking b : bookings) {
            if (b != null && todayStr.equals(b.getDate())) todayCount++;
        }
        if (tvTodayBookingsValue != null) tvTodayBookingsValue.setText(String.valueOf(todayCount));

        // Active courts count as "Active users" placeholder
        int activeCourts = 0;
        if (courts != null) {
            for (Court c : courts) {
                if (c != null && "active".equalsIgnoreCase(c.getStatus())) activeCourts++;
            }
        }
        if (tvActiveUsersValue != null) tvActiveUsersValue.setText(String.valueOf(activeCourts));

        // Slots left: simple placeholder (e.g. 8 if we had 8 slots per day - or leave 0)
        if (tvSlotsLeft != null) tvSlotsLeft.setText("● — slots left");

        // Earnings: sum of today's confirmed/paid bookings
        double earningsToday = 0;
        for (Booking b : bookings) {
            if (b == null || !todayStr.equals(b.getDate())) continue;
            if (b.getCourt() != null && b.getCourt().getPrice() != null) {
                try {
                    String p = b.getCourt().getPrice().replaceAll("[^0-9.]", "");
                    if (!p.isEmpty()) earningsToday += Double.parseDouble(p);
                } catch (NumberFormatException ignore) {}
            }
        }
        if (tvEarningsValue != null) tvEarningsValue.setText(String.format(Locale.US, "Rs. %.2f", earningsToday));

        // Hours booked: total hours from all confirmed bookings (today or week - use today for simplicity)
        double hoursToday = 0;
        for (Booking b : bookings) {
            if (b == null || !todayStr.equals(b.getDate())) continue;
            if (b.getStartTime() != null && b.getEndTime() != null) {
                try {
                    Date s = apiTime.parse(b.getStartTime());
                    Date e = apiTime.parse(b.getEndTime());
                    if (s != null && e != null) hoursToday += (e.getTime() - s.getTime()) / (1000.0 * 60 * 60);
                } catch (Exception ignore) {}
            }
        }
        if (tvHoursValue != null) tvHoursValue.setText(String.format(Locale.US, "%.1f hrs", hoursToday));

        // Court usage analytics (weekly utilization)
        bindUsageAnalytics(courts, bookings, apiDate, apiTime);

        // Live court status cards
        if (courtStatusContainer != null) {
            courtStatusContainer.removeAllViews();
            if (courts != null && !courts.isEmpty()) {
                for (Court court : courts) {
                    if (court == null) continue;
                    Booking occupying = findCurrentBookingForCourt(court.getId(), bookings, todayStr, nowMs, apiDate, apiTime);
                    View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_vendor_court_status, courtStatusContainer, false);
                    TextView tvCourtLabel = card.findViewById(R.id.tvCourtLabel);
                    TextView tvCourtStatusBadge = card.findViewById(R.id.tvCourtStatusBadge);
                    TextView lblStatusRow = card.findViewById(R.id.lblStatusRow);
                    TextView tvStatusValue = card.findViewById(R.id.tvStatusValue);
                    ImageView iconStatusExtra = card.findViewById(R.id.iconStatusExtra);
                    TextView tvStatusExtra = card.findViewById(R.id.tvStatusExtra);

                    String courtName = court.getCourtName() != null ? court.getCourtName() : ("Court " + court.getId());
                    tvCourtLabel.setText(courtName.toUpperCase(Locale.US).replace(" ", " "));

                    if (occupying != null) {
                        tvCourtLabel.setTextColor(0xFFFACC15);
                        tvCourtStatusBadge.setText("OCCUPIED");
                        tvCourtStatusBadge.setBackgroundResource(R.drawable.bg_vendor_button);
                        tvCourtStatusBadge.getBackground().setTint(0x40FACC15);
                        tvCourtStatusBadge.setTextColor(0xFF000000);
                        lblStatusRow.setText("CURRENT PLAYER");
                        tvStatusValue.setText(bookingDisplayName(occupying));
                        tvStatusValue.setVisibility(View.VISIBLE);
                        iconStatusExtra.setImageResource(R.drawable.ic_hourglass);
                        tvStatusExtra.setText(formatTimeLeft(occupying.getDate(), occupying.getEndTime(), apiDate, apiTime));
                        tvStatusExtra.setTextColor(0xFFFACC15);
                        card.setBackgroundResource(R.drawable.bg_vendor_card_gold);
                    } else {
                        tvCourtLabel.setTextColor(0xFF4B5563);
                        tvCourtStatusBadge.setText("AVAILABLE");
                        tvCourtStatusBadge.setBackgroundResource(R.drawable.bg_vendor_input);
                        tvCourtStatusBadge.getBackground().setTint(0xFF111827);
                        tvCourtStatusBadge.setTextColor(0xFFFACC15);
                        lblStatusRow.setText("STATUS");
                        tvStatusValue.setText("Ready for play");
                        tvStatusValue.getPaint().setFakeBoldText(false);
                        iconStatusExtra.setImageResource(R.drawable.ic_check);
                        tvStatusExtra.setText("Open Slot");
                        tvStatusExtra.setTextColor(0xFF4B5563);
                        card.setBackgroundResource(R.drawable.bg_vendor_card);
                    }
                    courtStatusContainer.addView(card);
                }
            }
        }
    }

    private void bindUsageAnalytics(List<Court> courts, List<Booking> bookings, SimpleDateFormat apiDate, SimpleDateFormat apiTime) {
        if (tvUtilLabel == null || tvUtilValue == null || tvUtilValueDelta == null) return;

        // Define current week (last 7 days including today) and previous week (7 days before that)
        Calendar cal = Calendar.getInstance();
        long todayStartMs;
        {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            todayStartMs = cal.getTimeInMillis();
        }
        long weekStartMs = todayStartMs - 6L * 24 * 60 * 60 * 1000;
        long prevWeekEndMs = weekStartMs - 1;
        long prevWeekStartMs = prevWeekEndMs - 6L * 24 * 60 * 60 * 1000;

        double openHoursPerDay = 0.0;
        if (courts != null) {
            for (Court c : courts) {
                if (c == null || c.getOpeningTime() == null || c.getClosingTime() == null) continue;
                try {
                    Date ot = apiTime.parse(c.getOpeningTime());
                    Date ct = apiTime.parse(c.getClosingTime());
                    if (ot != null && ct != null && ct.after(ot)) {
                        openHoursPerDay += (ct.getTime() - ot.getTime()) / (1000.0 * 60 * 60);
                    }
                } catch (Exception ignored) {}
            }
        }

        double weekBooked = 0.0;
        double prevWeekBooked = 0.0;
        if (bookings != null) {
            for (Booking b : bookings) {
                if (b == null || b.getDate() == null || b.getStartTime() == null || b.getEndTime() == null) continue;
                try {
                    Date d = apiDate.parse(b.getDate());
                    Date s = apiTime.parse(b.getStartTime());
                    Date e = apiTime.parse(b.getEndTime());
                    if (d == null || s == null || e == null) continue;
                    Calendar dayCal = Calendar.getInstance();
                    dayCal.setTime(d);
                    long dayStartMs = dayCal.getTimeInMillis();
                    if (dayStartMs < prevWeekStartMs || dayStartMs > todayStartMs) continue;
                    double hours = (e.getTime() - s.getTime()) / (1000.0 * 60 * 60);
                    if (hours <= 0) continue;
                    if (dayStartMs >= weekStartMs) {
                        weekBooked += hours;
                    } else {
                        prevWeekBooked += hours;
                    }
                } catch (Exception ignored) {}
            }
        }

        double daysPerWeek = 7.0;
        double weekOpen = openHoursPerDay * daysPerWeek;
        double prevWeekOpen = weekOpen; // same schedule assumed

        double weekUtil = (weekOpen > 0) ? (weekBooked / weekOpen * 100.0) : 0.0;
        double prevWeekUtil = (prevWeekOpen > 0) ? (prevWeekBooked / prevWeekOpen * 100.0) : 0.0;
        double delta = weekUtil - prevWeekUtil;

        tvUtilLabel.setText("Weekly Utilization");
        tvUtilValue.setText(String.format(Locale.US, "%.0f%% Avg", weekUtil));

        if (Double.isNaN(delta) || weekOpen == 0) {
            tvUtilValueDelta.setText("No data yet");
            tvUtilValueDelta.setTextColor(0xFF9CA3AF);
        } else {
            String sign = delta > 0 ? "+" : "";
            tvUtilValueDelta.setText(String.format(Locale.US, "%s%.0f%% vs last week", sign, delta));
            tvUtilValueDelta.setTextColor(delta >= 0 ? 0xFF10B981 : 0xFFEF4444);
        }
    }

    private long getBookingStartMs(SimpleDateFormat apiDate, SimpleDateFormat apiTime, Booking b) {
        try {
            java.util.Date d = apiDate.parse(b.getDate());
            java.util.Date t = apiTime.parse(b.getStartTime());
            if (d == null || t == null) return Long.MAX_VALUE;
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Calendar tCal = Calendar.getInstance();
            tCal.setTime(t);
            cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, tCal.get(Calendar.SECOND));
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private Booking findCurrentBookingForCourt(int courtId, List<Booking> bookings, String todayStr, long nowMs, SimpleDateFormat apiDate, SimpleDateFormat apiTime) {
        for (Booking b : bookings) {
            if (b == null || b.getCourt() == null || b.getCourt().getId() != courtId) continue;
            if (!todayStr.equals(b.getDate())) continue;
            if (!"Confirmed".equalsIgnoreCase(b.getStatus())) continue;
            try {
                java.util.Date date = apiDate.parse(b.getDate());
                java.util.Date start = apiTime.parse(b.getStartTime());
                java.util.Date end = apiTime.parse(b.getEndTime());
                if (date == null || start == null || end == null) continue;
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(date);
                Calendar startTimeCal = Calendar.getInstance();
                startTimeCal.setTime(start);
                startCal.set(Calendar.HOUR_OF_DAY, startTimeCal.get(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startTimeCal.get(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startTimeCal.get(Calendar.SECOND));
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(date);
                Calendar endTimeCal = Calendar.getInstance();
                endTimeCal.setTime(end);
                endCal.set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE));
                endCal.set(Calendar.SECOND, endTimeCal.get(Calendar.SECOND));
                long startMs = startCal.getTimeInMillis();
                long endMs = endCal.getTimeInMillis();
                if (nowMs >= startMs && nowMs <= endMs) return b;
            } catch (Exception ignore) {}
        }
        return null;
    }

    private String bookingDisplayName(Booking b) {
        if (b.getUser() != null && b.getUser().getFullName() != null) return b.getUser().getFullName();
        if (b.getCustomerName() != null) return b.getCustomerName();
        return "—";
    }

    private String formatTimeRange(String start, String end) {
        if (start == null || end == null) return "—";
        try {
            SimpleDateFormat in = new SimpleDateFormat("HH:mm:ss", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("hh:mm a", Locale.US);
            return out.format(in.parse(start)) + " - " + out.format(in.parse(end));
        } catch (Exception e) {
            return start + " - " + end;
        }
    }

    private String formatStartsIn(String dateStr, String startTimeStr, SimpleDateFormat apiDate, SimpleDateFormat apiTime) {
        try {
            java.util.Date d = apiDate.parse(dateStr);
            java.util.Date t = apiTime.parse(startTimeStr);
            if (d == null || t == null) return "";
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Calendar tCal = Calendar.getInstance();
            tCal.setTime(t);
            cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, 0);
            long diff = cal.getTimeInMillis() - System.currentTimeMillis();
            if (diff <= 0) return "Starting now";
            int min = (int) (diff / (60 * 1000));
            if (min < 60) return "Starts in " + min + "m";
            int h = min / 60;
            min = min % 60;
            return "Starts in " + h + "h " + min + "m";
        } catch (Exception e) {
            return "";
        }
    }

    private String formatTimeLeft(String dateStr, String endTimeStr, SimpleDateFormat apiDate, SimpleDateFormat apiTime) {
        try {
            java.util.Date d = apiDate.parse(dateStr);
            java.util.Date t = apiTime.parse(endTimeStr);
            if (d == null || t == null) return "";
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Calendar tCal = Calendar.getInstance();
            tCal.setTime(t);
            cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, tCal.get(Calendar.SECOND));
            long diff = cal.getTimeInMillis() - System.currentTimeMillis();
            if (diff <= 0) return "Ended";
            int min = (int) (diff / (60 * 1000));
            return min + "m left";
        } catch (Exception e) {
            return "";
        }
    }
}
