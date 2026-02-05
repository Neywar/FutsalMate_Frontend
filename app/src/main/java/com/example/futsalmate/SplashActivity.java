package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.utils.TokenManager;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_splash);
            
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkLoginStatusAndNavigate();
                }
            }, SPLASH_TIME_OUT);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            checkLoginStatusAndNavigate();
        }
    }
    
    private void checkLoginStatusAndNavigate() {
        TokenManager tokenManager = new TokenManager(this);
        Intent intent;
        
        if (tokenManager.isLoggedIn()) {
            String role = tokenManager.getUserRole();
            Log.d(TAG, "User logged in as " + role);
            
            if (TokenManager.ROLE_VENDOR.equals(role)) {
                intent = new Intent(SplashActivity.this, VendorMainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }
        } else {
            Log.d(TAG, "User not logged in, navigating to Login");
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish();
    }
}
