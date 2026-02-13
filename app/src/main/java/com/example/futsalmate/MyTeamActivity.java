package com.example.futsalmate;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;

import java.util.HashSet;
import java.util.Set;

public class MyTeamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_team);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        TextView tvTeamName = findViewById(R.id.team_name);
        TextView tvTeamDescription = findViewById(R.id.team_description);
        TextView tvTeamPhone = findViewById(R.id.tvTeamPhone);
        LinearLayout tvPreferredCourts = findViewById(R.id.tvPreferredCourtsValue);
        LinearLayout tvPreferredDays = findViewById(R.id.tvPreferredDaysValue);

        int teamId = getIntent().getIntExtra("team_id", -1);
        String name = getIntent().getStringExtra("team_name");
        String description = getIntent().getStringExtra("team_description");
        String phone = getIntent().getStringExtra("team_phone");
        String courts = getIntent().getStringExtra("preferred_courts");
        String days = getIntent().getStringExtra("preferred_days");

        if (tvTeamName != null) {
            tvTeamName.setText(TextUtils.isEmpty(name) ? "My Team" : name);
        }
        if (tvTeamDescription != null) {
            tvTeamDescription.setText(TextUtils.isEmpty(description) ? "" : description);
        }
        if (tvTeamPhone != null) {
            tvTeamPhone.setText(TextUtils.isEmpty(phone) ? "N/A" : phone);
        }
        if (tvPreferredCourts != null) {
            bindPreferredCourts(tvPreferredCourts, courts);
        }
        if (tvPreferredDays != null) {
            bindPreferredDays(tvPreferredDays, days);
        }

        // Check if this is the user's own team
        boolean isOwnTeam = getIntent().getBooleanExtra("is_own_team", false);
        
        View btnEdit = findViewById(R.id.edit_team_profile_button);
        View btnDelete = findViewById(R.id.delete_team_button);
        View scrollView = findViewById(R.id.scrollView);
        
        // Hide edit and delete buttons if viewing another user's team
        if (!isOwnTeam) {
            if (btnEdit != null) {
                btnEdit.setVisibility(View.GONE);
            }
            if (btnDelete != null) {
                btnDelete.setVisibility(View.GONE);
            }
            // Adjust scroll view to extend to bottom when buttons are hidden
            if (scrollView != null && scrollView.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) scrollView.getLayoutParams();
                params.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                scrollView.setLayoutParams(params);
            }
        } else {
            // Only show buttons for own team
            if (btnEdit != null) {
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EditTeamActivity.class);
                    intent.putExtra("team_id", teamId);
                    intent.putExtra("team_name", name);
                    intent.putExtra("team_description", description);
                    intent.putExtra("team_phone", phone);
                    intent.putExtra("preferred_courts", courts);
                    intent.putExtra("preferred_days", days);
                    startActivity(intent);
                });
            }
            if (btnDelete != null) {
                btnDelete.setVisibility(View.VISIBLE);
                // Delete functionality can be added here if needed
            }
        }
    }

    private void bindPreferredCourts(LinearLayout container, String courts) {
        container.removeAllViews();
        String value = courts != null ? courts.trim() : "";
        if (value.isEmpty()) {
            container.addView(createCourtChip("Any"));
            return;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            String label = part != null ? part.trim() : "";
            if (!label.isEmpty()) {
                container.addView(createCourtChip(label));
            }
        }
    }

    private Chip createCourtChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setClickable(false);
        chip.setCheckable(false);
        chip.setTextColor(Color.WHITE);
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#4E4E50")));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);
        return chip;
    }

    private void bindPreferredDays(LinearLayout container, String days) {
        String value = days != null ? days.trim() : "";
        Set<String> selected = splitAndTrim(value);
        if (selected.isEmpty()) {
            return;
        }
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (!(child instanceof TextView)) {
                continue;
            }
            TextView textView = (TextView) child;
            String label = textView.getText() != null ? textView.getText().toString().trim() : "";
            boolean isSelected = selected.contains(label) || selected.contains(expandDayLabel(label));
            textView.setBackgroundResource(isSelected ? R.drawable.bg_day_selected : 0);
        }
    }

    private Set<String> splitAndTrim(String value) {
        Set<String> result = new HashSet<>();
        if (value == null || value.trim().isEmpty()) {
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

    private String expandDayLabel(String value) {
        if (value == null) {
            return "";
        }
        switch (value.toUpperCase()) {
            case "SUN":
                return "Sunday";
            case "MON":
                return "Monday";
            case "TUE":
                return "Tuesday";
            case "WED":
                return "Wednesday";
            case "THU":
                return "Thursday";
            case "FRI":
                return "Friday";
            case "SAT":
                return "Saturday";
            default:
                return value;
        }
    }

    private int dpToPx(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}