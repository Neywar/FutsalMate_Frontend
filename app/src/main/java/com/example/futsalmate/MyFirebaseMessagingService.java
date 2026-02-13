package com.example.futsalmate;

import androidx.annotation.NonNull;

import com.example.futsalmate.utils.TokenManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        tokenManager.saveFcmToken(token);
        // We only save the token locally; backend update happens on login.
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if (title == null) {
            title = remoteMessage.getData().get("title");
        }
        if (body == null) {
            body = remoteMessage.getData().get("body");
        }

        if (title == null && body == null) {
            return;
        }

        NotificationUtils.showNotification(getApplicationContext(), title, body);
    }
}

