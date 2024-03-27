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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

/**
 * NotificationHelper class provides utility methods to send, cancel, and check the status of notifications.
 */
public class NotificationHelper {

    /**
     * sendNotification method sends a notification with the specified title, content, and channel information.
     * @param context The context of the application.
     * @param notificationId The unique identifier for the notification.
     * @param title The title of the notification.
     * @param content The content of the notification.
     * @param CHANNEL_ID The ID of the notification channel.
     */
    public static void sendNotification(Context context, int notificationId, String title, String content, String CHANNEL_ID, boolean fullScreenIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.rechenmax_notification_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE);

        if(fullScreenIntent) {
            builder.setFullScreenIntent(pendingIntent, true);
        }

        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }


    /**
     * cancelNotification method cancels a notification with the specified notificationId.
     * @param context The context of the application.
     * @param notificationId The unique identifier for the notification to be cancelled.
     */
    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    /**
     * isNotificationActive method checks if a notification with the specified notificationId is active.
     * @param context The context of the application.
     * @param notificationId The unique identifier for the notification to check.
     * @return True if the notification is active, otherwise false.
     */
    public static boolean isNotificationActive(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

        for (StatusBarNotification activeNotification : activeNotifications) {
            if (activeNotification.getId() == notificationId) {
                return true;
            }
        }
        return false;
    }
}
