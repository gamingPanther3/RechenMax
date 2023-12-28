package com.mlprograms.rechenmax;

import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * HistoryActivity - Displays calculation history.
 * @author Max Lemberg
 * @version 1.4.4
 * @date 27.12.2023
 */
public class HistoryActivity extends AppCompatActivity {

    // Declare a Context object and initialize it to this instance
    private final Context context = this;
    // Declare a DataManager object
    DataManager dataManager;
    // Declare a static MainActivity object
    @SuppressLint("StaticFieldLeak")
    private static MainActivity mainActivity;

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.
     *     Otherwise, it is null.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        // Initialize DataManager
        dataManager = new DataManager();

        System.out.println("Opened HistoryActivity.java with history.xml");

        // Create an instance of the outer LinearLayout
        LinearLayout outerLinearLayout = findViewById(R.id.history_scroll_linearlayout);

        // Create an instance of the inner LinearLayout
        LinearLayout innerLinearLayout = new LinearLayout(this);
        innerLinearLayout.setOrientation(LinearLayout.VERTICAL);

        // Set the inner LinearLayout as the content of the outer LinearLayout
        outerLinearLayout.addView(innerLinearLayout);

        // Use a separate thread to create TextViews in the background
        new Thread(() -> {
            final String value = dataManager.readFromJSON("historyTextViewNumber", getMainActivityContext());
            if (value == null) {
                dataManager.saveToJSON("historyTextViewNumber", "0", getApplicationContext());
            }

            runOnUiThread(() -> {
                if (value != null && value.equals("0")) {
                    createEmptyHistoryTextView();
                    switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
                } else {
                    deleteEmptyHistoryTextView();
                    for (int i = 1; i < Integer.parseInt(value) + 1; i++) {
                        // Create a new TextView
                        TextView textView = createHistoryTextView(dataManager.readFromJSON(String.valueOf(i), getMainActivityContext()));

                        // Create a thin line
                        View line = createLine();

                        // Add TextView and line to the inner LinearLayout
                        innerLinearLayout.addView(textView, 0);
                        innerLinearLayout.addView(line, 1);
                    }
                    switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
                }
            });
        }).start();
    }

    /**
     * Creates a new TextView for displaying calculation history.
     *
     * @param text The text content of the TextView.
     * @return The created TextView.
     */
    private TextView createHistoryTextView(String text) {
        TextView textView = new TextView(this);

        // Create layout parameters for the TextView
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textLayoutParams.setMargins(
                getResources().getDimensionPixelSize(R.dimen.history_margin_left),
                getResources().getDimensionPixelSize(R.dimen.history_margin_top),
                getResources().getDimensionPixelSize(R.dimen.history_margin_right),
                getResources().getDimensionPixelSize(R.dimen.history_margin_bottom)
        );

        // Apply layout parameters to the TextView
        textView.setLayoutParams(textLayoutParams);

        // Set additional TextView properties
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        textView.setGravity(Gravity.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.history_result_size));

        // Add a click listener to the TextView
        textView.setOnClickListener(v -> {
            // Output the text of the clicked TextView to the console
            TextView clickedTextView = (TextView) v;
            String clickedText = clickedTextView.getText().toString();

            // Split at "=" character
            String[] parts = clickedText.split("=");

            // Check and remove leading and trailing spaces
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                System.out.println("Key: '" + key + "'");
                System.out.println("Value: '" + value + "'");
                try {
                    dataManager.saveToJSON("calculate_text", key, getMainActivityContext());
                    dataManager.saveToJSON("result_text", value, getMainActivityContext());
                    dataManager.saveToJSON("removeValue", false, getMainActivityContext());
                    dataManager.saveToJSON("rotate_op", true, getMainActivityContext());
                    showToast("Rechnung wurde übernommen ...");
                } catch (Exception e) {
                    Log.i("createHistoryTextView", String.valueOf(e));
                }
            }
        });

        return textView;
    }

    private void showToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        // Create and show the toast
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    /**
     * Creates a thin line view for visual separation in the history.
     *
     * @return The created line view.
     */
    private View createLine() {
        // Create a thin line
        View line = new View(this);
        LinearLayout.LayoutParams lineLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.history_line_height)
        );

        // Set the margin for horizontal spacing on the right and left to 10dp
        lineLayoutParams.setMargins(
                getResources().getDimensionPixelSize(R.dimen.history_line_margin_horizontal),
                0,
                getResources().getDimensionPixelSize(R.dimen.history_line_margin_horizontal),
                0
        );

        line.setLayoutParams(lineLayoutParams);
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.history_line_color));

        // Add an OnConfigurationChangedListener to update line color on configuration changes
        line.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            updateLineColor(line);
        });

        return line;
    }

    /**
     * Updates the color of the line based on the current night mode.
     *
     * @param line The line view to update.
     */
    private void updateLineColor(View line) {
        int lineColor = 0;
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        dataManager = new DataManager();

        String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

        if (getSelectedSetting().equals("Systemstandard")) {
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                if ("true".equals(trueDarkMode)) {
                    lineColor = ContextCompat.getColor(this, R.color.darkmode_white);
                } else {
                    lineColor = ContextCompat.getColor(this, R.color.white);
                }
            } else {
                lineColor = ContextCompat.getColor(this, android.R.color.black);
            }
        } else if (getSelectedSetting().equals("Tageslichtmodus")) {
            lineColor = ContextCompat.getColor(this, android.R.color.black);
        } else {
            lineColor = ContextCompat.getColor(this, android.R.color.white);
        }

        // Set the updated color
        line.setBackgroundColor(lineColor);
    }

    private void createEmptyHistoryTextView() {
        // Überprüfen Sie, ob die TextView bereits vorhanden ist, und entfernen Sie sie ggf.
        deleteEmptyHistoryTextView();

        // Erstellen Sie die leere TextView
        TextView emptyTextView = createHistoryTextView("\n\n\n\n\n\n\nDein Verlauf ist leer.");
        emptyTextView.setId(R.id.history_empty_textview); // Fügen Sie die ID hinzu

        // Setzen Sie die Textfarbe basierend auf dem aktuellen Nachtmodus und true dark mode
        int textColor = calculateTextColor();
        emptyTextView.setTextColor(textColor);

        // Setzen Sie die Textgröße und die Schwerkraft
        emptyTextView.setTextSize(35f);
        emptyTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        // Fügen Sie die TextView zum Layout hinzu
        final LinearLayout linearLayout = findViewById(R.id.history_scroll_linearlayout);
        linearLayout.addView(emptyTextView);
    }

    /**
     * Diese Methode entfernt die leere TextView aus dem Layout, falls vorhanden.
     */
    private void deleteEmptyHistoryTextView() {
        // Finden Sie die TextView im Layout und entfernen Sie sie
        final LinearLayout linearLayout = findViewById(R.id.history_scroll_linearlayout);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View child = linearLayout.getChildAt(i);
            if (child instanceof TextView && child.getId() == R.id.history_empty_textview) {
                linearLayout.removeView(child);
                break;
            }
        }
    }

    /**
     * Diese Methode berechnet die Textfarbe basierend auf dem aktuellen Nachtmodus und dem true dark mode.
     * @return Die berechnete Textfarbe.
     */
    private int calculateTextColor() {
        int textColor;
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        dataManager = new DataManager();

        String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

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

        return textColor;
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

        String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

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

        // Änderung: Führe UI-Änderungen auf dem UI-Thread aus
        final int finalTextColor = textColor;
        runOnUiThread(() -> textView.setTextColor(finalTextColor));
    }

    /**
     * Resets the names and values in the UI and performs background actions.
     */
    private void resetNamesAndValues() {
        // Update the UI
        runOnUiThread(this::updateUI);
        LinearLayout innerLinearLayout = findViewById(R.id.history_scroll_linearlayout);
        innerLinearLayout.removeAllViews();
        createEmptyHistoryTextView();

        // Perform background actions here
        new Thread(() -> {
            int historyTextViewNumber = Integer.parseInt(dataManager.readFromJSON("historyTextViewNumber", getApplicationContext()));

            // Prepare stack for batch operations
            List<String> keysToRemove = new ArrayList<>();
            for (int i = 1; i <= historyTextViewNumber; i++) {
                keysToRemove.add(String.valueOf(i));
            }

            // Batch operations for database access
            for (String key : keysToRemove) {
                dataManager.saveToJSON(key, null, getApplicationContext());
            }
            dataManager.saveToJSON("historyTextViewNumber", "0", getApplicationContext());
        }).start();
    }


    /**
     * Updates the UI by creating and adding TextViews for each history entry.
     */
    private void updateUI() {
        // Perform UI updates here
        LinearLayout innerLinearLayout = findViewById(R.id.history_scroll_linearlayout);
        innerLinearLayout.removeAllViews();
        createEmptyHistoryTextView();

        final String value = dataManager.readFromJSON("historyTextViewNumber", getMainActivityContext());
        if (value != null && !value.equals("0")) {
            for (int i = 1; i < Integer.parseInt(value) + 1; i++) {
                // Create a new TextView
                TextView textView = createHistoryTextView(dataManager.readFromJSON(String.valueOf(i), getMainActivityContext()));

                // Add the TextView to the inner LinearLayout
                innerLinearLayout.addView(textView);

                // Create a thin line and add it to the inner LinearLayout
                innerLinearLayout.addView(createLine());
            }
        }
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
     * This method is called when the back button is pressed.
     * It overrides the default behavior and returns to the calculator.
     */
    @Override
    public void onBackPressed() {
        returnToCalculator();
    }

    /**
     * Perform any final cleanup before an activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null && dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()).equals("true")) {
            dataManager.saveToJSON("disablePatchNotesTemporary", "false", getApplicationContext());
        }
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

                        updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                        deleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24_light));
                        returnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));

                        updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    }
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);

                    updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

                    historyReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
                    historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24));
                    break;
            }
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Tageslichtmodus"))  {
            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
            updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

            historyReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
            historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24));
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Dunkelmodus")) {
            if(trueDarkMode != null && trueDarkMode.equals("true")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);

                deleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24_true_darkmode));
                returnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));

                updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                deleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24_light));
                returnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));

                updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            } else {
                historyReturnButton.setForeground(getDrawable(R.drawable.baseline_history_24_light));
                historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_settings_24_light));
            }
        }
    }

    /**
     * This method updates the UI elements according to the night mode.
     *
     * @param historyScrollView           The history scroll view.
     * @param historyTextView             The history text view.
     * @param newColorBTNForegroundAccent The new color for the button foreground accent.
     * @param newColorBTNBackgroundAccent The new color for the button background accent.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUIAccordingToNightMode(ScrollView historyScrollView, TextView historyTextView, int newColorBTNForegroundAccent, int newColorBTNBackgroundAccent) {
        if (historyScrollView != null) {
            historyScrollView.setBackgroundColor(newColorBTNBackgroundAccent);
        }
        if (historyTextView != null) {
            historyTextView.setTextColor(newColorBTNForegroundAccent);
        }

        // Change the foreground and background colors of all buttons in your layout
        if (newColorBTNForegroundAccent != 0 && newColorBTNBackgroundAccent != 0) {
            changeButtonColors(findViewById(R.id.historyUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeTextViewColors(newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
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
     * @param foregroundColor The color to be set as the text color of the TextViews.
     *                        This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the TextViews and the layout.
     *                        This should be a resolved color, not a resource id.
     */
    private void changeTextViewColors(int foregroundColor, int backgroundColor) {
        ViewGroup layout = findViewById(R.id.historyUI).findViewById(R.id.history_scroll_linearlayout);
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

    private void changeTextViewColorsRecursive(ViewGroup layout, int foregroundColor, int backgroundColor) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
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
     * This method is used to get the selected setting.
     * It reads the selected setting from the JSON file and returns the corresponding setting.
     * @return If the selected setting is "System", it returns "Systemstandard".
     *         If the selected setting is "Dark", it returns "Dunkelmodus".
     *         If the selected setting is "Light", it returns "Tageslichtmodus".
     *         If no setting is selected, it returns null.
     */
    public String getSelectedSetting() {
        final String setting = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());
        if (setting != null) {
            switch (setting) {
                case "System":
                    return "Systemstandard";
                case "Dark":
                    return "Dunkelmodus";
                case "Light":
                    return "Tageslichtmodus";
                default:
                    // Handle unexpected settings or return a default value
                    return "Systemstandard";
            }
        }
        // Handle the case when the setting is not found in the JSON file
        return "Systemstandard";
    }


    /**
     * This method handles button clicks.
     * @param view The view that was clicked.
     */
    public void ButtonListener2(View view) {
        if (view.getTag().equals("return")) {
            returnToCalculator();
        } else if (view.getTag().equals("delete")) {
            resetNamesAndValues();
        }
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     * It also switches the display mode based on the current night mode.
     */
    public void returnToCalculator() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            //switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}