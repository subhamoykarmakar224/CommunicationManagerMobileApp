package com.subhamoykarmakar.dm.v2.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.subhamoykarmakar.dm.v2.MainActivity;
import com.subhamoykarmakar.dm.v2.R;
import com.subhamoykarmakar.dm.v2.utils.Constants;

public class ForegroundService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerNotification();

        return START_STICKY;
    }

    private void registerNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0
        );
        Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Communication Manager")
                .setContentText("Started...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
