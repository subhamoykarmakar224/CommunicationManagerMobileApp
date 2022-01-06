
package com.example.dm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.dm.services.BluetoothHelper;
import com.example.dm.services.Constants;
import com.example.dm.services.ForegroundService;
import com.example.dm.services.LocationServiceHelper;
import com.example.dm.services.NetworkServiceHelper;
import com.example.dm.services.PacketRoomDatabase;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private boolean isConnectedToTPDb;
    AsyncTask<?, ?, ?> runningTask;
    BluetoothAdapter bluetoothAdapter;

    private Button btnStart, btnStop;
    RadioGroup radioGroupDeliveryType, radioGroupMsgType;
    private RadioButton btnOrderedDelivery, btnUnorderedDelivery;
    private RadioButton btnMsgType0, btnMsgType1, btnMsgType2;
    private Spinner spinnerServerList;
    private EditText textViewStatus;

    private static int REQUEST_CODE = 11023;
    private static String[] PERMISSIONS = {
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // TODO :: DELETE
    Handler handler;
    BluetoothDevice bluetoothDevice = null;


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
                            btnStopComponentHelper();
                            Intent intentService = new Intent(context, ForegroundService.class);
                            stopService(intentService);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnOrderedDelivery = findViewById(R.id.radioButtonOrderedDelivery);
        btnUnorderedDelivery = findViewById(R.id.radioButtonUnorderedDelivery);
        btnMsgType0 = findViewById(R.id.radioButtonType0);
        btnMsgType1 = findViewById(R.id.radioButtonType1);
        btnMsgType2 = findViewById(R.id.radioButtonType2);
        radioGroupMsgType = findViewById(R.id.radioGroupMsgType);
        radioGroupDeliveryType = findViewById(R.id.radioGroupDeliveryType);
        spinnerServerList = findViewById(R.id.spinnerServerList);
        textViewStatus = findViewById(R.id.textViewStatus);

        // Initialize values and UI
        initializeValues();

        // Runtime permission check
        checkRuntimePermissions();

        // TODO:: Delete :: BLE Scan
//        handler = new Handler();
//        bleScanDemoExample();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int cnt = PacketRoomDatabase.getInstance(getApplicationContext())
                        .packetDAO().getCommittedPacketCount();
                Log.i("ollo", "Count of Data :: " + cnt);
            }
        }).start();
    }

    /**
     * Initialize the UI components and other values
     */
    private void initializeValues() {
        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcaseReceiver2Discoverability, intentFilter);

        isConnectedToTPDb = Boolean.FALSE;
        btnStop.setEnabled(Boolean.FALSE);
        btnOrderedDelivery.setChecked(Boolean.TRUE);
        btnMsgType0.setChecked(Boolean.TRUE);


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            bluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        // Populate the server list spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.server_ips, R.layout.support_simple_spinner_dropdown_item
        );
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerServerList.setAdapter(adapter);

        textViewStatus.setEnabled(Boolean.FALSE);

        // TODO :: DELETE THIS LATER
        spinnerServerList.setSelection(1);

        // Check if my foreground service is running
        if (isMyServiceRunning(ForegroundService.class)) {
            btnStartComponentHelper();
            textViewStatus.setText(textViewStatus.getText().append("Data collection is running..."));
        } else {
            btnStopComponentHelper();
        }
    }

    /**
     * Checks Runtime permission for the application
     */
    private void checkRuntimePermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_CODE);
        for (String p : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, p) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{p},
                        REQUEST_CODE);
            }
        }
    }

    /**
     * Start Button on click listener
     *
     * @param v
     */
    public void btnStartService(View v) {
        textViewStatus.setText("Checking bluetooth status...");
        if (!BluetoothHelper.getBluetoothONOFFStatus(bluetoothAdapter)) {
            textViewStatus.setText(textViewStatus.getText().append("ERROR\n"));

            // Prompt user to switch on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }

        if (!BluetoothHelper.getBLEAdvertisementSupport(bluetoothAdapter)) {
            showDialogBox("Error", "Your device does not support Bluetooth Advertisement.");
            return;
        }

        textViewStatus.setText(textViewStatus.getText().append("OK\n"));

        textViewStatus.setText(textViewStatus.getText().append("Checking location status..."));
        if (!(new LocationServiceHelper()).getLocationServiceONOFFStatus(this)) {
            showDialogBox("Error", "Please switch on location and try again.");
            textViewStatus.setText(textViewStatus.getText().append("ERROR\n"));
            return;
        }
        textViewStatus.setText(textViewStatus.getText().append("OK\n"));

        textViewStatus.setText(textViewStatus.getText().append("Checking network status..."));
        if (!NetworkServiceHelper.hasInternetAccess(this)) {
            textViewStatus.setText(textViewStatus.getText().append("No connection!\n"));
        } else {
            textViewStatus.setText(textViewStatus.getText().append("OK\n"));
        }

        // Check and Start Background Service
        String tmpMsgType = "", tmpDeliveryType = "";
        if (btnMsgType0.isChecked()) {
            tmpMsgType = "T0";
        } else if (btnMsgType1.isChecked()) {
            tmpMsgType = "T1";
        } else if (btnMsgType2.isChecked()) {
            tmpMsgType = "T2";
        } else {
            tmpMsgType = "T0";
        }
        textViewStatus.setText(textViewStatus.getText().append("Message type...").append(tmpMsgType).append("\n"));

        if (btnOrderedDelivery.isChecked()) {
            tmpDeliveryType = "ORD";
        } else if (btnUnorderedDelivery.isChecked()) {
            tmpDeliveryType = "UORD";
        } else {
            tmpDeliveryType = "UORD";
        }

        // START FOREGROUND SERVICE
        Intent intentService = new Intent(MainActivity.this, ForegroundService.class);
        intentService.putExtra(Constants.EXTRA_STRING_DELIVERY_TYPE, tmpDeliveryType);
        intentService.putExtra(Constants.EXTRA_STRING_MESSAGE_TYPE, tmpMsgType);
        intentService.putExtra(Constants.EXTRA_STRING_SERVER_IP, spinnerServerList.getSelectedItem().toString());
        ContextCompat.startForegroundService(MainActivity.this, intentService);

        // Disable the start button
        btnStartComponentHelper();
    }

    private void btnStartComponentHelper() {
        btnStart.setEnabled(Boolean.FALSE);
        btnStop.setEnabled(Boolean.TRUE);
        for (int i = 0; i < radioGroupDeliveryType.getChildCount(); i++) {
            radioGroupDeliveryType.getChildAt(i).setEnabled(Boolean.FALSE);
        }
        for (int i = 0; i < radioGroupMsgType.getChildCount(); i++) {
            radioGroupMsgType.getChildAt(i).setEnabled(Boolean.FALSE);
        }
        spinnerServerList.setEnabled(Boolean.FALSE);
    }

    /**
     * Stop Button on click Listener
     *
     * @param v
     */
    public void btnStopService(View v) {
        btnStopComponentHelper();
        Intent intentService = new Intent(this, ForegroundService.class);
        stopService(intentService);
    }

    private void btnStopComponentHelper() {
        btnStop.setEnabled(Boolean.FALSE);
        btnStart.setEnabled(Boolean.TRUE);
        textViewStatus.setText("");
        for (int i = 0; i < radioGroupDeliveryType.getChildCount(); i++) {
            radioGroupDeliveryType.getChildAt(i).setEnabled(Boolean.TRUE);
        }
        for (int i = 0; i < radioGroupMsgType.getChildCount(); i++) {
            radioGroupMsgType.getChildAt(i).setEnabled(Boolean.TRUE);
        }
        spinnerServerList.setEnabled(Boolean.TRUE);
    }

    /**
     * Dialog Box Builder
     *
     * @param title
     * @param msg
     */
    private void showDialogBox(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(Boolean.FALSE);
        builder.setPositiveButton("Ok", null);
        AlertDialog alert = builder.create();
        alert.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcaseReceiver2Discoverability);
        bluetoothAdapter = null;
    }
}