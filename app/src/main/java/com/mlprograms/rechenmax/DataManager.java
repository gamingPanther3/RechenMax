package com.mlprograms.rechenmax;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * DataManager
 * @author Max Lemberg
 * @version 1.4.5
 * @date 18.02.2024
 */

//  | Names                            | Values                           | Context                              |
//  |----------------------------------|----------------------------------|--------------------------------------|
//  | selectedSpinnerSetting           | System / Dark / Light            | MainActivity                         |
//  | functionMode                     | Deg / Rad                        | MainActivity                         |
//  | settingReleaseNotesSwitch        | true / false                     | SettingsActivity                     |
//  | removeValue                      | true / false                     | MainActivity                         |
//  | settingsTrueDarkMode             | true / false                     | MainActivity -> SettingsActivity     |
//  | showPatchNotes                   | true / false                     | MainActivity -> SettingsActivity     |
//  | disablePatchNotesTemporary       | true / false                     | MainActivity -> SettingsActivity     |
//  | showReleaseNotesOnVeryFirstStart | true / false                     | MainActivity                         |
//  | showScienceRow                   | true / false                     | MainActivity                         |
//  | rotate_op                        | true / false                     | MainActivity                         |
//  | lastnumber                       | Integer                          | MainActivity                         |
//  | historyTextViewNumber            | Integer                          | MainActivity                         |
//  | result_text                      | String                           | MainActivity                         |
//  | calculate_text                   | String                           | MainActivity                         |
//  | lastop                           | String                           | MainActivity                         |
//  | isNotation                       | true / false                     | MainActivity                         |
//  | eNotation                        | true / false                     | MainActivity                         |
//  | showShiftRow                     | true / false                     | MainActivity                         |
//  | shiftRow                         | true / false                     | MainActivity                         |
//  | logX                             | true / false                     | MainActivity                         |
//  | calculationMode                  | Standard / Vereinfacht           | MainActivity                         |
//  | currentVersion                   | String                           | MainActivity                         |
//  | old_version                      | String                           | MainActivity                         |
//  | returnToCalculator               | true / false                     | MainActivity                         |
//  | notificationSent                 | true / false                     | BackgroundService                    |
//  | pressedCalculate                 | true / false                     | MainActivity                         |
//  | allowNotification                | true / false                     | SettingsActivity                     |
//  | allowRememberNotifications       | true / false                     | SettingsActivity                     |
//  | allowDailyNotifications          | true / false                     | SettingsActivity                     |
//  | allowRememberNotificationsActive | true / false                     | SettingsActivity                     |
//  | allowDailyNotificationsActive    | true / false                     | SettingsActivity                     |
//  | refactorPI                       | true / false                     | MainActivity                         |
//  | historyMode                      | single / multiple                | MainActivity                         |
//  | convertMode                      | W / F / S / E / V            (*) | SettingsActivity                     |
// * = Winkel / Fläche / Speicher / Entfernung / Volumen

public class DataManager {

    // Declare a MainActivity object
    private MainActivity mainActivity;

    // Define the names of the files
    private static final String JSON_FILE = "settings.json";
    private static final String HISTORY_FILE = "history.json";

    /**
     * This constructor is used to create a DataManager object for the MainActivity.
     *
     * @param mainActivity The MainActivity instance that this DataManager will be associated with.
     */
    public DataManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * This constructor is used to create a DataManager object.
     *
     */
    public DataManager() {
        // Declare a SettingsActivity object
    }

    public DataManager(HelpActivity helpActivity) {
    }

