package com.mlprograms.rechenmax;

import android.app.Notification;
import android.app.NotificationChannel;
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
     * @param CHANNEL_NAME The name of the notification channel.
     */
    public static void sendNotification(Context context, int notificationId, String title, String content, String CHANNEL_ID, String CHANNEL_NAME) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

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
