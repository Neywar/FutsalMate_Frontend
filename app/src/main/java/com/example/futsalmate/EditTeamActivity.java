package com.example.futsalmate;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class EditTeamActivity extends AppCompatActivity {

    private EditText etTeamName, etTeamPhone, etTeamBio, etPreferredCourts, etPreferredDays;
    private MaterialButton btnSaveTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_team);

        // Initialize views
        etTeamName = findViewById(R.id.etTeamName);
        etTeamPhone = findViewById(R.id.etTeamPhone);
        etTeamBio = findViewById(R.id.etTeamBio);
        etPreferredCourts = findViewById(R.id.etPreferredCourts);
        etPreferredDays = findViewById(R.id.etPreferredDays);
        btnSaveTeam = findViewById(R.id.btnSaveTeam);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSaveTeam.setOnClickListener(v -> {
            String name = etTeamName.getText().toString().trim();
            if (name.isEmpty()) {
                etTeamName.setError("Team name is required");
                return;
            }
            
            // Logic to save team changes would go here
            Toast.makeText(this, "Team updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
