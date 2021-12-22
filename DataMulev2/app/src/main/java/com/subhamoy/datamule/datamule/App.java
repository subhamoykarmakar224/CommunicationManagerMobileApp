package com.subhamoy.datamule.datamule;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.subhamoy.datamule.datamule.helper.Constants;

public class App extends Application {
    private static final String CHANNEL_ID = Constants.NOTIFICATION_CHANNEL_ID;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    "DATA MULE :: Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
