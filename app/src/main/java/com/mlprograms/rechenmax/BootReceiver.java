package com.mlprograms.rechenmax;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BootReceiver class extends BroadcastReceiver and handles system boot events.
 * It starts the BackgroundService when the device boots up.
 */
public class BootReceiver extends BroadcastReceiver {

    /**
     * onReceive method called when a system broadcast is received.
     * Starts the BackgroundService when the device boots up.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            Intent serviceIntent = new Intent(context, BackgroundService.class);
            serviceIntent.putExtra("started_by_boot_receiver", true);
            context.startService(serviceIntent);
        }
    }
}
