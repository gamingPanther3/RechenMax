package com.mlprograms.rechenmax;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NotificationReceiver:WakeLock");
        wakeLock.acquire();

        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        NotificationHelper.sendNotification(context, 2, title, content);

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
