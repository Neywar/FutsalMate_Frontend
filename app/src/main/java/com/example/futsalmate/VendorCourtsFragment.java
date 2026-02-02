package com.example.futsalmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class VendorCourtsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_courts, container, false);

        View btnSearch = view.findViewById(R.id.btnSearch);
        View searchContainer = view.findViewById(R.id.searchContainer);
        View btnCloseSearch = view.findViewById(R.id.btnCloseSearch);
        EditText etSearch = view.findViewById(R.id.etSearch);

        // Show search box
        if (btnSearch != null && searchContainer != null) {
            btnSearch.setOnClickListener(v -> {
                searchContainer.setVisibility(View.VISIBLE);
                if (etSearch != null) {
                    etSearch.requestFocus();
                }
            });
        }

        // Hide search box
        if (btnCloseSearch != null && searchContainer != null) {
            btnCloseSearch.setOnClickListener(v -> {
                if (etSearch != null) {
                    etSearch.setText("");
                }
                searchContainer.setVisibility(View.GONE);
            });
        }

        // FAB to add new court
        view.findViewById(R.id.fabAddCourt).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddCourtActivity.class));
        });

        // Setup Court actions (Edit and Delete) for all static cards
        setupCourtActions(view, R.id.ivEdit1, R.id.ivDelete1, R.id.cardCourt1, "Stadium A");
        setupCourtActions(view, R.id.ivEdit2, R.id.ivDelete2, R.id.cardCourt2, "Stadium B");
        setupCourtActions(view, R.id.ivEdit3, R.id.ivDelete3, R.id.cardCourt3, "Central Arena");

        return view;
    }

    private void setupCourtActions(View rootView, int editId, int deleteId, int cardId, String courtName) {
        View editBtn = rootView.findViewById(editId);
        View deleteBtn = rootView.findViewById(deleteId);
        View cardView = rootView.findViewById(cardId);

        // Edit Redirection
        if (editBtn != null) {
            editBtn.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), EditCourtActivity.class));
            });
        }

        // Delete Confirmation
        if (deleteBtn != null && cardView != null) {
            deleteBtn.setOnClickListener(v -> {
                showDeleteConfirmation(courtName, cardView);
            });
        }
    }

    private void showDeleteConfirmation(String courtName, View cardView) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Court")
                .setMessage("Are you sure you want to delete \"" + courtName + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Remove the court card from the UI
                    cardView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), courtName + " deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
