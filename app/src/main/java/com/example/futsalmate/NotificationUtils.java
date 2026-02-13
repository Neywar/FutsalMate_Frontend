package com.example.futsalmate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.futsalmate.models.NotificationItem;
import com.example.futsalmate.utils.NotificationStore;
import com.example.futsalmate.utils.TokenManager;

import java.util.UUID;

public class NotificationUtils {

    private static final String CHANNEL_ID = "futsalmate_booking_channel";

    public static void showNotification(Context context, String title, String body) {
        createChannelIfNeeded(context);

        // Save notification to local store
        NotificationStore store = new NotificationStore(context);
        NotificationItem item = new NotificationItem(
                UUID.randomUUID().toString(),
                title != null ? title : "Notification",
                body != null ? body : "",
                System.currentTimeMillis(),
                false
        );
        store.saveNotification(item);

        // Decide target activity based on logged-in role
        TokenManager tokenManager = new TokenManager(context);
        String role = tokenManager.getUserRole();

        Intent intent;
        if (TokenManager.ROLE_VENDOR.equalsIgnoreCase(role)) {
            // Vendor: open vendor main and jump to Bookings tab
            intent = new Intent(context, VendorMainActivity.class);
            intent.putExtra("TARGET_FRAGMENT", "BOOKINGS");
        } else {
            // Player: open user main (dashboard or bookings as desired)
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("TARGET_FRAGMENT", "BOOKINGS");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_futsal_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for bookings and vendor updates");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}

