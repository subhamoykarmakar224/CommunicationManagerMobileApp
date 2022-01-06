package com.example.dm.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class BluetoothHelper {
    public static boolean getBluetoothONOFFStatus(BluetoothAdapter mBluetooth) {
        return mBluetooth.isEnabled();
    }

    public static boolean getBLEAdvertisementSupport(BluetoothAdapter mBluetooth) {
        return mBluetooth.isMultipleAdvertisementSupported();
    }
}
