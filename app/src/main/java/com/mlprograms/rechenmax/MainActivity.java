package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.CalculatorActivity.setMainActivity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.android.material.textview.MaterialTextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private boolean rotateOperatorAfterRoot = false;
    private Context context = this;
    private boolean removevalue = false;
    private String last_number = "0";
    private String last_op = "+";
    private DataManager dataManager;
    private String calculatingMode;
    SharedPreferences prefs = null;
    private boolean isnotation = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculatorui);
        context = this;
        setMainActivity(this);

        dataManager = new DataManager(this);
        //dataManager.deleteJSON(getApplicationContext());
        dataManager.createJSON(getApplicationContext());
        //resetReleaseNoteConfig(getApplicationContext());

        dataManager.loadNumbers();
        dataManager.checkAndCreateFile();

        calculatingMode = dataManager.readFromJSON("calculatingMode", context);

        prefs = getSharedPreferences("com.mlprograms.RechenMax", MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            setContentView(R.layout.patchnotes);
            checkDarkmodeSetting();

            // 'firstrun' auf false setzen, nachdem der Code ausgeführt wurde
            prefs.edit().putBoolean("firstrun", false).commit();
        }

        MaterialTextView mEditText1 = (MaterialTextView) findViewById(R.id.calculate_label);
        MaterialTextView mEditText2 = (MaterialTextView) findViewById(R.id.result_label);
        Log.e("MainActivity", "showPatchNotes=" + dataManager.readFromJSON("showPatchNotes", getApplicationContext()));
        Log.e("MainActivity", "disablePatchNotesTemporary=" + dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()));

        final String showPatNot = dataManager.readFromJSON("showPatchNotes", getApplicationContext());
        final String disablePatNotTemp = dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext());

        if (showPatNot != null && disablePatNotTemp != null) {
            if (showPatNot.equals("true") && disablePatNotTemp.equals("false")) {
                setContentView(R.layout.patchnotes);
                checkDarkmodeSetting();
            }
        }
        setUpListeners();
        checkScienceButtonState();
        checkDarkmodeSetting();
    }
    public void setUpListeners() {
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
        setButtonListener(R.id.clipOn, this::clipOnAction);
        setButtonListener(R.id.clipOff, this::clipOffAction);
        setButtonListener(R.id.power, this::powerAction);
        setButtonListener(R.id.root, this::rootAction);
        setScienceButtonListener();
    }
    private void setScienceButtonListener() {
        Button toggleButton = (Button) findViewById(R.id.scientificButton);
        if(toggleButton != null) {
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });
        }
    }
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
    private void setCommaButtonListener(int buttonId) {
        Button btn = (Button) findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommaAction();
                    dataManager.saveNumbers(getApplicationContext());
                }
            });
        }
    }
    private void setCalculateButtonListener(int buttonId) {
        Button btn = (Button) findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Calculate();
                        dataManager.saveNumbers(getApplicationContext());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
    private void setNumberButtonListener(int buttonId) {
        Button btn = (Button) findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String num = v.getTag().toString();
                    NumberAction(num);
                    dataManager.saveNumbers(getApplicationContext());
                }
            });
        }
    }
    private void setOperationButtonListener(int buttonId, String operation) {
        Button btn = (Button) findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OperationAction(operation);
                    dataManager.saveNumbers(getApplicationContext());
                }
            });
        }
    }
    private void setEmptyButtonListener(int buttonId, String action) {
        Button btn = (Button) findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EmptyAction(action);
                    dataManager.saveNumbers(getApplicationContext());
                }
            });
        }
    }
    private void setButtonListener(int buttonId, Runnable action) {
        Button btn = (Button) findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    action.run();
                    dataManager.saveNumbers(getApplicationContext());
                }
            });
        }
    }
    private void setNegateButtonListener(int buttonId) {
        Button btn = (Button) findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NegativAction();
                    dataManager.saveNumbers(getApplicationContext());
                }
            });
        }
    }
    private void setClipboardButtonListener(int buttonId, String action) {
        Button btn = (Button) findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardAction(action);
                    dataManager.saveNumbers(getApplicationContext());
                }
            });
        }
    }
    private void clipOnAction() {
        addCalculateText("(");
    }
    private void clipOffAction() {
        Pattern pattern = Pattern.compile("√\\(\\d+\\)$");
        Matcher matcher = pattern.matcher(getCalculateText());

        if (matcher.find()) {
            addCalculateText(" )");
        } else {
            addCalculateText(getResultText() + " )");
        }
        setRotateOperator(true);
    }
    private void powerAction() {
        if(!getRotateOperatorAfterRoot()) {
            addCalculateText(getResultText() + " ^");
            setRemoveValue(true);
        } else {
            if (getCalculateText().replace(" ", "").charAt(getCalculateText().replace(" ", "").length() - 1) == ')') {
                addCalculateText("^");
            } else {
                addCalculateText(" ^");
            }
            setRemoveValue(true);
            setRotateOperator(false);
        }
    }
    private void rootAction() {
        if(!getRotateOperatorAfterRoot()) {
            addCalculateText("√(" + getResultText() + ")");
        } else {
            addCalculateText(getLastOp() + " √(" + getResultText() + ")");
        }
        setRemoveValue(true);
        setRotateOperator(true);
    }
     public void patchNotesOkayButtonAction() {
        CheckBox checkBox = findViewById(R.id.checkBox);
        if (checkBox.isChecked()) {
            dataManager.saveToJSON("showPatchNotes", false, getApplicationContext());
            dataManager.saveToJSON("disablePatchNotesTemporary", true, getApplicationContext());
            dataManager.saveToJSON("settingReleaseNotesSwitch", false, getApplicationContext());
            Log.e("MainActivity", "showPatchNotes=" + dataManager.readFromJSON("showPatchNotes", getApplicationContext()));
            Log.e("MainActivity", "disablePatchNotesTemporary=" + dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()));
        } else {
            dataManager.saveToJSON("showPatchNotes", true, getApplicationContext());
            dataManager.saveToJSON("disablePatchNotesTemporary", true, getApplicationContext());
            dataManager.saveToJSON("settingReleaseNotesSwitch", true, getApplicationContext());
            Log.e("MainActivity", "showPatchNotes=" + dataManager.readFromJSON("showPatchNotes", getApplicationContext()));
            Log.e("MainActivity", "disablePatchNotesTemporary=" + dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()));
        }
        setContentView(R.layout.calculatorui);
        dataManager.loadNumbers();
        checkDarkmodeSetting();
        checkScienceButtonState();
        setUpListeners();
    }
    public void switchToSettingsAction() {
        SettingsActivity settingsActivity = new SettingsActivity();
        settingsActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    private void switchToHistoryAction() {
        HistoryActivity historyActivity = new HistoryActivity();
        historyActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }
    public void checkDarkmodeSetting() {
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }
    private void switchDisplayMode(int currentNightMode) {
        // Globale Variablen
        TextView historyButton = (TextView) findViewById(R.id.history_button);
        TextView settingsButton = (TextView) findViewById(R.id.settings_button);
        TextView scienceButton = (TextView) findViewById(R.id.scientificButton);
        currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int newColorBTNBackgroundAccent = 0;
        int newColorBTNForegroundAccent = 0;

        // Theme-Einstellung abrufen
        String selectedSetting = getSelectedSetting();
        // UI-Elemente aktualisieren
        final String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext());
        if (selectedSetting != null) {
            if (selectedSetting.equals("Systemstandard")) {
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        if(trueDarkMode != null && trueDarkMode.equals("true")) {
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                        } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);
                        }
                        if (historyButton != null) {
                            historyButton.setForeground(getDrawable(R.drawable.baseline_history_24_light));
                        }
                        if(settingsButton != null) {
                            settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24_light));
                        }
                        if(scienceButton != null) {
                            scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24_light));
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                        if (historyButton != null) {
                            historyButton.setForeground(getDrawable(R.drawable.baseline_history_24));
                        }
                        if(settingsButton != null) {
                            settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24));
                        }
                        if(scienceButton != null) {
                            scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24));
                        }
                        break;
                }
            } else if (selectedSetting.equals("Tageslichtmodus")) {
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                if (historyButton != null) {
                    historyButton.setForeground(getDrawable(R.drawable.baseline_history_24));
                }
                if(settingsButton != null) {
                    settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24));
                }
                if(scienceButton != null) {
                    scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24));
                }
            } else if (selectedSetting.equals("Dunkelmodus")) {
                dataManager = new DataManager(this);

                if (trueDarkMode != null) {
                    if (trueDarkMode.equals("false")) {
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                    } else {
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                    }
                } else {
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                }
                if (historyButton != null) {
                    historyButton.setForeground(getDrawable(R.drawable.baseline_history_24_light));
                }
                if(settingsButton != null) {
                    settingsButton.setForeground(getDrawable(R.drawable.baseline_settings_24_light));
                }
                if(scienceButton != null) {
                    scienceButton.setForeground(getDrawable(R.drawable.baseline_science_24_light));
                }
            }

            // UI-Elemente aktualisieren
            changeTextViewColors((ViewGroup) findViewById(R.id.patchnotesUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeButtonColors((ViewGroup) findViewById(R.id.patchnotesUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeTextViewColors((ViewGroup) findViewById(R.id.calculatorUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeButtonColors((ViewGroup) findViewById(R.id.calculatorUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
        } else {
            dataManager.saveToJSON("selectedSpinnerSetting", "System", getApplicationContext());
            switchDisplayMode(currentNightMode);
        }
    }
    public Integer getSelectetSettingPosition() {
        Integer num = null;
        final String readselectedSetting = dataManager.readFromJSON("selectedSpinnerSetting", getApplicationContext());

        if(readselectedSetting != null) {
            if(readselectedSetting.equals("System")) {
                num = 0;
            } else if (readselectedSetting.equals("Light")) {
                num = 1;
            } else if (readselectedSetting.equals("Dark")) {
                num = 2;
            }
        }
        return num;
    }
    public String getSelectedSetting() {
        final String setting = dataManager.readFromJSON("selectedSpinnerSetting", getApplicationContext());
        if(setting != null) {
            if(setting.equals("System")) {
                return "Systemstandard";
            } else if (setting.equals("Dark")) {
                return "Dunkelmodus";
            } else if (setting.equals("Light")) {
                return "Tageslichtmodus";
            }
        }
        return null;
    }
    private void changeButtonColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);

                // Wenn das child ein Button ist, ändere die Vordergrund- und Hintergrundfarben
                if (v instanceof Button) {
                    ((Button) v).setTextColor(foregroundColor);
                    ((Button) v).setBackgroundColor(backgroundColor);
                }
                // Wenn das child selbst ein ViewGroup (z.B. ein Layout) ist, rufe Funktion rekursiv auf
                else if (v instanceof ViewGroup) {
                    changeButtonColors((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }
    private void changeTextViewColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);

                // Wenn das child ein TextView ist, ändere die Vordergrund- und Hintergrundfarben
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(foregroundColor);
                    ((TextView) v).setBackgroundColor(backgroundColor);
                }
                // Wenn das child selbst ein ViewGroup (z.B. ein Layout) ist, rufe Funktion rekursiv auf
                else if (v instanceof ViewGroup) {
                    changeTextViewColors((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }
    public void resetReleaseNoteConfig(Context applicationContext) {
        dataManager.saveToJSON("showPatchNotes", true, getApplicationContext());
        dataManager.saveToJSON("disablePatchNotesTemporary", false, getApplicationContext());
    }
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()).equals("true")) {
            dataManager.saveToJSON("disablePatchNotesTemporary", false, getApplicationContext());
        }
        finish();
    }
    public void NumberAction(String num) {
        String resultText = getResultText();
        String calculateText = getCalculateText().toString();
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
    public void ClipboardAction(final String c) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (c.equals("MC")) {
            ClipData clipData = ClipData.newPlainText("", "");
            clipboardManager.setPrimaryClip(clipData);
        } else if (c.equals("MR")) {
            handleMRAction(clipboardManager);
        } else if (c.equals("MS")) {
            ClipData clipData = ClipData.newPlainText("", getResultText().toString());
            clipboardManager.setPrimaryClip(clipData);
        }
        adjustTextSize();
    }
    private void handleMRAction(ClipboardManager clipboardManager) {
        ClipData clipData = clipboardManager.getPrimaryClip();
        ClipData.Item item = clipData.getItemAt(0);
        String text = (String) item.getText();
        if (text.replace(".", "").matches("^-?\\d+([.,]\\d*)?([eE][+-]?\\d+)?$")) {
            setRemoveValue(false);
            String result = formatResultTextAfterCalculate(text);
            setResultText(result.toLowerCase());
        } else {
            TextView label = findViewById(R.id.result_label);
            setResultText("Ungültige Eingabe");
            setRemoveValue(true);
        }
        adjustTextSize();
    }

    public void OperationAction(final String op) {
        setLastOp(op);
        final String new_op = op.replace("*", "×").replace("/", "÷");
        if(!getRotateOperatorAfterRoot()) {
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
    }
    public void EmptyAction(final String e) {
        if (e.equals("⌫")) {
            handleBackspaceAction();
        } else if (e.equals("C")) {
            setResultText("0");
            setCalculateText("");
            setRotateOperator(false);
        } else if (e.equals("CE")) {
            setResultText("0");
        }
        adjustTextSize();
    }
    private void handleBackspaceAction() {
        String resultText = getResultText();
        if (!resultText.equals("Ungültige Eingabe")) {
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
    public void NegativAction() {
        final char firstchar = getResultText().charAt(0);
        if (String.valueOf(firstchar).equals("-")) {
            setResultText(getResultText().substring(1));
        } else {
            setResultText("-" + getResultText());
        }
    }
    public void CommaAction() {
        if (!getResultText().contains(",")) {
            addResultText(",");
        }
    }
    public void Calculate() throws Exception {
        String calcText = getCalculateText().replace("*", "×").replace("/", "÷");
        TextView calclab = (TextView) findViewById(R.id.calculate_label);
        TextView reslab = (TextView) findViewById(R.id.result_label);

        if(getRotateOperatorAfterRoot()) {
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
            //formatResultTextAfterCalculate(getResultText());

            setCalculateText(getCalculateText().replace("*", "×").replace("/", "÷"));
            dataManager.addtoHistory(getCalculateText() + "\n" + getResultText(), getApplicationContext());
            dataManager.saveNumbers(getApplicationContext());
        } else {
            if (!calcText.contains("=")) {
                setLastNumber(getResultText());
                calclab.setText(calcText + " " + getResultText() + " =");
                reslab.setText(CalculatorActivity.calculate(getCalculateText().replace("×", "*").replace("÷", "/")));
            } else {
                if (!getLastOp().isEmpty()) {
                    calclab.setText(getResultText() + " " + getLastOp() + " " + getLastNumber() + " =");
                } else {
                    calclab.setText(getResultText() + " =");
                }
                reslab.setText(CalculatorActivity.calculate(getResultText() + " " + getLastOp().replace("×", "*").replace("÷", "/") + " " + getLastNumber()));
            }
            //formatResultTextAfterCalculate(getResultText());

            setCalculateText(getCalculateText().replace("*", "×").replace("/", "÷"));
            dataManager.addtoHistory(getCalculateText() + "\n" + getResultText(), getApplicationContext());
            dataManager.saveNumbers(getApplicationContext());
        }
        setRotateOperator(false);
        formatResultTextAfterType();
        setRemoveValue(true);
        adjustTextSize();
    }
    private boolean isInvalidInput(String text) {
        return text.contains("Ungültige Eingabe") || text.contains("Unendlich") || text.contains("Syntax Fehler");
    }
    public static BigDecimal round(String value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(8, RoundingMode.HALF_UP);
        return bd;
    }
    public String formatResultTextAfterCalculate(String text) {
        String formattedNumber;
        if (text.length() >= 18) {
            double number = Double.parseDouble(text.replace(".", "").replace(",", "."));
            int exponent = (int) Math.floor(Math.log10(number));
            formattedNumber = String.format("%.8fE%+d", number / Math.pow(10, exponent), exponent);
        } else {
            if (text.matches("^-?\\d+([.,]\\d*)?([eE][+-]?\\d+)$")) {
                formattedNumber = text;
            } else {
                final java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#,###.####");
                formattedNumber = decimalFormat.format(Double.parseDouble(text.replace(".", "").replace(",", ".")));
            }
        }
        setResultText(formattedNumber);
        return formattedNumber;
    }
    public void formatResultTextAfterType() {
        String originalText = getResultText();
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
        if(!getResultText().equals("Unendlich") && !getResultText().equals("Syntax Fehler") && !getIsNotation()) {
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            String formattedNumber = decimalFormat.format(Long.parseLong(result));
            setResultText(formattedNumber + result2);
        } else if (getIsNotation()) {
            setIsNotation(false);
        }
    }
    public void adjustTextSize() {
        int len = getResultText().replace(",", "").replace(".", "").replace("-", "").length();
        TextView label = findViewById(R.id.result_label);
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
            label.setTextSize(50f);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    public void setIsNotation(final boolean val) { isnotation = val; }
    public boolean getIsNotation() { return isnotation; }
    public void setRotateOperator(final boolean rotate) { rotateOperatorAfterRoot = rotate; }
    public boolean getRotateOperatorAfterRoot() { return rotateOperatorAfterRoot; }
    public String getLastOp() {
        return last_op;
    }
    public void setLastOp(final String s) {
        last_op = s;
    }
    public boolean getRemoveValue() {
        return removevalue;
    }
    public void setRemoveValue(final boolean b) {
        removevalue = b;
    }
    public void setLastNumber(final String s) {
        last_number = s.replace(".", "");
    }
    public String getLastNumber() {
        final String num = last_number.replace(".", "").replace(",", ".");
        final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        final String formattedNumber = decimalFormat.format(Double.parseDouble(num));
        return formattedNumber;
    }
    public String getResultText() {
        TextView resulttext = (TextView) findViewById(R.id.result_label);
        return resulttext.getText().toString();
    }
    public void addResultText(final char c) {
        TextView resulttext = (TextView) findViewById(R.id.result_label);
        resulttext.setText(getResultText() + c);
    }
    public void addResultText(final String s) {
        TextView resulttext = (TextView) findViewById(R.id.result_label);
        resulttext.setText(getResultText() + s);
    }
    public void setResultText(final char c) {
        TextView resulttext = (TextView) findViewById(R.id.result_label);
        if(resulttext != null) { resulttext.setText(c); }
    }
    public void setResultText(final String s) {
        TextView resulttext = (TextView) findViewById(R.id.result_label);
        if(resulttext != null) { resulttext.setText(s); }
    }
    public String getCalculateText() {
        TextView calculatetext = (TextView) findViewById(R.id.calculate_label);
        return calculatetext.getText().toString();
    }
    public void addCalculateText(final char c) {
        TextView calculatetext = (TextView) findViewById(R.id.calculate_label);
        calculatetext.setText(getCalculateText() + " " + c);
    }
    public void addCalculateText(final String s) {
        TextView calculatetext = (TextView) findViewById(R.id.calculate_label);
        calculatetext.setText(getCalculateText() + " " + s);
    }
    public void setCalculateText(final char c) {
        TextView calculatetext = (TextView) findViewById(R.id.calculate_label);
        calculatetext.setText(c);
    }
    public void setCalculateText(final String s) {
        TextView calculatetext = (TextView) findViewById(R.id.calculate_label);
        calculatetext.setText(s);
    }
}