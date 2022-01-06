package com.subhamoykarmakar.dm.v2.sp;

import android.content.Context;

import com.subhamoykarmakar.dm.v2.utils.Constants;

public class SPUpdateLocation {
    private SharedPrefController sharedPrefController;

    public SPUpdateLocation(Context context) {
        sharedPrefController = SharedPrefController.getInstance(context);
    }

    public void updateLatLong(String Lat, String Lng) {
        sharedPrefController.setData(Constants.KEY_SP_LATITUDE, Lat);
        sharedPrefController.setData(Constants.KEY_SP_LONGITUDE, Lng);
    }

    public String[] getLatLong() {
        String [] res = {
                sharedPrefController.getData(Constants.KEY_SP_LATITUDE),
                sharedPrefController.getData(Constants.KEY_SP_LONGITUDE)
        };
        return res;
    }
}
