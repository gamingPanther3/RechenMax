 package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.ToastHelper.*;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.text.InputType;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * HistoryActivity - Displays calculation history.
 * @author Max Lemberg
 * @version 1.4.4
 * @date 27.12.2023
 */
public class HistoryActivity extends AppCompatActivity {

    // Declare a Context object and initialize it to this instance
    private final Context context = this;
    // Declare a DataManager object
    DataManager dataManager;
    // Declare a static MainActivity object
    @SuppressLint("StaticFieldLeak")
    private static MainActivity mainActivity;
    private LinearLayout innerLinearLayout;

    private final int ITEMS_PER_LOAD = 10;
    private int historyTextViewNumber = 0;
    private int currentHistoryTextViewNumber;
    private boolean isEndReached = false;

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.
     *     Otherwise, it is null.
     */
    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        dataManager = new DataManager();
        try {
            historyTextViewNumber = Integer.parseInt(dataManager.getJSONSettingsData("historyTextViewNumber", getMainActivityContext()).getString("value"));
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
        LinearLayout outerLinearLayout = findViewById(R.id.history_scroll_linearlayout);

        // Create an instance of the inner LinearLayout
        innerLinearLayout = new LinearLayout(this);
        innerLinearLayout.setOrientation(LinearLayout.VERTICAL);

        // Set the inner LinearLayout as the content of the outer LinearLayout
        outerLinearLayout.addView(innerLinearLayout);

        // Use a separate thread to create TextViews in the background
        // Using ExecutorService instead of AsyncTask

        createLoadingHistoryTextView();
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                return historyTextViewNumber;
            }

