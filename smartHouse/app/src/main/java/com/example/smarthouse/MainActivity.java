package com.example.smarthouse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

public class MainActivity extends AppCompatActivity {

    private int id = 0;
    private final int INVALID_ID = 0;

    private static final int PICTURE_RESULT = 2;
    private static final int ADD_PICTURE_RESULT = 3;
    private static final int IMAGE_QUALITY = 50;

    private BottomNavigationView bottomNavigationView;
    private Fragment fragment = null;

    private Uri imageUri;
    private Bitmap bit;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set the tool bar
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // set the bottom nav bar
        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);

        // move to the home fragment
        fragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

        // setup the file manager
        FileManager fileManager = FileManager.getInstance();
        String jsonString = fileManager.readFromFile(getApplicationContext(), FileManager.SETTINGS_FILE);
        JSONObject jsonObject  = null;

        // get the data from the current settings
        try {

            jsonObject = new JSONObject(jsonString);
            id = Integer.valueOf(jsonObject.getString("id"));

        } catch (Exception e) {

        }

        // start the connection to the server
        new communicator(this).execute();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addOpenCamera();
                return true;

            case R.id.settings:
                Intent switchActivityIntent = new Intent(this, Settings.class);
                startActivity(switchActivityIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addOpenCamera()
    {

        // check if the id is invalid
        if (id <= INVALID_ID)
        {
            Toast.makeText(this, "Invalid id", Toast.LENGTH_SHORT).show();
            return;
        }

        // open the camera and set it to add picture
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, ADD_PICTURE_RESULT);
    }

    public void clickOpenCamera()
    {

        // check if the id is invalid
        if (id <= INVALID_ID)
        {
            Toast.makeText(this, "Invalid id", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PICTURE_RESULT);

    }

    public void closeLock()
    {
        JSONObject userRequest = new JSONObject();

        if (id == INVALID_ID)
        {
            Toast.makeText(this, "Enter id", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            userRequest.put("state", "close_lock");
            userRequest.put("id", new Integer(id));
            userRequest.put("data", "-");
            Log.e("DataReadSocket", "send");
            communicator.writeRsa(userRequest.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    // called when the camera return a picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case PICTURE_RESULT:
                if (requestCode == PICTURE_RESULT)
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            JSONObject userRequest = new JSONObject();

                            // save the image
                            bit = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                            // send the image
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            bit.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, bout);

                            userRequest.put("state", "check_image");
                            userRequest.put("id", new Integer(id));
                            userRequest.put("data", new Integer(bout.size()));

                            communicator.writeRsa(userRequest.toString());
                            Log.e("DataSendImg", "send img meta data");

                            Thread.sleep(1000);

                            communicator.write(bout.toByteArray());

                            Log.e("DataSendImg", "send image");

                        } catch (Exception e) {
                            Log.e("myErrorSenImg", e.toString());
                        }

                    }

            case ADD_PICTURE_RESULT:
                if (requestCode == ADD_PICTURE_RESULT)
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            JSONObject userRequest = new JSONObject();

                            // save the image
                            bit = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                            // send the image
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            bit.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, bout);

                            userRequest.put("state", "insert_image");
                            userRequest.put("id", new Integer(id));
                            userRequest.put("data", new Integer(bout.size()));

                            communicator.writeRsa(userRequest.toString());
                            Log.e("DataSendImg", "send img meta data");

                            Thread.sleep(1000);

                            communicator.write(bout.toByteArray());

                            Log.e("DataSendImg", "send image");

                        } catch (Exception e) {
                            Log.e("myErrorSenImg", e.toString());
                        }

                    }
        }

    }


    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId())
            {
                case R.id.home:
                    fragment = new HomeFragment();
                    break;

                case R.id.lock:
                    fragment = new LockFragment(MainActivity.this);
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            return true;
        }
    };
}