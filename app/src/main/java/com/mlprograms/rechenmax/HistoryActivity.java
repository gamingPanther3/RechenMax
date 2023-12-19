package com.mlprograms.rechenmax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * HistoryActivity
 * @author Max Lemberg
 * @version 1.0.0
 * @date 03.12.2023
 */
public class HistoryActivity extends AppCompatActivity {

    // Declare a Context object and initialize it to this instance
    private final Context context = this;
    // Declare a DataManager object
    DataManager dataManager;
    // Declare a static MainActivity object
    @SuppressLint("StaticFieldLeak")
    private static MainActivity mainActivity;

    // Define the name of the File
    private static final String FILE_NAME = "history.txt";

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        TextView history_text_view = findViewById(R.id.history_textview);
        history_text_view.setText(loadHistory(getApplicationContext()));

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switchDisplayMode(currentNightMode);
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
    }

    /**
     * This method is called when the back button is pressed.
     * It overrides the default behavior and returns to the calculator.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnToCalculator();
    }

    /**
     * Perform any final cleanup before an activity is destroyed.
     */
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null && dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()).equals("true")) {
            dataManager.saveToJSON("disablePatchNotesTemporary", "false", getApplicationContext());
        }
        finish();
    }

    /**
     * This method switches the display mode based on the current night mode.
     * @param currentNightMode The current night mode.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchDisplayMode(int currentNightMode) {
        @SuppressLint("CutPasteId") Button deleteButton = findViewById(R.id.history_delete_button);
        @SuppressLint("CutPasteId") Button returnButton = findViewById(R.id.history_return_button);
        ScrollView historyScrollView = findViewById(R.id.history_scroll_textview);
        TextView historyTextView = findViewById(R.id.history_textview);
        TextView historyTitle = findViewById(R.id.history_title);
        @SuppressLint("CutPasteId") TextView historyReturnButton = findViewById(R.id.history_return_button);
        @SuppressLint("CutPasteId") TextView historyDeleteButton = findViewById(R.id.history_delete_button);

        int newColorBTNForegroundAccent;
        int newColorBTNBackgroundAccent;

        dataManager = new DataManager();
        final String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());
        if (getSelectedSetting() != null && getSelectedSetting().equals("Systemstandard")) {
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_YES:
                    if(trueDarkMode != null && trueDarkMode.equals("true")) {
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);

                        deleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24_true_darkmode));
                        returnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));

                        updateUIAccordingToNightMode(historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                        deleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24_light));
                        returnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));

                        updateUIAccordingToNightMode(historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    }
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);

                    updateUIAccordingToNightMode(historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

                    historyReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
                    historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24));
                    break;
            }
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Tageslichtmodus"))  {
            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
            updateUIAccordingToNightMode(historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

            historyReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
            historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24));
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Dunkelmodus")) {
            if(trueDarkMode != null && trueDarkMode.equals("true")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);

                deleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24_true_darkmode));
                returnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));

                updateUIAccordingToNightMode(historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                updateUIAccordingToNightMode(historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            } else {
                historyReturnButton.setForeground(getDrawable(R.drawable.baseline_history_24_light));
                historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_settings_24_light));
            }
        }
    }

    /**
     * This method updates the UI elements according to the night mode.
     * @param historyScrollView The history scroll view.
     * @param historyTextView The history text view.
     * @param historyTitle The history title.
     * @param newColorBTNForegroundAccent The new color for the button foreground accent.
     * @param newColorBTNBackgroundAccent The new color for the button background accent.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUIAccordingToNightMode(ScrollView historyScrollView, TextView historyTextView, TextView historyTitle, int newColorBTNForegroundAccent, int newColorBTNBackgroundAccent) {
        if (historyScrollView != null) {
            historyScrollView.setBackgroundColor(newColorBTNBackgroundAccent);
        }
        if (historyTextView != null) {
            historyTextView.setTextColor(newColorBTNForegroundAccent);
        }
        if (historyTitle != null) {
            historyTitle.setTextColor(newColorBTNForegroundAccent);
        }
        // Change the foreground and background colors of all buttons in your layout
        if (newColorBTNForegroundAccent != 0 && newColorBTNBackgroundAccent != 0) {
            changeButtonColors(findViewById(R.id.historyUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
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
     * This method deletes the history.
     * @param context The application context.
     */
    public void deleteHistory(Context context) {
        try {
            FileOutputStream fileOut = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileOut);
            outputWriter.write("");
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView history_text_view = findViewById(R.id.history_textview);
        history_text_view.setText(loadHistory(getApplicationContext()));
    }

    /**
     * This method loads the history from a file.
     * @param context The application context.
     * @return The history as a string.
     */
    public String loadHistory(Context context) {
        StringBuilder result = new StringBuilder();
        try {
            FileInputStream fileIn = context.openFileInput(FILE_NAME);
            InputStreamReader inputReader = new InputStreamReader(fileIn);
            char[] inputBuffer = new char[100];
            int charRead;
            while ((charRead = inputReader.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                result.append(readString);
            }
            inputReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView history_text_view = findViewById(R.id.history_textview);
        if (result.toString().equals("")) {
            history_text_view.setTextSize(40);
            history_text_view.setGravity(Gravity.CENTER_HORIZONTAL);
            return "\n\n\n\n\n\nDein Verlauf ist leer.";
        } else {
            history_text_view.setTextSize(45);
            history_text_view.setGravity(Gravity.END);
            return result.toString();
        }
    }

    /**
     * This static method sets the context of the MainActivity.
     * @param activity The MainActivity whose context is to be set.
     */
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
     * This method gets the selected setting.
     * @return The selected setting.
     */
    public String getSelectedSetting() {
        if(dataManager != null) {
            final String setting = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());
            if(setting != null) {
                switch (setting) {
                    case "System":
                        return "Systemstandard";
                    case "Dark":
                        return "Dunkelmodus";
                    case "Light":
                        return "Tageslichtmodus";
                }
            }
        }
        return null;
    }

    /**
     * This method handles button clicks.
     * @param view The view that was clicked.
     */
    public void ButtonListener2(View view) {
        if (view.getTag().equals("return")) {
            returnToCalculator();
        } else if (view.getTag().equals("delete")) {
            deleteHistory(getApplicationContext());
        }
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     * It also switches the display mode based on the current night mode.
     */
    public void returnToCalculator() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }
}