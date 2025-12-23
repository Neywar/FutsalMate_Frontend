package com.example.futsalmate;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SplashActivity onCreate started");
        
        try {
            Log.d(TAG, "Loading splash layout...");
            setContentView(R.layout.activity_splash);
            Log.d(TAG, "Splash layout loaded successfully");
            
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigateToLogin();
                }
            }, SPLASH_TIME_OUT);
        } catch (Exception e) {
            Log.e(TAG, "FATAL: Error in onCreate - " + e.getMessage(), e);
            e.printStackTrace();
            // Try to show error and navigate
            try {
                Toast.makeText(SplashActivity.this, "Error loading app: " + e.getMessage(), Toast.LENGTH_LONG).show();
                navigateToLogin();
            } catch (Exception ex) {
                Log.e(TAG, "FATAL: Cannot recover from error", ex);
                finish();
            }
        }
    }
    
    private void navigateToLogin() {
        try {
            Log.d(TAG, "Navigating to LoginActivity...");
            
            // Verify LoginActivity exists
            try {
                Class<?> loginClass = Class.forName("com.example.futsalmate.LoginActivity");
                Log.d(TAG, "LoginActivity class found");
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "LoginActivity class not found!", e);
                Toast.makeText(this, "LoginActivity not found. Check for compilation errors.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            Log.d(TAG, "Successfully navigated to LoginActivity");
        } catch (Exception e) {
            Log.e(TAG, "FATAL: Error navigating to LoginActivity - " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage() + ". Check Logcat for details.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
