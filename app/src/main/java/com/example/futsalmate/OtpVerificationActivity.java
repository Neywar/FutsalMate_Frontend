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
import android.widget.TextView;
import android.widget.Toast;

// IMPORTANT: Change this import
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

// IMPORTANT: Change this to extend AppCompatActivity
public class OtpVerificationActivity extends AppCompatActivity {

    private EditText[] otpBoxes;
    private TextView txtTimer;
    private LinearLayout resendContainer;
    private MaterialButton btnVerify;
    private ImageView imgBack;

    private static final long COUNTDOWN_IN_MILLIS = 30000; // 30 seconds
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This line tells the activity to use your XML layout file
        setContentView(R.layout.activity_otp_verification);

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
        // --- TODO: Add your real OTP verification logic ---
        // For testing, let's assume "123456" is the correct OTP.
        Toast.makeText(this, "Verifying OTP: " + otp, Toast.LENGTH_SHORT).show();

        if (otp.equals("123456")) {
            Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(OtpVerificationActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resendOtp() {
        // --- TODO: Add your logic to request a new OTP from your server ---
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();
        startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
