package com.example.dm.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dm.MainActivity;
import com.example.dm.R;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ForegroundService extends Service implements LocationListener {
    private static final String LOG_TAG = "datamule";
    int cnt;
    ExecutorService es;
    private ScanCallback mScanCallback;
    private Handler dataSendHandler;
    BluetoothAdapter bluetoothAdapter;
    LocationManager locationManager;
    public String latitude, longitude;
    String hexMAC, dt;
    int metaSeqNumber;
    int deliveryType = 0;
    int messageSemantics = 0;
    Boolean sendingDataPackets;
    ThreadPoolExecutor threadPoolExecutor;

    String deliType; // ORD/UORD
    String msgType; // T0/T1/T2
    String serverIP;

    SaveToDBLooperThread saveToDBLooperThread;
    SendDataToServerv2 sendDataToServerv2;
    RequestQueue queue;

    boolean stillWaitingForCurrentRequest;
    StringBuilder stringBuilderURI;
    StringBuilder packetList;

    private Runnable runnableDeleteData;
    private Thread threadDeleteData;
    private Runnable runnableSendData;
    private Thread threadSendData;
    private boolean serviceRunning;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        stillWaitingForCurrentRequest = false;
        serviceRunning = false;
        stillWaitingForCurrentRequest = false;
        sendingDataPackets = false;

//        stringBuilderURI = new StringBuilder(serverIP);
//        stringBuilderURI.append(Constants.TEST_API_ADD_DATA_LIST);

        packetList = new StringBuilder("");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        cnt = 1;
        dt = "";
        metaSeqNumber = 0;
        getLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        serviceRunning = true;

        // Save to DB looper
        saveToDBLooperThread = new SaveToDBLooperThread();
        saveToDBLooperThread.start();

//        sendDataToServerv2 = new SendDataToServerv2();
//        sendDataToServerv2.start();

        queue = Volley.newRequestQueue(this);

        deliType = intent.getStringExtra(Constants.EXTRA_STRING_DELIVERY_TYPE); // ORD/UORD
        msgType = intent.getStringExtra(Constants.EXTRA_STRING_MESSAGE_TYPE); // T0/T1/T2
        serverIP = intent.getStringExtra(Constants.EXTRA_STRING_SERVER_IP);

        switch (deliType) {
            case "ORD":
                deliveryType = 1;
                break;

            default:
                deliveryType = 0;
                break;
        }

        switch (msgType) {
            case "T1":
                messageSemantics = 1;
                break;

            case "T2":
                messageSemantics = 2;
                break;

            default:
                messageSemantics = 0;
                break;
        }

        dataSendHandler = new Handler();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0
        );
        Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("DM: Data Collection Started...")
                .setContentText(cnt + " Type :: ".concat(deliType).concat(" Message :: ").concat(msgType))
                .setSmallIcon(R.mipmap.ic_donkey)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        NetworkServiceHelper.checkServerConnection(this, serverIP);

        // TODO ::  Data collection and save
        startScanning();

        // TODO :: Send data thread pool
        sendDataExample();
