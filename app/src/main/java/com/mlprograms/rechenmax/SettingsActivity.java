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

import static com.mlprograms.rechenmax.BackgroundService.CHANNEL_ID_BACKGROUND;
import static com.mlprograms.rechenmax.BackgroundService.CHANNEL_ID_HINTS;
import static com.mlprograms.rechenmax.BackgroundService.CHANNEL_ID_REMEMBER;
import static com.mlprograms.rechenmax.BackgroundService.createNotificationChannel;
import static com.mlprograms.rechenmax.ToastHelper.showToastLong;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * SettingsActivity
 *
 * @author Max Lemberg
 * @version 1.0.3
 * @date 14.02.2023
 */

public class SettingsActivity extends AppCompatActivity {

    // Declare a DataManager object
    DataManager dataManager;
    private boolean isProgrammaticChange = false;

    private ArrayList<CustomItems> customListDisplayMode = new ArrayList<>();
    private ArrayList<CustomItems> customListCalculationMode = new ArrayList<>();
    private ArrayList<CustomItems> customListFunctionMode = new ArrayList<>();
    private ArrayList<CustomItems> customListHistoryMode = new ArrayList<>();
    private ArrayList<CustomItems> customListDecimalPoints = new ArrayList<>();

    private CustomAdapter customAdapter1;
    private CustomAdapter customAdapter2;
    private CustomAdapter customAdapter3;
    private CustomAdapter customAdapter4;
    private CustomAdapter customAdapter5;

    private Spinner customSpinner1;
    private Spinner customSpinner2;
    private Spinner customSpinner3;
    private Spinner customSpinner4;
    private Spinner customSpinner5;

    // Declare a static MainActivity object
    @SuppressLint("StaticFieldLeak")
    private static MainActivity mainActivity;
    private static final String PREFS_NAME = "NotificationPermissionPrefs";
    private static final String PERMISSION_GRANTED_KEY = "permission_granted";

