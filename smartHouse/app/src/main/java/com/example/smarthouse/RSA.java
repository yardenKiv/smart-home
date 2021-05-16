package com.example.smarthouse;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.smarthouse.RsaMath.generatePrime;
import static com.example.smarthouse.RsaMath.isCoprime;

import java.math.BigInteger;
import java.security.SecureRandom;


public class RSA {
    private final static BigInteger one = new BigInteger("1");
    private final static SecureRandom random = new SecureRandom();

    private BigInteger privateKey;
    private BigInteger publicKey;
    private BigInteger modulus;
    private BigInteger serverPublicKey;
    private BigInteger serverModulus;

    // generate an N-bit (roughly) public and private key
    RSA(int N) {

        BigInteger p = BigInteger.probablePrime(N / 2, random);
        BigInteger q = BigInteger.probablePrime(N / 2, random);
        BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));


        modulus = p.multiply(q);
        publicKey = new BigInteger("65537");     // common value in practice = 2^16 + 1
        privateKey = publicKey.modInverse(phi);
    }


    BigInteger encrypt(BigInteger message) {
        return message.modPow(serverPublicKey, serverModulus);
    }

    BigInteger decrypt(BigInteger encrypted) {
        return encrypted.modPow(privateKey, modulus);
    }

    public String encryptText(String text)
    {
        String msg = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            BigInteger num = BigInteger.valueOf(letter);

            BigInteger res = this.encrypt(num);
            msg = msg + res.toString() + " ";
        }

        msg = msg.substring(0, msg.length() - 1);

        return msg;
    }

    public String decryptText(String text)
    {
        String msg = "";

        String[] splited = text.split("\\s+");

        for (String letter : splited) {
            BigInteger num = BigInteger.valueOf(Long.parseLong(letter));

            BigInteger res = this.decrypt(num);
            msg = msg + (char) res.intValue();
        }

        return msg;
    }

    public String toString() {
        String s = "";
        s += "public  = " + publicKey + "\n";
        s += "private = " + privateKey + "\n";
        s += "server public = " + privateKey + "\n";
        s += "server modulus = " + serverModulus + "\n";
        s += "modulus = " + modulus;
        return s;
    }


    // convert the keys to json

    public  JSONObject publicKeyToJson() throws JSONException {
        JSONObject a = new JSONObject();
        a.put("e", this.publicKey.intValue());
        a.put("n", this.modulus.intValue());

        return a;
    }

    public  JSONObject privateKeyToJson() throws JSONException {
        JSONObject a = new JSONObject();
        a.put("d", this.privateKey.intValue());
        a.put("n", this.modulus.intValue());

        return a;
    }


    // assign the keys from json to the object

    public void jsonToPrivateKey(JSONObject a) throws JSONException {
        int e = a.getInt("e");
        int n = a.getInt("n");

        this.publicKey = BigInteger.valueOf(e);
        this.modulus = BigInteger.valueOf(n);
    }

    public void jsonToPublicKey(JSONObject a) throws JSONException {
        int e = a.getInt("e");
        int n = a.getInt("n");

        this.publicKey = BigInteger.valueOf(e);
        this.modulus = BigInteger.valueOf(n);
    }

    public void jsonToServerPublicKey(JSONObject a) throws JSONException {
        int e = a.getInt("e");
        int n = a.getInt("n");

        this.serverPublicKey = BigInteger.valueOf(e);
        this.serverModulus = BigInteger.valueOf(n);
    }
}
