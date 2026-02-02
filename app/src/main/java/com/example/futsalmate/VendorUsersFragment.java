package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class VendorUsersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_users, container, false);

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
        view.findViewById(R.id.btnViewHistory1).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), VendorUserHistoryActivity.class);
            startActivity(intent);
        });

        // Click listener for "View History" on Recent Activity Card 2
        view.findViewById(R.id.btnViewHistory2).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), VendorUserHistoryActivity.class);
            startActivity(intent);
        });

        // "See all" top customers
        view.findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Showing all customers", Toast.LENGTH_SHORT).show();
        });

        // Filter button
        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Opening filters", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