    /**
     * The `savedInstanceState` Bundle contains data that was saved in {@link #onSaveInstanceState}
     * when the activity was previously destroyed. This data can be used to restore the activity's
     * state when it is re-initialized. If the activity is being created for the first time,
     * this Bundle is null.
     *
     * @param savedInstanceState The Bundle containing the saved state, or null if the activity is
     *                           being created for the first time.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint({"UseCompatLoadingForDrawables", "UseSwitchCompatOrMaterialCode", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopBackgroundService();

        setContentView(R.layout.settings);

        dataManager = new DataManager();
        dataManager.saveToJSONSettings("lastActivity", "Set", getApplicationContext());

        @SuppressLint("CutPasteId") Button button = findViewById(R.id.report_return_button);
        button.setOnClickListener(v -> returnToCalculator());

        findViewById(R.id.settingsUI);

        //Switch settingsReleaseNotesSwitch = findViewById(R.id.settings_release_notes);
        Switch settingsTrueDarkMode = findViewById(R.id.settings_true_darkmode);
        Switch settingPI = findViewById(R.id.settings_pi);
        Switch settingWarningMessage = findViewById(R.id.settingsConverterDevelopmentMessage);

        //updateSwitchState(settingsReleaseNotesSwitch, "settingReleaseNotesSwitch");
        updateSwitchState(settingsTrueDarkMode, "settingsTrueDarkMode");
        updateSwitchState(settingPI, "refactorPI");
        updateSwitchState(settingWarningMessage, "showConverterDevelopmentMessage");

        appendSpaceToSwitches(findViewById(R.id.settingsUI));

        //settingsReleaseNotesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        //    dataManager.saveToJSONSettings("settingReleaseNotesSwitch", isChecked, getApplicationContext());
        //    dataManager.saveToJSONSettings("showPatchNotes", isChecked, getMainActivityContext());
        //    dataManager.saveToJSONSettings("disablePatchNotesTemporary", isChecked, getMainActivityContext());
        //    try {
        //        Log.i("Settings", "settingReleaseNotesSwitch=" + dataManager.getJSONSettingsData("settingReleaseNotesSwitch", getMainActivityContext()).getString("value"));
        //    } catch (JSONException e) {
        //        throw new RuntimeException(e);
        //    }
        //});
        settingsTrueDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dataManager.saveToJSONSettings("settingsTrueDarkMode", isChecked, getMainActivityContext());
            try {
                Log.i("Settings", "settingsTrueDarkMode=" + dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        });
        settingPI.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dataManager.saveToJSONSettings("refactorPI", isChecked, getMainActivityContext());
            try {
                Log.i("Settings", "refactorPI=" + dataManager.getJSONSettingsData("refactorPI", getMainActivityContext()).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        settingWarningMessage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                final boolean showMessage = Boolean.parseBoolean(dataManager.getJSONSettingsData("showConverterDevelopmentMessage", getMainActivityContext()).getString("value"));
                dataManager.updateValuesInJSONSettingsData("showConverterDevelopmentMessage", "value", String.valueOf(!showMessage), getMainActivityContext());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings));
        customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day));
        customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night));
        customAdapter1 = new CustomAdapter(this, customListDisplayMode);

        customSpinner1 = findViewById(R.id.settings_display_mode_spinner);
        if (customSpinner1 != null) {
            customSpinner1.setAdapter(customAdapter1);

            try {
                String mode;
                mode = dataManager.getJSONSettingsData("selectedSpinnerSetting", getMainActivityContext()).getString("value");

                if (mode.equals("System")) {
                    customSpinner1.setSelection(0);
                } else if (mode.equals("Light")) {
                    customSpinner1.setSelection(1);
                } else {
                    customSpinner1.setSelection(2);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        assert customSpinner1 != null;
        customSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CustomItems items = (CustomItems) adapterView.getSelectedItem();
                String spinnerText = items.getSpinnerText();

                if (isProgrammaticChange) {
                    isProgrammaticChange = false;
                    return;
                }

                if (spinnerText.equals(getString(R.string.systemDefault))) {
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "System", getMainActivityContext());
                } else if (spinnerText.equals(getString(R.string.lightMode))) {
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "Light", getMainActivityContext());
                } else {
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "Dark", getMainActivityContext());
                }
                updateSpinner(adapterView);
                switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.settings));
        customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.day));
        customAdapter2 = new CustomAdapter(this, customListCalculationMode);

        customSpinner2 = findViewById(R.id.settings_calculation_mode_spinner);
        if (customSpinner2 != null) {
            customSpinner2.setAdapter(customAdapter2);

            final String mode;
            try {
                mode = dataManager.getJSONSettingsData("calculationMode", getMainActivityContext()).getString("value");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            if (mode.equals("Standard")) {
                customSpinner2.setSelection(0);
            } else {
                customSpinner2.setSelection(1);
            }
        }

        assert customSpinner2 != null;
        customSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CustomItems items = (CustomItems) adapterView.getSelectedItem();
                String spinnerText = items.getSpinnerText();

                if (isProgrammaticChange) {
                    isProgrammaticChange = false;
                    return;
                }

                if (spinnerText.equals(getString(R.string.defaultCalculationMode))) {
                    dataManager.saveToJSONSettings("calculationMode", "Standard", getMainActivityContext());
                } else {
                    dataManager.saveToJSONSettings("calculationMode", "Vereinfacht", getMainActivityContext());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // Declare a Spinner object
        customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree));
        customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian));
        customAdapter3 = new CustomAdapter(this, customListFunctionMode);

        customSpinner3 = findViewById(R.id.settings_function_spinner);
        if (customSpinner3 != null) {
            customSpinner3.setAdapter(customAdapter3);

            final String mode;
            try {
                mode = dataManager.getJSONSettingsData("functionMode", getMainActivityContext()).getString("value");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            if (mode.equals(getString(R.string.degree))) {
                customSpinner3.setSelection(0);
            } else {
                customSpinner3.setSelection(1);
            }
        }

        assert customSpinner3 != null;
        customSpinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CustomItems items = (CustomItems) adapterView.getSelectedItem();
                String spinnerText = items.getSpinnerText();

                if (spinnerText.equals(getString(R.string.degree))) {
                    dataManager.saveToJSONSettings("functionMode", "Deg", getMainActivityContext());
                } else {
                    dataManager.saveToJSONSettings("functionMode", "Rad", getMainActivityContext());
                }

                //updateSpinner2(adapterView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // Declare a Spinner object
        customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline));
        customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline));
        customAdapter4 = new CustomAdapter(this, customListHistoryMode);

        customSpinner4 = findViewById(R.id.settings_history_mode_spinner);
        if (customSpinner4 != null) {
            customSpinner4.setAdapter(customAdapter4);

            final String mode;
            try {
                mode = dataManager.getJSONSettingsData("historyMode", getMainActivityContext()).getString("value");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            if (mode.equals("single")) {
                customSpinner4.setSelection(0);
            } else {
                customSpinner4.setSelection(1);
            }
        }

        assert customSpinner4 != null;
        customSpinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CustomItems items = (CustomItems) adapterView.getSelectedItem();
                String spinnerText = items.getSpinnerText();

                if (spinnerText.equals(getString(R.string.settingsHistoryModeSingle))) {
                    dataManager.saveToJSONSettings("historyMode", "single", getMainActivityContext());
                } else {
                    dataManager.saveToJSONSettings("historyMode", "multiple", getMainActivityContext());
                }
                //Log.e("DEBUG", dataManager.getJSONSettingsData("historyMode", getMainActivityContext()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        Button helpButton = findViewById(R.id.help_button);
        helpButton.setOnClickListener(v -> {
            HelpActivity.setMainActivityContext(this);
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        });

        Button reportButton = findViewById(R.id.report_button);
        reportButton.setOnClickListener(v -> {
            ReportActivity.setMainActivityContext(this);
            Intent intent = new Intent(this, ReportActivity.class);
            startActivity(intent);
        });

        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));
        customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

        customSpinner5 = findViewById(R.id.settings_decimalpoints_spinner);
        if (customSpinner5 != null) {
            customSpinner5.setAdapter(customAdapter5);

            try {
                customSpinner5.setSelection(Integer.parseInt(dataManager.getJSONSettingsData("numberOfDecimals", getMainActivityContext()).getString("value")));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        assert customSpinner5 != null;
        customSpinner5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CustomItems items = (CustomItems) adapterView.getSelectedItem();
                String spinnerText = items.getSpinnerText();

                dataManager.updateValuesInJSONSettingsData("numberOfDecimals", "value", String.valueOf(Integer.parseInt(spinnerText)), getMainActivityContext());
                //Log.e("DEBUG", String.valueOf(dataManager.getAllDataFromJSONSettings(getMainActivityContext())));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        createNotificationChannel(this);
        createNotificationButtonListeners();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void createNotificationButtonListeners() {
        Switch allowNotifications = findViewById(R.id.settings_notifications);
        Switch allowRememberNotification = findViewById(R.id.settings_remember);
        Switch allowDailyNotifications = findViewById(R.id.settings_daily_hints);

        TextView allowNotificationText = findViewById(R.id.settings_notifications_text);
        TextView allowRememberNotificationText = findViewById(R.id.settings_remember_text);
        TextView allowDailyNotificationText = findViewById(R.id.settings_daily_hints_text);

        allowNotifications.setVisibility(View.GONE);
        allowNotificationText.setVisibility(View.GONE);
        allowRememberNotification.setVisibility(View.GONE);
        allowRememberNotificationText.setVisibility(View.GONE);
        allowDailyNotifications.setVisibility(View.GONE);
        allowDailyNotificationText.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            allowNotifications.setVisibility(View.VISIBLE);
            allowNotificationText.setVisibility(View.VISIBLE);
            allowRememberNotification.setVisibility(View.VISIBLE);
            allowRememberNotificationText.setVisibility(View.VISIBLE);
            allowDailyNotifications.setVisibility(View.VISIBLE);
            allowDailyNotificationText.setVisibility(View.VISIBLE);

            if (!isChannelPermissionGranted(this, CHANNEL_ID_BACKGROUND) ||
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
                dataManager.saveToJSONSettings("allowNotifications", false, getMainActivityContext());
                dataManager.saveToJSONSettings("allowRememberNotifications", false, getMainActivityContext());
                dataManager.saveToJSONSettings("allowDailyNotifications", false, getMainActivityContext());
                allowNotifications.setChecked(false);
                allowRememberNotification.setChecked(false);
                allowDailyNotifications.setChecked(false);
            } else {
                try {
                    allowNotifications.setChecked((ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                            dataManager.getJSONSettingsData("allowNotification", getMainActivityContext()).getString("value").equals("true"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!isChannelPermissionGranted(this, CHANNEL_ID_REMEMBER)) {
                dataManager.saveToJSONSettings("allowRememberNotifications", false, getMainActivityContext());
                allowRememberNotification.setChecked(false);
            } else {
                try {
                    allowRememberNotification.setChecked((ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                            isChannelPermissionGranted(this, CHANNEL_ID_REMEMBER) &&
                            dataManager.getJSONSettingsData("allowNotification", getMainActivityContext()).getString("value").equals("true") &&
                            dataManager.getJSONSettingsData("allowRememberNotificationsActive", getApplicationContext()).getString("value").equals("true"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!isChannelPermissionGranted(this, CHANNEL_ID_HINTS)) {
                dataManager.saveToJSONSettings("allowDailyNotifications", false, getMainActivityContext());
                allowDailyNotifications.setChecked(false);
            } else {
                try {
                    allowDailyNotifications.setChecked((ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                            isChannelPermissionGranted(this, CHANNEL_ID_HINTS) &&
                            dataManager.getJSONSettingsData("allowNotification", getMainActivityContext()).getString("value").equals("true") &&
                            dataManager.getJSONSettingsData("allowDailyNotificationsActive", getApplicationContext()).getString("value").equals("true"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            allowNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                boolean isPermissionGranted = isNotificationPermissionGranted();
                if (!isPermissionGranted) {
                    requestNotificationPermission();
                }

                if (!isChannelPermissionGranted(this, CHANNEL_ID_BACKGROUND)) {
                    dataManager.saveToJSONSettings("allowNotifications", false, getMainActivityContext());
                    activateNotificationMessage();
                    allowNotifications.setChecked(false);
                    return;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    //Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    //intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                    //this.startActivity(intent);

                    //activateNotificationMessage();
                    requestNotificationPermission();
                    allowNotifications.setChecked(false);
                    return;
                }

                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                            isChannelPermissionGranted(this, CHANNEL_ID_BACKGROUND)) {
                        dataManager.saveToJSONSettings("allowNotification", true, getMainActivityContext());
                        allowNotifications.setChecked(true);

                        try {
                            if (dataManager.getJSONSettingsData("allowRememberNotificationsActive", getApplicationContext()).getString("value").equals("true")) {
                                dataManager.saveToJSONSettings("allowRememberNotifications", true, getMainActivityContext());
                                allowRememberNotification.setChecked(true);
                            }
                            if (dataManager.getJSONSettingsData("allowDailyNotificationsActive", getApplicationContext()).getString("value").equals("true")) {
                                dataManager.saveToJSONSettings("allowDailyNotifications", true, getMainActivityContext());
                                allowDailyNotifications.setChecked(true);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        activateChannelMessage();
                        dataManager.saveToJSONSettings("allowNotification", false, getMainActivityContext());
                        dataManager.saveToJSONSettings("allowRememberNotifications", false, getMainActivityContext());
                        dataManager.saveToJSONSettings("allowDailyNotifications", false, getMainActivityContext());
                        allowNotifications.setChecked(false);
                        allowDailyNotifications.setChecked(false);
                        allowRememberNotification.setChecked(false);
                    }
                } else {
                    dataManager.saveToJSONSettings("allowNotification", false, getMainActivityContext());
                    allowNotifications.setChecked(false);
                    allowDailyNotifications.setChecked(false);
                    allowRememberNotification.setChecked(false);
                }
                try {
                    Log.i("Settings", "allowNotification=" + dataManager.getJSONSettingsData("allowNotification", getMainActivityContext()).getString("value"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
            allowRememberNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !allowNotifications.isChecked()) {
                    dataManager.saveToJSONSettings("allowRememberNotifications", false, getMainActivityContext());
                    turnOnNotificationsMessage();
                    allowRememberNotification.setChecked(false);
                    return;
                }
                if (!isChannelPermissionGranted(this, CHANNEL_ID_REMEMBER)) {
                    dataManager.saveToJSONSettings("allowRememberNotifications", false, getMainActivityContext());
                    allowRememberNotification.setChecked(false);
                    return;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    allowRememberNotification.setChecked(false);
                    return;
                }

                if (isChecked) {
                    dataManager.saveToJSONSettings("allowRememberNotifications", true, getMainActivityContext());
                    dataManager.saveToJSONSettings("allowRememberNotificationsActive", true, getApplicationContext());
                    allowRememberNotification.setChecked(true);
                } else {
                    allowRememberNotification.setChecked(false);
                    dataManager.saveToJSONSettings("allowRememberNotifications", false, getMainActivityContext());
                    if (allowNotifications.isChecked()) {
                        dataManager.saveToJSONSettings("allowRememberNotificationsActive", false, getApplicationContext());
                    }

                }
                try {
                    Log.i("Settings", "allowRememberNotifications=" + dataManager.getJSONSettingsData("allowRememberNotifications", getMainActivityContext()).getString("value"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
            allowDailyNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !allowNotifications.isChecked()) {
                    dataManager.saveToJSONSettings("allowDailyNotifications", false, getMainActivityContext());
                    turnOnNotificationsMessage();
                    allowDailyNotifications.setChecked(false);
                    return;
                }
                if (!isChannelPermissionGranted(this, CHANNEL_ID_HINTS)) {
                    dataManager.saveToJSONSettings("allowDailyNotifications", false, getMainActivityContext());
                    allowDailyNotifications.setChecked(false);
                    return;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    allowDailyNotifications.setChecked(false);
                    return;
                }

                if (isChecked) {
                    dataManager.saveToJSONSettings("allowDailyNotifications", true, getMainActivityContext());
                    dataManager.saveToJSONSettings("allowDailyNotificationsActive", true, getApplicationContext());
                    allowDailyNotifications.setChecked(true);
                } else {
                    allowDailyNotifications.setChecked(false);
                    dataManager.saveToJSONSettings("allowDailyNotifications", false, getMainActivityContext());
                    if (allowNotifications.isChecked()) {
                        dataManager.saveToJSONSettings("allowDailyNotificationsActive", false, getApplicationContext());
                    }
                }
                try {
                    Log.i("Settings", "allowDailyNotifications=" + dataManager.getJSONSettingsData("allowDailyNotifications", getMainActivityContext()).getString("value"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void requestNotificationPermission() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isPermissionGranted = sharedPreferences.getBoolean(PERMISSION_GRANTED_KEY, false);

        if (!isPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
                }
            }
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            savePermissionStatus(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Switch allowNotifications = findViewById(R.id.settings_notifications);
                Switch allowRememberNotification = findViewById(R.id.settings_remember);
                Switch allowDailyNotifications = findViewById(R.id.settings_daily_hints);

                dataManager.saveToJSONSettings("allowNotification", true, getMainActivityContext());
                dataManager.saveToJSONSettings("allowRememberNotifications", true, getMainActivityContext());
                dataManager.saveToJSONSettings("allowDailyNotifications", true, getMainActivityContext());
                allowNotifications.setChecked(true);
                allowDailyNotifications.setChecked(true);
                allowRememberNotification.setChecked(true);

                createNotificationButtonListeners();
            }
        }
    }

    private boolean isNotificationPermissionGranted() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(PERMISSION_GRANTED_KEY, false);
    }

    private void savePermissionStatus(boolean isGranted) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PERMISSION_GRANTED_KEY, isGranted);
        editor.apply();
    }

    private void turnOnNotificationsMessage() {
        showToastLong(getString(R.string.enableNotifications), this);
    }

    private void activateChannelMessage() {
        showToastLong(getString(R.string.activateChannel), this);
    }

    private void activateNotificationMessage() {
        showToastLong(getString(R.string.activateNotifications), this);
    }

    private boolean isChannelPermissionGranted(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
                if (channel != null) {
                    return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * onPause method is called when the activity is paused.
     * It starts the background service.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            createNotificationButtonListeners();
        }
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
     * This method updates the display mode and text color of a spinner based on the selected setting.
     * It first checks the current night mode of the device.
     * Then it reads the selected setting from the spinner.
     * If the selected setting is "Dunkelmodus", it saves "Dark" to the JSON file, switches the display mode, and sets the text color to white or darkmode_white based on the "settingsTrueDarkMode" value in the JSON file.
     * If the selected setting is "Tageslichtmodus", it saves "Light" to the JSON file, switches the display mode, and sets the text color to black.
     * If the selected setting is "Systemstandard", it saves "System" to the JSON file, switches the display mode, and sets the text color to white, darkmode_white, or black based on the current night mode and the "settingsTrueDarkMode" value in the JSON file.
     *
     * @param parent The AdapterView where the selection happened. This is used to get the selected setting and the TextView object.
     */
    public void updateSpinner(AdapterView<?> parent) {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        final int readselectedSetting = parent.getSelectedItemPosition();

        // Check if the TextView object is null before calling methods on it
        TextView textView = null;
        if (parent.getChildAt(0) instanceof TextView) {
            textView = (TextView) parent.getChildAt(0);
        }

        if (textView != null) {
            textView.setTextSize(20);
            switch (readselectedSetting) {
                case 2:
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "Dark", getMainActivityContext());
                    switchDisplayMode(currentNightMode);
                    try {
                        if (dataManager.getJSONSettingsData("settingsTrueDarkMode", getApplicationContext()).getString("value").equals("true")) {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                        } else {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case 1:
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "Light", getMainActivityContext());
                    textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    switchDisplayMode(currentNightMode);
                    break;
                case 0:
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "System", getMainActivityContext());
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        try {
                            if (dataManager.getJSONSettingsData("settingsTrueDarkMode", getApplicationContext()).getString("value").equals("true")) {
                                textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                            } else {
                                textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    }
                    switchDisplayMode(currentNightMode);
                    break;
            }
        }
    }

