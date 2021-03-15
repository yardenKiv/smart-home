package com.example.smarthouse;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Random;

public class RsaMath {

    public static int gcd(int p, int q)
    {
        BigInteger b1 = BigInteger.valueOf(q);
        BigInteger b2 = BigInteger.valueOf(p);
        BigInteger gcd = b1.gcd(b2);
        return gcd.intValue();
    }

    public static boolean isCoprime(int x, int y)
    {
        return gcd(x, y) == 1;
    }

    public static int generatePrime(int max)
    {
        int num = 0;
        int min = 1;

        Random rand = new Random(); // generate a random number
        num = rand.nextInt(max - min) + 1 + min;

        while (!isPrime(num)) {
            num = rand.nextInt(max - min) + 1 + min;
        }

        return num;
    }

    public static boolean isPrime(int inputNum)
    {
        if (inputNum <= 3 || inputNum % 2 == 0)
            return inputNum == 2 || inputNum == 3; //this returns false if number is <=1 & true if number = 2 or 3
        int divisor = 3;
        while ((divisor <= Math.sqrt(inputNum)) && (inputNum % divisor != 0))
            divisor += 2; //iterates through all possible divisors
        return inputNum % divisor != 0; //returns true/false
    }


}
