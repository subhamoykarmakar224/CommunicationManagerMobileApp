package com.subhamoykarmakar.dm.v2.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.subhamoykarmakar.dm.v2.MainActivity;
import com.subhamoykarmakar.dm.v2.sp.SPUpdateLocationController;
import com.subhamoykarmakar.dm.v2.utils.Constants;

import java.util.concurrent.Executor;

public class ForegroundService extends Service {
    private static String LOG_FOREGROUND = "LOG::ForegroundService";

    public static final String
            ACTION_LOCATION_BROADCAST = ForegroundService.class.getName() + "LocationBroadcast",
            EXTRA_LATITUDE = "extra_latitude",
            EXTRA_LONGITUDE = "extra_longitude",
            EXTRA_ACCURACY = "extra_accuracy";

    // Shared Preference Data Access
    SPUpdateLocationController spUpdateLocationController;
    Context context;

    // Location Request
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    // Location Service
    FusedLocationProviderClient fusedLocationProviderClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        spUpdateLocationController = new SPUpdateLocationController(this);
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerNotification();

        initLocationListeners();

        startLocationService();

        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void startLocationService() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            Log.i(LOG_FOREGROUND, "LOCATION :: " + location.getLatitude() + " , " + location.getLongitude());
                            sendBroadcastMessage(location);
                        } else {
                            Log.i(LOG_FOREGROUND, "Failed to get location.");
                        }
                    }
                });
    }

    private void sendBroadcastMessage(Location location) {
        if (location != null) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_LATITUDE, location.getLatitude());
            intent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
            intent.putExtra(EXTRA_ACCURACY, location.getAccuracy());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    /**
     * LOCATION MODULE
     */
    private void initLocationListeners() {
        // Init Location services
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * Constants.DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * Constants.FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Event triggered whenever the update interval is met
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Save the location
                Location location = locationResult.getLastLocation();

                // Update SharedPreference data
                spUpdateLocationController.updateLatLong(
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude())
                );

                // Update UI
                sendBroadcastMessage(location);
            }
        };

        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }
    }

    /**
     * Register notification for the Foreground service
     */
    private void registerNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0
        );
        Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Communication Manager")
                .setContentText("Started...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

//    private void getApproxAddress(Location location) {
//        Geocoder geocoder = new Geocoder(MainActivity.this);
//        try{
//            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//            textViewApproxAddress.setText(addresses.get(0).getAddressLine(0));
//        } catch (Exception e) {
//            textViewApproxAddress.setText("-error-");
//        }
//    }
}
