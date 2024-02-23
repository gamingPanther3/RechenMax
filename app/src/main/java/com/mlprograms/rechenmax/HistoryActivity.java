package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.ToastHelper.*;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    private LinearLayout innerLinearLayout;

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

        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);

        Button historyReturnButton = findViewById(R.id.history_return_button);
        Button historyDeleteButton = findViewById(R.id.history_delete_button);

        historyReturnButton.setOnClickListener(v -> returnToCalculator());
        historyDeleteButton.setOnClickListener(v -> resetNamesAndValues());

        // Create an instance of the outer LinearLayout
        LinearLayout outerLinearLayout = findViewById(R.id.history_scroll_linearlayout);

        // Create an instance of the inner LinearLayout
        innerLinearLayout = new LinearLayout(this);
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
                    for (int i = 1; i < Integer.parseInt(Objects.requireNonNull(value)) + 1; i++) {
                        if(dataManager.readFromJSON(String.valueOf(i), getMainActivityContext()) != null) {
                            // Create a new TextView
                            TextView textView = createHistoryTextView(dataManager.readFromJSON(String.valueOf(i), getMainActivityContext()));
                            textView.setId(i);

                            // Create a thin line
                            View line = createLine();

                            // Add TextView and line to the inner LinearLayout
                            innerLinearLayout.addView(textView, 0);
                            innerLinearLayout.addView(line, 1);
                        }
                    }
                    //innerLinearLayout.removeViewAt(innerLinearLayout.getChildCount() - 1);
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
        AtomicBoolean clickListener = new AtomicBoolean(true);

        TextView emptyTextView = findViewById(R.id.history_empty_textview);
        if(emptyTextView != null) {
            emptyTextView.setVisibility(View.GONE);
        }

        textView.setOnLongClickListener(v -> {
            try {
                TextView clickedTextView = (TextView) v;
                String clickedText = clickedTextView.getText().toString();

                // Get the system clipboard manager
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                // Create a ClipData with plain text representing the result text
                ClipData clipData = ClipData.newPlainText("", clickedText.replace("\n", " ") );

                // Set the created ClipData as the primary clip on the clipboard
                clipboardManager.setPrimaryClip(clipData);

                // Display a toast indicating that the data has been saved
                if(Locale.getDefault().getDisplayLanguage().equals("English")) {
                    showToastShort("Invoice has been copied ...", getApplicationContext());
                } else if(Locale.getDefault().getDisplayLanguage().equals("français")) {
                    showToastShort("La facture a été copiée ...", getApplicationContext());
                } else if(Locale.getDefault().getDisplayLanguage().equals("español")) {
                    showToastShort("La factura ha sido copiada ...", getApplicationContext());
                } else {
                    showToastShort("Rechnung wurde kopiert ...", getApplicationContext());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            clickListener.set(false);
            return false;
        });

        textView.setOnClickListener(new ClickListener() {
            @Override
            public void onDoubleClick(View v) {
                dataManager.saveToJSON(String.valueOf(textView.getId()), null, getApplicationContext());

                LinearLayout linearLayout = findViewById(R.id.history_scroll_linearlayout);
                int indexOfTextView = linearLayout.indexOfChild(textView);
                linearLayout.removeView(textView);

                if (indexOfTextView < linearLayout.getChildCount()) {
                    View nextView = linearLayout.getChildAt(indexOfTextView);
                    linearLayout.removeView(nextView);
                }

                if(innerLinearLayout.getChildCount() <= 2) {
                    resetNamesAndValues();
                } else {
                    recreate();
                }
            }

            @Override
            public void onSingleClick(View v) {
                if(clickListener.get()) {
                    // Output the text of the clicked TextView to the console
                    dataManager.saveToJSON("pressedCalculate", false, getMainActivityContext());
                    TextView clickedTextView = (TextView) v;
                    String clickedText = clickedTextView.getText().toString();

                    // Split at "=" character
                    String[] parts = clickedText.split("=");

                    // Check and remove leading and trailing spaces
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        try {
                            if(dataManager.readFromJSON("calculationMode", getApplicationContext()).equals("Vereinfacht")) {
                                dataManager.saveToJSON("calculate_text", key.replace(" ", ""), getMainActivityContext());
                                dataManager.saveToJSON("result_text", value.replace(" ", ""), getMainActivityContext());
                            } else {
                                dataManager.saveToJSON("calculate_text", key, getMainActivityContext());
                                dataManager.saveToJSON("result_text", value, getMainActivityContext());
                            }
                            dataManager.saveToJSON("removeValue", false, getMainActivityContext());
                            dataManager.saveToJSON("rotate_op", true, getMainActivityContext());

                            if(Locale.getDefault().getDisplayLanguage().equals("English")) {
                                showToastShort("Invoice has been accepted ...", getApplicationContext());
                            } else if(Locale.getDefault().getDisplayLanguage().equals("français")) {
                                showToastShort("La facture a été acceptée ...", getApplicationContext());
                            } else if(Locale.getDefault().getDisplayLanguage().equals("español")) {
                                showToastShort("La factura ha sido aceptada ...", getApplicationContext());;
                            } else {
                                showToastShort("Rechnung wurde übernommen ...", getApplicationContext());
                            }
                        } catch (Exception e) {
                            Log.i("createHistoryTextView", String.valueOf(e));
                        }
                    }
                }
                clickListener.set(true);
            }
        });

        return textView;
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
        lineLayoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.history_line_margin_horizontal));
        lineLayoutParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.history_line_margin_horizontal));

        line.setLayoutParams(lineLayoutParams);
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.history_line_color));

        // Add an OnConfigurationChangedListener to update line color on configuration changes
        line.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateLineColor(line));

        return line;
    }

    /**
     * Updates the color of the line based on the current night mode.
     *
     * @param line The line view to update.
     */
    private void updateLineColor(View line) {
        int lineColor;
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

    /**
     * This method is responsible for creating and displaying an empty TextView in the history
     * section of the application. It first checks if a TextView already exists and removes it if
     * present. Then, it creates a new TextView with a specific message indicating an empty history.
     * The TextView is assigned a unique ID (R.id.history_empty_textview), and its text color is set
     * based on the current night mode and true dark mode. Additionally, the method sets the text size
     * and gravity for proper alignment. Finally, the newly created TextView is added to the layout.
     * <p>
     * Note: This method is typically called when the history section is empty, providing a visual
     * indication to the user that there is no history data available.
     */
    private void createEmptyHistoryTextView() {
        if(findViewById(R.id.history_empty_textview) == null) {
            TextView emptyTextView = new TextView(this);
            emptyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            emptyTextView.setId(R.id.history_empty_textview);
            emptyTextView.setText(getString(R.string.historyIsEmpty));
            emptyTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            emptyTextView.setTextSize(35f);
            emptyTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            LinearLayout linearLayout = findViewById(R.id.history_scroll_linearlayout);
            linearLayout.addView(emptyTextView);
        }

        TextView emptyTextView = findViewById(R.id.history_empty_textview);
        emptyTextView.setVisibility(View.VISIBLE);

        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    /**
     * Diese Methode entfernt die leere TextView aus dem Layout, falls vorhanden.
     */
    private void deleteEmptyHistoryTextView() {
        TextView emptyTextView = createHistoryTextView("\n\n\n\n\n\n\nDein Verlauf ist leer.");
        emptyTextView.setVisibility(View.GONE);
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
        } else {
            createEmptyHistoryTextView();
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
     * This method is called when the activity is destroyed.
     * It checks if "disablePatchNotesTemporary" is true in the JSON file, and if so, it saves "disablePatchNotesTemporary" as false in the JSON file.
     * It then calls the finish() method to close the activity.
     */
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()).equals("true")) {
            dataManager.saveToJSON("disablePatchNotesTemporary", false, getApplicationContext());
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

                        deleteButton.setForeground(getDrawable(R.drawable.trash_true_darkmode));
                        returnButton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));

                        updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                        deleteButton.setForeground(getDrawable(R.drawable.trash_light));
                        returnButton.setForeground(getDrawable(R.drawable.arrow_back_light));

                        updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    }
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);

                    updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

                    historyReturnButton.setForeground(getDrawable(R.drawable.arrow_back));
                    historyDeleteButton.setForeground(getDrawable(R.drawable.trash));
                    break;
            }
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Tageslichtmodus"))  {
            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
            updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

            historyReturnButton.setForeground(getDrawable(R.drawable.arrow_back));
            historyDeleteButton.setForeground(getDrawable(R.drawable.trash));
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Dunkelmodus")) {
            if(trueDarkMode != null && trueDarkMode.equals("true")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);

                deleteButton.setForeground(getDrawable(R.drawable.trash_true_darkmode));
                returnButton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));

                updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                deleteButton.setForeground(getDrawable(R.drawable.trash_light));
                returnButton.setForeground(getDrawable(R.drawable.arrow_back_light));

                updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
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
        if ((TextView) findViewById(R.id.history_empty_textview) != null) {
            TextView emptyTextView =  findViewById(R.id.history_empty_textview);
            emptyTextView.setTextColor(newColorBTNForegroundAccent);
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