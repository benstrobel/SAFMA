package com.strobel.healthaggregation.payload.datasources;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.strobel.healthaggregation.FirebaseMessagingService;
import com.strobel.healthaggregation.MainActivity;
import com.strobel.healthaggregation.R;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoogleFitDataSource {

    public static final FitnessOptions fitnessOptions = FitnessOptions
            .builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build();

    private static final DataSource googleFitStepDataSource = new DataSource
            .Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build();

    public static int getDailyStepCountForDateBlocking(Context context, LocalDate targetDay) throws Exception {
        return getDailyStepCountForDateRangeBlocking(context, targetDay, targetDay)[0];
    }

    public static int[] getDailyStepCountForDateRangeBlocking(Context context, LocalDate fromDay, LocalDate toDay) throws Exception {
        Object lockObject = new Object();
        final List<Integer> steps = new ArrayList<>();
        final Exception[] exception = new Exception[1];

        ZonedDateTime startTime = fromDay.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endTime = toDay.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault());

        DataReadRequest dataRequest = new DataReadRequest.Builder()
                .aggregate(googleFitStepDataSource)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(context, fitnessOptions);

        if(!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.e("GoogleSignIn", "The app doesn't have permission to access the google account, please open the app again.");

            createPermissionReminderNotification(context);
        }

        Fitness.getHistoryClient(context, account)
                .readData(dataRequest)
                .addOnSuccessListener(e -> {
                    e.getBuckets()
                            .stream()
                            .map(x -> x.getDataSets().stream().flatMap(y -> y.getDataPoints().stream()).reduce(0, (subtotal, element) -> subtotal + element.getValue(Field.FIELD_STEPS).asInt(), Integer::sum))
                            .mapToInt(i -> i)
                            .forEach(steps::add);
                    synchronized (lockObject) {
                        lockObject.notify();
                    }
                })
                .addOnFailureListener(e -> {
                    synchronized (lockObject) {
                        exception[0] = e;
                        lockObject.notify();
                    }
                });

        synchronized (lockObject) {
            try {
                if(steps.size() == 0) {
                    lockObject.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(exception[0] != null) {
            throw exception[0];
        }

        return steps.stream().mapToInt(i -> i).toArray();
    }

    private static void createPermissionReminderNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FirebaseMessagingService.PERMISSION_REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.data_collection)
                .setContentTitle("Permission expired")
                .setContentText("Permission to access google fit data has expired, please reopen the app")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat.from(context).notify(FirebaseMessagingService.notificationId++, builder.build());
    }
}
