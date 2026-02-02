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

public class BookingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        // Header Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
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
        view.findViewById(R.id.btnMenu1).setOnClickListener(this::showBookingMenu);
        view.findViewById(R.id.btnMenu2).setOnClickListener(this::showBookingMenu);

        return view;
    }

    private void showBookingMenu(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(getActivity(), R.style.DarkPopupMenuTheme);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenu().add("Edit Booking");
        popup.getMenu().add("Delete Booking");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit Booking")) {
                Toast.makeText(getActivity(), "Edit Booking clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getTitle().equals("Delete Booking")) {
                Toast.makeText(getActivity(), "Delete Booking clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }
}
