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
import static com.mlprograms.rechenmax.CalculatorEngine.calculate;
import static com.mlprograms.rechenmax.CalculatorEngine.fixExpression;
import static com.mlprograms.rechenmax.CalculatorEngine.isFunction;
import static com.mlprograms.rechenmax.CalculatorEngine.isOperator;
import static com.mlprograms.rechenmax.CalculatorEngine.setMainActivity;
import static com.mlprograms.rechenmax.NumberHelper.PI;
import static com.mlprograms.rechenmax.ParenthesesBalancer.balanceParentheses;
import static com.mlprograms.rechenmax.ToastHelper.showToastLong;
import static com.mlprograms.rechenmax.ToastHelper.showToastShort;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MainActivity
 * @author Max Lemberg
 * @version 1.8.2
 * @date 17.05.2024
 */

public class MainActivity extends AppCompatActivity {
    private Context context = this;
    private DataManager dataManager;
    private InAppUpdate inAppUpdate;

    private int newColorBTNForegroundAccent;
    private int newColorBTNBackgroundAccent;

    private static final String PREFS_NAME = "NotificationPermissionPrefs";
    private static final String PERMISSION_GRANTED_KEY = "permission_granted";

    private HorizontalScrollView horizontalScrollView;
    private TextView textView;
    private int characterCount = 0;
    private StringBuilder textBuilder = new StringBuilder();

    public static final String SINE = "sin(";
    public static final String A_SINE = "sin⁻¹(";
    public static final String SINE_H = "sinh(";
    public static final String A_SINE_H = "sinh⁻¹(";
    public static final String COSINE = "cos(";
    public static final String A_COSINE = "cos⁻¹(";
    public static final String COSINE_H = "cosh(";
    public static final String A_COSINE_H = "cosh⁻¹(";
    public static final String TANGENT = "tan(";
    public static final String A_TANGENT = "tan⁻¹(";
    public static final String TANGENT_H = "tanh(";
    public static final String A_TANGENT_H = "tanh⁻¹(";

    public static final String LOG = "log(";
    public static final String LOG_X = "log";
    public static final String LOG2 = "log₂(";
    public static final String LN = "ln(";

    public static final String PERMUTATION = "Ƥ";
    public static final String SEMICOLON = ";";
    public static final String RAN_SHARP = "Ran#";
    public static final String PERCENT = "%";
    public static final String RAN_INT = "RanInt(";
    public static final String REC = "Rec(";
    public static final String POL = "Pol(";
    public static final String BINOMIAL_COEFFICIENT = "С";

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *        previously being shut down, then this Bundle contains the data it most
     *        recently supplied in {@link #onSaveInstanceState}.
     *        <b><i>Note: Otherwise, it is null.</i></b>
     */
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass onCreate method
        super.onCreate(savedInstanceState);
        stopBackgroundService();

        // Set the content view to the calculator UI layout
        setContentView(R.layout.calculatorui);

        // Set the context to this instance
        context = this;

        // Set the main activity to this instance
        setMainActivity(this);

        // Initialize DataManager with the current context
        dataManager = new DataManager(this);
        dataManager.initializeSettings(this);

        /*
        inAppReview = new InAppReview(this);
        inAppReview.activateReviewInfo();
        */

        switchDisplayMode();
        try {
            JSONObject currentVersionData = dataManager.getJSONSettingsData("currentVersion", getApplicationContext());
            JSONObject oldVersionData = dataManager.getJSONSettingsData("old_version", getApplicationContext());
            JSONObject showPatchNotesData = dataManager.getJSONSettingsData("showPatchNotes", getApplicationContext());

            String showPatchNotes = showPatchNotesData.getString("value");

            if(showPatchNotes.equals("true")) {
                showPatchNotes();
            } else if (currentVersionData.has("value") && oldVersionData.has("value")) {
                String currentValue = currentVersionData.getString("value");
                String oldValue = oldVersionData.getString("value");

                if (!Objects.equals(currentValue, oldValue)) {
                    // Set the flag to show patch notes and switch to the patch notes layout

                    boolean showHelpActivity = Objects.equals(oldValue, "0");
                    dataManager.updateValuesInJSONSettingsData(
                            "old_version",
                            "value",
                            currentValue,
                            getApplicationContext()
                    );

                    dataManager.updateValuesInJSONSettingsData(
                            "returnToCalculator",
                            "value",
                            "true",
                            getApplicationContext()
                    );

                    dataManager.updateValuesInJSONSettingsData(
                            "showPatchNotes",
                            "value",
                            "true",
                            getApplicationContext()
                    );

                    if(showHelpActivity) {
                        HelpActivity.setMainActivityContext(this);
                        startActivity(new Intent(this, HelpActivity.class));
                    } else {
                        showPatchNotes();
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Load numbers, set up listeners, check science button state, check dark mode setting, format result text, adjust text size
        setUpListeners();
        showOrHideScienceButtonState();
        switchDisplayMode();
        formatResultTextAfterTyping();

        // Scroll down in the calculate label
        scrollToStart(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));

        //showAllSettings();
        adjustTextSize();

        setCalculateText(getCalculateText().replace(" ", "").replace("=", ""));

        inAppUpdate = new InAppUpdate(MainActivity.this);
        inAppUpdate.checkForAppUpdate();

        HorizontalScrollView calculateScrollView = findViewById(R.id.calculate_scrollview);
        HorizontalScrollView resultScrollView = findViewById(R.id.result_scrollview);

        calculateScrollView.setHorizontalScrollBarEnabled(false);
        resultScrollView.setVerticalScrollBarEnabled(false);

        findMaxCharactersWithoutScrolling(); // loads numbers after finish
    }

    private void findMaxCharactersWithoutScrolling() {
        horizontalScrollView = findViewById(R.id.result_scrollview);
        textView = findViewById(R.id.result_label);

        horizontalScrollView.post(() -> {
            int initialScrollX = horizontalScrollView.getScrollX();

            // Loop to find the minimum number of characters needed to scroll
            while (true) {
                // Add one character to the TextView
                textBuilder.append("0");
                textView.setText(textBuilder.toString());

                // Measure the TextView width
                textView.measure(0, 0);
                int textViewWidth = textView.getMeasuredWidth();

                // Check if scrolling is possible
                if (textViewWidth > horizontalScrollView.getWidth()) {
                    break;
                }

                // Increase character count
                characterCount++;
            }
            characterCount -= 2;

            // Print the result
            //System.out.println("Minimum number of characters needed to scroll: " + characterCount);
            dataManager.saveToJSONSettings("maxNumbersWithoutScrolling", String.valueOf(characterCount), getApplicationContext());
            try {
                dataManager.loadNumbers();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void checkForAskNotification() {
        try {
            final int calculateCount;
            calculateCount = Integer.parseInt(dataManager.getJSONSettingsData("calculationCount", getApplicationContext()).getString("value"));
            dataManager.saveToJSONSettings("calculationCount", String.valueOf(calculateCount + 1), getApplicationContext());

            if(calculateCount >= 1000) {
                dataManager.saveToJSONSettings("calculationCount", "10", getApplicationContext());
                return;
            }

            if(calculateCount == 9) {
                if(dataManager.getJSONSettingsData("allowNotification", getApplicationContext()).getString("value").equals("false")) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.activate_notifications, null);

                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

                    popupView.setBackgroundColor(newColorBTNBackgroundAccent);

                    TextView textViewTitle = popupView.findViewById(R.id.activate_notification_layout_title);
                    TextView textViewActivate = popupView.findViewById(R.id.activateNotificationButton);
                    TextView textViewLater = popupView.findViewById(R.id.laterNotificationButton);

                    LinearLayout notificationOutline = popupView.findViewById(R.id.notificationOutline);
                    Drawable backgroundDrawable = getResources().getDrawable(R.drawable.textview_border_thick);

                    if (backgroundDrawable instanceof GradientDrawable) {
                        GradientDrawable gradientDrawable = (GradientDrawable) backgroundDrawable;
                        gradientDrawable.setStroke(10, newColorBTNForegroundAccent);

                        notificationOutline.setBackground(backgroundDrawable);
                    }

                    textViewTitle.setTextColor(newColorBTNForegroundAccent);
                    textViewActivate.setTextColor(newColorBTNForegroundAccent);
                    textViewLater.setTextColor(newColorBTNForegroundAccent);

                    textViewTitle.setBackgroundColor(newColorBTNBackgroundAccent);
                    textViewActivate.setBackgroundColor(newColorBTNBackgroundAccent);
                    textViewLater.setBackgroundColor(newColorBTNBackgroundAccent);

                    popupWindow.showAtLocation(findViewById(R.id.calculatorUI), Gravity.CENTER, 0, 0);

                    textViewActivate.setOnClickListener(v -> {
                        boolean isPermissionGranted = isNotificationPermissionGranted();
                        if (!isPermissionGranted) {
                            requestNotificationPermission();
                        }

                        if (!SettingsActivity.isChannelPermissionGranted(this, CHANNEL_ID_BACKGROUND)) {
                            dataManager.saveToJSONSettings("allowNotifications", false, getApplicationContext());
                        }

                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            //Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            //intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                            //this.startActivity(intent);

                            requestNotificationPermission();
                        }

                        popupWindow.dismiss();
                    });

                    textViewLater.setOnClickListener(v -> popupWindow.dismiss());
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            savePermissionStatus(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                dataManager.saveToJSONSettings("allowNotifications", true, getApplicationContext());

                dataManager.saveToJSONSettings("allowNotification", true, getApplicationContext());
                dataManager.saveToJSONSettings("allowDailyNotifications", true, getApplicationContext());
                dataManager.saveToJSONSettings("allowRememberNotifications", true, getApplicationContext());
                dataManager.saveToJSONSettings("allowDailyNotificationsActive", true, getApplicationContext());
                dataManager.saveToJSONSettings("allowRememberNotificationsActive", true, getApplicationContext());
            }
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

    boolean isNotificationPermissionGranted() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(PERMISSION_GRANTED_KEY, false);
    }

    private void savePermissionStatus(boolean isGranted) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PERMISSION_GRANTED_KEY, isGranted);
        editor.apply();
    }

    private void showPatchNotes() {
        switchDisplayMode();

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.patchnotes, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        LinearLayout patchnotesLayout = popupView.findViewById(R.id.patchnotesLayout);

        if (patchnotesLayout != null) {
            Drawable backgroundDrawable = getResources().getDrawable(R.drawable.textview_border_thick);
            if (backgroundDrawable instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) backgroundDrawable;
                gradientDrawable.setStroke(10, newColorBTNForegroundAccent);
                patchnotesLayout.setBackground(backgroundDrawable);
            }
        }

        TextView releasenotesTitle = popupView.findViewById(R.id.releasenotes_title);
        TextView releasenotesDate = popupView.findViewById(R.id.releasenotes_date);
        TextView releasenotesText = popupView.findViewById(R.id.releasenotes_text);
        TextView understoodButton = popupView.findViewById(R.id.understoodButton);

        releasenotesTitle.setTextColor(newColorBTNForegroundAccent);
        releasenotesDate.setTextColor(newColorBTNForegroundAccent);
        releasenotesText.setTextColor(newColorBTNForegroundAccent);
        understoodButton.setTextColor(newColorBTNForegroundAccent);

        understoodButton.setBackgroundColor(newColorBTNBackgroundAccent);

        popupView.setPadding(20, 20, 20, 20);
        popupView.setBackgroundColor(newColorBTNBackgroundAccent);

        findViewById(R.id.calculatorUI).post(() ->
                popupWindow.showAtLocation(findViewById(R.id.calculatorUI),
                Gravity.CENTER, 0, 0
        ));

        understoodButton.setOnClickListener(v -> popupWindow.dismiss());

        dataManager.updateValuesInJSONSettingsData(
                "showPatchNotes",
                "value",
                "false",
                getApplicationContext()
        );
    }

    /**
     * Scrolls a ScrollView to the bottom of its content.
     * <p>
     * This method posts a Runnable to the ScrollView's message queue, which
     * ensures that the scrolling operation is executed after the view is
     * created and laid out. It uses the fullScroll method with FOCUS_DOWN
     * parameter to scroll the ScrollView to the bottom.
     *
     * @param scrollView The ScrollView to be scrolled to the bottom.
     */
    private void scrollToStart(final HorizontalScrollView scrollView) {
        // Executes the scrolling to the bottom of the ScrollView in a Runnable.
        if(scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_LEFT));
        }
    }

    private void scrollToEnd(final HorizontalScrollView scrollView) {
        // Executes the scrolling to the bottom of the ScrollView in a Runnable.
        if(scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_RIGHT));
        }
    }

