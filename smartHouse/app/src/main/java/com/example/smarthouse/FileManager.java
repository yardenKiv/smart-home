package com.example.smarthouse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileManager {

    public static final String SETTINGS_FILE = "settings.txt";

    private FileWriter writer;
    private Context context;

    private static final FileManager ourInstance = new FileManager();

    /*
     *
     * create the output file that will contain all the data from the program
     * and create the file writer
     *
     * */
    public void FileManager(String fileName, Context context)
    {

        try {

            File root = context.getExternalFilesDir(null);
            root.listFiles();
            File outputFile;
            String newFileName = fileName;

            this.context = context;

            outputFile = new File(root, newFileName + ".csv");

            this.writer = new  FileWriter(outputFile, true);

            if (outputFile.length() == 0)
                this.writer.append("Time(sec),IRL time,Distance\n");

            this.writer.flush();

        } catch (Exception e) {
            Log.e("ErrorCreate", e.toString());
        }

    }

    static public void writeToFile(Context context, String fileName, String data)
    {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String readFromFile(Context context, String fileName)
    {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    /*
     *
     * write into the csv output file of the program
     * containing all the data that have bean collected
     *
     * */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void writeOutputFile(String time, String timeIRL, String distance)
    {

        try
        {
            this.writer.append(time + "," +  timeIRL +  "," + distance + "\n");
            this.writer.flush();

        }catch(Exception e)
        {
            Log.e("ErrorCSVWrite", e.toString());
        }
    }

    public static FileManager getInstance() {
        return FileManager.ourInstance;
    }


}




