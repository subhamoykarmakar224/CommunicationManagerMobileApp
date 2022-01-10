package com.subhamoykarmakar.dm.v2.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
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
import com.subhamoykarmakar.dm.v2.bean.Packet;
import com.subhamoykarmakar.dm.v2.sp.SPUpdateLocationController;
import com.subhamoykarmakar.dm.v2.sp.SPDeviceIdController;
import com.subhamoykarmakar.dm.v2.utils.BLEParser;
import com.subhamoykarmakar.dm.v2.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    SPDeviceIdController spDeviceIdController;
    Context context;

    // Location Request
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    // Location Service
    FusedLocationProviderClient fusedLocationProviderClient;

    // Bluetooth Modules
    BluetoothAdapter bluetoothAdapter;

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
        // LOCATION
        spUpdateLocationController = new SPUpdateLocationController(this);
        spDeviceIdController = new SPDeviceIdController(this);
        context = getApplicationContext();

        registerNotification();

        initLocationListeners();

        startLocationService();

        // BLUETOOTH
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startBLEScan();

        return START_STICKY;
    }


    /*
     * BLE MODULES
     */

//        BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
//            @Override
//            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//                String [] lat_long = spUpdateLocationController.getLatLong();
//                String sensorId = spDeviceIdController.getDeviceId();
//                String deviceMAC = device.getAddress().replace(":", "").toLowerCase();
//                String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
//                int metaSeqNumber = Integer.parseInt(dt.substring(dt.indexOf(".") + 1));
//                // TODO :: Add this feature later
//                int deliveryType = 0, messageSemantics = 0;
//                (String sensorId, String rssi, String nativeMAC,
//                String longitude, String latitude, String id,
//                String advRecord, String name, String type,
//                String timestamp, String first_packet, String commitStatus,
//                int orderedDeliveryStatus, int msgSemantics, int metaSeqNumber)
//                Packet packet = new Packet(
//                        sensorId, String.valueOf(rssi), device.toString(),
//                        lat_long[1], lat_long[0], "00000000-0000-0000-0000-".concat(deviceMAC),
//                        packetToString(scanRecord), Settings.Secure.getString(getContentResolver(), "bluetooth_name"), "7",
//                        dt.substring(0, dt.indexOf(".")), "0", "0",
//                        deliveryType, messageSemantics, metaSeqNumber
//                );
//                Log.i(LOG_FOREGROUND, "DATA:: " + packet.toString());
//               // TODO :: Save to DB
//            }
//        };

    @SuppressLint("MissingPermission")
    private void startBLEScan() {
//        bluetoothAdapter.startLeScan(scanCallback);
    }

    @SuppressLint("MissingPermission")
    private void stopBLEScan() {
//        bluetoothAdapter.stopLeScan(scanCallback);
    }

    /**
     * Parsing Advertisement data
     *
     * @param scanRecord
     */
    public String packetToString(byte[] scanRecord) {
        // Simply print all raw bytes
//        try {
//            String decodedRecord = new String(scanRecord, "UTF-8");
//            Log.i(LOG_FOREGROUND,"Byte to String : " + ByteArrayToString(scanRecord));
//            Log.i(LOG_FOREGROUND,"decoded String : " + decodedRecord);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        // Parse data bytes into individual records
//        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);
//
//        // Print individual records
//        if (records.size() == 0) {
//            return "Scan Record Empty";
//        }
//        return TextUtils.join(",", records);

        Map<Integer, String> res = BLEParser.ParseRecord(scanRecord);
        Log.i(LOG_FOREGROUND, "Service UUID:: " + BLEParser.getServiceUUID(res));
        for(Integer k : res.keySet()) {
            Log.i(LOG_FOREGROUND, "Data:: " + k + " :: " + res.get(k));
        }
        Log.i(LOG_FOREGROUND, "------------------------------");

        return "";
    }

    public static String ByteArrayToString(byte[] ba) {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }

    /**
     * LOCATION MODULES
     */
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
        stopBLEScan();
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

    public static class AdRecord {
        public AdRecord(int length, int type, byte[] data) {
            String decodedRecord = "";
            try {
                decodedRecord = new String(data, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
//            Log.i(LOG_TAG, "Length: " + length + " Type : " + type + " Data : " + ByteArrayToString(data));
        }

        // ...

        public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
            List<AdRecord> records = new ArrayList<>();

            int index = 0;
            while (index < scanRecord.length) {
                int length = scanRecord[index++];
                //Done once we run out of records
                if (length == 0) break;

                int type = scanRecord[index];
                //Done if our record isn't a valid type
                if (type == 0) break;

                byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

                records.add(new AdRecord(length, type, data));
                //Advance
                index += length;
            }
            return records;
        }

        // ...
    }
}

/*
Packet{
pktId=0,
sensorId='',
rssi='-85',
nativeMAC='5C:30:B8:4B:D7:4B',
longitude='-117.8330569',
latitude='33.6416336',
id='00000000-0000-0000-0000-5c30b84bd74b',
advRecord='com.subhamoykarmakar.dm.v2.services.ForegroundService$AdRecord@a1cd08f,
com.subhamoykarmakar.dm.v2.services.ForegroundService$AdRecord@36a8f1c,
com.subhamoykarmakar.dm.v2.services.ForegroundService$AdRecord@7a1a725',
name='Subhamoy's Galaxy A31',
type='7',
timestamp='2022-01-09 13:27:21',
first_packet='0',
commitStatus='0',
orderedDeliveryStatus=0,
msgSemantics=0,
metaSeqNumber=983}
 */
