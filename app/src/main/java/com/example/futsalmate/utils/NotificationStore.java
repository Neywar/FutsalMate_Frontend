package com.example.futsalmate.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.futsalmate.models.NotificationItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificationStore {
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications";

    private final SharedPreferences prefs;
    private final Gson gson;

    public NotificationStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveNotification(NotificationItem item) {
        List<NotificationItem> notifications = getAll();
        notifications.add(0, item); // newest first
        saveAll(notifications);
    }

    public List<NotificationItem> getAll() {
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<NotificationItem>>() {}.getType();
        List<NotificationItem> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public void saveAll(List<NotificationItem> notifications) {
        String json = gson.toJson(notifications);
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply();
    }

    public void markAllAsRead() {
        List<NotificationItem> notifications = getAll();
        for (NotificationItem item : notifications) {
            item.setRead(true);
        }
        saveAll(notifications);
    }

    public void clearAll() {
        prefs.edit().remove(KEY_NOTIFICATIONS).apply();
    }

    public int getUnreadCount() {
        List<NotificationItem> notifications = getAll();
        int count = 0;
        for (NotificationItem item : notifications) {
            if (!item.isRead()) count++;
        }
        return count;
    }
}
