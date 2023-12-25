package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.CalculatorActivity.setMainActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MainActivity
 * @author Max Lemberg
 * @version 1.2.8
 * @date 22.12.2023
 */

public class MainActivity extends AppCompatActivity {
    /**
     * Flag to control the rotation of operators after a root operation. If true, operator will swap with the number.
     */
    private boolean rotateOperatorAfterRoot = false;

    /**
     * The context of the current object. Useful for accessing resources, launching new activities, etc.
     */
    private Context context = this;

    /**
     * Stores the last number entered or calculated. Initialized to "0" to handle the case where no number has been entered yet.
     */
    private String last_number = "0";

    /**
     * Stores the last operator used. Initialized to "+" as it is the default operator.
     */
    private String last_op = "+";

    /**
     * Instance of DataManager to handle data-related tasks such as saving and retrieving data.
     */
    private DataManager dataManager;

    /**
     * Instance of SharedPreferences for storing and retrieving small amounts of primitive data as key-value pairs.
     */
    SharedPreferences prefs = null;

    /**
     * Flag to control the notation. If true, a certain notation (e.g., scientific notation) will be used.
     */
    private boolean isnotation = false;

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculatorui);
        context = this;
        setMainActivity(this);

        dataManager = new DataManager(this);
        dataManager.createJSON(getApplicationContext());
        dataManager.checkAndCreateFile();

        // If it's the first run of the application
        prefs = getSharedPreferences("com.mlprograms.RechenMax", MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            dataManager.saveToJSON("showPatchNotes", true, getApplicationContext());
            setContentView(R.layout.patchnotes);
            checkDarkmodeSetting();
            prefs.edit().putBoolean("firstrun", false).apply();
        }

        Log.i("MainActivity", "showPatchNotes=" + dataManager.readFromJSON("showPatchNotes", getApplicationContext()));
        Log.i("MainActivity", "disablePatchNotesTemporary=" + dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()));

        final String showPatNot = dataManager.readFromJSON("showPatchNotes", getApplicationContext());
        final String disablePatNotTemp = dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext());

        if (showPatNot != null && disablePatNotTemp != null) {
            if (showPatNot.equals("true") && disablePatNotTemp.equals("false")) {
                setContentView(R.layout.patchnotes);
                checkDarkmodeSetting();
            }
        }
        dataManager.loadNumbers();
        // dataManager.saveNumbers(getApplicationContext());
        setUpListeners();
        checkScienceButtonState();
        checkDarkmodeSetting();
        formatResultTextAfterType();
        adjustTextSize();

        // scroll down in calculate_label
        scrollToBottom(findViewById(R.id.calculate_scrollview));
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
    private void scrollToBottom(final ScrollView scrollView) {
        // Executes the scrolling to the bottom of the ScrollView in a Runnable.
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    /**
     * Sets up the listeners for each button in the application
     */
    private void setUpListeners() {
        setButtonListener(R.id.history_button, this::switchToHistoryAction);
        setButtonListener(R.id.settings_button, this::switchToSettingsAction);

        setButtonListener(R.id.okay_button, this::patchNotesOkayButtonAction);

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
        setScienceButtonListener();
    }

    /**
     * Sets up the listener for the scientific buttons
     */
    private void setScienceButtonListener() {
        Button toggleButton = findViewById(R.id.scientificButton);
        if(toggleButton != null) {
            toggleButton.setOnClickListener(v -> {
                LinearLayout buttonRow = findViewById(R.id.scientificRow);
                final String data = dataManager.readFromJSON("showScienceRow", getApplicationContext());
                if(buttonRow != null && data != null) {
                    if (data.equals("true")) {
                        buttonRow.setVisibility(View.GONE);
                        dataManager.saveToJSON("showScienceRow", false, getApplicationContext());
                    } else if (data.equals("false")) {
                        buttonRow.setVisibility(View.VISIBLE);
                        dataManager.saveToJSON("showScienceRow", true, getApplicationContext());
                    }
                }
            });
        }
    }

    /**
     * Checks the state of the science button
     */
    public void checkScienceButtonState() {
        LinearLayout buttonRow = findViewById(R.id.scientificRow);
        final String data = dataManager.readFromJSON("showScienceRow", getApplicationContext());
        if(buttonRow != null && data != null) {
            if(data.equals("true")) {
                buttonRow.setVisibility(View.VISIBLE);
            } else {
                buttonRow.setVisibility(View.GONE);
            }
        }
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
            });
        }
    }

    /**
     * Sets up the listener for all number buttons
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     * @param action The action which belongs to the button.
     */
    private void setButtonListener(int buttonId, Runnable action) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> {
                action.run();
                dataManager.saveNumbers(getApplicationContext());
            });
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
            });
        }
    }

    /**
     * This method adds an opening parenthesis to the calculation text.
     */
    private void parenthesisOnAction() {
        addCalculateText("(");
        scrollToBottom(findViewById(R.id.calculate_scrollview));
    }

    /**
     * This method adds a closing parenthesis to the calculation text.
     * If the last operation was a square root, it adds a closing parenthesis.
     * Otherwise, it adds the result text and a closing parenthesis.
     */
    private void parenthesisOffAction() {
        Pattern pattern = Pattern.compile("√\\(\\d+\\)$");
        Matcher matcher = pattern.matcher(getCalculateText());
        if (matcher.find()) {
            addCalculateText(")");
        } else {
            if(!getRotateOperator()) {
                addCalculateText(getResultText() + " )");
            } else {
                addCalculateText(")");
            }
        }
        setRotateOperator(true);
        scrollToBottom(findViewById(R.id.calculate_scrollview));
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
        final String calc_text = getCalculateText().replace(" ", "");

        if (calc_text.isEmpty()) {
            addCalculateText(getResultText() + "!");
            setRotateOperator(true);
        } else {
            String lastchar = String.valueOf(calc_text.charAt(calc_text.length() - 1));
            if (isValidOperator(lastchar.replace("!", "")) || lastchar.equals("(")) {
                addCalculateText(getResultText() + "!");
                setRotateOperator(true);
            } else {
                addCalculateText(getLastOp() + " " + getResultText() + "!");
                setRotateOperator(true);
            }
        }
        scrollToBottom(findViewById(R.id.calculate_scrollview));
    }

    /**
     * The 'isValidOperator' method evaluates whether the input string is a recognized operator.
     *
     * @param op The string that is to be evaluated as an operator.
     * @return boolean This returns 'true' if the input string matches one of the following operators: "+", "-", "×", "÷", "^", "!".
     *                 It returns 'false' if the input string does not match any of the recognized operators.
     */
    private boolean isValidOperator(final String op) {
        return  String.valueOf(op).equals("+") ||
                String.valueOf(op).equals("-") ||
                String.valueOf(op).equals("×") ||
                String.valueOf(op).equals("÷") ||
                String.valueOf(op).equals("^") ||
                String.valueOf(op).equals("!");
    }

    /**
     * This method adds a power operation to the calculation text.
     * Depending on the state of the rotate operator flag, it handles the power operation differently.
     */
    private void powerAction() {
        setLastOp("^");
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
            if (getCalculateText().replace(" ", "").charAt(getCalculateText().replace(" ", "").length() - 1) == ')') {
                addCalculateText("^");
            } else {
                addCalculateText("^");
            }
            setRemoveValue(true);
            setRotateOperator(false);
        }
        scrollToBottom(findViewById(R.id.calculate_scrollview));
    }

    /**
     * This method adds a root operation to the calculation text.
     * Depending on the state of the rotate operator flag, it handles the root operation differently.
     */
    private void rootAction() {
        if(!getRotateOperator()) {
            addCalculateText("√(");
        } else {
            addCalculateText(getLastOp() + " √(");
        }
        setRemoveValue(true);
        //setRotateOperator(true);
        scrollToBottom(findViewById(R.id.calculate_scrollview));
    }

    /**
     * Handles the action when the okay button in the patch notes is clicked.
     * Depending on whether the checkbox is checked or not, it saves different values to JSON.
     * Then it sets the content view, loads numbers, checks dark mode setting, checks science button state, and sets up listeners.
     */
    public void patchNotesOkayButtonAction() {
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CheckBox checkBox = findViewById(R.id.checkBox);
        if (checkBox.isChecked()) {
            dataManager.saveToJSON("showPatchNotes", false, getApplicationContext());
            dataManager.saveToJSON("disablePatchNotesTemporary", true, getApplicationContext());
            dataManager.saveToJSON("settingReleaseNotesSwitch", false, getApplicationContext());
        } else {
            dataManager.saveToJSON("showPatchNotes", true, getApplicationContext());
            dataManager.saveToJSON("disablePatchNotesTemporary", true, getApplicationContext());
            dataManager.saveToJSON("settingReleaseNotesSwitch", true, getApplicationContext());
        }
        setContentView(R.layout.calculatorui);
        dataManager.loadNumbers();
        checkDarkmodeSetting();
        checkScienceButtonState();
        setUpListeners();
    }

    /**
     * Switches to the settings activity.
     * It creates a new SettingsActivity, sets the main activity context, and starts the activity.
     */
    public void switchToSettingsAction() {
        SettingsActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the history activity.
     * It creates a new HistoryActivity, sets the main activity context, and starts the activity.
     */
    private void switchToHistoryAction() {
        HistoryActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, HistoryActivity.class);
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
        TextView scienceButton = findViewById(R.id.scientificButton);
        int newColorBTNBackgroundAccent = 0;
        int newColorBTNForegroundAccent = 0;

        // Retrieving theme setting
        String selectedSetting = getSelectedSetting();
        // Updating UI elements
        final String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext());
        if (selectedSetting != null) {
            switch (selectedSetting) {
                case "Systemstandard":
                    switch (currentNightMode) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            if (historyButton != null) {
                                historyButton.setForeground(getDrawable(R.drawable.baseline_history_24_light));
                            }
                            if (settingsButton != null) {
                                settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24_light));
                            }
                            if (scienceButton != null) {
                                scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24_light));
                            }

                            if (trueDarkMode != null && trueDarkMode.equals("true")) {
                                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                                if (scienceButton != null) {
                                    scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24_true_darkmode));
                                }
                                if (historyButton != null) {
                                    historyButton.setForeground(getDrawable(R.drawable.baseline_history_24_true_darkmode));
                                }
                                if (settingsButton != null) {
                                    settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24_true_darkmode));
                                }
                            } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);
                            }
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                            if (historyButton != null) {
                                historyButton.setForeground(getDrawable(R.drawable.baseline_history_24));
                            }
                            if (settingsButton != null) {
                                settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24));
                            }
                            if (scienceButton != null) {
                                scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24));
                            }
                            break;
                    }
                    break;
                case "Tageslichtmodus":
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                    if (historyButton != null) {
                        historyButton.setForeground(getDrawable(R.drawable.baseline_history_24));
                    }
                    if (settingsButton != null) {
                        settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24));
                    }
                    if (scienceButton != null) {
                        scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24));
                    }
                    break;
                case "Dunkelmodus":
                    dataManager = new DataManager(this);
                    if (historyButton != null) {
                        historyButton.setForeground(getDrawable(R.drawable.baseline_history_24_light));
                    }
                    if (settingsButton != null) {
                        settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24_light));
                    }
                    if (scienceButton != null) {
                        scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24_light));
                    }
                    if (trueDarkMode != null) {
                        if (trueDarkMode.equals("false")) {
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                        } else {
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);

                            if (scienceButton != null) {
                                scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24_true_darkmode));
                            }
                            if (historyButton != null) {
                                historyButton.setForeground(getDrawable(R.drawable.baseline_history_24_true_darkmode));
                            }
                            if (settingsButton != null) {
                                settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24_true_darkmode));
                            }
                        }
                    } else {
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                    }
                    break;
            }

            // Updating UI elements
            changeTextViewColors(findViewById(R.id.patchnotesUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeButtonColors(findViewById(R.id.patchnotesUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeTextViewColors(findViewById(R.id.calculatorUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeButtonColors(findViewById(R.id.calculatorUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
        } else {
            dataManager.saveToJSON("selectedSpinnerSetting", "System", getApplicationContext());
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
        final String setting = dataManager.readFromJSON("selectedSpinnerSetting", getApplicationContext());
        if(setting != null) {
            switch (setting) {
                case "System":
                    return "Systemstandard";
                case "Dark":
                    return "Dunkelmodus";
                case "Light":
                    return "Tageslichtmodus";
            }
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
        if (dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()).equals("true")) {
            dataManager.saveToJSON("disablePatchNotesTemporary", false, getApplicationContext());
        }
        finish();
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
        String resultText = getResultText();
        String calculateText = getCalculateText();
        if (isInvalidInput(resultText) || isInvalidInput(calculateText)) {
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
        if (resultText.replace(".", "").replace(",", "").replace("-", "").length() < 18) {
            if (resultText.equals("0") || resultText.equals("-0")) {
                setResultText(resultText.replace("0", num));
            } else {
                addResultText(num);
            }
        }
        formatResultTextAfterType();
        adjustTextSize();
    }

    /**
     * This method is called when a clipboard button is clicked.
     * It interacts with the system clipboard service to perform various clipboard operations.
     *
     * @param c The operation to be performed. This can be "MC" to clear the clipboard, "MR" to retrieve data from the clipboard, or "MS" to save data to the clipboard.
     */
    public void ClipboardAction(final String c) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        switch (c) {
            case "MC": {
                ClipData clipData = ClipData.newPlainText("", "");
                clipboardManager.setPrimaryClip(clipData);
                break;
            }
            case "MR":
                handleMRAction(clipboardManager);
                break;
            case "MS": {
                ClipData clipData = ClipData.newPlainText("", getResultText());
                clipboardManager.setPrimaryClip(clipData);
                break;
            }
        }
        adjustTextSize();
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
        String text = (String) item.getText();
        if (text != null && text.replace(".", "").matches("^-?\\d+([.,]\\d*)?([eE][+-]?\\d+)?$")) {
            setRemoveValue(false);
            String result = formatResultTextAfterCalculate(text);
            setResultText(result.toLowerCase());
        } else {
            setResultText("Ungültige Eingabe");
            setRemoveValue(true);
        }
        adjustTextSize();
    }

    /**
     * This method is called when an operation button is clicked.
     * It performs various actions based on the operation and updates the calculate text and result text accordingly.
     *
     * @param op The operation to be performed. This can be any mathematical operation like addition (+), subtraction (-), multiplication (*), or division (/).
     */
    public void OperationAction(final String op) {
        setLastOp(op);
        final String new_op = op.replace("*", "×").replace("/", "÷");
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
        scrollToBottom(findViewById(R.id.calculate_scrollview));
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
                break;
            case "C":
                setResultText("0");
                setCalculateText("");
                setRotateOperator(false);
                break;
            case "CE":
                setResultText("0");
                break;
        }
        adjustTextSize();
    }

    /**
     * This method is called when the backspace button is clicked.
     * It removes the last character from the result text.
     * If the result text is "Ungültige Eingabe", it resets the calculate text and result text.
     * If the result text is empty after removing the last character, it sets the result text to "0".
     * It then formats the result text and saves the numbers to the application context.
     */
    private void handleBackspaceAction() {
        String resultText = getResultText();
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
        formatResultTextAfterType();
        dataManager.saveNumbers(getApplicationContext());
    }

    /**
     * This method is called when the negate button is clicked.
     * It toggles the sign of the result text.
     * If the first character of the result text is "-", it removes the "-" from the result text.
     * If the first character of the result text is not "-", it adds "-" to the beginning of the result text.
     */
    public void NegativAction() {
        final char firstchar = getResultText().charAt(0);
        if (String.valueOf(firstchar).equals("-")) {
            setResultText(getResultText().substring(1));
        } else {
            setResultText("-" + getResultText());
        }
    }

    /**
     * This method is called when the comma button is clicked.
     * It adds a comma to the result text if it does not already contain a comma.
     */
    public void CommaAction() {
        if (!getResultText().contains(",")) {
            addResultText(",");
        }
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
     *
     */
    @SuppressLint("SetTextI18n")
    public void Calculate() {
        String calcText = getCalculateText().replace("*", "×").replace("/", "÷");
        TextView calculatelabel = findViewById(R.id.calculate_label);
        TextView resultlabel = findViewById(R.id.result_label);

        if(!isInvalidInput(getResultText()) && !isInvalidInput(getCalculateText())) {
            if(getRotateOperator()) {
                if (!calcText.contains("=")) {
                    setLastNumber(getResultText());
                    setCalculateText(calcText + " =");
                    setResultText(CalculatorActivity.calculate(getCalculateText().replace("×", "*").replace("÷", "/")));
                } else {
                    if (!getLastOp().isEmpty() && !getLastOp().equals("√")) {
                        setCalculateText(getResultText() + " " + getLastOp() + " " + getLastNumber() + " =");
                    } else {
                        setCalculateText(getResultText() + " =");
                    }
                    setResultText(CalculatorActivity.calculate(getResultText() + " " + getLastOp().replace("×", "*").replace("÷", "/") + " " + getLastNumber()));
                }

            } else {
                if (!calcText.contains("=")) {
                    setLastNumber(getResultText());
                    calculatelabel.setText(calcText + " " + getResultText() + " =");
                    resultlabel.setText(CalculatorActivity.calculate(getCalculateText().replace("×", "*").replace("÷", "/")));
                } else {
                    if (!getLastOp().isEmpty()) {
                        calculatelabel.setText(getResultText() + " " + getLastOp() + " " + getLastNumber() + " =");
                    } else {
                        calculatelabel.setText(getResultText() + " =");
                    }
                    resultlabel.setText(CalculatorActivity.calculate(getResultText() + " " + getLastOp().replace("×", "*").replace("÷", "/") + " " + getLastNumber()));
                }

            }
            setCalculateText(getCalculateText().replace("*", "×").replace("/", "÷"));
            dataManager.addToHistory(getCalculateText() + "\n" + getResultText(), getApplicationContext());
            dataManager.saveNumbers(getApplicationContext());
        } else {
            setResultText("0");
            setCalculateText("");
        }

        setRotateOperator(false);
        formatResultTextAfterType();
        setRemoveValue(true);
        adjustTextSize();
        formatResultTextAfterType();
        scrollToBottom(findViewById(R.id.calculate_scrollview));
    }

    /**
     * This method checks if the input text is invalid.
     *
     * @param text The text to be checked. This should be a string containing the text input from the user or the result of a calculation.
     * @return Returns true if the text is invalid (contains "Ungültige Eingabe", "Unendlich", "Syntax Fehler", or "Domainfehler"), and false otherwise.
     */
    private boolean isInvalidInput(String text) {
        return  text.contains("Ungültige Eingabe") ||
                text.contains("Unendlich") ||
                text.contains("Syntax Fehler") ||
                text.contains("Domainfehler") ||
                text.contains("For input string") ||
                text.contains("Wert zu groß") ||
                text.contains("Kein Teilen") ||
                text.contains("Ungültiges Zahlenformat");
    }

    /**
     * This method formats the result text after a calculation.
     *
     * @param text The text to be formatted. This should be a string representation of the result of a calculation.
     * @return Returns a string representation of the formatted number.
     */
    @SuppressLint("DefaultLocale")
    public String formatResultTextAfterCalculate(String text) {
        String formattedNumber;
        double v = Double.parseDouble(text.replace(".", "").replace(",", "."));
        if (text.length() >= 18) {
            int exponent = (int) Math.floor(Math.log10(v));
            formattedNumber = String.format("%.8fE%+d", v / Math.pow(10, exponent), exponent);
        } else {
            if (text.matches("^-?\\d+([.,]\\d*)?([eE][+-]?\\d+)$")) {
                formattedNumber = text;
            } else {
                final java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#,###.####");
                formattedNumber = decimalFormat.format(v);
            }
        }
        setResultText(formattedNumber);
        return formattedNumber;
    }

    /**
     * This method formats the result text after a type operation.
     * It separates the integer and fractional parts of the result text, formats them separately, and then combines them.
     * If the result text is an error message or the notation flag is set, it does not format the result text.
     */
    public void formatResultTextAfterType() {
        String originalText = getResultText();
        if(originalText != null) {
            int index = originalText.indexOf(',');
            String result;
            String result2;
            if (index != -1) {
                result = originalText.substring(0, index).replace(".", "");
                result2 = originalText.substring(index);
            } else {
                result = originalText.replace(".", "");
                result2 = "";
            }
            if(!isInvalidInput(getResultText())){
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                try {
                    BigDecimal bigDecimalResult = new BigDecimal(result);
                    String formattedNumber = decimalFormat.format(bigDecimalResult);
                    setResultText(formattedNumber + result2);
                } catch (NumberFormatException e) {
                    System.out.println("Ungültiges Zahlenformat: " + result);
                }
            } else if (getIsNotation()) {
                setIsNotation(false);
            }
        }
    }

    /**
     * This method adjusts the text size of the result label based on its length.
     * If the result text is not "Nur reelle Zahlen" and its length is 12 or more, the text size is reduced to fit the label.
     * If the result text is "Ungültige Eingabe" or "Nur reelle Zahlen", the text size is set to a specific value.
     */
    public void adjustTextSize() {
        if(getResultText() != null) {
            int len = getResultText().replace(",", "").replace(".", "").replace("-", "").length();
            TextView label = findViewById(R.id.result_label);
            if(!getResultText().equals("Nur reelle Zahlen")) {
                if (!getResultText().equals("Ungültige Eingabe")) {
                    if (len >= 12) {
                        label.setTextSize(45f);
                        if (len >= 15) {
                            label.setTextSize(35f);
                        }
                    } else {
                        label.setTextSize(55f);
                    }
                } else {
                    label.setTextSize(45f);
                }
            } else {
                label.setTextSize(50f);
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
    public void setIsNotation(final boolean val) { isnotation = val; }
    public boolean getIsNotation() { return isnotation; }
    public void setRotateOperator(final boolean rotate) { rotateOperatorAfterRoot = rotate; }
    public boolean getRotateOperator() { return rotateOperatorAfterRoot; }
    public String getLastOp() {
        return last_op;
    }
    public void setLastOp(final String s) {
        last_op = s;
    }
    public boolean getRemoveValue() {
        final String value = dataManager.readFromJSON("removeValue", getApplicationContext());
        if(value == null) {
            dataManager.saveToJSON("removeValue", false,getApplicationContext());
        }
        assert value != null;
        return value.equals("true");
    }
    public void setRemoveValue(final boolean b) {
        dataManager.saveToJSON("removeValue", b, getApplicationContext());
    }
    public void setLastNumber(final String s) {
        last_number = s.replace(".", "");
    }
    public String getLastNumber() {
        final String num = last_number.replace(".", "").replace(",", ".");
        final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        return decimalFormat.format(Double.parseDouble(num));
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
        resulttext.setText(getResultText() + s);
    }
    public void setResultText(final String s) {
        TextView resulttext = findViewById(R.id.result_label);
        if(resulttext != null) { resulttext.setText(s); }
    }
    public String getCalculateText() {
        TextView calculatetext = findViewById(R.id.calculate_label);
        return calculatetext.getText().toString();
    }
    @SuppressLint("SetTextI18n")
    public void addCalculateText(final String s) {
        TextView calculatetext = findViewById(R.id.calculate_label);
        calculatetext.setText(getCalculateText() + " " + s);
    }
    public void setCalculateText(final String s) {
        TextView calculatetext = findViewById(R.id.calculate_label);
        calculatetext.setText(s);
    }
}