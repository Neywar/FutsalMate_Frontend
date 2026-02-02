package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.futsalmate.utils.TokenManager;

public class VendorMainActivity extends AppCompatActivity {

    private View navDashboard, navBookings, navCourts, navUsers, navProfile;
    private ImageView ivDashboard, ivBookings, ivCourts, ivUsers, ivProfile;
    private TextView tvDashboard, tvBookings, tvCourts, tvUsers, tvProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_main);

        initViews();
        setupNavigation();

        // Set default fragment or target fragment from intent
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String target = intent.getStringExtra("TARGET_FRAGMENT");
            if ("COURTS".equals(target)) {
                loadFragment(new VendorCourtsFragment(), R.id.nav_vendor_courts);
            } else if ("BOOKINGS".equals(target)) {
                loadFragment(new VendorBookingsFragment(), R.id.nav_vendor_bookings);
            } else {
                switchToDashboard();
            }
        }
    }

    private void initViews() {
        navDashboard = findViewById(R.id.nav_vendor_dashboard);
        navBookings = findViewById(R.id.nav_vendor_bookings);
        navCourts = findViewById(R.id.nav_vendor_courts);
        navUsers = findViewById(R.id.nav_vendor_users);
        navProfile = findViewById(R.id.nav_vendor_profile);

        ivDashboard = findViewById(R.id.iv_nav_vendor_dashboard);
        ivBookings = findViewById(R.id.iv_nav_vendor_bookings);
        ivCourts = findViewById(R.id.iv_nav_vendor_courts);
        ivUsers = findViewById(R.id.iv_nav_vendor_users);
        ivProfile = findViewById(R.id.iv_nav_vendor_profile);

        tvDashboard = findViewById(R.id.tv_nav_vendor_dashboard);
        tvBookings = findViewById(R.id.tv_nav_vendor_bookings);
        tvCourts = findViewById(R.id.tv_nav_vendor_courts);
        tvUsers = findViewById(R.id.tv_nav_vendor_users);
        tvProfile = findViewById(R.id.tv_nav_vendor_profile);
    }

    private void setupNavigation() {
        if (navDashboard != null) navDashboard.setOnClickListener(v -> switchToDashboard());
        if (navBookings != null) navBookings.setOnClickListener(v -> loadFragment(new VendorBookingsFragment(), R.id.nav_vendor_bookings));
        if (navCourts != null) navCourts.setOnClickListener(v -> loadFragment(new VendorCourtsFragment(), R.id.nav_vendor_courts));
        if (navUsers != null) navUsers.setOnClickListener(v -> loadFragment(new VendorUsersFragment(), R.id.nav_vendor_users));
        if (navProfile != null) navProfile.setOnClickListener(v -> loadFragment(new VendorProfileFragment(), R.id.nav_vendor_profile));
    }

    private void loadFragment(Fragment fragment, int navId) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.vendor_fragment_container, fragment);
            transaction.commit();
            updateNavUI(navId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToBookings() {
        loadFragment(new VendorBookingsFragment(), R.id.nav_vendor_bookings);
    }

    public void switchToDashboard() {
        loadFragment(new VendorDashboardFragment(), R.id.nav_vendor_dashboard);
    }

    private void updateNavUI(int selectedId) {
        int activeColor = android.graphics.Color.parseColor("#FACC15"); // Gold
        int inactiveColor = getResources().getColor(R.color.text_grey);

        resetNavUI(inactiveColor);

        if (selectedId == R.id.nav_vendor_dashboard && ivDashboard != null) {
            ivDashboard.setColorFilter(activeColor);
            tvDashboard.setTextColor(activeColor);
            tvDashboard.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (selectedId == R.id.nav_vendor_bookings && ivBookings != null) {
            ivBookings.setColorFilter(activeColor);
            tvBookings.setTextColor(activeColor);
            tvBookings.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (selectedId == R.id.nav_vendor_courts && ivCourts != null) {
            ivCourts.setColorFilter(activeColor);
            tvCourts.setTextColor(activeColor);
            tvCourts.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (selectedId == R.id.nav_vendor_users && ivUsers != null) {
            ivUsers.setColorFilter(activeColor);
            tvUsers.setTextColor(activeColor);
            tvUsers.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (selectedId == R.id.nav_vendor_profile && ivProfile != null) {
            ivProfile.setColorFilter(activeColor);
            tvProfile.setTextColor(activeColor);
            tvProfile.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void resetNavUI(int color) {
        if (ivDashboard != null) ivDashboard.setColorFilter(color);
        if (ivBookings != null) ivBookings.setColorFilter(color);
        if (ivCourts != null) ivCourts.setColorFilter(color);
        if (ivUsers != null) ivUsers.setColorFilter(color);
        if (ivProfile != null) ivProfile.setColorFilter(color);

        if (tvDashboard != null) {
            tvDashboard.setTextColor(color);
            tvDashboard.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (tvBookings != null) {
            tvBookings.setTextColor(color);
            tvBookings.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (tvCourts != null) {
            tvCourts.setTextColor(color);
            tvCourts.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (tvUsers != null) {
            tvUsers.setTextColor(color);
            tvUsers.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (tvProfile != null) {
            tvProfile.setTextColor(color);
            tvProfile.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
    
    public void logout() {
        new TokenManager(this).clearToken();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
