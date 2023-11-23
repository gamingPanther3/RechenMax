package com.mlprograms.rechenmax;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class HistoryActivity extends AppCompatActivity {

    private Context context = this;
    private LinearLayout linearLayout;
    DataManager dataManager;
    private static MainActivity mainActivity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        TextView history_text_view = (TextView) findViewById(R.id.history_textview);
        history_text_view.setText(loadHistory(getApplicationContext()));

        // DialogHelper dialogHelper = new DialogHelper(this);
        // dialogHelper.showPatchNotesDialog();

        linearLayout = findViewById(R.id.historyUI);

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switchDisplayMode(currentNightMode);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switchDisplayMode(currentNightMode);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnToCalculator();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null && dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()).equals("true")) {
            dataManager.saveToJSON("disablePatchNotesTemporary", "false", getApplicationContext());
        }
        finish();
    }

    private void switchDisplayMode(int currentNightMode) {
        Button deleteButton = findViewById(R.id.history_delete_button);
        Button returnButton = findViewById(R.id.history_return_button);
        ScrollView historyScrollView = findViewById(R.id.history_scroll_textview);
        TextView historyTextView = findViewById(R.id.history_textview);
        TextView historyTitle = findViewById(R.id.history_title);
        TextView historyReturnButton = (TextView) findViewById(R.id.history_return_button);
        TextView historyDeleteButton = (TextView) findViewById(R.id.history_delete_button);

        int newColorBTNForegroundAccent;
        int newColorBTNBackgroundAccent;

        dataManager = new DataManager(this);
        final String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());
        if (getSelectedSetting() != null && getSelectedSetting().equals("Systemstandard")) {
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_YES:
                    if(trueDarkMode != null && trueDarkMode.equals("true")) {
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);

                        updateUIAccordingToNightMode(deleteButton, returnButton, historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                        updateUIAccordingToNightMode(deleteButton, returnButton, historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    }
                    historyReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                    historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24_light));
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);

                    updateUIAccordingToNightMode(deleteButton, returnButton, historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

                    historyReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
                    historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24));
                    break;
            }
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Tageslichtmodus"))  {
            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
            updateUIAccordingToNightMode(deleteButton, returnButton, historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

            historyReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
            historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24));
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Dunkelmodus")) {
            historyReturnButton.setForeground(getDrawable(R.drawable.baseline_history_24_light));
            historyDeleteButton.setForeground(getDrawable(R.drawable.baseline_settings_24_light));

            if(trueDarkMode != null && trueDarkMode.equals("true")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);

                updateUIAccordingToNightMode(deleteButton, returnButton, historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            } else if (trueDarkMode != null && trueDarkMode.equals("false")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                updateUIAccordingToNightMode(deleteButton, returnButton, historyScrollView, historyTextView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            }
        }
    }

    private void updateUIAccordingToNightMode(Button deleteButton, Button returnButton, ScrollView historyScrollView, TextView historyTextView, TextView historyTitle, int newColorBTNForegroundAccent, int newColorBTNBackgroundAccent) {
        if (deleteButton != null) {
            deleteButton.setTextColor(newColorBTNForegroundAccent);
            deleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24));
        }
        if (returnButton != null) {
            returnButton.setTextColor(newColorBTNForegroundAccent);
            returnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
        }
        if (historyScrollView != null) {
            historyScrollView.setBackgroundColor(newColorBTNBackgroundAccent);
        }
        if (historyTextView != null) {
            historyTextView.setTextColor(newColorBTNForegroundAccent);
        }
        if (historyTitle != null) {
            historyTitle.setTextColor(newColorBTNForegroundAccent);
        }
        // Change the foreground and background colors of all buttons in your layout
        if (newColorBTNForegroundAccent != 0 && newColorBTNBackgroundAccent != 0) {
            changeButtonColors((ViewGroup) findViewById(R.id.historyUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
        }

        deleteButton.setTextColor(newColorBTNForegroundAccent);
        deleteButton.setBackgroundColor(newColorBTNBackgroundAccent);
        deleteButton.setForeground(getDrawable(R.drawable.baseline_delete_24_light));
        returnButton.setTextColor(newColorBTNForegroundAccent);
        returnButton.setBackgroundColor(newColorBTNBackgroundAccent);
        returnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
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
    private static final String FILE_NAME = "history.txt";
    public void deleteHistory(Context context) {
        try {
            FileOutputStream fileOut = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileOut);
            outputWriter.write("");
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView history_text_view = (TextView) findViewById(R.id.history_textview);
        history_text_view.setText(loadHistory(getApplicationContext()));
    }
    public String loadHistory(Context context) {
        StringBuilder result = new StringBuilder();
        try {
            FileInputStream fileIn = context.openFileInput(FILE_NAME);
            InputStreamReader inputReader = new InputStreamReader(fileIn);
            char[] inputBuffer = new char[100];
            int charRead;
            while ((charRead = inputReader.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                result.append(readString);
            }
            inputReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView history_text_view = (TextView) findViewById(R.id.history_textview);
        if (result.toString().equals("")) {
            history_text_view.setTextSize(40);
            history_text_view.setGravity(Gravity.CENTER_HORIZONTAL);
            return "\n\n\n\n\n\nDein Verlauf ist leer.";
        } else {
            history_text_view.setTextSize(45);
            history_text_view.setGravity(Gravity.END);
            return result.toString();
        }
    }
    public Integer getSelectetSettingPosition() {
        Integer num = null;
        final String readselectedSetting = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());

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
    public static void setMainActivityContext(MainActivity activity) {
        mainActivity = activity;
    }
    public Context getMainActivityContext() {
        return mainActivity;
    }
    public String getSelectedSetting() {
        if(dataManager != null) {
            final String setting = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());
            if(setting != null) {
                if(setting.equals("System")) {
                    return "Systemstandard";
                } else if (setting.equals("Dark")) {
                    return "Dunkelmodus";
                } else if (setting.equals("Light")) {
                    return "Tageslichtmodus";
                }
            }
        }
        return null;
    }

    public void ButtonListener2(View view) {
        if (view.getTag().equals("return")) {
            returnToCalculator();
        } else if (view.getTag().equals("delete")) {
            deleteHistory(getApplicationContext());
        }
    }

    public void returnToCalculator() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }
}