package com.mlprograms.rechenmax;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "RechenMax";
    private static final String CHANNEL_NAME = "Erinnerung";

    public static void showNotification(Context context, String title, String content) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Erstelle NotificationChannel für Android-Versionen ab Oreo (API-Level 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Erstelle eine NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Zeige die Benachrichtigung an
        int notificationId = 1; // Eindeutige ID für die Benachrichtigung
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }
}
