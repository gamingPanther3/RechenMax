package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.NotificationHelper.sendNotification;
import static com.mlprograms.rechenmax.NotificationText.mainNotificationContentEnglish;
import static com.mlprograms.rechenmax.NotificationText.mainNotificationContentFrench;
import static com.mlprograms.rechenmax.NotificationText.mainNotificationContentGerman;
import static com.mlprograms.rechenmax.NotificationText.mainNotificationContentSpanish;
import static com.mlprograms.rechenmax.NotificationText.mainNotificationTitleEnglish;
import static com.mlprograms.rechenmax.NotificationText.mainNotificationTitleFrench;
import static com.mlprograms.rechenmax.NotificationText.mainNotificationTitleGerman;
import static com.mlprograms.rechenmax.NotificationText.mainNotificationTitleSpanish;
import static com.mlprograms.rechenmax.NotificationText.notificationHintsListEnglish;
import static com.mlprograms.rechenmax.NotificationText.notificationHintsListFrench;
import static com.mlprograms.rechenmax.NotificationText.notificationHintsListGerman;
import static com.mlprograms.rechenmax.NotificationText.notificationHintsListSpanish;

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
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * This class represents a background service for RechenMax app.
 * It sends reminders at specified intervals and manages the service lifecycle.
 */
public class BackgroundService extends Service {
    // Notification IDs and channel IDs for the service and reminders
    public static final int NOTIFICATION_ID_BACKGROUND = 1;
    public static final int NOTIFICATION_ID_REMEMBER = 2;
    public static final int NOTIFICATION_ID_HINTS = 3;
    public static final String CHANNEL_ID_BACKGROUND = "BackgroundServiceChannel";
    public static final String CHANNEL_NAME_BACKGROUND = "BackgroundService";
    public static final String CHANNEL_ID_REMEMBER = "RechenMax Remember";
    public static final String CHANNEL_NAME_REMEMBER = "Remember";
    public static final String CHANNEL_ID_HINTS = "RechenMax Hints";
    public static final String CHANNEL_NAME_HINTS = "Hints";

    // Name for shared preferences file and key for last background time
    private static final String PREFS_NAME = "BackgroundServicePrefs";
    private static final String LAST_BACKGROUND_TIME_KEY = "lastBackgroundTime";

    // Interval for reminders (4 days)
    private static final long NOTIFICATION_INTERVAL = 1000 * 60 * 60 * 24 * 4; // 1000 * 60 * 60 * 24 * 4 = 4 days

    // Handler for scheduling reminders, and other variables
    private SharedPreferences sharedPreferences;
    private final DataManager dataManager = new DataManager();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private boolean isServiceRunning = true;
    private static final int min = 12;
    private static final int max = 15;

