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

import static com.mlprograms.rechenmax.CalculatorEngine.fixExpression;
import static com.mlprograms.rechenmax.CalculatorEngine.isOperator;
import static com.mlprograms.rechenmax.CalculatorEngine.isStandardOperator;
import static com.mlprograms.rechenmax.CalculatorEngine.setMainActivity;
import static com.mlprograms.rechenmax.NumberHelper.PI;
import static com.mlprograms.rechenmax.ToastHelper.showToastLong;
import static com.mlprograms.rechenmax.ToastHelper.showToastShort;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MainActivity
 * @author Max Lemberg
 * @version 1.7.8
 * @date 06.04.2024
 */

public class MainActivity extends AppCompatActivity {
    private Context context = this;
    private DataManager dataManager;

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

        // If it's the first run of the application
        try {
            //switch(dataManager.getJSONSettingsData("lastActivity", getApplicationContext()).getString("value")) {
            //    case "Set":
            //        switchToSettingsAction();
            //        break;
            //    case "Rep":
            //        switchToReportAction();
            //        break;
            //    case "Con":
            //        switchToConvertAction();
            //        break;
            //    case "Help":
            //        switchToHelpAction();
            //        break;
            //    case "His":
            //        switchToHistoryAction();
            //        break;
            //    default:
            //        dataManager.saveToJSONSettings("lastActivity", "Main", getApplicationContext());
            //        break;
            //}

            JSONObject currentVersionData = dataManager.getJSONSettingsData("currentVersion", getApplicationContext());
            JSONObject oldVersionData = dataManager.getJSONSettingsData("old_version", getApplicationContext());

            if (currentVersionData.has("value") && oldVersionData.has("value")) {
                String currentValue = currentVersionData.getString("value");
                String oldValue = oldVersionData.getString("value");

                if (!Objects.equals(currentValue, oldValue)) {
                    // Set the flag to show patch notes and switch to the patch notes layout
                    dataManager.saveToJSONSettings("showPatchNotes", true, getApplicationContext());
                    dataManager.saveToJSONSettings("old_version", currentValue, getApplicationContext());
                    dataManager.saveToJSONSettings("returnToCalculator", "true", getApplicationContext());
                    //HelpActivity.setMainActivityContext(this);
                    //startActivity(new Intent(this, HelpActivity.class));
                } else {
                    // Read values from DataManager
                    final JSONObject showPatchNotesData = dataManager.getJSONSettingsData("showPatchNotes", getApplicationContext());
                    final JSONObject disablePatchNotesTempData = dataManager.getJSONSettingsData("disablePatchNotesTemporary", getApplicationContext());

                    if (showPatchNotesData.has("value") && disablePatchNotesTempData.has("value")) {
                        String showPatchNotesValue = showPatchNotesData.getString("value");
                        String disablePatchNotesTempValue = disablePatchNotesTempData.getString("value");

                        // If patch notes are set to be shown and not temporarily disabled, switch to patch notes layout
                        if ("true".equals(showPatchNotesValue) && "false".equals(disablePatchNotesTempValue)) {
                            setContentView(R.layout.patchnotes);
                            checkDarkmodeSetting();
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Load numbers, set up listeners, check science button state, check dark mode setting, format result text, adjust text size
        setUpListeners();
        showOrHideScienceButtonState();
        checkDarkmodeSetting();
        try {
            dataManager.loadNumbers();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();

        // Scroll down in the calculate label
        scrollToStart(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));

        // Show all settings
        //showAllSettings();
        adjustTextSize();

        try {
            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true") &&
                dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setCalculateText("");

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1.5F;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setCalculateText(getCalculateText().replace(" ", "").replace("=", ""));
            } else {
                setCalculateText(addSpaceToOperators(getCalculateText().replace(" ", "")));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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

        setActionButtonListener(R.id.okay_button, this::patchNotesOkayButtonAction);

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

        setButtonListener(R.id.clipOn, this::parenthesisOnAction);
        setButtonListener(R.id.clipOff, this::parenthesisOffAction);

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
        setButtonListener(R.id.e, this::eAction); // Manuelle Notation
        setButtonListener(R.id.е, this::еAction); // Eulersche Zahl
        setButtonListener(R.id.pi, this::piAction);

        setButtonListenerWithoutChangedWeight(R.id.half, this::halfAction);
        setButtonListenerWithoutChangedWeight(R.id.third, this::thirdAction);
        setButtonListenerWithoutChangedWeight(R.id.quarter, this::quarterAction);

        setButtonListener(R.id.thirdRoot, this::thirdRootAction);

        setButtonListenerWithoutChangedWeight(R.id.scientificButton, this::setScienceButtonState);
        setButtonListenerWithoutChangedWeight(R.id.shift, this::setShiftButtonState);

        setLongTextViewClickListener(R.id.calculate_label, this::saveCalculateLabelData);
        setLongTextViewClickListener(R.id.result_label, this::saveResultLabelData);

        if (findViewById(R.id.functionMode_text) != null) {
            findViewById(R.id.functionMode_text).setOnClickListener(view -> changeFunctionMode());
        }
        if (findViewById(R.id.shiftMode_text) != null) {
            findViewById(R.id.shiftMode_text).setOnClickListener(view -> setShiftButtonState());
        }
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
                if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            // Update the displayed text with the new function mode
            if (function_mode_text != null) {
                try {
                    function_mode_text.setText(dataManager.getJSONSettingsData("functionMode", getApplicationContext()).getString("value"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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
            } else {
                dataManager.saveToJSONSettings("showScienceRow", "false", getApplicationContext());
            }

            // Handle the visual representation or behavior associated with the state change
            showOrHideScienceButtonState();
            adjustTextSize();

            if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true") &&
                    dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setCalculateText("");

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1.5F;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
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
            if (dataManager.getJSONSettingsData("shiftRow", getApplicationContext()).getString("value").equals("3")) {
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
            LinearLayout buttonRow1 = findViewById(R.id.scientificRow11);
            LinearLayout buttonRow2 = findViewById(R.id.scientificRow21);
            LinearLayout buttonRow12 = findViewById(R.id.scientificRow12);
            LinearLayout buttonRow22 = findViewById(R.id.scientificRow22);
            LinearLayout buttonRow13 = findViewById(R.id.scientificRow13);
            LinearLayout buttonRow23 = findViewById(R.id.scientificRow23);
            LinearLayout buttonRow3 = findViewById(R.id.scientificRow3);
            TextView shiftModeText = findViewById(R.id.shiftMode_text);

            // Read the current state of the shift button from the stored data
            final String shiftValue = dataManager.getJSONSettingsData("shiftRow", getApplicationContext()).getString("value");
            final String rowValue = dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value");

            // Toggle the visibility of different LinearLayouts and update TextView based on the shift button state
            if(rowValue.equals("true") && (buttonRow1 != null && buttonRow2 != null && buttonRow12 != null
                    && buttonRow22 != null && buttonRow13 != null && buttonRow23 != null && buttonRow3 != null
                    && shiftModeText != null)) {
                switch (shiftValue) {
                    case "1":
                        buttonRow1.setVisibility(View.VISIBLE);
                        buttonRow2.setVisibility(View.VISIBLE);
                        buttonRow12.setVisibility(View.GONE);
                        buttonRow22.setVisibility(View.GONE);
                        buttonRow13.setVisibility(View.GONE);
                        buttonRow23.setVisibility(View.GONE);
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
                        buttonRow3.setVisibility(View.VISIBLE);
                        shiftModeText.setText("3");
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

        LinearLayout buttonRow1 = findViewById(R.id.scientificRow11);
        LinearLayout buttonRow12 = findViewById(R.id.scientificRow12);
        LinearLayout buttonRow13 = findViewById(R.id.scientificRow13);

        LinearLayout buttonRow2 = findViewById(R.id.scientificRow21);
        LinearLayout buttonRow22 = findViewById(R.id.scientificRow22);
        LinearLayout buttonRow23 = findViewById(R.id.scientificRow23);

        LinearLayout buttonRow3 = findViewById(R.id.scientificRow3);

        Button shiftButton = findViewById(R.id.shift);

        LinearLayout buttonLayout = findViewById(R.id.button_layout);

        if (function_mode_text != null) {
            try {
                function_mode_text.setText(dataManager.getJSONSettingsData("functionMode", getApplicationContext()).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        if(buttonLayout != null) {
            try {
                LinearLayout.LayoutParams layoutParams;
                if (dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value").equals("false")) {

                    layoutParams = (LinearLayout.LayoutParams) buttonLayout.getLayoutParams();
                    layoutParams.weight = 4;
                    buttonLayout.setLayoutParams(layoutParams);

                    buttonRow1.setVisibility(View.GONE);
                    buttonRow2.setVisibility(View.GONE);
                    buttonRow12.setVisibility(View.GONE);
                    buttonRow22.setVisibility(View.GONE);
                    buttonRow13.setVisibility(View.GONE);
                    buttonRow23.setVisibility(View.GONE);
                    shiftButton.setVisibility(View.GONE);
                    buttonRow3.setVisibility(View.GONE);

                    assert function_mode_text != null;
                    assert shiftModeText != null;
                    function_mode_text.setVisibility(View.GONE);
                    shiftModeText.setVisibility(View.GONE);
                } else {
                    layoutParams = (LinearLayout.LayoutParams) buttonLayout.getLayoutParams();
                    layoutParams.weight = 7;
                    buttonLayout.setLayoutParams(layoutParams);

                    buttonRow1.setVisibility(View.VISIBLE);
                    buttonRow2.setVisibility(View.VISIBLE);
                    buttonRow12.setVisibility(View.VISIBLE);
                    buttonRow22.setVisibility(View.VISIBLE);
                    buttonRow13.setVisibility(View.VISIBLE);
                    buttonRow23.setVisibility(View.VISIBLE);
                    shiftButton.setVisibility(View.VISIBLE);
                    buttonRow3.setVisibility(View.VISIBLE);

                    assert function_mode_text != null;
                    assert shiftModeText != null;
                    function_mode_text.setVisibility(View.VISIBLE);
                    shiftModeText.setVisibility(View.VISIBLE);
                }

                layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
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

                try {
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
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

                try {
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
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

                try {
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
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

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);

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

                try {
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
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

                try {
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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

                try {
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
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

                try {
                    if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void resetIfPressedCalculate() {
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("sin(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("sin(");
                        } else {
                            addCalculateText("sin(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("sinh(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("sinh(");
                        } else {
                            addCalculateText("sinh(");
                        }
                    }
                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("sin⁻¹(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("sin⁻¹(");
                        } else {
                            addCalculateText("sin⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("sinh⁻¹(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("sinh⁻¹(");
                        } else {
                            addCalculateText("sinh⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("cos(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("cos(");
                        } else {
                            addCalculateText("cos(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("cosh(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("cosh(");
                        } else {
                            addCalculateText("cosh(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("cos⁻¹(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("cos⁻¹(");
                        } else {
                            addCalculateText("cos⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("cosh⁻¹(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("cosh⁻¹(");
                        } else {
                            addCalculateText("cosh⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("tan(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("tan(");
                        } else {
                            addCalculateText("tan(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("tanh(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("tanh(");
                        } else {
                            addCalculateText("tanh(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("tan⁻¹(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("tan⁻¹(");
                        } else {
                            addCalculateText("tan⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("tanh⁻¹(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("tanh⁻¹(");
                        } else {
                            addCalculateText("tanh⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("log(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("log(");
                        } else {
                            addCalculateText("log(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("log₂(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("log₂(");
                        } else {
                            addCalculateText("log₂(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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
                    if(getCalculateText().contains("=")) {
                        setCalculateText("");
                        if(isInvalidInput(getResultText())) {
                            setResultText("0");
                        }
                        setRemoveValue(true);
                    }

                    dataManager.saveToJSONSettings("logX", "true", getApplicationContext());
                    if (getCalculateText().isEmpty()) {
                        setCalculateText("log");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("log");
                        } else {
                            addCalculateText("log");
                            setRotateOperator(true);
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
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

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("ln(");
                    } else {
                        if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("ln(");
                        } else {
                            addCalculateText("ln(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * Handles the insertion or removal of the "e" symbol based on its current presence in the result text.
     */
    private void eAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        // Check if logarithmic mode is disabled
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                showToastLong(getString(R.string.buttonIsDisabled), getApplicationContext());
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    // Read the current eNotation mode from the data manager
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");

                    // Check if eNotation mode is currently set to false
                    if (mode.equals("false")) {
                        // Check if the remove value flag is set
                        if(getRemoveValue()) {
                            setCalculateText("");
                            // If the result text is invalid, set it to "0"
                            if(isInvalidInput(getResultText())) {
                                setResultText("0");
                            }
                            setRemoveValue(false);
                        }

                        // Add or remove "e" based on its current presence in the result text
                        if (!getResultText().contains("e+") && !getResultText().contains("e-")) {
                            dataManager.saveToJSONSettings("eNotation", true, getApplicationContext());
                            addResultText("e");
                        } else if (getResultText().contains("e") && !getResultText().contains("e+") || !getResultText().contains("e-")) {
                            dataManager.saveToJSONSettings("eNotation", false, getApplicationContext());
                            setResultText(getResultText().replace("e", ""));
                        }
                    } else {
                        // Remove all occurrences of "e", "e+", and "e-" from the result text
                        setResultText(getResultText().replace("e+", "").replace("e-", "").replace("e", ""));
                        dataManager.saveToJSONSettings("eNotation", false, getApplicationContext());
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Format the result text after typing
        formatResultTextAfterType();
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
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                addCalculateTextWithoutSpace("е");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    // Read the current eNotation mode from the data manager
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");

                    // Check if eNotation mode is currently set to false
                    if (mode.equals("false")) {
                        // Check and handle the calculate text
                        checkCalculateText();

                        // Add "е" to the calculate text
                        if (getCalculateText().isEmpty()) {
                            setCalculateText("е");
                        } else {
                            final String text = getCalculateText().replace(" ", "")
                                    .replace("×", "*").replace("÷", "/");
                            char lastChar = text.charAt(text.length() - 1);

                            // Check the last character and add "е" accordingly
                            if(!isOperator(String.valueOf(lastChar)) && !String.valueOf(lastChar).equals("(")) {
                                if(getRotateOperator()) {
                                    addCalculateTextWithoutSpace("е");
                                } else {
                                    addCalculateText(getLastOp() + " е");
                                }
                            } else {
                                addCalculateText("е");
                            }
                        }
                        setRotateOperator(true);
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Format the result text after typing
        formatResultTextAfterType();
    }

    /**
     * Appends or sets the text "π" to the calculation input and sets the rotate operator flag to true.
     */
    private void piAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        // Check if logarithmic mode is disabled
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                addCalculateTextWithoutSpace("π");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    // Read the current eNotation mode from the data manager
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");

                    // Check if eNotation mode is currently set to false
                    if (mode.equals("false")) {
                        // Check and handle the calculate text
                        checkCalculateText();

                        // Add "π" to the calculate text
                        if (getCalculateText().isEmpty()) {
                            setCalculateText("π");
                        } else {
                            final String text = getCalculateText().replace(" ", "")
                                    .replace("×", "*").replace("÷", "/");
                            char lastChar = text.charAt(text.length() - 1);

                            // Check the last character and add "π" accordingly
                            if(!isOperator(String.valueOf(lastChar)) && !String.valueOf(lastChar).equals("(")) {
                                if(getRotateOperator()) {
                                    addCalculateTextWithoutSpace("π");
                                } else {
                                    addCalculateText(getLastOp() + " π");
                                }
                            } else {
                                addCalculateText("π");
                            }
                        }
                        setRotateOperator(true);
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Format the result text after typing
        formatResultTextAfterType();
    }

    /**
     * This method adds an opening parenthesis to the calculation text.
     */
    private void parenthesisOnAction() {
        // Check if calculate text is empty and set or add opening parenthesis accordingly
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                }
                addCalculateTextWithoutSpace("(");
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        checkCalculateText();

                        if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                            if (getCalculateText().isEmpty()) {
                                setCalculateText("(");
                            } else {
                                final String text = getCalculateText().replace(" ", "")
                                        .replace("×", "*").replace("÷", "/");
                                char lastChar = text.charAt(text.length() - 1);

                                if(!isOperator(String.valueOf(lastChar)) && !String.valueOf(lastChar).equals("(")) {
                                    if(getRotateOperator()) {
                                        addCalculateTextWithoutSpace("(");
                                    } else {
                                        addCalculateText(getLastOp() + " (");
                                    }
                                } else {
                                    addCalculateText("(");
                                }
                            }
                        } else {
                            addCalculateText(getResultText() + " (");
                            dataManager.saveToJSONSettings("logX", "false", getApplicationContext());
                            setRemoveValue(true);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        scrollToStart(findViewById(R.id.calculate_scrollview));

        setRotateOperator(false);
        formatResultTextAfterType();
    }

    /**
     * This method adds a closing parenthesis to the calculation text.
     * If the last operation was a square root, it adds a closing parenthesis.
     * Otherwise, it adds the result text and a closing parenthesis.
     */
    private void parenthesisOffAction() {
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                }
                addCalculateTextWithoutSpace(")");
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");

                    if (mode.equals("false")) {
                        checkCalculateText();

                        if(getCalculateText().contains("=")) {
                            setCalculateText("");
                            if(isInvalidInput(getResultText())) {
                                setResultText("0");
                            }
                        }

                        Pattern pattern = Pattern.compile("√\\(\\d+\\)$");
                        Matcher matcher = pattern.matcher(getCalculateText());

                        if(!getCalculateText().isEmpty() && getCalculateText().contains("(")) {
                            if (matcher.find()) {
                                addCalculateText(")");
                            } else {
                                if(!getRotateOperator()) {
                                    addCalculateText(getResultText() + " )");
                                } else {
                                    final String text = getCalculateText().replace(" ", "")
                                            .replace("×", "*").replace("÷", "/");
                                    char lastChar = text.charAt(text.length() - 1);

                                    if(String.valueOf(lastChar).equals(")")) {
                                        addCalculateText(" )");
                                    } else {
                                        if(!String.valueOf(lastChar).equals("½") &&
                                                !String.valueOf(lastChar).equals("⅓") &&
                                                !String.valueOf(lastChar).equals("¼")) {
                                            addCalculateText(getLastOp() + " " + getResultText() + " )");
                                        } else {
                                            addCalculateTextWithoutSpace(")");
                                        }
                                    }
                                }
                            }
                            setRotateOperator(true);
                            if(findViewById(R.id.calculate_scrollview) != null) {
                                scrollToStart(findViewById(R.id.calculate_scrollview));
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
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
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    addCalculateTextWithoutSpace(getResultText() + "!");
                    return;
                }
                addCalculateTextWithoutSpace("!");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        final String calc_text = getCalculateText().replace(" ", "");

                        checkCalculateText();

                        if (calc_text.isEmpty()) {
                            addCalculateText(getResultText() + "!");
                            setRotateOperator(true);
                        } else {
                            String lastchar = String.valueOf(calc_text.replace(" ", "").charAt(calc_text.length() - 1));
                            if (lastchar.equals("!")) {
                                addCalculateText(getLastOp().replace("*", "×").replace("/", "÷") + " " + getResultText() + "!");
                                setRotateOperator(true);
                            } else if(lastchar.equals(")")) {
                                addCalculateText("!");
                                setRotateOperator(true);
                            } else {
                                addCalculateText(getResultText() + "!");
                                setRotateOperator(true);
                            }
                        }
                        if(findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * This method adds a power operation to the calculation text.
     * Depending on the state of the rotate operator flag, it handles the power operation differently.
     */
    private void powerAction() {
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                    addCalculateTextWithoutSpace(getResultText() + "^");
                    return;
                }
                addCalculateTextWithoutSpace("^");
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        setLastOp("^");
                        checkCalculateText();

                        if(!getRotateOperator()) {
                            setRemoveValue(true);
                            setLastNumber(getResultText());
                            if (getCalculateText().contains("=")) {
                                setCalculateText(getResultText() + " ^");
                            } else {
                                addCalculateText(getResultText() + " ^");
                            }
                            setRemoveValue(true);
                        } else {
                            addCalculateText("^");
                            setRemoveValue(true);
                            setRotateOperator(false);
                        }
                        if(findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * This method adds a root operation to the calculation text.
     * Depending on the state of the rotate operator flag, it handles the root operation differently.
     */
    private void rootAction() {
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                }
                addCalculateTextWithoutSpace("√(");
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    checkCalculateText();

                    if (mode.equals("false")) {
                        if(!getRotateOperator()) {
                            addCalculateText("√(");
                        } else if (!getCalculateText().isEmpty()){
                            addCalculateText(getLastOp() + " √(");
                        }
                        setRemoveValue(true);
                        //setRotateOperator(true);
                        if(findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * This method adds a root operation to the calculation text.
     * Depending on the state of the rotate operator flag, it handles the root operation differently.
     */
    private void thirdRootAction() {
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                }
                addCalculateTextWithoutSpace("³√(");
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    checkCalculateText();

                    if (mode.equals("false")) {
                        if(!getRotateOperator()) {
                            addCalculateText("³√(");
                        } else if (!getCalculateText().isEmpty()){
                            addCalculateText(getLastOp() + " ³√(");
                        }
                        setRemoveValue(true);
                        //setRotateOperator(true);
                        if(findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
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
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    final String input = getResultText() + "÷2=";
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷2")));
                    formatResultTextAfterType();
                    addToHistory(input);
                    return;
                }
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                addCalculateTextWithoutSpace("½");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    checkCalculateText();

                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        if(!getRotateOperator()) {
                            addCalculateText("½");
                        } else if (!getCalculateText().isEmpty()){
                            addCalculateText(getLastOp() + " ½");
                        }
                        setRemoveValue(true);
                        setRotateOperator(true);
                        if(findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * This method performs the action of adding "⅓" (third) to the calculation text.
     * It follows a similar procedure to the halfAction() method but adds "⅓" instead of "½".
     */
    private void thirdAction() {
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    final String input = getResultText() + "÷3=";
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷3")));
                    formatResultTextAfterType();
                    addToHistory(input);
                    return;
                }
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                addCalculateTextWithoutSpace("⅓");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    checkCalculateText();

                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        if(!getRotateOperator()) {
                            addCalculateText("⅓");
                        } else if (!getCalculateText().isEmpty()){
                            addCalculateText(getLastOp() + " ⅓");
                        }
                        setRemoveValue(true);
                        setRotateOperator(true);
                        if(findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * This method performs the action of adding "¼" (quarter) to the calculation text.
     * It follows a similar procedure to the halfAction() method but adds "¼" instead of "½".
     */
    private void quarterAction() {
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    final String input = getResultText() + "÷4=";
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷4")));
                    formatResultTextAfterType();
                    addToHistory(input);
                    return;
                }
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                addCalculateTextWithoutSpace("¼");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    checkCalculateText();

                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        if(!getRotateOperator()) {
                            addCalculateText("¼");
                        } else if (!getCalculateText().isEmpty()){
                            addCalculateText(getLastOp() + " ¼");
                        }
                        setRemoveValue(true);
                        setRotateOperator(true);
                        if(findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * Handles the action when the okay button in the patch notes is clicked.
     * Depending on whether the checkbox is checked or not, it saves different values to JSON.
     * Then it sets the content view, loads numbers, checks dark mode setting, checks science button state, and sets up listeners.
     */
    public void patchNotesOkayButtonAction() {
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CheckBox checkBox = findViewById(R.id.checkBox);
        if (checkBox.isChecked()) {
            dataManager.saveToJSONSettings("showPatchNotes", false, getApplicationContext());
            dataManager.saveToJSONSettings("disablePatchNotesTemporary", true, getApplicationContext());
            dataManager.saveToJSONSettings("settingReleaseNotesSwitch", false, getApplicationContext());
        } else {
            dataManager.saveToJSONSettings("showPatchNotes", true, getApplicationContext());
            dataManager.saveToJSONSettings("disablePatchNotesTemporary", true, getApplicationContext());
            dataManager.saveToJSONSettings("settingReleaseNotesSwitch", true, getApplicationContext());
        }
        setContentView(R.layout.calculatorui);
        try {
            dataManager.loadNumbers();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        checkDarkmodeSetting();
        showOrHideScienceButtonState();
        setUpListeners();
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
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    /**
     * Checks the dark mode setting.
     * It switches the display mode based on the current night mode.
     */
    public void checkDarkmodeSetting() {
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    /**
     * This method is used to switch the display mode of the application based on the user's selected setting.
     * It first initializes the UI elements and gets the current night mode status and the user's selected setting.
     * If a setting is selected, it switches the display mode based on the selected setting.
     * If no setting is selected, it saves "System" as the selected setting and calls itself again.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchDisplayMode(int currentNightMode) {
        // Global variables
        TextView historyButton = findViewById(R.id.history_button);
        TextView settingsButton = findViewById(R.id.settings_button);
        TextView convertButton = findViewById(R.id.convert_button);
        TextView scienceButton = findViewById(R.id.scientificButton);
        //TextView helpButton = findViewById(R.id.help_button);
        Button shiftButton = findViewById(R.id.shift);
        int newColorBTNBackgroundAccent = 0;
        int newColorBTNForegroundAccent = 0;

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
                            //if (helpButton != null) {
                            //    helpButton.setForeground(getDrawable(R.drawable.help_light));
                            //}

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
                                //if (helpButton != null) {
                                //    helpButton.setForeground(getDrawable(R.drawable.help_true_darkmode));
                                //}
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
                            //if (helpButton != null) {
                            //    helpButton.setForeground(getDrawable(R.drawable.help));
                            //}
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
                    //if (helpButton != null) {
                    //    helpButton.setForeground(getDrawable(R.drawable.help));
                    //}
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
                    //if (helpButton != null) {
                    //    helpButton.setForeground(getDrawable(R.drawable.help_light));
                    //}

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
                        //if (helpButton != null) {
                        //    helpButton.setForeground(getDrawable(R.drawable.help_true_darkmode));
                        //}
                    }
                    break;
            }

            // Updating UI elements
            changeTextViewColors(findViewById(R.id.patchnotesUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeButtonColors(findViewById(R.id.patchnotesUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeTextViewColors(findViewById(R.id.calculatorUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeButtonColors(findViewById(R.id.calculatorUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
        } else {
            dataManager.saveToJSONSettings("selectedSpinnerSetting", "System", getApplicationContext());
            switchDisplayMode(currentNightMode);
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
     * It checks if "disablePatchNotesTemporary" is true in the JSON file, and if so, it saves "disablePatchNotesTemporary" as false in the JSON file.
     * It then calls the finish() method to close the activity.
     */
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (dataManager.getJSONSettingsData("disablePatchNotesTemporary", getApplicationContext()).getString("value").equals("true")) {
                dataManager.saveToJSONSettings("disablePatchNotesTemporary", false, getApplicationContext());
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
        formatResultTextAfterType();
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
        boolean b = Integer.parseInt(num) >= 2 && Integer.parseInt(num) <= 9;
        setLastNumber(getResultText());
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("true") && b) {
                    dataManager.saveToJSONSettings("logX", "false", getApplicationContext());
                    String small_number = convertToSmallNumber(Integer.parseInt(num));
                    addCalculateTextWithoutSpace(small_number);
                    addCalculateTextWithoutSpace("(");
                } else {
                    addCalculateTextWithoutSpace(num);
                }
                final String calculate_text = balanceParentheses(getCalculateText());
                if(!isInvalidInput(calculate_text)) {
                    setResultText(CalculatorEngine.calculate(balanceParentheses(calculate_text)));
                } else {
                    setResultText("0");
                }
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    if (dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value").equals("true")) {
                        addResultText(num);
                        dataManager.saveToJSONSettings("eNotation", "false", getApplicationContext());
                    } else {
                        String calculateText = getCalculateText();
                        if (isInvalidInput(getResultText()) || isInvalidInput(calculateText)) {
                            setCalculateText("");
                            setRemoveValue(true);
                        }
                        if (getRemoveValue()) {
                            if (isInvalidInput(calculateText) || calculateText.contains("=")) {
                                setCalculateText("");
                            }
                            setResultText("0");
                            setRemoveValue(false);
                        }

                        if (getResultText().replace(".", "").replace(",", "").length() < 18) {
                            if (getResultText().equals("0")) {
                                setResultText(num);
                            } else if (getResultText().equals("-0")) {
                                // Replace "0" with the new digit, and add the negative sign back
                                setResultText("-" + num);
                            } else {
                                // Add the new digit, and add the negative sign back if needed
                                addResultText(num);
                            }
                        }
                    }
                } else {
                    if (b) {
                        dataManager.saveToJSONSettings("logX", "false", getApplicationContext());
                        String small_number = convertToSmallNumber(Integer.parseInt(num));
                        addCalculateTextWithoutSpace(small_number);
                        addCalculateTextWithoutSpace("(");
                        setRotateOperator(false);
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        setCalculateText(replacePiWithSymbolInString(getCalculateText()));
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
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
        formatResultTextAfterType();

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

        assert clipData != null;
        ClipData.Item item = clipData.getItemAt(0);
        String clipText = (String) item.getText();

        if (clipData.getItemCount() == 0) {
            // Handle the case where clipboard data is null or empty
            showToastShort(getString(R.string.clipboardIsEmpty), getApplicationContext());

            return;
        }

        String scientificNotationPattern = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";
        String mathTaskPattern = "[-+*/%^()0-9.eE\\s]+";

        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(!clipText.isEmpty()) {
                    addCalculateTextWithoutSpace(clipText.replace(" ", ""));
                    showToastShort(getString(R.string.pastedClipboard), getApplicationContext());
                }
            } else {
                if (clipText.matches(scientificNotationPattern) && !clipText.matches(mathTaskPattern)) {
                    processScientificNotation(clipText);
                } else if ((clipText.matches(mathTaskPattern) && !clipText.matches(scientificNotationPattern)) || clipText.matches("[-+]?[0-9]+")) {
                    processMathTaskOrNumber(clipText);
                } else {
                    showToastLong(getString(R.string.invalidInput), getApplicationContext());
                }

                if (!getCalculateText().isEmpty()) {
                    setRotateOperator(!isOperator(String.valueOf(getCalculateText().charAt(getCalculateText().length() - 1))));
                }
            }

            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
        layoutParams.weight = 1;
        findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);

        scrollToStart(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));
    }

    /**
     * This method processes scientific notation in the provided text.
     * It first calculates the result of the provided text using the CalculatorActivity's calculate() method.
     * Then, it sets the result text to the original input text, formats it, and saves it as new_text.
     * After that, it resets the result text to the calculated result.
     * If the calculation text is empty, it sets it to new_text; otherwise, it appends new_text to the existing calculation text.
     * @param text The text containing scientific notation to be processed.
     */
    private void processScientificNotation(String text) {
        final String resultText = CalculatorEngine.calculate(balanceParentheses(text));
        setResultText(text);
        formatResultTextAfterType();
        final String new_text = getResultText();
        setResultText(resultText);

        if (getCalculateText().isEmpty()) {
            setCalculateText(new_text);
        } else {
            addCalculateText(new_text);
        }
    }

    /**
     * This method processes mathematical tasks or numbers in the provided text.
     * If the text does not match the pattern of a mathematical task (e.g., "+", "-", "*", "/", "^") or a number,
     * it adds the text to the calculation text if it's not empty; otherwise, it sets the calculation text to the provided text.
     * If the text represents a number, it sets the result text to the provided text.
     * @param text The text containing a mathematical task or number to be processed.
     */
    private void processMathTaskOrNumber(String text) {
        if (!text.matches("[-+]?[0-9]+")) {
            if (getCalculateText().isEmpty()) {
                setCalculateText(text);
            } else {
                addCalculateText(text);
            }
        } else {
            setResultText(text);
        }
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
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    addCalculateTextWithoutSpace(getResultText() + new_op);
                    return;
                }
                addCalculateTextWithoutSpace(new_op);
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext() ).getString("value").equals("false")) {
                    setLastOp(op);

                    // Check if there is one operator at the end

                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("true") && getResultText().length() > 1 && (new_op.equals("+") || new_op.equals("-"))) {
                        int lastIndex = getResultText().length() - 1;
                        char lastChar = getResultText().charAt(lastIndex);

                        // Check if the last character isn't an operator
                        if (!isOperator(String.valueOf(lastChar))) {
                            setResultText(getResultText() + new_op);
                            return;
                        } else {
                            setResultText(getResultText() + "0");
                            dataManager.saveToJSONSettings("eNotation", false, getApplicationContext());
                            setRemoveValue(true);
                        }
                    }

                    if (mode.equals("false")) {

                        if(!getRotateOperator()) {
                            setLastNumber(getResultText());
                            if (getCalculateText().contains("=")) {
                                setCalculateText(getResultText() + " " + new_op);
                            } else {
                                addCalculateText(getResultText() + " " + new_op);
                            }
                            setRemoveValue(true);
                        } else {
                            addCalculateText(new_op);
                            setRemoveValue(true);
                        }
                        setRotateOperator(false);
                        if(findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
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
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();

        switch (e) {
            case "⌫":
                handleBackspaceAction();
                scrollToEnd(findViewById(R.id.calculate_scrollview));
                break;
            case "C":
                setResultText("0");
                setCalculateText("");
                setRotateOperator(false);
                dataManager.saveToJSONSettings("logX", "false", getApplicationContext());

                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
                break;
            case "CE":
                setResultText("0");

                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
                break;
        }
    }

    public static String removeOperatorsFromRight(String input) {
        for (int i = input.length() - 1; i >= 0; i--) {
            char currentChar = input.charAt(i);
            String currentToken = String.valueOf(currentChar);

            if (isStandardOperator(currentToken)) {
                input = input.substring(0, input.length() - 1);
            } else {
                break;
            }
        }

        return input;
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
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(!getCalculateText().isEmpty()) {
                    if(getCalculateText().endsWith("(")) {
                        setCalculateText(removeOperators(getCalculateText().substring(0, getCalculateText().length() - 1)));
                    } else {
                        if(getCalculateText().length() > 1) {
                            setCalculateText(getCalculateText().substring(0, getCalculateText().length() - 1));

                            if(Character.isDigit(getCalculateText().charAt(getCalculateText().length() - 1))) {
                                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                                if(isInvalidInput(getResultText())) {
                                    setResultText(CalculatorEngine.calculate(balanceParentheses(removeOperatorsFromRight(getCalculateText()))));
                                }
                            }
                        } else {
                            setCalculateText("");
                        }
                    }
                }

                if(getCalculateText().isEmpty()) {
                    setResultText("0");
                } else {
                    final String oldText = getResultText();
                    setResultText(CalculatorEngine.calculate(balanceParentheses(removeOperatorsFromRight(getCalculateText()))));
                    if(isInvalidInput(getResultText())) {
                        setResultText(oldText);
                    }
                }
            } else {
                String resultText = getResultText();
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    if (!isInvalidInput(getResultText())) {
                        if (!resultText.equals("0") && !resultText.isEmpty()) {
                            setResultText(resultText.substring(0, resultText.length() - 1));
                            if (resultText.equals("-")) {
                                setResultText(resultText + "0");
                            }
                        } else {
                            setResultText("0");
                        }
                    } else {
                        setCalculateText("");
                        setResultText("0");
                    }
                    if (getResultText().isEmpty() || getResultText().equals("")) {
                        setResultText("0");
                    }
                    dataManager.saveNumbers(getApplicationContext());
                } else {
                    setResultText(getResultText().replace("e+", "").replace("e-", "").replace("e", ""));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
        adjustTextSize();
    }

    /**
     * This method removes operators from the end of the input string.
     * It iterates through the input string from the end and checks if any of the predefined operators match the substring.
     * If an operator is found, it subtracts its length from the index and continues the iteration.
     * If no operator is found, it breaks the loop.
     * Finally, it returns the substring of the input string up to the index where the last operator was found.
     * @param input The input string from which operators are to be removed.
     * @return The input string with operators removed from the end.
     */
    public static String removeOperators(String input) {
        String[] operators = {"³√", "ln", "log₂", "log₃", "log₄", "log₅", "log₆", "log₇", "log₈",
                "log₉", "tanh⁻¹", "cosh⁻¹", "sinh⁻¹", "tan⁻¹", "cos⁻¹", "sin⁻¹", "tanh", "cosh",
                "sinh", "tan", "cos", "sin", "√"};

        StringBuilder result = new StringBuilder();
        int i = input.length() - 1;

        while (i >= 0) {
            boolean foundOperator = false;

            for (String operator : operators) {
                if (i - operator.length() + 1 >= 0 && input.substring(i - operator.length() + 1, i + 1).equals(operator)) {
                    i -= operator.length();
                    foundOperator = true;
                    break;
                }
            }

            if (!foundOperator) {
                break;
            }
        }

        result.append(input.substring(0, i + 1));

        return result.toString();
    }

    /**
     * This method is called when the negate button is clicked.
     * It toggles the sign of the result text.
     * If the first character of the result text is "-", it removes the "-" from the result text.
     * If the first character of the result text is not "-", it adds "-" to the beginning of the result text.
     */
    public void NegativAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                addCalculateTextWithoutSpace("-");
            } else {
                final char firstchar = getResultText().charAt(0);
                if (String.valueOf(firstchar).equals("-")) {
                    setResultText(getResultText().substring(1));
                } else {
                    setResultText("-" + getResultText());
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * This method is called when the comma button is clicked.
     * It adds a comma to the result text if it does not already contain a comma.
     */
    public void CommaAction() {
        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    addCalculateTextWithoutSpace("0,");
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                    return;
                }
                addCalculateTextWithoutSpace(",");
            } else {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    if (!getResultText().contains(",")) {
                        addResultText(",");
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * This method takes a mathematical expression as input, checks if parentheses are
     * balanced, and adds missing parentheses as needed to balance the expression.
     *
     * @param input The input mathematical expression.
     * @return The balanced mathematical expression.
     */
    public String balanceParentheses(String input) {
        // Count the number of opening and closing parentheses
        int openCount = 0;
        int closeCount = 0;
        final String oldInput = input;

        input = input.replace(" ", "").replace("=", "");

        if(input.isEmpty()) {
            return "0";
        }

        for (char ch : input.toCharArray()) {
            if (ch == '(') {
                openCount++;
            } else if (ch == ')') {
                closeCount++;
            }
        }

        // Add missing opening parentheses
        StringBuilder inputBuilder = new StringBuilder(input);
        while (openCount < closeCount) {
            try {
                if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                    inputBuilder.insert(0, "(");
                } else {
                    inputBuilder.insert(0, "( ");
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            openCount++;
        }

        // Add missing closing parentheses
        while (closeCount < openCount) {
            try {
                if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                    inputBuilder.append(")");
                } else {
                    inputBuilder.append(" )");
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            closeCount++;
        }

        input = inputBuilder.toString();

        //if(String.valueOf(input.charAt(0)).equals("(") && String.valueOf(input.charAt(input.length() - 1)).equals(")")) {
        //    input = input.substring(1, input.length() - 1);
        //}

        if(oldInput.contains("=")) {
            return input + " =";
        } else {
            return input;
        }
    }

    private String addSpaceToOperators(String string) {
        final String symbols = "+-*/÷×√^π½⅓¼=";
        StringBuilder stringBuilder = new StringBuilder();

        if (!string.isEmpty()) {
            string = string.replace(" ", "");
            for (int i = 0; i < string.length(); i++) {
                if(i + 1 < string.length()) {
                    if(String.valueOf(string.charAt(i)).equals("³") && String.valueOf(string.charAt(i + 1)).equals("√")) {
                        stringBuilder.append(string.charAt(i));
                        continue;
                    } else if(String.valueOf(string.charAt(i)).equals("√") && String.valueOf(string.charAt(i + 1)).equals("(")) {
                        stringBuilder.append(string.charAt(i));
                        continue;
                    }
                }

                if (symbols.indexOf(string.charAt(i)) != -1) {
                    if(i != 0 && !String.valueOf(string.charAt(i - 1)).equals("³")) {
                        stringBuilder.append(" ");
                    }
                    stringBuilder.append(string.charAt(i));
                    if (i + 1 < string.length() && isOperator(String.valueOf(symbols.indexOf(string.charAt(i + 1))))) {
                        stringBuilder.append(" ");
                    }
                } else {
                    stringBuilder.append(string.charAt(i));
                }
            }

            if (String.valueOf(stringBuilder.charAt(stringBuilder.length() - 1)).equals(" ")) {
                stringBuilder = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
        }

        if (!String.valueOf(stringBuilder).isEmpty() && String.valueOf(stringBuilder.charAt(0)).equals(" ")) {
            stringBuilder.deleteCharAt(0);
        }
        return stringBuilder.toString();
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
        addSpaceToOperators(getCalculateText());

        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if(dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    return;
                }

                if(getCalculateText().isEmpty()) {
                    setResultText("0");
                } else {
                    setCalculateText(balanceParentheses(getCalculateText()));
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                }
            } else {
                if(dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    // Replace special characters for proper calculation
                    String calcText = getCalculateText().replace("*", "×").replace("/", "÷");

                    // Check if there is one operator at the end
                    if (getResultText().length() > 1) {
                        int lastIndex = getResultText().length() - 1;
                        char lastChar = getResultText().charAt(lastIndex);

                        // Check if the last character isn't an operator
                        if (isOperator(String.valueOf(lastChar))) {
                            setResultText(getResultText() + "0");
                            dataManager.saveToJSONSettings("eNotation", false, getApplicationContext());
                            setRemoveValue(false);
                            formatResultTextAfterType();
                            return;
                        }
                    }

                    // Check for valid input before performing calculations
                    if (!isInvalidInput(getResultText()) && !isInvalidInput(getCalculateText())) {
                        // Handle calculation based on the rotate operator flag
                        if (getRotateOperator()) {
                            if (!calcText.contains("=")) {
                                // Handle calculation when equals sign is not present
                                setLastNumber(getResultText());
                                setCalculateText(addSpaceToOperators(balanceParentheses(getCalculateText() + "=")));
                                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                            } else {
                                // Handle calculation when equals sign is present
                                if (!getCalculateText().replace("=", "").replace(" ", "").matches("^(sin|cos|tan)\\(.*\\)$")) {
                                    if (!getLastOp().isEmpty() && !getLastOp().equals("√")) {
                                        setCalculateText(addSpaceToOperators(getResultText() + getLastOp() + getLastNumber() + "="));
                                    } else {
                                        setCalculateText(addSpaceToOperators(getResultText() + "="));
                                    }
                                    setCalculateText(addSpaceToOperators(balanceParentheses(getCalculateText())));
                                    setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + " " + getLastOp() + " " + getLastNumber())));
                                } else {
                                    setCalculateText(balanceParentheses(getCalculateText()));
                                    setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                                }
                            }
                        } else {
                            if (!calcText.contains("=")) {
                                // Handle calculation when equals sign is not present
                                setCalculateText(addSpaceToOperators(balanceParentheses(getCalculateText() + getResultText() + "=")));
                                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                            } else {
                                // Handle calculation when equals sign is present
                                if (!getCalculateText().replace("=", "").replace(" ", "").matches("^(sin|cos|tan)\\(.*\\)$")) {
                                    int nonNumericIndex = 0;
                                    while (nonNumericIndex < getCalculateText().length() && Character.isDigit(getCalculateText().charAt(nonNumericIndex))) {
                                        nonNumericIndex++;
                                    }

                                    setCalculateText(getResultText() + getCalculateText().substring(nonNumericIndex));
                                } else {
                                    setCalculateText(addSpaceToOperators(balanceParentheses(getCalculateText())));
                                }

                                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                            }
                        }

                        // Replace special characters back for displaying
                        setCalculateText(addSpaceToOperators(getCalculateText().replace("*", "×").replace("/", "÷")));

                        // Save history, update UI, and set removeValue flag
                        dataManager.saveNumbers(getApplicationContext());
                    } else {
                        // Handle invalid input by resetting result text and calculate text
                        setResultText("0");
                        setCalculateText("");
                    }

                    // Reset rotate operator flag, format result text, adjust text size, and scroll to bottom if necessary
                    setRotateOperator(false);
                    setRemoveValue(true);
                    setCalculateText(addSpaceToOperators(replacePiWithSymbolInString(getCalculateText())));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
        adjustTextSize();

        if(!isNumber(getCalculateText()) && !getCalculateText().replace("=", "").replace(" ", "").equals("π")
            && !isInvalidInput(getResultText())) {
            addToHistoryAfterCalculate(balanceParentheses(getCalculateText()));
        }

        try {
            if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht") &&
                !isInvalidInput(getResultText())) {
                dataManager.saveToJSONSettings("pressedCalculate", true, getApplicationContext());
                setCalculateText("");

                if(dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                    scrollToEnd(findViewById(R.id.calculate_scrollview));
                    scrollToStart(findViewById(R.id.result_scrollview));
                } else {
                    scrollToBottom(findViewById(R.id.calculate_scrollview));
                    scrollToBottom(findViewById(R.id.result_scrollview));
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1.5F;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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

    private void addToHistoryAfterCalculate(String input) {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd. MMMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());

        input = fixExpression(input.replace("=", "").replace(" ", ""));

        // Code snippet to save calculation to history
        final Context context1 = getApplicationContext();
        String finalInput = input;
        new Thread(() -> runOnUiThread(() -> {
            final String value;
            try {
                value = dataManager.getHistoryData("historyTextViewNumber", context1).getString("value");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            final int old_value;
            try {
                old_value = Integer.parseInt(dataManager.getHistoryData("historyTextViewNumber", context1).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            final int new_value = old_value + 1;

            dataManager.saveToHistory("historyTextViewNumber", Integer.toString(new_value), context1);
            String calculate_text = finalInput;

            if (calculate_text.isEmpty()) {
                calculate_text = "0";
            }
            if (!calculate_text.contains("=")) {
                calculate_text = calculate_text + "=";
            }

            dataManager.saveToHistory(String.valueOf(old_value + 1), formattedDate, "",
                    addSpaceToOperators(balanceParentheses(calculate_text) + getResultText()), context1);
        })).start();
    }


    private void addToHistory(String input) {
        if(!isInvalidInput(input)) {
            input = fixExpression(input.replace(" ", "").replace("=", ""));
        }

        // Code snippet to save calculation to history
        String finalInput = input;
        new Thread(() -> runOnUiThread(() -> {
            try {
                final int old_value;
                old_value = Integer.parseInt(dataManager.getHistoryData("historyTextViewNumber", getApplicationContext()).getString("value"));
                final int new_value = old_value + 1;

                dataManager.saveToHistory("historyTextViewNumber", Integer.toString(new_value), getApplicationContext());
                String calculate_text = finalInput;

                if(calculate_text.isEmpty()) {
                    calculate_text = "0";
                }

                if(!calculate_text.contains("=")) {
                    calculate_text = calculate_text + "=";
                }
                dataManager.saveToJSONSettings(String.valueOf(old_value + 1), addSpaceToOperators(balanceParentheses(calculate_text)), getApplicationContext());
                //Log.i("Calculate", "historyTextViewNumber: " + dataManager.getHistoryData("historyTextViewNumber", getApplicationContext()).getString("value"));
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
     * Formats the result text after a mathematical operation has been performed.
     * This method handles various formats, including scientific notation and decimal formatting.
     */
    public void formatResultTextAfterType() {
        // Get the result text
        String text = getResultText();

        // Check if result text is not null
        if (text != null && !isInvalidInput(text)) {

            // Check if the number is negative
            boolean isNegative = text.startsWith("-");
            if (isNegative) {
                // If negative, remove the negative sign for further processing
                text = text.substring(1);
            }

            // Check for scientific notation
            if (text.toLowerCase().matches(".*[eE].*")) {
                try {
                    // Convert scientific notation to BigDecimal with increased precision
                    BigDecimal bigDecimalResult = new BigDecimal(text.replace(".", "").replace(",", "."), MathContext.DECIMAL128);
                    String formattedNumber = bigDecimalResult.toPlainString();
                    formattedNumber = formattedNumber.replace(".", ",");

                    // Extract exponent part and shift decimal point accordingly
                    String[] parts = formattedNumber.split("[eE]");
                    if (parts.length == 2) {
                        int exponent = Integer.parseInt(parts[1]);
                        String[] numberParts = parts[0].split(",");
                        if (exponent < 0) {
                            // Shift decimal point to the left, allowing up to 9 positions
                            int shiftIndex = Math.min(numberParts[0].length() + exponent, 9);
                            formattedNumber = numberParts[0].substring(0, shiftIndex) + "," +
                                    numberParts[0].substring(shiftIndex) + numberParts[1] + "e" + exponent;
                        } else {
                            // Shift decimal point to the right
                            int shiftIndex = Math.min(numberParts[0].length() + exponent, numberParts[0].length());
                            formattedNumber = numberParts[0].substring(0, shiftIndex) + "," +
                                    numberParts[0].substring(shiftIndex) + numberParts[1];
                        }
                    }

                    // Add negative sign if necessary and set the result text
                    if (isNegative) {
                        formattedNumber = "-" + formattedNumber;
                    }
                    setResultText(formattedNumber.replace("E", "e"));

                    // Adjust text size and recursively call the method
                    adjustTextSize();
                    formatResultTextAfterType();
                    return;
                } catch (NumberFormatException e) {
                    // Handle invalid number format in scientific notation
                    System.out.println("Invalid number format: " + text);
                }
            }

            // Handle non-scientific notation
            int index = text.indexOf(',');
            String result;
            String result2;
            if (index != -1) {
                // Split the text into integral and fractional parts
                result = text.substring(0, index).replace(".", "");
                result2 = text.substring(index);
            } else {
                result = text.replace(".", "");
                result2 = "";
            }

            // Check for invalid input
            if (!isInvalidInput(getResultText())) {
                // Format the integral part using DecimalFormat
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                Locale locale = Locale.getDefault();

                // default: German, French, Spanish
                symbols.setDecimalSeparator(',');
                symbols.setGroupingSeparator('.');

                //if (locale.getLanguage().equals("en")) {
                //    symbols.setDecimalSeparator('.');
                //    symbols.setGroupingSeparator(',');
                //}

                DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
                //decimalFormat.setGroupingUsed(false);
                try {
                    BigDecimal bigDecimalResult1 = new BigDecimal(result, MathContext.DECIMAL128);
                    String formattedNumber1 = decimalFormat.format(bigDecimalResult1);

                    // Set the result text with formatted numbers
                    setResultText((isNegative ? "-" : "") + formattedNumber1 + result2);
                } catch (NumberFormatException e) {
                    // Handle invalid number format in the integral part
                    System.out.println("Invalid number format: " + result);
                }
            } else if (getIsNotation()) {
                // Reset scientific notation flag if needed
                setIsNotation(false);
            }
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

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
            layoutParams.weight = 1.5F;
            findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);

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
        finish();
    }

    /**
     * The following methods are simple getter and setter methods for various properties.
     */
    private void checkCalculateText() {
        if(getCalculateText().contains("=")) {
            setCalculateText("");
            if(isInvalidInput(getResultText())) {
                setResultText("0");
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
    public void setRotateOperator(final boolean rotate) {
        dataManager.saveToJSONSettings("rotate_op", rotate, getApplicationContext());
        /*
        try {
            Log.i("setRotateOperator", "rotate_op: '" + dataManager.getJSONSettingsData("rotate_op", getApplicationContext()).getString("value") + "'");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        */
    }
    public boolean getRotateOperator() {
        try {
            //Log.i("setRotateOperator", "rotate_op: '" + dataManager.getJSONSettingsData("rotate_op", getApplicationContext()) + "'");
            return Boolean.parseBoolean(dataManager.getJSONSettingsData("rotate_op", getApplicationContext()).getString("value"));
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