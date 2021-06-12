/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import com.openstego.desktop.OpenStegoException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

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
     * @throws OpenStegoException Processing issues
     */
    public static byte[] streamToBytes(InputStream is) throws OpenStegoException {
        final int BUF_SIZE = 512;
        int bytesRead;
        byte[] data;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            data = new byte[BUF_SIZE];

            while ((bytesRead = is.read(data, 0, BUF_SIZE)) >= 0) {
                bos.write(data, 0, bytesRead);
            }

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
     * @throws OpenStegoException Processing issues
     */
    public static byte[] fileToBytes(File file) throws OpenStegoException {
        try (InputStream is = new FileInputStream(file)) {
            return streamToBytes(is);
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to write file data to disk
     *
     * @param fileData File data
     * @param fileName File name (If this is <code>null</code>, then data is written to stdout)
     * @throws OpenStegoException Processing issues
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
     * @param file     File object (If this is <code>null</code>, then data is written to stdout)
     * @throws OpenStegoException Processing issues
     */
    public static void writeFile(byte[] fileData, File file) throws OpenStegoException {
        // If file is not provided, then write the data to stdout
        try (OutputStream os = (file == null ? System.out : new FileOutputStream(file))) {
            os.write(fileData);
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to enable/disable a Swing JTextField object
     *
     * @param textField Swing JTextField object
     * @param enabled   Flag to indicate whether to enable or disable the object
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
     * @param fileList  Delimiter separated list of filenames
     * @param delimiter Delimiter for tokenization
     * @return List of filenames after tokenizing and wildcard expansion
     */
    public static List<File> parseFileList(String fileList, String delimiter) {
        int index;
        StringTokenizer tokenizer;
        String fileName;
        String dirName;
        List<File> output = new ArrayList<>();
        File fileDir;
        File[] arrFile;

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
            if (arrFile != null) {
                Collections.addAll(output, arrFile);
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
        StringBuilder buffer = new StringBuilder();
        char[] chars = input.toCharArray();

        for (char aChar : chars) {
            if (aChar == '*') {
                buffer.append(".*");
            } else if (aChar == '?') {
                buffer.append(".{1}");
            } else if ("+()^$.{}[]|\\".indexOf(aChar) != -1) { // Escape rest of the java regexp wildcards
                buffer.append('\\').append(aChar);
            } else {
                buffer.append(aChar);
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
        private final String filter;

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
         * @param dir  Directory to traverse
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
}
