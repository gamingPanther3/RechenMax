package com.mlprograms.rechenmax;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

/**
 * DataManager
 * @author Max Lemberg
 * @version 1.2.1
 * @date 27.12.2023
 *
 *  | Names                            | Values                   | Context                              |
 *  |----------------------------------|--------------------------|--------------------------------------|
 *  | settingReleaseNotesSwitch        | true / false             | SettingsActivity                     |
 *  | removeValue                      | true / false             | MainActivity                         |
 *  | settingsTrueDarkMode             | true / false             | MainActivity -> SettingsActivity     |
 *  | showPatchNotes                   | true / false             | MainActivity -> SettingsActivity     |
 *  | disablePatchNotesTemporary       | true / false             | MainActivity -> SettingsActivity     |
 *  | showReleaseNotesOnVeryFirstStart | true / false             | MainActivity                         |
 *  | selectedSpinnerSetting           | System / Dark / Light    | MainActivity                         |
 *  | showScienceRow                   | true / false             | MainActivity                         |
 *  | lastop                           | String                   | MainActivity                         |
 *  | lastnumber                       | Integer                  | MainActivity                         |
 *  | historyTextViewNumber            | Integer                  | MainActivity                         |
 *  | result_text                      | String                   | MainActivity                         |
 *  | calculate_text                   | String                   | MainActivity                         |
 *  | rotate_op                        | true / false             | MainActivity                         |
 */
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
    }

    /**
     * This method is used to delete the JSON file from the application's file directory.
     *
     * @param applicationContext The application context, which is used to get the application's file directory.
     */
    public void deleteJSON(Context applicationContext) {
        File file = new File(applicationContext.getFilesDir(), JSON_FILE);
        file.delete();
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
                initializeSettings(applicationContext);
                if (!file.createNewFile()) {
                    Log.e("saveToJSON", "Failed to create new file");
                    initializeSettings(applicationContext);
                    return null;
                }
            } else {
                String content = new String(Files.readAllBytes(file.toPath()));
                if (content.isEmpty()) {
                    Log.e("readFromJSON", "File is empty (" + name + ")");
                    initializeSettings(applicationContext);
                    return readFromJSON(name, applicationContext);
                }
                JSONObject jsonRead = new JSONObject(new JSONTokener(content));
                if (jsonRead.has(name)) {
                    setting = jsonRead.getString(name);
                } else {
                    Log.e("readFromJSON", "Key: " + name + " does not exist in JSON");
                    initializeSettings(applicationContext);
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
        if(readFromJSON("settingReleaseNotesSwitch", applicationContext) == null) {
            saveToJSON("settingReleaseNotesSwitch", "true", applicationContext);
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
        if(readFromJSON("selectedSpinnerSetting", applicationContext) == null) {
            saveToJSON("selectedSpinnerSetting", "System", applicationContext);
        }
        if(readFromJSON("showScienceRow", applicationContext) == null) {
            saveToJSON("showScienceRow", false, applicationContext);
        }
        if(readFromJSON("calculatingMode", applicationContext) == null) {
            saveToJSON("calculatingMode", "easy", applicationContext);
        }
        if(readFromJSON("removeValue", applicationContext) == null) {
            saveToJSON("removeValue", "false", applicationContext);
        }
        if(readFromJSON("result_text", applicationContext) == null) {
            saveToJSON("result_text", "0", applicationContext);
        }
        if(readFromJSON("calculate_text", applicationContext) == null) {
            saveToJSON("calculate_text", "", applicationContext);
        }
        if(readFromJSON("historyTextViewNumber", applicationContext) == null) {
            saveToJSON("historyTextViewNumber", "0", applicationContext);
        }
    }

    /**
     * This method checks if a file exists and creates it if it doesn't.
     */
    public void checkAndCreateFile() {
        File file = new File(mainActivity.getFilesDir(), HISTORY_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
                initializeSettings(mainActivity.getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream fileIn = mainActivity.openFileInput(HISTORY_FILE);
            InputStreamReader inputReader = new InputStreamReader(fileIn);
            char[] inputBuffer = new char[100];
            int charRead;
            while ((charRead = inputReader.read(inputBuffer)) > 0) {
                String.copyValueOf(inputBuffer, 0, charRead);
            }
            inputReader.close();
        } catch (Exception e) {
            e.printStackTrace();
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
                if (resultText != null && !resultText.equals("")) {
                    resultlabel.setText(resultText);
                } else {
                    resultlabel.setText("0");
                }
            }
        }
    }
}