package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.futsalmate.adapters.FeaturedCourtsAdapter;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.ShowCourtsResponse;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.api.models.UserDashboardData;
import com.example.futsalmate.api.models.UserDashboardResponse;
import com.example.futsalmate.utils.TokenManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardDebug";
    private TokenManager tokenManager;
    private FeaturedCourtsAdapter featuredAdapter;
    private RecyclerView recyclerFeaturedCourts;
    private TextView tvFeaturedEmpty;
    private TextView tvGreetingName;
    private ImageView ivProfile;
    private final List<Court> featuredCourts = new ArrayList<>();
    private View activeBookingHeader;
    private View activeBookingCard;
    private View activeBookingEmptyCard;
    private TextView tvActiveBookingCountdown;
    private TextView tvActiveBookingCourtName;
    private TextView tvActiveBookingTime;
    private TextView btnBookNow;
    private Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tokenManager = new TokenManager(requireContext());
        recyclerFeaturedCourts = view.findViewById(R.id.recyclerFeaturedCourts);
        tvFeaturedEmpty = view.findViewById(R.id.tvFeaturedEmpty);
        tvGreetingName = view.findViewById(R.id.tvGreetingName);
        ivProfile = view.findViewById(R.id.ivProfile);
        activeBookingHeader = view.findViewById(R.id.activeBookingHeader);
        activeBookingCard = view.findViewById(R.id.activeBookingCard);
        activeBookingEmptyCard = view.findViewById(R.id.activeBookingEmptyCard);
        tvActiveBookingCountdown = view.findViewById(R.id.tvActiveBookingCountdown);
        tvActiveBookingCourtName = view.findViewById(R.id.tvActiveBookingCourtName);
        tvActiveBookingTime = view.findViewById(R.id.tvActiveBookingTime);
        btnBookNow = view.findViewById(R.id.btnBookNow);

        recyclerFeaturedCourts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        featuredAdapter = new FeaturedCourtsAdapter(this::openCourtDetails);
        recyclerFeaturedCourts.setAdapter(featuredAdapter);

        String cachedName = tokenManager.getUserName();
        if (tvGreetingName != null) {
            if (cachedName != null && !cachedName.isEmpty()) {
                tvGreetingName.setText(cachedName);
            } else {
                tvGreetingName.setText("Player");
            }
        }
        loadCachedAvatar();

        view.findViewById(R.id.ivProfile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToProfile();
            }
        });

        view.findViewById(R.id.ivNotification).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
        });

        view.findViewById(R.id.btnBook).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CourtsActivity.class));
        });

        if (btnBookNow != null) {
            btnBookNow.setOnClickListener(v -> startActivity(new Intent(getActivity(), CourtsActivity.class)));
        }

        view.findViewById(R.id.tvViewAll).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CourtsActivity.class));
        });

        loadFeaturedCourts();
        loadUserDashboard();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

    private void handleUnauthorizedError() {
        if(isAdded()) {
            tokenManager.clearToken();
            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void loadFeaturedCourts() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            loadFeaturedCourtsPublic();
            return;
        }

        RetrofitClient.getInstance().getApiService().showBookCourt(token)
                .enqueue(new Callback<ShowCourtsResponse>() {
                    @Override
                    public void onResponse(Call<ShowCourtsResponse> call, Response<ShowCourtsResponse> response) {
                        if (!isAdded()) return;
                        if (response.code() == 401) {
                            handleUnauthorizedError();
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null || response.body().getCourts() == null) {
                            loadFeaturedCourtsPublic();
                            return;
                        }
                        updateFeaturedCourts(response.body().getCourts());
                    }

                    @Override
                    public void onFailure(Call<ShowCourtsResponse> call, Throwable t) {
                        if(isAdded()) {
                            loadFeaturedCourtsPublic();
                        }
                    }
                });
    }

    private void loadFeaturedCourtsPublic() {
        RetrofitClient.getInstance().getApiService().showBookCourtPublic()
                .enqueue(new Callback<ShowCourtsResponse>() {
                    @Override
                    public void onResponse(Call<ShowCourtsResponse> call, Response<ShowCourtsResponse> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null || response.body().getCourts() == null) {
                            showFeaturedEmpty();
                            return;
                        }
                        updateFeaturedCourts(response.body().getCourts());
                    }

                    @Override
                    public void onFailure(Call<ShowCourtsResponse> call, Throwable t) {
                        if(isAdded()) {
                            Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            showFeaturedEmpty();
                        }
                    }
                });
    }

    private void updateFeaturedCourts(List<Court> courts) {
        featuredCourts.clear();
        if (courts != null) {
            int max = Math.min(courts.size(), 4);
            featuredCourts.addAll(courts.subList(0, max));
        }
        featuredAdapter.setCourts(featuredCourts);
        boolean isEmpty = featuredCourts.isEmpty();
        recyclerFeaturedCourts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (tvFeaturedEmpty != null) {
            tvFeaturedEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void showFeaturedEmpty() {
        featuredAdapter.setCourts(new ArrayList<>());
        recyclerFeaturedCourts.setVisibility(View.GONE);
        if (tvFeaturedEmpty != null) {
            tvFeaturedEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void openCourtDetails(Court court) {
        Intent intent = new Intent(getActivity(), CourtDetailsActivity.class);
        intent.putExtra("court_id", court.getId());
        intent.putExtra("court_name", court.getCourtName());
        intent.putExtra("court_location", court.getLocation());
        intent.putExtra("court_price", court.getPrice());
        intent.putExtra("court_image", court.getImage());
        startActivity(intent);
    }

    private void loadUserDashboard() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            showActiveBookingEmpty();
            return;
        }

        RetrofitClient.getInstance().getApiService().userDashboard(token)
                .enqueue(new Callback<UserDashboardResponse>() {
                    @Override
                    public void onResponse(Call<UserDashboardResponse> call, Response<UserDashboardResponse> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (response.code() == 401) {
                            handleUnauthorizedError();
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "API Response: " + new com.google.gson.Gson().toJson(response.body()));
                            if (response.body().getData() != null) {
                                bindUserDashboard(response.body().getData());
                            } else {
                                Log.w(TAG, "API response data is null");
                                showActiveBookingEmpty();
                            }
                        } else {
                            Log.w(TAG, "API call not successful. Code: " + response.code());
                            showActiveBookingEmpty();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserDashboardResponse> call, Throwable t) {
                        if(isAdded()) {
                            Log.e(TAG, "API call failed.", t);
                            showActiveBookingEmpty();
                        }
                    }
                });
    }

    private void bindUserDashboard(UserDashboardData data) {
        if (data == null) {
            showActiveBookingEmpty();
            return;
        }

        User user = data.getUser();
        if (tvGreetingName != null) {
            String name = user != null ? user.getFullName() : null;
            tvGreetingName.setText(name != null && !name.trim().isEmpty() ? name : "Player");
            if (name != null && !name.trim().isEmpty()) {
                tokenManager.saveUserName(name);
            }
        }
        if (user != null && user.getProfilePhotoUrl() != null && tokenManager != null) {
            tokenManager.saveUserAvatar(user.getProfilePhotoUrl());
            loadAvatar(user.getProfilePhotoUrl());
        }

        List<Booking> source = data.getUpcomingBookings();
        List<Booking> upcoming = filterUpcoming(source);

        if (!upcoming.isEmpty()) {
            Collections.sort(upcoming, (b1, b2) -> {
                Date d1 = parseDateTime(b1.getDate(), b1.getStartTime());
                Date d2 = parseDateTime(b2.getDate(), b2.getStartTime());
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d1.compareTo(d2);
            });
            bindActiveBooking(upcoming.get(0), false);
            return;
        }

        List<Booking> current = filterCurrent(source);
        if (!current.isEmpty()) {
            Collections.sort(current, (b1, b2) -> {
                Date d1 = parseDateTime(b1.getDate(), b1.getStartTime());
                Date d2 = parseDateTime(b2.getDate(), b2.getStartTime());
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d2.compareTo(d1); // Most recent first
            });
            bindActiveBooking(current.get(0), true);
            return;
        }

        showActiveBookingEmpty();
    }

    private void bindActiveBooking(final Booking booking, final boolean isCurrent) {
        if (booking == null) {
            showActiveBookingEmpty();
            return;
        }

        activeBookingHeader.setVisibility(View.VISIBLE);
        activeBookingCard.setVisibility(View.VISIBLE);
        activeBookingEmptyCard.setVisibility(View.GONE);

        String courtName = booking.getCourt() != null ? booking.getCourt().getCourtName() : "Court";
        tvActiveBookingCourtName.setText(courtName);
        tvActiveBookingTime.setText(formatBookingTime(booking));

        View btnDetails = activeBookingCard.findViewById(R.id.btnDetails);
        btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BookedDetailsActivity.class);
            Court court = booking.getCourt();
            if (court != null) {
                intent.putExtra("court_name", court.getCourtName());
                intent.putExtra("court_location", court.getLocation());
                intent.putExtra("court_image", court.getImage());
                intent.putExtra("court_price", court.getPrice());
            }
            intent.putExtra("booking_date", booking.getDate());
            intent.putExtra("start_time", booking.getStartTime());
            intent.putExtra("end_time", booking.getEndTime());
            intent.putExtra("status", booking.getStatus());
            startActivity(intent);
        });

        if (countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                tvActiveBookingCountdown.setText(formatCountdown(booking, isCurrent));
                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private void showActiveBookingEmpty() {
        activeBookingHeader.setVisibility(View.GONE);
        activeBookingCard.setVisibility(View.GONE);
        activeBookingEmptyCard.setVisibility(View.VISIBLE);
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

    private String formatBookingTime(Booking booking) {
        Date startTime = parseDateTime(booking.getDate(), booking.getStartTime());
        Date endTime = parseDateTime(booking.getDate(), booking.getEndTime());

        if (startTime == null || endTime == null) return "";

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String startText = timeFormat.format(startTime);
        String endText = timeFormat.format(endTime);

        Calendar bookingCal = Calendar.getInstance();
        bookingCal.setTime(startTime);
        Calendar todayCal = Calendar.getInstance();

        String dateLabel;
        if (bookingCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                bookingCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
            dateLabel = "Today";
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.US);
            dateLabel = dateFormat.format(startTime);
        }
        return dateLabel + ", " + startText + " - " + endText;
    }

    private String formatCountdown(Booking booking, boolean isCurrent) {
        Date startTime = parseDateTime(booking.getDate(), booking.getStartTime());
        Date endTime = parseDateTime(booking.getDate(), booking.getEndTime());
        Date now = new Date();

        if (isCurrent && endTime != null && (startTime.before(now) || startTime.equals(now)) && endTime.after(now)) {
            return "Playing now";
        }
        
        if (startTime == null) return "Upcoming";

        long diffInMillis = startTime.getTime() - now.getTime();
        if (diffInMillis <= 0) {
            return "Upcoming";
        }

        long seconds = diffInMillis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format(Locale.US, "Starts in %02d:%02d:%02d", hours, minutes, secs);
    }

    private List<Booking> filterUpcoming(List<Booking> source) {
        List<Booking> result = new ArrayList<>();
        if (source == null) return result;
        Date now = new Date();
        for (Booking booking : source) {
            Date startTime = parseDateTime(booking.getDate(), booking.getStartTime());
            if (startTime != null && startTime.after(now)) {
                result.add(booking);
            }
        }
        return result;
    }

    private List<Booking> filterCurrent(List<Booking> source) {
        List<Booking> result = new ArrayList<>();
        if (source == null) return result;
        Date now = new Date();
        for (Booking booking : source) {
            Date startTime = parseDateTime(booking.getDate(), booking.getStartTime());
            Date endTime = parseDateTime(booking.getDate(), booking.getEndTime());
            if (startTime != null && endTime != null && (startTime.before(now) || startTime.equals(now)) && endTime.after(now)) {
                result.add(booking);
            }
        }
        return result;
    }

    private Date parseDateTime(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) return null;

        String time = timeStr.substring(0, Math.min(timeStr.length(), 8)); // Handle cases with milliseconds
        String dateTimeString = dateStr + " " + time;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            return sdf.parse(dateTimeString);
        } catch (ParseException e) {
            // Try another format if the first fails
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            try {
                return sdf.parse(dateTimeString);
            } catch (ParseException e2) {
                return null;
            }
        }
    }

    private void loadCachedAvatar() {
        if (tokenManager == null) {
            return;
        }
        String cached = tokenManager.getUserAvatar();
        if (cached != null && !cached.trim().isEmpty()) {
            loadAvatar(cached);
        }
    }

    private void loadAvatar(String value) {
        if (ivProfile == null || value == null || value.trim().isEmpty()) {
            return;
        }
        Object source = normalizeAvatarSource(value);
        Glide.with(this)
                .load(source)
                .placeholder(R.drawable.ic_1)
                .error(R.drawable.ic_1)
                .into(ivProfile);
    }

    private Object normalizeAvatarSource(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("content://") || trimmed.startsWith("file://")) {
            return trimmed;
        }
        return new File(trimmed);
    }
}
