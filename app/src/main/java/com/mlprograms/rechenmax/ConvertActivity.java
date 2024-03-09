package com.mlprograms.rechenmax;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class ConvertActivity extends AppCompatActivity {
    DataManager dataManager;
    private static MainActivity mainActivity;

    private Spinner customSpinner;
    private CustomAdapter customAdapter;

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

        customSpinner = findViewById(R.id.convertCustomSpinner);

        customSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CustomItems items = (CustomItems) adapterView.getSelectedItem();
                String spinnerText = items.getSpinnerText();

                if (spinnerText.equals(getString(R.string.defaultCalculationMode))) {
                    dataManager.saveToJSON("calculationMode", "Standard", getMainActivityContext());
                } else {
                    dataManager.saveToJSON("calculationMode", "Vereinfacht", getMainActivityContext());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });


            ArrayList<CustomItems> customList = new ArrayList<>();
        customList.add(new CustomItems("Winkel", R.drawable.angle));
        customList.add(new CustomItems("Fläche", R.drawable.area));
        customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard));
        customList.add(new CustomItems("Entfernung", R.drawable.triangle));
        customList.add(new CustomItems("Volumen", R.drawable.cylinder));

        customAdapter = new CustomAdapter(this, customList);

        if(customSpinner != null) {
            customSpinner.setAdapter(customAdapter);
            customSpinner.setSelection(3);
        }
        switchDisplayMode();
    }

    private void switchModes() {

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

        int index = 0;
        if(customSpinner != null) {
            index = customSpinner.getSelectedItemPosition();
        }

        if(getSelectedSetting() != null) {
            if(getSelectedSetting().equals("Systemstandard")) {
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Nightmode is activated
                        dataManager = new DataManager();
                        String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

                        if (trueDarkMode != null) {
                            if (trueDarkMode.equals("false")) {
                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                                }

                                updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));

                                customSpinner = findViewById(R.id.convertCustomSpinner);
                                ArrayList<CustomItems> customList = new ArrayList<>();
                                customList.add(new CustomItems("Winkel", R.drawable.angle_light));
                                customList.add(new CustomItems("Fläche", R.drawable.area_light));
                                customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard_light));
                                customList.add(new CustomItems("Entfernung", R.drawable.triangle_light));
                                customList.add(new CustomItems("Volumen", R.drawable.cylinder_light));

                                customAdapter = new CustomAdapter(this, customList);
                                customAdapter.setTextColor(Color.parseColor("#FFFFFF"));
                                customAdapter.setBackgroundColor(Color.parseColor("#151515"));
                            } else {
                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                                }

                                updateUI(Color.parseColor("#000000"), Color.parseColor("#D5D5D5"));

                                customSpinner = findViewById(R.id.convertCustomSpinner);
                                ArrayList<CustomItems> customList = new ArrayList<>();
                                customList.add(new CustomItems("Winkel", R.drawable.angle_true_darkmode));
                                customList.add(new CustomItems("Fläche", R.drawable.area_true_darkmode));
                                customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard_true_darkmode));
                                customList.add(new CustomItems("Entfernung", R.drawable.triangle_true_darkmode));
                                customList.add(new CustomItems("Volumen", R.drawable.cylinder_true_darkmode));

                                customAdapter = new CustomAdapter(this, customList);
                                customAdapter.setTextColor(Color.parseColor("#D5D5D5"));
                                customAdapter.setBackgroundColor(Color.parseColor("#000000"));
                            }
                        } else {
                            if(backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                            }

                            updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));

                            customSpinner = findViewById(R.id.convertCustomSpinner);
                            ArrayList<CustomItems> customList = new ArrayList<>();
                            customList.add(new CustomItems("Winkel", R.drawable.angle_light));
                            customList.add(new CustomItems("Fläche", R.drawable.area_light));
                            customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard_light));
                            customList.add(new CustomItems("Entfernung", R.drawable.triangle_light));
                            customList.add(new CustomItems("Volumen", R.drawable.cylinder_light));

                            customAdapter = new CustomAdapter(this, customList);
                            customAdapter.setTextColor(Color.parseColor("#FFFFFF"));
                            customAdapter.setBackgroundColor(Color.parseColor("#151515"));
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                        }

                        updateUI(Color.parseColor("#FFFFFF"), Color.parseColor("#151515"));

                        customSpinner = findViewById(R.id.convertCustomSpinner);
                        ArrayList<CustomItems> customList = new ArrayList<>();
                        customList.add(new CustomItems("Winkel", R.drawable.angle));
                        customList.add(new CustomItems("Fläche", R.drawable.area));
                        customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard));
                        customList.add(new CustomItems("Entfernung", R.drawable.triangle));
                        customList.add(new CustomItems("Volumen", R.drawable.cylinder));

                        customAdapter = new CustomAdapter(this, customList);
                        customAdapter.setTextColor(Color.parseColor("#151515"));
                        customAdapter.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        break;
                }
            } else if (getSelectedSetting().equals("Tageslichtmodus")) {
                if(backbutton != null) {
                    backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                }

                updateUI(Color.parseColor("#FFFFFF"), Color.parseColor("#151515"));

                customSpinner = findViewById(R.id.convertCustomSpinner);
                ArrayList<CustomItems> customList = new ArrayList<>();
                customList.add(new CustomItems("Winkel", R.drawable.angle));
                customList.add(new CustomItems("Fläche", R.drawable.area));
                customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard));
                customList.add(new CustomItems("Entfernung", R.drawable.triangle));
                customList.add(new CustomItems("Volumen", R.drawable.cylinder));

                customAdapter = new CustomAdapter(this, customList);
                customAdapter.setTextColor(Color.parseColor("#151515"));
                customAdapter.setBackgroundColor(Color.parseColor("#FFFFFF"));

                customSpinner.setAdapter(customAdapter);
            } else if (getSelectedSetting().equals("Dunkelmodus")) {
                dataManager = new DataManager();
                String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

                if (trueDarkMode != null) {
                    if (trueDarkMode.equals("false")) {
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                        }

                        updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));

                        customSpinner = findViewById(R.id.convertCustomSpinner);
                        ArrayList<CustomItems> customList = new ArrayList<>();
                        customList.add(new CustomItems("Winkel", R.drawable.angle_light));
                        customList.add(new CustomItems("Fläche", R.drawable.area_light));
                        customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard_light));
                        customList.add(new CustomItems("Entfernung", R.drawable.triangle_light));
                        customList.add(new CustomItems("Volumen", R.drawable.cylinder_light));

                        customAdapter = new CustomAdapter(this, customList);
                        customAdapter.setTextColor(Color.parseColor("#FFFFFF"));
                        customAdapter.setBackgroundColor(Color.parseColor("#151515"));

                        customSpinner.setAdapter(customAdapter);
                    } else {
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                        }

                        updateUI(Color.parseColor("#000000"), Color.parseColor("#D5D5D5"));

                        customSpinner = findViewById(R.id.convertCustomSpinner);
                        ArrayList<CustomItems> customList = new ArrayList<>();
                        customList.add(new CustomItems("Winkel", R.drawable.angle_true_darkmode));
                        customList.add(new CustomItems("Fläche", R.drawable.area_true_darkmode));
                        customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard_true_darkmode));
                        customList.add(new CustomItems("Entfernung", R.drawable.triangle_true_darkmode));
                        customList.add(new CustomItems("Volumen", R.drawable.cylinder_true_darkmode));

                        customAdapter = new CustomAdapter(this, customList);
                        customAdapter.setTextColor(Color.parseColor("#D5D5D5"));
                        customAdapter.setBackgroundColor(Color.parseColor("#000000"));

                        customSpinner.setAdapter(customAdapter);
                    }
                } else {
                    if(backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                    }

                    updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));

                    customSpinner = findViewById(R.id.convertCustomSpinner);
                    ArrayList<CustomItems> customList = new ArrayList<>();
                    customList.add(new CustomItems("Winkel", R.drawable.angle_light));
                    customList.add(new CustomItems("Fläche", R.drawable.area_light));
                    customList.add(new CustomItems("Digitaler Speicher", R.drawable.sdcard_light));
                    customList.add(new CustomItems("Entfernung", R.drawable.triangle_light));
                    customList.add(new CustomItems("Volumen", R.drawable.cylinder_light));

                    customAdapter = new CustomAdapter(this, customList);
                    customAdapter.setTextColor(Color.parseColor("#FFFFFF"));
                    customAdapter.setBackgroundColor(Color.parseColor("#151515"));

                    customSpinner.setAdapter(customAdapter);
                }
            }
        }
        if(customSpinner != null) {
            customSpinner.setSelection(index);
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
        LinearLayout convertUI = findViewById(R.id.convertUI);
        Spinner customSpinner = findViewById(R.id.convertCustomSpinner);

        convertReturnButton.setBackgroundColor(backgroundColor);
        convertTitle.setTextColor(textColor);
        convertLayout.setBackgroundColor(backgroundColor);
        convertScrollLayout.setBackgroundColor(backgroundColor);
        convertUI.setBackgroundColor(backgroundColor);
        customSpinner.setBackgroundColor(backgroundColor);

        setTextViewColors(findViewById(R.id.convertUI), textColor, backgroundColor);
    }

    private void setTextViewColors(View view, int textColor, int backgroundColor) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(textColor);
            textView.setBackgroundColor(backgroundColor);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setTextViewColors(viewGroup.getChildAt(i), textColor, backgroundColor);
            }
        }
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
