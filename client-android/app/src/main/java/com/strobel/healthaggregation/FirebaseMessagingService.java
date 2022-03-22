package com.strobel.healthaggregation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    public static int notificationId = 0;
    private static final String TAG = "FirebaseMsgService";
    private static final String CHANNEL_ID = "firebaseMessageReceived";
    public static final String PERMISSION_REMINDER_CHANNEL_ID = "permissionReminder";

    @Override
    public void onCreate() {
        createNotificationChannels();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // Method isn't needed since topics are being used instead of single target communication
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom() + "'" + "'");

        if (remoteMessage.getData().size() > 0) {
            if(!remoteMessage.getData().containsKey("requested_at") ||
                    !remoteMessage.getData().containsKey("requested_at") ||
                    !remoteMessage.getData().containsKey("requested_at")) {
                Log.e(TAG, "Broken message received. Message content \"" + remoteMessage.getData() + "\"");
                return;
            }
            Log.d(TAG, "Valid request received, creating notification...");
            String requested_at = remoteMessage.getData().get("requested_at");
            String requested_url = remoteMessage.getData().get("requested_url");
            String request_id = remoteMessage.getData().get("request_id");
            int request_expected_dimensionality = Integer.parseInt(remoteMessage.getData().get("request_expected_dimensionality"));
            // createDebugNotification(requested_at, requested_url, request_id);
            Data data = new Data.Builder()
                    .putString("request_id", request_id)
                    .putString("requested_url", requested_url)
                    .putString("requested_at", requested_at)
                    .putInt("request_expected_dimensionality", request_expected_dimensionality)
                    .build();
            OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SecureAggregationWorker.class)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(this).beginWith(work).enqueue();
        }
    }

    private void createNotificationChannels() {
        CharSequence debug_name = getString(R.string.debug_channel_name);
        String debug_description = getString(R.string.debug_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel debug_channel = new NotificationChannel(CHANNEL_ID, debug_name, importance);
        debug_channel.setDescription(debug_description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        CharSequence permission_reminder_name = getString(R.string.permission_reminder_channel_name);
        String permission_reminder_description = getString(R.string.permission_reminder_channel_description);
        NotificationChannel permission_reminder_channel = new NotificationChannel(PERMISSION_REMINDER_CHANNEL_ID, permission_reminder_name, importance);
        permission_reminder_channel.setDescription(permission_reminder_description);

        notificationManager.createNotificationChannel(debug_channel);
        notificationManager.createNotificationChannel(permission_reminder_channel);
    }

    private void createDebugNotification(String requested_at, String requested_url, String request_id) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.data_collection)
                .setContentTitle("New data request recevied")
                .setContentText("Request ID: " + request_id + "\nRequested URL: \"" + requested_url + "\" \nRequested at: " + requested_at)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Request ID: " + request_id + "\nRequested URL: \"" + requested_url + "\" \nRequested at: " + requested_at))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat.from(this).notify(notificationId++, builder.build());
    }

}
