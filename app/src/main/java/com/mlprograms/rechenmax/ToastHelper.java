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

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
































