package com.example.futsalmate;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.adapters.BookingsAdapter;
import com.example.futsalmate.adapters.PastBookingsTableAdapter;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.Booking;
import com.example.futsalmate.api.models.PastBookingsResponse;
import com.example.futsalmate.api.models.UpcomingBookingsResponse;
import com.example.futsalmate.api.models.ViewBookingResponse;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingsFragment extends Fragment {

    private TokenManager tokenManager;
    private BookingsAdapter adapter;
    private PastBookingsTableAdapter pastAdapter;
    private RecyclerView recyclerBookings;
    private RecyclerView recyclerPastTable;
    private TextView tvBookingsEmpty;
    private TextView tvActiveCount;
    private TextView tvUpcomingHeader;
    private View pastTableHeader;
    private MaterialButton btnFilterUpcoming;
    private MaterialButton btnFilterCurrent;
    private MaterialButton btnFilterPast;
    private BookingFilter currentFilter = BookingFilter.UPCOMING;
    private final List<Booking> bookings = new ArrayList<>();

    private enum BookingFilter {
        UPCOMING,
        CURRENT,
        PAST
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        tokenManager = new TokenManager(requireContext());
        recyclerBookings = view.findViewById(R.id.recyclerBookings);
        recyclerPastTable = view.findViewById(R.id.recyclerPastTable);
        tvBookingsEmpty = view.findViewById(R.id.tvBookingsEmpty);
        tvUpcomingHeader = view.findViewById(R.id.tvUpcomingHeader);
        pastTableHeader = view.findViewById(R.id.pastTableHeader);
        btnFilterUpcoming = view.findViewById(R.id.btnFilterUpcoming);
        btnFilterCurrent = view.findViewById(R.id.btnFilterCurrent);
        btnFilterPast = view.findViewById(R.id.btnFilterPast);

        recyclerBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookingsAdapter(this::openBookingDetails);
        recyclerBookings.setAdapter(adapter);

        if (recyclerPastTable != null) {
            recyclerPastTable.setLayoutManager(new LinearLayoutManager(requireContext()));
            pastAdapter = new PastBookingsTableAdapter();
            recyclerPastTable.setAdapter(pastAdapter);
        }

        // Header Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new DashboardFragment(), R.id.nav_home);
            }
        });

        if (btnFilterUpcoming != null) {
            btnFilterUpcoming.setOnClickListener(v -> selectFilter(BookingFilter.UPCOMING));
        }
        if (btnFilterCurrent != null) {
            btnFilterCurrent.setOnClickListener(v -> selectFilter(BookingFilter.CURRENT));
        }
        if (btnFilterPast != null) {
            btnFilterPast.setOnClickListener(v -> selectFilter(BookingFilter.PAST));
        }

        selectFilter(BookingFilter.UPCOMING);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerBookings != null) {
            selectFilter(currentFilter);
        }
    }

    private void selectFilter(BookingFilter filter) {
        currentFilter = filter;
        applyFilterUi();
        adapter.setBookings(new ArrayList<>());
        if (pastAdapter != null) {
            pastAdapter.setBookings(new ArrayList<>());
        }
        switch (filter) {
            case UPCOMING:
                loadUpcomingBookings();
                break;
            case CURRENT:
                loadCurrentBookings();
                break;
            case PAST:
                loadPastBookings();
                break;
        }
    }

    private void applyFilterUi() {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.bright_green);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_grey);
        if (btnFilterUpcoming != null) {
            int color = currentFilter == BookingFilter.UPCOMING ? activeColor : inactiveColor;
            btnFilterUpcoming.setTextColor(color);
            btnFilterUpcoming.setStrokeColor(ColorStateList.valueOf(color));
        }
        if (btnFilterCurrent != null) {
            int color = currentFilter == BookingFilter.CURRENT ? activeColor : inactiveColor;
            btnFilterCurrent.setTextColor(color);
            btnFilterCurrent.setStrokeColor(ColorStateList.valueOf(color));
        }
        if (btnFilterPast != null) {
            int color = currentFilter == BookingFilter.PAST ? activeColor : inactiveColor;
            btnFilterPast.setTextColor(color);
            btnFilterPast.setStrokeColor(ColorStateList.valueOf(color));
        }
        if (tvUpcomingHeader != null) {
            String header = currentFilter == BookingFilter.UPCOMING
                    ? "UPCOMING BOOKINGS"
                    : currentFilter == BookingFilter.CURRENT ? "CURRENT BOOKINGS" : "PAST BOOKINGS";
            tvUpcomingHeader.setText(header);
            tvUpcomingHeader.setTextColor(currentFilter == BookingFilter.PAST ? inactiveColor : activeColor);
        }
        if (pastTableHeader != null) {
            pastTableHeader.setVisibility(currentFilter == BookingFilter.PAST ? View.VISIBLE : View.GONE);
        }
        if (recyclerPastTable != null) {
            recyclerPastTable.setVisibility(currentFilter == BookingFilter.PAST ? View.VISIBLE : View.GONE);
        }
        if (recyclerBookings != null) {
            recyclerBookings.setVisibility(currentFilter == BookingFilter.PAST ? View.GONE : View.VISIBLE);
        }
        if (tvBookingsEmpty != null) {
            String empty = currentFilter == BookingFilter.UPCOMING
                    ? "No upcoming bookings"
                    : currentFilter == BookingFilter.CURRENT ? "No current bookings" : "No past bookings";
            tvBookingsEmpty.setText(empty);
        }
    }

    private void loadUpcomingBookings() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getActivity(), "Please login again.", Toast.LENGTH_SHORT).show();
            showEmpty();
            return;
        }

        RetrofitClient.getInstance().getApiService().upcomingBookings(token)
                .enqueue(new Callback<UpcomingBookingsResponse>() {
                    @Override
                    public void onResponse(Call<UpcomingBookingsResponse> call, Response<UpcomingBookingsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            showEmpty();
                            return;
                        }
                        List<Booking> upcoming = filterUpcoming(response.body().getUpcomingBookings());
                        bookings.clear();
                        if (upcoming != null) {
                            bookings.addAll(upcoming);
                        }
                        adapter.setBookings(bookings);
                        updateCountLabel(bookings.size());
                        toggleEmpty(bookings.isEmpty());
                    }

                    @Override
                    public void onFailure(Call<UpcomingBookingsResponse> call, Throwable t) {
                        showEmpty();
                    }
                });
    }

    private void loadCurrentBookings() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getActivity(), "Please login again.", Toast.LENGTH_SHORT).show();
            showEmpty();
            return;
        }

        RetrofitClient.getInstance().getApiService().upcomingBookings(token)
                .enqueue(new Callback<UpcomingBookingsResponse>() {
                    @Override
                    public void onResponse(Call<UpcomingBookingsResponse> call, Response<UpcomingBookingsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            showEmpty();
                            return;
                        }
                        List<Booking> current = filterCurrent(response.body().getUpcomingBookings());
                        bookings.clear();
                        if (current != null) {
                            bookings.addAll(current);
                        }
                        adapter.setBookings(bookings);
                        updateCountLabel(bookings.size());
                        toggleEmpty(bookings.isEmpty());
                    }

                    @Override
                    public void onFailure(Call<UpcomingBookingsResponse> call, Throwable t) {
                        showEmpty();
                    }
                });
    }

    private void loadPastBookings() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getActivity(), "Please login again.", Toast.LENGTH_SHORT).show();
            showEmpty();
            return;
        }

        RetrofitClient.getInstance().getApiService().pastBookings(token)
                .enqueue(new Callback<PastBookingsResponse>() {
                    @Override
                    public void onResponse(Call<PastBookingsResponse> call, Response<PastBookingsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            showEmpty();
                            return;
                        }
                        List<Booking> past = filterPast(response.body().getPastBookings());
                        bookings.clear();
                        if (past != null) {
                            bookings.addAll(past);
                        }
                        if (pastAdapter != null) {
                            pastAdapter.setBookings(bookings);
                        }
                        updateCountLabel(bookings.size());
                        toggleEmpty(bookings.isEmpty());
                    }

                    @Override
                    public void onFailure(Call<PastBookingsResponse> call, Throwable t) {
                        showEmpty();
                    }
                });
    }

    private void openBookingDetails(Booking booking) {
        if (booking == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), BookedDetailsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        if (booking.getCourt() != null) {
            intent.putExtra("court_id", booking.getCourt().getId());
            intent.putExtra("opening_time", booking.getCourt().getOpeningTime());
            intent.putExtra("closing_time", booking.getCourt().getClosingTime());
        }
        intent.putExtra("booking_date", booking.getDate());
        intent.putExtra("booking_start", booking.getStartTime());
        intent.putExtra("booking_end", booking.getEndTime());
        intent.putExtra("booking_status", booking.getStatus());
        intent.putExtra("booking_payment_status", booking.getPaymentStatus());
        if (booking.getCourt() != null) {
            intent.putExtra("court_name", booking.getCourt().getCourtName());
            intent.putExtra("court_location", booking.getCourt().getLocation());
            intent.putExtra("court_price", booking.getCourt().getPrice());
            intent.putExtra("court_image", booking.getCourt().getImage());
        }
        intent.putExtra("booking_filter", currentFilter.name());
        startActivity(intent);
    }

    private void updateCountLabel(int count) {
        if (tvActiveCount != null) {
            String label = currentFilter == BookingFilter.UPCOMING
                    ? "Upcoming"
                    : currentFilter == BookingFilter.CURRENT ? "Current" : "Past";
            tvActiveCount.setText(count + " " + label);
        }
    }

    private void toggleEmpty(boolean empty) {
        if (recyclerBookings != null && currentFilter != BookingFilter.PAST) {
            recyclerBookings.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (recyclerPastTable != null && currentFilter == BookingFilter.PAST) {
            recyclerPastTable.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (pastTableHeader != null && currentFilter == BookingFilter.PAST) {
            pastTableHeader.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (tvBookingsEmpty != null) {
            tvBookingsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmpty() {
        updateCountLabel(0);
        toggleEmpty(true);
    }

    private List<Booking> filterUpcoming(List<Booking> source) {
        if (source == null || source.isEmpty()) return source;
        List<Booking> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        for (Booking booking : source) {
            LocalDate date = parseDate(booking != null ? booking.getDate() : null);
            LocalTime start = parseTime(booking != null ? booking.getStartTime() : null);
            if (date == null) continue;
            if (date.isAfter(today)) {
                result.add(booking);
            } else if (date.isEqual(today) && start != null && start.isAfter(now)) {
                result.add(booking);
            }
        }
        return result;
    }

    private List<Booking> filterCurrent(List<Booking> source) {
        if (source == null || source.isEmpty()) return source;
        List<Booking> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        for (Booking booking : source) {
            LocalDate date = parseDate(booking != null ? booking.getDate() : null);
            LocalTime start = parseTime(booking != null ? booking.getStartTime() : null);
            LocalTime end = parseTime(booking != null ? booking.getEndTime() : null);
            if (date == null || start == null || end == null) continue;
            if (date.isEqual(today) && (start.equals(now) || start.isBefore(now)) && end.isAfter(now)) {
                result.add(booking);
            }
        }
        return result;
    }

    private List<Booking> filterPast(List<Booking> source) {
        if (source == null || source.isEmpty()) return source;
        List<Booking> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        for (Booking booking : source) {
            LocalDate date = parseDate(booking != null ? booking.getDate() : null);
            LocalTime end = parseTime(booking != null ? booking.getEndTime() : null);
            if (date == null) continue;
            if (date.isBefore(today)) {
                result.add(booking);
            } else if (date.isEqual(today) && end != null && !end.isAfter(now)) {
                result.add(booking);
            }
        }
        return result;
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.trim().isEmpty()) return null;
        String trimmed = time.trim();
        int dotIndex = trimmed.indexOf('.');
        if (dotIndex > 0) {
            trimmed = trimmed.substring(0, dotIndex);
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()),
                DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()),
                DateTimeFormatter.ofPattern("H:mm:ss", Locale.getDefault()),
                DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(trimmed, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
