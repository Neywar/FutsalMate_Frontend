package com.example.futsalmate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private View navHome, navBookings, navCommunity, navProfile;
    private ImageView ivHome, ivBookings, ivCommunity, ivProfile;
    private TextView tvHome, tvBookings, tvCommunity, tvProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();

        // Handle navigation from other activities
        String targetFragment = getIntent().getStringExtra("TARGET_FRAGMENT");
        if (savedInstanceState == null) {
            if ("BOOKINGS".equals(targetFragment)) {
                loadFragment(new BookingsFragment(), R.id.nav_bookings);
            } else if ("COMMUNITY".equals(targetFragment)) {
                loadFragment(new CommunityFragment(), R.id.nav_community);
            } else if ("PROFILE".equals(targetFragment)) {
                loadFragment(new ProfileFragment(), R.id.nav_profile);
            } else {
                loadFragment(new DashboardFragment(), R.id.nav_home);
            }
        }
    }

    private void initViews() {
        navHome = findViewById(R.id.nav_home);
        navBookings = findViewById(R.id.nav_bookings);
        navCommunity = findViewById(R.id.nav_community);
        navProfile = findViewById(R.id.nav_profile);

        ivHome = findViewById(R.id.iv_nav_home);
        ivBookings = findViewById(R.id.iv_nav_bookings);
        ivCommunity = findViewById(R.id.iv_nav_community);
        ivProfile = findViewById(R.id.iv_nav_profile);

        tvHome = findViewById(R.id.tv_nav_home);
        tvBookings = findViewById(R.id.tv_nav_bookings);
        tvCommunity = findViewById(R.id.tv_nav_community);
        tvProfile = findViewById(R.id.tv_nav_profile);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> loadFragment(new DashboardFragment(), R.id.nav_home));
        navBookings.setOnClickListener(v -> loadFragment(new BookingsFragment(), R.id.nav_bookings));
        navCommunity.setOnClickListener(v -> loadFragment(new CommunityFragment(), R.id.nav_community));
        navProfile.setOnClickListener(v -> loadFragment(new ProfileFragment(), R.id.nav_profile));
    }

    public void loadFragment(Fragment fragment, int navId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        updateNavUI(navId);
    }

    public void switchToBookings() {
        loadFragment(new BookingsFragment(), R.id.nav_bookings);
    }

    public void switchToProfile() {
        loadFragment(new ProfileFragment(), R.id.nav_profile);
    }

    private void updateNavUI(int selectedId) {
        int activeColor = getResources().getColor(R.color.bright_green);
        int inactiveColor = getResources().getColor(R.color.text_grey);

        resetNavUI(inactiveColor);

        if (selectedId == R.id.nav_home) {
            ivHome.setColorFilter(activeColor);
            tvHome.setTextColor(activeColor);
            tvHome.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (selectedId == R.id.nav_bookings) {
            ivBookings.setColorFilter(activeColor);
            tvBookings.setTextColor(activeColor);
            tvBookings.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (selectedId == R.id.nav_community) {
            ivCommunity.setColorFilter(activeColor);
            tvCommunity.setTextColor(activeColor);
            tvCommunity.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (selectedId == R.id.nav_profile) {
            ivProfile.setColorFilter(activeColor);
            tvProfile.setTextColor(activeColor);
            tvProfile.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void resetNavUI(int color) {
        ivHome.setColorFilter(color);
        ivBookings.setColorFilter(color);
        ivCommunity.setColorFilter(color);
        ivProfile.setColorFilter(color);

        tvHome.setTextColor(color);
        tvBookings.setTextColor(color);
        tvCommunity.setTextColor(color);
        tvProfile.setTextColor(color);

        tvHome.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvBookings.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvCommunity.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvProfile.setTypeface(null, android.graphics.Typeface.NORMAL);
    }
}
