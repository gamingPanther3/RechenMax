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

/**
 * DataManager
 * @author Max Lemberg
 * @version 1.4.2
 * @date 09.02.2024
 */

//  | Names                            | Values                   | Context                              |
//  |----------------------------------|--------------------------|--------------------------------------|
//  | selectedSpinnerSetting           | System / Dark / Light    | MainActivity                         |
//  | functionMode                     | Deg / Rad                | MainActivity                         |
//  | settingReleaseNotesSwitch        | true / false             | SettingsActivity                     |
//  | removeValue                      | true / false             | MainActivity                         |
//  | settingsTrueDarkMode             | true / false             | MainActivity -> SettingsActivity     |
//  | showPatchNotes                   | true / false             | MainActivity -> SettingsActivity     |
//  | disablePatchNotesTemporary       | true / false             | MainActivity -> SettingsActivity     |
//  | showReleaseNotesOnVeryFirstStart | true / false             | MainActivity                         |
//  | showScienceRow                   | true / false             | MainActivity                         |
//  | rotate_op                        | true / false             | MainActivity                         |
//  | lastnumber                       | Integer                  | MainActivity                         |
//  | historyTextViewNumber            | Integer                  | MainActivity                         |
//  | result_text                      | String                   | MainActivity                         |
//  | calculate_text                   | String                   | MainActivity                         |
//  | lastop                           | String                   | MainActivity                         |
//  | isNotation                       | true / false             | MainActivity                         |
//  | eNotation                        | true / false             | MainActivity                         |
//  | showShiftRow                     | true / false             | MainActivity                         |
//  | shiftRow                         | true / false             | MainActivity                         |
//  | logX                             | true / false             | MainActivity                         |
//  | calculationMode                  | Standard / Vereinfacht   | MainActivity                         |
//  | currentVersion                   | String                   | MainActivity                         |
//  | old_version                      | String                   | MainActivity                         |
//  | returnToCalculator               | true / false             | MainActivity                         |
//  | notificationSent                 | true / false             | BackgroundService                    |
//  | pressedCalculate                 | true / false             | MainActivity                         |
//  | allowNotification                | true / false             | SettingsActivity                         |
//  | allowRememberNotifications       | true / false             | SettingsActivity                         |
//  | allowDailyNotifications          | true / false             | SettingsActivity                     |

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

    /**
     * This method is used to save a boolean value to a JSON file.
     * It first checks if the file exists, and if not, it creates a new file.
     * It then reads the content of the file and converts it to a JSONObject.
     * It puts the given name and value into the JSONObject and writes it back to the file.
     *
     * @param name The name to be saved in the JSON file. This should be a string.
     * @param value The boolean value to be saved in the JSON file.
     * @param applicationContext The application context, which is used to get the application's file directory.
     */
    public void saveToJSON(String name, boolean value, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToJSON", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }
            jsonObj.put(name, value);
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to save a string value to a JSON file.
     * It first checks if the file exists, and if not, it creates a new file.
     * It then reads the content of the file and converts it to a JSONObject.
     * It puts the given name and value into the JSONObject and writes it back to the file.
     *
     * @param name The name to be saved in the JSON file. This should be a string.
     * @param value The string value to be saved in the JSON file.
     * @param applicationContext The application context, which is used to get the application's file directory.
     */
    public void saveToJSON(String name, String value, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToJSON", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }
            jsonObj.put(name, value);
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method reads a value from a JSON file.
     * If the file does not exist or is empty, it initializes the settings and creates a new file.
     * If the requested name does not exist in the JSON file, it initializes the settings.
     *
     * @param name The name of the value to be read from the JSON file.
     * @param applicationContext The application context, which is used to get the application's file directory.
     * @return Returns the value associated with the given name in the JSON file, or null if the file does not exist, is empty, or does not contain the name.
     */
    public String readFromJSON(final String name, Context applicationContext) {
        String setting = null;
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (!file.exists()) {
                Log.e("readFromJSON", "File does not exist");
                createJSON(mainActivity.getApplicationContext());
                if (!file.createNewFile()) {
                    Log.e("saveToJSON", "Failed to create new file");
                    createJSON(mainActivity.getApplicationContext());
                    return null;
                }
            } else {
                String content = new String(Files.readAllBytes(file.toPath()));
                if (content.isEmpty()) {
                    Log.e("readFromJSON", "File is empty (" + name + ")");
                }
                JSONObject jsonRead = new JSONObject(new JSONTokener(content));
                if (jsonRead.has(name)) {
                    setting = jsonRead.getString(name);
                } else {
                    // Log.e("readFromJSON", "Key: " + name + " does not exist in JSON");
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return setting;
    }

    /**
     * This method initializes the settings by saving default values to the JSON file.
     *
     * @param applicationContext The application context, which is used to get the application's file directory.
     */
    public void initializeSettings(Context applicationContext) {
        if(readFromJSON("selectedSpinnerSetting", applicationContext) == null) {
            saveToJSON("selectedSpinnerSetting", "System", applicationContext);
        }
        if(readFromJSON("functionMode", applicationContext) == null) {
            saveToJSON("functionMode", "Deg", applicationContext);
        }
        if(readFromJSON("settingReleaseNotesSwitch", applicationContext) == null) {
            saveToJSON("settingReleaseNotesSwitch", "true", applicationContext);
        }
        if(readFromJSON("removeValue", applicationContext) == null) {
            saveToJSON("removeValue", "false", applicationContext);
        }
        if(readFromJSON("settingsTrueDarkMode", applicationContext) == null) {
            saveToJSON("settingsTrueDarkMode", "false", applicationContext);
        }
        if(readFromJSON("showPatchNotes", applicationContext) == null) {
            saveToJSON("showPatchNotes", "true", applicationContext);
        }
        if(readFromJSON("disablePatchNotesTemporary", applicationContext) == null) {
            saveToJSON("disablePatchNotesTemporary", "false", applicationContext);
        }
        if(readFromJSON("showReleaseNotesOnVeryFirstStart", applicationContext) == null) {
            saveToJSON("showReleaseNotesOnVeryFirstStart", "true", applicationContext);
        }
        if(readFromJSON("showScienceRow", applicationContext) == null) {
            saveToJSON("showScienceRow", "false", applicationContext);
        }
        if(readFromJSON("rotate_op", applicationContext) == null) {
            saveToJSON("rotate_op", "false", applicationContext);
        }
        if(readFromJSON("lastnumber", applicationContext) == null) {
            saveToJSON("lastnumber", "0", applicationContext);
        }
        if(readFromJSON("historyTextViewNumber", applicationContext) == null) {
            saveToJSON("historyTextViewNumber", "0", applicationContext);
        }
        if(readFromJSON("result_text", applicationContext) == null) {
            saveToJSON("result_text", "0", applicationContext);
        }
        if(readFromJSON("calculate_text", applicationContext) == null) {
            saveToJSON("calculate_text", "", applicationContext);
        }
        if(readFromJSON("lastop", applicationContext) == null) {
            saveToJSON("lastop", "+", applicationContext);
        }
        if(readFromJSON("isNotation", applicationContext) == null) {
            saveToJSON("isNotation", "false", applicationContext);
        }
        if(readFromJSON("eNotation", applicationContext) == null) {
            saveToJSON("eNotation", "false", applicationContext);
        }
        if(readFromJSON("showShiftRow", applicationContext) == null) {
            saveToJSON("showShiftRow", "false", applicationContext);
        }
        if(readFromJSON("shiftRow", applicationContext) == null) {
            saveToJSON("shiftRow", "1", applicationContext);
        }
        if(readFromJSON("logX", applicationContext) == null) {
            saveToJSON("logX", "false", applicationContext);
        }
        if(readFromJSON("calculationMode", applicationContext) == null) {
            saveToJSON("calculationMode", "Standard", applicationContext);
        }
        if(readFromJSON("currentVersion", applicationContext) == null) {
            saveToJSON("currentVersion", "1.6.3", applicationContext);
        }
        if(readFromJSON("old_version", applicationContext) == null) {
            saveToJSON("old_version", "0", applicationContext);
        }
        if(readFromJSON("returnToCalculator", applicationContext) == null) {
            saveToJSON("returnToCalculator", "false", applicationContext);
        }
        if(readFromJSON("allowDailyNotifications", applicationContext) == null) {
            saveToJSON("allowDailyNotifications", "false", applicationContext);
        }
        if(readFromJSON("allowRememberNotifications", applicationContext) == null) {
            saveToJSON("allowRememberNotifications", "false", applicationContext);
        }
        if(readFromJSON("notificationSent", applicationContext) == null) {
            saveToJSON("notificationSent", "false", applicationContext);
        }
        if(readFromJSON("pressedCalculate", applicationContext) == null) {
            saveToJSON("pressedCalculate", "false", applicationContext);
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
                String calculateText = mainActivity.getCalculateText();
                String resultText = mainActivity.getResultText();

                // Save calculate_text using dataManager
                saveToJSON("calculate_text", calculateText, applicationContext);

                // Save result_text using dataManager
                saveToJSON("result_text", resultText, applicationContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method loads numbers from two files and sets the text of two TextViews.
     */
    public void loadNumbers() {
        if (mainActivity != null) {
            // Load calculate_text using dataManager
            String calculateText = readFromJSON("calculate_text", mainActivity.getApplicationContext());

            // Load result_text using dataManager
            String resultText = readFromJSON("result_text", mainActivity.getApplicationContext());

            TextView calculatelabel = mainActivity.findViewById(R.id.calculate_label);
            TextView resultlabel = mainActivity.findViewById(R.id.result_label);

            if (calculatelabel != null && resultlabel != null) {
                calculatelabel.setText(calculateText);
                if (resultText != null && !resultText.isEmpty()) {
                    resultlabel.setText(resultText);
                } else {
                    resultlabel.setText("0");
                }
            }
        }
    }
}