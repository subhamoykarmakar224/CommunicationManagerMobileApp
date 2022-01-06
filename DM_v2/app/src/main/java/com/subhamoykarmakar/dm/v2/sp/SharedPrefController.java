package com.subhamoykarmakar.dm.v2.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


public class SharedPrefController {

    private static SharedPrefController sf_instance = null;
    private Context context;
    private SharedPreferences sPref = null;
    Editor editor = null;

    public static SharedPrefController getInstance(Context context) {
        if(sf_instance == null) {
            sf_instance = new SharedPrefController(context);
        }
        return sf_instance;
    }

    public SharedPrefController(Context context) {
        this.context = context;
        sPref = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public String getData(String key) {
        return sPref.getString(key, "");
    }

    public void setData(String key, String val) {
        editor = sPref.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public void deleteData(String key) {
        editor = sPref.edit();
        editor.remove(key);
        editor.commit();
    }

    public void deleteAllData() {
        editor = sPref.edit();
        editor.clear();
        editor.commit();
    }




}
