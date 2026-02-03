package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class RegisterTeamActivity extends AppCompatActivity {

    private TextView tvPreferredCourts;
    private String[] courtList;
    private boolean[] checkedCourts;
    private ArrayList<Integer> selectedCourts = new ArrayList<>();
    private MaterialButton btnPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_team);

        tvPreferredCourts = findViewById(R.id.tvPreferredCourts);
        btnPublish = findViewById(R.id.btnPublish);
        
        // Use a fallback list if array resource is missing
        try {
            courtList = getResources().getStringArray(R.array.courts_array);
        } catch (Exception e) {
            courtList = new String[]{"Pro Arena", "West End Arena", "Stadium A", "Stadium B"};
        }
        
        checkedCourts = new boolean[courtList.length];

        tvPreferredCourts.setOnClickListener(v -> {
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

        btnPublish.setOnClickListener(v -> {
            // Simulation: Save team registration
            Toast.makeText(this, "Team Registered Successfully!", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TEAM_REGISTERED", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