    /**
     * Runnable for sending reminders at intervals
     */
    private final Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isServiceRunning) {
                final int currentTime = Integer.parseInt((String) DateFormat.format("HH", new Date()));
                final String language = Locale.getDefault().getDisplayLanguage();
                handler.postDelayed(this, 600000); // 600000 = 10min

                checkBackgroundServiceNotification();

                if(dataManager.readFromJSON("allowRememberNotifications", getApplicationContext()).equals("true")) {
                    checkRememberNotification(currentTime, language);
                }

                if(dataManager.readFromJSON("allowDailyNotifications", getApplicationContext()).equals("true")) {
                    checkHintNotification(currentTime, language);
                }
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
        if(dataManager.readFromJSON("allowNotification", getApplicationContext()) == null) {
            dataManager.saveToJSON("allowNotification", false, getApplicationContext());
        }
        String allowNotification = dataManager.readFromJSON("allowNotification", getApplicationContext());
        String allowRememberNotifications = dataManager.readFromJSON("allowRememberNotifications", getApplicationContext());
        String allowDailyNotifications = dataManager.readFromJSON("allowDailyNotifications", getApplicationContext());

        //Log.e("DEBUG", allowNotification);
        //Log.e("DEBUG", allowRememberNotifications);
        //Log.e("DEBUG", allowDailyNotifications);

        if ("true".equals(allowNotification) && (("true".equals(allowRememberNotifications) || "true".equals(allowDailyNotifications)))) {
            //dataManager.saveToJSON("notificationSent", false, this);

            createNotificationChannel(this);
            NotificationHelper.cancelNotification(this, NOTIFICATION_ID_BACKGROUND);
            NotificationHelper.cancelNotification(this, NOTIFICATION_ID_REMEMBER);
            startForeground(NOTIFICATION_ID_BACKGROUND, buildNotification());
            Log.d(CHANNEL_NAME_BACKGROUND, "Service created");
        }
    }

    /**
     * onStartCommand method called when the service is started.
     * Starts the notificationRunnable for scheduling reminders and updates the last background time.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String allowNotification = dataManager.readFromJSON("allowNotification", getApplicationContext());
        String allowRememberNotifications = dataManager.readFromJSON("allowRememberNotifications", getApplicationContext());
        String allowDailyNotifications = dataManager.readFromJSON("allowDailyNotifications", getApplicationContext());

        if ("true".equals(allowNotification) && (("true".equals(allowRememberNotifications) || "true".equals(allowDailyNotifications)))) {
            Log.d(CHANNEL_NAME_BACKGROUND, "Service started");

            boolean startedByBootReceiver = intent != null && intent.getBooleanExtra("started_by_boot_receiver", false);

            if(!startedByBootReceiver) {
                setLastBackgroundTime(System.currentTimeMillis());
            }

            handler.post(notificationRunnable);
        }

        return START_STICKY;
    }

    private void checkBackgroundServiceNotification() {
        if(!NotificationHelper.isNotificationActive(this, NOTIFICATION_ID_BACKGROUND)) {
            startForeground(NOTIFICATION_ID_BACKGROUND, buildNotification());
        }
    }

    /**
     * checkNotification method checks if a reminder needs to be sent based on the last background time.
     * If the time since the last background exceeds the notification interval, a random reminder is sent.
     * If the main notification is not active, the foreground service is restarted.
     */
    private void checkRememberNotification(final int currentTime, final String language) {

        if ((System.currentTimeMillis() - getLastBackgroundTime() > NOTIFICATION_INTERVAL) || (System.currentTimeMillis() - getLastBackgroundTime() <= 0)) {
            String title_remember = getRandomElement(mainNotificationTitleGerman);
            String content_remember = getRandomElement(mainNotificationContentGerman);

            switch (language) {
                case "English":
                    title_remember = getRandomElement(mainNotificationTitleEnglish);
                    content_remember = getRandomElement(mainNotificationContentEnglish);
                    break;
                case "français":
                    title_remember = getRandomElement(mainNotificationTitleFrench);
                    content_remember = getRandomElement(mainNotificationContentFrench);
                    break;
                case "español":
                    title_remember = getRandomElement(mainNotificationTitleSpanish);
                    content_remember = getRandomElement(mainNotificationContentSpanish);
                    break;
            }

            if (currentTime >= 14 && currentTime <= 18) {
                sendNotification(this, NOTIFICATION_ID_REMEMBER, title_remember, content_remember, CHANNEL_ID_REMEMBER, true);
                setLastBackgroundTime(System.currentTimeMillis());
            }
        }
    }

    private void checkHintNotification(final int currentTime, final String language) {
        String title_hints = "Wusstest du schon?";
        String content_hints = getRandomElement(notificationHintsListGerman);

        switch (language) {
            case "English":
                title_hints = "Did you know?";
                content_hints = getRandomElement(notificationHintsListEnglish);
                break;
            case "français":
                title_hints = "Saviez-vous?";
                content_hints = getRandomElement(notificationHintsListFrench);
                break;
            case "español":
                title_hints = "¿Sabías que?";
                content_hints = getRandomElement(notificationHintsListSpanish);
                break;
        }

        if (currentTime >= min && currentTime <= max) {
            if(!Boolean.parseBoolean(dataManager.readFromJSON("notificationSent", this)) && random.nextInt(20) == 1) {
                dataManager.saveToJSON("notificationSent", true, this);
                sendNotification(this, NOTIFICATION_ID_HINTS, title_hints, content_hints, CHANNEL_ID_HINTS, true);
            }
        } else if (currentTime >= max && !Boolean.parseBoolean(dataManager.readFromJSON("notificationSent", this))) {
            dataManager.saveToJSON("notificationSent", true, this);
            sendNotification(this, NOTIFICATION_ID_HINTS, title_hints, content_hints, CHANNEL_ID_HINTS, true);
        } else if (currentTime >= 0 && currentTime <= 1 && Boolean.parseBoolean(dataManager.readFromJSON("notificationSent", this))) {
            dataManager.saveToJSON("notificationSent", false, this);
        }
    }

    private static String getRandomElement(List<String> list) {
        Random rand = new Random();
        int randomIndex = rand.nextInt(list.size());
        return list.get(randomIndex);
    }

    /**
     * getLastBackgroundTime method retrieves the last background time from shared preferences.
     */
    private long getLastBackgroundTime() {
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
        Log.d(CHANNEL_NAME_BACKGROUND, "Service destroyed");
    }

    /**
     * buildNotification method constructs the foreground notification for the service.
     */
    private Notification buildNotification() {
        Notification.Builder builder;
        builder = new Notification.Builder(this, CHANNEL_ID_BACKGROUND);
        final String language = Locale.getDefault().getDisplayLanguage();

        switch (language) {
            case "English":
                return builder.setContentTitle("RechenMax in the background")
                        .setContentText("RechenMax is now active in the background.")
                        .setSmallIcon(R.drawable.rechenmax_notification_icon)
                        .build();
            case "français":
                return builder.setContentTitle("RechenMax en arrière-plan")
                        .setContentText("RechenMax est maintenant actif en arrière-plan.")
                        .setSmallIcon(R.drawable.rechenmax_notification_icon)
                        .build();
            case "español":
                return builder.setContentTitle("RechenMax en segundo plano")
                        .setContentText("RechenMax está ahora activo en segundo plano.")
                        .setSmallIcon(R.drawable.rechenmax_notification_icon)
                        .build();
            default:
                return builder.setContentTitle("RechenMax im Hintergrund")
                        .setContentText("RechenMax ist nun im Hintergrund aktiv.")
                        .setSmallIcon(R.drawable.rechenmax_notification_icon)
                        .build();
        }
    }

    /**
     * createNotificationChannel method creates the notification channel for the service.
     */
    public static void createNotificationChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);

        NotificationChannel backgroundChannel = new NotificationChannel(
                CHANNEL_ID_BACKGROUND,
                CHANNEL_NAME_BACKGROUND,
                NotificationManager.IMPORTANCE_MIN
        );

        NotificationChannel rememberChannel = new NotificationChannel(
                CHANNEL_ID_REMEMBER,
                CHANNEL_NAME_REMEMBER,
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationChannel hintsChannel = new NotificationChannel(
                CHANNEL_ID_HINTS,
                CHANNEL_NAME_HINTS,
                NotificationManager.IMPORTANCE_HIGH
        );

        manager.createNotificationChannel(backgroundChannel);
        manager.createNotificationChannel(rememberChannel);
        manager.createNotificationChannel(hintsChannel);
    }
}