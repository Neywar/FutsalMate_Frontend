package com.example.futsalmate.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREFS_NAME = "FutsalMatePrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_EMAIL = "user_email";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    
    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
    
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    
    public void saveUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
    
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }
    
    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }
    
    public String getAuthHeader() {
        String token = getToken();
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return null;
    }
}


