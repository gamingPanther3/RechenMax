package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.NotificationHelper.sendNotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.Random;

/**
 * This class represents a background service for RechenMax app.
 * It sends reminders at specified intervals and manages the service lifecycle.
 */
public class BackgroundService extends Service {
    // Notification IDs and channel IDs for the service and reminders
    private static final int NOTIFICATION_ID_1 = 1;
    private static final int NOTIFICATION_ID_2 = 2;
    private static final String CHANNEL_ID_1 = "BackgroundServiceChannel";
    private static final String CHANNEL_NAME_1 = "BackgroundService";
    private static final String CHANNEL_ID_2 = "RechenMax";
    private static final String CHANNEL_NAME_2 = "Erinnerung";

    // Name for shared preferences file and key for last background time
    private static final String PREFS_NAME = "BackgroundServicePrefs";
    private static final String LAST_BACKGROUND_TIME_KEY = "lastBackgroundTime";

    // Interval for reminders (4 days)
    private static final long NOTIFICATION_INTERVAL = 1000 * 60 * 60 * 24 * 4; // 1000 * 60 * 60 * 24 * 4 = 4 days

    // Handler for scheduling reminders, and other variables
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean startedByBootReceiver = false;
    private final Random random = new Random();
    private SharedPreferences sharedPreferences;
    private boolean isServiceRunning = true;

    /**
     * Runnable for sending reminders at intervals
     */
    private final Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isServiceRunning) {
                checkNotification();
                handler.postDelayed(this, 600000); // 600000 = 10min
            }
            Log.d("Remaining Time", "Remaining Time: " + ((NOTIFICATION_INTERVAL + 1000 - (System.currentTimeMillis() - getLastBackgroundTime())) / 1000) + "s");
        }
    };

    /**
     * onBind method required by Service class but not used in this implementation.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * onCreate method initializes necessary variables and creates notification channels.
     * It also cancels any existing notifications and starts the service in the foreground.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        createNotificationChannel();
        NotificationHelper.cancelNotification(this, NOTIFICATION_ID_1);
        NotificationHelper.cancelNotification(this, NOTIFICATION_ID_2);
        startForeground(NOTIFICATION_ID_1, buildNotification());
        Log.d(CHANNEL_NAME_1, "Service created");
    }

    /**
     * onStartCommand method called when the service is started.
     * Starts the notificationRunnable for scheduling reminders and updates the last background time.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(CHANNEL_NAME_1, "Service started");

        startedByBootReceiver = intent != null && intent.getBooleanExtra("started_by_boot_receiver", false);

        if(!startedByBootReceiver) {
            setLastBackgroundTime(System.currentTimeMillis());
        }

        handler.post(notificationRunnable);

        return START_STICKY;
    }

    /**
     * checkNotification method checks if a reminder needs to be sent based on the last background time.
     * If the time since the last background exceeds the notification interval, a random reminder is sent.
     * If the main notification is not active, the foreground service is restarted.
     */
    private void checkNotification() {
        if (System.currentTimeMillis() - getLastBackgroundTime() > NOTIFICATION_INTERVAL) {
            final int num = random.nextInt(4);
            System.out.println(num);
            switch(num) {
                case 0:
                    sendNotification(this, 2, "Vergiss mich nicht!", "Hey, du hast schon eine Weile nichts mehr gerechnet. Vielleicht wird es mal wieder Zeit.", CHANNEL_ID_2, CHANNEL_NAME_2);
                    break;
                case 1:
                    sendNotification(this, 2, "Es wird Zeit zu rechnen!", "Dein RechenMax gähnt vor Langeweile. Zeit für ein paar knifflige Berechnungen!", CHANNEL_ID_2, CHANNEL_NAME_2);
                    break;
                case 2:
                    sendNotification(this, 2, "Rechenzeit!", "Berechne die Antwort auf das Leben, das Universum und alles!", CHANNEL_ID_2, CHANNEL_NAME_2);
                    break;
                case 3:
                    sendNotification(this, 2, "RechenMax wartet!", "RechenMax freut sich auf dich!", CHANNEL_ID_2, CHANNEL_NAME_2);
                    break;
            }
            setLastBackgroundTime(System.currentTimeMillis());
        }
        if(!NotificationHelper.isNotificationActive(this, 1)) {
            startForeground(NOTIFICATION_ID_1, buildNotification());
        }
    }

    /**
     * getLastBackgroundTime method retrieves the last background time from shared preferences.
     */
    public long getLastBackgroundTime() {
        return sharedPreferences.getLong(LAST_BACKGROUND_TIME_KEY, System.currentTimeMillis());
    }

    /**
     * setLastBackgroundTime method sets the last background time in shared preferences.
     */
    private void setLastBackgroundTime(long time) {
        sharedPreferences.edit().putLong(LAST_BACKGROUND_TIME_KEY, time).apply();
    }

    /**
     * onDestroy method called when the service is destroyed.
     * Updates the last background time, stops the service, and removes callbacks from the handler.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        setLastBackgroundTime(System.currentTimeMillis());
        isServiceRunning = false;
        handler.removeCallbacks(notificationRunnable);
        Log.d(CHANNEL_NAME_1, "Service destroyed");
    }

    /**
     * buildNotification method constructs the foreground notification for the service.
     */
    public Notification buildNotification() {
        Notification.Builder builder;
        builder = new Notification.Builder(this, CHANNEL_ID_1);

        builder.setDefaults(0);

        return builder.setContentTitle("RechenMax im Hintergrund")
                .setContentText("RechenMax ist nun im Hintergrund aktiv.")
                .setSmallIcon(R.drawable.rechenmax_notification_icon)
                .build();
    }

    /**
     * createNotificationChannel method creates the notification channel for the service.
     */
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID_1,
                "RechenMax Background Service",
                NotificationManager.IMPORTANCE_MIN
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
}