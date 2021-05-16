package com.example.smarthouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Settings extends AppCompatActivity {

    private FileManager fileManager;

    private EditText idInput;
    private Toolbar toolbar;

    private int id = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        idInput = findViewById(R.id.id_input);

        // setup the ui
        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        // setup the file manager
        fileManager = FileManager.getInstance();
        String jsonString = fileManager.readFromFile(getApplicationContext(), FileManager.SETTINGS_FILE);

        // get the current settings
        try {

            JSONObject jsonObject  = new JSONObject(jsonString);
            idInput.setText(jsonObject.getString("id"));
            id = Integer.valueOf(jsonObject.getString("id"));

        } catch (JSONException e) {
            Log.e("ErrorJasonGet", e.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.back:
                saveId();
                Intent switchActivityIntent = new Intent(this, MainActivity.class);
                startActivity(switchActivityIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    save the settings that the user have entered
     */
    public void saveId() {

        try
        {
            id = Integer.valueOf(idInput.getText().toString());
        }
        catch (Exception e) {
            return;
        }

        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            String userString = jsonObject.toString();

            fileManager.writeToFile(getApplicationContext(), FileManager.SETTINGS_FILE, userString);;

            System.out.println(jsonObject);

        } catch (Exception e) {
            Log.e("ErrorJasonPut", e.toString());
        }

    }

}