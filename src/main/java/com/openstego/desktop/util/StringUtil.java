/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import com.openstego.desktop.OpenStegoException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to manipulate strings
 */
public class StringUtil {
    /**
     * Constructor is private so that this class is not instantiated
     */
    private StringUtil() {
    }

    /**
     * Method to convert byte array to hexadecimal string
     *
     * @param raw Raw byte array
     * @return Hex string
     */
    public static String getHexString(byte[] raw) {
        BigInteger bigInteger = new BigInteger(1, raw);
        return String.format("%0" + (raw.length << 1) + "x", bigInteger);
    }

    /**
     * Method to get the long hash from the password. This is used for seeding the random number generator
     *
     * @param password Password to hash
     * @return Long hash of the password
     */
    public static long passwordHash(String password) throws OpenStegoException {
        final long DEFAULT_HASH = 98234782; // Default to a random (but constant) seed
        byte[] byteHash;
        String hexString;

        if (password == null || password.equals("")) {
            return DEFAULT_HASH;
        }

        try {
            byteHash = MessageDigest.getInstance("MD5").digest(password.getBytes(StandardCharsets.UTF_8));
            hexString = getHexString(byteHash);

            // Hex string will be 32 bytes long whereas parsing to long can handle only 16 bytes, so trim it
            hexString = hexString.substring(0, 15);
            return Long.parseLong(hexString, 16);
        } catch (NoSuchAlgorithmException nsaEx) {
            throw new OpenStegoException(nsaEx);
        }
    }

    /**
     * Method to tokenize a string by line breaks
     *
     * @param input Input string
     * @return List of strings tokenized by line breaks
     * @throws OpenStegoException Processing issues
     */
    public static List<String> getStringLines(String input) throws OpenStegoException {
        String str;
        List<String> stringList = new ArrayList<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new StringReader(input));
            while ((str = reader.readLine()) != null) {
                str = str.trim();
                if (str.equals("") || str.startsWith("#")) {
                    continue;
                }
                stringList.add(str.trim());
            }
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }

        return stringList;
    }
}
