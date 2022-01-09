package com.subhamoykarmakar.dm.v2.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.subhamoykarmakar.dm.v2.MainActivity;
import com.subhamoykarmakar.dm.v2.sp.SPUpdateLocationController;
import com.subhamoykarmakar.dm.v2.utils.Constants;

import java.util.List;

public class ForegroundService extends Service {
    private static String LOG_FOREGROUND = "LOG::ForegroundService";

    public static final String
            ACTION_LOCATION_BROADCAST = ForegroundService.class.getName() + "LocationBroadcast",
            EXTRA_LATITUDE = "extra_latitude",
            EXTRA_LONGITUDE = "extra_longitude",
            EXTRA_ACCURACY = "extra_accuracy",
            EXTRA_ADDRESS = "extra_address";

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        spUpdateLocationController = new SPUpdateLocationController(this);
        context = getApplicationContext();

        registerNotification();

        initLocationListeners();

        startLocationService();

        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void startLocationService() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
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

                Log.i(LOG_FOREGROUND, "LOCATION :: " + location.getLatitude() + " , " + location.getLongitude());

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
     * Send Location information as Broadcast message to MainActivity
     * @param location
     */
    private void sendBroadcastMessage(Location location) {
        if (location != null) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_LATITUDE, String.valueOf(location.getLatitude()));
            intent.putExtra(EXTRA_LONGITUDE, String.valueOf(location.getLongitude()));
            intent.putExtra(EXTRA_ACCURACY, String.valueOf(location.getAccuracy()));
//            intent.putExtra(EXTRA_ADDRESS, getApproxAddress(location));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

// Uncomment if you want address.
//    private String getApproxAddress(Location location) {
//        Geocoder geocoder = new Geocoder(context);
//        String address = "";
//        try{
//            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//            address = addresses.get(0).getAddressLine(0);
//        } catch (Exception e) {
//            address = "-error-";
//        }
//        return address;
//    }
}
