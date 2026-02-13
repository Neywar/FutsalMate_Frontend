package com.example.futsalmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.AvailableCourt;
import com.example.futsalmate.api.models.AvailableCourtsResponse;
import com.example.futsalmate.api.models.CommunityTeam;
import com.example.futsalmate.api.models.EditTeamRequest;
import com.example.futsalmate.utils.TokenManager;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTeamActivity extends AppCompatActivity {

    private EditText etTeamName;
    private EditText etTeamPhone;
    private EditText etTeamBio;
    private TextView tvPreferredCourts;
    private ChipGroup cgPreferredDays;
    private MaterialButton btnSaveTeam;
    private TokenManager tokenManager;
    private String[] courtList;
    private boolean[] checkedCourts;
    private ArrayList<Integer> selectedCourts = new ArrayList<>();
    private ArrayList<AvailableCourt> availableCourts = new ArrayList<>();
    private int teamId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_team);

        tokenManager = new TokenManager(this);

        etTeamName = findViewById(R.id.etTeamName);
        etTeamPhone = findViewById(R.id.etTeamPhone);
        etTeamBio = findViewById(R.id.etTeamBio);
        tvPreferredCourts = findViewById(R.id.tvPreferredCourts);
        cgPreferredDays = findViewById(R.id.cgPreferredDays);
        btnSaveTeam = findViewById(R.id.btnSaveTeam);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        teamId = getIntent().getIntExtra("team_id", -1);
        bindExistingValues();

        loadAvailableCourts();

        if (tvPreferredCourts != null) {
            tvPreferredCourts.setOnClickListener(v -> openCourtsDialog());
        }

        if (btnSaveTeam != null) {
            btnSaveTeam.setOnClickListener(v -> submitTeamEdit());
        }
    }

    private void bindExistingValues() {
        String name = getIntent().getStringExtra("team_name");
        String description = getIntent().getStringExtra("team_description");
        String phone = getIntent().getStringExtra("team_phone");
        String courts = getIntent().getStringExtra("preferred_courts");
        String days = getIntent().getStringExtra("preferred_days");

        if (etTeamName != null && !TextUtils.isEmpty(name)) {
            etTeamName.setText(name);
        }
        if (etTeamBio != null && !TextUtils.isEmpty(description)) {
            etTeamBio.setText(description);
        }
        if (etTeamPhone != null && !TextUtils.isEmpty(phone)) {
            etTeamPhone.setText(phone);
        }
        if (tvPreferredCourts != null && !TextUtils.isEmpty(courts)) {
            tvPreferredCourts.setText(courts);
        }

        if (cgPreferredDays != null && !TextUtils.isEmpty(days)) {
            Set<String> selected = splitAndTrim(days);
            for (int i = 0; i < cgPreferredDays.getChildCount(); i++) {
                View child = cgPreferredDays.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    String label = chip.getText() != null ? chip.getText().toString().trim() : "";
                    if (selected.contains(label)) {
                        chip.setChecked(true);
                    }
                }
            }
        }
    }

    private void openCourtsDialog() {
        if (courtList == null) {
            Toast.makeText(this, "Loading courts...", Toast.LENGTH_SHORT).show();
            loadAvailableCourts();
            return;
        }
        if (courtList.length == 0) {
            Toast.makeText(this, "No courts available right now.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Preferred Courts");
        builder.setCancelable(false);

        builder.setMultiChoiceItems(courtList, checkedCourts, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!selectedCourts.contains(which)) {
                    selectedCourts.add(which);
                }
            } else {
                selectedCourts.remove(Integer.valueOf(which));
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            if (tvPreferredCourts == null) {
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < selectedCourts.size(); i++) {
                stringBuilder.append(courtList[selectedCourts.get(i)]);
                if (i != selectedCourts.size() - 1) {
                    stringBuilder.append(", ");
                }
            }
            tvPreferredCourts.setText(stringBuilder.toString());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void submitTeamEdit() {
        if (btnSaveTeam != null) {
            btnSaveTeam.setEnabled(false);
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            if (btnSaveTeam != null) {
                btnSaveTeam.setEnabled(true);
            }
            return;
        }
        if (teamId <= 0) {
            Toast.makeText(this, "Team not found.", Toast.LENGTH_SHORT).show();
            if (btnSaveTeam != null) {
                btnSaveTeam.setEnabled(true);
            }
            return;
        }

        String teamName = etTeamName != null ? etTeamName.getText().toString().trim() : "";
        String phone = etTeamPhone != null ? etTeamPhone.getText().toString().trim() : "";
        String description = etTeamBio != null ? etTeamBio.getText().toString().trim() : "";
        String preferredCourts = tvPreferredCourts != null ? tvPreferredCourts.getText().toString().trim() : "";

        if (teamName.isEmpty()) {
            Toast.makeText(this, "Team name is required.", Toast.LENGTH_SHORT).show();
            if (btnSaveTeam != null) {
                btnSaveTeam.setEnabled(true);
            }
            return;
        }
        if (preferredCourts.equalsIgnoreCase("Select preferred courts")) {
            preferredCourts = "";
        }

        ArrayList<String> preferredDays = new ArrayList<>();
        if (cgPreferredDays != null) {
            for (int i = 0; i < cgPreferredDays.getChildCount(); i++) {
                View child = cgPreferredDays.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.isChecked()) {
                        preferredDays.add(chip.getText().toString());
                    }
                }
            }
        }

        String preferredDaysValue = preferredDays.isEmpty() ? null : String.join(", ", preferredDays);

        EditTeamRequest request = new EditTeamRequest(
                teamName,
                preferredCourts,
                phone.isEmpty() ? null : phone,
                description.isEmpty() ? null : description,
                preferredDaysValue
        );

        RetrofitClient.getInstance().getApiService().editTeam(token, teamId, request)
                .enqueue(new Callback<ApiResponse<CommunityTeam>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CommunityTeam>> call, Response<ApiResponse<CommunityTeam>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(EditTeamActivity.this, extractErrorMessage(response), Toast.LENGTH_SHORT).show();
                            if (btnSaveTeam != null) {
                                btnSaveTeam.setEnabled(true);
                            }
                            return;
                        }
                        String message = response.body().getMessage();
                        Toast.makeText(EditTeamActivity.this, message != null ? message : "Team updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CommunityTeam>> call, Throwable t) {
                        Toast.makeText(EditTeamActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        if (btnSaveTeam != null) {
                            btnSaveTeam.setEnabled(true);
                        }
                    }
                });
    }

    private String extractErrorMessage(Response<?> response) {
        if (response == null) {
            return "Failed to update team";
        }
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                JSONObject json = new JSONObject(body);
                if (json.has("message")) {
                    return json.getString("message");
                }
            }
        } catch (Exception ignored) {
        }
        return "Failed to update team (" + response.code() + ")";
    }

    private void loadAvailableCourts() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            courtList = new String[0];
            return;
        }
        RetrofitClient.getInstance().getApiService().availableCourts(token)
                .enqueue(new Callback<AvailableCourtsResponse>() {
                    @Override
                    public void onResponse(Call<AvailableCourtsResponse> call, Response<AvailableCourtsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getCourts() == null) {
                            courtList = new String[0];
                            return;
                        }
                        availableCourts.clear();
                        availableCourts.addAll(response.body().getCourts());
                        courtList = new String[availableCourts.size()];
                        for (int i = 0; i < availableCourts.size(); i++) {
                            AvailableCourt court = availableCourts.get(i);
                            courtList[i] = court.getName() != null ? court.getName() : "Court";
                        }
                        checkedCourts = new boolean[courtList.length];
                        syncCourtSelectionFromText();
                    }

                    @Override
                    public void onFailure(Call<AvailableCourtsResponse> call, Throwable t) {
                        courtList = new String[0];
                    }
                });
    }

    private void syncCourtSelectionFromText() {
        if (tvPreferredCourts == null || courtList == null || checkedCourts == null) {
            return;
        }
        String value = tvPreferredCourts.getText() != null ? tvPreferredCourts.getText().toString() : "";
        if (value.trim().isEmpty() || value.equalsIgnoreCase("Select preferred courts")) {
            return;
        }
        Set<String> existing = splitAndTrim(value);
        selectedCourts.clear();
        for (int i = 0; i < courtList.length; i++) {
            String label = courtList[i] != null ? courtList[i].trim() : "";
            boolean isSelected = existing.contains(label);
            checkedCourts[i] = isSelected;
            if (isSelected) {
                selectedCourts.add(i);
            }
        }
    }

    private Set<String> splitAndTrim(String value) {
        Set<String> result = new HashSet<>();
        if (value == null) {
            return result;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            String trimmed = part != null ? part.trim() : "";
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
