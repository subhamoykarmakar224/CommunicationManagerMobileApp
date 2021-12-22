package com.subhamoy.datamule.datamule.helper;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class GetPermissions {
    public static void getRuntimePermissions(Activity activity) {
        String[] PERMISSIONS = Constants.PERMISSIONS;
        ActivityCompat.requestPermissions((Activity) activity, PERMISSIONS, Constants.REQUEST_CODE);
        for (String p : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{p},
                        Constants.REQUEST_CODE);
            }
        }
    }
}
