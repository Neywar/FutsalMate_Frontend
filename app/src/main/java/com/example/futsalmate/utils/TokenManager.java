package com.example.futsalmate.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREFS_NAME = "FutsalMatePrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_TEAM_REGISTERED = "team_registered";
    
    public static final String ROLE_PLAYER = "PLAYER";
    public static final String ROLE_VENDOR = "VENDOR";
    
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

    public void saveUserRole(String role) {
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, ROLE_PLAYER);
    }

    public void setTeamRegistered(boolean registered) {
        editor.putBoolean(KEY_TEAM_REGISTERED, registered);
        editor.apply();
    }

    public boolean isTeamRegistered() {
        return sharedPreferences.getBoolean(KEY_TEAM_REGISTERED, false);
    }
    
    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_TEAM_REGISTERED);
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
