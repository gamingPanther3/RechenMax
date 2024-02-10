package com.mlprograms.rechenmax;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

/**
 * ClickListener class implements View.OnClickListener and View.OnLongClickListener interfaces to handle click and long click events.
 */
public class ClickListener implements View.OnClickListener, View.OnLongClickListener {
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private static final long LONG_CLICK_THRESHOLD = 500;
    private long lastClickTime = 0;
    private long lastDownTime = 0;
    private final Handler handler = new Handler();
    private int clickCount = 0;

    /**
     * onClick method handles click events.
     * It detects single and double clicks and triggers corresponding actions.
     */
    @Override
    public void onClick(final View v) {
        long clickTime = System.currentTimeMillis();

        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            // Double click detected
            clickCount++;
            if (clickCount == 2) {
                onDoubleClick(v);
                clickCount = 0;
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

    /**
     * onLongClick method handles long click events.
     * It detects long clicks and triggers corresponding actions.
     */
    @Override
    public boolean onLongClick(View v) {
        long currentDownTime = System.currentTimeMillis();
        if (currentDownTime - lastDownTime >= LONG_CLICK_THRESHOLD) {
            onLongClickEvent(v);
        }
        lastDownTime = currentDownTime;
        return true;
    }

    /**
     * onLongClickEvent method is called when a long click event occurs.
     * It can be overridden to provide specific behavior for long clicks.
     */
    public void onLongClickEvent(View v) {}

    /**
     * onLongClickEvent method is called when a long click event occurs on a TextView.
     * It can be overridden to provide specific behavior for long clicks on TextViews.
     */
    public void onLongClickEvent(TextView v) {}

    /**
     * onDoubleClick method is called when a double click event occurs.
     * It can be overridden to provide specific behavior for double clicks.
     */
    public void onDoubleClick(View v) {}

    /**
     * onLongClick method is called when a long click event occurs.
     * It can be overridden to provide specific behavior for long clicks.
     */
    public void onLongClick(TextView v) {}

    /**
     * onSingleClick method is called when a single click event occurs.
     * It can be overridden to provide specific behavior for single clicks.
     */
    public void onSingleClick(View v) {}

    /**
     * onDoubleClick method is called when a double click event occurs.
     * It can be overridden to provide specific behavior for double clicks on TextViews.
     */
    public void onDoubleClick(TextView v) {}

    /**
     * onSingleClick method is called when a single click event occurs.
     * It can be overridden to provide specific behavior for single clicks on TextViews.
     */
    public void onSingleClick(TextView v) {}
}
