package com.example.futsalmate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.adapters.NotificationsAdapter;
import com.example.futsalmate.models.NotificationItem;
import com.example.futsalmate.utils.NotificationStore;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifications;
    private NotificationStore notificationStore;
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationStore = new NotificationStore(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnClearAll = findViewById(R.id.btnClearAll);
        TextView btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmptyNotifications = findViewById(R.id.tvEmptyNotifications);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> confirmClearAll());
        }

        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> {
                notificationStore.markAllAsRead();
                loadNotifications();
            });
        }

        adapter = new NotificationsAdapter(false); // false = user theme
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        List<NotificationItem> notifications = notificationStore.getAll();
        adapter.setNotifications(notifications);

        if (tvEmptyNotifications != null) {
            tvEmptyNotifications.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void confirmClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Notifications")
                .setMessage("Are you sure you want to delete all notifications?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    notificationStore.clearAll();
                    loadNotifications();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
