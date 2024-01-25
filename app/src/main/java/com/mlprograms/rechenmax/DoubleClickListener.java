package com.mlprograms.rechenmax;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class DoubleClickListener implements View.OnClickListener {
    private static final long DOUBLE_CLICK_TIME_DELTA = 200; // Zeitintervall zwischen Klicks
    private long lastClickTime = 0;
    private final Handler handler = new Handler();
    private int clickCount = 0;

    @Override
    public void onClick(final View v) {
        long clickTime = System.currentTimeMillis();

        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            // Double click detected
            clickCount++;
            if (clickCount == 2) {
                onDoubleClick(v);
                clickCount = 0; // Zurücksetzen, um auf den nächsten Doppelklick zu warten
            }
        } else {
            // Single click
            clickCount = 1;
            handler.postDelayed(() -> {
                if (clickCount == 1) {
                    onSingleClick(v);
                }
                clickCount = 0;
            }, DOUBLE_CLICK_TIME_DELTA);
        }

        lastClickTime = clickTime;
    }

    public void onDoubleClick(View v) {}

    public void onDoubleClick(TextView v) {}

    public void onSingleClick(View v) {}
}