    private void scrollToTop(final HorizontalScrollView scrollView) {
        // Executes the scrolling to the bottom of the ScrollView in a Runnable.
        if(scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
        }
    }

    private void scrollToBottom(final HorizontalScrollView scrollView) {
        // Executes the scrolling to the bottom of the ScrollView in a Runnable.
        if(scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        }
    }

    /**
     * Sets up the listeners for each button in the application
     */
    private void setUpListeners() {
        setActionButtonListener(R.id.history_button, this::switchToHistoryAction);
        setActionButtonListener(R.id.settings_button, this::switchToSettingsAction);
        setActionButtonListener(R.id.convert_button, this::switchToConvertAction);

        setClipboardButtonListener(R.id.emptyclipboard, "MC");
        setClipboardButtonListener(R.id.pastefromclipboard, "MR");
        setClipboardButtonListener(R.id.copytoclipboard, "MS");

        setEmptyButtonListener(R.id.clearresult, "CE");
        setEmptyButtonListener(R.id.clearall, "C");
        setEmptyButtonListener(R.id.backspace, "⌫");

        setOperationButtonListener(R.id.divide, "/");
        setOperationButtonListener(R.id.multiply, "*");
        setOperationButtonListener(R.id.subtract, "-");
        setOperationButtonListener(R.id.add, "+");
        setNegateButtonListener(R.id.negative);

        setNumberButtonListener(R.id.zero);
        setNumberButtonListener(R.id.one);
        setNumberButtonListener(R.id.two);
        setNumberButtonListener(R.id.three);
        setNumberButtonListener(R.id.four);
        setNumberButtonListener(R.id.five);
        setNumberButtonListener(R.id.six);
        setNumberButtonListener(R.id.seven);
        setNumberButtonListener(R.id.eight);
        setNumberButtonListener(R.id.nine);

        setCalculateButtonListener(R.id.calculate);
        setCommaButtonListener(R.id.comma);

        setButtonListener(R.id.clipOn, this::parenthesesOnAction);
        setButtonListener(R.id.clipOff, this::parenthesesOffAction);

        setButtonListener(R.id.power, this::powerAction);
        setButtonListener(R.id.root, this::rootAction);
        setButtonListener(R.id.faculty, this::factorial);

        setButtonListener(R.id.sinus, this::sinusAction);
        setButtonListener(R.id.cosinus, this::cosinusAction);
        setButtonListener(R.id.tangens, this::tangensAction);
        setButtonListener(R.id.sinush, this::sinushAction);
        setButtonListener(R.id.cosinush, this::cosinushAction);
        setButtonListener(R.id.tangensh, this::tangenshAction);

        setButtonListener(R.id.asinus, this::aSinusAction);
        setButtonListener(R.id.acosinus, this::aCosinusAction);
        setButtonListener(R.id.atangens, this::aTangensAction);

        setButtonListener(R.id.asinush, this::aSinusHAction);
        setButtonListener(R.id.acosinush, this::aCosinusHAction);
        setButtonListener(R.id.atangensh, this::aTangensHAction);

        setButtonListener(R.id.log, this::logAction);
        setButtonListener(R.id.log2x, this::log2Action);
        setButtonListener(R.id.ln, this::lnAction);
        setButtonListener(R.id.logxx, this::logXAction);

        // important: "е" and "e" are different characters
        setButtonListener(R.id.е, this::еAction); // Eulersche Zahl
        setButtonListener(R.id.pi, this::piAction);

        setButtonListenerWithoutChangedWeight(R.id.half, this::halfAction);
        setButtonListenerWithoutChangedWeight(R.id.third, this::thirdAction);
        setButtonListenerWithoutChangedWeight(R.id.quarter, this::quarterAction);
        setButtonListenerWithoutChangedWeight(R.id.fifth, this::fifthAction);
        setButtonListenerWithoutChangedWeight(R.id.tenth, this::tenthAction);

        setButtonListener(R.id.thirdRoot, this::thirdRootAction);

        setButtonListenerWithoutChangedWeight(R.id.scientificButton, this::setScienceButtonState);
        setButtonListenerWithoutChangedWeight(R.id.shift, this::setShiftButtonState);

        setLongTextViewClickListener(R.id.calculate_label, this::saveCalculateLabelData);
        setLongTextViewClickListener(R.id.result_label, this::saveResultLabelData);

        setButtonListener(R.id.functionMode_text, this::changeFunctionMode);
        setButtonListener(R.id.shiftMode_text, this::setShiftButtonState);

        //setButtonListener(R.id.picture_in_picture, this::enterPictureInPictureModeIfPossible);

        setButtonListener(R.id.nPr, this::nPr);
        setButtonListener(R.id.nCr, this::nCr);
        setButtonListener(R.id.pol, this::pol);
        setButtonListener(R.id.rec, this::rec);
        setButtonListener(R.id.ranint, this::ranInt);
        setButtonListener(R.id.percent, this::percent);
        setButtonListener(R.id.ran_hashtag, this::ranSharp);
        setButtonListener(R.id.semicolon, this::semicolon);
    }

    /*
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enterPictureInPictureModeIfPossible() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRatio = new Rational(9, 16);
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
            pipBuilder.setAspectRatio(aspectRatio);
            enterPictureInPictureMode(pipBuilder.build());
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPictureInPictureModeIfPossible();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            // Verstecke unnötige UI-Komponenten
        } else {
            // Zeige UI-Komponenten wieder an
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "ACTION_PLAY":
                    // Handle play action
                    break;
                case "ACTION_PAUSE":
                    // Handle pause action
                    break;
            }
        }
    }
     */

