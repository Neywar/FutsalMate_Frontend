package com.example.futsalmate.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.futsalmate.LoginActivity;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthHelper {
    
    /**
     * Logout the current user and redirect to LoginActivity
     * 
     * @param context The current context (usually Activity.this)
     * @param tokenManager TokenManager instance to clear saved tokens
     */
    public static void logout(Context context, TokenManager tokenManager) {
        String authToken = tokenManager.getAuthHeader();
        
        if (authToken == null) {
            // No token found, just clear and redirect
            tokenManager.clearToken();
            redirectToLogin(context);
            return;
        }
        
        // Make logout API call
        Call<ApiResponse<Void>> call = RetrofitClient.getInstance().getApiService().logout(authToken);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                // Clear token regardless of API response
                tokenManager.clearToken();
                
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, 
                            response.body().getMessage() != null ? response.body().getMessage() : "Logged out successfully",
                            Toast.LENGTH_SHORT).show();
                }
                
                redirectToLogin(context);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // Clear token even if API call fails
                tokenManager.clearToken();
                Toast.makeText(context, "Logged out locally", Toast.LENGTH_SHORT).show();
                redirectToLogin(context);
            }
        });
    }
    
    private static void redirectToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).finish();
        }
    }
}


