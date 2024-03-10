package com.mlprograms.rechenmax;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;

public class HelpActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static Context mainActivity;
    private final Context context = this;

    /**
     * Instance of DataManager to handle data-related tasks such as saving and retrieving data.
     */
    private DataManager dataManager = new DataManager();

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.
     *     Otherwise, it is null.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopBackgroundService();

        setContentView(R.layout.help);

        @SuppressLint("CutPasteId") Button button = findViewById(R.id.help_return_button);
        button.setOnClickListener(v -> {
            try {
                if(dataManager.getJSONSettingsData("returnToCalculator", getMainActivityContext()).getString("value").equals("true")) {
                    dataManager.saveToJSONSettings("returnToCalculator", "false", getApplicationContext());
                    returnToCalculator();
                } else {
                    returnToSettings();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    /**
     * This method is used to switch the display mode of the application based on the user's selected setting.
     * It first initializes the UI elements and gets the current night mode status and the user's selected setting.
     * If a setting is selected, it switches the display mode based on the selected setting.
     * If no setting is selected, it saves "System" as the selected setting and calls itself again.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchDisplayMode(int currentNightMode) {
        // Global variables
        TextView returnButton = findViewById(R.id.help_return_button);
        Button helpButton = findViewById(R.id.help_button);

        int newColorBTNBackgroundAccent = 0;
        int newColorBTNForegroundAccent = 0;

        // Retrieving theme setting
        String selectedSetting = getSelectedSetting();
        // Updating UI elements
        final String trueDarkMode;
        try {
            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getApplicationContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (selectedSetting != null) {
            switch (selectedSetting) {
                case "Systemstandard":
                    switch (currentNightMode) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            if (returnButton != null) {
                                returnButton.setForeground(getDrawable(R.drawable.arrow_back_light));
                            }
                            if (helpButton != null) {
                                helpButton.setForeground(getDrawable(R.drawable.arrow_back_light));
                            }

                            if (trueDarkMode.equals("true")) {
                                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                                if (returnButton != null) {
                                    returnButton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                                }
                                if (helpButton != null) {
                                    helpButton.setForeground(getDrawable(R.drawable.help_true_darkmode));
                                }

                            } else if (trueDarkMode.equals("false")) {
                                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);
                            }
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                            if (returnButton != null) {
                                returnButton.setForeground(getDrawable(R.drawable.arrow_back));
                            }
                            if (helpButton != null) {
                                helpButton.setForeground(getDrawable(R.drawable.help));
                            }
                            break;
                    }
                    break;
                case "Tageslichtmodus":
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                    if (returnButton != null) {
                        returnButton.setForeground(getDrawable(R.drawable.arrow_back));
                    }
                    if (helpButton != null) {
                        helpButton.setForeground(getDrawable(R.drawable.help));
                    }

                    break;
                case "Dunkelmodus":
                    dataManager = new DataManager(this);
                    if (returnButton != null) {
                        returnButton.setForeground(getDrawable(R.drawable.arrow_back_light));
                    }
                    if (helpButton != null) {
                        helpButton.setForeground(getDrawable(R.drawable.help_light));
                    }

                    if (trueDarkMode != null) {
                        if (trueDarkMode.equals("false")) {
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                        } else {
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);

                            if (returnButton != null) {
                                returnButton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                            }
                            if (helpButton != null) {
                                helpButton.setForeground(getDrawable(R.drawable.help_true_darkmode));
                            }
                        }
                    } else {
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                    }
                    break;
            }

            // Updating UI elements
            changeTextViewColors(newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeButtonColors(findViewById(R.id.helpUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
        } else {
            dataManager.saveToJSONSettings("selectedSpinnerSetting", "System", getApplicationContext());
            switchDisplayMode(currentNightMode);
        }
    }

    /**
     * This method is used to get the selected setting.
     * It reads the selected setting from the JSON file and returns the corresponding setting.
     * @return If the selected setting is "System", it returns "Systemstandard".
     *         If the selected setting is "Dark", it returns "Dunkelmodus".
     *         If the selected setting is "Light", it returns "Tageslichtmodus".
     *         If no setting is selected, it returns null.
     */
    public String getSelectedSetting() {
        final String setting;
        try {
            setting = dataManager.getJSONSettingsData("selectedSpinnerSetting", getApplicationContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        switch (setting) {
            case "System":
                return "Systemstandard";
            case "Dark":
                return "Dunkelmodus";
            case "Light":
                return "Tageslichtmodus";
        }
        return "Systemstandard";
    }

    /**
     * This method is called when the configuration of the device changes.
     * @param newConfig The new device configuration.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switchDisplayMode(currentNightMode);

        // Rufen Sie die Methode auf, um die Textfarbe zu aktualisieren, nur wenn die TextView vorhanden ist
        final TextView emptyHistoryTextView = findViewById(R.id.history_empty_textview);
        if (emptyHistoryTextView != null) {
            updateTextViewColor(emptyHistoryTextView);
        }
    }

    /**
     * Diese Methode aktualisiert die Textfarbe des TextView basierend auf dem aktuellen Nachtmodus und den Einstellungen.
     *
     * @param textView Der TextView, dessen Textfarbe aktualisiert werden soll.
     */
    private void updateTextViewColor(TextView textView) {
        dataManager = new DataManager();

        int textColor;
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        String trueDarkMode = null;
        try {
            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (getSelectedSetting().equals("Systemstandard")) {
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                if ("true".equals(trueDarkMode)) {
                    textColor = ContextCompat.getColor(this, R.color.darkmode_white);
                } else {
                    textColor = ContextCompat.getColor(this, R.color.white);
                }
            } else {
                textColor = ContextCompat.getColor(this, android.R.color.black);
            }
        } else if (getSelectedSetting().equals("Tageslichtmodus")) {
            textColor = ContextCompat.getColor(this, android.R.color.black);
        } else {
            textColor = ContextCompat.getColor(this, android.R.color.white);
        }

        final int finalTextColor = textColor;
        runOnUiThread(() -> textView.setTextColor(finalTextColor));
    }

    /**
     * This method is called when the back button is pressed.
     * It overrides the default behavior and returns to the calculator.
     */
    @Override
    public void onBackPressed() {
        try {
            if(dataManager.getJSONSettingsData("returnToCalculator", getMainActivityContext()).getString("value").equals("true")) {
                dataManager.saveToJSONSettings("returnToCalculator", "false", getApplicationContext());
                returnToCalculator();
            } else {
                returnToSettings();
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is called when the activity is destroyed.
     * It checks if "disablePatchNotesTemporary" is true in the JSON file, and if so, it saves "disablePatchNotesTemporary" as false in the JSON file.
     * It then calls the finish() method to close the activity.
     */
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (dataManager.getJSONSettingsData("disablePatchNotesTemporary", getApplicationContext()).getString("value").equals("true")) {
                dataManager.saveToJSONSettings("disablePatchNotesTemporary", false, getApplicationContext());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * onPause method is called when the activity is paused.
     * It starts the background service.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startBackgroundService();
        }
    }

    /**
     * onResume method is called when the activity is resumed.
     * It stops the background service.
     */
    @Override
    protected void onResume() {
        super.onResume();
        stopBackgroundService();
    }

    /**
     * This method stops the background service.
     * It creates an intent to stop the BackgroundService and calls stopService() with that intent.
     * This method is typically called when the activity is being destroyed or when it's no longer necessary to run the background service.
     */
    private void stopBackgroundService() {
        try {
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            stopService(serviceIntent);
        } catch (Exception e) {
            Log.e("stopBackgroundService", e.toString());
        }
    }

    /**
     * This method starts a background service if the necessary permission is granted.
     * It checks if the app has the required permission to post notifications.
     * If the permission is granted, it starts the BackgroundService.
     * This method is typically called when the window loses focus.
     */
    private void startBackgroundService() {
        stopBackgroundService();
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(this, BackgroundService.class));
            }
        } catch (Exception e) {
            Log.e("startBackgoundService", e.toString());
        }
    }

    /**
     * This method changes the colors of all buttons in a layout.
     * @param layout The layout containing the buttons.
     * @param foregroundColor The color to be used for the foreground.
     * @param backgroundColor The color to be used for the background.
     */
    private void changeButtonColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);

                // If the child is a Button, change the foreground and background colors
                if (v instanceof Button) {
                    ((Button) v).setTextColor(foregroundColor);
                    v.setBackgroundColor(backgroundColor);
                }
                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                else if (v instanceof ViewGroup) {
                    changeButtonColors((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }

    /**
     * This method is used to change the colors of the TextViews in a given layout.
     *
     * @param foregroundColor The color to be set as the text color of the TextViews.
     *                        This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the TextViews and the layout.
     *                        This should be a resolved color, not a resource id.
     */
    private void changeTextViewColors(int foregroundColor, int backgroundColor) {
        ViewGroup layout = findViewById(R.id.helpUI);
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);

                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                if (v instanceof ViewGroup) {
                    changeTextViewColorsRecursive((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }

    /**
     * This recursive method traverses a ViewGroup, typically a layout, and changes the foreground
     * and background colors of each child View. It takes as parameters the ViewGroup to traverse,
     * the new foreground color, and the new background color. For each child View, it sets the
     * background color and, if the child is a TextView, it also changes the text color. If the child
     * is itself a ViewGroup, the method is called recursively to ensure all nested Views are
     * processed.
     * <p>
     * Note: This method is useful for applying a consistent color scheme to all child Views within
     * a given layout, and it can be used, for example, to implement a dark mode or change the
     * appearance of a specific section of the UI.
     *
     * @param layout The ViewGroup to traverse and update colors.
     * @param foregroundColor The new color for the foreground (text color) of TextViews.
     * @param backgroundColor The new color for the background of Views.
     */
    private void changeTextViewColorsRecursive(ViewGroup layout, int foregroundColor, int backgroundColor) {
        // Iterate through each child View in the ViewGroup
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);

            // Set the background color for the current child View
            v.setBackgroundColor(backgroundColor);

            // If the child is a TextView, change the foreground and background colors
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(foregroundColor);
                v.setBackgroundColor(backgroundColor);
            }

            // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
            if (v instanceof ViewGroup) {
                changeTextViewColorsRecursive((ViewGroup) v, foregroundColor, backgroundColor);
            }
        }
    }

    /**
     * This static method sets the context of the MainActivity.
     * @param activity The MainActivity whose context is to be set.
     */
    public static void setMainActivityContext(SettingsActivity activity) {
        mainActivity = activity;
    }

    public static void setMainActivityContext(MainActivity activity) {
        mainActivity = activity;
    }

    /**
     * This method gets the context of the MainActivity.
     * @return The context of the MainActivity.
     */
    public Context getMainActivityContext() {
        return mainActivity;
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     * It also switches the display mode based on the current night mode.
     */
    public void returnToSettings() {
        try {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     */
    public void returnToCalculator() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
