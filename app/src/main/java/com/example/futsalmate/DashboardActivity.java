package com.example.futsalmate; // <-- IMPORTANT: Use your actual package name here

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DashboardActivity extends AppCompatActivity {

    // Declare UI components
    private BottomNavigationView bottomNav;
    private ImageView btnNotifications;
    // You can declare other views like TextViews, CardViews etc. if you need to interact with them

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Views
        initializeViews();

        // Setup Listeners
        setupClickListeners();

        // Handle Bottom Navigation
        setupBottomNavigation();
    }

    private void initializeViews() {
        // Find views by their ID from the XML layout
        bottomNav = findViewById(R.id.bottomNav);
        btnNotifications = findViewById(R.id.btnNotifications);

        // You can find other views here if you need to change them dynamically
        // For example:
        // TextView txtUsername = findViewById(R.id.txtUsername);
        // txtUsername.setText("Faisal"); // Example of changing text
    }

    private void setupClickListeners() {
        // Set a listener for the notifications button
        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle notifications click, e.g., open a new activity
                Toast.makeText(DashboardActivity.this, "Notifications Clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        // You can add listeners for other elements like "See All" or "Book New Court"
        findViewById(R.id.txtSeeAll).setOnClickListener(v -> {
            Toast.makeText(DashboardActivity.this, "See All Clicked!", Toast.LENGTH_SHORT).show();
            // Intent to see all recent courts
        });

        findViewById(R.id.btnBookCourt).setOnClickListener(v -> {
            Toast.makeText(DashboardActivity.this, "Book New Court Clicked!", Toast.LENGTH_SHORT).show();
            // Intent to booking activity
        });
    }

    private void setupBottomNavigation() {
        // Set the 'Home' item as selected initially
        bottomNav.setSelectedItemId(R.id.nav_home);

        // Set listener for item selection
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // You are already on the Home screen
                    return true;
                } else if (itemId == R.id.nav_bookings) {
                    // Navigate to Bookings Activity
                    Toast.makeText(DashboardActivity.this, "Bookings", Toast.LENGTH_SHORT).show();
                    // startActivity(new Intent(getApplicationContext(), BookingsActivity.class));
                    // overridePendingTransition(0, 0); // Optional: for smooth transition
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Navigate to Profile Activity
                    Toast.makeText(DashboardActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                    // startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    // overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_courts) {
                    // Navigate to Settings Activity
                    Toast.makeText(DashboardActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                    // startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    // overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });
    }
}
