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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.adapters.FeaturedCourtsAdapter;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.ShowCourtsResponse;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.api.models.UserDashboardData;
import com.example.futsalmate.api.models.UserDashboardResponse;
import com.example.futsalmate.utils.TokenManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TokenManager tokenManager;
    private FeaturedCourtsAdapter featuredAdapter;
    private RecyclerView recyclerFeaturedCourts;
    private TextView tvFeaturedEmpty;
    private TextView tvGreetingName;
    private final List<Court> featuredCourts = new ArrayList<>();
    private View activeBookingHeader;
    private View activeBookingCard;
    private TextView tvActiveBookingCountdown;
    private TextView tvActiveBookingCourtName;
    private TextView tvActiveBookingTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tokenManager = new TokenManager(requireContext());
        recyclerFeaturedCourts = view.findViewById(R.id.recyclerFeaturedCourts);
        tvFeaturedEmpty = view.findViewById(R.id.tvFeaturedEmpty);
        tvGreetingName = view.findViewById(R.id.tvGreetingName);
        activeBookingHeader = view.findViewById(R.id.activeBookingHeader);
        activeBookingCard = view.findViewById(R.id.activeBookingCard);
        tvActiveBookingCountdown = view.findViewById(R.id.tvActiveBookingCountdown);
        tvActiveBookingCourtName = view.findViewById(R.id.tvActiveBookingCourtName);
        tvActiveBookingTime = view.findViewById(R.id.tvActiveBookingTime);

        recyclerFeaturedCourts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        featuredAdapter = new FeaturedCourtsAdapter(this::openCourtDetails);
        recyclerFeaturedCourts.setAdapter(featuredAdapter);

        if (tvGreetingName != null) {
            tvGreetingName.setText("Player");
        }

        // Profile click -> switchToProfile
        view.findViewById(R.id.ivProfile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToProfile();
            }
        });

        // btnBook click -> CourtsActivity
        view.findViewById(R.id.btnBook).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CourtsActivity.class));
        });

        view.findViewById(R.id.btnDetails).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), BookedDetailsActivity.class));
        });

        view.findViewById(R.id.tvViewAll).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CourtsActivity.class));
        });

        loadFeaturedCourts();
        loadUserDashboard();

        return view;
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
                        if (!response.isSuccessful() || response.body() == null || response.body().getCourts() == null) {
                            loadFeaturedCourtsPublic();
                            return;
                        }
                        updateFeaturedCourts(response.body().getCourts());
                    }

                    @Override
                    public void onFailure(Call<ShowCourtsResponse> call, Throwable t) {
                        loadFeaturedCourtsPublic();
                    }
                });
    }

    private void loadFeaturedCourtsPublic() {
        RetrofitClient.getInstance().getApiService().showBookCourtPublic()
                .enqueue(new Callback<ShowCourtsResponse>() {
                    @Override
                    public void onResponse(Call<ShowCourtsResponse> call, Response<ShowCourtsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getCourts() == null) {
                            showFeaturedEmpty();
                            return;
                        }
                        updateFeaturedCourts(response.body().getCourts());
                    }

                    @Override
                    public void onFailure(Call<ShowCourtsResponse> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        showFeaturedEmpty();
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
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            showActiveBookingEmpty();
                            return;
                        }
                        bindUserDashboard(response.body().getData());
                    }

                    @Override
                    public void onFailure(Call<UserDashboardResponse> call, Throwable t) {
                        showActiveBookingEmpty();
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
        }

        List<Booking> upcoming = data.getUpcomingBookings();
        if (upcoming == null || upcoming.isEmpty()) {
            showActiveBookingEmpty();
            return;
        }
        bindActiveBooking(upcoming.get(0));
    }

    private void bindActiveBooking(Booking booking) {
        if (booking == null) {
            showActiveBookingEmpty();
            return;
        }

        if (activeBookingHeader != null) {
            activeBookingHeader.setVisibility(View.VISIBLE);
        }
        if (activeBookingCard != null) {
            activeBookingCard.setVisibility(View.VISIBLE);
        }

        String courtName = booking.getCourt() != null ? booking.getCourt().getCourtName() : null;
        if (tvActiveBookingCourtName != null) {
            tvActiveBookingCourtName.setText(courtName != null ? courtName : "Court");
        }

        String timeText = formatBookingTime(booking);
        if (tvActiveBookingTime != null) {
            tvActiveBookingTime.setText(timeText);
        }

        if (tvActiveBookingCountdown != null) {
            tvActiveBookingCountdown.setText(formatCountdown(booking));
        }
    }

    private void showActiveBookingEmpty() {
        if (activeBookingHeader != null) {
            activeBookingHeader.setVisibility(View.GONE);
        }
        if (activeBookingCard != null) {
            activeBookingCard.setVisibility(View.GONE);
        }
    }

    private String formatBookingTime(Booking booking) {
        String date = booking.getDate();
        LocalDate bookingDate = parseDate(date);
        LocalTime start = parseTime(booking.getStartTime());
        LocalTime end = parseTime(booking.getEndTime());

        String startText = start != null ? start.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())) : "";
        String endText = end != null ? end.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())) : "";

        if (bookingDate != null) {
            LocalDate today = LocalDate.now();
            String dateLabel = bookingDate.equals(today)
                    ? "Today"
                    : bookingDate.format(DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault()));
            return dateLabel + ", " + startText + " - " + endText;
        }
        return startText + " - " + endText;
    }

    private String formatCountdown(Booking booking) {
        LocalDate bookingDate = parseDate(booking.getDate());
        LocalTime start = parseTime(booking.getStartTime());
        if (bookingDate == null || start == null) {
            return "Upcoming";
        }
        LocalDateTime startDateTime = LocalDateTime.of(bookingDate, start);
        Duration duration = Duration.between(LocalDateTime.now(), startDateTime);
        long seconds = Math.max(0, duration.getSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format(Locale.getDefault(), "Starts in %02d:%02d:%02d", hours, minutes, secs);
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return null;
        }
        String trimmed = time.trim();
        int dotIndex = trimmed.indexOf('.');
        if (dotIndex > 0) {
            trimmed = trimmed.substring(0, dotIndex);
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("H:mm")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(trimmed, formatter);
            } catch (Exception ignored) {
                // try next
            }
        }
        return null;
    }
}