            @Override
            protected void onPostExecute(Integer value) {
                if (value == null) {
                    dataManager.saveToJSONSettings("historyTextViewNumber", "0", getApplicationContext());
                }

                if (value == 0) {
                    TextView loadTextView = findViewById(R.id.history_load_textview);
                    if(loadTextView != null) {
                        loadTextView.setVisibility(View.GONE);
                    }
                    createEmptyHistoryTextView();
                    switchDisplayMode();
                } else {
                    hideEmptyHistoryTextView();

                    for (int i = historyTextViewNumber; i >= Math.max(0, historyTextViewNumber - (2 * ITEMS_PER_LOAD)); i--) {
                        currentHistoryTextViewNumber = i;
                        try {
                            if (dataManager.getJSONSettingsData("historyMode", getMainActivityContext()).getString("value").equals("multiple")) {
                                LinearLayout linearLayout;
                                try {
                                    linearLayout = createHistoryTextViewMultiple(i);
                                    if(linearLayout != null) {
                                        innerLinearLayout.addView(linearLayout, innerLinearLayout.getChildCount());
                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                LinearLayout linearLayout;
                                try {
                                    linearLayout = createHistoryTextViewSingle(i);
                                    if(linearLayout != null) {
                                        innerLinearLayout.addView(linearLayout, innerLinearLayout.getChildCount());
                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
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
            if (isEndOfScrollView(historyScrollView) && !isEndReached) {
                isEndReached = true;
                updateUI();
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
        return diff <= 200;
    }

    /**
     * Updates the UI by creating and adding TextViews for each history entry.
     */
    private void updateUI() {
        // Perform UI updates here
        //innerLinearLayout.removeAllViews();

        if (historyTextViewNumber != 0 && currentHistoryTextViewNumber <= historyTextViewNumber) {
            int loadedItems = 0;
            for (int i = currentHistoryTextViewNumber - 1; i >= Math.max(0, currentHistoryTextViewNumber - ITEMS_PER_LOAD); i--) {
                if (i >= 0 && loadedItems < ITEMS_PER_LOAD) {
                    currentHistoryTextViewNumber = i;
                    try {
                        if (dataManager.getJSONSettingsData("historyMode", getMainActivityContext()).getString("value").equals("multiple")) {
                            try {
                                LinearLayout linearLayout = createHistoryTextViewSingle(i);
                                if (linearLayout != null) {
                                    innerLinearLayout.addView(linearLayout, innerLinearLayout.getChildCount());
                                    loadedItems++;
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            try {
                                LinearLayout linearLayout = createHistoryTextViewMultiple(i);
                                if (linearLayout != null) {
                                    innerLinearLayout.addView(linearLayout, innerLinearLayout.getChildCount());
                                    loadedItems++;
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    break;
                }
            }
            switchDisplayMode();
            Log.e("DEBUG", "childs:" + innerLinearLayout.getChildCount());
        }
    }

    /**
     * Creates a new TextView for displaying calculation history.
     *
     * @return The created TextView.
     */
    private LinearLayout createHistoryTextViewSingle(int i) throws JSONException {
        JSONObject data = dataManager.getHistoryData(String.valueOf(i), getMainActivityContext());

        if(data == null) {
            return null;
        }

        TextView emptyTextView = findViewById(R.id.history_empty_textview);
        if (emptyTextView != null) {
            emptyTextView.setVisibility(View.GONE);
        }

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setId(i);

        // date
        TextView textView1 = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(
                30,
                10,
                30,
                0);
        textView1.setLayoutParams(textViewParams);

        textView1.setText(data.getString("date"));
        textView1.setTextColor(Color.BLACK);
        textView1.setTypeface(null, Typeface.BOLD);
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView1.setGravity(Gravity.START);
        linearLayout.addView(textView1);

        HorizontalScrollView horizontalScrollView1 = new HorizontalScrollView(this);
        horizontalScrollView1.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // calculation
        TextView textView2 = new TextView(this);
        textViewParams.setMargins(
                30,
                15,
                30,
                0);
        textView2.setLayoutParams(textViewParams);
        textView2.setPadding(0, 0, 60, 0);

        textView2.setText(data.getString("calculation"));
        textView2.setTextColor(Color.BLACK);
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.history_result_size));
        textView2.setGravity(Gravity.END);

        AtomicBoolean clickListener = new AtomicBoolean(true);

        textView2.setOnLongClickListener(v -> {
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

        textView2.setOnClickListener(new ClickListener() {
            @Override
            public void onDoubleClick(View v) {
                dataManager.deleteNameFromHistory(String.valueOf(i), getMainActivityContext());
                innerLinearLayout.removeView(findViewById(i));

                if (innerLinearLayout.getChildCount() == 1) {
                    resetNamesAndValues();
                    TextView emptyTextView = findViewById(R.id.history_empty_textview);
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSingleClick(View v) {
                if (clickListener.get()) {
                    // Output the text of the clicked TextView to the console
                    dataManager.saveToJSONSettings("pressedCalculate", false, getMainActivityContext());
                    TextView clickedTextView = (TextView) v;
                    String clickedText = clickedTextView.getText().toString();

                    // Split at "=" character
                    String[] parts = clickedText.split("=");

                    // Check and remove leading and trailing spaces
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        try {
                            if (dataManager.getJSONSettingsData("calculationMode", getMainActivityContext()).getString("value").equals("Vereinfacht")) {
                                dataManager.saveToJSONSettings("calculate_text", key.replace(" ", ""), getMainActivityContext());
                                dataManager.saveToJSONSettings("result_text", value.replace(" ", ""), getMainActivityContext());
                            } else {
                                dataManager.saveToJSONSettings("calculate_text", key, getMainActivityContext());
                                dataManager.saveToJSONSettings("result_text", value, getMainActivityContext());
                            }
                            dataManager.saveToJSONSettings("removeValue", false, getMainActivityContext());
                            dataManager.saveToJSONSettings("rotate_op", true, getMainActivityContext());

                            showToastShort(getString(R.string.historyCalculationSave), getApplicationContext());
                        } catch (Exception e) {
                            Log.i("createHistoryTextView", String.valueOf(e));
                        }
                    }
                }
                clickListener.set(true);
            }
        });

        horizontalScrollView1.addView(textView2);
        linearLayout.addView(horizontalScrollView1);

        HorizontalScrollView horizontalScrollView2 = new HorizontalScrollView(this);
        horizontalScrollView2.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        // description title
        TextView textView3 = new TextView(this);
        textViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(
                30,
                10,
                30,
                0);

        textView3.setLayoutParams(textViewParams);
        textView3.setText(getString(R.string.historyDescription));
        textView3.setTextColor(Color.BLACK);
        textView3.setTypeface(null, Typeface.BOLD);
        textView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView3.setGravity(Gravity.START);
        linearLayout.addView(textView3);

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

        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                final String inputText = textView.getText().toString();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.clearFocus();

                dataManager.updateDetailsInHistoryData(String.valueOf(i), inputText, getMainActivityContext());

                return true;
            }
            return false;
        });

        if(!data.getString("details").equals("")) {
            editText.setText(data.getString("details"));
        }

        linearLayout.addView(editText);
        linearLayout.addView(createLine());

        return linearLayout;
    }

    private LinearLayout createHistoryTextViewMultiple(int i) throws JSONException {
        JSONObject data = dataManager.getHistoryData(String.valueOf(i), getMainActivityContext());

        if(data == null) {
            return null;
        }
        TextView emptyTextView = findViewById(R.id.history_empty_textview);
        if (emptyTextView != null) {
            emptyTextView.setVisibility(View.GONE);
        }

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setId(i);

        // date
        TextView textView1 = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(
                30,
                10,
                30,
                0);
        textView1.setLayoutParams(textViewParams);

        textView1.setText(data.getString("date"));
        textView1.setTextColor(Color.BLACK);
        textView1.setTypeface(null, Typeface.BOLD);
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView1.setGravity(Gravity.START);
        linearLayout.addView(textView1);

        // calculation
        TextView textView2 = new TextView(this);
        textViewParams.setMargins(
                30,
                15,
                30,
                0);
        textView2.setLayoutParams(textViewParams);
        textView2.setPadding(0, 0, 60, 0);

        textView2.setText(data.getString("calculation"));
        textView2.setTextColor(Color.BLACK);
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.history_result_size));
        textView2.setGravity(Gravity.START);

        AtomicBoolean clickListener = new AtomicBoolean(true);

        textView2.setOnLongClickListener(v -> {
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

        textView2.setOnClickListener(new ClickListener() {
            @Override
            public void onDoubleClick(View v) {
                dataManager.deleteNameFromHistory(String.valueOf(i), getMainActivityContext());
                //Log.e("DEBUG", String.valueOf(dataManager.getAllData(getMainActivityContext())));

                if (innerLinearLayout.getChildCount() == 1) {
                    resetNamesAndValues();
                    TextView emptyTextView = findViewById(R.id.history_empty_textview);
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    recreate();
                }
            }

            @Override
            public void onSingleClick(View v) {
                if (clickListener.get()) {
                    // Output the text of the clicked TextView to the console
                    dataManager.saveToJSONSettings("pressedCalculate", false, getMainActivityContext());
                    TextView clickedTextView = (TextView) v;
                    String clickedText = clickedTextView.getText().toString();

                    // Split at "=" character
                    String[] parts = clickedText.split("=");

                    // Check and remove leading and trailing spaces
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        try {
                            if (dataManager.getJSONSettingsData("calculationMode", getMainActivityContext()).getString("value").equals("Vereinfacht")) {
                                dataManager.saveToJSONSettings("calculate_text", key.replace(" ", ""), getMainActivityContext());
                                dataManager.saveToJSONSettings("result_text", value.replace(" ", ""), getMainActivityContext());
                            } else {
                                dataManager.saveToJSONSettings("calculate_text", key, getMainActivityContext());
                                dataManager.saveToJSONSettings("result_text", value, getMainActivityContext());
                            }
                            dataManager.saveToJSONSettings("removeValue", false, getMainActivityContext());
                            dataManager.saveToJSONSettings("rotate_op", true, getMainActivityContext());

                            showToastShort(getString(R.string.historyCalculationSave), getApplicationContext());
                        } catch (Exception e) {
                            Log.i("createHistoryTextView", String.valueOf(e));
                        }
                    }
                }
                clickListener.set(true);
            }
        });

        linearLayout.addView(textView2);

        // description title
        TextView textView3 = new TextView(this);
        textViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(
                30,
                10,
                30,
                0);

        textView3.setLayoutParams(textViewParams);
        textView3.setText(getString(R.string.historyDescription));
        textView3.setTextColor(Color.BLACK);
        textView3.setTypeface(null, Typeface.BOLD);
        textView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView3.setGravity(Gravity.START);
        linearLayout.addView(textView3);

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

        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                final String inputText = textView.getText().toString();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.clearFocus();

                dataManager.updateDetailsInHistoryData(String.valueOf(i), inputText, getMainActivityContext());

                return true;
            }
            return false;
        });

        if(!data.getString("details").equals("")) {
            editText.setText(data.getString("details"));
        }

        linearLayout.addView(editText);
        linearLayout.addView(createLine());

        return linearLayout;
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
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            emptyTextView.setId(R.id.history_empty_textview);
            emptyTextView.setText(getString(R.string.historyIsEmpty));
            emptyTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            emptyTextView.setTextSize(35f);
            emptyTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            LinearLayout linearLayout = findViewById(R.id.history_scroll_linearlayout);
            linearLayout.addView(emptyTextView);
        }

        TextView emptyTextView = findViewById(R.id.history_empty_textview);
        emptyTextView.setVisibility(View.VISIBLE);

        switchDisplayMode();
    }

    private void createLoadingHistoryTextView() {
        if(findViewById(R.id.history_load_textview) == null) {
            TextView loadTextView = new TextView(this);
            loadTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            loadTextView.setId(R.id.history_load_textview);
            loadTextView.setText(getString(R.string.historyLoadText));
            loadTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            loadTextView.setTextSize(35f);
            loadTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            LinearLayout linearLayout = findViewById(R.id.history_scroll_linearlayout);
            linearLayout.addView(loadTextView);
        }

        TextView loadTextView = findViewById(R.id.history_load_textview);
        loadTextView.setVisibility(View.VISIBLE);

        switchDisplayMode();
    }

    /**
     * Diese Methode entfernt die leere TextView aus dem Layout, falls vorhanden.
     */
    private void hideEmptyHistoryTextView() {
        LinearLayout linearLayout = findViewById(R.id.history_scroll_linearlayout);

        // Ein neues TextView erstellen und einrichten
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(getString(R.string.historyIsEmpty));
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTextSize(35);
        textView.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.CENTER_HORIZONTAL);

        // TextView zum LinearLayout hinzufügen
        linearLayout.addView(textView);

        textView.setVisibility(View.GONE);
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

        // Änderung: Führe UI-Änderungen auf dem UI-Thread aus
        final int finalTextColor = textColor;
        runOnUiThread(() -> textView.setTextColor(finalTextColor));
    }

    /**
     * Resets the names and values in the UI and performs background actions.
     */
    private void resetNamesAndValues() {
        if(countLinearLayouts(findViewById(R.id.history_scroll_linearlayout)) > 1) {
            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.confirm_delete, null);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

            popupWindow.showAtLocation(findViewById(R.id.historyUI), Gravity.CENTER, 0, 0);

            TextView delete = popupView.findViewById(R.id.deleteConfirmButton);
            TextView cancel = popupView.findViewById(R.id.cancelConfirmButton);

            if(delete != null && cancel != null) {
                delete.setOnClickListener(v -> {
                    dataManager.clearHistory(getMainActivityContext());
                    dataManager.saveToJSONSettings("historyTextViewNumber", "0", getMainActivityContext());

                    LinearLayout innerLinearLayout = findViewById(R.id.history_scroll_linearlayout);
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

        int newColorBTNForegroundAccent;
        int newColorBTNBackgroundAccent;

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
        ViewGroup layout = findViewById(R.id.historyUI).findViewById(R.id.history_scroll_linearlayout);
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
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            //switchDisplayMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}