//        sendDataExample2();

        // TODO :: Delete committed Data
        deleteCommittedData();

        return START_STICKY;
    }

    /**
     * TODO :: Delete comitted data thread pool
     */
    public void deleteCommittedData() {
        runnableDeleteData = new Runnable() {
            @Override
            public void run() {
                while (serviceRunning) {
                    PacketRoomDatabase.getInstance(getApplicationContext())
                            .packetDAO()
                            .deleteCommittedPacket();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        threadDeleteData = new Thread(runnableDeleteData);
        if (!threadDeleteData.isAlive()) {
            threadDeleteData.start();
        }
    }

    public void sendDataExample2() {
        runnableSendData = new Runnable() {
            String json2 = "";

            @Override
            public void run() {
                while (serviceRunning) {
                    if (!NetworkServiceHelper.checkServerConnection(getApplicationContext(), serverIP)) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    Packet p = PacketRoomDatabase.getInstance(getApplicationContext())
                            .packetDAO().getSinglePacketsNotCommitted();
                    json2 = "{\"sensor_id\":" + "\"" + p.getSensorId() + "\"" + ",";
                    json2 = json2 + "\"name\":" + "\"" + p.getName() + "\"" + ",";
                    json2 = json2 + "\"type\":" + "\"" + p.getType() + "\"" + ",";
                    json2 = json2 + "\"timestamp\":" + "\"" + p.getTimestamp() + "\"" + ",";
                    json2 = json2 + "\"payload\":{\"id\":" + "\"" + p.getId() + "\"" + ",";
                    json2 = json2 + "\"native\":" + "\"" + p.getNativeMAC() + "\"" + ",";
                    json2 = json2 + "\"rssi\":" + "\"" + p.getRssi() + "\"" + ",";
                    json2 = json2 + "\"lat\":" + "\"" + p.getLatitude() + "\"" + ",";
                    json2 = json2 + "\"lon\":" + "\"" + p.getLongitude() + "\"" + ",";
                    json2 = json2 + "\"advrecord\":" + "\"" + p.getAdvRecord() + "\"";
                    json2 = json2 + "}}";

                    // Todo :: send data
                    if (msgType == "T0") {
                        SendDataToServer sendDataToServer = new SendDataToServer(
                                json2, queue, stringBuilderURI, p, getApplicationContext()
                        );
                    } else {

                    }
                    json2 = "";
                }
            }
        };
        threadSendData = new Thread(runnableSendData);
        if (!threadSendData.isAlive()) {
            threadSendData.start();
        }
    }

    /**
     * TODO :: Send data thread pool
     */
    public void sendDataExample() {
        threadPoolExecutor = new ThreadPoolExecutor(
                1, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );

//        threadPoolExecutor = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();

//        String s = serverIP + Constants.API_ADD_DATA;
        StringBuilder stringBuilderURI = new StringBuilder(serverIP);
        stringBuilderURI.append(Constants.TEST_API_ADD_DATA);
        String s = serverIP + Constants.TEST_API_ADD_DATA;
        Log.i(Constants.TAG_LOG, "Send Data to :: " + stringBuilderURI.toString());
        sendingDataPackets = true;

        // Send data to server
//        dataSendRunnable.run();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String json2 = "";
                while(sendingDataPackets) {
                    if(!NetworkServiceHelper.checkServerConnection(getApplicationContext(), serverIP))
                        continue;

                    List<Packet> packets = PacketRoomDatabase.getInstance(getApplicationContext())
                            .packetDAO().getPacketsNotCommitted();

                    for (Packet p : packets) {
                        Log.i("subha", "Packet: " + p.toString());
                        json2 = "{\"sensor_id\":" + "\"" + p.getSensorId() + "\"" + ",";
                        json2 = json2 + "\"name\":" + "\"" + p.getName() + "\"" + ",";
                        json2 = json2 + "\"type\":" + "\"" + p.getType() + "\"" + ",";
                        json2 = json2 + "\"timestamp\":" + "\"" + p.getTimestamp() + "\"" + ",";
                        json2 = json2 + "\"payload\":{\"id\":" + "\"" + p.getId() + "\"" + ",";
                        json2 = json2 + "\"native\":" + "\"" + p.getNativeMAC() + "\"" + ",";
                        json2 = json2 + "\"rssi\":" + "\"" + p.getRssi() + "\"" + ",";
                        json2 = json2 + "\"lat\":" + "\"" + p.getLatitude() + "\"" + ",";
                        json2 = json2 + "\"lon\":" + "\"" + p.getLongitude() + "\"" + ",";
                        json2 = json2 + "\"advrecord\":" + "\"" + p.getAdvRecord() + "\"";
                        json2 = json2 + "}}";

                        Log.i(Constants.TAG_LOG, "SEND TO :: " + stringBuilderURI.toString());

                        SendDataToServer sendDataToServer = new SendDataToServer(
                                json2, queue, stringBuilderURI, p, getApplicationContext()
                        );
                        threadPoolExecutor.execute(sendDataToServer);
                        json2 = "";
                    }
                }
            }
        }).start();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScanning();
        sendingDataPackets = false;
        serviceRunning = false;
        if (threadDeleteData.isAlive())
            threadDeleteData.interrupt();

        if (threadSendData.isAlive())
            threadSendData.interrupt();
    }

    /**
     * Get BLE advertisement data from near by devices
     * Specification
     * https://www.bluetooth.com/specifications/assigned-numbers/generic-access-profile/
     */
    private void startScanning() {
        Log.i(LOG_TAG, "Starting scanning...");
        Log.i(LOG_TAG, "COUNTER :: " + cnt);
        bluetoothAdapter.startLeScan(scanCallback);
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    private void stopScanning() {
        Log.i(LOG_TAG, "Stopping scanning....");
        serviceRunning = false;
        saveToDBLooperThread.looper.quitSafely();
//        sendDataToServerv2.looper.quitSafely();
        bluetoothAdapter.stopLeScan(scanCallback);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_SHORT).show();
            return;
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        onLocationChanged(locationGPS);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            longitude = String.valueOf(location.getLongitude());
            latitude = String.valueOf(location.getLatitude());
        } catch (Exception e) {
            Log.i(Constants.TAG_LOG, "Location manager error : " + e.getMessage());
            stopSelf();
        }
    }

    /**
     * BLE scan call back function
     */
    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            // TODO :: Delete later
