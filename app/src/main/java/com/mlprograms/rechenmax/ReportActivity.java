package com.mlprograms.rechenmax;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import static com.mlprograms.rechenmax.ToastHelper.*;

public class ReportActivity extends AppCompatActivity {
    private static SettingsActivity mainActivity;
    private DataManager dataManager;
    private String trueDarkMode = null;
    private boolean isReturn = false;
    private boolean isSendReport = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
        stopBackgroundService();

        dataManager = new DataManager();
        try {
            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        setUpButtonListeners();
        switchDisplayMode();
    }

    private void setUpButtonListeners() {
        final Button backButton = findViewById(R.id.report_return_button);
        final Button sendReportButton = findViewById(R.id.sendReportButton);

        backButton.setOnClickListener(v -> {
            isReturn = true;
            returnToSettings();
        });

        sendReportButton.setOnClickListener(v -> {
            final EditText title = findViewById(R.id.sendReportTitle);
            final EditText message = findViewById(R.id.sendReportMessage);

            if(title.getText().toString().isEmpty() || message.getText().toString().isEmpty()) {
                showToastLong(getString(R.string.sendReportFillAllBoxes), this);
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri data = Uri.parse("mailto:ml.programs.service@gmail.com?subject=" + title.getText().toString() + "&body=" + message.getText().toString());
                    intent.setData(data);
                    startActivity(intent);
                    isSendReport = true;
                } catch (android.content.ActivityNotFoundException e) {
                    showToastLong(getString(R.string.reportNoEmailClient), this);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isReturn) {
            startBackgroundService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isSendReport) {
            returnToCalculator();
            showToastLong(getString(R.string.sendReportThankYou), this);
            isSendReport = false;
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
     * This static method sets the context of the MainActivity.
     * @param activity The MainActivity whose context is to be set.
     */
    public static void setMainActivityContext(SettingsActivity activity) {
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
     * This method switches the display mode based on the current night mode.
     */
    private void switchDisplayMode() {
        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        Button backbutton = findViewById(R.id.report_return_button);

        if(getSelectedSetting() != null) {
            if(getSelectedSetting().equals("Systemstandard")) {
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
                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                                }
                                updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));
                            } else {
                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                                }
                                updateUI(Color.parseColor("#000000"), Color.parseColor("#D5D5D5"));
                            }
                        } else {
                            if(backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                            }
                            updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                        }
                        updateUI(Color.parseColor("#FFFFFF"), Color.parseColor("#151515"));
                        break;
                }
            } else if (getSelectedSetting().equals("Tageslichtmodus")) {
                if(backbutton != null) {
                    backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                }
                updateUI(Color.parseColor("#FFFFFF"), Color.parseColor("#151515"));
            } else if (getSelectedSetting().equals("Dunkelmodus")) {
                if (trueDarkMode != null) {
                    if (trueDarkMode.equals("false")) {
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                        }
                        updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));
                    } else {
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                        }
                        updateUI(Color.parseColor("#000000"), Color.parseColor("#D5D5D5"));
                    }
                } else {
                    if(backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                    }
                    updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));
                }
            }
        }
    }

    private void updateUI(int backgroundColor, int textColor) {
        LinearLayout reportUI = findViewById(R.id.reportUI);
        LinearLayout reportLayout = findViewById(R.id.report_layout);
        TextView textView = findViewById(R.id.report_title);
        Button sendButton = findViewById(R.id.sendReportButton);
        Button returnButton = findViewById(R.id.report_return_button);

        reportUI.setBackgroundColor(backgroundColor);
        reportLayout.setBackgroundColor(backgroundColor);
        textView.setTextColor(textColor);
        sendButton.setTextColor(textColor);
        sendButton.setBackgroundColor(backgroundColor);
        returnButton.setBackgroundColor(backgroundColor);
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
        isReturn = true;
        returnToSettings();
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     */
    public void returnToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     */
    public void returnToCalculator() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
