package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.AvailableCourt;
import com.example.futsalmate.api.models.AvailableCourtsResponse;
import com.example.futsalmate.api.models.CommunityTeam;
import com.example.futsalmate.api.models.RegisterTeamRequest;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterTeamActivity extends AppCompatActivity {

    private TextView tvPreferredCourts;
    private EditText etTeamName;
    private EditText etTeamPhone;
    private EditText etTeamDescription;
    private ChipGroup cgPreferredDays;
    private String[] courtList;
    private boolean[] checkedCourts;
    private ArrayList<Integer> selectedCourts = new ArrayList<>();
    private ArrayList<AvailableCourt> availableCourts = new ArrayList<>();
    private MaterialButton btnPublish;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_team);

        tokenManager = new TokenManager(this);
        tvPreferredCourts = findViewById(R.id.tvPreferredCourts);
        etTeamName = findViewById(R.id.etTeamName);
        etTeamPhone = findViewById(R.id.etTeamPhone);
        etTeamDescription = findViewById(R.id.etTeamDescription);
        cgPreferredDays = findViewById(R.id.cgPreferredDays);
        btnPublish = findViewById(R.id.btnPublish);
        
        loadAvailableCourts();

        tvPreferredCourts.setOnClickListener(v -> {
            if (courtList == null) {
                Toast.makeText(this, "Loading courts...", Toast.LENGTH_SHORT).show();
                loadAvailableCourts();
                return;
            }
            if (courtList.length == 0) {
                Toast.makeText(this, "No courts available right now.", Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterTeamActivity.this);
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
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < selectedCourts.size(); i++) {
                    stringBuilder.append(courtList[selectedCourts.get(i)]);
                    if (i != selectedCourts.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                tvPreferredCourts.setText(stringBuilder.toString());
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        btnPublish.setOnClickListener(v -> submitTeamRegistration());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void submitTeamRegistration() {
        if (btnPublish != null) {
            btnPublish.setEnabled(false);
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            if (btnPublish != null) {
                btnPublish.setEnabled(true);
            }
            return;
        }

        String teamName = etTeamName != null ? etTeamName.getText().toString().trim() : "";
        String phone = etTeamPhone != null ? etTeamPhone.getText().toString().trim() : "";
        String description = etTeamDescription != null ? etTeamDescription.getText().toString().trim() : "";
        String preferredCourts = tvPreferredCourts != null ? tvPreferredCourts.getText().toString().trim() : "";

        if (teamName.isEmpty()) {
            Toast.makeText(this, "Team name is required.", Toast.LENGTH_SHORT).show();
            if (btnPublish != null) {
                btnPublish.setEnabled(true);
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

        RegisterTeamRequest request = new RegisterTeamRequest(
                teamName,
                preferredCourts,
                phone.isEmpty() ? null : phone,
                description.isEmpty() ? null : description,
            preferredDaysValue
        );

        RetrofitClient.getInstance().getApiService().registerTeam(token, request)
                .enqueue(new retrofit2.Callback<ApiResponse<CommunityTeam>>() {
                    @Override
                    public void onResponse(retrofit2.Call<ApiResponse<CommunityTeam>> call, retrofit2.Response<ApiResponse<CommunityTeam>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(RegisterTeamActivity.this, extractErrorMessage(response), Toast.LENGTH_SHORT).show();
                            if (btnPublish != null) {
                                btnPublish.setEnabled(true);
                            }
                            return;
                        }
                        tokenManager.setTeamRegistered(true);
                        String message = response.body().getMessage();
                        Toast.makeText(RegisterTeamActivity.this, message != null ? message : "Team registered successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterTeamActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ApiResponse<CommunityTeam>> call, Throwable t) {
                        Toast.makeText(RegisterTeamActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        if (btnPublish != null) {
                            btnPublish.setEnabled(true);
                        }
                    }
                });
    }

    private String extractErrorMessage(retrofit2.Response<?> response) {
        if (response == null) {
            return "Failed to register team";
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
        return "Failed to register team (" + response.code() + ")";
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
                    }

                    @Override
                    public void onFailure(Call<AvailableCourtsResponse> call, Throwable t) {
                        courtList = new String[0];
                    }
                });
    }
}
