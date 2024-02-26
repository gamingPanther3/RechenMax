package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.BackgroundService.CHANNEL_ID_BACKGROUND;
import static com.mlprograms.rechenmax.BackgroundService.CHANNEL_ID_HINTS;
import static com.mlprograms.rechenmax.BackgroundService.CHANNEL_ID_REMEMBER;
import static com.mlprograms.rechenmax.BackgroundService.createNotificationChannel;
import static com.mlprograms.rechenmax.ToastHelper.showToastLong;
import static com.mlprograms.rechenmax.ToastHelper.showToastShort;

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

import java.util.ArrayList;
import java.util.Locale;

/**
 * SettingsActivity
 * @author Max Lemberg
 * @version 1.0.3
 * @date 14.02.2023
 */

// I don't remember what I was thinking, but it works
public class SettingsActivity extends AppCompatActivity {

    // Declare a DataManager object
    DataManager dataManager;
    private boolean isProgrammaticChange = false;
    private ArrayList<CustomItems> customListDisplayMode = new ArrayList<>();
    private ArrayList<CustomItems> customListCalculationMode = new ArrayList<>();
    private ArrayList<CustomItems> customListFunctionMode = new ArrayList<>();
    private CustomAdapter customAdapter1;
    private CustomAdapter customAdapter2;
    private CustomAdapter customAdapter3;
    private Spinner customSpinner1;
    private Spinner customSpinner2;
    private Spinner customSpinner3;
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
     *                         being created for the first time.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint({"UseCompatLoadingForDrawables", "UseSwitchCompatOrMaterialCode", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopBackgroundService();

        setContentView(R.layout.settings);

        dataManager = new DataManager();
        //dataManager.deleteJSON(getApplicationContext());
        dataManager.createJSON(getApplicationContext());
        //resetReleaseNoteConfig(getApplicationContext());

        @SuppressLint("CutPasteId") Button button = findViewById(R.id.settings_return_button);
        button.setOnClickListener(v -> returnToCalculator());

        findViewById(R.id.settingsUI);

        Switch settingsReleaseNotesSwitch = findViewById(R.id.settings_release_notes);
        Switch settingsTrueDarkMode = findViewById(R.id.settings_true_darkmode);
        Switch settingPI = findViewById(R.id.settings_pi);

        updateSwitchState(settingsReleaseNotesSwitch, "settingReleaseNotesSwitch");
        updateSwitchState(settingsTrueDarkMode, "settingsTrueDarkMode");
        updateSwitchState(settingPI, "refactorPI");

        appendSpaceToSwitches(findViewById(R.id.settingsUI));

        settingsReleaseNotesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dataManager.saveToJSON("settingReleaseNotesSwitch", isChecked, getApplicationContext());
            dataManager.saveToJSON("showPatchNotes", isChecked, getMainActivityContext());
            dataManager.saveToJSON("disablePatchNotesTemporary", isChecked, getMainActivityContext());
            Log.i("Settings", "settingReleaseNotesSwitch=" + dataManager.readFromJSON("settingReleaseNotesSwitch", getMainActivityContext()));
        });
        settingsTrueDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dataManager.saveToJSON("settingsTrueDarkMode", isChecked, getMainActivityContext());
            Log.i("Settings", "settingsTrueDarkMode=" + dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext()));

            switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        });
        settingPI.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dataManager.saveToJSON("refactorPI", isChecked, getMainActivityContext());
            Log.i("Settings", "refactorPI=" + dataManager.readFromJSON("refactorPI", getMainActivityContext()));
        });

        customListDisplayMode.add(new CustomItems(getString(R.string.systemDefault), R.drawable.settings));
        customListDisplayMode.add(new CustomItems(getString(R.string.lightMode), R.drawable.day));
        customListDisplayMode.add(new CustomItems(getString(R.string.darkmode), R.drawable.night));
        customAdapter1 = new CustomAdapter(this, customListDisplayMode);

        customSpinner1 = findViewById(R.id.settings_display_mode_spinner);
        if(customSpinner1 != null) {
            customSpinner1.setAdapter(customAdapter1);

            final String mode = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());
            if(mode.equals("System")) {
                customSpinner1.setSelection(0);
            } else if (mode.equals("Light")) {
                customSpinner1.setSelection(1);
            } else {
                customSpinner1.setSelection(2);
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
                    dataManager.saveToJSON("selectedSpinnerSetting", "System", getMainActivityContext());
                } else if (spinnerText.equals(getString(R.string.lightMode))) {
                    dataManager.saveToJSON("selectedSpinnerSetting", "Light", getMainActivityContext());
                } else {
                    dataManager.saveToJSON("selectedSpinnerSetting", "Dark", getMainActivityContext());
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
        if(customSpinner2 != null) {
            customSpinner2.setAdapter(customAdapter2);

            final String mode = dataManager.readFromJSON("calculationMode", getMainActivityContext());
            if(mode.equals("Standard")) {
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

                if(spinnerText.equals(getString(R.string.defaultCalculationMode))) {
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

        // Declare a Spinner object
        customListFunctionMode.add(new CustomItems(getString(R.string.degree), R.drawable.degree));
        customListFunctionMode.add(new CustomItems(getString(R.string.radian), R.drawable.radian));
        customAdapter3 = new CustomAdapter(this, customListFunctionMode);

        customSpinner3 = findViewById(R.id.settings_function_spinner);
        if(customSpinner3 != null) {
            customSpinner3.setAdapter(customAdapter3);

            final String mode = dataManager.readFromJSON("functionMode", getMainActivityContext());
            if(mode.equals(getString(R.string.degree))) {
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
                    dataManager.saveToJSON("functionMode", "Deg", getMainActivityContext());
                } else {
                    dataManager.saveToJSON("functionMode", "Rad", getMainActivityContext());
                }

                //updateSpinner2(adapterView);
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            allowNotifications.setVisibility(View.VISIBLE);
            allowNotificationText.setVisibility(View.VISIBLE);
            allowRememberNotification.setVisibility(View.VISIBLE);
            allowRememberNotificationText.setVisibility(View.VISIBLE);
            allowDailyNotifications.setVisibility(View.VISIBLE);
            allowDailyNotificationText.setVisibility(View.VISIBLE);

            if(!isChannelPermissionGranted(this, CHANNEL_ID_BACKGROUND) ||
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
                dataManager.saveToJSON("allowNotifications", false, getMainActivityContext());
                dataManager.saveToJSON("allowRememberNotifications", false, getMainActivityContext());
                dataManager.saveToJSON("allowDailyNotifications", false, getMainActivityContext());
                allowNotifications.setChecked(false);
                allowRememberNotification.setChecked(false);
                allowDailyNotifications.setChecked(false);
            } else {
                allowNotifications.setChecked((ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                        dataManager.readFromJSON("allowNotification", getMainActivityContext()).equals("true"));
            }

            if(!isChannelPermissionGranted(this, CHANNEL_ID_REMEMBER)) {
                dataManager.saveToJSON("allowRememberNotifications", false, getMainActivityContext());
                allowRememberNotification.setChecked(false);
            } else {
                allowRememberNotification.setChecked((ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                        isChannelPermissionGranted(this, CHANNEL_ID_REMEMBER) &&
                        dataManager.readFromJSON("allowNotification", getMainActivityContext()).equals("true") &&
                        dataManager.readFromJSON("allowRememberNotificationsActive", getApplicationContext()).equals("true"));
            }

            if(!isChannelPermissionGranted(this, CHANNEL_ID_HINTS)) {
                dataManager.saveToJSON("allowDailyNotifications", false, getMainActivityContext());
                allowDailyNotifications.setChecked(false);
            } else {
                allowDailyNotifications.setChecked((ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                        isChannelPermissionGranted(this, CHANNEL_ID_HINTS) &&
                        dataManager.readFromJSON("allowNotification", getMainActivityContext()).equals("true") &&
                        dataManager.readFromJSON("allowDailyNotificationsActive", getApplicationContext()).equals("true"));
            }

            allowNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                boolean isPermissionGranted = isNotificationPermissionGranted();
                if (!isPermissionGranted) {
                    requestNotificationPermission();
                }

                if(!isChannelPermissionGranted(this, CHANNEL_ID_BACKGROUND)) {
                    dataManager.saveToJSON("allowNotifications", false, getMainActivityContext());
                    activateNotificationMessage();
                    allowNotifications.setChecked(false);
                    return;
                }
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    //Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    //intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                    //this.startActivity(intent);

                    //activateNotificationMessage();
                    requestNotificationPermission();
                    allowNotifications.setChecked(false);
                    return;
                }

                if(isChecked) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                            isChannelPermissionGranted(this, CHANNEL_ID_BACKGROUND)) {
                        dataManager.saveToJSON("allowNotification", true, getMainActivityContext());
                        allowNotifications.setChecked(true);

                        if(dataManager.readFromJSON("allowRememberNotificationsActive", getApplicationContext()).equals("true")) {
                            dataManager.saveToJSON("allowRememberNotifications", true, getMainActivityContext());
                            allowRememberNotification.setChecked(true);
                        }
                        if(dataManager.readFromJSON("allowDailyNotificationsActive", getApplicationContext()).equals("true")) {
                            dataManager.saveToJSON("allowDailyNotifications", true, getMainActivityContext());
                            allowDailyNotifications.setChecked(true);
                        }
                    } else {
                        activateChannelMessage();
                        dataManager.saveToJSON("allowNotification", false, getMainActivityContext());
                        dataManager.saveToJSON("allowRememberNotifications", false, getMainActivityContext());
                        dataManager.saveToJSON("allowDailyNotifications", false, getMainActivityContext());
                        allowNotifications.setChecked(false);
                        allowDailyNotifications.setChecked(false);
                        allowRememberNotification.setChecked(false);
                    }
                } else {
                    dataManager.saveToJSON("allowNotification", false, getMainActivityContext());
                    allowNotifications.setChecked(false);
                    allowDailyNotifications.setChecked(false);
                    allowRememberNotification.setChecked(false);
                }
                Log.i("Settings", "allowNotification=" + dataManager.readFromJSON("allowNotification", getMainActivityContext()));
            });
            allowRememberNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked && !allowNotifications.isChecked()) {
                    dataManager.saveToJSON("allowRememberNotifications", false, getMainActivityContext());
                    turnOnNotificationsMessage();
                    allowRememberNotification.setChecked(false);
                    return;
                }
                if(!isChannelPermissionGranted(this, CHANNEL_ID_REMEMBER)) {
                    dataManager.saveToJSON("allowRememberNotifications", false, getMainActivityContext());
                    allowRememberNotification.setChecked(false);
                    return;
                }
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    allowRememberNotification.setChecked(false);
                    return;
                }

                if(isChecked) {
                    dataManager.saveToJSON("allowRememberNotifications", true, getMainActivityContext());
                    dataManager.saveToJSON("allowRememberNotificationsActive", true, getApplicationContext());
                    allowRememberNotification.setChecked(true);
                } else {
                    allowRememberNotification.setChecked(false);
                    dataManager.saveToJSON("allowRememberNotifications", false, getMainActivityContext());
                    if(allowNotifications.isChecked()) {
                        dataManager.saveToJSON("allowRememberNotificationsActive", false, getApplicationContext());
                    }

                }
                Log.i("Settings", "allowRememberNotifications=" + dataManager.readFromJSON("allowRememberNotifications", getMainActivityContext()));
            });
            allowDailyNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked && !allowNotifications.isChecked()) {
                    dataManager.saveToJSON("allowDailyNotifications", false, getMainActivityContext());
                    turnOnNotificationsMessage();
                    allowDailyNotifications.setChecked(false);
                    return;
                }
                if(!isChannelPermissionGranted(this, CHANNEL_ID_HINTS)) {
                    dataManager.saveToJSON("allowDailyNotifications", false, getMainActivityContext());
                    allowDailyNotifications.setChecked(false);
                    return;
                }
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    allowDailyNotifications.setChecked(false);
                    return;
                }

                if(isChecked) {
                    dataManager.saveToJSON("allowDailyNotifications", true, getMainActivityContext());
                    dataManager.saveToJSON("allowDailyNotificationsActive", true, getApplicationContext());
                    allowDailyNotifications.setChecked(true);
                } else {
                    allowDailyNotifications.setChecked(false);
                    dataManager.saveToJSON("allowDailyNotifications", false, getMainActivityContext());
                    if(allowNotifications.isChecked()) {
                        dataManager.saveToJSON("allowDailyNotificationsActive", false, getApplicationContext());
                    }
                }
                Log.i("Settings", "allowDailyNotifications=" + dataManager.readFromJSON("allowDailyNotifications", getMainActivityContext()));
            });
        }
    }

    public void requestNotificationPermission() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isPermissionGranted = sharedPreferences.getBoolean(PERMISSION_GRANTED_KEY, false);

        if (!isPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 100);
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

                dataManager.saveToJSON("allowNotification", true, getMainActivityContext());
                dataManager.saveToJSON("allowRememberNotifications", true, getMainActivityContext());
                dataManager.saveToJSON("allowDailyNotifications", true, getMainActivityContext());
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
        if(Locale.getDefault().getDisplayLanguage().equals("English")) {
            showToastShort("Turn on notifications first.", this);
        } else if(Locale.getDefault().getDisplayLanguage().equals("français")) {
            showToastShort("Activez d'abord les notifications.", this);
        } else if(Locale.getDefault().getDisplayLanguage().equals("español")) {
            showToastShort("Activa primero las notificaciones.", this);
        } else {
            showToastShort("Schalte vorher die Benachrichtigungen ein.", this);
        }
    }

    private void activateChannelMessage() {
        if(Locale.getDefault().getDisplayLanguage().equals("English")) {
            showToastLong("Please enable the channel in your phone's settings beforehand.", this);
        } else if(Locale.getDefault().getDisplayLanguage().equals("français")) {
            showToastLong("Veuillez activer le canal dans les paramètres de votre téléphone au préalable.", this);
        } else if(Locale.getDefault().getDisplayLanguage().equals("español")) {
            showToastLong("Por favor, activa el canal en la configuración de tu teléfono antes.", this);
        } else {
            showToastLong("Bitte aktiviere den Kanal zuvor in den Einstellungen deines Handys.", this);
        }
    }

    private void activateNotificationMessage() {
        if(Locale.getDefault().getDisplayLanguage().equals("Englisch")) {
            showToastLong("Please enable notifications in your phone's settings beforehand.", this);
        } else if(Locale.getDefault().getDisplayLanguage().equals("français")) {
            showToastLong("Veuillez activer les notifications dans les paramètres de votre téléphone au préalable.", this);
        } else if(Locale.getDefault().getDisplayLanguage().equals("español")) {
            showToastLong("Por favor, activa las notificaciones en la configuración de tu teléfono antes.", this);
        } else {
            showToastLong("Schalte vorher die Benachrichtigungen in den Einstellungen deines Handys ein.", this);
        }
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
        if(parent.getChildAt(0) instanceof TextView) {
            textView = (TextView) parent.getChildAt(0);
        }

        if(textView != null) {
            textView.setTextSize(20);
            switch (readselectedSetting) {
                case 2:
                    dataManager.saveToJSON("selectedSpinnerSetting", "Dark", getMainActivityContext());
                    switchDisplayMode(currentNightMode);
                    if(dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext()).equals("true")) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                    }
                    break;
                case 1:
                    dataManager.saveToJSON("selectedSpinnerSetting", "Light", getMainActivityContext());
                    textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    switchDisplayMode(currentNightMode);
                    break;
                case 0:
                    dataManager.saveToJSON("selectedSpinnerSetting", "System", getMainActivityContext());
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        if(dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext()).equals("true")) {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                        } else {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
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
     * @param parent The AdapterView where the selection happened.
     */
    public void updateSpinner2(AdapterView<?> parent) {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        final String readselectedSetting = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());

        // Check if the TextView object is null before calling methods on it
        TextView textView = null;
        if(parent.getChildAt(0) instanceof TextView) {
            textView = (TextView) parent.getChildAt(0);
        }

        if(textView != null) {
            textView.setTextSize(20);
            switch (readselectedSetting) {
                case "Dark":
                    dataManager.saveToJSON("selectedSpinnerSetting", "Dark", getMainActivityContext());
                    if(dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext()).equals("true")) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                    }
                    break;
                case "Light":
                    dataManager.saveToJSON("selectedSpinnerSetting", "Light", getMainActivityContext());
                    textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    break;
                case "System":
                    dataManager.saveToJSON("selectedSpinnerSetting", "System", getMainActivityContext());
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        if(dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext()).equals("true")) {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                        } else {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
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
     * @param parent The AdapterView where the selection happened.
     */
    public void updateSpinnerFunctionMode(AdapterView<?> parent) {
        // Check if the TextView object is null before calling methods on it
        TextView textView = null;
        if(parent.getChildAt(0) instanceof TextView) {
            textView = (TextView) parent.getChildAt(0);
        }

        if(textView != null) {
            textView.setTextSize(20);
            switch (dataManager.readFromJSON("functionMode", getMainActivityContext())) {
                case "Rad":
                    dataManager.saveToJSON("functionMode", "Rad", getMainActivityContext());
                    break;
                case "Deg":
                    dataManager.saveToJSON("functionMode", "Deg", getMainActivityContext());
                    break;
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
     * This method updates the state of a switch view based on a key.
     * @param switchView The switch view to update.
     * @param key The key to use to get the value.
     */
    private void updateSwitchState(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchView, String key) {
        String value = dataManager.readFromJSON(key, this);
        if (value != null) {
            switchView.setChecked(Boolean.parseBoolean(value));
        } else {
            Log.e("Settings", "Failed to read value for key: " + key);
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
     * This method switches the display mode based on the current night mode.
     * @param currentNightMode The current night mode.
     */
    @SuppressLint({"ResourceType", "UseCompatLoadingForDrawables", "CutPasteId"})
    private void switchDisplayMode(int currentNightMode) {
        Button helpButton = findViewById(R.id.help_button);
        Button backbutton = findViewById(R.id.settings_return_button);

        Spinner[] spinners = {
                findViewById(R.id.settings_display_mode_spinner),
                findViewById(R.id.settings_function_spinner),
                findViewById(R.id.settings_calculation_mode_spinner)
        };
        for (Spinner spinner : spinners) {
            updateSpinner2(spinner);
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
                                updateUI(R.color.black, R.color.white);

                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                                }
                                if (helpButton != null) {
                                    helpButton.setForeground(getDrawable(R.drawable.help_light));
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

                                customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                                customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                                customAdapter3 = new CustomAdapter(this, customListFunctionMode);

                                String color = "#FFFFFF";
                                String backgroundColor = "#151515";

                                CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3};

                                for (CustomAdapter adapter : adapters) {
                                    adapter.setTextColor(Color.parseColor(color));
                                    adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                                }

                            } else {
                                updateUI(R.color.darkmode_black, R.color.darkmode_white);

                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                                }
                                if (helpButton != null) {
                                    helpButton.setForeground(getDrawable(R.drawable.help_true_darkmode));
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

                                customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                                customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                                customAdapter3 = new CustomAdapter(this, customListFunctionMode);

                                String color = "#D5D5D5";
                                String backgroundColor = "#000000";

                                CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3};

                                for (CustomAdapter adapter : adapters) {
                                    adapter.setTextColor(Color.parseColor(color));
                                    adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                                }
                            }
                            customSpinner1.setAdapter(customAdapter1);
                            customSpinner2.setAdapter(customAdapter2);
                            customSpinner3.setAdapter(customAdapter3);

                            isProgrammaticChange = true;
                            customSpinner1.setSelection(0);
                        } else {
                            if(backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                            }
                            if (helpButton != null) {
                                helpButton.setForeground(getDrawable(R.drawable.help_light));
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

                            customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                            customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                            customAdapter3 = new CustomAdapter(this, customListFunctionMode);

                            String color = "#FFFFFF";
                            String backgroundColor = "#151515";

                            CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3};

                            for (CustomAdapter adapter : adapters) {
                                adapter.setTextColor(Color.parseColor(color));
                                adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                            }

                            customSpinner1.setAdapter(customAdapter1);
                            customSpinner2.setAdapter(customAdapter2);
                            customSpinner3.setAdapter(customAdapter3);

                            isProgrammaticChange = true;
                            customSpinner1.setSelection(0);

                            updateUI(R.color.black, R.color.white);
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                        }
                        if (helpButton != null) {
                            helpButton.setForeground(getDrawable(R.drawable.help));
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

                        customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                        customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                        customAdapter3 = new CustomAdapter(this, customListFunctionMode);

                        String color = "#000000";
                        String backgroundColor = "#FFFFFF";

                        CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3};

                        for (CustomAdapter adapter : adapters) {
                            adapter.setTextColor(Color.parseColor(color));
                            adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                        }

                        customSpinner1.setAdapter(customAdapter1);
                        customSpinner2.setAdapter(customAdapter2);
                        customSpinner3.setAdapter(customAdapter3);

                        isProgrammaticChange = true;
                        customSpinner1.setSelection(0);

                        updateUI(R.color.white, R.color.black);
                        break;
                }
            } else if (getSelectedSetting().equals("Tageslichtmodus")) {
                if(backbutton != null) {
                    backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                }
                if (helpButton != null) {
                    helpButton.setForeground(getDrawable(R.drawable.help));
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

                customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                customAdapter3 = new CustomAdapter(this, customListFunctionMode);

                String color = "#000000";
                String backgroundColor = "#FFFFFF";

                CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3};

                for (CustomAdapter adapter : adapters) {
                    adapter.setTextColor(Color.parseColor(color));
                    adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                }

                customSpinner1.setAdapter(customAdapter1);
                customSpinner2.setAdapter(customAdapter2);
                customSpinner3.setAdapter(customAdapter3);

                isProgrammaticChange = true;
                customSpinner1.setSelection(1);

                updateUI(R.color.white, R.color.black);
            } else if (getSelectedSetting().equals("Dunkelmodus")) {
                dataManager = new DataManager();
                String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

                if (trueDarkMode != null) {
                    if (trueDarkMode.equals("false")) {
                        updateUI(R.color.black, R.color.white);
                        updateSpinner2(findViewById(R.id.settings_display_mode_spinner));

                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                        }
                        if (helpButton != null) {
                            helpButton.setForeground(getDrawable(R.drawable.help_light));
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

                        customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                        customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                        customAdapter3 = new CustomAdapter(this, customListFunctionMode);

                        String color = "#FFFFFF";
                        String backgroundColor = "#151515";

                        CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3};

                        for (CustomAdapter adapter : adapters) {
                            adapter.setTextColor(Color.parseColor(color));
                            adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                        }

                    } else {
                        updateUI(R.color.darkmode_black, R.color.darkmode_white);
                        updateSpinner2(findViewById(R.id.settings_display_mode_spinner));

                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                        }
                        if (helpButton != null) {
                            helpButton.setForeground(getDrawable(R.drawable.help_true_darkmode));
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

                        customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                        customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                        customAdapter3 = new CustomAdapter(this, customListFunctionMode);

                        String color = "#D5D5D5";
                        String backgroundColor = "#000000";

                        CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3};

                        for (CustomAdapter adapter : adapters) {
                            adapter.setTextColor(Color.parseColor(color));
                            adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                        }
                    }

                    customSpinner1.setAdapter(customAdapter1);
                    customSpinner2.setAdapter(customAdapter2);
                    customSpinner3.setAdapter(customAdapter3);

                    isProgrammaticChange = true;
                    customSpinner1.setSelection(2);
                } else {
                    if(backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                    }
                    if (helpButton != null) {
                        helpButton.setForeground(getDrawable(R.drawable.help_light));
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

                    customAdapter1 = new CustomAdapter(this, customListDisplayMode);
                    customAdapter2 = new CustomAdapter(this, customListCalculationMode);
                    customAdapter3 = new CustomAdapter(this, customListFunctionMode);

                    String color = "#FFFFFF";
                    String backgroundColor = "#151515";

                    CustomAdapter[] adapters = {customAdapter1, customAdapter2, customAdapter3};

                    for (CustomAdapter adapter : adapters) {
                        adapter.setTextColor(Color.parseColor(color));
                        adapter.setBackgroundColor(Color.parseColor(backgroundColor));
                    }

                    customSpinner1.setAdapter(customAdapter1);
                    customSpinner2.setAdapter(customAdapter2);
                    isProgrammaticChange = true;
                    customSpinner1.setSelection(2);

                    updateUI(R.color.black, R.color.white);
                    updateSpinner2(findViewById(R.id.settings_display_mode_spinner));
                }
            }
            String calculationMode = dataManager.readFromJSON("calculationMode", getMainActivityContext());
            int selection = calculationMode.equals("Standard") ? 0 : 1;
            customSpinner2.setSelection(selection);

            calculationMode = dataManager.readFromJSON("functionMode", getMainActivityContext());
            selection = calculationMode.equals("Deg") ? 0 : 1;
            customSpinner3.setSelection(selection);

        }
    }

    /**
     * This method updates the UI elements with the given background color and text color.
     * @param backgroundColor The color to be used for the background.
     * @param textColor The color to be used for the text.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUI(int backgroundColor, int textColor) {
        ScrollView settingsScrollView = findViewById(R.id.settings_sroll_textview);
        LinearLayout settingsLayout = findViewById(R.id.settings_layout);
        Button settingsReturnButton = findViewById(R.id.settings_return_button);
        Button settingsHelpButton = findViewById(R.id.help_button);
        TextView settingsTitle = findViewById(R.id.settings_title);
        TextView settingsReleaseNotes = findViewById(R.id.settings_release_notes);
        TextView settingsReleaseNotesText = findViewById(R.id.settings_release_notes_text);

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
        TextView settingsCredits = findViewById(R.id.credits_view);
        FrameLayout frameLayout = findViewById(R.id.copyrightFrameLayout);

        TextView settingsFunctionModeTitle = findViewById(R.id.settings_function_title);
        TextView settingsFunctionModeText = findViewById(R.id.settings_function_text);
        Switch settingPI = findViewById(R.id.settings_pi);
        TextView settingPIText = findViewById(R.id.settings_pi_text);

        settingsLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsReturnButton.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsHelpButton.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsTitle.setTextColor(ContextCompat.getColor(this, textColor));
        settingsTitle.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsScrollView.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsReleaseNotes.setTextColor(ContextCompat.getColor(this, textColor));
        settingsReleaseNotesText.setTextColor(ContextCompat.getColor(this, textColor));
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
        settingsCredits.setTextColor(ContextCompat.getColor(this, textColor));
        settingsCredits.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        frameLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));

        settingsFunctionModeTitle.setTextColor(ContextCompat.getColor(this, textColor));
        settingsFunctionModeText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsFunctionModeTitle.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsFunctionModeText.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingPI.setTextColor(ContextCompat.getColor(this, textColor));
        settingPI.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingPIText.setTextColor(ContextCompat.getColor(this, textColor));
        settingPIText.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
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
                }
                else if (v instanceof ViewGroup) {
                    appendSpaceToSwitches((ViewGroup) v);
                }
            }
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