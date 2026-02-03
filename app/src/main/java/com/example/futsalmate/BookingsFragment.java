package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class BookingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        // Header Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new DashboardFragment(), R.id.nav_home);
            }
        });

        // 1. Details button (Wembley Arena) -> BookedDetailsActivity
        view.findViewById(R.id.btnDetails1).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), BookedDetailsActivity.class));
        });

        // 2. Details button (Stamford Bridge) -> BookedDetailsActivity
        view.findViewById(R.id.btnDetails2).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), BookedDetailsActivity.class));
        });

        // 3. Receipt button (The Emirates) -> BookedDetailsActivity
        view.findViewById(R.id.btnReceipt1).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), BookedDetailsActivity.class));
        });

        // Setup 3-dot menus
        view.findViewById(R.id.btnMenu1).setOnClickListener(v -> showBookingMenu(v, "Wembley Arena", 15, 10, 2024, 18, 0));
        view.findViewById(R.id.btnMenu2).setOnClickListener(v -> showBookingMenu(v, "Stamford Bridge", 15, 10, 2024, 20, 0));

        return view;
    }

    private void showBookingMenu(View view, String courtName, int day, int month, int year, int hour, int minute) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(getActivity(), R.style.DarkPopupMenuTheme);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenu().add("Edit Booking");
        popup.getMenu().add("Delete Booking");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit Booking")) {
                if (canModifyBooking(year, month, day, hour, minute)) {
                    Intent intent = new Intent(getActivity(), SelectTimeslotActivity.class);
                    intent.putExtra("EDIT_MODE", true);
                    intent.putExtra("COURT_NAME", courtName);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "Cannot edit booking within 2 hours of start time", Toast.LENGTH_LONG).show();
                }
                return true;
            } else if (item.getTitle().equals("Delete Booking")) {
                if (canModifyBooking(year, month, day, hour, minute)) {
                    // Logic to delete the booking would go here
                    view.setVisibility(View.GONE); // Visual feedback
                    Toast.makeText(getActivity(), "Booking for " + courtName + " deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Cannot delete booking within 2 hours of start time", Toast.LENGTH_LONG).show();
                }
                return true;
            }
            return false;
        });

        popup.show();
    }

    private boolean canModifyBooking(int year, int month, int day, int hour, int minute) {
        Calendar bookingTime = Calendar.getInstance();
        bookingTime.set(year, month - 1, day, hour, minute);
        
        long currentTimeMillis = System.currentTimeMillis();
        long bookingTimeMillis = bookingTime.getTimeInMillis();
        
        // Check if current time is more than 2 hours (2 * 60 * 60 * 1000 ms) before booking time
        return (bookingTimeMillis - currentTimeMillis) > (2 * 60 * 60 * 1000);
    }
}
