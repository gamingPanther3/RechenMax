package com.mlprograms.rechenmax;

/*
 * Copyright (c) 2024 by Max Lemberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static com.mlprograms.rechenmax.BackgroundService.CHANNEL_ID_BACKGROUND;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * NotificationReceiver class extends BroadcastReceiver and handles notifications received from the system.
 * It sends notifications using NotificationHelper.
 */
public class NotificationReceiver extends BroadcastReceiver {

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
        NotificationHelper.sendNotification(context, 2, title, content, CHANNEL_ID_BACKGROUND, true);

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
