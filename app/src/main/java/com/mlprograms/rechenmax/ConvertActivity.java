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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;

import java.util.ArrayList;

public class ConvertActivity extends AppCompatActivity {
    DataManager dataManager;
    private static MainActivity mainActivity;
    private Spinner customSpinnerMode;
    private Spinner customSpinnerMeasurement;
    private EditText customEditText;
    
    private ArrayList<CustomItems> customList = new ArrayList<>();
    private ArrayList<CustomItems> customItemListAngle = new ArrayList<>();
    private ArrayList<CustomItems> customItemListArea = new ArrayList<>();
    private ArrayList<CustomItems> customItemListStorage = new ArrayList<>();
    private ArrayList<CustomItems> customItemListDistance = new ArrayList<>();
    private ArrayList<CustomItems> customItemListVolume = new ArrayList<>();

    private CustomAdapter customAdapter;
    private CustomAdapter customAdapterMeasurement;
    private boolean firstStart = true;

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
        setUpCustomItemLists();

        // convert mode spinner
        customSpinnerMode = findViewById(R.id.convertCustomSpinner);
        customSpinnerMeasurement = findViewById(R.id.convertSpinnerMessurement);
        customEditText = findViewById(R.id.convertEditTextNumber);

        customList = new ArrayList<>();
        customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area));
        customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard));
        customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle));
        customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder));

        customAdapter = new CustomAdapter(this, customList);

        if(customSpinnerMode != null) {
            customSpinnerMode.setAdapter(customAdapter);
        }

        try {
            final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");
            final int pos = Integer.parseInt(dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Current"));
            final String number = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Number");

            switch (mode) {
                case "W":
                    customSpinnerMode.setSelection(0);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListAngle);
                    break;
                case "F":
                    customSpinnerMode.setSelection(1);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListArea);
                    break;
                case "S":
                    customSpinnerMode.setSelection(2);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListStorage);
                    break;
                case "E":
                    customSpinnerMode.setSelection(3);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListDistance);
                    break;
                case "V":
                    customSpinnerMode.setSelection(4);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListVolume);
                    break;
            }

            if(customSpinnerMeasurement != null) {
                customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                customAdapterMeasurement.notifyDataSetChanged();
                customSpinnerMeasurement.setSelection(pos);
            }
            customEditText.setText(number);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        assert customSpinnerMode != null;
        customSpinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CustomItems items = (CustomItems) adapterView.getSelectedItem();
                String spinnerText = items.getSpinnerText();

                String new_value = "";
                if(spinnerText.equals(getString(R.string.convertAngle))) {
                    new_value = "W";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListAngle);
                } else if(spinnerText.equals(getString(R.string.convertArea))) {
                    new_value = "F";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListArea);
                } else if(spinnerText.equals(getString(R.string.convertStorage))) {
                    new_value = "S";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListStorage);
                } else if(spinnerText.equals(getString(R.string.convertDistance))) {
                    new_value = "E";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListDistance);
                } else if(spinnerText.equals(getString(R.string.convertVolume))) {
                    new_value = "V";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListVolume);
                }

                final String mode;
                final int pos;
                final String number;
                try {
                    mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");
                    pos = Integer.parseInt(dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Current"));
                    number = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Number");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                customEditText.setText(number);

                if(!mode.equals(new_value) || firstStart) {
                    if(customSpinnerMeasurement != null) {
                        customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                        customSpinnerMeasurement.setSelection(pos);
                    }

                    firstStart = false;
                    changeConvertModes(spinnerText);
                    switchDisplayMode();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        switchDisplayMode();

        EditText editText = findViewById(R.id.convertEditTextNumber);
        editText.setMaxLines(1);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                final String inputText = editText.getText().toString();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.clearFocus();

                try {
                    final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");
                    dataManager.updateValuesInJSONSettingsData(
                            "convertMode",
                            mode + "Number",
                            inputText,
                            getMainActivityContext()
                    );
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                return true;
            }
            return false;
        });

        customSpinnerMeasurement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");

                    dataManager.updateValuesInJSONSettingsData(
                            "convertMode",
                            mode + "Current",
                            String.valueOf(position),
                            getMainActivityContext()
                    );

                    //Log.e("DEBUG", dataManager.getAllDataFromJSONSettings(getMainActivityContext()).toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void setUpCustomItemLists() {
        customItemListAngle.add(new CustomItems(getString(R.string.convertDeg)));
        customItemListAngle.add(new CustomItems(getString(R.string.convertRad)));
        customItemListAngle.add(new CustomItems(getString(R.string.convertPitch)));
        customItemListAngle.add(new CustomItems(getString(R.string.convertGon)));
        customItemListAngle.add(new CustomItems(getString(R.string.convertMrad)));
        customItemListAngle.add(new CustomItems(getString(R.string.convertMinuteOfTheArc)));
        customItemListAngle.add(new CustomItems(getString(R.string.convertSecondArc)));

        customItemListArea.add(new CustomItems(getString(R.string.convertSquareMillimeter)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareCentimeter)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareMeter)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareKilometer)));
        customItemListArea.add(new CustomItems(getString(R.string.convertAr)));
        customItemListArea.add(new CustomItems(getString(R.string.convertDekar)));
        customItemListArea.add(new CustomItems(getString(R.string.convertHectares)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareInch)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareFeet)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareYard)));
        customItemListArea.add(new CustomItems(getString(R.string.convertAcre)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareMiles)));

        customItemListStorage.add(new CustomItems(getString(R.string.convertBit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertByte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertKilobit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertKilobyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertMegabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertMegabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertGigabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertGigabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertTerabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertTerabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertPetabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertPetabyte)));

        customItemListDistance.add(new CustomItems(getString(R.string.convertPikometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertNanometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertMikrometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertMillimeter)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertCentimeter)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertDezimeter)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertMeter)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertHektometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertKilometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertFeet)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertYard)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertMiles)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertSeamiles)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertLightyear)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertAstronomicalUnit)));

        customItemListVolume.add(new CustomItems(getString(R.string.convertMikroliter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertMilliliter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertDeziliter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertLiter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertZentiliter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertMetrischeTasse)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikmillimeter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikzentimeter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikmeter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikInch)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikFeet)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikYard)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertGillsUS)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertGillsUK)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertTeaspoonsUS)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertTeaspoonsUK)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertTablespoonsUS)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertTablespoonsUK)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertCupsUS)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertCupsUK)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertGallonUS)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertGallonUK)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertBarrels)));
    }

    @SuppressLint("InflateParams")
    private void changeConvertModes(final String spinnerText) {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout otherLinearLayout = null;

        if(spinnerText.equals(getString(R.string.convertAngle))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","W", getMainActivityContext());
        } else if(spinnerText.equals(getString(R.string.convertArea))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","F", getMainActivityContext());
        } else if(spinnerText.equals(getString(R.string.convertStorage))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","S", getMainActivityContext());
        } else if(spinnerText.equals(getString(R.string.convertDistance))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","E", getMainActivityContext());
        } else if(spinnerText.equals(getString(R.string.convertVolume))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","V", getMainActivityContext());
        }

        if(spinnerText.equals(getString(R.string.convertAngle))) {
            otherLinearLayout = (LinearLayout) inflater.inflate(R.layout.angle, null);
        } else if(spinnerText.equals(getString(R.string.convertArea))) {
            otherLinearLayout = (LinearLayout) inflater.inflate(R.layout.area, null);
        } else if(spinnerText.equals(getString(R.string.convertStorage))) {
            otherLinearLayout = (LinearLayout) inflater.inflate(R.layout.digital_storage, null);
        } else if(spinnerText.equals(getString(R.string.convertDistance))) {
            otherLinearLayout = (LinearLayout) inflater.inflate(R.layout.distance, null);
        } else if(spinnerText.equals(getString(R.string.convertVolume))) {
            otherLinearLayout = (LinearLayout) inflater.inflate(R.layout.volume, null);
        }

        if (otherLinearLayout != null) {
            ScrollView scrollView = findViewById(R.id.convertScrollLayout);
            scrollView.removeAllViews();
            scrollView.addView(otherLinearLayout);
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
            if (dataManager.getJSONSettingsData("disablePatchNotesTemporary", getMainActivityContext()).getString("value").equals("true")) {
                dataManager.saveToJSONSettings("disablePatchNotesTemporary", false, getMainActivityContext());
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
        if(customSpinnerMode != null) {
            index = customSpinnerMode.getSelectedItemPosition();
        }

        if(getSelectedSetting() != null) {
            if(getSelectedSetting().equals("Systemstandard")) {
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Nightmode is activated
                        dataManager = new DataManager();
                        String trueDarkMode;
                        try {
                            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        if (trueDarkMode.equals("false")) {
                            if (backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                            }

                            updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));

                            customList = new ArrayList<>();
                            customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area_light));
                            customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard_light));
                            customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle_light));
                            customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder_light));

                            customAdapter = new CustomAdapter(this, customList);
                            customAdapter.setTextColor(Color.parseColor("#FFFFFF"));
                            customAdapter.setBackgroundColor(Color.parseColor("#151515"));
                            customSpinnerMode.setAdapter(customAdapter);

                            customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                            if (customAdapterMeasurement != null) {
                                customAdapterMeasurement.setTextColor(Color.parseColor("#FFFFFF"));
                                customAdapterMeasurement.setBackgroundColor(Color.parseColor("#151515"));
                                customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                            }

                        } else {
                            if (backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                            }

                            updateUI(Color.parseColor("#000000"), Color.parseColor("#D5D5D5"));

                            customList = new ArrayList<>();
                            customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder_true_darkmode));

                            customAdapter = new CustomAdapter(this, customList);
                            customAdapter.setTextColor(Color.parseColor("#D5D5D5"));
                            customAdapter.setBackgroundColor(Color.parseColor("#000000"));
                            customSpinnerMode.setAdapter(customAdapter);

                            customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                            if (customAdapterMeasurement != null) {
                                customAdapterMeasurement.setTextColor(Color.parseColor("#D5D5D5"));
                                customAdapterMeasurement.setBackgroundColor(Color.parseColor("#000000"));
                                customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                            }
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                        }

                        updateUI(Color.parseColor("#FFFFFF"), Color.parseColor("#151515"));

                        customList = new ArrayList<>();
                        customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area));
                        customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard));
                        customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle));
                        customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder));

                        customAdapter = new CustomAdapter(this, customList);
                        customAdapter.setTextColor(Color.parseColor("#151515"));
                        customAdapter.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        customSpinnerMode.setAdapter(customAdapter);

                        customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                        if (customAdapterMeasurement != null) {
                            customAdapterMeasurement.setTextColor(Color.parseColor("#151515"));
                            customAdapterMeasurement.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                        }
                        break;
                }
            } else if (getSelectedSetting().equals("Tageslichtmodus")) {
                if(backbutton != null) {
                    backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                }

                updateUI(Color.parseColor("#FFFFFF"), Color.parseColor("#151515"));

                customList = new ArrayList<>();
                customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area));
                customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard));
                customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle));
                customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder));

                customAdapter = new CustomAdapter(this, customList);
                customAdapter.setTextColor(Color.parseColor("#151515"));
                customAdapter.setBackgroundColor(Color.parseColor("#FFFFFF"));
                customSpinnerMode.setAdapter(customAdapter);

                customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                if (customAdapterMeasurement != null) {
                    customAdapterMeasurement.setTextColor(Color.parseColor("#151515"));
                    customAdapterMeasurement.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                }
            } else if (getSelectedSetting().equals("Dunkelmodus")) {
                dataManager = new DataManager();
                String trueDarkMode;
                try {
                    trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (trueDarkMode.equals("false")) {
                    if (backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                    }

                    updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));

                    customList = new ArrayList<>();
                    customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area_light));
                    customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard_light));
                    customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle_light));
                    customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder_light));

                    customAdapter = new CustomAdapter(this, customList);
                    customAdapter.setTextColor(Color.parseColor("#FFFFFF"));
                    customAdapter.setBackgroundColor(Color.parseColor("#151515"));
                    customSpinnerMode.setAdapter(customAdapter);

                    customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                    if (customAdapterMeasurement != null) {
                        customAdapterMeasurement.setTextColor(Color.parseColor("#FFFFFF"));
                        customAdapterMeasurement.setBackgroundColor(Color.parseColor("#151515"));
                        customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                    }
                } else {
                    if (backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                    }

                    updateUI(Color.parseColor("#000000"), Color.parseColor("#D5D5D5"));

                    customList = new ArrayList<>();
                    customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder_true_darkmode));

                    customAdapter = new CustomAdapter(this, customList);
                    customAdapter.setTextColor(Color.parseColor("#D5D5D5"));
                    customAdapter.setBackgroundColor(Color.parseColor("#000000"));
                    customSpinnerMode.setAdapter(customAdapter);

                    customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                    if (customAdapterMeasurement != null) {
                        customAdapterMeasurement.setTextColor(Color.parseColor("#D5D5D5"));
                        customAdapterMeasurement.setBackgroundColor(Color.parseColor("#000000"));
                        customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                    }
                }
            }
        }
        if(customSpinnerMode != null) {
            customSpinnerMode.setSelection(index);
        }
        if(customSpinnerMeasurement != null) {
            try {
                final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");
                final int pos = Integer.parseInt(dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Current"));
                customSpinnerMeasurement.setSelection(pos);
            } catch (JSONException e) {
                throw new RuntimeException(e);
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
        firstStart = false;
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
        Spinner customSpinnerMeasurement = findViewById(R.id.convertSpinnerMessurement);
        EditText convertSpinnerNumber = findViewById(R.id.convertEditTextNumber);

        convertReturnButton.setBackgroundColor(backgroundColor);
        convertTitle.setTextColor(textColor);
        convertLayout.setBackgroundColor(backgroundColor);
        convertScrollLayout.setBackgroundColor(backgroundColor);
        convertUI.setBackgroundColor(backgroundColor);
        customSpinner.setBackgroundColor(backgroundColor);

        customSpinner.setBackgroundColor(backgroundColor);
        customSpinnerMeasurement.setBackgroundColor(backgroundColor);
        convertSpinnerNumber.setHintTextColor(Color.parseColor("#D5D5D5"));

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
        final String setting;
        try {
            setting = dataManager.getJSONSettingsData("selectedSpinnerSetting", getMainActivityContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        switch (setting) {
            case "Dark":
                return "Dunkelmodus";
            case "Light":
                return "Tageslichtmodus";
            default:
                return "Systemstandard";
        }
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
