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

import static com.mlprograms.rechenmax.Converter.Category.ANGLE;
import static com.mlprograms.rechenmax.Converter.Category.AREA;
import static com.mlprograms.rechenmax.Converter.Category.CURRENT;
import static com.mlprograms.rechenmax.Converter.Category.DATA;
import static com.mlprograms.rechenmax.Converter.Category.ENERGY;
import static com.mlprograms.rechenmax.Converter.Category.LENGTH;
import static com.mlprograms.rechenmax.Converter.Category.MASS;
import static com.mlprograms.rechenmax.Converter.Category.PRESSURE;
import static com.mlprograms.rechenmax.Converter.Category.SPEED;
import static com.mlprograms.rechenmax.Converter.Category.TEMPERATURE;
import static com.mlprograms.rechenmax.Converter.Category.TIME;
import static com.mlprograms.rechenmax.Converter.Category.TORQUE;
import static com.mlprograms.rechenmax.Converter.Category.VOLTAGE;
import static com.mlprograms.rechenmax.Converter.Category.VOLUME;
import static com.mlprograms.rechenmax.Converter.Category.WORK;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.*;
import static com.mlprograms.rechenmax.MainActivity.isInvalidInput;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

public class ConvertActivity extends AppCompatActivity {

    DataManager dataManager;
    public static MainActivity mainActivity;

    private Spinner customSpinnerMode;
    private Spinner customSpinnerMeasurement;
    private EditText customEditText;
    
    private ArrayList<CustomItems> customList = new ArrayList<>();
    private ArrayList<CustomItems> customItemListAngle = new ArrayList<>();
    private ArrayList<CustomItems> customItemListArea = new ArrayList<>();
    private ArrayList<CustomItems> customItemListStorage = new ArrayList<>();
    private ArrayList<CustomItems> customItemListDistance = new ArrayList<>();
    private ArrayList<CustomItems> customItemListVolume = new ArrayList<>();
    private ArrayList<CustomItems> customItemListMass = new ArrayList<>();
    private ArrayList<CustomItems> customItemListTime = new ArrayList<>();
    private ArrayList<CustomItems> customItemListTemperature = new ArrayList<>();
    private ArrayList<CustomItems> customItemListVoltage = new ArrayList<>();
    private ArrayList<CustomItems> customItemListCurrent = new ArrayList<>();
    private ArrayList<CustomItems> customItemListSpeed = new ArrayList<>();
    private ArrayList<CustomItems> customItemListEnergy = new ArrayList<>();
    private ArrayList<CustomItems> customItemListPressure = new ArrayList<>();
    private ArrayList<CustomItems> customItemListTorque = new ArrayList<>();
    private ArrayList<CustomItems> customItemListWork = new ArrayList<>();

    private CustomAdapter customAdapter;
    private CustomAdapter customAdapterMeasurement;

    private boolean firstStart = true;

    private LayoutInflater inflater;
    private LinearLayout outherLinearLayout = null;

    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass onCreate method
        super.onCreate(savedInstanceState);
        stopBackgroundService();
        dataManager = new DataManager();
        setContentView(R.layout.convert);

        setUpButtonListeners();
        setUpCustomItemLists();

        inflater = getLayoutInflater();

        // convert mode spinner
        customSpinnerMode = findViewById(R.id.convertCustomSpinner);
        customSpinnerMeasurement = findViewById(R.id.convertSpinnerMessurement);
        customEditText = findViewById(R.id.convertEditTextNumber);