    private void nPr() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(PERMUTATION);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }
    private void nCr() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(BINOMIAL_COEFFICIENT);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }
    private void pol() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(POL);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }
    private void rec() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(REC);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }
    private void ranInt() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(RAN_INT);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }
    private void percent() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(PERCENT);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }
    private void ranSharp() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(RAN_SHARP);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }
    private void semicolon() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(SEMICOLON);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method is responsible for toggling between two function modes, namely "Deg" (Degrees)
     * and "Rad" (Radians). It retrieves the current function mode from the application's stored data
     * using a DataManager and then switches it to the opposite mode. After updating the mode, it
     * updates the displayed text in a TextView with the new mode. Additionally, it logs the change
     * using Android's Log class for debugging purposes.
     * <p>
     * Note: The function mode is stored and retrieved from persistent storage to ensure that the
     * selected mode persists across application sessions.
     */
    private void changeFunctionMode() {
        try {
            // Get reference to the TextView for displaying function mode
            final TextView function_mode_text = findViewById(R.id.functionMode_text);

            // Read the current function mode from the stored data
            final String mode;
            mode = dataManager.getJSONSettingsData("functionMode", getApplicationContext()).getString("value");

            // Toggle between "Deg" and "Rad" modes
            switch (mode) {
                case "Deg":
                    dataManager.saveToJSONSettings("functionMode", "Rad", getApplicationContext());
                    break;
                case "Rad":
                    dataManager.saveToJSONSettings("functionMode", "Deg", getApplicationContext());
                    break;
            }

            try {
                if(!getCalculateText().isEmpty()) {
                    setResultText(CalculatorEngine.calculate(getCalculateText()));
                }
                function_mode_text.setText(dataManager.getJSONSettingsData("functionMode", getApplicationContext()).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method manages the state of a science button within the application. It reads the
     * current state of the science button from the application's stored data using a DataManager.
     * The state is toggled (from "true" to "false" or vice versa), and then the method calls
     * another method, showOrHideScienceButtonState(), to handle the visual representation or
     * behavior associated with the state change.
     * <p>
     * Note: The state of the science button is stored and retrieved from persistent storage to
     * ensure that the selected state persists across application sessions.
     */
    private void setScienceButtonState() {
        try {
            final String value;
            value = dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value");


            // Toggle the state of the science button
            if (value.equals("false")) {
                dataManager.saveToJSONSettings("showScienceRow", "true", getApplicationContext());
                dataManager.saveToJSONSettings("tempShowScienceRow", "true", getApplicationContext());
            } else {
                dataManager.saveToJSONSettings("showScienceRow", "false", getApplicationContext());
                dataManager.saveToJSONSettings("tempShowScienceRow", "false", getApplicationContext());
            }

            // Handle the visual representation or behavior associated with the state change
            showOrHideScienceButtonState();
            adjustTextSize();

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            }

            if(getCalculateText().isEmpty()) {
                setResultText("0");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method manages the state of a shift button within the application. It reads the
     * current state of the shift button from the application's stored data using a DataManager.
     * The state is toggled (from "true" to "false" or vice versa), and then the method calls
     * another method, shiftButtonAction(), to handle the visual representation or
     * behavior associated with the state change.
     * <p>
     * Note: The state of the shift button is stored and retrieved from persistent storage to
     * ensure that the selected state persists across application sessions.
     */
    private void setShiftButtonState() {
        // Toggle the state of the shift button
        try {
            if (dataManager.getJSONSettingsData("shiftRow", getApplicationContext()).getString("value").equals("4")) {
                dataManager.saveToJSONSettings("shiftRow", "1", getApplicationContext());
            } else {
                final String num = String.valueOf(Integer.parseInt(dataManager.getJSONSettingsData("shiftRow", getApplicationContext()).getString("value")) + 1);
                dataManager.saveToJSONSettings("shiftRow", num, getApplicationContext());
            }

            // Handle the visual representation or behavior associated with the state change
            shiftButtonAction();
            adjustTextSize();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * This method handles the visual representation or behavior associated with the shift button state change.
     * It adjusts the visibility of different LinearLayouts and updates a TextView based on the current shift button state.
     */
    private void shiftButtonAction() {
        try {
            GridLayout buttonRow1 = findViewById(R.id.scientificRow11);
            GridLayout buttonRow2 = findViewById(R.id.scientificRow21);
            GridLayout buttonRow12 = findViewById(R.id.scientificRow12);
            GridLayout buttonRow22 = findViewById(R.id.scientificRow22);
            GridLayout buttonRow13 = findViewById(R.id.scientificRow13);
            GridLayout buttonRow23 = findViewById(R.id.scientificRow23);
            GridLayout buttonRow14 = findViewById(R.id.scientificRow14);
            GridLayout buttonRow24 = findViewById(R.id.scientificRow24);
            GridLayout buttonRow3 = findViewById(R.id.scientificRow3);
            TextView shiftModeText = findViewById(R.id.shiftMode_text);

            // Read the current state of the shift button from the stored data
            final String shiftValue = dataManager.getJSONSettingsData("shiftRow", getApplicationContext()).getString("value");
            final String rowValue = dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value");

            // Toggle the visibility of different GridLayouts and update TextView based on the shift button state
            if(rowValue.equals("true") && (buttonRow1 != null && buttonRow2 != null && buttonRow12 != null
                    && buttonRow22 != null && buttonRow13 != null && buttonRow23 != null  && buttonRow14 != null  && buttonRow24 != null && buttonRow3 != null
                    && shiftModeText != null)) {
                switch (shiftValue) {
                    case "1":
                        buttonRow1.setVisibility(View.VISIBLE);
                        buttonRow2.setVisibility(View.VISIBLE);
                        buttonRow12.setVisibility(View.GONE);
                        buttonRow22.setVisibility(View.GONE);
                        buttonRow13.setVisibility(View.GONE);
                        buttonRow23.setVisibility(View.GONE);
                        buttonRow14.setVisibility(View.GONE);
                        buttonRow24.setVisibility(View.GONE);
                        buttonRow3.setVisibility(View.VISIBLE);
                        shiftModeText.setText("1");
                        break;
                    case "2":
                        buttonRow1.setVisibility(View.GONE);
                        buttonRow2.setVisibility(View.GONE);
                        buttonRow12.setVisibility(View.VISIBLE);
                        buttonRow22.setVisibility(View.VISIBLE);
                        buttonRow13.setVisibility(View.GONE);
                        buttonRow23.setVisibility(View.GONE);
                        buttonRow14.setVisibility(View.GONE);
                        buttonRow24.setVisibility(View.GONE);
                        buttonRow3.setVisibility(View.VISIBLE);
                        shiftModeText.setText("2");
                        break;
                    case "3":
                        buttonRow1.setVisibility(View.GONE);
                        buttonRow2.setVisibility(View.GONE);
                        buttonRow12.setVisibility(View.GONE);
                        buttonRow22.setVisibility(View.GONE);
                        buttonRow13.setVisibility(View.VISIBLE);
                        buttonRow23.setVisibility(View.VISIBLE);
                        buttonRow14.setVisibility(View.GONE);
                        buttonRow24.setVisibility(View.GONE);
                        buttonRow3.setVisibility(View.VISIBLE);
                        shiftModeText.setText("3");
                        break;
                    case "4":
                        buttonRow1.setVisibility(View.GONE);
                        buttonRow2.setVisibility(View.GONE);
                        buttonRow12.setVisibility(View.GONE);
                        buttonRow22.setVisibility(View.GONE);
                        buttonRow13.setVisibility(View.GONE);
                        buttonRow23.setVisibility(View.GONE);
                        buttonRow14.setVisibility(View.VISIBLE);
                        buttonRow24.setVisibility(View.VISIBLE);
                        buttonRow3.setVisibility(View.VISIBLE);
                        shiftModeText.setText("4");
                        break;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks the state of the science button
     */
    private void showOrHideScienceButtonState() {
        TextView function_mode_text = findViewById(R.id.functionMode_text);
        TextView shiftModeText = findViewById(R.id.shiftMode_text);

        GridLayout buttonRow1 = findViewById(R.id.scientificRow11);
        GridLayout buttonRow12 = findViewById(R.id.scientificRow12);
        GridLayout buttonRow13 = findViewById(R.id.scientificRow13);

        GridLayout buttonRow2 = findViewById(R.id.scientificRow21);
        GridLayout buttonRow22 = findViewById(R.id.scientificRow22);
        GridLayout buttonRow23 = findViewById(R.id.scientificRow23);

        GridLayout buttonRow14 = findViewById(R.id.scientificRow14);
        GridLayout buttonRow24 = findViewById(R.id.scientificRow24);

        GridLayout buttonRow3 = findViewById(R.id.scientificRow3);

        Button shiftButton = findViewById(R.id.shift);

        GridLayout buttonLayout = findViewById(R.id.standardButtonsLayout);

        if (function_mode_text != null) {
            try {
                function_mode_text.setText(dataManager.getJSONSettingsData("functionMode", getApplicationContext()).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        if(buttonLayout != null) {
            try {
                GridLayout.LayoutParams layoutParams;
                if (dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value").equals("false")) {

                    layoutParams = (GridLayout.LayoutParams) buttonLayout.getLayoutParams();
                    layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 3F);
                    buttonLayout.setLayoutParams(layoutParams);

                    buttonRow1.setVisibility(View.GONE);
                    buttonRow2.setVisibility(View.GONE);
                    buttonRow12.setVisibility(View.GONE);
                    buttonRow22.setVisibility(View.GONE);
                    buttonRow13.setVisibility(View.GONE);
                    buttonRow23.setVisibility(View.GONE);
                    buttonRow14.setVisibility(View.GONE);
                    buttonRow24.setVisibility(View.GONE);
                    shiftButton.setVisibility(View.GONE);
                    buttonRow3.setVisibility(View.GONE);

                    assert function_mode_text != null;
                    assert shiftModeText != null;
                    function_mode_text.setVisibility(View.GONE);
                    shiftModeText.setVisibility(View.GONE);
                } else {
                    layoutParams = (GridLayout.LayoutParams) buttonLayout.getLayoutParams();
                    layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 2F);
                    buttonLayout.setLayoutParams(layoutParams);

                    buttonRow1.setVisibility(View.VISIBLE);
                    buttonRow2.setVisibility(View.VISIBLE);
                    buttonRow12.setVisibility(View.VISIBLE);
                    buttonRow22.setVisibility(View.VISIBLE);
                    buttonRow13.setVisibility(View.VISIBLE);
                    buttonRow23.setVisibility(View.VISIBLE);
                    buttonRow14.setVisibility(View.VISIBLE);
                    buttonRow24.setVisibility(View.VISIBLE);
                    shiftButton.setVisibility(View.VISIBLE);
                    buttonRow3.setVisibility(View.VISIBLE);

                    assert function_mode_text != null;
                    assert shiftModeText != null;
                    function_mode_text.setVisibility(View.VISIBLE);
                    shiftModeText.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        shiftButtonAction();
    }

    /**
     * Sets up the listener for the comma button.
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     */
    private void setCommaButtonListener(int buttonId) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> {
                CommaAction();
                dataManager.saveNumbers(getApplicationContext());

                scrollToEnd(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
    }

    /**
     * Sets up the listener for the calculate button.
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     */
    private void setCalculateButtonListener(int buttonId) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> {
                try {
                    Calculate();
                    dataManager.saveNumbers(getApplicationContext());

                    /*
                    int oldValue = dataManager.getJSONSettingsData("calculationCount", getApplicationContext()).getInt("value");
                    dataManager.saveToJSONSettings("calculationCount", String.valueOf((oldValue + 1)),getApplicationContext());

                    if(oldValue == 19) { // which calls if the user calculated 20 times
                        inAppReview.startReviewFlow();
                    }
                    */
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Sets up the listener for all number buttons
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     */
    private void setNumberButtonListener(int buttonId) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> {
                final String num = v.getTag().toString();
                NumberAction(num);
                dataManager.saveNumbers(getApplicationContext());
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());

                scrollToEnd(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
    }

    /**
     * Sets up the listener for all operation buttons
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     * @param operation The action which belongs to the button.
     */
    private void setOperationButtonListener(int buttonId, String operation) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> {
                OperationAction(operation);
                dataManager.saveNumbers(getApplicationContext());
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                setCalculateText(replacePiWithSymbolInString(getCalculateText()));

                scrollToEnd(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
    }

    /**
     * Sets up the listener for all number buttons
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     * @param action The action which belongs to the button.
     */
    private void setEmptyButtonListener(int buttonId, String action) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> {
                EmptyAction(action);
                dataManager.saveNumbers(getApplicationContext());
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());

                scrollToStart(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
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
                dataManager.saveNumbers(getApplicationContext());

                if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                    setResultText(CalculatorEngine.calculate(getCalculateText()));
                } else {
                    setResultText("");
                }

                scrollToEnd(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
    }

    /**
     * Sets up the listener for all buttons
     *
     * @param textViewId The ID of the button to which the listener is to be set.
     * @param action The action which belongs to the button.
     */
    private void setButtonListenerWithoutChangedWeight(int textViewId, Runnable action) {
        TextView textView = findViewById(textViewId);
        if(textView != null) {
            textView.setOnClickListener(v -> {
                action.run();
                dataManager.saveNumbers(getApplicationContext());

                if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                    setResultText(CalculatorEngine.calculate(getCalculateText()));
                }

                scrollToEnd(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
    }

    /**
     * Sets up the listener for all buttons
     *
     * @param textViewId The ID of the button to which the listener is to be set.
     * @param action The action which belongs to the button.
     */
    private void setActionButtonListener(int textViewId, Runnable action) {
        TextView textView = findViewById(textViewId);
        if(textView != null) {
            textView.setOnClickListener(v -> {
                action.run();

                if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                    setResultText(CalculatorEngine.calculate(getCalculateText()));
                } else {
                    setResultText("");
                }

                dataManager.saveNumbers(getApplicationContext());
            });
        }
    }

    /**
     * Sets up the listener for all buttons.
     *
     * @param textViewId The ID of the button to which the listener is to be set.
     * @param action The action which belongs to the button.
     */
    private void setLongTextViewClickListener(int textViewId, Runnable action) {
        // Find the TextView with the specified ID
        TextView textView = findViewById(textViewId);

        // Check if the TextView is not null
        if (textView != null) {
            // Set a long click listener for the TextView
            textView.setOnLongClickListener(v -> {
                // Execute the specified action when the TextView is long-clicked
                action.run();
                // Return false to indicate that the event is not consumed
                return false;
            });
        }
    }

    /**
     * Saves the result label data to the clipboard.
     */
    private void saveResultLabelData() {
        // Get the system clipboard manager
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a ClipData with plain text representing the result text
        ClipData clipData = ClipData.newPlainText("", getResultText());

        // Set the created ClipData as the primary clip on the clipboard
        clipboardManager.setPrimaryClip(clipData);

        // Display a toast indicating that the data has been saved
        if(Locale.getDefault().getDisplayLanguage().equals("English")) {
            showToastLong("The value has been saved ...", getApplicationContext());
        } else if(Locale.getDefault().getDisplayLanguage().equals("français")) {
            showToastLong("La valeur a été enregistrée ...", getApplicationContext());
        } else if(Locale.getDefault().getDisplayLanguage().equals("español")) {
            showToastLong("El valor ha sido guardado ...", getApplicationContext());
        } else {
            showToastLong("Der Wert wurde gespeichert ...", getApplicationContext());
        }
    }

    /**
     * Saves the calculate label data to the clipboard.
     */
    private void saveCalculateLabelData() {
        // Get the system clipboard manager
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a ClipData with plain text representing the calculate text
        ClipData clipData = ClipData.newPlainText("", getCalculateText());

        // Set the created ClipData as the primary clip on the clipboard
        clipboardManager.setPrimaryClip(clipData);

        // Display a toast indicating that the data has been saved
        if(Locale.getDefault().getDisplayLanguage().equals("English")) {
            showToastLong("The value has been saved ...", getApplicationContext());
        } else if(Locale.getDefault().getDisplayLanguage().equals("français")) {
            showToastLong("La valeur a été enregistrée ...", getApplicationContext());
        } else if(Locale.getDefault().getDisplayLanguage().equals("español")) {
            showToastLong("El valor ha sido guardado ...", getApplicationContext());
        } else {
            showToastLong("Der Wert wurde gespeichert ...", getApplicationContext());
        }
    }

    /**
     * Sets up the listener for all number buttons
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     */
    private void setNegateButtonListener(int buttonId) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> {
                NegativAction();
                dataManager.saveNumbers(getApplicationContext());

                if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                    setResultText(CalculatorEngine.calculate(getCalculateText()));
                } else {
                    setResultText("");
                }

                scrollToEnd(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
    }

    /**
     * Sets up the listener for all number buttons
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     * @param action The action which belongs to the button.
     */
    private void setClipboardButtonListener(int buttonId, String action) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> {
                ClipboardAction(action);
                dataManager.saveNumbers(getApplicationContext());
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());

                if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                    setResultText(CalculatorEngine.calculate(getCalculateText()));
                } else {
                    setResultText("");
                }

                scrollToEnd(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
    }

    void resetIfPressedCalculate() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                setResultText("0");
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends or sets the text "sin(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void sinusAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(SINE);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "sinh(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void sinushAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(SINE_H);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "sin⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aSinusAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(A_SINE);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "sin⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aSinusHAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(A_SINE_H);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "cos(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void cosinusAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(COSINE);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "cosh(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void cosinushAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(COSINE_H);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "cos⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aCosinusAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(A_COSINE);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "sin⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aCosinusHAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(A_COSINE_H);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "tan(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void tangensAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(TANGENT);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "tanh(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void tangenshAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(TANGENT_H);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "tan⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aTangensAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(A_TANGENT);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "sin⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aTangensHAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(A_TANGENT_H);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of adding "log(" to the calculation text.
     * It checks if the "logX" flag is false in the JSON file, indicating that the logarithm function is not currently selected.
     * If the mode is not in "eNotation", it proceeds to add "log(" to the calculation text.
     * The method handles cases where the calculation text is empty or not, and whether to add "log(" with or without spaces depending on the calculation mode.
     * It also scrolls to the bottom of the scroll view if it exists.
     * After adding "log(" to the calculation text, it formats the result text accordingly.
     */
    private void logAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(LOG);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of adding "log₂(" to the calculation text.
     * It follows a similar procedure to the logAction() method but adds "log₂(" instead of "log(".
     */
    private void log2Action() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(LOG2);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of indicating the selection of the logarithm function in the calculation text.
     * It sets the "logX" flag to true in the JSON file and adds "log" to the calculation text.
     * Depending on the calculation mode, it adds "log" with or without spaces.
     * After adding "log" to the calculation text, it formats the result text accordingly.
     */
    private void logXAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    dataManager.saveToJSONSettings("logX", "true", getApplicationContext());
                    addCalculateTextWithoutSpace(LOG_X);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of adding "ln(" to the calculation text.
     * It follows a similar procedure to the logAction() method but adds "ln(" instead of "log(".
     */
    private void lnAction() {
        try {
            resetIfPressedCalculate();

            if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    addCalculateTextWithoutSpace(LN);

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterTyping();
    }

    /**
     * German: "Eulersche Zahl"
     * Handles the insertion or removal of the "е" symbol based on its current presence in the result text.
     * <p>
     * Note: "е" is not "e"
     */
    private void еAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        // Check if logarithmic mode is disabled
        addCalculateTextWithoutSpace("е");
        setResultText(CalculatorEngine.calculate(getCalculateText()));

        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            setResultText("");
        }

        // Format the result text after typing
        formatResultTextAfterTyping();
    }

    /**
     * Appends or sets the text "π" to the calculation input and sets the rotate operator flag to true.
     */
    private void piAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        // Check if logarithmic mode is disabled

        addCalculateTextWithoutSpace("π");

        setResultText(CalculatorEngine.calculate(getCalculateText()));
        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            setResultText("");
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method adds an opening parenthesis to the calculation text.
     */
    private void parenthesesOnAction() {
        dataManager.saveToJSONSettings("logX", "false", getApplicationContext());

        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                setResultText("0");
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        addCalculateTextWithoutSpace("(");

        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            setResultText("");
        }

        formatResultTextAfterTyping();
        scrollToStart(findViewById(R.id.calculate_scrollview));
    }

    /**
     * This method adds a closing parenthesis to the calculation text.
     * If the last operation was a square root, it adds a closing parenthesis.
     * Otherwise, it adds the result text and a closing parenthesis.
     */
    private void parenthesesOffAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                setResultText("0");
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        addCalculateTextWithoutSpace(")");

        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            setResultText("");
        }

        formatResultTextAfterTyping();
    }

    /**
     * The 'factorial' method adds a factorial operator to the calculation.
     * It appends a "!" to the result text and sets the rotate operator flag to true.
     * There are two conditions:
     * 1. If the calculation text is empty, it appends the result with "!" to the calculation text.
     * 2. Otherwise, it determines the last character in the calculation text. If the last character is a valid operator or an opening parenthesis, it appends the result with "!" to the calculation text.
     * 3. If the last character is neither a valid operator nor an opening parenthesis, it appends the last operator and the result with "!" to the calculation text.
     */
    private void factorial() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                addCalculateTextWithoutSpace(getResultText() + "!");
            } else {
                addCalculateTextWithoutSpace("!");
            }

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method adds a power operation to the calculation text.
     * Depending on the state of the rotate operator flag, it handles the power operation differently.
     */
    private void powerAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                addCalculateTextWithoutSpace(getResultText() + "^");
                return;
            }

            addCalculateTextWithoutSpace("^");
            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method adds a root operation to the calculation text.
     * Depending on the state of the rotate operator flag, it handles the root operation differently.
     */
    private void rootAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                setResultText("0");
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            }
            addCalculateTextWithoutSpace("√(");

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method adds a root operation to the calculation text.
     * Depending on the state of the rotate operator flag, it handles the root operation differently.
     */
    private void thirdRootAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                setResultText("0");
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            }

            addCalculateTextWithoutSpace("³√(");
            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of adding "½" (half) to the calculation text.
     * It first checks the calculation mode, and if it's "Vereinfacht" (simplified), it directly adds "½" without spaces and calculates the result.
     * If the calculation mode is not "Vereinfacht", it checks if the logarithm function is not selected ("logX" is false).
     * Then, it checks if the current mode is not "eNotation" to proceed with adding "½" to the calculation text accordingly.
     * It sets flags for removing the value and rotating the operator and scrolls to the bottom of the scroll view if it exists.
     * After adding "½" to the calculation text, it formats the result text accordingly.
     */
    private void halfAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                final String input = getResultText() + "÷2=";
                setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷2")));
                formatResultTextAfterTyping();

                addToHistory(input);
                return;
            }

            dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            addCalculateTextWithoutSpace("½");
            setResultText(CalculatorEngine.calculate(getCalculateText()));

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of adding "⅓" (third) to the calculation text.
     * It follows a similar procedure to the halfAction() method but adds "⅓" instead of "½".
     */
    private void thirdAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                final String input = getResultText() + "÷3=";
                setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷3")));
                formatResultTextAfterTyping();

                addToHistory(input);
                return;
            }

            dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            addCalculateTextWithoutSpace("⅓");
            setResultText(CalculatorEngine.calculate(getCalculateText()));

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of adding "¼" (quarter) to the calculation text.
     * It follows a similar procedure to the halfAction() method but adds "¼" instead of "½".
     */
    private void quarterAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                final String input = getResultText() + "÷4=";
                setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷4")));
                formatResultTextAfterTyping();

                addToHistory(input);
                return;
            }

            dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            addCalculateTextWithoutSpace("¼");
            setResultText(CalculatorEngine.calculate(getCalculateText()));

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of adding "¼" (quarter) to the calculation text.
     * It follows a similar procedure to the halfAction() method but adds "¼" instead of "½".
     */
    private void fifthAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                final String input = getResultText() + "÷5=";
                setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷5")));
                formatResultTextAfterTyping();

                addToHistory(input);
                return;
            }

            dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            addCalculateTextWithoutSpace("⅕");
            setResultText(CalculatorEngine.calculate(getCalculateText()));

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method performs the action of adding "¼" (quarter) to the calculation text.
     * It follows a similar procedure to the halfAction() method but adds "¼" instead of "½".
     */
    private void tenthAction() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                final String input = getResultText() + "÷10=";
                setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷10")));
                formatResultTextAfterTyping();

                addToHistory(input);
                return;
            }

            dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            addCalculateTextWithoutSpace("⅒");
            setResultText(CalculatorEngine.calculate(getCalculateText()));

            if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterTyping();
    }

    /**
     * Switches to the settings activity.
     * It creates a new SettingsActivity, sets the main activity context, and starts the activity.
     */
    public void switchToSettingsAction() {
        dataManager.saveToJSONSettings("lastActivity", "Set", getApplicationContext());
        SettingsActivity.setMainActivityContext(this);
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the settings activity.
     * It creates a new ReportActivity, sets the main activity context, and starts the activity.
     */
    public void switchToReportAction() {
        dataManager.saveToJSONSettings("lastActivity", "Rep", getApplicationContext());
        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the settings activity.
     * It creates a new SettingsActivity, sets the main activity context, and starts the activity.
     */
    public void switchToHelpAction() {
        dataManager.saveToJSONSettings("lastActivity", "Hel", getApplicationContext());
        HelpActivity.setMainActivityContext(this);
        Intent intent = new Intent(MainActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the convert activity.
     * It creates a new ConvertActivity, sets the main activity context, and starts the activity.
     */
    public void switchToConvertAction() {
        dataManager.saveToJSONSettings("lastActivity", "Con", getApplicationContext());
        ConvertActivity.setMainActivityContext(this);
        Intent intent = new Intent(MainActivity.this, ConvertActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the history activity.
     * It creates a new HistoryActivity, sets the main activity context, and starts the activity.
     */
    private void switchToHistoryAction() {
        dataManager.saveToJSONSettings("lastActivity", "His", getApplicationContext());
        HistoryActivity.setMainActivityContext(this);
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        startActivity(intent);
    }

    /**
     * Handles configuration changes.
     * It calls the superclass method and switches the display mode based on the current night mode.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // check the orientation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            dataManager.saveToJSONSettings("showScienceRow", "true", getApplicationContext());
            restartApp();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            try {
                final String value = dataManager.getJSONSettingsData("tempShowScienceRow", getApplicationContext()).getString("value");
                dataManager.saveToJSONSettings("showScienceRow", value, getApplicationContext());
                restartApp();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        switchDisplayMode();
    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * This method is used to switch the display mode of the application based on the user's selected setting.
     * It first initializes the UI elements and gets the current night mode status and the user's selected setting.
     * If a setting is selected, it switches the display mode based on the selected setting.
     * If no setting is selected, it saves "System" as the selected setting and calls itself again.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchDisplayMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        // Global variables
        TextView historyButton = findViewById(R.id.history_button);
        TextView settingsButton = findViewById(R.id.settings_button);
        TextView convertButton = findViewById(R.id.convert_button);
        TextView scienceButton = findViewById(R.id.scientificButton);
        TextView floatingButton = findViewById(R.id.picture_in_picture);
        Button shiftButton = findViewById(R.id.shift);

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
                            if (historyButton != null) {
                                historyButton.setForeground(getDrawable(R.drawable.historyicon_light));
                            }
                            if (settingsButton != null) {
                                settingsButton.setForeground(getDrawable(R.drawable.settings_light));
                            }
                            if (convertButton != null) {
                                convertButton.setForeground(getDrawable(R.drawable.convertbutton_light));
                            }
                            if (scienceButton != null) {
                                scienceButton.setForeground(getDrawable(R.drawable.science_light));
                            }
                            if (shiftButton != null) {
                                shiftButton.setForeground(getDrawable(R.drawable.compare_arrows_light));
                            }
                            if (floatingButton != null) {
                                floatingButton.setForeground(getDrawable(R.drawable.bildimbild_light));
                            }

                            if (trueDarkMode.equals("true")) {
                                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                                if (scienceButton != null) {
                                    scienceButton.setForeground(getDrawable(R.drawable.science_true_darkmode));
                                }
                                if (historyButton != null) {
                                    historyButton.setForeground(getDrawable(R.drawable.historyicon_true_darkmode));
                                }
                                if (settingsButton != null) {
                                    settingsButton.setForeground(getDrawable(R.drawable.settings_true_darkmode));
                                }
                                if (convertButton != null) {
                                    convertButton.setForeground(getDrawable(R.drawable.convertbutton_true_darkmode));
                                }
                                if (shiftButton != null) {
                                    shiftButton.setForeground(getDrawable(R.drawable.compare_arrows_true_darkmode));
                                }
                                if (floatingButton != null) {
                                    floatingButton.setForeground(getDrawable(R.drawable.bildimbild_true_darkmode));
                                }
                            } else if (trueDarkMode.equals("false")) {
                                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);
                            }
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                            if (historyButton != null) {
                                historyButton.setForeground(getDrawable(R.drawable.historyicon));
                            }
                            if (settingsButton != null) {
                                settingsButton.setForeground(getDrawable(R.drawable.settings));
                            }
                            if (scienceButton != null) {
                                scienceButton.setForeground(getDrawable(R.drawable.science));
                            }
                            if (convertButton != null) {
                                convertButton.setForeground(getDrawable(R.drawable.convertbutton));
                            }
                            if (shiftButton != null) {
                                shiftButton.setForeground(getDrawable(R.drawable.compare_arrows));
                            }
                            if (floatingButton != null) {
                                floatingButton.setForeground(getDrawable(R.drawable.bildimbild));
                            }
                            break;
                    }
                    break;
                case "Tageslichtmodus":
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                    if (historyButton != null) {
                        historyButton.setForeground(getDrawable(R.drawable.historyicon));
                    }
                    if (settingsButton != null) {
                        settingsButton.setForeground(getDrawable(R.drawable.settings));
                    }
                    if (scienceButton != null) {
                        scienceButton.setForeground(getDrawable(R.drawable.science));
                    }
                    if (convertButton != null) {
                        convertButton.setForeground(getDrawable(R.drawable.convertbutton));
                    }
                    if (shiftButton != null) {
                        shiftButton.setForeground(getDrawable(R.drawable.compare_arrows));
                    }
                    if (floatingButton != null) {
                        floatingButton.setForeground(getDrawable(R.drawable.bildimbild));
                    }
                    break;
                case "Dunkelmodus":
                    dataManager = new DataManager(this);
                    if (historyButton != null) {
                        historyButton.setForeground(getDrawable(R.drawable.historyicon_light));
                    }
                    if (settingsButton != null) {
                        settingsButton.setForeground(getDrawable(R.drawable.settings_light));
                    }
                    if (scienceButton != null) {
                        scienceButton.setForeground(getDrawable(R.drawable.science_light));
                    }
                    if (convertButton != null) {
                        convertButton.setForeground(getDrawable(R.drawable.convertbutton_light));
                    }
                    if (shiftButton != null) {
                        shiftButton.setForeground(getDrawable(R.drawable.compare_arrows_light));
                    }
                    if (floatingButton != null) {
                        floatingButton.setForeground(getDrawable(R.drawable.bildimbild_light));
                    }

                    if (trueDarkMode.equals("false")) {
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                    } else {
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);

                        if (scienceButton != null) {
                            scienceButton.setForeground(getDrawable(R.drawable.science_true_darkmode));
                        }
                        if (historyButton != null) {
                            historyButton.setForeground(getDrawable(R.drawable.historyicon_true_darkmode));
                        }
                        if (settingsButton != null) {
                            settingsButton.setForeground(getDrawable(R.drawable.settings_true_darkmode));
                        }
                        if (convertButton != null) {
                            convertButton.setForeground(getDrawable(R.drawable.convertbutton_true_darkmode));
                        }
                        if (shiftButton != null) {
                            shiftButton.setForeground(getDrawable(R.drawable.compare_arrows_true_darkmode));
                        }
                        if (floatingButton != null) {
                            floatingButton.setForeground(getDrawable(R.drawable.bildimbild_true_darkmode));
                        }
                    }
                    break;
            }

            // Updating UI elements
            //changeTextViewColors(findViewById(R.id.patchnotesUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            //changeButtonColors(findViewById(R.id.patchnotesUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeTextViewColors(findViewById(R.id.calculatorUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeButtonColors(findViewById(R.id.calculatorUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
        } else {
            dataManager.saveToJSONSettings("selectedSpinnerSetting", "System", getApplicationContext());
            switchDisplayMode();
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
        return null;
    }

    /**
     * This method is used to change the colors of the buttons in a given layout.
     *
     * @param layout The ViewGroup whose Button children should have their colors changed. This can be any layout containing Buttons.
     * @param foregroundColor The color to be set as the text color of the Buttons. This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the Buttons and the layout. This should be a resolved color, not a resource id.
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
     * @param layout The ViewGroup whose TextView children should have their colors changed. This can be any layout containing TextViews.
     * @param foregroundColor The color to be set as the text color of the TextViews. This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the TextViews and the layout. This should be a resolved color, not a resource id.
     */
    private void changeTextViewColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);

                // If the child is a TextView, change the foreground and background colors
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(foregroundColor);
                    v.setBackgroundColor(backgroundColor);
                }
                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                else if (v instanceof ViewGroup) {
                    changeTextViewColors((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }

    /**
     * This method is called when the activity is destroyed.
     * It then calls the finish() method to close the activity.
     */
    protected void onDestroy() {
        super.onDestroy();
        inAppUpdate.onDestroy();
        finish();
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
        inAppUpdate.onResume();
        stopBackgroundService();
        formatResultTextAfterTyping();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inAppUpdate.onActivityResult(requestCode, resultCode);
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
     * This method is called when a number button is clicked.
     * It gets the text of the result and calculate TextViews and checks if they contain invalid input.
     * If they do, it resets the calculate text and sets removeValue to true.
     * If removeValue is true, it resets the calculate text if it contains invalid input or an equals sign, and sets the result text to "0".
     * It then checks if the length of the result text is less than 18, and if so, it adds the clicked number to the result text.
     * Finally, it formats the result text and adjusts its size.
     *
     * @param num The number corresponding to the clicked button. This number will be added to the result text.
     */
    public void NumberAction(String num) {
        setLastNumber(getResultText());

        try {
            System.out.println(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value"));
            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("true")) {
                //dataManager.saveToJSONSettings("logX", "false", getApplicationContext());
                String small_number = convertToSmallNumber(Integer.parseInt(num));
                addCalculateTextWithoutSpace(small_number);
                //addCalculateTextWithoutSpace("(");
            } else {
                addCalculateTextWithoutSpace(num);
            }

            final String calculate_text = CalculatorEngine.calculate(balanceParentheses(getCalculateText()));
            if(!isInvalidInput(calculate_text)) {
                setResultText(calculate_text);
            } else {
                setResultText("");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        setCalculateText(replacePiWithSymbolInString(getCalculateText()));

        formatResultTextAfterTyping();
        adjustTextSize();
        scrollToEnd(findViewById(R.id.calculate_scrollview));
    }

    /**
     * This method converts an integer into a string representing a small number.
     * It utilizes Unicode characters for subscript numbers (Unicode Block: U+208x).
     * For example, it converts 0 to "₀", 1 to "₁", 2 to "₂", and so on.
     *
     * @param num An integer to be converted to a small number.
     * @return A string representing the small number.
     */
    private static String convertToSmallNumber(int num) {
        // Unicode block for subscript numbers: U+208x
        char subNull = '₀';
        int subNum = num + (int) subNull;
        return Character.toString((char) subNum);
    }

    /**
     * This method is called when a clipboard button is clicked.
     * It interacts with the system clipboard service to perform various clipboard operations.
     *
     * @param c The operation to be performed. This can be "MC" to clear the clipboard, "MR" to retrieve data from the clipboard, or "MS" to save data to the clipboard.
     */
    public void ClipboardAction(final String c) {
        final String mode;
        try {
            mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (mode.equals("false")) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            switch (c) {
                case "MC": {
                    ClipData clipData = ClipData.newPlainText("", "");
                    clipboardManager.setPrimaryClip(clipData);
                    showToastLong(getString(R.string.clipboardCleared), getApplicationContext());
                    break;
                }
                case "MR":
                    handleMRAction(clipboardManager);
                    break;
                case "MS": {
                    ClipData clipData = ClipData.newPlainText("", getResultText());
                    clipboardManager.setPrimaryClip(clipData);
                    showToastShort(getString(R.string.savedvalue), getApplicationContext());
                    break;
                }
            }
        }
        formatResultTextAfterTyping();

        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            setResultText("");
        }

        scrollToEnd(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));
    }

    /**
     * This method is called when the MR (Memory Recall) button is clicked.
     * It retrieves data from the clipboard and sets it as the result text if it's a valid number.
     *
     * @param clipboardManager The ClipboardManager instance used to interact with the system clipboard.
     */
    private void handleMRAction(ClipboardManager clipboardManager) {
        ClipData clipData = clipboardManager.getPrimaryClip();

        if (clipData == null || clipData.getItemCount() == 0) {
            // Handle the case where clipboard data is null or empty
            showToastShort(getString(R.string.clipboardIsEmpty), getApplicationContext());
            return;
        }

        ClipData.Item item = clipData.getItemAt(0);
        String clipText = (String) item.getText();

        if(!clipText.isEmpty()) {
            addCalculateTextWithoutSpace(clipText.replace(" ", ""));
            showToastShort(getString(R.string.pastedClipboard), getApplicationContext());
        }

        setResultText(CalculatorEngine.calculate(getCalculateText()));

        scrollToEnd(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));
    }

    /**
     * This method is called when an operation button is clicked.
     * It performs various actions based on the operation and updates the calculate text and result text accordingly.
     *
     * @param op The operation to be performed. This can be any mathematical operation like addition (+), subtraction (-), multiplication (*), or division (/).
     */
    public void OperationAction(final String op) {
        final String new_op = op.replace("*", "×").replace("/", "÷");
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                addCalculateTextWithoutSpace(getResultText() + new_op);
                return;
            }
            addCalculateTextWithoutSpace(new_op);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            setResultText("");
        }

        formatResultTextAfterTyping();
        scrollToEnd(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));
    }

    /**
     * This method is called when the C, CE, or backspace button is clicked.
     * It performs different actions based on the button that was clicked.
     *
     * @param e The action to be performed. This can be "⌫" for the backspace button, "C" for the C button, or "CE" for the CE button.
     */
    public void EmptyAction(final String e) {
        switch (e) {
            case "⌫":
                handleBackspaceAction();
                scrollToEnd(findViewById(R.id.calculate_scrollview));
                break;
            case "C":
                setResultText("0");
                setCalculateText("");
                dataManager.saveToJSONSettings("logX", "false", getApplicationContext());
                break;
            case "CE":
                setResultText("0");
                break;
        }

        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        }

        if(getCalculateText().isEmpty()) {
            setResultText("0");
        }
    }

    /**
     * This method is called when the backspace button is clicked.
     * It removes the last character from the result text.
     * If the result text is "Ungültige Eingabe", it resets the calculate text and result text.
     * If the result text is empty after removing the last character, it sets the result text to "0".
     * It then formats the result text and saves the numbers to the application context.
     */
    private void handleBackspaceAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        if(!getCalculateText().isEmpty()) {
            if(isOperator(getCalculateText().substring(getCalculateText().length() - 1))) {
                setCalculateText(getCalculateText().substring(0, getCalculateText().length() - 1));
            } else {
                setCalculateText(removeOperators(getCalculateText().substring(0, getCalculateText().length() - 1)));
            }
        } else {
            setResultText("0");
        }

        formatResultTextAfterTyping();
        adjustTextSize();
    }

    /**
     * Removes the trailing operator from a mathematical expression string.
     * <p>
     * This method analyzes the end of the input string to identify and remove a specific set of mathematical operators.
     * The recognized operators include common functions (e.g., "sin(", "ln(", "√("), inverse functions, and special cases like "RanInt(".
     * If the input string ends with one of these operators (or its prefix if it's a function with parentheses),
     * the method removes the operator and returns the modified string. If no recognized operator is found,
     * the original string is returned unchanged.
     *
     * @param input The mathematical expression string to process.
     * @return The input string with the trailing operator removed, or the original string if no recognized operator was found.
     */
    public static String removeOperators(String input) {
        String[] operators = {"³√(", "ln(", "tanh⁻¹(", "cosh⁻¹(", "sinh⁻¹(", "tan⁻¹(", "cos⁻¹(", "sin⁻¹(",
                "tanh(", "cosh(", "sinh(", "tan(", "cos(", "sin(", "√(", "Pol(", "Rec(",
                "RanInt", "Ran"};

        // Regular expression to match log with any subscript followed by "("
        Pattern logPattern = Pattern.compile("log[₀-₉]+\\($");

        int endIndex = input.length() - 1;
        boolean foundOperator = false;

        // Check for the regular operators first
        for (String operator : operators) {
            if (input.endsWith(operator) || (operator.endsWith("(") && input.endsWith(operator.substring(0, operator.length() - 1))) || input.endsWith("Ran#")) {
                endIndex -= operator.length() - (operator.endsWith("(") ? 1 : 0);
                foundOperator = true;
                break;
            }
        }

        // Check for log with subscript if no other operator is found
        if (!foundOperator) {
            Matcher logMatcher = logPattern.matcher(input);
            if (logMatcher.find() && logMatcher.end() == input.length()) {
                endIndex -= logMatcher.end() - logMatcher.start() - 1;
                foundOperator = true;
            }
        }

        // Ensure endIndex is within bounds
        endIndex = Math.max(0, endIndex);

        if (foundOperator) {
            if (input.substring(0, endIndex).isEmpty()) {
                return input.substring(0, endIndex);
            } else {
                return input.substring(0, endIndex + 1);
            }
        }
        return input;
    }

    /**
     * This method is called when the negate button is clicked.
     * It toggles the sign of the result text.
     * If the first character of the result text is "-", it removes the "-" from the result text.
     * If the first character of the result text is not "-", it adds "-" to the beginning of the result text.
     */
    public void NegativAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());

        addCalculateTextWithoutSpace("-");

        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            setResultText("");
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method is called when the comma button is clicked.
     * It adds a comma to the result text if it does not already contain a comma.
     */
    public void CommaAction() {
        addCalculateTextWithoutSpace(",");
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());

        if(!isInvalidInput(CalculatorEngine.calculate(getCalculateText()))) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            setResultText("");
        }

        formatResultTextAfterTyping();
    }

    /**
     * This method is called when the calculate button is clicked.
     * It performs the calculation based on the current calculate text and updates the result text with the result of the calculation.
     * If the rotate operator is true, it handles the calculation in a specific way.
     * If the calculate text does not contain an equals sign, it sets the last number to the current result text, adds an equals sign to the calculate text, and sets the result text to the result of the calculation.
     * If the calculate text does contain an equals sign, it checks if the last operator is not empty and not a square root. If so, it sets the calculate text to the current result text followed by the last operator and the last number, and an equals sign. Otherwise, it sets the calculate text to the current result text followed by an equals sign. It then sets the result text to the result of the calculation.
     * If the rotate operator is false, it checks if the calculate text does not contain an equals sign. If so, it sets the last number to the current result text, sets the calculate label text to the current calculate text followed by the current result text and an equals sign, and sets the result label text to the result of the calculation.
     * If the calculate text does contain an equals sign, it checks if the last operator is not empty. If so, it sets the calculate label text to the current result text followed by the last operator and the last number, and an equals sign. Otherwise, it sets the calculate label text to the current result text followed by an equals sign. It then sets the result label text to the result of the calculation.
     * After performing the calculation, it formats the result text, sets removeValue to true, and adjusts the text size.
     */
    @SuppressLint("SetTextI18n")
    public void Calculate() {
        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                return;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if(!isInvalidInput(getResultText()) && getCalculateText().contains("RanInt") && getCalculateText().contains("Ran#")) {
            setResultText(CalculatorEngine.calculate(getCalculateText()));
        } else {
            if(getCalculateText().isEmpty()) {
                setResultText("0");
            } else {
                setCalculateText(balanceParentheses(getCalculateText()));
                setResultText(CalculatorEngine.calculate(getCalculateText()));
            }
        }

        formatResultTextAfterTyping();
        adjustTextSize();

        if(!isNumber(getCalculateText()) && (!getCalculateText().replace("=", "").replace(" ", "").equals("π") ||
                !getCalculateText().replace("=", "").replace(" ", "").equals("e"))
                && !isInvalidInput(getResultText())
                && !removeNumbers(getCalculateText()
                    .replace(" ", "")
                    .replace("=", "")
                    .replace(".", "")
                    .replace(",", "")).isEmpty()) {

            addToHistory(fixExpression(balanceParentheses(getCalculateText())));

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkForAskNotification();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if(!isInvalidInput(getResultText()) && !getCalculateText().contains("RanInt") && !getCalculateText().contains("Ran")) {
            dataManager.saveToJSONSettings("pressedCalculate", true, getApplicationContext());
            setCalculateText("");
        }
    }

    private String removeNumbers(String calculation) {
        if(calculation.isEmpty()) {
            return "";
        }

        StringBuilder formattedCalculation = new StringBuilder();
        for(int x = 0; x < calculation.length(); x++) {
            if(!Character.isDigit(calculation.charAt(x))) {
                formattedCalculation.append(calculation.charAt(x));
            }
        }

        return formattedCalculation.toString();
    }

    private String replacePiWithSymbolInString(String text) {
        try {
            if(dataManager.getJSONSettingsData("refactorPI", getApplicationContext()).getString("value").equals("true")) {
                boolean isPI = false;
                int start, end;
                int l, m, n;

                for(l = 0; l < text.length(); l++) {
                    if(!(l + 3 < text.length())) {
                        break;
                    }

                    isPI = text.startsWith("3,14", l);
                    if(isPI) {
                        start = l;
                        for(m = 0; m < PI.length(); m++) {
                            if(l + m >= text.length() || !String.valueOf(PI.charAt(m)).equals(String.valueOf(text.charAt(l + m)))) {
                                for(n = l + m; n < text.length(); n++) {
                                    if(!Character.isDigit(text.charAt(n))) {
                                        break;
                                    }
                                }
                                end = n;
                                String partBefore = text.substring(0, start);
                                String partAfter = text.substring(end);
                                text = partBefore + "π" + partAfter;
                                isPI = false;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    public static boolean isNumber(String input) {
        String numberPattern = "^-?\\d+(\\,|\\.)?\\d*(\\.|\\,)?\\d*$";
        Pattern pattern = Pattern.compile(numberPattern);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private void addToHistory(String input) {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd. MMMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());

        input = input.replace("=", "").replace(" ", "");

        // Code snippet to save calculation to history
        final Context context = getApplicationContext();
        String finalInput = input;
        new Thread(() -> runOnUiThread(() -> {
            try {
                int old_value = Integer.parseInt(dataManager.getHistoryData("historyTextViewNumber", context).getString("value"));

                int new_value = old_value + 1;

                dataManager.updateValuesInHistoryData("historyTextViewNumber", "value", Integer.toString(new_value), context);
                String calculate_text = finalInput;

                if (calculate_text.isEmpty()) {
                    calculate_text = "0";
                }
                if (!calculate_text.contains("=")) {
                    calculate_text = calculate_text + "=";
                }

                //Log.e("DEBUG", dataManager.getHistoryData("historyTextViewNumber", context).getString("value"));

                if(calculate_text.contains("Rec") || calculate_text.contains("Pol")) {
                    calculate_text = calculate_text.replace("=", ": ");
                }

                dataManager.saveToHistory(String.valueOf(old_value + 1), formattedDate, "",
                        balanceParentheses(fixExpression(calculate_text)) + getResultText(), context);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        })).start();
    }

    /**
     * This method checks if the input text is invalid.
     *
     * @param text The text to be checked. This should be a string containing the text input from the user or the result of a calculation.
     * @return Returns true if the text is invalid (contains "Ungültige Eingabe", "Unendlich", "Syntax Fehler", or "Domainfehler"), and false otherwise.
     */
    public boolean isInvalidInput(String text) {
        return  text.equals(getString(R.string.errorMessage1))  ||
                text.equals(getString(R.string.errorMessage2))  ||
                text.equals(getString(R.string.errorMessage3))  ||
                text.equals(getString(R.string.errorMessage4))  ||
                text.equals(getString(R.string.errorMessage5))  ||
                text.equals(getString(R.string.errorMessage6))  ||
                text.equals(getString(R.string.errorMessage7))  ||
                text.equals(getString(R.string.errorMessage8))  ||
                text.equals(getString(R.string.errorMessage9))  ||
                text.equals(getString(R.string.errorMessage10)) ||
                text.equals(getString(R.string.errorMessage11)) ||
                text.equals(getString(R.string.errorMessage12)) ||
                text.equals(getString(R.string.errorMessage13)) ||
                text.equals(getString(R.string.errorMessage14)) ||
                text.equals(getString(R.string.errorMessage15)) ||
                text.equals(getString(R.string.errorMessage16)) ||
                text.equals(getString(R.string.errorMessage17)) ||

                text.contains(getString(R.string.errorMessage1))  ||
                text.contains(getString(R.string.errorMessage2))  ||
                text.contains(getString(R.string.errorMessage3))  ||
                text.contains(getString(R.string.errorMessage4))  ||
                text.contains(getString(R.string.errorMessage5))  ||
                text.contains(getString(R.string.errorMessage6))  ||
                text.contains(getString(R.string.errorMessage7))  ||
                text.contains(getString(R.string.errorMessage8))  ||
                text.contains(getString(R.string.errorMessage9))  ||
                text.contains(getString(R.string.errorMessage10)) ||
                text.contains(getString(R.string.errorMessage11)) ||
                text.contains(getString(R.string.errorMessage12)) ||
                text.contains(getString(R.string.errorMessage13)) ||
                text.contains(getString(R.string.errorMessage14)) ||
                text.contains(getString(R.string.errorMessage15)) ||
                text.contains(getString(R.string.errorMessage16)) ||
                text.contains(getString(R.string.errorMessage17));
    }

    /**
     * Extracts digits from an input string and groups them with dots every three digits.
     *
     * @param input is the input string from which digits are to be extracted.
     * @return the extracted digits as a string.
     */
    private String extractDigitsWithGrouping(String input) {
        StringBuilder resultBuilder = new StringBuilder();
        boolean isNegative = input.contains("-");
        input = input.replace(".", "").replace("-", "");
        int count = 0;

        if(input.isEmpty()) {
            return "";
        }

        for (int x = input.length(); x > 0; x--) {
            if (Character.isDigit(input.charAt(x - 1))) {
                count++;
                resultBuilder.insert(0, input.charAt(x - 1));
                if (count == 3 && x != 1) {
                    resultBuilder.insert(0, ".");
                    count = 0;
                }
            } else {
                resultBuilder.insert(0, input.charAt(x - 1));
            }
        }

        return (isNegative ? "-" : "") + resultBuilder.toString();
    }

    /**
     * Formats the result text after a user types something.
     * This method performs the following steps:
     * 1. Removes any trailing decimal points from the result text.
     * 2. Checks if the result text contains a comma.
     * 3. If the result text contains a comma, it splits the text into two parts at the comma and formats each part separately.
     * 4. If the result text does not contain a comma, it simply formats the entire result text.
     * 5. Sets the formatted result text.
     */
    public void formatResultTextAfterTyping() {
        String resultText = getResultText().replace(".", "");
        String[] resultParts;
        String[] resultParts2;
        StringBuilder resultTextBuilder = new StringBuilder();

        if(resultText.contains("r=") || resultText.contains("θ=")) {
            resultParts = resultText
                    .replace(" ", "")
                    .replace("r=", "")
                    .replace("θ=", "")
                    .split(";");

            if (resultParts[0].contains(",")) {
                resultParts2 = resultParts[0].split(",");

                resultTextBuilder
                        .append("r= ")
                        .append(extractDigitsWithGrouping(resultParts2[0]));
            }
            if (resultParts[1].contains(",")) {
                resultParts2 = resultParts[1].split(",");

                resultTextBuilder
                        .append("θ= ")
                        .append(extractDigitsWithGrouping(resultParts2[0]));
            }
        } else if(resultText.contains("x=") || resultText.contains("y=")) {
            resultParts = resultText
                    .replace(" ", "")
                    .replace("x=", "")
                    .replace("y=", "")
                    .split(";");

            if (resultParts[0].contains(",")) {
                resultParts2 = resultParts[0].split(",");

                resultTextBuilder
                        .append("x= ")
                        .append(extractDigitsWithGrouping(resultParts2[0]));
            }
            if (resultParts[1].contains(",")) {
                resultParts2 = resultParts[1].split(",");

                resultTextBuilder
                        .append("y= ")
                        .append(extractDigitsWithGrouping(resultParts2[0]));
            }
        } else {
            if (resultText.contains(",")) {
                resultParts = resultText.split(",");
                setResultText(extractDigitsWithGrouping(resultParts[0]) + "," + resultParts[1]);
            } else {
                setResultText(extractDigitsWithGrouping(resultText));
            }
        }
    }
    public String formatResultText(String input) {
        input = input.replace(".", "");

        if (input.contains(",")) {
            String[] resultParts = input.split(",");
            return extractDigitsWithGrouping(resultParts[0]) + "," + resultParts[1];
        } else {
            return extractDigitsWithGrouping(input);
        }
    }

    /**
     * Adjusts the text size of two TextViews dynamically.
     * <p>
     * This method is responsible for adjusting the text size of TextViews in two separate ScrollViews
     * to ensure better readability and user experience. It first scrolls the first ScrollView to the bottom,
     * then adjusts the text size of the label within it using a uniform text size configuration.
     * After that, it scrolls the second ScrollView to the top and adjusts the text size of the label within
     * it using another uniform text size configuration.
     * <p>
     * Note: The TextViews and ScrollViews are defined in the layout XML with specific IDs:
     * <br>
     * - R.id.calculate_scrollview: ScrollView containing the first TextView
     * <br>
     * - R.id.calculate_label: TextView whose text size needs adjustment within the first ScrollView
     * <br>
     * - R.id.result_scrollview: ScrollView containing the second TextView
     * <br>
     * - R.id.result_label: TextView whose text size needs adjustment within the second ScrollView
     */
    public void adjustTextSize() {

        // TODO: fix later
        // ignore this function (too many bugs in the TextViews)
        // i wrote 'if(true)' because if i don't do it, the ide would throw an error message
        if(true) {
            return;
        }

        if(findViewById(R.id.calculate_label) != null && findViewById(R.id.result_label) != null) {
            HorizontalScrollView calculate_scrollview = findViewById(R.id.calculate_scrollview);
            HorizontalScrollView result_scrollview = findViewById(R.id.result_scrollview);

            TextView label1 = findViewById(R.id.calculate_label);
            TextView label2 = findViewById(R.id.result_label);
            try {
                if(dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value").equals("true")) {
                    scrollToStart(calculate_scrollview);
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        //label1.setTextSize(35f);
                        label1.setAutoSizeTextTypeUniformWithConfiguration(25, 35, 1, TypedValue.COMPLEX_UNIT_SP);
                        //label2.setTextSize(30f);
                        label2.setAutoSizeTextTypeUniformWithConfiguration(35, 45, 1, TypedValue.COMPLEX_UNIT_SP);
                    } else {
                        //label1.setTextSize(30f);
                        label1.setAutoSizeTextTypeUniformWithConfiguration(20, 30, 1, TypedValue.COMPLEX_UNIT_SP);
                        //label2.setTextSize(35f);
                        label2.setAutoSizeTextTypeUniformWithConfiguration(40, 50, 1, TypedValue.COMPLEX_UNIT_SP);
                    }
                } else {
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        //label1.setTextSize(45f);
                        label1.setAutoSizeTextTypeUniformWithConfiguration(35, 45, 1, TypedValue.COMPLEX_UNIT_SP);
                        //label2.setTextSize(40f);
                        label2.setAutoSizeTextTypeUniformWithConfiguration(45, 55, 1, TypedValue.COMPLEX_UNIT_SP);
                    } else {
                        //label1.setTextSize(40f);
                        label1.setAutoSizeTextTypeUniformWithConfiguration(30, 40, 1, TypedValue.COMPLEX_UNIT_SP);
                        //label2.setTextSize(45f);
                        label2.setAutoSizeTextTypeUniformWithConfiguration(50, 60, 1, TypedValue.COMPLEX_UNIT_SP);
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            try {
                if (!dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")){
                    calculate_scrollview.post(() -> calculate_scrollview.fullScroll(ScrollView.FOCUS_DOWN));
                    result_scrollview.post(() -> result_scrollview.fullScroll(ScrollView.FOCUS_UP));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * This method is called when the back button is pressed.
     * It calls the superclass's onBackPressed method and then finishes the activity.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        finishActivity(1);
    }

    /**
     * The following methods are simple getter and setter methods for various properties.
     */
    private void checkCalculateText() {
        if(getCalculateText().contains("=")) {
            setCalculateText("");
            if(isInvalidInput(getResultText())) {
                setResultText("");
            }
            setRemoveValue(true);
        }
    }
    public void setIsNotation(final boolean val) {
        dataManager.saveToJSONSettings("isNotation", val, getApplicationContext());
        //("setIsNotation", "isNotation: '" + val + "'");
    }
    public boolean getIsNotation() {
        try {
            return Boolean.parseBoolean(dataManager.getJSONSettingsData("isNotation", getApplicationContext()).getString("value"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public String getLastOp() {
        final String last_op;
        try {
            last_op = dataManager.getJSONSettingsData("lastop", getApplicationContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return last_op.replace("*", "×").replace("/", "÷");
    }
    public void setLastOp(final String s) {
        dataManager.saveToJSONSettings("lastop", s, getApplicationContext());
        /*
        try {
            Log.i("setLastOp", "lastOp: " + dataManager.getJSONSettingsData("lastop", getApplicationContext()).getString("value"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        */
    }
    public boolean getRemoveValue() {
        final String value;
        try {
            value = dataManager.getJSONSettingsData("removeValue", getApplicationContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        dataManager.saveToJSONSettings("removeValue", "false", getApplicationContext());
        return value.equals("true");
    }
    public void setRemoveValue(final boolean b) {
        dataManager.saveToJSONSettings("removeValue", b, getApplicationContext());
        /*
        try {
            Log.i("setRemoveValue", "removeValue: " + dataManager.getJSONSettingsData("removeValue", getApplicationContext()).getString("value"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        */
    }
    public void setLastNumber(final String s) {
        //try {
            final String last_number = s.replace(".", "");
            dataManager.saveToJSONSettings("lastnumber", last_number, getApplicationContext());
            //Log.i("setLastNumber", "lastNumber: " + dataManager.getJSONSettingsData("lastnumber", getApplicationContext()));
        //} catch (JSONException e) {
        //    throw new RuntimeException(e);
        //}
    }
    public String getLastNumber() {
        try {
            final String last_number = dataManager.getJSONSettingsData("lastnumber", getApplicationContext()).getString("value");
            dataManager.saveToJSONSettings("lastnumber", "0", getApplicationContext());
            return last_number;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public String getResultText() {
        TextView resulttext = findViewById(R.id.result_label);
        if(resulttext != null) {
            return resulttext.getText().toString();
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    public void addResultText(final String s) {
        TextView resulttext = findViewById(R.id.result_label);
        if(resulttext != null) { resulttext.setText(getResultText() + s); }
    }
    public void setResultText(final String s) {
        TextView resulttext = findViewById(R.id.result_label);
        if(resulttext != null) { resulttext.setText(s); }
    }
    public String getCalculateText() {
        if(findViewById(R.id.calculate_label) != null) {
            TextView calculatetext = findViewById(R.id.calculate_label);
            return calculatetext.getText().toString();
        } else {
            return "";
        }
    }
    @SuppressLint("SetTextI18n")
    public void addCalculateText(final String s) {
        TextView calculatetext = findViewById(R.id.calculate_label);
        calculatetext.setText(getCalculateText() + " " + s);
    }
    @SuppressLint("SetTextI18n")
    public void addCalculateTextWithoutSpace(final String s) {
        TextView calculatetext = findViewById(R.id.calculate_label);
        calculatetext.setText(getCalculateText() + s);
    }
    public void setCalculateText(final String s) {
        if(findViewById(R.id.calculate_label) != null) {
            TextView calculatetext = findViewById(R.id.calculate_label);
            calculatetext.setText(s);
        }
    }
}