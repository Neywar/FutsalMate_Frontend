package com.example.futsalmate.api;

import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.LoginRequest;
import com.example.futsalmate.api.models.OtpVerifyRequest;
import com.example.futsalmate.api.models.ResendOtpRequest;
import com.example.futsalmate.api.models.SignupRequest;
import com.example.futsalmate.api.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    
    @POST("signup")
    Call<ApiResponse<User>> signup(@Body SignupRequest request);
    
    @POST("login")
    Call<ApiResponse<User>> login(@Body LoginRequest request);
    
    @POST("logout")
    Call<ApiResponse<Void>> logout(@Header("Authorization") String token);
    
    @POST("email/verify/otp")
    Call<ApiResponse<Void>> verifyOtp(@Body OtpVerifyRequest request);
    
    @POST("email/verify/resend-otp")
    Call<ApiResponse<Void>> resendOtp(@Body ResendOtpRequest request);
}