        customList = new ArrayList<>();
        customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area));
        customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard));
        customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle));
        customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder));
        customList.add(new CustomItems(getString(R.string.convertMassWeigth), R.drawable.mass_weigh));
        customList.add(new CustomItems(getString(R.string.convertTime), R.drawable.time));
        customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.temperature));
        customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.voltage));
        customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.current));
        customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.speed));
        customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.energy));
        customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.compare_arrows));
        customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.advanced));
        customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.settings));

        customAdapter = new CustomAdapter(this, customList);

        if(customSpinnerMode != null) {
            customSpinnerMode.setAdapter(customAdapter);
        }

        try {
            final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");
            final int pos = Integer.parseInt(dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Current"));
            final String number = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Number");

            switch (mode) {
                case "Winkel":
                    customSpinnerMode.setSelection(0);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListAngle);
                    break;
                case "Fläche":
                    customSpinnerMode.setSelection(1);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListArea);
                    break;
                case "Speicher":
                    customSpinnerMode.setSelection(2);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListStorage);
                    break;
                case "Entfernung":
                    customSpinnerMode.setSelection(3);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListDistance);
                    break;
                case "Volumen":
                    customSpinnerMode.setSelection(4);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListVolume);
                    break;
                case "MasseGewicht":
                    customSpinnerMode.setSelection(5);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListMass);
                    break;
                case "Zeit":
                    customSpinnerMode.setSelection(6);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListTime);
                    break;
                case "Temperatur":
                    customSpinnerMode.setSelection(7);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListTemperature);
                    break;
                case "StromSpannung":
                    customSpinnerMode.setSelection(8);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListVoltage);
                    break;
                case "StromStärke":
                    customSpinnerMode.setSelection(9);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListCurrent);
                    break;
                case "Geschwindigkeit":
                    customSpinnerMode.setSelection(10);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListSpeed);
                    break;
                case "Energie":
                    customSpinnerMode.setSelection(11);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListEnergy);
                    break;
                case "Druck":
                    customSpinnerMode.setSelection(12);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListPressure);
                    break;
                case "Drehmoment":
                    customSpinnerMode.setSelection(13);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListTorque);
                    break;
                default: /* Arbeit */
                    customSpinnerMode.setSelection(14);
                    customAdapterMeasurement = new CustomAdapter(this, customItemListWork);
                    break;
            }

            customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
            customAdapterMeasurement.notifyDataSetChanged();
            customSpinnerMeasurement.setSelection(pos);
            customEditText.setText(number);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        assert customSpinnerMode != null;
        customSpinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CustomItems items = (CustomItems) adapterView.getSelectedItem();
                String spinnerText = items.getSpinnerText();

                String new_value = "";
                if(spinnerText.equals(getString(R.string.convertAngle))) {
                    new_value = "Winkel";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListAngle);
                } else if(spinnerText.equals(getString(R.string.convertArea))) {
                    new_value = "Fläche";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListArea);
                } else if(spinnerText.equals(getString(R.string.convertStorage))) {
                    new_value = "Speicher";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListStorage);
                } else if(spinnerText.equals(getString(R.string.convertDistance))) {
                    new_value = "Entfernung";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListDistance);
                } else if(spinnerText.equals(getString(R.string.convertVolume))) {
                    new_value = "Volumen";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListVolume);
                } else if(spinnerText.equals(getString(R.string.convertMassWeigth))) {
                    new_value = "MasseGewicht";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListMass);
                } else if(spinnerText.equals(getString(R.string.convertTime))) {
                    new_value = "Zeit";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListTime);
                } else if(spinnerText.equals(getString(R.string.convertTemperature))) {
                    new_value = "Temperatur";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListTemperature);
                } else if(spinnerText.equals(getString(R.string.convertVoltage))) {
                    new_value = "StromSpannung";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListVoltage);
                } else if(spinnerText.equals(getString(R.string.convertCurrent))) {
                    new_value = "StromStärke";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListCurrent);
                } else if(spinnerText.equals(getString(R.string.convertSpeed))) {
                    new_value = "Geschwindigkeit";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListSpeed);
                } else if(spinnerText.equals(getString(R.string.convertPressure))) {
                    new_value = "Druck";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListPressure);
                } else if(spinnerText.equals(getString(R.string.convertTorque))) {
                    new_value = "Drehmoment";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListTorque);
                } else if(spinnerText.equals(getString(R.string.convertWork))) {
                    new_value = "Arbeit";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListWork);
                } else if(spinnerText.equals(getString(R.string.convertEnergy))) {
                    new_value = "Energie";
                    customAdapterMeasurement = new CustomAdapter(getApplicationContext(), customItemListEnergy);
                }

                final String mode;
                final int pos;
                final String number;
                try {
                    mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");
                    pos = Integer.parseInt(dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Current"));
                    number = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Number");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                customEditText.setText(number);

                if(!mode.equals(new_value) || firstStart) {
                    if(customSpinnerMeasurement != null) {
                        customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                        customSpinnerMeasurement.setSelection(pos);
                    }

                    firstStart = false;
                    changeConvertModes(spinnerText);
                    switchDisplayMode();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        EditText editText = findViewById(R.id.convertEditTextNumber);
        editText.setMaxLines(1);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence chars, int start, int before, int count) {
                if(!chars.equals("")) {
                    calculateAndSetText();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String inputText = s.toString();
                try {
                    final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");
                    dataManager.updateValuesInJSONSettingsData(
                            "convertMode",
                            mode + "Number",
                            inputText,
                            getMainActivityContext()
                    );
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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

        customSpinnerMeasurement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");

                    dataManager.updateValuesInJSONSettingsData(
                            "convertMode",
                            mode + "Current",
                            String.valueOf(position),
                            getMainActivityContext()
                    );
                    calculateAndSetText();

                    //Log.e("DEBUG", dataManager.getAllDataFromJSONSettings(getMainActivityContext()).toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        switchDisplayMode();
        calculateAndSetText();
    }

    public static String formatResultTextAfterType(String text) {
        String[] newText2 = text.split(" ");
        String newText = newText2[0].replace(".", ",");

        if(newText.isEmpty() || newText.matches("\\s*[,\\.0]*\\s*")) {
            return "0,00" + " " + newText2[1];
        }

        // Check if input newText is not null and not invalid
        if (newText != null && !isInvalidInput(newText)) {
            // Check if the number is negative
            boolean isNegative = newText.startsWith("-");
            if (isNegative) {
                // If negative, remove the negative sign for further processing
                newText = newText.substring(1);
            }

            // Check for scientific notation
            if (newText.toLowerCase().matches(".*[eE].*")) {
                try {
                    // Convert scientific notation to BigDecimal with increased precision
                    BigDecimal bigDecimalResult = new BigDecimal(newText.replace(".", "").replace(",", "."), MathContext.DECIMAL128);
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

                    // Add negative sign if necessary
                    if (isNegative) {
                        formattedNumber = "-" + formattedNumber;
                    }

                    // Recursively call the method
                    return formatResultTextAfterType(formattedNumber.replace("E", "e")) + " " + newText2[1];
                } catch (NumberFormatException e) {
                    // Handle invalid number format in scientific notation
                    System.out.println("Invalid number format: " + newText);
                    // Return original newText
                    return newText + " " + newText2[1];
                }
            }

            // Handle non-scientific notation
            int index = newText.indexOf(',');
            String result;
            String result2;
            if (index != -1) {
                // Split the newText into integral and fractional parts
                result = newText.substring(0, index).replace(".", "");
                result2 = newText.substring(index);
            } else {
                result = newText.replace(".", "");
                result2 = "";
            }

            // Check for invalid input
            if (!isInvalidInput(newText)) {
                // Format the integral part using DecimalFormat
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();

                // default: German, French, Spanish
                symbols.setDecimalSeparator(',');
                symbols.setGroupingSeparator('.');

                DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
                try {
                    BigDecimal bigDecimalResult1 = new BigDecimal(result, MathContext.DECIMAL128);
                    String formattedNumber1 = decimalFormat.format(bigDecimalResult1);

                    // Return the formatted result
                    return (isNegative ? "-" : "") + formattedNumber1 + result2  + " " + newText2[1];
                } catch (NumberFormatException e) {
                    // Handle invalid number format in the integral part
                    System.out.println("Invalid number format: " + result);
                    // Return original newText
                    return newText + " " + newText2[1];
                }
            }
        }
        // Return original newText if invalid or null
        return newText + " " + newText2[1];
    }

    @SuppressLint("SetTextI18n")
    private void calculateAndSetText() {
        if(outherLinearLayout != null) {
            try {
                final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");

                EditText editText = findViewById(R.id.convertEditTextNumber);
                String editTextNumber2 = editText.getText().toString().replace(".", "").replace(",", ".").replace(" ", "");

                if (editTextNumber2.matches("\\s*[,\\.0]*\\s*")) {
                    editTextNumber2 = "0.00";
                } else if (editTextNumber2.equals("-")) {
                    editTextNumber2 = "-0.00";
                }

                double editTextNumber = Double.parseDouble(editTextNumber2);

                Spinner spinner = findViewById(R.id.convertSpinnerMessurement);
                switch (mode) {
                    case "Winkel":
                        TextView convertDeg = findViewById(R.id.convertDegTextView);
                        TextView convertRad = findViewById(R.id.convertRadTextView);

                        Converter angleConverter = new Converter(ANGLE, DEGREE);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                angleConverter = new Converter(ANGLE, DEGREE);
                                break;
                            case 1:
                                angleConverter = new Converter(ANGLE, RADIAN);
                                break;
                            default:
                                convertDeg.setText("0,00");
                                convertRad.setText("0,00");
                                break;
                        }

                        convertDeg.setText(     formatResultTextAfterType(angleConverter.convertToString(editTextNumber, DEGREE)));
                        convertRad.setText(     formatResultTextAfterType(angleConverter.convertToString(editTextNumber, RADIAN)));
                        break;
                    case "Fläche" /* Fläche */:
                        TextView convertSquareMicrometer = findViewById(R.id.convertSquareMicrometerTextView);
                        TextView convertSquareMillimeter = findViewById(R.id.convertSquareMillimeterTextView);
                        TextView convertSquareCentimeter = findViewById(R.id.convertSquareCentimeterTextView);
                        TextView convertSquareMeter = findViewById(R.id.convertSquareMeterTextView);
                        TextView convertSquareKilometer = findViewById(R.id.convertSquareKilometerTextView);
                        TextView convertAr = findViewById(R.id.convertArTextView);
                        TextView convertHectares = findViewById(R.id.convertHectaresTextView);
                        TextView convertSquareInch = findViewById(R.id.convertSquareInchTextView);
                        TextView convertSquareFeet = findViewById(R.id.convertSquareFeetTextView);
                        TextView convertAcre = findViewById(R.id.convertAcreTextView);

                        Converter areaConverter = new Converter(AREA, SQUARE_MICROMETER);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                areaConverter = new Converter(AREA, SQUARE_MICROMETER);
                                break;
                            case 1:
                                areaConverter = new Converter(AREA, SQUARE_MILLIMETER);
                                break;
                            case 2:
                                areaConverter = new Converter(AREA, SQUARE_CENTIMETER);
                                break;
                            case 3:
                                areaConverter = new Converter(AREA, SQUARE_METER);
                                break;
                            case 4:
                                areaConverter = new Converter(AREA, SQUARE_KILOMETER);
                                break;
                            case 5:
                                areaConverter = new Converter(AREA, ARES);
                                break;
                            case 6:
                                areaConverter = new Converter(AREA, HECTARE);
                                break;
                            case 7:
                                areaConverter = new Converter(AREA, SQUARE_INCH);
                                break;
                            case 8:
                                areaConverter = new Converter(AREA, SQUARE_FOOT);
                                break;
                            case 9:
                                areaConverter = new Converter(AREA, ACRE);
                                break;
                            default:
                                convertSquareMicrometer.setText("0,00");
                                convertSquareMillimeter.setText("0,00");
                                convertSquareCentimeter.setText("0,00");
                                convertSquareMeter.setText("0,00");
                                convertSquareKilometer.setText("0,00");
                                convertAr.setText("0,00");
                                convertHectares.setText("0,00");
                                convertSquareInch.setText("0,00");
                                convertSquareFeet.setText("0,00");
                                convertAcre.setText("0,00");
                                break;
                        }

                        convertSquareMicrometer.setText(   formatResultTextAfterType(areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER)));
                        convertSquareMillimeter.setText(   formatResultTextAfterType(areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER)));
                        convertSquareCentimeter.setText(   formatResultTextAfterType(areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER)));
                        convertSquareMeter.setText(        formatResultTextAfterType(areaConverter.convertToString(editTextNumber, SQUARE_METER)));
                        convertSquareKilometer.setText(    formatResultTextAfterType(areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER)));
                        convertAr.setText(                 formatResultTextAfterType(areaConverter.convertToString(editTextNumber, ARES)));
                        convertHectares.setText(           formatResultTextAfterType(areaConverter.convertToString(editTextNumber, HECTARE)));
                        convertSquareInch.setText(         formatResultTextAfterType(areaConverter.convertToString(editTextNumber, SQUARE_INCH)));
                        convertSquareFeet.setText(         formatResultTextAfterType(areaConverter.convertToString(editTextNumber, SQUARE_FOOT)));
                        convertAcre.setText(               formatResultTextAfterType(areaConverter.convertToString(editTextNumber, ACRE)));
                        break;
                    case "Speicher" /* Speicher */:
                        TextView convertBit = findViewById(R.id.convertBitTextView);
                        TextView convertByte = findViewById(R.id.convertByteTextView);
                        TextView convertKilobit = findViewById(R.id.convertKilobitTextView);
                        TextView convertKilobyte = findViewById(R.id.convertKilobyteTextView);
                        TextView convertMegabit = findViewById(R.id.convertMegabitTextView);
                        TextView convertMegabyte = findViewById(R.id.convertMegabyteTextView);
                        TextView convertGigabit = findViewById(R.id.convertGigabitTextView);
                        TextView convertGigabyte = findViewById(R.id.convertGigabyteTextView);
                        TextView convertTerabit = findViewById(R.id.convertTerabitTextView);
                        TextView convertTerabyte = findViewById(R.id.convertTerabyteTextView);
                        TextView convertPetabit = findViewById(R.id.convertPetabitTextView);
                        TextView convertPetabyte = findViewById(R.id.convertPetabyteTextView);
                        TextView convertExabit = findViewById(R.id.convertExabitTextView);
                        TextView convertExabyte = findViewById(R.id.convertExabyteTextView);
                        TextView convertZetabit = findViewById(R.id.convertZetabitTextView);
                        TextView convertZetabyte = findViewById(R.id.convertZetabyteTextView);
                        TextView convertYotabit = findViewById(R.id.convertYotabitTextView);
                        TextView convertYotabyte = findViewById(R.id.convertYotabyteTextView);

                        Converter storageConverter = new Converter(DATA, BIT);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                storageConverter = new Converter(DATA, BIT);
                                break;
                            case 1:
                                storageConverter = new Converter(DATA, BYTE);
                                break;
                            case 2:
                                storageConverter = new Converter(DATA, KILOBIT_B1000);
                                break;
                            case 3:
                                storageConverter = new Converter(DATA, KILOBYTE_B1000);
                                break;
                            case 4:
                                storageConverter = new Converter(DATA, MEGABIT_B1000);
                                break;
                            case 5:
                                storageConverter = new Converter(DATA, MEGABYTE_B1000);
                                break;
                            case 6:
                                storageConverter = new Converter(DATA, GIGABIT_B1000);
                                break;
                            case 7:
                                storageConverter = new Converter(DATA, GIGABYTE_B1000);
                                break;
                            case 8:
                                storageConverter = new Converter(DATA, TERABIT_B1000);
                                break;
                            case 9:
                                storageConverter = new Converter(DATA, TERABYTE_B1000);
                                break;
                            case 10:
                                storageConverter = new Converter(DATA, PETABIT_B1000);
                                break;
                            case 11:
                                storageConverter = new Converter(DATA, PETABYTE_B1000);
                                break;
                            case 12:
                                storageConverter = new Converter(DATA, EXABIT_B1000);
                                break;
                            case 13:
                                storageConverter = new Converter(DATA, EXABYTE_B1000);
                                break;
                            case 14:
                                storageConverter = new Converter(DATA, ZETABIT_B1000);
                                break;
                            case 15:
                                storageConverter = new Converter(DATA, ZETABYTE_B1000);
                                break;
                            case 16:
                                storageConverter = new Converter(DATA, YOTABIT_B1000);
                                break;
                            case 17:
                                storageConverter = new Converter(DATA, YOTABYTE_B1000);
                                break;
                            default:
                                convertBit.setText("0,00");
                                convertByte.setText("0,00");
                                convertKilobit.setText("0,00");
                                convertKilobyte.setText("0,00");
                                convertMegabit.setText("0,00");
                                convertMegabyte.setText("0,00");
                                convertGigabit.setText("0,00");
                                convertGigabyte.setText("0,00");
                                convertTerabit.setText("0,00");
                                convertTerabyte.setText("0,00");
                                convertPetabit.setText("0,00");
                                convertPetabyte.setText("0,00");
                                convertExabit.setText("0,00");
                                convertExabyte.setText("0,00");
                                convertZetabit.setText("0,00");
                                convertZetabyte.setText("0,00");
                                convertYotabit.setText("0,00");
                                convertYotabyte.setText("0,00");
                                break;
                        }

                        convertBit.setText(          formatResultTextAfterType(storageConverter.convertToString(editTextNumber, BIT)));
                        convertByte.setText(         formatResultTextAfterType(storageConverter.convertToString(editTextNumber, BYTE)));
                        convertKilobit.setText(      formatResultTextAfterType(storageConverter.convertToString(editTextNumber, KILOBIT_B1000)));
                        convertKilobyte.setText(     formatResultTextAfterType(storageConverter.convertToString(editTextNumber, KILOBYTE_B1000)));
                        convertMegabit.setText(      formatResultTextAfterType(storageConverter.convertToString(editTextNumber, MEGABIT_B1000)));
                        convertMegabyte.setText(     formatResultTextAfterType(storageConverter.convertToString(editTextNumber, MEGABYTE_B1000)));
                        convertGigabit.setText(      formatResultTextAfterType(storageConverter.convertToString(editTextNumber, GIGABIT_B1000)));
                        convertGigabyte.setText(     formatResultTextAfterType(storageConverter.convertToString(editTextNumber, GIGABYTE_B1000)));
                        convertTerabit.setText(      formatResultTextAfterType(storageConverter.convertToString(editTextNumber, TERABIT_B1000)));
                        convertTerabyte.setText(     formatResultTextAfterType(storageConverter.convertToString(editTextNumber, TERABYTE_B1000)));
                        convertPetabit.setText(      formatResultTextAfterType(storageConverter.convertToString(editTextNumber, PETABIT_B1000)));
                        convertPetabyte.setText(     formatResultTextAfterType(storageConverter.convertToString(editTextNumber, PETABYTE_B1000)));
                        convertExabit.setText(       formatResultTextAfterType(storageConverter.convertToString(editTextNumber, EXABIT_B1000)));
                        convertExabyte.setText(      formatResultTextAfterType(storageConverter.convertToString(editTextNumber, EXABYTE_B1000)));
                        convertZetabit.setText(      formatResultTextAfterType(storageConverter.convertToString(editTextNumber, ZETABIT_B1000)));
                        convertZetabyte.setText(     formatResultTextAfterType(storageConverter.convertToString(editTextNumber, ZETABYTE_B1000)));
                        convertYotabit.setText(      formatResultTextAfterType(storageConverter.convertToString(editTextNumber, YOTABIT_B1000)));
                        convertYotabyte.setText(     formatResultTextAfterType(storageConverter.convertToString(editTextNumber, YOTABYTE_B1000)));
                        break;
                    case "Entfernung" /* Entfernung */:
                        TextView convertAngstrom = findViewById(R.id.convertAngstromTextView);
                        TextView convertFemtometer = findViewById(R.id.convertFemtometerTextView);
                        TextView convertParsec = findViewById(R.id.convertParsecTextView);
                        TextView convertPixel = findViewById(R.id.convertPixelTextView);
                        TextView convertPoint = findViewById(R.id.convertPointTextView);
                        TextView convertPica = findViewById(R.id.convertPicaTextView);
                        TextView convertEm = findViewById(R.id.convertEmTextView);
                        TextView convertPikometer = findViewById(R.id.convertPikometerTextView);
                        TextView convertNanometer = findViewById(R.id.convertNanometerTextView);
                        TextView convertMikrometer = findViewById(R.id.convertMikrometerTextView);
                        TextView convertMillimeter = findViewById(R.id.convertMillimeterTextView);
                        TextView convertCentimeter = findViewById(R.id.convertCentimeterTextView);
                        TextView convertDezimeter = findViewById(R.id.convertDezimeterTextView);
                        TextView convertMeter = findViewById(R.id.convertMeterTextView);
                        TextView convertHektometer = findViewById(R.id.convertHektometerTextView);
                        TextView convertKilometer = findViewById(R.id.convertKilometerTextView);
                        TextView convertFeet = findViewById(R.id.convertFeetTextView);
                        TextView convertYard = findViewById(R.id.convertYardTextView);
                        TextView convertInch = findViewById(R.id.convertInchTextView);
                        TextView convertMiles = findViewById(R.id.convertMilesTextView);
                        TextView convertSeamiles = findViewById(R.id.convertSeamilesTextView);
                        TextView convertLightyear = findViewById(R.id.convertLightyearTextView);

                        Converter distanceConverter = new Converter(LENGTH, ANGSTROM);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                distanceConverter = new Converter(LENGTH, ANGSTROM);
                                break;
                            case 1:
                                distanceConverter = new Converter(LENGTH, FEMTOMETER);
                                break;
                            case 2:
                                distanceConverter = new Converter(LENGTH, PARSEC);
                                break;
                            case 3:
                                distanceConverter = new Converter(LENGTH, PIXEL);
                                break;
                            case 4:
                                distanceConverter = new Converter(LENGTH, POINT);
                                break;
                            case 5:
                                distanceConverter = new Converter(LENGTH, PICA);
                                break;
                            case 6:
                                distanceConverter = new Converter(LENGTH, EM);
                                break;
                            case 7:
                                distanceConverter = new Converter(LENGTH, PICOMETER);
                                break;
                            case 8:
                                distanceConverter = new Converter(LENGTH, NANOMETER);
                                break;
                            case 9:
                                distanceConverter = new Converter(LENGTH, MICROMETER);
                                break;
                            case 10:
                                distanceConverter = new Converter(LENGTH, MILLIMETER);
                                break;
                            case 11:
                                distanceConverter = new Converter(LENGTH, CENTIMETER);
                                break;
                            case 12:
                                distanceConverter = new Converter(LENGTH, DECIMETER);
                                break;
                            case 13:
                                distanceConverter = new Converter(LENGTH, METER);
                                break;
                            case 14:
                                distanceConverter = new Converter(LENGTH, HECTOMETER);
                                break;
                            case 15:
                                distanceConverter = new Converter(LENGTH, KILOMETER);
                                break;
                            case 16:
                                distanceConverter = new Converter(LENGTH, FEET);
                                break;
                            case 17:
                                distanceConverter = new Converter(LENGTH, YARD);
                                break;
                            case 18:
                                distanceConverter = new Converter(LENGTH, INCHES);
                                break;
                            case 19:
                                distanceConverter = new Converter(LENGTH, MILES);
                                break;
                            case 20:
                                distanceConverter = new Converter(LENGTH, NAUTICAL_MILES);
                                break;
                            case 21:
                                distanceConverter = new Converter(LENGTH, LIGHT_YEAR);
                                break;
                            default:
                                convertAngstrom.setText("0,00");
                                convertFemtometer.setText("0,00");
                                convertParsec.setText("0,00");
                                convertPixel.setText("0,00");
                                convertPoint.setText("0,00");
                                convertPica.setText("0,00");
                                convertEm.setText("0,00");
                                convertPikometer.setText("0,00");
                                convertNanometer.setText("0,00");
                                convertMikrometer.setText("0,00");
                                convertMillimeter.setText("0,00");
                                convertCentimeter.setText("0,00");
                                convertDezimeter.setText("0,00");
                                convertMeter.setText("0,00");
                                convertHektometer.setText("0,00");
                                convertKilometer.setText("0,00");
                                convertFeet.setText("0,00");
                                convertYard.setText("0,00");
                                convertInch.setText("0,00");
                                convertMiles.setText("0,00");
                                convertSeamiles.setText("0,00");
                                convertLightyear.setText("0,00");
                        }

                        convertAngstrom.setText(       formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, ANGSTROM)));
                        convertFemtometer.setText(     formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, FEMTOMETER)));
                        convertParsec.setText(         formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, PARSEC)));
                        convertPixel.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, PIXEL)));
                        convertPoint.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, POINT)));
                        convertPica.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, PICA)));
                        convertEm.setText(             formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, EM)));
                        convertPikometer.setText(      formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, PICOMETER)));
                        convertNanometer.setText(      formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, NANOMETER)));
                        convertMikrometer.setText(     formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, MICROMETER)));
                        convertMillimeter.setText(     formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, MILLIMETER)));
                        convertCentimeter.setText(     formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, CENTIMETER)));
                        convertDezimeter.setText(      formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, DECIMETER)));
                        convertMeter.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, METER)));
                        convertHektometer.setText(     formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, HECTOMETER)));
                        convertKilometer.setText(      formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, KILOMETER)));
                        convertFeet.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, FEET)));
                        convertYard.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, YARD)));
                        convertInch.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, INCHES)));
                        convertMiles.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, MILES)));
                        convertSeamiles.setText(       formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, NAUTICAL_MILES)));
                        convertLightyear.setText(      formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, LIGHT_YEAR)));
                        break;
                    case "Volumen" /* Volumen */:
                        TextView convertMilliliter = findViewById(R.id.convertMilliliterTextView);
                        TextView convertLiter = findViewById(R.id.convertLiterTextView);
                        TextView convertKubikmillimeter = findViewById(R.id.convertKubikmillimeterTextView);
                        TextView convertKubikmeter = findViewById(R.id.convertKubikmeterTextView);
                        TextView convertKubikInch = findViewById(R.id.convertKubikInchTextView);
                        TextView convertKubikFeet = findViewById(R.id.convertKubikFeetTextView);
                        TextView convertGallonUS = findViewById(R.id.convertGallonUSTextView);

                        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikmillimeter)));
                        customItemListVolume.add(new CustomItems(getString(R.string.convertMilliliter)));
                        customItemListVolume.add(new CustomItems(getString(R.string.convertLiter)));
                        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikmeter)));
                        customItemListVolume.add(new CustomItems(getString(R.string.convertGallonUS)));
                        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikFeet)));
                        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikInch)));

                        Converter volumeConverter = new Converter(VOLUME, CUBIC_MILLIMETER);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                volumeConverter = new Converter(VOLUME, CUBIC_MILLIMETER);
                                break;
                            case 1:
                                volumeConverter = new Converter(VOLUME, MILLILITER);
                                break;
                            case 2:
                                volumeConverter = new Converter(VOLUME, LITER);
                                break;
                            case 3:
                                volumeConverter = new Converter(VOLUME, CUBIC_METER);
                                break;
                            case 4:
                                volumeConverter = new Converter(VOLUME, GALLON);
                                break;
                            case 5:
                                volumeConverter = new Converter(VOLUME, CUBIC_FEET);
                                break;
                            case 6:
                                volumeConverter = new Converter(VOLUME, CUBIC_INCH);
                                break;
                            default:
                                convertMilliliter.setText("0,00");
                                convertLiter.setText("0,00");
                                convertKubikmillimeter.setText("0,00");
                                convertKubikmeter.setText("0,00");
                                convertKubikInch.setText("0,00");
                                convertKubikFeet.setText("0,00");
                                convertGallonUS.setText("0,00");
                        }

                        convertMilliliter.setText(       formatResultTextAfterType(volumeConverter.convertToString(editTextNumber, MILLILITER)));
                        convertLiter.setText(            formatResultTextAfterType(volumeConverter.convertToString(editTextNumber, LITER)));
                        convertKubikmillimeter.setText(  formatResultTextAfterType(volumeConverter.convertToString(editTextNumber, CUBIC_MILLIMETER)));
                        convertKubikmeter.setText(       formatResultTextAfterType(volumeConverter.convertToString(editTextNumber, CUBIC_METER)));
                        convertKubikInch.setText(        formatResultTextAfterType(volumeConverter.convertToString(editTextNumber, CUBIC_INCH)));
                        convertKubikFeet.setText(        formatResultTextAfterType(volumeConverter.convertToString(editTextNumber, CUBIC_FEET)));
                        convertGallonUS.setText(         formatResultTextAfterType(volumeConverter.convertToString(editTextNumber, GALLON)));
                        break;
                    case "MasseGewicht":
                        TextView convertFemtogramm = findViewById(R.id.convertFemtogrammTextView);
                        TextView convertPicogramm = findViewById(R.id.convertPicogrammTextView);
                        TextView convertNanogramm = findViewById(R.id.convertNanogrammTextView);
                        TextView convertMikrogramm = findViewById(R.id.convertMikrogrammTextView);
                        TextView convertMilligramm = findViewById(R.id.convertMilligrammTextView);
                        TextView convertGramm = findViewById(R.id.convertGrammTextView);
                        TextView convertKilogramm = findViewById(R.id.convertKilogrammTextView);
                        TextView convertTonne= findViewById(R.id.convertTonneTextView);
                        TextView convertUnzen= findViewById(R.id.convertUnzenTextView);
                        TextView convertPfund= findViewById(R.id.convertPfundTextView);

                        Converter massWeightConverter = new Converter(MASS, MILLILITER);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                massWeightConverter = new Converter(MASS, FEMTOGRAM);
                                break;
                            case 1:
                                massWeightConverter = new Converter(MASS, PICOGRAM);
                                break;
                            case 2:
                                massWeightConverter = new Converter(MASS, NANOGRAM);
                                break;
                            case 3:
                                massWeightConverter = new Converter(MASS, MICROGRAM);
                                break;
                            case 4:
                                massWeightConverter = new Converter(MASS, MILLIGRAM);
                                break;
                            case 5:
                                massWeightConverter = new Converter(MASS, GRAM);
                                break;
                            case 6:
                                massWeightConverter = new Converter(MASS, KILOGRAM);
                                break;
                            case 7:
                                massWeightConverter = new Converter(MASS, TON);
                                break;
                            case 8:
                                massWeightConverter = new Converter(MASS, OUNCE);
                                break;
                            case 9:
                                massWeightConverter = new Converter(MASS, POUND);
                                break;
                            default:
                                convertFemtogramm.setText("0,00");
                                convertPicogramm.setText("0,00");
                                convertNanogramm.setText("0,00");
                                convertMikrogramm.setText("0,00");
                                convertMilligramm.setText("0,00");
                                convertGramm.setText("0,00");
                                convertKilogramm.setText("0,00");
                                convertTonne.setText("0,00");
                                convertUnzen.setText("0,00");
                                convertPfund.setText("0,00");
                        }

                        convertFemtogramm.setText(    formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, FEMTOGRAM)));
                        convertPicogramm.setText(     formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, PICOGRAM)));
                        convertNanogramm.setText(     formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, NANOGRAM)));
                        convertMikrogramm.setText(    formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, MICROGRAM)));
                        convertMilligramm.setText(    formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, MILLIGRAM)));
                        convertGramm.setText(         formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, GRAM)));
                        convertKilogramm.setText(     formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, KILOGRAM)));
                        convertTonne.setText(         formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, TON)));
                        convertUnzen.setText(         formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, OUNCE)));
                        convertPfund.setText(         formatResultTextAfterType(massWeightConverter.convertToString(editTextNumber, POUND)));
                        break;
                    case "Zeit":
                        TextView convertWoche = findViewById(R.id.convertWocheTextView);
                        TextView convertTag = findViewById(R.id.convertTagTextView);
                        TextView convertStunde = findViewById(R.id.convertStundeTextView);
                        TextView convertMinute = findViewById(R.id.convertMinuteTextView);
                        TextView convertSekunde = findViewById(R.id.convertSekundeTextView);
                        TextView convertMillisekunde = findViewById(R.id.convertMillisekundeTextView);
                        TextView convertMikrosekunde = findViewById(R.id.convertMikrosekundeTextView);
                        TextView convertNanosekunde= findViewById(R.id.convertNanosekundeTextView);
                        TextView convertPicosekunde= findViewById(R.id.convertPicosekundeTextView);
                        TextView convertFemtosekunde= findViewById(R.id.convertFemtosekundeTextView);

                        Converter timeConverter = new Converter(TIME, FEMTOSECOND);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                timeConverter = new Converter(TIME, FEMTOSECOND);
                                break;
                            case 1:
                                timeConverter = new Converter(TIME, PICOSECOND);
                                break;
                            case 2:
                                timeConverter = new Converter(TIME, NANOSECOND);
                                break;
                            case 3:
                                timeConverter = new Converter(TIME, MICROSECOND);
                                break;
                            case 4:
                                timeConverter = new Converter(TIME, MILLISECOND);
                                break;
                            case 5:
                                timeConverter = new Converter(TIME, SECOND);
                                break;
                            case 6:
                                timeConverter = new Converter(TIME, MINUTE);
                                break;
                            case 7:
                                timeConverter = new Converter(TIME, HOUR);
                                break;
                            case 8:
                                timeConverter = new Converter(TIME, DAY);
                                break;
                            case 9:
                                timeConverter = new Converter(TIME, WEEK);
                                break;
                            default:
                                convertWoche.setText("0,00");
                                convertTag.setText("0,00");
                                convertStunde.setText("0,00");
                                convertMinute.setText("0,00");
                                convertSekunde.setText("0,00");
                                convertMillisekunde.setText("0,00");
                                convertMikrosekunde.setText("0,00");
                                convertNanosekunde.setText("0,00");
                                convertPicosekunde.setText("0,00");
                                convertFemtosekunde.setText("0,00");
                        }

                        convertWoche.setText(           formatResultTextAfterType(timeConverter.convertToString(editTextNumber, WEEK)));
                        convertTag.setText(             formatResultTextAfterType(timeConverter.convertToString(editTextNumber, DAY)));
                        convertStunde.setText(          formatResultTextAfterType(timeConverter.convertToString(editTextNumber, HOUR)));
                        convertMinute.setText(          formatResultTextAfterType(timeConverter.convertToString(editTextNumber, MINUTE)));
                        convertSekunde.setText(         formatResultTextAfterType(timeConverter.convertToString(editTextNumber, SECOND)));
                        convertMillisekunde.setText(    formatResultTextAfterType(timeConverter.convertToString(editTextNumber, MILLISECOND)));
                        convertMikrosekunde.setText(    formatResultTextAfterType(timeConverter.convertToString(editTextNumber, MICROSECOND)));
                        convertNanosekunde.setText(     formatResultTextAfterType(timeConverter.convertToString(editTextNumber, NANOSECOND)));
                        convertPicosekunde.setText(     formatResultTextAfterType(timeConverter.convertToString(editTextNumber, PICOSECOND)));
                        convertFemtosekunde.setText(    formatResultTextAfterType(timeConverter.convertToString(editTextNumber, FEMTOSECOND)));
                        break;
                    case "Temperatur":
                        TextView convertCelsius = findViewById(R.id.convertCelsiusTextView);
                        TextView convertKelvin = findViewById(R.id.convertKelvinTextView);
                        TextView convertFahrenheit = findViewById(R.id.convertFahrenheitTextView);

                        Converter temperatureConverter = new Converter(TEMPERATURE, CELSIUS);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                temperatureConverter = new Converter(TEMPERATURE, CELSIUS);
                                break;
                            case 1:
                                temperatureConverter = new Converter(TEMPERATURE, KELVIN);
                                break;
                            case 2:
                                temperatureConverter = new Converter(TEMPERATURE, FAHRENHEIT);
                                break;
                            default:
                                convertCelsius.setText("0,00");
                                convertKelvin.setText("0,00");
                                convertFahrenheit.setText("0,00");
                        }

                        convertCelsius.setText(         formatResultTextAfterType(temperatureConverter.convertToString(editTextNumber, CELSIUS)));
                        convertKelvin.setText(          formatResultTextAfterType(temperatureConverter.convertToString(editTextNumber, KELVIN)));
                        convertFahrenheit.setText(      formatResultTextAfterType(temperatureConverter.convertToString(editTextNumber, FAHRENHEIT)));
                        break;
                    case "StromSpannung":
                        TextView convertMillivolt = findViewById(R.id.convertMillivoltTextView);
                        TextView convertVolt = findViewById(R.id.convertVoltTextView);
                        TextView convertKilovolt = findViewById(R.id.convertKilovoltTextView);
                        TextView convertMegavolt = findViewById(R.id.convertMegavoltTextView);

                        Converter voltageConverter = new Converter(VOLTAGE, MILLIVOLT);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                voltageConverter = new Converter(VOLTAGE, MILLIVOLT);
                                break;
                            case 1:
                                voltageConverter = new Converter(VOLTAGE, VOLT);
                                break;
                            case 2:
                                voltageConverter = new Converter(VOLTAGE, KILOVOLT);
                                break;
                            case 3:
                                voltageConverter = new Converter(VOLTAGE, MEGAVOLT);
                                break;
                            default:
                                convertMillivolt.setText("0,00");
                                convertVolt.setText("0,00");
                                convertKilovolt.setText("0,00");
                                convertMegavolt.setText("0,00");
                        }

                        convertMillivolt.setText(     formatResultTextAfterType(voltageConverter.convertToString(editTextNumber, MILLIVOLT)));
                        convertVolt.setText(          formatResultTextAfterType(voltageConverter.convertToString(editTextNumber, VOLT)));
                        convertKilovolt.setText(      formatResultTextAfterType(voltageConverter.convertToString(editTextNumber, KILOVOLT)));
                        convertMegavolt.setText(      formatResultTextAfterType(voltageConverter.convertToString(editTextNumber, MEGAVOLT)));
                        break;
                    case "StromStärke":
                        TextView convertPicoampere = findViewById(R.id.convertPicoampereTextView);
                        TextView convertNanoampere = findViewById(R.id.convertNanoampereTextView);
                        TextView convertMikroampere = findViewById(R.id.convertMikroampereTextView);
                        TextView convertMilliampere = findViewById(R.id.convertMilliampereTextView);
                        TextView convertAmpere = findViewById(R.id.convertAmpereTextView);
                        TextView convertKiloAmpere = findViewById(R.id.convertKiloAmpereTextView);

                        Converter currentConverter = new Converter(CURRENT, PICOAMPERE);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                currentConverter = new Converter(CURRENT, PICOAMPERE);
                                break;
                            case 1:
                                currentConverter = new Converter(CURRENT, NANOAMPERE);
                                break;
                            case 2:
                                currentConverter = new Converter(CURRENT, MICROAMPERE);
                                break;
                            case 3:
                                currentConverter = new Converter(CURRENT, MILLIAMPERE);
                                break;
                            case 4:
                                currentConverter = new Converter(CURRENT, AMPERE);
                                break;
                            case 5:
                                currentConverter = new Converter(CURRENT, KILOAMPERE);
                                break;
                            default:
                                convertPicoampere.setText("0,00");
                                convertNanoampere.setText("0,00");
                                convertMikroampere.setText("0,00");
                                convertMilliampere.setText("0,00");
                                convertAmpere.setText("0,00");
                                convertKiloAmpere.setText("0,00");
                        }

                        convertPicoampere.setText(      formatResultTextAfterType(currentConverter.convertToString(editTextNumber, PICOAMPERE)));
                        convertNanoampere.setText(      formatResultTextAfterType(currentConverter.convertToString(editTextNumber, NANOAMPERE)));
                        convertMikroampere.setText(     formatResultTextAfterType(currentConverter.convertToString(editTextNumber, MICROAMPERE)));
                        convertMilliampere.setText(     formatResultTextAfterType(currentConverter.convertToString(editTextNumber, MILLIAMPERE)));
                        convertAmpere.setText(          formatResultTextAfterType(currentConverter.convertToString(editTextNumber, AMPERE)));
                        convertKiloAmpere.setText(      formatResultTextAfterType(currentConverter.convertToString(editTextNumber, KILOAMPERE)));
                        break;
                    case "Geschwindigkeit":
                        TextView convertMillimeterProSekunde = findViewById(R.id.convertMillimeterProSekundeTextView);
                        TextView convertMeterProSekunde = findViewById(R.id.convertMeterProSekundeTextView);
                        TextView convertKilometerProStunde = findViewById(R.id.convertKilometerProStundeTextView);
                        TextView convertMilesProStunde = findViewById(R.id.convertMilesProStundeTextView);
                        TextView convertKnoten = findViewById(R.id.convertKnotenTextView);
                        TextView convertMach = findViewById(R.id.convertMachTextView);

                        Converter speedConverter = new Converter(SPEED, MILLIMETER_PER_SECOND);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                speedConverter = new Converter(SPEED, MILLIMETER_PER_SECOND);
                                break;
                            case 1:
                                speedConverter = new Converter(SPEED, METER_PER_SECOND);
                                break;
                            case 2:
                                speedConverter = new Converter(SPEED, KILOMETER_PER_HOUR);
                                break;
                            case 3:
                                speedConverter = new Converter(SPEED, MILES_PER_HOUR);
                                break;
                            case 4:
                                speedConverter = new Converter(SPEED, KNOT);
                                break;
                            case 5:
                                speedConverter = new Converter(SPEED, MACH);
                                break;
                            default:
                                convertMillimeterProSekunde.setText("0,00");
                                convertMeterProSekunde.setText("0,00");
                                convertKilometerProStunde.setText("0,00");
                                convertMilesProStunde.setText("0,00");
                                convertKnoten.setText("0,00");
                                convertMach.setText("0,00");
                        }

                        convertMillimeterProSekunde.setText(    formatResultTextAfterType(speedConverter.convertToString(editTextNumber, MILLIMETER_PER_SECOND)));
                        convertMeterProSekunde.setText(         formatResultTextAfterType(speedConverter.convertToString(editTextNumber, METER_PER_SECOND)));
                        convertKilometerProStunde.setText(      formatResultTextAfterType(speedConverter.convertToString(editTextNumber, KILOMETER_PER_HOUR)));
                        convertMilesProStunde.setText(          formatResultTextAfterType(speedConverter.convertToString(editTextNumber, MILES_PER_HOUR)));
                        convertKnoten.setText(                  formatResultTextAfterType(speedConverter.convertToString(editTextNumber, KNOT)));
                        convertMach.setText(                    formatResultTextAfterType(speedConverter.convertToString(editTextNumber, MACH)));
                        break;
                    case "Energie":
                        TextView convertMillijoule = findViewById(R.id.convertMillijouleTextView);
                        TextView convertJoule = findViewById(R.id.convertJouleTextView);
                        TextView convertKilojoule = findViewById(R.id.convertKilojouleTextView);
                        TextView convertMegajoule = findViewById(R.id.convertMegajouleTextView);
                        TextView convertKalorie = findViewById(R.id.convertKalorieTextView);
                        TextView convertKilokalorie = findViewById(R.id.convertKilokalorieTextView);
                        TextView convertWattsekunde = findViewById(R.id.convertWattsekundeTextView);
                        TextView convertWattstunde = findViewById(R.id.convertWattstundeTextView);
                        TextView convertKilowattsekunde = findViewById(R.id.convertKilowattsekundeTextView);
                        TextView convertKilowattstunde = findViewById(R.id.convertKilowattstundeTextView);

                        Converter energyConverter = new Converter(ENERGY, MILLIJOULE);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                energyConverter = new Converter(ENERGY, MILLIJOULE);
                                break;
                            case 1:
                                energyConverter = new Converter(ENERGY, JOULE);
                                break;
                            case 2:
                                energyConverter = new Converter(ENERGY, KILOJOULE);
                                break;
                            case 3:
                                energyConverter = new Converter(ENERGY, MEGAJOULE);
                                break;
                            case 4:
                                energyConverter = new Converter(ENERGY, CALORY);
                                break;
                            case 5:
                                energyConverter = new Converter(ENERGY, KILOCALORY);
                                break;
                            case 6:
                                energyConverter = new Converter(ENERGY, WATT_SECOND);
                                break;
                            case 7:
                                energyConverter = new Converter(ENERGY, WATT_HOUR);
                                break;
                            case 8:
                                energyConverter = new Converter(ENERGY, KILOWATT_SECOND);
                                break;
                            case 9:
                                energyConverter = new Converter(ENERGY, KILOWATT_HOUR);
                                break;
                            default:
                                convertMillijoule.setText("0,00");
                                convertJoule.setText("0,00");
                                convertKilojoule.setText("0,00");
                                convertMegajoule.setText("0,00");
                                convertKalorie.setText("0,00");
                                convertKilokalorie.setText("0,00");
                                convertWattsekunde.setText("0,00");
                                convertWattstunde.setText("0,00");
                                convertKilowattsekunde.setText("0,00");
                                convertKilowattstunde.setText("0,00");
                        }

                        convertMillijoule.setText(              formatResultTextAfterType(energyConverter.convertToString(editTextNumber, MILLIJOULE)));
                        convertJoule.setText(                   formatResultTextAfterType(energyConverter.convertToString(editTextNumber, JOULE)));
                        convertKilojoule.setText(               formatResultTextAfterType(energyConverter.convertToString(editTextNumber, KILOJOULE)));
                        convertMegajoule.setText(               formatResultTextAfterType(energyConverter.convertToString(editTextNumber, MEGAJOULE)));
                        convertKalorie.setText(                 formatResultTextAfterType(energyConverter.convertToString(editTextNumber, CALORY)));
                        convertKilokalorie.setText(             formatResultTextAfterType(energyConverter.convertToString(editTextNumber, KILOCALORY)));
                        convertWattsekunde.setText(             formatResultTextAfterType(energyConverter.convertToString(editTextNumber, WATT_SECOND)));
                        convertWattstunde.setText(              formatResultTextAfterType(energyConverter.convertToString(editTextNumber, WATT_HOUR)));
                        convertKilowattsekunde.setText(         formatResultTextAfterType(energyConverter.convertToString(editTextNumber, KILOWATT_SECOND)));
                        convertKilowattstunde.setText(          formatResultTextAfterType(energyConverter.convertToString(editTextNumber, KILOWATT_HOUR)));
                        break;
                    case "Druck":
                        TextView convertMillipascal = findViewById(R.id.convertMillipascalTextView);
                        TextView convertPascal = findViewById(R.id.convertPascalTextView);
                        TextView convertHectopascal = findViewById(R.id.convertHectopascalTextView);
                        TextView convertKilopascal = findViewById(R.id.convertKilopascalTextView);
                        TextView convertBar = findViewById(R.id.convertBarTextView);
                        TextView convertMillibar = findViewById(R.id.convertMillibarTextView);
                        TextView convertTorr = findViewById(R.id.convertTorrTextView);
                        TextView convertPSI = findViewById(R.id.convertPSITextView);
                        TextView convertPSF = findViewById(R.id.convertPSFTextView);

                        Converter pressureConverter = new Converter(PRESSURE, MILLIPASCAL);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                pressureConverter = new Converter(PRESSURE, MILLIPASCAL);
                                break;
                            case 1:
                                pressureConverter = new Converter(PRESSURE, PASCAL);
                                break;
                            case 2:
                                pressureConverter = new Converter(PRESSURE, HECTOPASCAL);
                                break;
                            case 3:
                                pressureConverter = new Converter(PRESSURE, KILOPASCAL);
                                break;
                            case 4:
                                pressureConverter = new Converter(PRESSURE, BAR);
                                break;
                            case 5:
                                pressureConverter = new Converter(PRESSURE, MILLIBAR);
                                break;
                            case 6:
                                pressureConverter = new Converter(PRESSURE, TORR);
                                break;
                            case 7:
                                pressureConverter = new Converter(PRESSURE, PSI);
                                break;
                            case 8:
                                pressureConverter = new Converter(PRESSURE, PSF);
                                break;
                            default:
                                convertMillipascal.setText("0,00");
                                convertPascal.setText("0,00");
                                convertHectopascal.setText("0,00");
                                convertKilopascal.setText("0,00");
                                convertBar.setText("0,00");
                                convertMillibar.setText("0,00");
                                convertTorr.setText("0,00");
                                convertPSI.setText("0,00");
                                convertPSF.setText("0,00");
                        }

                        convertMillipascal.setText(       formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, MILLIPASCAL)));
                        convertPascal.setText(            formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, PASCAL)));
                        convertHectopascal.setText(       formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, HECTOPASCAL)));
                        convertKilopascal.setText(        formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, KILOPASCAL)));
                        convertBar.setText(               formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, BAR)));
                        convertMillibar.setText(          formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, MILLIBAR)));
                        convertTorr.setText(              formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, TORR)));
                        convertPSI.setText(               formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, PSI)));
                        convertPSF.setText(               formatResultTextAfterType(pressureConverter.convertToString(editTextNumber, PSF)));
                        break;
                    case "Drehmoment":
                        TextView convertNewtonMeter = findViewById(R.id.convertNewtonMeterTextView);
                        TextView convertMeterKilogramm = findViewById(R.id.convertMeterKilogrammTextView);
                        TextView convertFootPound = findViewById(R.id.convertFootPoundTextView);
                        TextView convertInchPound = findViewById(R.id.convertInchPoundTextView);

                        Converter torqueConverter = new Converter(TORQUE, NEWTON_METER);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                torqueConverter = new Converter(TORQUE, NEWTON_METER);
                                break;
                            case 1:
                                torqueConverter = new Converter(TORQUE, METER_KILOGRAM);
                                break;
                            case 2:
                                torqueConverter = new Converter(TORQUE, FOOT_POUND_FORCE);
                                break;
                            case 3:
                                torqueConverter = new Converter(TORQUE, INCH_POUND_FORCE);
                                break;
                            default:
                                convertNewtonMeter.setText("0,00");
                                convertMeterKilogramm.setText("0,00");
                                convertFootPound.setText("0,00");
                                convertInchPound.setText("0,00");
                        }

                        convertNewtonMeter.setText(     formatResultTextAfterType(torqueConverter.convertToString(editTextNumber, NEWTON_METER)));
                        convertMeterKilogramm.setText(  formatResultTextAfterType(torqueConverter.convertToString(editTextNumber, METER_KILOGRAM)));
                        convertFootPound.setText(       formatResultTextAfterType(torqueConverter.convertToString(editTextNumber, FOOT_POUND_FORCE)));
                        convertInchPound.setText(       formatResultTextAfterType(torqueConverter.convertToString(editTextNumber, INCH_POUND_FORCE)));
                        break;
                    case "Arbeit":
                        TextView convertMilliwatt = findViewById(R.id.convertMilliwattTextView);
                        TextView convertWatt = findViewById(R.id.convertWattTextView);
                        TextView convertKilowatt = findViewById(R.id.convertKilowattTextView);
                        TextView convertMegawatt = findViewById(R.id.convertMegawattTextView);
                        TextView convertGigawatt = findViewById(R.id.convertGigawattTextView);
                        TextView convertPferdestaerke = findViewById(R.id.convertPferdestärkeTextView);
                        TextView convertJouleProSekunde = findViewById(R.id.convertJouleProSekundeTextView);

                        Converter workConverter = new Converter(WORK, MILLIWATT);
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                workConverter = new Converter(WORK, MILLIWATT);
                                break;
                            case 1:
                                workConverter = new Converter(WORK, WATT);
                                break;
                            case 2:
                                workConverter = new Converter(WORK, KILOWATT);
                                break;
                            case 3:
                                workConverter = new Converter(WORK, MEGAWATT);
                                break;
                            case 4:
                                workConverter = new Converter(WORK, GIGAWATT);
                                break;
                            case 5:
                                workConverter = new Converter(WORK, HORSEPOWER);
                                break;
                            case 6:
                                workConverter = new Converter(WORK, JOULE_PER_SECOND);
                                break;
                            default:
                                convertMilliwatt.setText("0,00");
                                convertWatt.setText("0,00");
                                convertKilowatt.setText("0,00");
                                convertMegawatt.setText("0,00");
                                convertGigawatt.setText("0,00");
                                convertPferdestaerke.setText("0,00");
                                convertJouleProSekunde.setText("0,00");
                        }

                        convertMilliwatt.setText(           formatResultTextAfterType(workConverter.convertToString(editTextNumber, MILLIWATT)));
                        convertWatt.setText(                formatResultTextAfterType(workConverter.convertToString(editTextNumber, WATT)));
                        convertKilowatt.setText(            formatResultTextAfterType(workConverter.convertToString(editTextNumber, KILOWATT)));
                        convertMegawatt.setText(            formatResultTextAfterType(workConverter.convertToString(editTextNumber, MEGAWATT)));
                        convertGigawatt.setText(            formatResultTextAfterType(workConverter.convertToString(editTextNumber, GIGAWATT)));
                        convertPferdestaerke.setText(       formatResultTextAfterType(workConverter.convertToString(editTextNumber, HORSEPOWER)));
                        convertJouleProSekunde.setText(     formatResultTextAfterType(workConverter.convertToString(editTextNumber, JOULE_PER_SECOND)));
                        break;
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setUpCustomItemLists() {
        customItemListAngle.add(new CustomItems(getString(R.string.convertDeg)));
        customItemListAngle.add(new CustomItems(getString(R.string.convertRad)));

        customItemListArea.add(new CustomItems(getString(R.string.convertSquareMicrometer)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareMillimeter)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareCentimeter)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareMeter)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareKilometer)));
        customItemListArea.add(new CustomItems(getString(R.string.convertAr)));
        customItemListArea.add(new CustomItems(getString(R.string.convertHectares)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareInch)));
        customItemListArea.add(new CustomItems(getString(R.string.convertSquareFeet)));
        customItemListArea.add(new CustomItems(getString(R.string.convertAcre)));

        customItemListStorage.add(new CustomItems(getString(R.string.convertBit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertByte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertKilobit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertKilobyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertMegabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertMegabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertGigabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertGigabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertTerabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertTerabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertPetabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertPetabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertExabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertExabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertZetabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertZetabyte)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertYotabit)));
        customItemListStorage.add(new CustomItems(getString(R.string.convertYotabyte)));

        customItemListDistance.add(new CustomItems(getString(R.string.convertAngstrom)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertFemtometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertParsec)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertPixel)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertPoint)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertPica)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertEm)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertPikometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertNanometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertMikrometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertMillimeter)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertCentimeter)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertDezimeter)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertMeter)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertHektometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertKilometer)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertFeet)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertYard)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertInch)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertMiles)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertSeamiles)));
        customItemListDistance.add(new CustomItems(getString(R.string.convertLightyear)));

        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikmillimeter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertMilliliter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertLiter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikmeter)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertGallonUS)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikFeet)));
        customItemListVolume.add(new CustomItems(getString(R.string.convertKubikInch)));

        customItemListMass.add(new CustomItems(getString(R.string.convertFemtogramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertPicogramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertNanogramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertMikrogramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertMilligramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertGramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertKilogramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertTonne)));
        customItemListMass.add(new CustomItems(getString(R.string.convertUnzen)));
        customItemListMass.add(new CustomItems(getString(R.string.convertPfund)));

        customItemListTime.add(new CustomItems(getString(R.string.convertFemtosekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertPicosekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertNanosekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertMikrosekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertMillisekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertSekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertMinute)));
        customItemListTime.add(new CustomItems(getString(R.string.convertStunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertTag)));
        customItemListTime.add(new CustomItems(getString(R.string.convertWoche)));

        customItemListTemperature.add(new CustomItems(getString(R.string.convertCelsius)));
        customItemListTemperature.add(new CustomItems(getString(R.string.convertKelvin)));
        customItemListTemperature.add(new CustomItems(getString(R.string.convertFahrenheit)));

        customItemListVoltage.add(new CustomItems(getString(R.string.convertMillivolt)));
        customItemListVoltage.add(new CustomItems(getString(R.string.convertVolt)));
        customItemListVoltage.add(new CustomItems(getString(R.string.convertKilovolt)));
        customItemListVoltage.add(new CustomItems(getString(R.string.convertMegavolt)));

        customItemListCurrent.add(new CustomItems(getString(R.string.convertPicoampere)));
        customItemListCurrent.add(new CustomItems(getString(R.string.convertNanoampere)));
        customItemListCurrent.add(new CustomItems(getString(R.string.convertMikroampere)));
        customItemListCurrent.add(new CustomItems(getString(R.string.convertMilliampere)));
        customItemListCurrent.add(new CustomItems(getString(R.string.convertAmpere)));
        customItemListCurrent.add(new CustomItems(getString(R.string.convertKiloAmpere)));

        customItemListSpeed.add(new CustomItems(getString(R.string.convertMillimeterProSekunde)));
        customItemListSpeed.add(new CustomItems(getString(R.string.convertMeterProSekunde)));
        customItemListSpeed.add(new CustomItems(getString(R.string.convertKilometerProStunde)));
        customItemListSpeed.add(new CustomItems(getString(R.string.convertMilesProStunde)));
        customItemListSpeed.add(new CustomItems(getString(R.string.convertKnoten)));
        customItemListSpeed.add(new CustomItems(getString(R.string.convertMach)));

        customItemListEnergy.add(new CustomItems(getString(R.string.convertMillijoule)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertJoule)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertKilojoule)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertMegajoule)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertKalorie)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertKilokalorie)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertWattsekunde)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertWattstunde)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertKilowattsekunde)));
        customItemListEnergy.add(new CustomItems(getString(R.string.convertKilowattstunde)));

        customItemListPressure.add(new CustomItems(getString(R.string.convertMillipascal)));
        customItemListPressure.add(new CustomItems(getString(R.string.convertPascal)));
        customItemListPressure.add(new CustomItems(getString(R.string.convertHectopascal)));
        customItemListPressure.add(new CustomItems(getString(R.string.convertKilopascal)));
        customItemListPressure.add(new CustomItems(getString(R.string.convertBar)));
        customItemListPressure.add(new CustomItems(getString(R.string.convertMillibar)));
        customItemListPressure.add(new CustomItems(getString(R.string.convertTorr)));
        customItemListPressure.add(new CustomItems(getString(R.string.convertPSI)));
        customItemListPressure.add(new CustomItems(getString(R.string.convertPSF)));

        customItemListTorque.add(new CustomItems(getString(R.string.convertNewtonMeter)));
        customItemListTorque.add(new CustomItems(getString(R.string.convertMeterKilogramm)));
        customItemListTorque.add(new CustomItems(getString(R.string.convertFootPound)));
        customItemListTorque.add(new CustomItems(getString(R.string.convertInchPound)));

        customItemListWork.add(new CustomItems(getString(R.string.convertMilliwatt)));
        customItemListWork.add(new CustomItems(getString(R.string.convertWatt)));
        customItemListWork.add(new CustomItems(getString(R.string.convertKilowatt)));
        customItemListWork.add(new CustomItems(getString(R.string.convertMegawatt)));
        customItemListWork.add(new CustomItems(getString(R.string.convertGigawatt)));
        customItemListWork.add(new CustomItems(getString(R.string.convertPferdestaerke)));
        customItemListWork.add(new CustomItems(getString(R.string.convertJouleProSekunde)));
    }

    @SuppressLint("InflateParams")
    private void changeConvertModes(final String spinnerText) {
        if(spinnerText.equals(getString(R.string.convertAngle))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Winkel", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.angle, null);
        } else if(spinnerText.equals(getString(R.string.convertArea))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Fläche", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.area, null);
        } else if(spinnerText.equals(getString(R.string.convertStorage))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Speicher", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.digital_storage, null);
        } else if(spinnerText.equals(getString(R.string.convertDistance))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Entfernung", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.distance, null);
        } else if(spinnerText.equals(getString(R.string.convertVolume))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Volumen", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.volume, null);
        } else if(spinnerText.equals(getString(R.string.convertMassWeigth))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","MasseGewicht", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.mass_weight, null);
        } else if(spinnerText.equals(getString(R.string.convertTime))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Zeit", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.time, null);
        } else if(spinnerText.equals(getString(R.string.convertTemperature))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Temperatur", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.temperature, null);
        } else if(spinnerText.equals(getString(R.string.convertVoltage))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","StromSpannung", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.voltage, null);
        } else if(spinnerText.equals(getString(R.string.convertCurrent))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","StromStärke", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.current, null);
        } else if(spinnerText.equals(getString(R.string.convertSpeed))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Geschwindigkeit", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.speed, null);
        } else if(spinnerText.equals(getString(R.string.convertEnergy))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Energie", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.energy, null);
        } else if(spinnerText.equals(getString(R.string.convertPressure))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Druck", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.pressure, null);
        } else if(spinnerText.equals(getString(R.string.convertTorque))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Drehmoment", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.torque, null);
        } else if(spinnerText.equals(getString(R.string.convertWork))) {
            dataManager.updateValuesInJSONSettingsData("convertMode", "value","Arbeit", getMainActivityContext());
            outherLinearLayout = (LinearLayout) inflater.inflate(R.layout.work, null);
        }

        if (outherLinearLayout != null) {
            ScrollView scrollView = findViewById(R.id.convertScrollLayout);
            scrollView.removeAllViews();
            scrollView.addView(outherLinearLayout);
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
            if (dataManager.getJSONSettingsData("disablePatchNotesTemporary", getMainActivityContext()).getString("value").equals("true")) {
                dataManager.saveToJSONSettings("disablePatchNotesTemporary", false, getMainActivityContext());
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
            Log.e("startBackgroundService", e.toString());
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
    public static Context getMainActivityContext() {
        return mainActivity;
    }

    /**
     * Sets up the listeners for each button in the application
     */
    private void setUpButtonListeners() {
        setButtonListener(R.id.convert_return_button, this::returnToCalculator);
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
            });
        }
    }

    /**
     * This method switches the display mode based on the current night mode.
     */
    @SuppressLint({"ResourceType", "UseCompatLoadingForDrawables", "CutPasteId"})
    private void switchDisplayMode() {
        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        Button backbutton = findViewById(R.id.convert_return_button);

        int index = 0;
        if(customSpinnerMode != null) {
            index = customSpinnerMode.getSelectedItemPosition();
        }

        if(getSelectedSetting() != null) {
            if(getSelectedSetting().equals("Systemstandard")) {
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Nightmode is activated
                        dataManager = new DataManager();
                        String trueDarkMode;
                        try {
                            trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        if (trueDarkMode.equals("false")) {
                            if (backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                            }

                            updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));

                            customList = new ArrayList<>();
                            customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area_light));
                            customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard_light));
                            customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle_light));
                            customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder_light));
                            customList.add(new CustomItems(getString(R.string.convertMassWeigth), R.drawable.mass_weigh_light));
                            customList.add(new CustomItems(getString(R.string.convertTime), R.drawable.time_light));
                            customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.temperature_light));
                            customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.voltage_light));
                            customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.current_light));
                            customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.speed_light));
                            customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.energy_light));
                            customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.compare_arrows_light));
                            customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.advanced_light));
                            customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.settings_light));

                            customAdapter = new CustomAdapter(this, customList);
                            customAdapter.setTextColor(Color.parseColor("#FFFFFF"));
                            customAdapter.setBackgroundColor(Color.parseColor("#151515"));
                            customSpinnerMode.setAdapter(customAdapter);

                            customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                            if (customAdapterMeasurement != null) {
                                customAdapterMeasurement.setTextColor(Color.parseColor("#FFFFFF"));
                                customAdapterMeasurement.setBackgroundColor(Color.parseColor("#151515"));
                                customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                            }

                        } else {
                            if (backbutton != null) {
                                backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                            }

                            updateUI(Color.parseColor("#000000"), Color.parseColor("#D5D5D5"));

                            customList = new ArrayList<>();
                            customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertMassWeigth), R.drawable.mass_weigh_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertTime), R.drawable.time_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.temperature_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.voltage_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.current_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.speed_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.energy_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.compare_arrows_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.advanced_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.settings_true_darkmode));

                            customAdapter = new CustomAdapter(this, customList);
                            customAdapter.setTextColor(Color.parseColor("#D5D5D5"));
                            customAdapter.setBackgroundColor(Color.parseColor("#000000"));
                            customSpinnerMode.setAdapter(customAdapter);

                            customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                            if (customAdapterMeasurement != null) {
                                customAdapterMeasurement.setTextColor(Color.parseColor("#D5D5D5"));
                                customAdapterMeasurement.setBackgroundColor(Color.parseColor("#000000"));
                                customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                            }
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated
                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                        }

                        updateUI(Color.parseColor("#FFFFFF"), Color.parseColor("#151515"));

                        customList = new ArrayList<>();
                        customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area));
                        customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard));
                        customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle));
                        customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder));
                        customList.add(new CustomItems(getString(R.string.convertMassWeigth), R.drawable.mass_weigh));
                        customList.add(new CustomItems(getString(R.string.convertTime), R.drawable.time));
                        customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.temperature));
                        customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.voltage));
                        customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.current));
                        customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.speed));
                        customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.energy));
                        customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.compare_arrows));
                        customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.advanced));
                        customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.settings));

                        customAdapter = new CustomAdapter(this, customList);
                        customAdapter.setTextColor(Color.parseColor("#151515"));
                        customAdapter.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        customSpinnerMode.setAdapter(customAdapter);

                        customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                        if (customAdapterMeasurement != null) {
                            customAdapterMeasurement.setTextColor(Color.parseColor("#151515"));
                            customAdapterMeasurement.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                        }
                        break;
                }
            } else if (getSelectedSetting().equals("Tageslichtmodus")) {
                if(backbutton != null) {
                    backbutton.setForeground(getDrawable(R.drawable.arrow_back));
                }

                updateUI(Color.parseColor("#FFFFFF"), Color.parseColor("#151515"));

                customList = new ArrayList<>();
                customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area));
                customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard));
                customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle));
                customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder));
                customList.add(new CustomItems(getString(R.string.convertMassWeigth), R.drawable.mass_weigh));
                customList.add(new CustomItems(getString(R.string.convertTime), R.drawable.time));
                customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.temperature));
                customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.voltage));
                customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.current));
                customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.speed));
                customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.energy));
                customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.compare_arrows));
                customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.advanced));
                customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.settings));

                customAdapter = new CustomAdapter(this, customList);
                customAdapter.setTextColor(Color.parseColor("#151515"));
                customAdapter.setBackgroundColor(Color.parseColor("#FFFFFF"));
                customSpinnerMode.setAdapter(customAdapter);

                customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                if (customAdapterMeasurement != null) {
                    customAdapterMeasurement.setTextColor(Color.parseColor("#151515"));
                    customAdapterMeasurement.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                }
            } else if (getSelectedSetting().equals("Dunkelmodus")) {
                dataManager = new DataManager();
                String trueDarkMode;
                try {
                    trueDarkMode = dataManager.getJSONSettingsData("settingsTrueDarkMode", getMainActivityContext()).getString("value");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (trueDarkMode.equals("false")) {
                    if (backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.arrow_back_light));
                    }

                    updateUI(Color.parseColor("#151515"), Color.parseColor("#FFFFFF"));

                    customList = new ArrayList<>();
                    customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area_light));
                    customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard_light));
                    customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle_light));
                    customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder_light));
                    customList.add(new CustomItems(getString(R.string.convertMassWeigth), R.drawable.mass_weigh_light));
                    customList.add(new CustomItems(getString(R.string.convertTime), R.drawable.time_light));
                    customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.temperature_light));
                    customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.voltage_light));
                    customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.current_light));
                    customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.speed_light));
                    customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.energy_light));
                    customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.compare_arrows_light));
                    customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.advanced_light));
                    customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.settings_light));

                    customAdapter = new CustomAdapter(this, customList);
                    customAdapter.setTextColor(Color.parseColor("#FFFFFF"));
                    customAdapter.setBackgroundColor(Color.parseColor("#151515"));
                    customSpinnerMode.setAdapter(customAdapter);

                    customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                    if (customAdapterMeasurement != null) {
                        customAdapterMeasurement.setTextColor(Color.parseColor("#FFFFFF"));
                        customAdapterMeasurement.setBackgroundColor(Color.parseColor("#151515"));
                        customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                    }
                } else {
                    if (backbutton != null) {
                        backbutton.setForeground(getDrawable(R.drawable.arrow_back_true_darkmode));
                    }

                    updateUI(Color.parseColor("#000000"), Color.parseColor("#D5D5D5"));

                    customList = new ArrayList<>();
                    customList.add(new CustomItems(getString(R.string.convertAngle), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertArea), R.drawable.area_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertStorage), R.drawable.sdcard_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertDistance), R.drawable.triangle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertVolume), R.drawable.cylinder_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertMassWeigth), R.drawable.mass_weigh_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertTime), R.drawable.time_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.temperature_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.voltage_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.current_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.speed_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.energy_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.compare_arrows_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.advanced_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.settings_true_darkmode));

                    customAdapter = new CustomAdapter(this, customList);
                    customAdapter.setTextColor(Color.parseColor("#D5D5D5"));
                    customAdapter.setBackgroundColor(Color.parseColor("#000000"));
                    customSpinnerMode.setAdapter(customAdapter);

                    customAdapterMeasurement = (CustomAdapter) customSpinnerMeasurement.getAdapter();
                    if (customAdapterMeasurement != null) {
                        customAdapterMeasurement.setTextColor(Color.parseColor("#D5D5D5"));
                        customAdapterMeasurement.setBackgroundColor(Color.parseColor("#000000"));
                        customSpinnerMeasurement.setAdapter(customAdapterMeasurement);
                    }
                }
            }
        }
        if(customSpinnerMode != null) {
            customSpinnerMode.setSelection(index);
        }
        if(customSpinnerMeasurement != null) {
            try {
                final String mode = dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString("value");
                final int pos = Integer.parseInt(dataManager.getJSONSettingsData("convertMode", getMainActivityContext()).getString(mode + "Current"));
                customSpinnerMeasurement.setSelection(pos);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Handles configuration changes.
     * It calls the superclass method and switches the display mode based on the current night mode.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switchDisplayMode();
        firstStart = false;
    }

    /**
     * This method updates the UI elements with the given background color and text color.
     * @param backgroundColor The color to be used for the background.
     * @param textColor The color to be used for the text.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUI(int backgroundColor, int textColor) {
        Button convertReturnButton = findViewById(R.id.convert_return_button);
        TextView convertTitle = findViewById(R.id.convert_title);
        LinearLayout convertLayout = findViewById(R.id.convertlayout);
        ScrollView convertScrollLayout = findViewById(R.id.convertScrollLayout);
        LinearLayout convertUI = findViewById(R.id.convertUI);
        Spinner customSpinner = findViewById(R.id.convertCustomSpinner);
        Spinner customSpinnerMeasurement = findViewById(R.id.convertSpinnerMessurement);
        EditText convertSpinnerNumber = findViewById(R.id.convertEditTextNumber);

        convertReturnButton.setBackgroundColor(backgroundColor);
        convertTitle.setTextColor(textColor);
        convertLayout.setBackgroundColor(backgroundColor);
        convertScrollLayout.setBackgroundColor(backgroundColor);
        convertUI.setBackgroundColor(backgroundColor);
        customSpinner.setBackgroundColor(backgroundColor);

        customSpinner.setBackgroundColor(backgroundColor);
        customSpinnerMeasurement.setBackgroundColor(backgroundColor);
        convertSpinnerNumber.setHintTextColor(Color.parseColor("#D5D5D5"));

        setTextViewColors(findViewById(R.id.convertUI), textColor, backgroundColor);
    }

    private void setTextViewColors(View view, int textColor, int backgroundColor) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(textColor);
            textView.setBackgroundColor(backgroundColor);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setTextViewColors(viewGroup.getChildAt(i), textColor, backgroundColor);
            }
        }
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
        returnToCalculator();
    }

    /**
     * This method returns to the calculator by starting the MainActivity.
     */
    public void returnToCalculator() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
