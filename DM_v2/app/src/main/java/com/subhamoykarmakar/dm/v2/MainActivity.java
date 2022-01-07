package com.subhamoykarmakar.dm.v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.subhamoykarmakar.dm.v2.services.ForegroundService;
import com.subhamoykarmakar.dm.v2.sp.SPUpdateLocationController;
import com.subhamoykarmakar.dm.v2.utils.Constants;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static String LOG_MAINACTIVITY = "LOG::MainActivity";
    private static int REQUEST_CODE = 11023;

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

    // Shared Preference Data Access
    SPUpdateLocationController spUpdateLocationController;

    // Bluetooth Service
    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();

        initLocationListeners();

        bluetoothModulesBroadcastRecevierInit();

    } // End of onCreate method

    private void initWidgets() {
        btnStartStop = findViewById(R.id.btnStartStop);
        textViewLatLong = findViewById(R.id.textViewLatLong);
        switchUsePowerSaver = findViewById(R.id.switchUsePowerSaver);
        textViewSensorUsedStatus = findViewById(R.id.textViewSensorUsedStatus);
        textViewAccuracy = findViewById(R.id.textViewAccuracy);
        textViewApproxAddress = findViewById(R.id.textViewApproxAddress);

        spUpdateLocationController = new SPUpdateLocationController(this);
    }

    /**
     * LOCATION MODULE
     */

    private void initLocationListeners() {
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

                // Update SharedPreference data
                spUpdateLocationController.updateLatLong(
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude())
                );

                // Update UI
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
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        btnStartStop.setText("Stop");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        startForegroundService();
    }

    private void stopLocationUpdate() {
        btnStartStop.setText("Start");
        textViewLatLong.setText("-, -");
        textViewAccuracy.setText("Accuracy: -na-");
        textViewApproxAddress.setText("Address: -na-");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        stopForegroundService();
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

    /**
     * BLUETOOTH MODULE
     */
    // Create a BroadcastReceiver for discoverability mode on/off or expire.
    private final BroadcastReceiver mBroadcaseReceiver2Discoverability = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, bluetoothAdapter.ERROR);
                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        if (isMyServiceRunning(ForegroundService.class)) {
                            Intent intentService = new Intent(context, ForegroundService.class);
                            stopService(intentService);
                            stopLocationUpdate();
                        }
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage("You will need Bluetooth to use this app. Please switch on bluetooth to continue.");
                        alertDialog.show();
                        break;
                }
            }
        }
    };

    private void bluetoothModulesBroadcastRecevierInit() {
        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcaseReceiver2Discoverability, intentFilter);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            bluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

    private void startForegroundService() {
        Intent intentService = new Intent(MainActivity.this, ForegroundService.class);
        ContextCompat.startForegroundService(MainActivity.this, intentService);
    }

    private void stopForegroundService() {
        if (isMyServiceRunning(ForegroundService.class)) {
            Intent intentService = new Intent(this, ForegroundService.class);
            stopService(intentService);
        }
    }


    /**
     * Checks if the service is still running in the background
     *
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks Runtime permission for the application
     */
    private void checkRuntimePermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, Constants.PERMISSIONS, REQUEST_CODE);
        for (String p : Constants.PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, p) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{p},
                        REQUEST_CODE);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdate();
        unregisterReceiver(mBroadcaseReceiver2Discoverability);
    }
}