    /**
     * This method updates the second spinner based on the selected setting.
     *
     * @param parent The AdapterView where the selection happened.
     */
    public void updateSpinner2(AdapterView<?> parent) {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        final String readselectedSetting;
        try {
            readselectedSetting = dataManager.getJSONSettingsData("selectedSpinnerSetting", getMainActivityContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Check if the TextView object is null before calling methods on it
        TextView textView = null;
        if (parent.getChildAt(0) instanceof TextView) {
            textView = (TextView) parent.getChildAt(0);
        }

        if (textView != null) {
            textView.setTextSize(20);
            switch (readselectedSetting) {
                case "Dark":
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "Dark", getMainActivityContext());
                    try {
                        if (dataManager.getJSONSettingsData("settingsTrueDarkMode", getApplicationContext()).getString("value").equals("true")) {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                        } else {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Light":
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "Light", getMainActivityContext());
                    textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    break;
                case "System":
                    dataManager.saveToJSONSettings("selectedSpinnerSetting", "System", getMainActivityContext());
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        try {
                            if (dataManager.getJSONSettingsData("settingsTrueDarkMode", getApplicationContext()).getString("value").equals("true")) {
                                textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                            } else {
                                textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    }
                    break;
            }
        }
    }

    /**
     * This method updates the second spinner based on the selected setting.
     *
     * @param parent The AdapterView where the selection happened.
     */
    public void updateSpinnerFunctionMode(AdapterView<?> parent) {
        // Check if the TextView object is null before calling methods on it
        TextView textView = null;
        if (parent.getChildAt(0) instanceof TextView) {
            textView = (TextView) parent.getChildAt(0);
        }

        if (textView != null) {
            textView.setTextSize(20);
            try {
                switch (dataManager.getJSONSettingsData("functionMode", getMainActivityContext()).getString("value")) {
                    case "Rad":
                        dataManager.saveToJSONSettings("functionMode", "Rad", getMainActivityContext());
                        break;
                    case "Deg":
                        dataManager.saveToJSONSettings("functionMode", "Deg", getMainActivityContext());
                        break;
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method gets the selected setting.
     *
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
     * This method updates the state of a switch view based on a key.
     *
     * @param switchView The switch view to update.
     * @param key        The key to use to get the value.
     */
    private void updateSwitchState(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchView, String key) {
        try {
            String value = dataManager.getJSONSettingsData(key, this).getString("value");
            switchView.setChecked(Boolean.parseBoolean(value));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This static method sets the context of the MainActivity.
     *
     * @param activity The MainActivity whose context is to be set.
     */
    public static void setMainActivityContext(MainActivity activity) {
        mainActivity = activity;
    }

    /**
     * This method gets the context of the MainActivity.
     *
     * @return The context of the MainActivity.
     */
    public Context getMainActivityContext() {
        return mainActivity;
    }

    /**
     * This method is called when the configuration of the device changes.
     *
     * @param newConfig The new device configuration.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switchDisplayMode(currentNightMode);
    }

    /**
     * This method switches the display mode based on the current night mode.
     *
     * @param currentNightMode The current night mode.
     */
    @SuppressLint({"ResourceType", "UseCompatLoadingForDrawables", "CutPasteId"})
    private void switchDisplayMode(int currentNightMode) {
        Button helpButton = findViewById(R.id.help_button);
        Button backbutton = findViewById(R.id.report_return_button);
        Button reportButton = findViewById(R.id.report_button);

        Spinner[] spinners = {
                findViewById(R.id.settings_display_mode_spinner),
                findViewById(R.id.settings_function_spinner),
                findViewById(R.id.settings_calculation_mode_spinner)
        };
        for (Spinner spinner : spinners) {
            updateSpinner2(spinner);
        }

        if (getSelectedSetting() != null) {
            if (getSelectedSetting().equals("Systemstandard")) {
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Nightmode is activated
                        dataManager = new DataManager();
                        String trueDarkMode = null;
                        try {
                            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        if (trueDarkMode != null) {
                            if (trueDarkMode.equals("false")) {
                                updateUI(R.color.black, R.color.white);

                                if (backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                                }
                                if (helpButton != null) {
                                    helpButton.setForeground(getDrawable(R.drawable.help_light));
                                }
                                if (reportButton != null) {
                                    reportButton.setForeground(getDrawable(R.drawable.report_light));
                                }

                                customListDisplayMode = new ArrayList<>();
                                customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings_light));
                                customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day_light));
                                customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night_light));

                                customListCalculationMode = new ArrayList<>();
                                customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.advanced_light));
                                customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.settings_light));

                                customListFunctionMode = new ArrayList<>();
                                customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree_light));
                                customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian_light));

                                customListHistoryMode = new ArrayList<>();
                                customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline_light));
                                customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline_light));

                                customListDecimalPoints = new ArrayList<>();
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));

                                customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                                customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                                customAdapter3 = new CustomAdapter(this, customListFunctionMode);
                                customAdapter4 = new CustomAdapter(this, customListHistoryMode);
                                customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

                                String color = "#FFFFFF";
                                String backgroundColor = "#151515";

                                CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3, customAdapter4, customAdapter5};

                                for (CustomAdapter adapter : adapters) {
                                    adapter.setTextColor(Color.parseColor(color));
                                    adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                                }

                            } else {
                                updateUI(R.color.darkmode_black, R.color.darkmode_white);

                                if (backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                                }
                                if (helpButton != null) {
                                    helpButton.setForeground(getDrawable(R.drawable.help_true_darkmode));
                                }
                                if (reportButton != null) {
                                    reportButton.setForeground(getDrawable(R.drawable.report_true_darkmode));
                                }

                                customListDisplayMode = new ArrayList<>();
                                customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings_true_darkmode));
                                customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day_true_darkmode));
                                customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night_true_darkmode));

                                customListCalculationMode = new ArrayList<>();
                                customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.advanced_true_darkmode));
                                customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.settings_true_darkmode));

                                customListFunctionMode = new ArrayList<>();
                                customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree_true_darkmode));
                                customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian_true_darkmode));

                                customListHistoryMode = new ArrayList<>();
                                customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline_true_darkmode));
                                customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline_true_darkmode));

                                customListDecimalPoints = new ArrayList<>();
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
                                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));

                                customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                                customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                                customAdapter3 = new CustomAdapter(this, customListFunctionMode);
                                customAdapter4 = new CustomAdapter(this, customListHistoryMode);
                                customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

                                String color = "#D5D5D5";
                                String backgroundColor = "#000000";

                                CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3, customAdapter4, customAdapter5};

                                for (CustomAdapter adapter : adapters) {
                                    adapter.setTextColor(Color.parseColor(color));
                                    adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                                }
                            }
                            customSpinner1.setAdapter(customAdapter1);
                            customSpinner2.setAdapter(customAdapter2);
                            customSpinner3.setAdapter(customAdapter3);
                            customSpinner4.setAdapter(customAdapter4);
                            customSpinner5.setAdapter(customAdapter5);

                            isProgrammaticChange = true;
                            customSpinner1.setSelection(0);
                        } else {
                            if (backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                            }
                            if (helpButton != null) {
                                helpButton.setForeground(getDrawable(R.drawable.help_light));
                            }
                            if (reportButton != null) {
                                reportButton.setForeground(getDrawable(R.drawable.report_light));
                            }

                            customListDisplayMode = new ArrayList<>();
                            customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings_light));
                            customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day_light));
                            customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night_light));

                            customListCalculationMode = new ArrayList<>();
                            customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.advanced_light));
                            customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.settings_light));

                            customListFunctionMode = new ArrayList<>();
                            customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree_light));
                            customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian_light));

                            customListHistoryMode = new ArrayList<>();
                            customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline_light));
                            customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline_light));

                            customListDecimalPoints = new ArrayList<>();
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
                            customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));

                            customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                            customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                            customAdapter3 = new CustomAdapter(this, customListFunctionMode);
                            customAdapter4 = new CustomAdapter(this, customListHistoryMode);
                            customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

                            String color = "#FFFFFF";
                            String backgroundColor = "#151515";

                            CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3, customAdapter4, customAdapter5};

                            for (CustomAdapter adapter : adapters) {
                                adapter.setTextColor(Color.parseColor(color));
                                adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                            }

                            customSpinner1.setAdapter(customAdapter1);
                            customSpinner2.setAdapter(customAdapter2);
                            customSpinner3.setAdapter(customAdapter3);
                            customSpinner4.setAdapter(customAdapter4);
                            customSpinner5.setAdapter(customAdapter5);

                            isProgrammaticChange = true;
                            customSpinner1.setSelection(0);

                            updateUI(R.color.black, R.color.white);
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated
                        if (backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                        }
                        if (helpButton != null) {
                            helpButton.setForeground(getDrawable(R.drawable.help));
                        }
                        if (reportButton != null) {
                            reportButton.setForeground(getDrawable(R.drawable.report));
                        }

                        customListDisplayMode = new ArrayList<>();
                        customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings));
                        customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day));
                        customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night));

                        customListCalculationMode = new ArrayList<>();
                        customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.advanced));
                        customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.settings));

                        customListFunctionMode = new ArrayList<>();
                        customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree));
                        customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian));

                        customListHistoryMode = new ArrayList<>();
                        customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline));
                        customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline));

                        customListDecimalPoints = new ArrayList<>();
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));

                        customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                        customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                        customAdapter3 = new CustomAdapter(this, customListFunctionMode);
                        customAdapter4 = new CustomAdapter(this, customListHistoryMode);
                        customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

                        String color = "#000000";
                        String backgroundColor = "#FFFFFF";

                        CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3, customAdapter4, customAdapter5};

                        for (CustomAdapter adapter : adapters) {
                            adapter.setTextColor(Color.parseColor(color));
                            adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                        }

                        customSpinner1.setAdapter(customAdapter1);
                        customSpinner2.setAdapter(customAdapter2);
                        customSpinner3.setAdapter(customAdapter3);
                        customSpinner4.setAdapter(customAdapter4);
                        customSpinner5.setAdapter(customAdapter5);

                        isProgrammaticChange = true;
                        customSpinner1.setSelection(0);

                        updateUI(R.color.white, R.color.black);
                        break;
                }
            } else if (getSelectedSetting().equals("Tageslichtmodus")) {
                if (backbutton != null) {
                    backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                }
                if (helpButton != null) {
                    helpButton.setForeground(getDrawable(R.drawable.help));
                }
                if (reportButton != null) {
                    reportButton.setForeground(getDrawable(R.drawable.report));
                }

                customListDisplayMode = new ArrayList<>();
                customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings));
                customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day));
                customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night));

                customListCalculationMode = new ArrayList<>();
                customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.advanced));
                customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.settings));

                customListFunctionMode = new ArrayList<>();
                customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree));
                customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian));

                customListHistoryMode = new ArrayList<>();
                customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline));
                customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline));

                customListDecimalPoints = new ArrayList<>();
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
                customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));

                customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                customAdapter3 = new CustomAdapter(this, customListFunctionMode);
                customAdapter4 = new CustomAdapter(this, customListHistoryMode);
                customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

                String color = "#000000";
                String backgroundColor = "#FFFFFF";

                CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3, customAdapter4, customAdapter5};

                for (CustomAdapter adapter : adapters) {
                    adapter.setTextColor(Color.parseColor(color));
                    adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                }

                customSpinner1.setAdapter(customAdapter1);
                customSpinner2.setAdapter(customAdapter2);
                customSpinner3.setAdapter(customAdapter3);
                customSpinner4.setAdapter(customAdapter4);
                customSpinner5.setAdapter(customAdapter5);

                isProgrammaticChange = true;
                customSpinner1.setSelection(1);

                updateUI(R.color.white, R.color.black);
            } else if (getSelectedSetting().equals("Dunkelmodus")) {
                dataManager = new DataManager();
                String trueDarkMode = null;
                try {
                    trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (trueDarkMode != null) {
                    if (trueDarkMode.equals("false")) {
                        updateUI(R.color.black, R.color.white);
                        updateSpinner2(findViewById(R.id.settings_display_mode_spinner));

                        if (backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                        }
                        if (helpButton != null) {
                            helpButton.setForeground(getDrawable(R.drawable.help_light));
                        }
                        if (reportButton != null) {
                            reportButton.setForeground(getDrawable(R.drawable.report_light));
                        }

                        customListDisplayMode = new ArrayList<>();
                        customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings_light));
                        customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day_light));
                        customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night_light));

                        customListCalculationMode = new ArrayList<>();
                        customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.advanced_light));
                        customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.settings_light));

                        customListFunctionMode = new ArrayList<>();
                        customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree_light));
                        customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian_light));

                        customListHistoryMode = new ArrayList<>();
                        customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline_light));
                        customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline_light));

                        customListDecimalPoints = new ArrayList<>();
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));

                        customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                        customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                        customAdapter3 = new CustomAdapter(this, customListFunctionMode);
                        customAdapter4 = new CustomAdapter(this, customListHistoryMode);
                        customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

                        String color = "#FFFFFF";
                        String backgroundColor = "#151515";

                        CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3, customAdapter4, customAdapter5};

                        for (CustomAdapter adapter : adapters) {
                            adapter.setTextColor(Color.parseColor(color));
                            adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                        }

                    } else {
                        updateUI(R.color.darkmode_black, R.color.darkmode_white);
                        updateSpinner2(findViewById(R.id.settings_display_mode_spinner));

                        if (backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                        }
                        if (helpButton != null) {
                            helpButton.setForeground(getDrawable(R.drawable.help_true_darkmode));
                        }
                        if (reportButton != null) {
                            reportButton.setForeground(getDrawable(R.drawable.report_true_darkmode));
                        }

                        customListDisplayMode = new ArrayList<>();
                        customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings_true_darkmode));
                        customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day_true_darkmode));
                        customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night_true_darkmode));

                        customListCalculationMode = new ArrayList<>();
                        customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.advanced_true_darkmode));
                        customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.settings_true_darkmode));

                        customListFunctionMode = new ArrayList<>();
                        customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree_true_darkmode));
                        customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian_true_darkmode));

                        customListHistoryMode = new ArrayList<>();
                        customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline_true_darkmode));
                        customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline_true_darkmode));

                        customListDecimalPoints = new ArrayList<>();
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
                        customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));

                        customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                        customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                        customAdapter3 = new CustomAdapter(this, customListFunctionMode);
                        customAdapter4 = new CustomAdapter(this, customListHistoryMode);
                        customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

                        String color = "#D5D5D5";
                        String backgroundColor = "#000000";

                        CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3, customAdapter4, customAdapter5};

                        for (CustomAdapter adapter : adapters) {
                            adapter.setTextColor(Color.parseColor(color));
                            adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                        }
                    }

                    customSpinner1.setAdapter(customAdapter1);
                    customSpinner2.setAdapter(customAdapter2);
                    customSpinner3.setAdapter(customAdapter3);
                    customSpinner4.setAdapter(customAdapter4);
                    customSpinner5.setAdapter(customAdapter5);

                    isProgrammaticChange = true;
                    customSpinner1.setSelection(2);
                } else {
                    if (backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                    }
                    if (helpButton != null) {
                        helpButton.setForeground(getDrawable(R.drawable.help_light));
                    }
                    if (reportButton != null) {
                        reportButton.setForeground(getDrawable(R.drawable.report_light));
                    }

                    customListDisplayMode = new ArrayList<>();
                    customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings_light));
                    customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day_light));
                    customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night_light));

                    customListCalculationMode = new ArrayList<>();
                    customListCalculationMode.add(new CustomItems(getString(R.string.defaultCalculationMode), R.drawable.advanced_light));
                    customListCalculationMode.add(new CustomItems(getString(R.string.easyCalculationMode), R.drawable.settings_light));

                    customListFunctionMode = new ArrayList<>();
                    customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree_light));
                    customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian_light));

                    customListHistoryMode = new ArrayList<>();
                    customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeSingle), R.drawable.historymodesingleline_light));
                    customListHistoryMode.add(new CustomItems(getString(R.string.settingsHistoryModeMultiple), R.drawable.historymodemultipleline_light));

                    customListDecimalPoints = new ArrayList<>();
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints1)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints2)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints3)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints4)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints5)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints6)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints7)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints8)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints9)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints10)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints11)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints12)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints13)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints14)));
                    customListDecimalPoints.add(new CustomItems(getString(R.string.settingsDecimalPoints15)));

                    customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                    customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                    customAdapter3 = new CustomAdapter(this, customListFunctionMode);
                    customAdapter4 = new CustomAdapter(this, customListHistoryMode);
                    customAdapter5 = new CustomAdapter(this, customListDecimalPoints);

                    String color = "#FFFFFF";
                    String backgroundColor = "#151515";

                    CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3, customAdapter4, customAdapter5};

                    for (CustomAdapter adapter : adapters) {
                        adapter.setTextColor(Color.parseColor(color));
                        adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                    }

                    customSpinner1.setAdapter(customAdapter1);
                    customSpinner2.setAdapter(customAdapter2);
                    customSpinner3.setAdapter(customAdapter3);
                    customSpinner4.setAdapter(customAdapter4);
                    customSpinner5.setAdapter(customAdapter5);

                    isProgrammaticChange = true;
                    customSpinner1.setSelection(2);

                    updateUI(R.color.black, R.color.white);
                    updateSpinner2(findViewById(R.id.settings_display_mode_spinner));
                }
            }
            try {
                String mode;
                mode = dataManager.getJSONSettingsData("calculationMode", getMainActivityContext()).getString("value");
                int selection = mode.equals("Standard") ? 0 : 1;
                customSpinner2.setSelection(selection);

                mode = dataManager.getJSONSettingsData("functionMode", getMainActivityContext()).getString("value");
                selection = mode.equals("Deg") ? 0 : 1;
                customSpinner3.setSelection(selection);

                mode = dataManager.getJSONSettingsData("historyMode", getMainActivityContext()).getString("value");
                selection = mode.equals("single") ? 0 : 1;
                customSpinner4.setSelection(selection);

                mode = dataManager.getJSONSettingsData("numberOfDecimals", getMainActivityContext()).getString("value");
                customSpinner5.setSelection(Integer.parseInt(mode) - 1);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * This method updates the UI elements with the given background color and text color.
     *
     * @param backgroundColor The color to be used for the background.
     * @param textColor       The color to be used for the text.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUI(int backgroundColor, int textColor) {
        ScrollView settingsScrollView = findViewById(R.id.settings_sroll_textview);
        LinearLayout settingsLayout = findViewById(R.id.settings_layout);
        Button settingsReturnButton = findViewById(R.id.report_return_button);
        Button settingsHelpButton = findViewById(R.id.help_button);
        Button settingsReportButton = findViewById(R.id.report_button);
        TextView settingsTitle = findViewById(R.id.settings_title);
        //TextView settingsReleaseNotes = findViewById(R.id.settings_release_notes);
        //TextView settingsReleaseNotesText = findViewById(R.id.settings_release_notes_text);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch settingsTrueDarkMode = findViewById(R.id.settings_true_darkmode);
        TextView settingsTrueDarkModeText = findViewById(R.id.settings_true_darkmode_text);
        TextView settingsDisplayModeText = findViewById(R.id.settings_display_mode_text);
        TextView settingsDisplayModeTitle = findViewById(R.id.settings_display_mode_title);
        TextView allowNotifications = findViewById(R.id.settings_notifications);
        TextView allowNotificationsText = findViewById(R.id.settings_notifications_text);
        TextView allowRememberNotifications = findViewById(R.id.settings_remember);
        TextView allowRememberNotificationsText = findViewById(R.id.settings_remember_text);
        TextView allowDailyNotifications = findViewById(R.id.settings_daily_hints);
        TextView allowDailyNotificationsText = findViewById(R.id.settings_daily_hints_text);

        TextView settingsCalculationModeText = findViewById(R.id.settings_calculation_mode_text);
        TextView settingsCalculationModeTitle = findViewById(R.id.settings_calculation_mode_title);
        //TextView settingsCredits = findViewById(R.id.credits_view);
        //FrameLayout frameLayout = findViewById(R.id.copyrightFrameLayout);

        TextView settingsFunctionModeTitle = findViewById(R.id.settings_function_title);
        TextView settingsFunctionModeText = findViewById(R.id.settings_function_text);
        Switch settingPI = findViewById(R.id.settings_pi);
        TextView settingPIText = findViewById(R.id.settings_pi_text);

        TextView settingsHistoryMode = findViewById(R.id.settings_history_mode);
        TextView settingsHistoryModeText = findViewById(R.id.settings_history_mode_text);

        TextView settingsDecimalPointsTitle = findViewById(R.id.settings_decimalpoints_title);
        TextView settingsDecimalPointsText = findViewById(R.id.settings_decimalpoints_text);

        TextView settingsConverterDevelopmentMessage = findViewById(R.id.settingsConverterDevelopmentMessage);
        TextView settingsConverterDevelopmentMessageText = findViewById(R.id.settingsConverterDevelopmentMessageText);

        settingsLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsReturnButton.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsHelpButton.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsReportButton.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsTitle.setTextColor(ContextCompat.getColor(this, textColor));
        settingsTitle.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsScrollView.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        //settingsReleaseNotes.setTextColor(ContextCompat.getColor(this, textColor));
        //settingsReleaseNotesText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsTrueDarkMode.setTextColor(ContextCompat.getColor(this, textColor));
        settingsTrueDarkModeText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsDisplayModeText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsDisplayModeTitle.setTextColor(ContextCompat.getColor(this, textColor));
        allowNotifications.setTextColor(ContextCompat.getColor(this, textColor));
        allowNotificationsText.setTextColor(ContextCompat.getColor(this, textColor));
        allowRememberNotifications.setTextColor(ContextCompat.getColor(this, textColor));
        allowRememberNotificationsText.setTextColor(ContextCompat.getColor(this, textColor));
        allowDailyNotifications.setTextColor(ContextCompat.getColor(this, textColor));
        allowDailyNotificationsText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsCalculationModeText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsCalculationModeTitle.setTextColor(ContextCompat.getColor(this, textColor));
        //settingsCredits.setTextColor(ContextCompat.getColor(this, textColor));
        //settingsCredits.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        //frameLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));

        settingsFunctionModeTitle.setTextColor(ContextCompat.getColor(this, textColor));
        settingsFunctionModeText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsFunctionModeTitle.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsFunctionModeText.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingPI.setTextColor(ContextCompat.getColor(this, textColor));
        settingPI.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingPIText.setTextColor(ContextCompat.getColor(this, textColor));
        settingPIText.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsHistoryMode.setTextColor(ContextCompat.getColor(this, textColor));
        settingsHistoryMode.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsHistoryModeText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsHistoryModeText.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsDecimalPointsTitle.setTextColor(ContextCompat.getColor(this, textColor));
        settingsDecimalPointsText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsConverterDevelopmentMessage.setTextColor(ContextCompat.getColor(this, textColor));
        settingsConverterDevelopmentMessageText.setTextColor(ContextCompat.getColor(this, textColor));
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
     * This method appends spaces to the text of all Switch views in a layout.
     *
     * @param layout The layout containing the Switch views.
     */
    private void appendSpaceToSwitches(ViewGroup layout) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                if (v instanceof Switch) {
                    String switchText = ((Switch) v).getText().toString();
                    // Define a string of spaces
                    String space = "                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ";
                    switchText = switchText + space;
                    ((Switch) v).setText(switchText);
                } else if (v instanceof ViewGroup) {
                    appendSpaceToSwitches((ViewGroup) v);
                }
            }
        }
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     */
    public void returnToCalculator() {
        dataManager.saveToJSONSettings("lastActivity", "Main", getApplicationContext());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}