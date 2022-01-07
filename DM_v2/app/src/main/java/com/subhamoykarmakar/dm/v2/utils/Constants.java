package com.subhamoykarmakar.dm.v2.utils;

import android.Manifest;

public class Constants {
    public static final String DB_NAME = "datamule";
    public static final Integer DB_VER = 1;

    public static final String [] LIST_OF_TABLE_LOCATION = { "location" };

    public static final String TABLE_LOCATION = "location";
    public static final String KEY_PKEY = "pkey";  // 1, 2, 3
    public static final String KEY_LAT = "latitude";  // Curr-2, Curr-1, Curr-(Delta)-1, curr-(Delta)-2
    public static final String KEY_LONG = "longitude";  // Curr-2, Curr-1, Curr-(Delta)-1, curr-(Delta)-2
    public static final String KEY_CURR_IDENTIFIER = "identifier";  // 1, 2, 3

    public static final String CREATE_TABLE_LOCATION = "create table " + TABLE_LOCATION + "(" +
            KEY_PKEY + " int, " + KEY_LAT + " text, " + KEY_LONG + " text, " +
            KEY_CURR_IDENTIFIER + " int)";

    // Notification channel ID
    public static final String NOTIFICATION_CHANNEL_ID = "DataMuleNotification";

    public static final String[] PERMISSIONS = {
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // Shared Preference Keys
    public static final String KEY_SP_LATITUDE = "latitude";
    public static final String KEY_SP_LONGITUDE = "longitude";

}
