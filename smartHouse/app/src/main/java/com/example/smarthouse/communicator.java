package com.example.smarthouse;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

public class communicator extends AsyncTask<Void, Void, String> {

    static final private String SERVER_IP = "192.168.0.111";
    static final private int SERVER_PORT = 11223;

    static private Socket socket;
    static private DataOutputStream output;
    static private BufferedReader input;

    static private Activity activity;
    static private RSA rsa;

    public communicator(Activity activity)
    {
        communicator.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {

        String message = "";

        try
        {
            try
            {
              // create the encryption keys
              rsa = new RSA(16);

            }
            catch (Exception e)
            {
                Log.e("myErrorCreateKeys", e.toString());
            }


            try
            {

                // create the connection to the server
                communicator.socket = new Socket(SERVER_IP, SERVER_PORT);
                communicator.output = new DataOutputStream(socket.getOutputStream());
                communicator.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            catch (Exception e)
            {
                Log.e("myErrorConnect", e.toString());
            }

            try
            {
               // send the keys to the server and recover his keys
               exchangeKeys();
            }
            catch (Exception e)
            {
                Log.e("myErrorExchangeKeys", e.toString());
            }

            Thread.sleep(2000);
        }
        catch (Exception e)
        {
            Log.e("myErrorStart", e.toString());
        }


        // wait for messages from the server
        while(true)
        {
            try {

                // read and deycript the data
                message = read();
                final String res = rsa.decryptText(message);

                // show it to the client
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, res, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e)
            {
                Log.e("myErrorReadSocket", e.toString());
            }
        }

    }


    /*
   the function send text to the server
    */
    static void write(final String text)
    {

            new Thread(new Runnable(){
                @Override
                public void run() {

                    try
                    {
                        communicator.output.writeUTF(text);
                        communicator.output.flush();

                    } catch (IOException e)
                    {
                        Log.e("myErrorSendSocket", e.toString());
                    }

                }
            }).start();
    }

    /*
    the function send encrypted text to the server
     */
    static void writeRsa(final String text)
    {
        new Thread(new Runnable(){
            @Override
            public void run() {

                try
                {
                    Thread.sleep(100);
                    String a = rsa.encryptText(text);
                    communicator.output.writeUTF(a);
                    communicator.output.flush();

                } catch (Exception e)
                {
                    Log.e("myErrorSendSocket", e.toString());
                }

            }
        }).start();
    }

    /*
    the function send binary data to the server


     */
    static void write(final byte[] bytes)
    {


        new Thread(new Runnable(){
            @Override
            public void run() {

                try
                {
                    Thread.sleep(100);
                    communicator.output.write(bytes);
                    Log.e("myErrorSendSocket", "aaa");


                } catch (Exception e)
                {
                    Log.e("myErrorSendSocket", e.toString());
                }

            }
        }).start();
    }

    /*
    the function exchange keys with the server
     */
    static void exchangeKeys() throws IOException, JSONException {
        String t = String.valueOf(rsa.publicKeyToJson());
        write(t);

        String jsonString = read();
        Log.e("DataData", jsonString);

        JSONObject public_key = new JSONObject(jsonString);
        rsa.jsonToServerPublicKey(public_key);
    }

    /*
    the function read data from the server
     */
    static String read()
    {
        char buffer[] = new char[1024];

        try
        {
            int bytesRead = input.read(buffer, 0, buffer.length);
            return new String(buffer, 0, bytesRead);

        } catch (Exception e)
        {

        }

        return "";
    }

}
