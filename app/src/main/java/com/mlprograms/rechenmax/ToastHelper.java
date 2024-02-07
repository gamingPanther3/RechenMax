package com.mlprograms.rechenmax;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {
    /**
     * This method displays a toast on the screen.
     * It retrieves the context of the current application and sets the duration of the toast to short.
     * A toast with the message "Rechnung wurde übernommen ..." is created and displayed.
     */
    public static void showToastLong(final String text, Context context) {
        int duration = Toast.LENGTH_LONG;

        // Create and show the toast
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * This method displays a toast on the screen.
     * It retrieves the context of the current application and sets the duration of the toast to short.
     * A toast with the message "Rechnung wurde übernommen ..." is created and displayed.
     */
    public static void showToastShort(final String text, Context context) {
        int duration = Toast.LENGTH_SHORT;

        // Create and show the toast
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
