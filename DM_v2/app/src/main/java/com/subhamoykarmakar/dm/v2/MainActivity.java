package com.subhamoykarmakar.dm.v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 5;
    public static final int FAST_UPDATE_INTERVAL = 1;
    public static final int PERMISSIONS_FINE_LOCATION = 99;

    private Button btnStartStop;
    private TextView textViewLatLong, textViewSensorUsedStatus, textViewAccuracy, textViewApproxAddress;
    private SwitchCompat switchUsePowerSaver;

    // Location Service
    FusedLocationProviderClient fusedLocationProviderClient;

    // Location Request
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartStop = findViewById(R.id.btnStartStop);
        textViewLatLong = findViewById(R.id.textViewLatLong);
        switchUsePowerSaver = findViewById(R.id.switchUsePowerSaver);
        textViewSensorUsedStatus = findViewById(R.id.textViewSensorUsedStatus);
        textViewAccuracy = findViewById(R.id.textViewAccuracy);
        textViewApproxAddress = findViewById(R.id.textViewApproxAddress);

        // Init Location services
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        // Event triggered whenever the update interval is met
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        if(fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        }

        // Switch Listener
        switchUsePowerSaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchUsePowerSaver.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    textViewSensorUsedStatus.setText("Sensor: Using High Accuracy GPS.");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    textViewSensorUsedStatus.setText("Sensor: Using Towers + WiFi.");
                }
            }
        });

        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmpS = String.valueOf(btnStartStop.getText());
                if (tmpS.equalsIgnoreCase("start")) {
                    // Turn On location tracking
                    startLocationUpdate();
                } else if (tmpS.equalsIgnoreCase("stop")) {
                    // Turn Off location tracking
                    stopLocationUpdate();

                }
            }
        });
    } // End of onCreate method

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        btnStartStop.setText("Stop");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdate() {
        btnStartStop.setText("Start");
        textViewLatLong.setText("-, -");
        textViewAccuracy.setText("Accuracy: -na-");
        textViewApproxAddress.setText("Address: -na-");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS() {
        // Get permissions from user to track GPS
        // Get the current location from the fused client
        // Update UI
        if(fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        }
        // PERMISSIONS
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // User provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // We get Location. Put the values of the location.
                    updateUIValues(location);
                }
            });
        } else {
            // Yet to get permission from the user
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String [] { Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {
        // Update all of the text view objects with a new location
        textViewLatLong.setText(
                "[" + location.getLatitude() + ", " + location.getLongitude() + "]"
        );
        textViewAccuracy.setText(String.valueOf(location.getAccuracy()));

        // Get Approx Address
//        getApproxAddress(location);
    }

    private void getApproxAddress(Location location) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            textViewApproxAddress.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e) {
            textViewApproxAddress.setText("-error-");
        }
    }
}