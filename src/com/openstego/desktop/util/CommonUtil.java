/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTextField;
import javax.swing.UIManager;

import com.openstego.desktop.OpenStegoException;

/**
 * Common utilities for OpenStego
 */
public class CommonUtil {
    /**
     * Constructor is private so that this class is not instantiated
     */
    private CommonUtil() {
    }

    /**
     * Method to get byte array data from given InputStream
     *
     * @param is InputStream to read
     * @return Stream data as byte array
     * @throws OpenStegoException
     */
    public static byte[] getStreamBytes(InputStream is) throws OpenStegoException {
        final int BUF_SIZE = 512;
        ByteArrayOutputStream bos = null;
        int bytesRead = 0;
        byte[] data = null;

        try {
            data = new byte[BUF_SIZE];
            bos = new ByteArrayOutputStream();

            while ((bytesRead = is.read(data, 0, BUF_SIZE)) >= 0) {
                bos.write(data, 0, bytesRead);
            }

            is.close();
            bos.close();

            return bos.toByteArray();
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to get byte array data from given file
     *
     * @param file File to read
     * @return File data as byte array
     * @throws OpenStegoException
     */
    public static byte[] getFileBytes(File file) throws OpenStegoException {
        try {
            return getStreamBytes(new FileInputStream(file));
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to write file data to disk
     *
     * @param fileData File data
     * @param fileName File name (If this is <code>null</code>, then data is written to stdout)
     * @throws OpenStegoException
     */
    public static void writeFile(byte[] fileData, String fileName) throws OpenStegoException {
        File file = null;

        if (fileName != null) {
            file = new File(fileName);
        }
        writeFile(fileData, file);
    }

    /**
     * Method to write file data to disk
     *
     * @param fileData File data
     * @param file File object (If this is <code>null</code>, then data is written to stdout)
     * @throws OpenStegoException
     */
    public static void writeFile(byte[] fileData, File file) throws OpenStegoException {
        OutputStream os = null;

        try {
            // If file is not provided, then write the data to stdout
            if (file == null) {
                os = System.out;
            } else {
                os = new FileOutputStream(file);
            }
            os.write(fileData);
            os.close();
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to enable/disable a Swing JTextField object
     *
     * @param textField Swing JTextField object
     * @param enabled Flag to indicate whether to enable or disable the object
     */
    public static void setEnabled(JTextField textField, boolean enabled) {
        if (enabled) {
            textField.setEnabled(true);
            textField.setBackground(Color.WHITE);
        } else {
            textField.setEnabled(false);
            textField.setBackground(UIManager.getColor("Panel.background"));
        }
    }

    /**
     * Method to parse a delimiter separated list of files into arraylist of filenames. It supports wildcard characters
     * "*" and "?" within the filenames.
     *
     * @param fileList Delimiter separated list of filenames
     * @param delimiter Delimiter for tokenization
     * @return List of filenames after tokenizing and wildcard expansion
     */
    public static List<File> parseFileList(String fileList, String delimiter) {
        int index = 0;
        StringTokenizer tokenizer = null;
        String fileName = null;
        String dirName = null;
        List<File> output = new ArrayList<File>();
        File fileDir = null;
        File[] arrFile = null;

        if (fileList == null) {
            return output;
        }

        tokenizer = new StringTokenizer(fileList, delimiter);
        while (tokenizer.hasMoreTokens()) {
            fileName = tokenizer.nextToken().trim();
            index = fileName.lastIndexOf(File.separator);

            if (index >= 0) {
                dirName = fileName.substring(0, index);
                fileName = fileName.substring(index + 1);
            } else {
                dirName = ".";
            }
            fileName = replaceWildcards(fileName);

            fileDir = new File(dirName.equals("") ? "." : dirName);
            arrFile = fileDir.listFiles(new WildcardFilenameFilter(fileName));

            for (int i = 0; i < arrFile.length; i++) {
                output.add(arrFile[i]);
            }
        }

        return output;
    }

    /**
     * Byte to Int converter
     *
     * @param b Input byte value
     * @return Int value
     */
    public static int byteToInt(int b) {
        int i = b;
        if (i < 0) {
            i = i + 256;
        }
        return i;
    }

    /**
     * Helper method to replace file wildcard characters with Java regexp wildcard chararcters
     *
     * @param input Input String
     * @return String containing modified wildcard characters
     */
    private static String replaceWildcards(String input) {
        StringBuffer buffer = new StringBuffer();
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '*') {
                buffer.append(".*");
            } else if (chars[i] == '?') {
                buffer.append(".{1}");
            } else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1) // Escape rest of the java regexp wildcards
            {
                buffer.append('\\').append(chars[i]);
            } else {
                buffer.append(chars[i]);
            }
        }

        return buffer.toString();
    }

    /**
     * Inner class for wildcard filename filter
     */
    static class WildcardFilenameFilter implements FilenameFilter {
        /**
         * Variable to hold the filter string
         */
        String filter = null;

        /**
         * Default constructor
         *
         * @param filter Filter string
         */
        public WildcardFilenameFilter(String filter) {
            this.filter = filter.toLowerCase();
        }

        /**
         * Implementation of <code>accept</code> method
         *
         * @param dir Directory to traverse
         * @param name Name of the file
         * @return Whether file is accepted by the filter or not
         */
        @Override
        public boolean accept(File dir, String name) {
            return (name.toLowerCase().matches(this.filter));
        }
    }

    /**
     * Returns the floor of the half of the input value
     *
     * @param num Input number
     * @return Floor of the half of the input number
     */
    public static int floorHalf(int num) {
        if ((num & 1) == 1) {
            return (num - 1) / 2;
        } else {
            return num / 2;
        }
    }

    /**
     * Returns the ceiling of the half of the input value
     *
     * @param num Input number
     * @return Ceiling of the half of the input number
     */
    public static int ceilingHalf(int num) {
        if ((num & 1) == 1) {
            return (num + 1) / 2;
        } else {
            return num / 2;
        }
    }

    /**
     * Returns the modulus of the input value (taking care of the sign of the value)
     *
     * @param num Input number
     * @param div Divisor for modulus
     * @return Modulus of num by div
     */
    public static int mod(int num, int div) {
        if (num < 0) {
            return div - (-num % div);
        } else {
            return num % div;
        }
    }

    /**
     * Get maximum of two given values
     *
     * @param x Value 1
     * @param y value 2
     * @return Max of the two values
     */
    public static int max(int x, int y) {
        return (x > y) ? x : y;
    }

    /**
     * Get maximum of two given values
     *
     * @param x Value 1
     * @param y value 2
     * @return Max of the two values
     */
    public static double max(double x, double y) {
        return (x > y) ? x : y;
    }

    /**
     * Get minimum of two given values
     *
     * @param x Value 1
     * @param y value 2
     * @return Min of the two values
     */
    public static int min(int x, int y) {
        return (x < y) ? x : y;
    }

    /**
     * Get minimum of two given values
     *
     * @param x Value 1
     * @param y value 2
     * @return Min of the two values
     */
    public static double min(double x, double y) {
        return (x < y) ? x : y;
    }
}
