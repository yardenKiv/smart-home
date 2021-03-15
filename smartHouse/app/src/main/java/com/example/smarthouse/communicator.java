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

    static private Socket socket;
    static private DataOutputStream output;
    static private BufferedReader input;

    static public boolean sendImage;
    static private Activity activity;
    static private Map<String, JSONObject> keys;
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
              rsa = new RSA(16);

            }
            catch (Exception e)
            {
                Log.e("myErrorCreateKeys", e.toString());
            }


            try
            {
                communicator.socket = new Socket("34.105.163.102", 11223);
                communicator.output = new DataOutputStream(socket.getOutputStream());
                communicator.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            catch (Exception e)
            {
                Log.e("myErrorConnect", e.toString());
            }

            try
            {
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

        Log.e("DataReadSocket", "start read");

        while(true)
        {
            try {

                message = read();


                if (socket.getInputStream().available() != 0)
                {

                    Log.e("DataReadSocket", "got");

                    Log.e("DataReadSocket", message);

                }
            } catch (Exception e)
            {
                Log.e("myErrorReadSocket", e.toString());
            }
        }

    }

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

    static void exchangeKeys() throws IOException, JSONException {
        String t = String.valueOf(rsa.publicKeyToJson());
        write(t);

        String jsonString = read();
        Log.e("DataData", jsonString);

        JSONObject public_key = new JSONObject(jsonString);
        rsa.jsonToServerPublicKey(public_key);
    }

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
