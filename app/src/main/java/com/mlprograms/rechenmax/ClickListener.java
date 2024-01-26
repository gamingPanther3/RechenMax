package com.mlprograms.rechenmax;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ClickListener implements View.OnClickListener, View.OnLongClickListener {
    private static final long DOUBLE_CLICK_TIME_DELTA = 200;
    private static final long LONG_CLICK_THRESHOLD = 500;
    private long lastClickTime = 0;
    private long lastDownTime = 0;
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

    @Override
    public boolean onLongClick(View v) {
        long currentDownTime = System.currentTimeMillis();
        if (currentDownTime - lastDownTime >= LONG_CLICK_THRESHOLD) {
            onLongClickEvent(v);
        }
        lastDownTime = currentDownTime;
        return true;
    }

    public void onLongClickEvent(View v) {}

    public void onLongClickEvent(TextView v) {}

    public void onDoubleClick(View v) {}

    public void onLongClick(TextView v) {}

    public void onSingleClick(View v) {}

    public void onDoubleClick(TextView v) {}

    public void onSingleClick(TextView v) {}
}
