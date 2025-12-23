# Authentication Integration Guide

This document describes the Retrofit integration with Laravel backend for user authentication.

## Overview

The Android app is now integrated with the Laravel backend using Retrofit for the following authentication endpoints:
- User Signup
- User Login  
- OTP Verification
- Resend OTP
- Logout

## Configuration

### 1. Base URL Configuration

Update the `BASE_URL` in `RetrofitClient.java` based on your setup:

- **Android Emulator**: `http://10.0.2.2:8000/api/`
- **Physical Device**: `http://YOUR_COMPUTER_IP:8000/api/`
  - Find your IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
- **Production**: Your actual production API URL

**Location**: `app/src/main/java/com/example/futsalmate/api/RetrofitClient.java`

### 2. Dependencies

The following dependencies have been added to `build.gradle.kts`:
- Retrofit 2.9.0
- Gson Converter 2.9.0
- OkHttp 4.12.0
- OkHttp Logging Interceptor 4.12.0

## API Endpoints

All endpoints are defined in `ApiService.java`:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/signup` | POST | User registration |
| `/login` | POST | User login |
| `/logout` | POST | User logout (requires auth token) |
| `/email/verify/otp` | POST | Verify OTP |
| `/email/verify/resend-otp` | POST | Resend OTP |

## Usage Examples

### Signup

The `SignUpActivity` automatically handles signup:
1. User fills in registration form
2. API call is made to `/api/signup`
3. On success, user is redirected to `OtpVerificationActivity` with email passed as intent extra

### Login

The `LoginActivity` handles login:
1. User enters email and password
2. API call is made to `/api/login`
3. On success, auth token is saved using `TokenManager`
4. User is redirected to `DashboardActivity`

### OTP Verification

The `OtpVerificationActivity` handles OTP verification:
1. Receives email from signup intent
2. User enters 6-digit OTP
3. API call is made to `/api/email/verify/otp`
4. On success, user is redirected to `LoginActivity`

### Resend OTP

Users can resend OTP:
1. Timer countdown (30 seconds)
2. After timer expires, user can click "Resend code"
3. API call is made to `/api/email/verify/resend-otp`

### Logout

To implement logout in any activity:

```java
import com.example.futsalmate.utils.AuthHelper;
import com.example.futsalmate.utils.TokenManager;

// In your activity
TokenManager tokenManager = new TokenManager(this);
AuthHelper.logout(this, tokenManager);
```

## File Structure

```
app/src/main/java/com/example/futsalmate/
├── api/
│   ├── ApiService.java          # Retrofit interface defining API endpoints
│   ├── RetrofitClient.java      # Retrofit client singleton
│   └── models/
│       ├── ApiResponse.java     # Generic API response model
│       ├── User.java            # User model
│       ├── SignupRequest.java   # Signup request model
│       ├── LoginRequest.java    # Login request model
│       ├── OtpVerifyRequest.java # OTP verification request model
│       └── ResendOtpRequest.java # Resend OTP request model
├── utils/
│   ├── TokenManager.java        # Manages auth token storage
│   └── AuthHelper.java          # Utility methods for authentication
├── LoginActivity.java           # Updated with API integration
├── SignUpActivity.java          # Updated with API integration
└── OtpVerificationActivity.java # Updated with API integration
```

## Token Management

The `TokenManager` class handles:
- Saving authentication tokens
- Retrieving authentication tokens
- Saving user email
- Clearing tokens on logout
- Checking login status

Tokens are stored in SharedPreferences with the key `FutsalMatePrefs`.

## Error Handling

All API calls include proper error handling:
- Network errors
- Validation errors (422)
- Authentication errors (401)
- Email verification errors (403)
- Rate limiting (429)

Error messages are displayed to users via Toast notifications.

## Testing

1. Start your Laravel backend server
2. Update `BASE_URL` in `RetrofitClient.java`
3. Run the Android app
4. Test the authentication flow:
   - Signup → OTP Verification → Login → Dashboard

## Notes

- Progress bars are referenced in activities but may need to be added to your XML layouts
- All API responses follow the Laravel API response structure with `status`, `message`, and data fields
- Auth tokens are automatically included in logout requests via the Authorization header
- The app uses Bearer token authentication for protected endpoints


