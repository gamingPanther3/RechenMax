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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ConvertActivity extends AppCompatActivity {
    DataManager dataManager;
    private static MainActivity mainActivity;
    private Spinner customSpinner;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *        previously being shut down, then this Bundle contains the data it most
     *        recently supplied in {@link #onSaveInstanceState}.
     *        <b><i>Note: Otherwise, it is null.</i></b>
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass onCreate method
        super.onCreate(savedInstanceState);
        stopBackgroundService();
        dataManager = new DataManager();
        setContentView(R.layout.convert);

        setUpButtonListeners();
        switchDisplayMode();

        customSpinner = findViewById(R.id.convertCustomSpinner);
        ArrayList<CustomItems> customList = new ArrayList<>();
        customList.add(new CustomItems("Winkel", R.drawable.acute_angle_of_45_degrees_svgrepo_com));
        customList.add(new CustomItems("Fläche", R.drawable.area_vectormaker_co));
        customList.add(new CustomItems("Digitaler Speicher", R.drawable.sd_card_svgrepo_com));
        customList.add(new CustomItems("Entfernung", R.drawable.set_square_svgrepo_com));
        customList.add(new CustomItems("Volumen", R.drawable.cylinder_shape_svgrepo_com));

        CustomAdapter customAdapter = new CustomAdapter(this, customList);

        if(customSpinner != null) {
            customSpinner.setAdapter(customAdapter);
        }
    }

    /**
     * This method is called when the activity is destroyed.
     * It checks if "disablePatchNotesTemporary" is true in the JSON file, and if so, it saves "disablePatchNotesTemporary" as false in the JSON file.
     * It then calls the finish() method to close the activity.
     */
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager.readFromJSON("disablePatchNotesTemporary", getMainActivityContext()).equals("true")) {
            dataManager.saveToJSON("disablePatchNotesTemporary", false, getMainActivityContext());
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
            Log.e("startBackgroundService", e.toString());
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
    private Context getMainActivityContext() {
        return mainActivity;
    }

    /**
     * Sets up the listeners for each button in the application
     */
    private void setUpButtonListeners() {
        setButtonListener(R.id.convert_return_button, this::returnToCalculator);
    }

    /**
     * Sets up the listener for all buttons
     *
     * @param textViewId The ID of the button to which the listener is to be set.
     * @param action The action which belongs to the button.
     */
    private void setButtonListener(int textViewId, Runnable action) {
        TextView textView = findViewById(textViewId);
        if(textView != null) {
            textView.setOnClickListener(v -> {
                action.run();
            });
        }
    }

    /**
     * This method switches the display mode based on the current night mode.
     */
    @SuppressLint({"ResourceType", "UseCompatLoadingForDrawables", "CutPasteId"})
    private void switchDisplayMode() {
        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        Button backbutton = findViewById(R.id.convert_return_button);

        if(getSelectedSetting() != null) {
            if(getSelectedSetting().equals("Systemstandard")) {
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Nightmode is activated
                        dataManager = new DataManager();
                        String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

                        if (trueDarkMode != null) {
                            if (trueDarkMode.equals("false")) {
                                updateUI(R.color.black, R.color.white);

                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                                }
                            } else {
                                updateUI(R.color.darkmode_black, R.color.darkmode_white);

                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));
                                }
                            }
                        } else {
                            if(backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                            }

                            updateUI(R.color.black, R.color.white);
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
                        }

                        updateUI(R.color.white, R.color.black);
                        break;
                }
            } else if (getSelectedSetting().equals("Tageslichtmodus")) {
                if(backbutton != null) {
                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
                }

                updateUI(R.color.white, R.color.black);
            } else if (getSelectedSetting().equals("Dunkelmodus")) {
                dataManager = new DataManager();
                String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

                if (trueDarkMode != null) {
                    if (trueDarkMode.equals("false")) {
                        updateUI(R.color.black, R.color.white);

                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                        }
                    } else {
                        updateUI(R.color.darkmode_black, R.color.darkmode_white);

                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));
                        }
                    }
                } else {
                    if(backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                    }

                    updateUI(R.color.black, R.color.white);
                }
            }
        }
    }

    /**
     * Handles configuration changes.
     * It calls the superclass method and switches the display mode based on the current night mode.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switchDisplayMode();
    }

    /**
     * This method updates the UI elements with the given background color and text color.
     * @param backgroundColor The color to be used for the background.
     * @param textColor The color to be used for the text.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUI(int backgroundColor, int textColor) {

        Button convertReturnButton = findViewById(R.id.convert_return_button);
        TextView convertTitle = findViewById(R.id.convert_title);
        LinearLayout convertLayout = findViewById(R.id.convertlayout);
        ScrollView convertScrollLayout = findViewById(R.id.convertScrollLayout);

        convertReturnButton.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        convertTitle.setTextColor(ContextCompat.getColor(this, textColor));
        convertLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        convertScrollLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
    }

    /**
     * This method gets the selected setting.
     * @return The selected setting.
     */
    public String getSelectedSetting() {
        final String setting = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());

        if(setting != null) {
            switch (setting) {
                case "Dark":
                    return "Dunkelmodus";
                case "Light":
                    return "Tageslichtmodus";
                default:
                    return "Systemstandard";
            }
        }
        return "Systemstandard";
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
     * This method returns to the calculator by starting the MainActivity.
     */
    public void returnToCalculator() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
