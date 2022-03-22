package com.strobel.healthaggregation;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.strobel.healthaggregation.payload.datasources.GoogleFitDataSource;

public class MainActivity extends AppCompatActivity {

    public static final Gson GSON = new Gson();

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted.
                } else {
                    requestPermissionIfNeeded();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("data_requests");
        requestPermissionIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissionIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissionIfNeeded();
    }

    private void requestPermissionIfNeeded() {
        Activity activity = this;
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) ==
                PackageManager.PERMISSION_GRANTED) {

            // Checking/Requesting App Permission to access Google Fit Data of Google Account
            GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(activity.getApplicationContext(), GoogleFitDataSource.fitnessOptions);

            if(!GoogleSignIn.hasPermissions(account, GoogleFitDataSource.fitnessOptions)) {
                Log.i("GoogleSignIn", "Asking for permission");
                GoogleSignIn.requestPermissions(activity,
                        1,
                        account,
                        GoogleFitDataSource.fitnessOptions
                );
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissionLauncher.launch(
                        Manifest.permission.ACTIVITY_RECOGNITION);
            }
        }
    }
}