//            Log.i(Constants.TAG_LOG, "SCANNING......");
//            Log.i(Constants.TAG_LOG, "-----------------------------------------------------------------");
//            Log.i(Constants.TAG_LOG, "PAYLOAD");
//            Log.i(Constants.TAG_LOG, "SensorID:" + Settings.Secure.getString(
//                    getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
//            Log.i(Constants.TAG_LOG, "rssi:" + rssi);
//            Log.i(Constants.TAG_LOG, "native: " + device.toString());
//            Log.i(Constants.TAG_LOG, "lon: " + longitude);
//            Log.i(Constants.TAG_LOG, "lat: " + latitude);
//            hexMAC = device.getAddress().replace(":", "").toLowerCase();
//            Log.i(Constants.TAG_LOG, "id: " + "00000000-0000-0000-0000-".concat(hexMAC));
//            Log.i(Constants.TAG_LOG, "advrecord: " + packetToString(scanRecord));
//            Log.i(Constants.TAG_LOG, "name:  " + Settings.Secure.getString(getContentResolver(), "bluetooth_name"));
//            Log.i(Constants.TAG_LOG, "type:  (5/7)???"); // TODO :: What is "type"?
//            Log.i(Constants.TAG_LOG, "timestamp: " +  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
//            Log.i(Constants.TAG_LOG, "-----------------------------------------------------------------");

            hexMAC = device.getAddress().replace(":", "").toLowerCase();
            dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            metaSeqNumber = Integer.parseInt(dt.substring(dt.indexOf(".") + 1));
            Packet packet = new Packet(
                    Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID),
                    String.valueOf(rssi), device.toString(), longitude, latitude,
                    "00000000-0000-0000-0000-".concat(hexMAC),
                    packetToString(scanRecord), Settings.Secure.getString(getContentResolver(), "bluetooth_name"),
                    "7", dt.substring(0, dt.indexOf(".")),
                    "0", "0", deliveryType, messageSemantics, metaSeqNumber);

            // Save to DB in a serialized fashion
            saveToDBLooperThread.handler.post(new Runnable() {
                @Override
                public void run() {
                    PacketRoomDatabase.getInstance(getApplicationContext())
                            .packetDAO()
                            .insertPacket(packet);
                }
            });
        }
    };

    /**
     * Parsing Advertisement data
     *
     * @param scanRecord
     */
    public String packetToString(byte[] scanRecord) {
        // Simply print all raw bytes
        try {
            String decodedRecord = new String(scanRecord, "UTF-8");
//            Log.i(LOG_TAG,"Byte to String : " + ByteArrayToString(scanRecord));
//            Log.i(LOG_TAG,"decoded String : " + decodedRecord);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Parse data bytes into individual records
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);

        // Print individual records
        if (records.size() == 0) {
            return "Scan Record Empty";
        }
        return TextUtils.join(",", records);
    }


    public static String ByteArrayToString(byte[] ba) {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }

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
