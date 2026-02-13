package com.example.futsalmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.adapters.VendorCourtsAdapter;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.CourtsResponse;
import com.example.futsalmate.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorCourtsFragment extends Fragment {

    private static final String TAG = "VendorCourtsFragment";
    private RecyclerView recyclerViewCourts;
    private ProgressBar progressBar;
    private View emptyState;
    private VendorCourtsAdapter adapter;
    private TokenManager tokenManager;
    
    private TextView btnFilterAll, btnFilterActive, btnFilterInactive;
    private List<Court> allCourts = new ArrayList<>();
    private String currentFilter = "all"; // "all", "active", "inactive"
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_courts, container, false);

        tokenManager = new TokenManager(requireContext());
        
        // Initialize views
        recyclerViewCourts = view.findViewById(R.id.recyclerViewCourts);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterActive = view.findViewById(R.id.btnFilterActive);
        btnFilterInactive = view.findViewById(R.id.btnFilterInactive);
        
        // Setup filter buttons
        setupFilterButtons();
        
        // Setup RecyclerView
        recyclerViewCourts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new VendorCourtsAdapter(requireContext(), new VendorCourtsAdapter.OnCourtActionListener() {
            @Override
            public void onEditCourt(Court court) {
                // Use AddCourtActivity in edit mode so that
                // all fields (hours, facilities, images) and
                // the proper updateCourt API are reused.
                Intent intent = new Intent(requireContext(), AddCourtActivity.class);
                intent.putExtra("court_data", court);
                startActivity(intent);
            }

            @Override
            public void onDeleteCourt(Court court) {
                showDeleteConfirmation(court);
            }
        });
        recyclerViewCourts.setAdapter(adapter);

        // Back button
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof VendorMainActivity) {
                    ((VendorMainActivity) getActivity()).switchToDashboard();
                }
            });
        }

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
                searchQuery = "";
                applyFilter();
            });
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s != null ? s.toString().trim() : "";
                    applyFilter();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // FAB to add new court
        view.findViewById(R.id.fabAddCourt).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddCourtActivity.class));
        });
        
        // Load courts
        loadVendorCourts();

        return view;
    }
    
    private void setupFilterButtons() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterUI();
            applyFilter();
        });
        
        btnFilterActive.setOnClickListener(v -> {
            currentFilter = "active";
            updateFilterUI();
            applyFilter();
        });
        
        btnFilterInactive.setOnClickListener(v -> {
            currentFilter = "inactive";
            updateFilterUI();
            applyFilter();
        });
    }
    
    private void updateFilterUI() {
        // Reset all buttons to unselected state
        btnFilterAll.setBackgroundResource(R.drawable.bg_vendor_card);
        btnFilterAll.setBackgroundTintList(getResources().getColorStateList(R.color.gray_border));
        btnFilterAll.setTextColor(getResources().getColor(R.color.gray_text));
        
        btnFilterActive.setBackgroundResource(R.drawable.bg_vendor_card);
        btnFilterActive.setBackgroundTintList(getResources().getColorStateList(R.color.gray_border));
        btnFilterActive.setTextColor(getResources().getColor(R.color.gray_text));
        
        btnFilterInactive.setBackgroundResource(R.drawable.bg_vendor_card);
        btnFilterInactive.setBackgroundTintList(getResources().getColorStateList(R.color.gray_border));
        btnFilterInactive.setTextColor(getResources().getColor(R.color.gray_text));
        
        // Set selected button
        switch (currentFilter) {
            case "all":
                btnFilterAll.setBackgroundResource(R.drawable.bg_vendor_button);
                btnFilterAll.setBackgroundTintList(null);
                btnFilterAll.setTextColor(getResources().getColor(R.color.black));
                break;
            case "active":
                btnFilterActive.setBackgroundResource(R.drawable.bg_vendor_button);
                btnFilterActive.setBackgroundTintList(null);
                btnFilterActive.setTextColor(getResources().getColor(R.color.black));
                break;
            case "inactive":
                btnFilterInactive.setBackgroundResource(R.drawable.bg_vendor_button);
                btnFilterInactive.setBackgroundTintList(null);
                btnFilterInactive.setTextColor(getResources().getColor(R.color.black));
                break;
        }
    }
    
    private void applyFilter() {
        Log.d(TAG, "Applying filter: " + currentFilter + ", Total courts: " + allCourts.size());
        List<Court> filteredCourts = new ArrayList<>();
        
        for (Court court : allCourts) {
            boolean matchesStatus = currentFilter.equals("all")
                    || (currentFilter.equals("active") && "active".equalsIgnoreCase(court.getStatus()))
                    || (currentFilter.equals("inactive") && !"active".equalsIgnoreCase(court.getStatus()));

            if (!matchesStatus) {
                continue;
            }

            if (searchQuery.isEmpty()) {
                filteredCourts.add(court);
                continue;
            }

            String name = court.getCourtName() != null ? court.getCourtName() : "";
            String location = court.getLocation() != null ? court.getLocation() : "";
            String queryLower = searchQuery.toLowerCase();

            if (name.toLowerCase().contains(queryLower) || location.toLowerCase().contains(queryLower)) {
                filteredCourts.add(court);
            }
        }
        
        Log.d(TAG, "Filtered courts count: " + filteredCourts.size());
        adapter.setCourts(filteredCourts);
        
        if (filteredCourts.isEmpty()) {
            Log.d(TAG, "Showing empty state");
            showEmptyState();
        } else {
            Log.d(TAG, "Showing RecyclerView with courts");
            recyclerViewCourts.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload courts when fragment resumes (e.g., after adding a new court)
        loadVendorCourts();
    }
    
    private void loadVendorCourts() {
        Log.d(TAG, "Loading vendor courts...");
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewCourts.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        
        String token = "Bearer " + tokenManager.getToken();
        Log.d(TAG, "Token: " + (token != null ? "Present" : "Null"));
        
        RetrofitClient.getInstance().getApiService().viewVendorCourts(token)
                .enqueue(new Callback<ApiResponse<CourtsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CourtsResponse>> call, Response<ApiResponse<CourtsResponse>> response) {
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Response code: " + response.code());
                        
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<CourtsResponse> apiResponse = response.body();
                            Log.d(TAG, "Status: " + apiResponse.getStatus());
                            Log.d(TAG, "Message: " + apiResponse.getMessage());
                            
                            // Log raw response for debugging
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                String json = gson.toJson(apiResponse);
                                Log.d(TAG, "Full JSON response: " + json);
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting response to JSON", e);
                            }
                            
                            if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                                CourtsResponse courtsResponse = apiResponse.getData();
                                Log.d(TAG, "CourtsResponse object: " + (courtsResponse != null ? "Present" : "Null"));
                                
                                List<Court> courts = null;
                                if (courtsResponse != null) {
                                    courts = courtsResponse.getCourts();
                                    Log.d(TAG, "Courts from getCourts(): " + (courts != null ? courts.size() + " courts" : "null"));
                                    
                                    // Also try getData() in case it's different
                                    if (courts == null) {
                                        courts = courtsResponse.getData();
                                        Log.d(TAG, "Courts from getData(): " + (courts != null ? courts.size() + " courts" : "null"));
                                    }
                                }
                                
                                if (courts != null && !courts.isEmpty()) {
                                    allCourts = new ArrayList<>(courts);
                                    Log.d(TAG, "Successfully loaded " + allCourts.size() + " courts");
                                    
                                    // Log first court details for verification
                                    if (allCourts.size() > 0) {
                                        Court firstCourt = allCourts.get(0);
                                        Log.d(TAG, "First court: " + firstCourt.getCourtName() + 
                                              ", Status: " + firstCourt.getStatus() +
                                              ", Price: " + firstCourt.getPrice());
                                    }
                                    
                                    applyFilter();
                                } else {
                                    Log.d(TAG, "No courts found or courts list is empty");
                                    allCourts.clear();
                                    showEmptyState();
                                }
                            } else {
                                Log.e(TAG, "API returned non-success status");
                                Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                showEmptyState();
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                Log.e(TAG, "Error response: " + errorBody);
                                Toast.makeText(requireContext(), "Failed to load courts: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CourtsResponse>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Network error", t);
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                });
    }
    
    private void showEmptyState() {
        recyclerViewCourts.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
    
    private void showDeleteConfirmation(Court court) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Court")
                .setMessage("Are you sure you want to delete " + court.getCourtName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteCourt(court);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void deleteCourt(Court court) {
        Log.d(TAG, "Deleting court: " + court.getCourtName() + " (ID: " + court.getId() + ")");
        progressBar.setVisibility(View.VISIBLE);
        
        String token = "Bearer " + tokenManager.getToken();
        
        RetrofitClient.getInstance().getApiService().deleteCourt(token, court.getId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();
                            if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                                Toast.makeText(requireContext(), "Court deleted successfully", Toast.LENGTH_SHORT).show();
                                
                                // Remove from local list
                                allCourts.remove(court);
                                
                                // Reapply filter to update UI
                                applyFilter();
                            } else {
                                Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                Log.e(TAG, "Delete failed: " + response.code() + " - " + errorBody);
                                Toast.makeText(requireContext(), "Failed to delete: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                                Toast.makeText(requireContext(), "Failed to delete court", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Network error during delete", t);
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

