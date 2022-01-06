package com.example.dm.services;

import android.os.Handler;
import android.os.Looper;

public class SaveToDBLooperThread extends Thread {

    public Handler handler;
    public Looper looper;

    @Override
    public void run() {
        Looper.prepare();
        looper = Looper.myLooper();
        handler = new Handler();
        Looper.loop();
    }
}
