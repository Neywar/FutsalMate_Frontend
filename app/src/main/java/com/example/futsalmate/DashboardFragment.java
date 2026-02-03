package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Profile click -> switchToProfile
        view.findViewById(R.id.ivProfile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToProfile();
            }
        });

        // btnBook click -> BookingsFragment
        view.findViewById(R.id.btnBook).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToBookings();
            }
        });

        view.findViewById(R.id.btnDetails).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), BookedDetailsActivity.class));
        });

        view.findViewById(R.id.tvViewAll).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CourtsActivity.class));
        });

        view.findViewById(R.id.courtCard1).setOnClickListener(v -> openCourtDetails());
        view.findViewById(R.id.courtCard2).setOnClickListener(v -> openCourtDetails());
        view.findViewById(R.id.courtCard3).setOnClickListener(v -> openCourtDetails());

        return view;
    }

    private void openCourtDetails() {
        startActivity(new Intent(getActivity(), CourtDetailsActivity.class));
    }
}