    /**
     * This method is used to create a new JSON file in the application's file directory.
     *
     * @param applicationContext The application context, which is used to get the application's file directory.
     */
    public void createJSON(Context applicationContext) {
        File file = new File(applicationContext.getFilesDir(), JSON_FILE);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToJSONSettings(String name, String value, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToHistory", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }

            JSONObject dataObj = new JSONObject();
            dataObj.put("value", value);

            jsonObj.put(name, dataObj);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveToJSONSettings(String name, boolean value, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToHistory", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }

            JSONObject dataObj = new JSONObject();
            dataObj.put("value", String.valueOf(value));

            jsonObj.put(name, dataObj);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONSettingsData(String name, Context applicationContext) throws JSONException {
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                if (jsonObj.has(name)) {
                    return jsonObj.getJSONObject(name);
                } else {
                    Log.e("getDataForName", "Data with name " + name + " not found.");
                }
            } else {
                Log.e("getDataForName", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, JSONObject> getAllDataFromJSONSettings(Context applicationContext) {
        Map<String, JSONObject> allData = new HashMap<>();
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                Iterator<String> keys = jsonObj.keys();
                while (keys.hasNext()) {
                    String name = keys.next();
                    JSONObject dataObj = jsonObj.getJSONObject(name);
                    allData.put(name, dataObj);
                }
            } else {
                Log.e("getAllData", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return allData;
    }

    public void clearJSONSettings(Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (file.exists()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("");
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteNameFromJSONSettings(String name, Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                if (jsonObj.has(name)) {
                    jsonObj.remove(name); // Entferne den Namen aus dem JSON-Objekt
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(jsonObj.toString());
                    fileWriter.flush();
                    fileWriter.close();
                    Log.d("deleteNameFromHistory", "Name " + name + " deleted successfully.");
                } else {
                    Log.e("deleteNameFromHistory", "Data with name " + name + " not found.");
                }
            } else {
                Log.e("deleteNameFromHistory", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateValuesInJSONSettingsData(String name, String valueName, String newValue, Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                if (jsonObj.has(name)) {
                    JSONObject dataObj = jsonObj.getJSONObject(name);
                    dataObj.put(valueName, newValue);
                    jsonObj.put(name, dataObj);

                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(jsonObj.toString());
                    fileWriter.flush();
                    fileWriter.close();

                    Log.d("updateDetailsInHistoryData", "Details for " + name + " updated successfully.");
                } else {
                    Log.e("updateDetailsInHistoryData", "Data with name " + name + " not found.");
                }
            } else {
                Log.e("updateDetailsInHistoryData", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void addValueWithCustomNameToJSONSettings(String name, String valueName, String value, Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                JSONObject dataObj;
                if (jsonObj.has(name)) {
                    dataObj = jsonObj.getJSONObject(name);
                } else {
                    dataObj = new JSONObject();
                }

                if (!dataObj.has(valueName)) {
                    dataObj.put(valueName, value);
                    jsonObj.put(name, dataObj);

                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(jsonObj.toString());
                    fileWriter.flush();
                    fileWriter.close();

                    Log.d("addValueWithCustomName", "Value " + value + " with name " + valueName + " added successfully to " + name + ".");
                } else {
                    Log.d("addValueWithCustomName", "Value with name " + valueName + " already exists for " + name + ".");
                }
            } else {
                Log.e("addValueWithCustomName", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method initializes the settings by saving default values to the JSON file.
     *
     * @param applicationContext The application context, which is used to get the application's file directory.
     */
    public void initializeSettings(Context applicationContext) {
        try {
            if(getJSONSettingsData("selectedSpinnerSetting", applicationContext) == null) {
                saveToJSONSettings("selectedSpinnerSetting", "System", applicationContext);
            }
            if(getJSONSettingsData("functionMode", applicationContext) == null) {
                saveToJSONSettings("functionMode", "Deg", applicationContext);
            }
            if(getJSONSettingsData("settingReleaseNotesSwitch", applicationContext) == null) {
                saveToJSONSettings("settingReleaseNotesSwitch", "true", applicationContext);
            }
            if(getJSONSettingsData("removeValue", applicationContext) == null) {
                saveToJSONSettings("removeValue", "false", applicationContext);
            }
            if(getJSONSettingsData("settingsTrueDarkMode", applicationContext) == null) {
                saveToJSONSettings("settingsTrueDarkMode", "false", applicationContext);
            }
            if(getJSONSettingsData("showPatchNotes", applicationContext) == null) {
                saveToJSONSettings("showPatchNotes", "true", applicationContext);
            }
            if(getJSONSettingsData("disablePatchNotesTemporary", applicationContext) == null) {
                saveToJSONSettings("disablePatchNotesTemporary", "false", applicationContext);
            }
            if(getJSONSettingsData("showReleaseNotesOnVeryFirstStart", applicationContext) == null) {
                saveToJSONSettings("showReleaseNotesOnVeryFirstStart", "true", applicationContext);
            }
            if(getJSONSettingsData("showScienceRow", applicationContext) == null) {
                saveToJSONSettings("showScienceRow", "false", applicationContext);
            }
            if(getJSONSettingsData("rotate_op", applicationContext) == null) {
                saveToJSONSettings("rotate_op", "false", applicationContext);
            }
            if(getJSONSettingsData("lastnumber", applicationContext) == null) {
                saveToJSONSettings("lastnumber", "0", applicationContext);
            }
            if(getJSONSettingsData("historyTextViewNumber", applicationContext) == null) {
                saveToJSONSettings("historyTextViewNumber", "0", applicationContext);
            }
            if(getJSONSettingsData("result_text", applicationContext) == null) {
                saveToJSONSettings("result_text", "0", applicationContext);
            }
            if(getJSONSettingsData("calculate_text", applicationContext) == null) {
                saveToJSONSettings("calculate_text", "", applicationContext);
            }
            if(getJSONSettingsData("lastop", applicationContext) == null) {
                saveToJSONSettings("lastop", "+", applicationContext);
            }
            if(getJSONSettingsData("isNotation", applicationContext) == null) {
                saveToJSONSettings("isNotation", "false", applicationContext);
            }
            if(getJSONSettingsData("eNotation", applicationContext) == null) {
                saveToJSONSettings("eNotation", "false", applicationContext);
            }
            if(getJSONSettingsData("showShiftRow", applicationContext) == null) {
                saveToJSONSettings("showShiftRow", "false", applicationContext);
            }
            if(getJSONSettingsData("shiftRow", applicationContext) == null) {
                saveToJSONSettings("shiftRow", "1", applicationContext);
            }
            if(getJSONSettingsData("logX", applicationContext) == null) {
                saveToJSONSettings("logX", "false", applicationContext);
            }
            if(getJSONSettingsData("calculationMode", applicationContext) == null) {
                saveToJSONSettings("calculationMode", "Standard", applicationContext);
            }
            if(getJSONSettingsData("currentVersion", applicationContext) == null) {
                saveToJSONSettings("currentVersion", "1.6.3", applicationContext);
            }
            if(getJSONSettingsData("old_version", applicationContext) == null) {
                saveToJSONSettings("old_version", "0", applicationContext);
            }
            if(getJSONSettingsData("returnToCalculator", applicationContext) == null) {
                saveToJSONSettings("returnToCalculator", "false", applicationContext);
            }
            if(getJSONSettingsData("allowNotification", applicationContext) == null) {
                saveToJSONSettings("allowNotification", "false", applicationContext);
            }
            if(getJSONSettingsData("allowDailyNotifications", applicationContext) == null) {
                saveToJSONSettings("allowDailyNotifications", "false", applicationContext);
            }
            if(getJSONSettingsData("allowRememberNotifications", applicationContext) == null) {
                saveToJSONSettings("allowRememberNotifications", "false", applicationContext);
            }
            if(getJSONSettingsData("allowDailyNotificationsActive", applicationContext) == null) {
                saveToJSONSettings("allowDailyNotificationsActive", "true", applicationContext);
            }
            if(getJSONSettingsData("allowRememberNotificationsActive", applicationContext) == null) {
                saveToJSONSettings("allowRememberNotificationsActive", "true", applicationContext);
            }
            if(getJSONSettingsData("notificationSent", applicationContext) == null) {
                saveToJSONSettings("notificationSent", "false", applicationContext);
            }
            if(getJSONSettingsData("pressedCalculate", applicationContext) == null) {
                saveToJSONSettings("pressedCalculate", "false", applicationContext);
            }
            if(getJSONSettingsData("refactorPI", applicationContext) == null) {
                saveToJSONSettings("refactorPI", "true", applicationContext);
            }
            if(getJSONSettingsData("historyMode", applicationContext) == null) {
                saveToJSONSettings("historyMode", "single", applicationContext);
            }
            if(getJSONSettingsData("dayPassed", applicationContext) == null) {
                saveToJSONSettings("dayPassed", "true", applicationContext);
            }
            if(getJSONSettingsData("convertMode", applicationContext) == null) {
                saveToJSONSettings("convertMode", "E", applicationContext);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method saves numbers to two files.
     *
     * @param applicationContext The application context.
     */
    public void saveNumbers(Context applicationContext) {
        if (mainActivity != null) {
            try {
                // Save calculate_text using dataManager
                saveToJSONSettings("calculate_text", mainActivity.getCalculateText(), applicationContext);

                // Save result_text using dataManager
                saveToJSONSettings("result_text", mainActivity.getResultText(), applicationContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method loads numbers from two files and sets the text of two TextViews.
     */
    public void loadNumbers() throws JSONException {
        if (mainActivity != null) {
            JSONObject calculateText = getJSONSettingsData("calculate_text", mainActivity.getApplicationContext());
            JSONObject resultText = getJSONSettingsData("result_text", mainActivity.getApplicationContext());

            TextView calculatelabel = mainActivity.findViewById(R.id.calculate_label);
            TextView resultlabel = mainActivity.findViewById(R.id.result_label);

            if (calculatelabel != null && resultlabel != null) {
                try {
                    calculatelabel.setText(calculateText.getString("value"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    final String value = String.valueOf(resultText.getString("value").isEmpty());
                    if (!value.isEmpty()) {
                        try {
                            resultlabel.setText(resultText.getString("value"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        resultlabel.setText("0");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void saveToHistory(String name, String date, String details, String calculation, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToHistory", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }

            JSONObject dataObj = new JSONObject();
            dataObj.put("date", date);
            dataObj.put("details", details);
            dataObj.put("calculation", calculation);

            jsonObj.put(name, dataObj);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getHistoryData(String name, Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                if (jsonObj.has(name)) {
                    return jsonObj.getJSONObject(name);
                } else {
                    //Log.e("getDataForName", "Data with name " + name + " not found.");
                }
            } else {
                //Log.e("getDataForName", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, JSONObject> getAllData(Context applicationContext) {
        Map<String, JSONObject> allData = new HashMap<>();
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                Iterator<String> keys = jsonObj.keys();
                while (keys.hasNext()) {
                    String name = keys.next();
                    JSONObject dataObj = jsonObj.getJSONObject(name);
                    allData.put(name, dataObj);
                }
            } else {
                //Log.e("getAllData", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return allData;
    }

    public void clearHistory(Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("");
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteNameFromHistory(String name, Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                if (jsonObj.has(name)) {
                    jsonObj.remove(name); // Entferne den Namen aus dem JSON-Objekt
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(jsonObj.toString());
                    fileWriter.flush();
                    fileWriter.close();
                    //Log.d("deleteNameFromHistory", "Name " + name + " deleted successfully.");
                } else {
                    //Log.e("deleteNameFromHistory", "Data with name " + name + " not found.");
                }
            } else {
                //Log.e("deleteNameFromHistory", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public Map<String, JSONObject> getAllDataFromHistory(Context applicationContext) {
        Map<String, JSONObject> allData = new HashMap<>();
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                Iterator<String> keys = jsonObj.keys();
                while (keys.hasNext()) {
                    String name = keys.next();
                    JSONObject dataObj = jsonObj.getJSONObject(name);
                    allData.put(name, dataObj);
                }
            } else {
                Log.e("getAllData", "History file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return allData;
    }

    public void updateDetailsInHistoryData(String name, String newDetails, Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                if (jsonObj.has(name)) {
                    JSONObject dataObj = jsonObj.getJSONObject(name);
                    dataObj.put("details", newDetails);
                    jsonObj.put(name, dataObj);

                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(jsonObj.toString());
                    fileWriter.flush();
                    fileWriter.close();

                    //Log.d("updateDetailsInHistoryData", "Details for " + name + " updated successfully.");
                } else {
                    //Log.e("updateDetailsInHistoryData", "Data with name " + name + " not found.");
                }
            } else {
                //Log.e("updateDetailsInHistoryData", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}