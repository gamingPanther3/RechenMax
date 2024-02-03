package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.NotificationHelper.sendNotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class BackgroundService extends Service {
    private static final int NOTIFICATION_ID_1 = 1;
    private static final int NOTIFICATION_ID_2 = 2;
    private static final String CHANNEL_ID_1 = "BackgroundServiceChannel";
    private static final String CHANNEL_NAME_1 = "BackgroundService";
    private static final String CHANNEL_ID_2 = "RechenMax";
    private static final String CHANNEL_NAME_2 = "Erinnerung";

    private final Handler handler = new Handler(Looper.getMainLooper());
    //                                                 ms  * ss * mm * hh * t
    private static final long NOTIFICATION_INTERVAL = 1000 * 60 * 1 * 1 * 1; // = 4 days = 1000 * 60 * 60 * 24 * 4
    private long lastBackgroundTime = System.currentTimeMillis();
    private boolean isServiceRunning = true;

    private final Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if (isServiceRunning) {
                checkNotification(currentTime);
                handler.postDelayed(this, 60000); // 600000
            }
            Log.d("Remaining Time", "Remaining Time: " + ((NOTIFICATION_INTERVAL + 1000 - (currentTime - lastBackgroundTime)) / 1000) + "s");
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        NotificationHelper.cancelNotification(this, NOTIFICATION_ID_1);
        NotificationHelper.cancelNotification(this, NOTIFICATION_ID_2);
        startForeground(NOTIFICATION_ID_1, buildNotification());
        Log.d(CHANNEL_NAME_1, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(CHANNEL_NAME_1, "Service started");

        handler.post(notificationRunnable);

        return START_STICKY;
    }

    private void checkNotification(long currentTime) {
        if (currentTime - lastBackgroundTime > NOTIFICATION_INTERVAL) {
            sendNotification(this, 2,"Vergiss mich nicht!", "Hey, du hast schon eine Weile nichts mehr gerechnet. Vielleicht wird es mal wieder Zeit.", CHANNEL_ID_2, CHANNEL_NAME_2);
            lastBackgroundTime = currentTime;
        }
        if(!NotificationHelper.isNotificationActive(this, 1)) {
            startForeground(NOTIFICATION_ID_1, buildNotification());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        handler.removeCallbacks(notificationRunnable);
        Log.d(CHANNEL_NAME_1, "Service destroyed");
    }

    public Notification buildNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID_1);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setDefaults(0);

        return builder.setContentTitle("RechenMax im Hintergrund")
                .setContentText("RechenMax ist nun im Hintergrund aktiv.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID_1,
                    "RechenMax Background Service",
                    NotificationManager.IMPORTANCE_MIN
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}