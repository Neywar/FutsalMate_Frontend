package com.example.futsalmate;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class RegisterTeamActivity extends AppCompatActivity {

    private TextView tvPreferredCourts;
    private String[] courtList;
    private boolean[] checkedCourts;
    private ArrayList<Integer> selectedCourts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_team);

        tvPreferredCourts = findViewById(R.id.tvPreferredCourts);
        courtList = getResources().getStringArray(R.array.courts_array);
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

            builder.setNeutralButton("Clear All", (dialog, which) -> {
                for (int i = 0; i < checkedCourts.length; i++) {
                    checkedCourts[i] = false;
                    selectedCourts.clear();
                    tvPreferredCourts.setText("Select preferred courts");
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
