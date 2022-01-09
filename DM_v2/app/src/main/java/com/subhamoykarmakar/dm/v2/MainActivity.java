package com.subhamoykarmakar.dm.v2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
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

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.subhamoykarmakar.dm.v2.services.ForegroundService;
import com.subhamoykarmakar.dm.v2.sp.SPUpdateLocationController;
import com.subhamoykarmakar.dm.v2.utils.Constants;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static String LOG_MAINACTIVITY = "LOG::MainActivity";
    private static int REQUEST_CODE = 11023;


    public static final int PERMISSIONS_FINE_LOCATION = 99;

    private Button btnStartStop;
    private TextView textViewLatLong, textViewSensorUsedStatus, textViewAccuracy, textViewApproxAddress;
    private SwitchCompat switchUsePowerSaver;

    // Shared Preference Data Access
    SPUpdateLocationController spUpdateLocationController;

    // Bluetooth Service
    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();

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

        // Foreground Service Broadcast Init
        foregroundServiceBroadcastInit();

        // Switch Listener
        // TODO :: Add location request priority control to Foreground service
        switchUsePowerSaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchUsePowerSaver.isChecked()) {
//                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    textViewSensorUsedStatus.setText("Sensor: Using High Accuracy GPS.");
                } else {
//                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    textViewSensorUsedStatus.setText("Sensor: Using Towers + WiFi.");
                }
            }
        });

        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmpS = String.valueOf(btnStartStop.getText());
                if (tmpS.equalsIgnoreCase("start")) {
                    // Turn On service
                    startUpdate();
                } else if (tmpS.equalsIgnoreCase("stop")) {
                    // Turn Off service
                    stopUpdate();
                }
            }
        });
    }

    private void foregroundServiceBroadcastInit() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String lat = intent.getStringExtra(ForegroundService.EXTRA_LATITUDE);
                        String lng = intent.getStringExtra(ForegroundService.EXTRA_LONGITUDE);
                        String accuracy = intent.getStringExtra(ForegroundService.EXTRA_ACCURACY);
                        updateUIValues(lat, lng, accuracy);
                    }
                }, new IntentFilter(ForegroundService.ACTION_LOCATION_BROADCAST)
        );
    }

    private void startUpdate() {
        btnStartStop.setText("Stop");
        startForegroundService();
    }

    private void stopUpdate() {
        btnStartStop.setText("Start");
        textViewLatLong.setText("-, -");
        textViewAccuracy.setText("Accuracy: -na-");
        textViewApproxAddress.setText("Address: -na-");
        stopForegroundService();
    }

    private void updateUIValues(String lat, String lng, String accuracy) {
        // Update all of the text view objects with a new location
        textViewLatLong.setText("[" + lat + ", " + lng + "]");
        textViewAccuracy.setText(accuracy);


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
                            stopUpdate();
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
        stopUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdate();
        unregisterReceiver(mBroadcaseReceiver2Discoverability);
    }
}