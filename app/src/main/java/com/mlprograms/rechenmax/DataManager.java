package com.mlprograms.rechenmax;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.CompoundButton;
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

public class DataManager {
    private static final String JSON_FILE = "settings.json";
    private HistoryActivity historyActivity;
    private MainActivity mainActivity;
    public DataManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    private SettingsActivity settingsActivity;

    public DataManager(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }

    public DataManager(HistoryActivity historyActivity) {
        this.historyActivity = historyActivity;
    }

    public void createJSON(Context applicationContext) {
        File file = new File(applicationContext.getFilesDir(), JSON_FILE);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void deleteJSON(Context applicationContext) {
        File file = new File(applicationContext.getFilesDir(), JSON_FILE);
        file.delete();
    }
    
    // names                            | values                | context
    // settingReleaseNotesSwitch        | true / false          | SettingsAcitiy
    // settingsTrueDarkMode             | true / false          | MainActivtiy -> SettingsAcitiy
    // showPatchNotes                   | true / false          | MainActivtiy -> SettingsAcitiy
    // disablePatchNotesTemporary       | true / false          | MainActivtiy -> SettingsAcitiy
    // showReleaseNotesOnVeryFirstStart | true / false          | MainActivtiy
    // selectedSpinnerSetting           | System / Dark / Light | MainActivtiy
    // -> = ... wird übergeben zu ...

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

    // names                            | values                   | context
    // settingReleaseNotesSwitch        | true / false             | SettingsActivity
    // settingsTrueDarkMode             | true / false             | MainActivity -> SettingsActivity
    // showPatchNotes                   | true / false             | MainActivity -> SettingsActivity
    // disablePatchNotesTemporary       | true / false             | MainActivity -> SettingsActivity
    // showReleaseNotesOnVeryFirstStart | true / false             | MainActivity
    // selectedSpinnerSetting           | System / Dark / Light    | MainActivity
    // showScienceRow                   | true / false             | MainActivity
    // calculatingMode                  | easy / normal            | MainActivity
    // -> = ... wird übergeben zu ...

    public String readFromJSON(final String name, Context applicationContext) {
        String setting = null;
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (!file.exists()) {
                Log.e("readFromJSON", "File does not exist");
                initializeSettings(applicationContext);
                if (!file.createNewFile()) {
                    Log.e("saveToJSON", "Failed to create new file");
                    return null;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (content.isEmpty()) {
                Log.e("readFromJSON", "File is empty");
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
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return setting;
    }
    private void initializeSettings(Context applicationContext) {
        saveToJSON("settingReleaseNotesSwitch", "true", applicationContext);
        saveToJSON("settingsTrueDarkMode", "false", applicationContext);
        saveToJSON("showPatchNotes", "true", applicationContext);
        saveToJSON("disablePatchNotesTemporary", "false", applicationContext);
        saveToJSON("showReleaseNotesOnVeryFirstStart", "true", applicationContext);
        saveToJSON("selectedSpinnerSetting", "System", applicationContext);
        saveToJSON("showScienceRow", false, applicationContext);
        saveToJSON("calculatingMode", "easy", applicationContext);
    }
    private static final String FILE_NAME1 = "history.txt";
    public void addtoHistory(String data, Context context) {
        try {
            FileInputStream fileIn = context.openFileInput(FILE_NAME1);
            InputStreamReader inputReader = new InputStreamReader(fileIn);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            StringBuilder oldData = new StringBuilder();

            while ((line = bufReader.readLine()) != null) {
                oldData.append(line).append("\n");
            }
            bufReader.close();

            FileOutputStream fileOut = context.openFileOutput(FILE_NAME1, Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileOut);
            outputWriter.write(data + "\n" + oldData.toString());
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void checkAndCreateFile() {
        File file = new File(mainActivity.getFilesDir(), FILE_NAME1);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream fileIn = mainActivity.openFileInput(FILE_NAME1);
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
    private static final String FILE_NAME2 = "calculate.txt";
    private static final String FILE_NAME3 = "result.txt";
    public void saveNumbers(Context applicationContext) {
        if (mainActivity != null) {
            try {
                FileOutputStream fileOut1 = applicationContext.openFileOutput(FILE_NAME2, Context.MODE_PRIVATE);
                OutputStreamWriter outputWriter1 = new OutputStreamWriter(fileOut1);
                outputWriter1.write(mainActivity.getCalculateText().toString());
                outputWriter1.close();

                FileOutputStream fileOut2 = applicationContext.openFileOutput(FILE_NAME3, Context.MODE_PRIVATE);
                OutputStreamWriter outputWriter2 = new OutputStreamWriter(fileOut2);
                outputWriter2.write(mainActivity.getResultText().toString());
                outputWriter2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void loadNumbers() {
        String result1 = "";
        try {
            FileInputStream fileIn = mainActivity.openFileInput(FILE_NAME2);
            InputStreamReader inputReader = new InputStreamReader(fileIn);
            char[] inputBuffer = new char[100];
            int charRead;
            while ((charRead = inputReader.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                result1 += readString;
            }
            inputReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String result2 = "";
        try {
            FileInputStream fileIn = mainActivity.openFileInput(FILE_NAME3);
            InputStreamReader inputReader = new InputStreamReader(fileIn);
            char[] inputBuffer = new char[100];
            int charRead;
            while ((charRead = inputReader.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                result2 += readString;
            }
            inputReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView calclab = (TextView) mainActivity.findViewById(R.id.calculate_label);
        TextView reslab = (TextView) mainActivity.findViewById(R.id.result_label);

        if (mainActivity != null && calclab != null && reslab != null) {
            calclab.setText(result1);
            if (!result2.equals("")) {
                reslab.setText(result2);
            } else {
                reslab.setText("0");
            }
        }
    }
}