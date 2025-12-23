package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.OtpVerifyRequest;
import com.example.futsalmate.api.models.ResendOtpRequest;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// IMPORTANT: Change this to extend AppCompatActivity
public class OtpVerificationActivity extends AppCompatActivity {

    private EditText[] otpBoxes;
    private TextView txtTimer;
    private LinearLayout resendContainer;
    private MaterialButton btnVerify;
    private ImageView imgBack;
    private ProgressBar progressBar;
    private String userEmail;

    private static final long COUNTDOWN_IN_MILLIS = 30000; // 30 seconds
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This line tells the activity to use your XML layout file
        setContentView(R.layout.activity_otp_verification);

//        // Get email from intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Email not found. Please sign up again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        initializeViews();
        setupClickListeners();
        setupOtpBoxListeners();
        startTimer();
    }

    private void initializeViews() {
        imgBack = findViewById(R.id.imgBack);
        btnVerify = findViewById(R.id.btnVerify);
        txtTimer = findViewById(R.id.txtTimer);
        resendContainer = findViewById(R.id.resendContainer);
        progressBar = findViewById(R.id.progressBar); // Add ProgressBar to your layout if not present
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        otpBoxes = new EditText[]{
                findViewById(R.id.otpBox1),
                findViewById(R.id.otpBox2),
                findViewById(R.id.otpBox3),
                findViewById(R.id.otpBox4),
                findViewById(R.id.otpBox5),
                findViewById(R.id.otpBox6)
        };
    }

    private void setupClickListeners() {
        imgBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            StringBuilder otpBuilder = new StringBuilder();
            for (EditText box : otpBoxes) {
                otpBuilder.append(box.getText().toString());
            }

            if (otpBuilder.length() == 6) {
                verifyOtp(otpBuilder.toString());
            } else {
                Toast.makeText(this, "Please enter the complete 6-digit code", Toast.LENGTH_SHORT).show();
            }
        });

        resendContainer.setOnClickListener(v -> {
            if (!isTimerRunning) {
                resendOtp();
            }
        });
    }

    private void setupOtpBoxListeners() {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int currentIndex = i;

            otpBoxes[currentIndex].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpBoxes.length - 1) {
                        otpBoxes[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpBoxes[currentIndex].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpBoxes[currentIndex].getText().toString().isEmpty() && currentIndex > 0) {
                        otpBoxes[currentIndex - 1].requestFocus();
                        otpBoxes[currentIndex - 1].setText("");
                    }
                }
                return false;
            });
        }
    }

    private void startTimer() {
        isTimerRunning = true;
        resendContainer.setClickable(false);
        TextView resendText = (TextView) resendContainer.getChildAt(0);
        resendText.setText("Resend code in ");
        txtTimer.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(COUNTDOWN_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txtTimer.setText(String.format(Locale.getDefault(), "00:%02d", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                resendContainer.setClickable(true);
                txtTimer.setVisibility(View.GONE);
                resendText.setText("Resend code");
            }
        }.start();
    }

    private void verifyOtp(String otp) {
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Email not found. Please sign up again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Disable button and show progress
        btnVerify.setEnabled(false);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Create OTP verify request
        OtpVerifyRequest otpRequest = new OtpVerifyRequest(userEmail, otp);

        android.util.Log.d("OtpVerificationActivity", "Verifying OTP for email: " + userEmail);
        
        // Make API call
        Call<ApiResponse<Void>> call = RetrofitClient.getInstance().getApiService().verifyOtp(otpRequest);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                btnVerify.setEnabled(true);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                android.util.Log.d("OtpVerificationActivity", "OTP verification response code: " + response.code());
                android.util.Log.d("OtpVerificationActivity", "Response isSuccessful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    android.util.Log.d("OtpVerificationActivity", "Response status: " + apiResponse.getStatus());
                    android.util.Log.d("OtpVerificationActivity", "Response message: " + apiResponse.getMessage());
                    
                    if ("success".equals(apiResponse.getStatus())) {
                        Toast.makeText(OtpVerificationActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Verification Successful!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to Dashboard after successful OTP verification
                        android.util.Log.d("OtpVerificationActivity", "OTP verified successfully, navigating to Dashboard");
                        navigateToDashboard();
                    } else {
                        android.util.Log.w("OtpVerificationActivity", "OTP verification failed with status: " + apiResponse.getStatus());
                        Toast.makeText(OtpVerificationActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Invalid OTP. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    android.util.Log.e("OtpVerificationActivity", "OTP verification failed. Code: " + response.code());
                    if (response.code() == 403 || response.code() == 404) {
                        String errorMsg = "Invalid or expired OTP.";
                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        android.util.Log.e("OtpVerificationActivity", "Error: " + errorMsg);
                        Toast.makeText(OtpVerificationActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        // Try to parse error body
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            android.util.Log.e("OtpVerificationActivity", "Error body: " + errorBody);
                            Toast.makeText(OtpVerificationActivity.this, "Verification failed: " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            android.util.Log.e("OtpVerificationActivity", "Error parsing error body", e);
                            Toast.makeText(OtpVerificationActivity.this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                btnVerify.setEnabled(true);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                android.util.Log.e("OtpVerificationActivity", "Network error during OTP verification", t);
                Toast.makeText(OtpVerificationActivity.this,
                        "Network error: " + t.getMessage() + ". Check if backend is running.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void navigateToDashboard() {
        try {
            Intent intent = new Intent(OtpVerificationActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            android.util.Log.e("OtpVerificationActivity", "Error navigating to Dashboard", e);
            Toast.makeText(this, "Error opening dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void resendOtp() {
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Email not found. Please sign up again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Disable resend button
        resendContainer.setClickable(false);

        // Create resend OTP request
        ResendOtpRequest resendRequest = new ResendOtpRequest(userEmail, false);

        // Make API call
        Call<ApiResponse<Void>> call = RetrofitClient.getInstance().getApiService().resendOtp(resendRequest);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        Toast.makeText(OtpVerificationActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "OTP sent successfully.",
                                Toast.LENGTH_SHORT).show();
                        startTimer();
                    } else {
                        Toast.makeText(OtpVerificationActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to resend OTP.",
                                Toast.LENGTH_SHORT).show();
                        resendContainer.setClickable(true);
                    }
                } else {
                    // Handle error response
                    if (response.code() == 429) {
                        Toast.makeText(OtpVerificationActivity.this, "Too many attempts. Please try again later.", Toast.LENGTH_SHORT).show();
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        Toast.makeText(OtpVerificationActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OtpVerificationActivity.this, "Failed to resend OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                    resendContainer.setClickable(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                resendContainer.setClickable(true);
                Toast.makeText(OtpVerificationActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
