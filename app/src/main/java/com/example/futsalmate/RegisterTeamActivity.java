package com.example.futsalmate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterTeamActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_team);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
