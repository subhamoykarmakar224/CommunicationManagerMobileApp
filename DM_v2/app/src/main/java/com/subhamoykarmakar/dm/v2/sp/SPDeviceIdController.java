package com.subhamoykarmakar.dm.v2.sp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.subhamoykarmakar.dm.v2.utils.Constants;

public class SPDeviceIdController {
    private SharedPrefController sharedPrefController;
    private Context context;

    public SPDeviceIdController(Context context) {
        sharedPrefController = SharedPrefController.getInstance(context);
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public void addDeviceId() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo.getMacAddress();
        sharedPrefController.setData(Constants.KEY_SP_DEVICE_ID, macAddress);
    }

    public String getDeviceId() {
        return sharedPrefController.getData(Constants.KEY_SP_DEVICE_ID);
    }
}
