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

import static com.mlprograms.rechenmax.ToastHelper.showToastShort;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HistoryActivity - Displays calculation history.
 * @author Max Lemberg
 * @version 1.4.4
 * @date 27.12.2023
 */
public class HistoryActivity extends AppCompatActivity {

    // Declare a Context object and initialize it to this instance
    private int newColorBTNForegroundAccent;
    private int newColorBTNBackgroundAccent;

    private final Context context = this;
    DataManager dataManager;
    private static MainActivity mainActivity;
    private LinearLayout innerLinearLayout;

    private int ITEMS_PER_LOAD = 20 * 2;
    private int historyTextViewNumber = 0;
    private int currentHistoryTextViewNumber;
    private boolean isEndReached = false;

    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historyui);

        dataManager = new DataManager();
        dataManager.saveToJSONSettings("lastActivity", "His", getApplicationContext());

        try {
            historyTextViewNumber = Integer.parseInt(dataManager.getHistoryData("historyTextViewNumber", getMainActivityContext()).getString("value"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        switchDisplayMode();

        //Log.e("DEBUG", dataManager.getJSONSettingsData("historyMode", getMainActivityContext()));

        Button historyReturnButton = findViewById(R.id.history_return_button);
        Button historyDeleteButton = findViewById(R.id.history_delete_button);

        historyReturnButton.setOnClickListener(v -> returnToCalculator());
        historyDeleteButton.setOnClickListener(v -> resetNamesAndValues());

        // Create an instance of the outer LinearLayout
        ScrollView outerLinearLayout = findViewById(R.id.history_scrollview);

        // Create an instance of the inner LinearLayout
        innerLinearLayout = new LinearLayout(this);
        innerLinearLayout.setOrientation(LinearLayout.VERTICAL);

        // Set the inner LinearLayout as the content of the outer LinearLayout
        outerLinearLayout.addView(innerLinearLayout);

        // Use a separate thread to create TextViews in the background
        // Using ExecutorService instead of AsyncTask

        createLoadingHistoryTextView();
        //Log.e("DEBUG", String.valueOf(dataManager.getAllDataFromHistory(getMainActivityContext())));

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                return historyTextViewNumber;
            }

            @Override
            protected void onPostExecute(Integer value) {
                if(value == null) {
                    dataManager.saveToHistory("historyTextViewNumber", "0", getApplicationContext());
                }

                if(value == 0) {
                    TextView loadTextView = findViewById(R.id.history_load_textview);
                    if(loadTextView != null) {
                        loadTextView.setVisibility(View.GONE);
                    }
                    createEmptyHistoryTextView();
                    switchDisplayMode();
                } else {
                    hideEmptyHistoryTextView();

                    try {
                        final String mode = dataManager.getJSONSettingsData("historyMode", getMainActivityContext()).getString("value");
                        final String advanced = dataManager.getJSONSettingsData("historyModeAdvanced", getMainActivityContext()).getString("value");

                        for (int i = historyTextViewNumber; i >= Math.max(0, historyTextViewNumber - ITEMS_PER_LOAD); i--) {
                            currentHistoryTextViewNumber = i;

                            LinearLayout linearLayout;
                            if (mode.equals("single")) {
                                if(advanced.equals("false")) {
                                    linearLayout = createHistoryTextViewSingleEasy(i);
                                } else {
                                    linearLayout = createHistoryTextViewSingleAdvanced(i);
                                }
                            } else { /* mode.equals("multiple") */
                                if (advanced.equals("false")) {
                                    linearLayout = createHistoryTextViewMultipleEasy(i);
                                } else {
                                    linearLayout = createHistoryTextViewMultipleAdvanced(i);
                                }
                            }

                            if(linearLayout != null) {
                                innerLinearLayout.addView(linearLayout, countLinearLayouts(innerLinearLayout));
                            } else {
                                ITEMS_PER_LOAD++;
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    TextView loadTextView = findViewById(R.id.history_load_textview);
                    if(loadTextView != null) {
                        loadTextView.setVisibility(View.GONE);
                    }
                    switchDisplayMode();
                }
            }
        }.execute();
        //Log.e("DEBUG", String.valueOf(dataManager.getAllData(getMainActivityContext())));

        ScrollView historyScrollView = findViewById(R.id.history_scrollview);
        historyScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (!isEndReached && isEndOfScrollView(historyScrollView)) {
                isEndReached = true;
                loadLayoutToUI(1);
            } else {
                isEndReached = false;
            }
        });
    }

    /**
     * Überprüft, ob das Ende des ScrollViews erreicht ist.
     *
     * @param scrollView Das ScrollView, das überwacht wird.
     * @return true, wenn das Ende des Scrolls erreicht ist, sonst false.
     */
    private boolean isEndOfScrollView(ScrollView scrollView) {
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        return diff <= 1000;
    }

    /**
     * Updates the UI by creating and adding TextViews for each history entry.
     */
    private void loadLayoutToUI(@Nullable Integer num) {
        if (num != null && num >= 0) {
            ITEMS_PER_LOAD = num;
        }

        if (historyTextViewNumber == 0 || currentHistoryTextViewNumber > historyTextViewNumber) {
            return;
        }

        int loadedItems = 0;
        try {
            final String mode = dataManager.getJSONSettingsData("historyMode", getMainActivityContext()).getString("value");
            final String advanced = dataManager.getJSONSettingsData("historyModeAdvanced", getMainActivityContext()).getString("value");
            int startIndex = currentHistoryTextViewNumber - 1;
            int endIndex = Math.max(0, currentHistoryTextViewNumber - ITEMS_PER_LOAD);

            for (int i = startIndex; i >= endIndex && loadedItems < ITEMS_PER_LOAD; i--) {
                currentHistoryTextViewNumber = i;

                LinearLayout linearLayout;
                if (mode.equals("single")) {
                    if(advanced.equals("false")) {
                        linearLayout = createHistoryTextViewSingleEasy(i);
                    } else {
                        linearLayout = createHistoryTextViewSingleAdvanced(i);
                    }
                } else { /* mode.equals("multiple") */
                    if (advanced.equals("false")) {
                        linearLayout = createHistoryTextViewMultipleEasy(i);
                    } else {
                        linearLayout = createHistoryTextViewMultipleAdvanced(i);
                    }
                }

                if (linearLayout != null) {
                    innerLinearLayout.addView(linearLayout, countLinearLayouts(innerLinearLayout));
                    loadedItems++;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        switchDisplayMode();
        ITEMS_PER_LOAD = 10;
    }

    /**
     * Creates a new TextView for displaying calculation history.
     *
     * @return The created TextView.
     */
    private LinearLayout createHistoryTextViewSingleAdvanced(int i) throws JSONException {
        JSONObject data = dataManager.getHistoryData(String.valueOf(i), getMainActivityContext());

        if(data == null) {
            return null;
        }
        final String[] parts = data.getString("calculation").split("=");

        if (parts.length <= 1) {
            return null;
        }

        AtomicBoolean clickListener = new AtomicBoolean(true);

        TextView emptyTextView = findViewById(R.id.history_empty_textview);
        if (emptyTextView != null) {
            emptyTextView.setVisibility(View.GONE);
        }

        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLinearLayout.setId(i);

        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(
                30,
                10,
                30,
                0);

        // date
        TextView timeDateTextView = new TextView(this);
        timeDateTextView.setLayoutParams(textViewLayoutParams);

        timeDateTextView.setText(data.getString("date"));
        timeDateTextView.setTextColor(Color.BLACK);
        timeDateTextView.setTypeface(null, Typeface.BOLD);
        timeDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        timeDateTextView.setGravity(Gravity.START);
        mainLinearLayout.addView(timeDateTextView);

        HorizontalScrollView resultHorizontalScrollView = new HorizontalScrollView(this);
        resultHorizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        resultHorizontalScrollView.setHorizontalScrollBarEnabled(false);
        resultHorizontalScrollView.setVerticalScrollBarEnabled(false);

        // result title
        TextView resultTextViewTitle = new TextView(this);
        textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(
                30,
                10,
                30,
                0);

        resultTextViewTitle.setLayoutParams(textViewLayoutParams);
        resultTextViewTitle.setText(getString(R.string.historyResult));
        resultTextViewTitle.setTextColor(Color.BLACK);
        resultTextViewTitle.setTypeface(null, Typeface.BOLD);
        resultTextViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        resultTextViewTitle.setGravity(Gravity.START);
        mainLinearLayout.addView(resultTextViewTitle);

        // result
        TextView resultTextView = new TextView(this);
        textViewLayoutParams.setMargins(
                30,
                15,
                30,
                0);
        resultTextView.setLayoutParams(textViewLayoutParams);
        resultTextView.setPadding(0, 0, 60, 0);

        resultTextView.setText(parts[1].replace(" ", ""));
        resultTextView.setTextColor(Color.BLACK);
        resultTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.history_result_size));
        resultTextView.setGravity(Gravity.END);

        resultTextView.setOnLongClickListener(v -> {
            try {
                TextView clickedTextView = (TextView) v;
                String clickedText = clickedTextView.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("", clickedText.replace("\n", " "));
                clipboardManager.setPrimaryClip(clipData);

                showToastShort(getString(R.string.historyCalculationCopied), getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        resultTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onDoubleClick(View v) {
                dataManager.deleteNameFromHistory(String.valueOf(i), getMainActivityContext());
                innerLinearLayout.removeView(findViewById(i));

                Log.e("DEBUG", String.valueOf(countLinearLayouts(innerLinearLayout)));
                if (countLinearLayouts(innerLinearLayout) == 0) {
                    dataManager.clearHistory(getMainActivityContext());
                    dataManager.saveToHistory("historyTextViewNumber", "0", getMainActivityContext());

                    ScrollView innerLinearLayout = findViewById(R.id.history_scrollview);
                    innerLinearLayout.removeAllViews();

                    TextView emptyTextView = findViewById(R.id.history_empty_textview);

                    if(emptyTextView != null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        createEmptyHistoryTextView();
                    }
                } else {
                    loadLayoutToUI(1);
                }
            }
        });

        resultHorizontalScrollView.addView(resultTextView);
        mainLinearLayout.addView(resultHorizontalScrollView);


        HorizontalScrollView calculationHorizontalScrollView = new HorizontalScrollView(this);
        calculationHorizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        calculationHorizontalScrollView.setHorizontalScrollBarEnabled(false);
        calculationHorizontalScrollView.setVerticalScrollBarEnabled(false);

        // calculation title
        TextView calculationTextViewTitle = new TextView(this);
        textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(
                30,
                10,
                30,
                0);

        calculationTextViewTitle.setLayoutParams(textViewLayoutParams);
        calculationTextViewTitle.setText(getString(R.string.historyCalculation));
        calculationTextViewTitle.setTextColor(Color.BLACK);
        calculationTextViewTitle.setTypeface(null, Typeface.BOLD);
        calculationTextViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        calculationTextViewTitle.setGravity(Gravity.START);
        mainLinearLayout.addView(calculationTextViewTitle);

        // calculation
        TextView calculationTextView = new TextView(this);
        textViewLayoutParams.setMargins(
                30,
                15,
                30,
                0);
        calculationTextView.setLayoutParams(textViewLayoutParams);
        calculationTextView.setPadding(0, 0, 60, 0);

        calculationTextView.setText(parts[0]);
        calculationTextView.setTextColor(Color.BLACK);
        calculationTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.history_result_size));
        calculationTextView.setGravity(Gravity.END);

        calculationTextView.setOnLongClickListener(v -> {
            try {
                TextView clickedTextView = (TextView) v;
                String clickedText = clickedTextView.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("", clickedText.replace("\n", " "));
                clipboardManager.setPrimaryClip(clipData);

                showToastShort(getString(R.string.historyCalculationCopied), getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        calculationTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onDoubleClick(View v) {
                dataManager.deleteNameFromHistory(String.valueOf(i), getMainActivityContext());
                innerLinearLayout.removeView(findViewById(i));

                Log.e("DEBUG", String.valueOf(countLinearLayouts(innerLinearLayout)));
                if (countLinearLayouts(innerLinearLayout) == 0) {
                    dataManager.clearHistory(getMainActivityContext());
                    dataManager.saveToHistory("historyTextViewNumber", "0", getMainActivityContext());

                    ScrollView innerLinearLayout = findViewById(R.id.history_scrollview);
                    innerLinearLayout.removeAllViews();

                    TextView emptyTextView = findViewById(R.id.history_empty_textview);

                    if(emptyTextView != null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        createEmptyHistoryTextView();
                    }
                } else {
                    loadLayoutToUI(1);
                }
            }
        });

        resultTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onSingleClick(View v) {
                final String calculationText = calculationTextView.getText().toString().replace(" ", "");
                final String resultText = resultTextView.getText().toString().replace(" ", "");

                try {
                    dataManager.saveToJSONSettings("calculate_text", calculationText.replace("=", ""), getMainActivityContext());
                    dataManager.saveToJSONSettings("result_text",  resultText.replace("=", ""), getMainActivityContext());

                    ToastHelper.showToastLong(getString(R.string.historySuccesfullMessageLoad), getApplicationContext());
                    mainActivity.resetIfPressedCalculate();
                } catch (Exception e) {
                    ToastHelper.showToastLong(getString(R.string.historyErrorMessageLoad), getApplicationContext());
                }
            }
        });

        calculationTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onSingleClick(View v) {
                final String calculationText = calculationTextView.getText().toString().replace(" ", "");
                final String resultText = resultTextView.getText().toString().replace(" ", "");

                try {
                    dataManager.saveToJSONSettings("calculate_text", calculationText.replace("=", ""), getMainActivityContext());
                    dataManager.saveToJSONSettings("result_text",  resultText.replace("=", ""), getMainActivityContext());

                    ToastHelper.showToastLong(getString(R.string.historySuccesfullMessageLoad), getApplicationContext());
                    mainActivity.resetIfPressedCalculate();
                } catch (Exception e) {
                    ToastHelper.showToastLong(getString(R.string.historyErrorMessageLoad), getApplicationContext());
                }
            }
        });

        calculationHorizontalScrollView.addView(calculationTextView);
        mainLinearLayout.addView(calculationHorizontalScrollView);

        HorizontalScrollView horizontalScrollView2 = new HorizontalScrollView(this);
        horizontalScrollView2.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        horizontalScrollView2.setHorizontalScrollBarEnabled(false);
        horizontalScrollView2.setVerticalScrollBarEnabled(false);

        // description title
        TextView textView3 = new TextView(this);
        textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(
                30,
                10,
                30,
                0);

        textView3.setLayoutParams(textViewLayoutParams);
        textView3.setText(getString(R.string.historyDescription));
        textView3.setTextColor(Color.BLACK);
        textView3.setTypeface(null, Typeface.BOLD);
        textView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView3.setGravity(Gravity.START);
        mainLinearLayout.addView(textView3);

        // description
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.setMargins(
                30,
                10,
                30,
                0);
        textView3.setPadding(0, 0, 30, 0);

        editText.setLayoutParams(editTextParams);
        editText.setHint(getString(R.string.historyDescriptionHint));
        editText.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        editText.setTextColor(Color.BLACK);
        editText.setGravity(Gravity.START);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setMaxLines(1);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String inputText = editText.getText().toString();
                dataManager.updateDetailsInHistoryData(String.valueOf(i), inputText, getMainActivityContext());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });

        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.clearFocus();
                return true;
            }
            return false;
        });

        if(!data.getString("details").equals("")) {
            editText.setText(data.getString("details"));
        }

        mainLinearLayout.addView(editText);
        mainLinearLayout.addView(createLine());

        return mainLinearLayout;
    }

    private LinearLayout createHistoryTextViewSingleEasy(int i) throws JSONException {
        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLinearLayout.setId(i);

        JSONObject data = dataManager.getHistoryData(String.valueOf(i), getMainActivityContext());

        if(data == null) {
            return null;
        }

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        horizontalScrollView.setVerticalScrollBarEnabled(false);

        TextView calculationTextView = new TextView(this);
        calculationTextView.setPadding(80, 20, 80, 20);

        calculationTextView.setText(data.getString("calculation").replace("=", " = "));
        calculationTextView.setTextColor(Color.BLACK);
        calculationTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.history_result_size));
        calculationTextView.setGravity(Gravity.START);

        calculationTextView.setOnLongClickListener(v -> {
            try {
                TextView clickedTextView = (TextView) v;
                String clickedText = clickedTextView.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("", clickedText.replace("\n", " "));
                clipboardManager.setPrimaryClip(clipData);

                showToastShort(getString(R.string.historyCalculationCopied), getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        calculationTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onSingleClick(View v) {
                final String calculationText = calculationTextView.getText().toString().replace(" ", "");
                String[] parts = calculationText.split("=");

                try {
                    dataManager.saveToJSONSettings("calculate_text", parts[0].replace("=", ""), getMainActivityContext());
                    dataManager.saveToJSONSettings("result_text",  parts[1].replace("=", ""), getMainActivityContext());

                    ToastHelper.showToastLong(getString(R.string.historySuccesfullMessageLoad), getApplicationContext());
                    mainActivity.resetIfPressedCalculate();
                } catch (Exception e) {
                    ToastHelper.showToastLong(getString(R.string.historyErrorMessageLoad), getApplicationContext());
                }
            }

            @Override
            public void onDoubleClick(View v) {
                dataManager.deleteNameFromHistory(String.valueOf(i), getMainActivityContext());
                innerLinearLayout.removeView(findViewById(i));

                Log.e("DEBUG", String.valueOf(countLinearLayouts(innerLinearLayout)));
                if (countLinearLayouts(innerLinearLayout) == 0) {
                    dataManager.clearHistory(getMainActivityContext());
                    dataManager.saveToHistory("historyTextViewNumber", "0", getMainActivityContext());

                    ScrollView innerLinearLayout = findViewById(R.id.history_scrollview);
                    innerLinearLayout.removeAllViews();

                    TextView emptyTextView = findViewById(R.id.history_empty_textview);

                    if(emptyTextView != null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        createEmptyHistoryTextView();
                    }
                } else {
                    loadLayoutToUI(1);
                }
            }
        });

        horizontalScrollView.addView(calculationTextView);
        mainLinearLayout.addView(horizontalScrollView);
        mainLinearLayout.addView(createLine());

        return mainLinearLayout;
    }

    private LinearLayout createHistoryTextViewMultipleAdvanced(int i) throws JSONException {
        JSONObject data = dataManager.getHistoryData(String.valueOf(i), getMainActivityContext());

        if(data == null) {
            return null;
        }
        final String[] parts = data.getString("calculation").split("=");
        AtomicBoolean clickListener = new AtomicBoolean(true);

        TextView emptyTextView = findViewById(R.id.history_empty_textview);
        if (emptyTextView != null) {
            emptyTextView.setVisibility(View.GONE);
        }

        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLinearLayout.setId(i);

        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(
                30,
                10,
                30,
                0);

        // date
        TextView timeDateTextView = new TextView(this);
        timeDateTextView.setLayoutParams(textViewLayoutParams);

        timeDateTextView.setText(data.getString("date"));
        timeDateTextView.setTextColor(Color.BLACK);
        timeDateTextView.setTypeface(null, Typeface.BOLD);
        timeDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        timeDateTextView.setGravity(Gravity.START);
        mainLinearLayout.addView(timeDateTextView);

        // result title
        TextView resultTextViewTitle = new TextView(this);
        textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(
                30,
                10,
                30,
                0);

        resultTextViewTitle.setLayoutParams(textViewLayoutParams);
        resultTextViewTitle.setText(getString(R.string.historyResult));
        resultTextViewTitle.setTextColor(Color.BLACK);
        resultTextViewTitle.setTypeface(null, Typeface.BOLD);
        resultTextViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        resultTextViewTitle.setGravity(Gravity.START);
        mainLinearLayout.addView(resultTextViewTitle);

        // result
        TextView resultTextView = new TextView(this);
        textViewLayoutParams.setMargins(
                30,
                15,
                30,
                0);
        resultTextView.setLayoutParams(textViewLayoutParams);
        resultTextView.setPadding(0, 0, 60, 0);

        resultTextView.setText(parts[1].replace(" ", ""));
        resultTextView.setTextColor(Color.BLACK);
        resultTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.history_result_size));
        resultTextView.setGravity(Gravity.START);

        resultTextView.setOnLongClickListener(v -> {
            try {
                TextView clickedTextView = (TextView) v;
                String clickedText = clickedTextView.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("", clickedText.replace("\n", " "));
                clipboardManager.setPrimaryClip(clipData);

                showToastShort(getString(R.string.historyCalculationCopied), getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        resultTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onDoubleClick(View v) {
                dataManager.deleteNameFromHistory(String.valueOf(i), getMainActivityContext());
                innerLinearLayout.removeView(findViewById(i));

                Log.e("DEBUG", String.valueOf(countLinearLayouts(innerLinearLayout)));
                if (countLinearLayouts(innerLinearLayout) == 0) {
                    dataManager.clearHistory(getMainActivityContext());
                    dataManager.saveToHistory("historyTextViewNumber", "0", getMainActivityContext());

                    ScrollView innerLinearLayout = findViewById(R.id.history_scrollview);
                    innerLinearLayout.removeAllViews();

                    TextView emptyTextView = findViewById(R.id.history_empty_textview);

                    if(emptyTextView != null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        createEmptyHistoryTextView();
                    }
                } else {
                    loadLayoutToUI(1);
                }
            }
        });

        mainLinearLayout.addView(resultTextView);

        // calculation title
        TextView calculationTextViewTitle = new TextView(this);
        textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(
                30,
                10,
                30,
                0);

        calculationTextViewTitle.setLayoutParams(textViewLayoutParams);
        calculationTextViewTitle.setText(getString(R.string.historyCalculation));
        calculationTextViewTitle.setTextColor(Color.BLACK);
        calculationTextViewTitle.setTypeface(null, Typeface.BOLD);
        calculationTextViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        calculationTextViewTitle.setGravity(Gravity.START);
        mainLinearLayout.addView(calculationTextViewTitle);

        // calculation
        TextView calculationTextView = new TextView(this);
        textViewLayoutParams.setMargins(
                30,
                15,
                30,
                0);
        calculationTextView.setLayoutParams(textViewLayoutParams);
        calculationTextView.setPadding(0, 0, 60, 0);

        calculationTextView.setText(parts[0]);
        calculationTextView.setTextColor(Color.BLACK);
        calculationTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.history_result_size));
        calculationTextView.setGravity(Gravity.START);

        calculationTextView.setOnLongClickListener(v -> {
            try {
                TextView clickedTextView = (TextView) v;
                String clickedText = clickedTextView.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("", clickedText.replace("\n", " "));
                clipboardManager.setPrimaryClip(clipData);

                showToastShort(getString(R.string.historyCalculationCopied), getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        calculationTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onDoubleClick(View v) {
                dataManager.deleteNameFromHistory(String.valueOf(i), getMainActivityContext());
                innerLinearLayout.removeView(findViewById(i));

                Log.e("DEBUG", String.valueOf(countLinearLayouts(innerLinearLayout)));
                if (countLinearLayouts(innerLinearLayout) == 0) {
                    dataManager.clearHistory(getMainActivityContext());
                    dataManager.saveToHistory("historyTextViewNumber", "0", getMainActivityContext());

                    ScrollView innerLinearLayout = findViewById(R.id.history_scrollview);
                    innerLinearLayout.removeAllViews();

                    TextView emptyTextView = findViewById(R.id.history_empty_textview);

                    if(emptyTextView != null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        createEmptyHistoryTextView();
                    }
                } else {
                    loadLayoutToUI(1);
                }
            }
        });

        resultTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onSingleClick(View v) {
                final String calculationText = calculationTextView.getText().toString().replace(" ", "");
                final String resultText = resultTextView.getText().toString().replace(" ", "");

                try {
                    dataManager.saveToJSONSettings("calculate_text", calculationText.replace("=", ""), getMainActivityContext());
                    dataManager.saveToJSONSettings("result_text",  resultText.replace("=", ""), getMainActivityContext());

                    ToastHelper.showToastLong(getString(R.string.historySuccesfullMessageLoad), getApplicationContext());
                    mainActivity.resetIfPressedCalculate();
                } catch (Exception e) {
                    ToastHelper.showToastLong(getString(R.string.historyErrorMessageLoad), getApplicationContext());
                }
            }
        });

        calculationTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onSingleClick(View v) {
                final String calculationText = calculationTextView.getText().toString().replace(" ", "");
                final String resultText = resultTextView.getText().toString().replace(" ", "");

                try {
                    dataManager.saveToJSONSettings("calculate_text", calculationText.replace("=", ""), getMainActivityContext());
                    dataManager.saveToJSONSettings("result_text",  resultText.replace("=", ""), getMainActivityContext());

                    ToastHelper.showToastLong(getString(R.string.historySuccesfullMessageLoad), getApplicationContext());
                    mainActivity.resetIfPressedCalculate();
                } catch (Exception e) {
                    ToastHelper.showToastLong(getString(R.string.historyErrorMessageLoad), getApplicationContext());
                }
            }
        });

        mainLinearLayout.addView(calculationTextView);

        HorizontalScrollView horizontalScrollView2 = new HorizontalScrollView(this);
        horizontalScrollView2.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        horizontalScrollView2.setHorizontalScrollBarEnabled(false);
        horizontalScrollView2.setVerticalScrollBarEnabled(false);

        // description title
        TextView textView3 = new TextView(this);
        textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(
                30,
                10,
                30,
                0);

        textView3.setLayoutParams(textViewLayoutParams);
        textView3.setText(getString(R.string.historyDescription));
        textView3.setTextColor(Color.BLACK);
        textView3.setTypeface(null, Typeface.BOLD);
        textView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView3.setGravity(Gravity.START);
        mainLinearLayout.addView(textView3);

        // description
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.setMargins(
                30,
                10,
                30,
                0);
        textView3.setPadding(0, 0, 30, 0);

        editText.setLayoutParams(editTextParams);
        editText.setHint(getString(R.string.historyDescriptionHint));
        editText.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        editText.setTextColor(Color.BLACK);
        editText.setGravity(Gravity.START);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setMaxLines(1);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String inputText = editText.getText().toString();
                dataManager.updateDetailsInHistoryData(String.valueOf(i), inputText, getMainActivityContext());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });

        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.clearFocus();
                return true;
            }
            return false;
        });


        if(!data.getString("details").equals("")) {
            editText.setText(data.getString("details"));
        }

        mainLinearLayout.addView(editText);
        mainLinearLayout.addView(createLine());

        return mainLinearLayout;
    }

    private LinearLayout createHistoryTextViewMultipleEasy(int i) throws JSONException {
        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLinearLayout.setId(i);

        JSONObject data = dataManager.getHistoryData(String.valueOf(i), getMainActivityContext());

        if(data == null) {
            return null;
        }

        TextView calculationTextView = new TextView(this);
        calculationTextView.setPadding(80, 20, 80, 20);

        calculationTextView.setText(data.getString("calculation").replace("=", " = "));
        calculationTextView.setTextColor(Color.BLACK);
        calculationTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.history_result_size));
        calculationTextView.setGravity(Gravity.START);

        calculationTextView.setOnLongClickListener(v -> {
            try {
                TextView clickedTextView = (TextView) v;
                String clickedText = clickedTextView.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("", clickedText.replace("\n", " "));
                clipboardManager.setPrimaryClip(clipData);

                showToastShort(getString(R.string.historyCalculationCopied), getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        calculationTextView.setOnClickListener(new ClickListener() {
            @Override
            public void onSingleClick(View v) {
                final String calculationText = calculationTextView.getText().toString().replace(" ", "");
                String[] parts = calculationText.split("=");

                try {
                    dataManager.saveToJSONSettings("calculate_text", parts[0].replace("=", ""), getMainActivityContext());
                    dataManager.saveToJSONSettings("result_text", parts[1].replace("=", ""), getMainActivityContext());

                    ToastHelper.showToastLong(getString(R.string.historySuccesfullMessageLoad), getApplicationContext());
                    mainActivity.resetIfPressedCalculate();
                } catch (Exception e) {
                    ToastHelper.showToastLong(getString(R.string.historyErrorMessageLoad), getApplicationContext());
                }
            }

            @Override
            public void onDoubleClick(View v) {
                dataManager.deleteNameFromHistory(String.valueOf(i), getMainActivityContext());
                innerLinearLayout.removeView(findViewById(i));

                Log.e("DEBUG", String.valueOf(countLinearLayouts(innerLinearLayout)));
                if (countLinearLayouts(innerLinearLayout) == 0) {
                    dataManager.clearHistory(getMainActivityContext());
                    dataManager.saveToHistory("historyTextViewNumber", "0", getMainActivityContext());

                    ScrollView innerLinearLayout = findViewById(R.id.history_scrollview);
                    innerLinearLayout.removeAllViews();

                    TextView emptyTextView = findViewById(R.id.history_empty_textview);

                    if(emptyTextView != null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        createEmptyHistoryTextView();
                    }
                } else {
                    loadLayoutToUI(1);
                }
            }
        });

        mainLinearLayout.addView(calculationTextView);
        mainLinearLayout.addView(createLine());

        return mainLinearLayout;
    }

    /**
     * Creates a thin line view for visual separation in the history.
     *
     * @return The created line view.
     */
    private View createLine() {
        // Create a thin line
        View line = new View(this);
        LinearLayout.LayoutParams lineLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.history_line_height)
        );
        lineLayoutParams.setMargins(60, 40, 60, 40);

        line.setLayoutParams(lineLayoutParams);
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.history_line_color));

        // Add an OnConfigurationChangedListener to update line color on configuration changes
        line.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateLineColor(line));
        line.setTag("line");

        return line;
    }

    /**
     * Updates the color of the line based on the current night mode.
     *
     * @param line The line view to update.
     */
    private void updateLineColor(View line) {
        int lineColor;
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        dataManager = new DataManager();

        String trueDarkMode = null;
        try {
            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (getSelectedSetting().equals("Systemstandard")) {
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                if ("true".equals(trueDarkMode)) {
                    lineColor = ContextCompat.getColor(this, R.color.darkmode_white);
                } else {
                    lineColor = ContextCompat.getColor(this, R.color.white);
                }
            } else {
                lineColor = ContextCompat.getColor(this, android.R.color.black);
            }
        } else if (getSelectedSetting().equals("Tageslichtmodus")) {
            lineColor = ContextCompat.getColor(this, android.R.color.black);
        } else {
            lineColor = ContextCompat.getColor(this, android.R.color.white);
        }

        // Set the updated color
        line.setBackgroundColor(lineColor);
    }

    /**
     * This method is responsible for creating and displaying an empty TextView in the history
     * section of the application. It first checks if a TextView already exists and removes it if
     * present. Then, it creates a new TextView with a specific message indicating an empty history.
     * The TextView is assigned a unique ID (R.id.history_empty_textview), and its text color is set
     * based on the current night mode and true dark mode. Additionally, the method sets the text size
     * and gravity for proper alignment. Finally, the newly created TextView is added to the layout.
     * <p>
     * Note: This method is typically called when the history section is empty, providing a visual
     * indication to the user that there is no history data available.
     */
    private void createEmptyHistoryTextView() {
        if(findViewById(R.id.history_empty_textview) == null) {
            TextView emptyTextView = new TextView(this);
            emptyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));
            emptyTextView.setId(R.id.history_empty_textview);
            emptyTextView.setText(getString(R.string.historyIsEmpty));
            emptyTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            emptyTextView.setTextSize(40f);
            emptyTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            LinearLayout parentLayout = findViewById(R.id.historyUI);
            parentLayout.addView(emptyTextView, 1);
        }

        TextView emptyTextView = findViewById(R.id.history_empty_textview);
        emptyTextView.setVisibility(View.VISIBLE);

        switchDisplayMode();
    }

    private void hideEmptyHistoryTextView() {
        createEmptyHistoryTextView();

        TextView textView = findViewById(R.id.history_empty_textview);
        textView.setVisibility(View.GONE);
    }

    private void createLoadingHistoryTextView() {
        if(findViewById(R.id.history_load_textview) == null) {
            TextView emptyTextView = new TextView(this);
            emptyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));
            emptyTextView.setId(R.id.history_load_textview);
            emptyTextView.setText(getString(R.string.historyLoadText));
            emptyTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            emptyTextView.setTextSize(40f);
            emptyTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            LinearLayout parentLayout = findViewById(R.id.historyUI);
            parentLayout.addView(emptyTextView, 1);
        }

        TextView emptyTextView = findViewById(R.id.history_load_textview);
        emptyTextView.setVisibility(View.VISIBLE);

        switchDisplayMode();
    }

    /**
     * Diese Methode aktualisiert die Textfarbe des TextView basierend auf dem aktuellen Nachtmodus und den Einstellungen.
     *
     * @param textView Der TextView, dessen Textfarbe aktualisiert werden soll.
     */
    private void updateTextViewColor(TextView textView) {
        dataManager = new DataManager();

        int textColor;
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        String trueDarkMode;
        try {
            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (getSelectedSetting().equals("Systemstandard")) {
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                if ("true".equals(trueDarkMode)) {
                    textColor = ContextCompat.getColor(this, R.color.darkmode_white);
                } else {
                    textColor = ContextCompat.getColor(this, R.color.white);
                }
            } else {
                textColor = ContextCompat.getColor(this, android.R.color.black);
            }
        } else if (getSelectedSetting().equals("Tageslichtmodus")) {
            textColor = ContextCompat.getColor(this, android.R.color.black);
        } else {
            textColor = ContextCompat.getColor(this, android.R.color.white);
        }

        final int finalTextColor = textColor;
        runOnUiThread(() -> textView.setTextColor(finalTextColor));
    }

    /**
     * Resets the names and values in the UI and performs background actions.
     */
    private void resetNamesAndValues() {
        if(findViewById(R.id.history_empty_textview) == null || findViewById(R.id.history_empty_textview).getVisibility() == View.GONE) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.confirm_delete, null);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

            popupView.setBackgroundColor(newColorBTNBackgroundAccent);

            TextView textViewTitle = popupView.findViewById(R.id.confirm_delete_layout_title);
            TextView textViewDelete = popupView.findViewById(R.id.deleteConfirmButton);
            TextView textViewCancel = popupView.findViewById(R.id.cancelConfirmButton);

            if (popupView.findViewById(R.id.confirmOutline) != null) {
                LinearLayout confirmOutline = popupView.findViewById(R.id.confirmOutline);
                Drawable backgroundDrawable = getResources().getDrawable(R.drawable.textview_border_thick);

                if (backgroundDrawable instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) backgroundDrawable;
                    gradientDrawable.setStroke(10, newColorBTNForegroundAccent);

                    confirmOutline.setBackground(backgroundDrawable);
                }
            }

            textViewTitle.setTextColor(newColorBTNForegroundAccent);
            textViewDelete.setTextColor(newColorBTNForegroundAccent);
            textViewCancel.setTextColor(newColorBTNForegroundAccent);

            textViewTitle.setBackgroundColor(newColorBTNBackgroundAccent);
            textViewDelete.setBackgroundColor(newColorBTNBackgroundAccent);
            textViewCancel.setBackgroundColor(newColorBTNBackgroundAccent);

            popupWindow.showAtLocation(findViewById(R.id.historyUI), Gravity.CENTER, 0, 0);

            TextView delete = popupView.findViewById(R.id.deleteConfirmButton);
            TextView cancel = popupView.findViewById(R.id.cancelConfirmButton);

            if(delete != null && cancel != null) {
                delete.setOnClickListener(v -> {
                    dataManager.clearHistory(getMainActivityContext());
                    dataManager.saveToHistory("historyTextViewNumber", "0", getMainActivityContext());

                    ScrollView innerLinearLayout = findViewById(R.id.history_scrollview);
                    innerLinearLayout.removeAllViews();
                    createEmptyHistoryTextView();
                    popupWindow.dismiss();
                });

                cancel.setOnClickListener(v -> {
                    popupWindow.dismiss();
                });
            }
        }
    }


    private int countLinearLayouts(LinearLayout layout) {
        int count = 0;

        for (int i = 0; i < layout.getChildCount(); i++) {
            if (layout.getChildAt(i) instanceof LinearLayout) {
                count++;
                count += countLinearLayouts((LinearLayout) layout.getChildAt(i));
            }
        }

        return count;
    }

    private int countScrollViewLayouts(ScrollView layout) {
        int count = 0;

        for (int i = 0; i < layout.getChildCount(); i++) {
            if (layout.getChildAt(i) instanceof ScrollView) {
                count++;
                count += countScrollViewLayouts((ScrollView) layout.getChildAt(i));
            }
        }

        return count;
    }

    /**
     * This method is called when the configuration of the device changes.
     * @param newConfig The new device configuration.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switchDisplayMode();

        final TextView emptyHistoryTextView = findViewById(R.id.history_empty_textview);
        if (emptyHistoryTextView != null) {
            updateTextViewColor(emptyHistoryTextView);
        }
    }

    /**
     * This method is called when the back button is pressed.
     * It overrides the default behavior and returns to the calculator.
     */
    @Override
    public void onBackPressed() {
        returnToCalculator();
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
            Log.e("startBackgoundService", e.toString());
        }
    }

    // This method switches the display mode based on the current night mode.
    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchDisplayMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        @SuppressLint("CutPasteId") Button deleteButton = findViewById(R.id.history_delete_button);
        @SuppressLint("CutPasteId") Button returnButton = findViewById(R.id.history_return_button);
        ScrollView historyScrollView = findViewById(R.id.history_scrollview);
        TextView historyTitle = findViewById(R.id.history_title);
        @SuppressLint("CutPasteId") TextView historyReturnButton = findViewById(R.id.history_return_button);
        @SuppressLint("CutPasteId") TextView historyDeleteButton = findViewById(R.id.history_delete_button);

        dataManager = new DataManager();
        final String trueDarkMode;
        try {
            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (getSelectedSetting() != null && getSelectedSetting().equals("Systemstandard")) {
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_YES:
                    if(trueDarkMode.equals("true")) {
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);

                        deleteButton.setForeground(getDrawable(R.drawable.trash_true_darkmode));
                        returnButton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));

                        updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    } else if (trueDarkMode.equals("false")) {
                        newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                        newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                        deleteButton.setForeground(getDrawable(R.drawable.trash_light));
                        returnButton.setForeground(getDrawable(R.drawable.arrow_back_light));

                        updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
                    }
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
                    newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);

                    updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

                    historyReturnButton.setForeground(getDrawable(R.drawable.arrow_back));
                    historyDeleteButton.setForeground(getDrawable(R.drawable.trash));
                    break;
            }
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Tageslichtmodus"))  {
            newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.black);
            newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.white);
            updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);

            historyReturnButton.setForeground(getDrawable(R.drawable.arrow_back));
            historyDeleteButton.setForeground(getDrawable(R.drawable.trash));
        } else if (getSelectedSetting() != null && getSelectedSetting().equals("Dunkelmodus")) {
            if(trueDarkMode.equals("true")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.darkmode_white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.darkmode_black);

                deleteButton.setForeground(getDrawable(R.drawable.trash_true_darkmode));
                returnButton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));

                updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            } else if (trueDarkMode.equals("false")) {
                newColorBTNForegroundAccent = ContextCompat.getColor(context, R.color.white);
                newColorBTNBackgroundAccent = ContextCompat.getColor(context, R.color.black);

                deleteButton.setForeground(getDrawable(R.drawable.trash_light));
                returnButton.setForeground(getDrawable(R.drawable.arrow_back_light));

                updateUIAccordingToNightMode(historyScrollView, historyTitle, newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            }
        }
    }

    /**
     * This method updates the UI elements according to the night mode.
     *
     * @param historyScrollView           The history scroll view.
     * @param historyTextView             The history text view.
     * @param newColorBTNForegroundAccent The new color for the button foreground accent.
     * @param newColorBTNBackgroundAccent The new color for the button background accent.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUIAccordingToNightMode(ScrollView historyScrollView, TextView historyTextView, int newColorBTNForegroundAccent, int newColorBTNBackgroundAccent) {
        if (historyScrollView != null) {
            historyScrollView.setBackgroundColor(newColorBTNBackgroundAccent);
        }
        if (historyTextView != null) {
            historyTextView.setTextColor(newColorBTNForegroundAccent);
        }
        if (findViewById(R.id.history_empty_textview) != null) {
            TextView emptyTextView = findViewById(R.id.history_empty_textview);
            emptyTextView.setTextColor(newColorBTNForegroundAccent);
        }

        if (findViewById(R.id.history_load_textview) != null) {
            TextView loadTextView = findViewById(R.id.history_load_textview);
            loadTextView.setTextColor(newColorBTNForegroundAccent);
        }

        // Change the foreground and background colors of all buttons in your layout
        if (newColorBTNForegroundAccent != 0 && newColorBTNBackgroundAccent != 0) {
            changeButtonColors(findViewById(R.id.historyUI), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeTextViewColors(newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
            changeTextViewAndEditTextColors(newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
        }
    }

    /**
     * This method changes the colors of all buttons in a layout.
     * @param layout The layout containing the buttons.
     * @param foregroundColor The color to be used for the foreground.
     * @param backgroundColor The color to be used for the background.
     */
    private void changeButtonColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                // Skip color change if the view is tagged as history_line
                if (!"line".equals(v.getTag())) {
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
    }

    /**
     * This method is used to change the colors of the TextViews in a given layout.
     * @param foregroundColor The color to be set as the text color of the TextViews.
     *                        This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the TextViews and the layout.
     *                        This should be a resolved color, not a resource id.
     */
    private void changeTextViewColors(int foregroundColor, int backgroundColor) {
        ViewGroup layout = findViewById(R.id.historyUI).findViewById(R.id.history_scrollview);
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                // Skip color change if the view is tagged as history_line
                if (!"line".equals(v.getTag())) {
                    v.setBackgroundColor(backgroundColor);

                    // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                    if (v instanceof ViewGroup) {
                        changeTextViewColorsRecursive((ViewGroup) v, foregroundColor, backgroundColor);
                    }
                }
            }
        }
    }

    /**
     * This recursive method traverses a ViewGroup, typically a layout, and changes the foreground
     * and background colors of each child View. It takes as parameters the ViewGroup to traverse,
     * the new foreground color, and the new background color. For each child View, it sets the
     * background color and, if the child is a TextView, it also changes the text color. If the child
     * is itself a ViewGroup, the method is called recursively to ensure all nested Views are
     * processed.
     * <p>
     * Note: This method is useful for applying a consistent color scheme to all child Views within
     * a given layout, and it can be used, for example, to implement a dark mode or change the
     * appearance of a specific section of the UI.
     *
     * @param layout The ViewGroup to traverse and update colors.
     * @param foregroundColor The new color for the foreground (text color) of TextViews.
     * @param backgroundColor The new color for the background of Views.
     */
    private void changeTextViewColorsRecursive(ViewGroup layout, int foregroundColor, int backgroundColor) {
        // Iterate through each child View in the ViewGroup
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);

            // Set the background color for the current child View
            if (!"line".equals(v.getTag())) {
                v.setBackgroundColor(backgroundColor);

                // If the child is a TextView, change the foreground and background colors
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(foregroundColor);
                    v.setBackgroundColor(backgroundColor);
                }

                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                if (v instanceof ViewGroup) {
                    changeTextViewColorsRecursive((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }

    /**
     * This method is used to change the colors of the TextViews and EditTexts in a given layout.
     * @param textColor The color to be set as the text color of the TextViews and EditTexts.
     *                        This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the TextViews and the layout.
     *                        This should be a resolved color, not a resource id.
     */
    private void changeTextViewAndEditTextColors(int textColor, int backgroundColor) {
        ViewGroup layout = findViewById(R.id.historyUI).findViewById(R.id.history_scrollview);
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                // Check if the view is an EditText and change its text color
                if (!"line".equals(v.getTag())) {
                    if (v instanceof EditText) {
                        ((EditText) v).setHintTextColor(textColor);
                    }
                    v.setBackgroundColor(backgroundColor);

                    // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                    if (v instanceof ViewGroup) {
                        changeTextViewAndEditTextColorsRecursive((ViewGroup) v, Color.parseColor("#D5D5D5"), backgroundColor);
                    }
                }
            }
        }
    }

    /**
     * Recursive method to change the colors of the TextViews and EditTexts in nested ViewGroups.
     * @param viewGroup The ViewGroup whose children's text and background colors are to be changed.
     * @param textColor The color to be set as the text color of the TextViews and EditTexts.
     *                        This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the TextViews and the layout.
     *                        This should be a resolved color, not a resource id.
     */
    private void changeTextViewAndEditTextColorsRecursive(ViewGroup viewGroup, int textColor, int backgroundColor) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            // Check if the view is an EditText and change its text color
            if (!"line".equals(v.getTag())) {
                if (v instanceof EditText) {
                    ((EditText) v).setHintTextColor(textColor);
                }
                v.setBackgroundColor(backgroundColor);

                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                if (v instanceof ViewGroup) {
                    changeTextViewAndEditTextColorsRecursive((ViewGroup) v, textColor, backgroundColor);
                }
            }
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
            setting = dataManager.getJSONSettingsData("selectedSpinnerSetting", getMainActivityContext()).getString("value");
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
            default:
                // Handle unexpected settings or return a default value
                return "Systemstandard";
        }
        // Handle the case when the setting is not found in the JSON file
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     * It also switches the display mode based on the current night mode.
     */
    public void returnToCalculator() {
        dataManager.saveToJSONSettings("lastActivity", "Main", getApplicationContext());
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            //switchDisplayMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}