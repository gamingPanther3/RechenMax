package com.mlprograms.rechenmax;

import static com.mlprograms.rechenmax.Converter.Category.ANGLE;
import static com.mlprograms.rechenmax.Converter.Category.AREA;
import static com.mlprograms.rechenmax.Converter.Category.DATA;
import static com.mlprograms.rechenmax.Converter.Category.LENGTH;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.ACRE;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.ANGSTROM;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.ARES;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.BIT;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.BYTE;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.CENTIMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.DECIMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.DEGREE;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.EM;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.EXABIT_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.EXABYTE_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.FEET;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.FEMTOMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.GIGABIT_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.GIGABYTE_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.GRAD;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.HECTARE;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.HECTOMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.KILO;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.KILOBIT_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.KILOBYTE_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.KILOMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.LIGHT_YEAR;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.MEGABIT_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.MEGABYTE_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.METER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.MICROMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.MILES;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.MILLIMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.NANOMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.NAUTICAL_MILES;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.PARSEC;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.PETABIT_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.PETABYTE_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.PICA;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.PICOMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.PIXEL;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.POINT;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.RADIAN;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.SQUARE_CENTIMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.SQUARE_FOOT;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.SQUARE_INCH;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.SQUARE_KILOMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.SQUARE_METER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.SQUARE_MICROMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.SQUARE_MILLIMETER;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.TERABIT_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.TERABYTE_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.YARD;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.YOTABIT_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.YOTABYTE_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.ZETABIT_B1000;
import static com.mlprograms.rechenmax.Converter.UnitDefinition.ZETABYTE_B1000;
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
import java.util.Locale;

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
        customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.angle));
        customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.angle));

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
                }
                double editTextNumber = Double.parseDouble(editTextNumber2);

                Spinner spinner = findViewById(R.id.convertSpinnerMessurement);
                switch (mode) {
                    case "Winkel":
                        TextView convertDeg = findViewById(R.id.convertDegTextView);
                        TextView convertRad = findViewById(R.id.convertRadTextView);

                        Converter angleConverter;
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                angleConverter = new Converter(ANGLE, DEGREE);
                                convertDeg.setText(     angleConverter.convertToString(editTextNumber, DEGREE));
                                convertRad.setText(     angleConverter.convertToString(editTextNumber, RADIAN));
                                break;
                            case 1:
                                angleConverter = new Converter(ANGLE, RADIAN);
                                convertDeg.setText(     angleConverter.convertToString(editTextNumber, DEGREE));
                                convertRad.setText(     angleConverter.convertToString(editTextNumber, RADIAN));
                                break;
                            case 2:
                                angleConverter = new Converter(ANGLE, GRAD);
                                convertDeg.setText(     angleConverter.convertToString(editTextNumber, DEGREE));
                                convertRad.setText(     angleConverter.convertToString(editTextNumber, RADIAN));
                                break;
                            default:
                                convertDeg.setText("0,00");
                                convertRad.setText("0,00");
                                break;
                        }

                        convertDeg.setText(formatResultTextAfterType(convertDeg.getText().toString()));
                        convertRad.setText(formatResultTextAfterType(convertRad.getText().toString()));
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

                        Converter areaConverter;
                        switch (spinner.getSelectedItemPosition()) {
                            case 0:
                                areaConverter = new Converter(AREA, SQUARE_MICROMETER);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 1:
                                areaConverter = new Converter(AREA, SQUARE_MILLIMETER);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 2:
                                areaConverter = new Converter(AREA, SQUARE_CENTIMETER);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 3:
                                areaConverter = new Converter(AREA, SQUARE_METER);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 4:
                                areaConverter = new Converter(AREA, SQUARE_KILOMETER);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 5:
                                areaConverter = new Converter(AREA, ARES);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 6:
                                areaConverter = new Converter(AREA, HECTARE);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 7:
                                areaConverter = new Converter(AREA, SQUARE_INCH);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 8:
                                areaConverter = new Converter(AREA, SQUARE_FOOT);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
                                break;
                            case 9:
                                areaConverter = new Converter(AREA, ACRE);
                                convertSquareMicrometer.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MICROMETER));
                                convertSquareMillimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_MILLIMETER));
                                convertSquareCentimeter.setText(   areaConverter.convertToString(editTextNumber, SQUARE_CENTIMETER));
                                convertSquareMeter.setText(        areaConverter.convertToString(editTextNumber, SQUARE_METER));
                                convertSquareKilometer.setText(    areaConverter.convertToString(editTextNumber, SQUARE_KILOMETER));
                                convertAr.setText(                 areaConverter.convertToString(editTextNumber, ARES));
                                convertHectares.setText(           areaConverter.convertToString(editTextNumber, HECTARE));
                                convertSquareInch.setText(         areaConverter.convertToString(editTextNumber, SQUARE_INCH));
                                convertSquareFeet.setText(         areaConverter.convertToString(editTextNumber, SQUARE_FOOT));
                                convertAcre.setText(               areaConverter.convertToString(editTextNumber, ACRE));
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

                        convertSquareMicrometer.setText(formatResultTextAfterType(convertSquareMicrometer.getText().toString()));
                        convertSquareMillimeter.setText(formatResultTextAfterType(convertSquareMillimeter.getText().toString()));
                        convertSquareCentimeter.setText(formatResultTextAfterType(convertSquareCentimeter.getText().toString()));
                        convertSquareMeter.setText(formatResultTextAfterType(convertSquareMeter.getText().toString()));
                        convertSquareKilometer.setText(formatResultTextAfterType(convertSquareKilometer.getText().toString()));
                        convertAr.setText(formatResultTextAfterType(convertAr.getText().toString()));
                        convertHectares.setText(formatResultTextAfterType(convertHectares.getText().toString()));
                        convertSquareInch.setText(formatResultTextAfterType(convertSquareInch.getText().toString()));
                        convertSquareFeet.setText(formatResultTextAfterType(convertSquareFeet.getText().toString()));
                        convertAcre.setText(formatResultTextAfterType(convertAcre.getText().toString()));
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
                                distanceConverter = new Converter(LENGTH, MILES);
                                break;
                            case 19:
                                distanceConverter = new Converter(LENGTH, NAUTICAL_MILES);
                                break;
                            case 20:
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
                                convertMiles.setText("0,00");
                                convertSeamiles.setText("0,00");
                                convertLightyear.setText("0,00");
                        }

                        convertAngstrom.setText(            formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, ANGSTROM)));
                        convertFemtometer.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, FEMTOMETER)));
                        convertParsec.setText(              formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, PARSEC)));
                        convertPixel.setText(               formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, PIXEL)));
                        convertPoint.setText(               formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, POINT)));
                        convertPica.setText(                formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, PICA)));
                        convertEm.setText(                  formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, EM)));
                        convertPikometer.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, PICOMETER)));
                        convertNanometer.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, NANOMETER)));
                        convertMikrometer.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, MICROMETER)));
                        convertMillimeter.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, MILLIMETER)));
                        convertCentimeter.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, CENTIMETER)));
                        convertDezimeter.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, DECIMETER)));
                        convertMeter.setText(               formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, METER)));
                        convertHektometer.setText(          formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, HECTOMETER)));
                        convertKilometer.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, KILOMETER)));
                        convertFeet.setText(                formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, FEET)));
                        convertYard.setText(                formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, YARD)));
                        convertMiles.setText(               formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, MILES)));
                        convertSeamiles.setText(            formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, NAUTICAL_MILES)));
                        convertLightyear.setText(           formatResultTextAfterType(distanceConverter.convertToString(editTextNumber, LIGHT_YEAR)));
                        break;
                    case "Volumen" /* Volumen */:
                        TextView convertMilliliter = findViewById(R.id.convertMilliliterTextView);
                        TextView convertLiter = findViewById(R.id.convertLiterTextView);
                        TextView convertKubikmillimeter = findViewById(R.id.convertKubikmillimeterTextView);
                        TextView convertKubikmeter = findViewById(R.id.convertKubikmeterTextView);
                        TextView convertKubikInch = findViewById(R.id.convertKubikInchTextView);
                        TextView convertKubikFeet = findViewById(R.id.convertKubikFeetTextView);
                        TextView convertGallonUS = findViewById(R.id.convertGallonUSTextView);

                        //convertMilliliter.setText(String.valueOf(editTextNumber));
                        //convertLiter.setText(String.valueOf(editTextNumber));
                        //convertKubikmillimeter.setText(String.valueOf(editTextNumber));
                        //convertKubikmeter.setText(String.valueOf(editTextNumber));
                        //convertKubikInch.setText(String.valueOf(editTextNumber));
                        //convertKubikFeet.setText(String.valueOf(editTextNumber));
                        //convertGallonUS.setText(String.valueOf(editTextNumber));
                        break;
                    case "MasseGewicht":
                        TextView convertAtomareMasseneinheit = findViewById(R.id.convertAtomareMasseneinheitTextView);
                        TextView convertNanogramm = findViewById(R.id.convertNanogrammTextView);
                        TextView convertMikrogramm = findViewById(R.id.convertMikrogrammTextView);
                        TextView convertMilligramm = findViewById(R.id.convertMilligrammTextView);
                        TextView convertGramm = findViewById(R.id.convertGrammTextView);
                        TextView convertKilogramm = findViewById(R.id.convertKilogrammTextView);
                        TextView convertTonne= findViewById(R.id.convertTonneTextView);
                        TextView convertUnzen= findViewById(R.id.convertUnzenTextView);
                        TextView convertPfund= findViewById(R.id.convertPfundTextView);

                        //convertAtomareMasseneinheit.setText(String.valueOf(editTextNumber));
                        //convertMikrogramm.setText(String.valueOf(editTextNumber));
                        //convertMilligramm.setText(String.valueOf(editTextNumber));
                        //convertGramm.setText(String.valueOf(editTextNumber));
                        //convertKilogramm.setText(String.valueOf(editTextNumber));
                        //convertTonne.setText(String.valueOf(editTextNumber));
                        //convertUnzen.setText(String.valueOf(editTextNumber));
                        //convertPfund.setText(String.valueOf(editTextNumber));
                        break;
                    case "Zeit":
                        break;
                    case "Temperatur":
                        break;
                    case "StromSpannung":
                        break;
                    case "StromStärke":
                        break;
                    case "Geschwindigkeit":
                        break;
                    case "Energie":
                        break;
                    case "Druck":
                        break;
                    case "Drehmoment":
                        break;
                    case "Arbeit":
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

        customItemListMass.add(new CustomItems(getString(R.string.convertTonne)));
        customItemListMass.add(new CustomItems(getString(R.string.convertKilogramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertGramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertMilligramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertMikrogramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convertNanogramm)));
        customItemListMass.add(new CustomItems(getString(R.string.convert)));
        customItemListMass.add(new CustomItems(getString(R.string.convert)));
        customItemListMass.add(new CustomItems(getString(R.string.convert)));

        customItemListTime.add(new CustomItems(getString(R.string.convertWoche)));
        customItemListTime.add(new CustomItems(getString(R.string.convertTag)));
        customItemListTime.add(new CustomItems(getString(R.string.convertStunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertMinute)));
        customItemListTime.add(new CustomItems(getString(R.string.convertSekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertMillisekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertMikrosekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertNanosekunde)));
        customItemListTime.add(new CustomItems(getString(R.string.convertPicosekunde)));

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
        customItemListWork.add(new CustomItems(getString(R.string.convertPferdestärke)));
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
                            customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.angle_light));
                            customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.angle_light));

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
                            customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.angle_true_darkmode));
                            customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.angle_true_darkmode));

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
                        customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.angle));
                        customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.angle));

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
                customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.angle));
                customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.angle));

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
                    customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.angle_light));
                    customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.angle_light));

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
                    customList.add(new CustomItems(getString(R.string.convertTemperature), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertVoltage), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertCurrent), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertSpeed), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertEnergy), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertPressure), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertTorque), R.drawable.angle_true_darkmode));
                    customList.add(new CustomItems(getString(R.string.convertWork), R.drawable.angle_true_darkmode));

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
