package com.mlprograms.rechenmax;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

/**
 * NotificationReceiver class extends BroadcastReceiver and handles notifications received from the system.
 * It sends notifications using NotificationHelper.
 */
public class NotificationReceiver extends BroadcastReceiver {
    // Channel ID and name for the background service channel
    private static final String CHANNEL_ID = "BackgroundServiceChannel";
    private static final String CHANNEL_NAME = "BackgroundService";

    /**
     * onReceive method called when a notification is received.
     * Acquires a wake lock, sends the notification using NotificationHelper, and releases the wake lock.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NotificationReceiver:WakeLock");
        wakeLock.acquire();

        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        NotificationHelper.sendNotification(context, 2, title, content, CHANNEL_ID, CHANNEL_NAME, true);

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
