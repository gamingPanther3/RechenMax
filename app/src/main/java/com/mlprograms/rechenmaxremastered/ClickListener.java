package com.mlprograms.rechenmaxremastered;

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

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

/**
 * ClickListener class implements View.OnClickListener and View.OnLongClickListener interfaces to handle click and long click events.
 */
public class ClickListener implements View.OnClickListener, View.OnLongClickListener {
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private static final long TRIPLE_CLICK_TIME_DELTA = 1000;
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

        if (clickTime - lastClickTime < TRIPLE_CLICK_TIME_DELTA) {
            // Within triple click time window
            clickCount++;

            if (clickCount == 2) {
                handler.removeCallbacksAndMessages(null); // Remove pending single click action
                onDoubleClick(v);
            } else if (clickCount == 3) {
                handler.removeCallbacksAndMessages(null); // Remove pending double click action
                onTripleClick(v);
                clickCount = 0; // Reset click count after triple click
            }
        } else {
            // Single click or click after triple click window
            clickCount = 1;
            handler.postDelayed(() -> {
                if (clickCount == 1) {
                    onSingleClick(v);
                }
                clickCount = 0; // Reset click count after single click timeout
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

    public void onTripleClick(View v) {}

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

    /**
     * onClickAndHold method is called when a click and hold event occurs.
     * It can be overridden to provide specific behavior for click and hold events.
     */
    public void onClickAndHold(View v) {}
}
