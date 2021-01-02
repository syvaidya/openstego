/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.openstego.desktop.OpenStegoException;

/**
 * Utility class to manipulate strings
 */
public class StringUtil {
    /**
     * Hexadecimal characters
     */
    private static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };

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
        byte[] hex = new byte[2 * raw.length];
        int index = 0;
        int byteVal;

        for (int i = 0; i < raw.length; i++) {
            byteVal = raw[i] & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[byteVal >>> 4];
            hex[index++] = HEX_CHAR_TABLE[byteVal & 0xF];
        }
        return new String(hex);
    }

    /**
     * Method to get the long hash from the password. This is used for seeding the random number generator
     *
     * @param password Password to hash
     * @return Long hash of the password
     */
    public static long passwordHash(String password) throws OpenStegoException {
        final long DEFAULT_HASH = 98234782; // Default to a random (but constant) seed
        byte[] byteHash = null;
        String hexString = null;

        if (password == null || password.equals("")) {
            return DEFAULT_HASH;
        }

        try {
            byteHash = MessageDigest.getInstance("MD5").digest(password.getBytes());
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
     * @throws OpenStegoException
     */
    public static List<String> getStringLines(String input) throws OpenStegoException {
        String str = null;
        List<String> stringList = new ArrayList<String>();
        BufferedReader reader = null;

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

    /**
     * Checks whether the given string is null or empty
     *
     * @param val Input string
     * @return flag
     */
    public static boolean isNullOrEmpty(String val) {
        return (val == null || val.length() == 0);
    }
}
