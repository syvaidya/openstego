/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2017 Samir Vaidya
 */
package com.openstego.desktop.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.OpenStegoException;

/**
 * User preferences manager
 */
public class UserPreferences {
    private static final String PREF_FILENAME = "openstego.ini";
    private static final String DEFAULT_PREF_FILENAME = "openstego.default.ini";
    private static Properties prefs = null;

    /**
     * Protected constructor. Expose only static methods
     */
    protected UserPreferences() {
        // Do nothing
    }

    /**
     * Initialize the preferences
     *
     * @throws OpenStegoException
     */
    public static void init() throws OpenStegoException {
        if (prefs != null) {
            return;
        }

        prefs = new Properties();

        // Create user preference file if it does not exist
        String userHome = System.getProperty("user.home");
        File prefFile = new File(userHome, PREF_FILENAME);
        if (!prefFile.exists()) {
            InputStream tmplIS = UserPreferences.class.getResourceAsStream("/" + DEFAULT_PREF_FILENAME);
            OutputStream prefFileOS = null;
            try {
                prefFile.createNewFile();
                prefFileOS = new FileOutputStream(prefFile);
                int len;
                byte[] buff = new byte[1024];
                while ((len = tmplIS.read(buff)) >= 0) {
                    prefFileOS.write(buff, 0, len);
                }
            } catch (IOException e) {
                throw new OpenStegoException(e);
            } finally {
                closeStream(tmplIS);
                closeStream(prefFileOS);
            }
        }

        InputStream prefFileIS = null;
        try {
            prefFileIS = new FileInputStream(prefFile);
            prefs.load(prefFileIS);
        } catch (IOException e) {
            throw new OpenStegoException(e);
        } finally {
            closeStream(prefFileIS);
        }
    }

    /**
     * Returns the user preference in form of string
     *
     * @param key Preference key
     * @return value
     */
    public static String getString(String key) {
        String val = prefs.getProperty(key);
        if (val == null) {
            return null;
        }
        return val.trim();
    }

    /**
     * Returns the user preference in form of integer
     *
     * @param key Preference key
     * @return value
     * @throws OpenStegoException
     */
    public static Integer getInteger(String key) throws OpenStegoException {
        String val = getString(key);
        if (val == null) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.USERPREF_INVALID_INT, key);
        }
    }

    /**
     * Returns the user preference in form of float
     *
     * @param key Preference key
     * @return value
     * @throws OpenStegoException
     */
    public static Float getFloat(String key) throws OpenStegoException {
        String val = getString(key);
        if (val == null) {
            return null;
        }
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException e) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.USERPREF_INVALID_FLOAT, key);
        }
    }

    /**
     * Returns the user preference in form of boolean
     *
     * @param key Preference key
     * @return value
     * @throws OpenStegoException
     */
    public static Boolean getBoolean(String key) throws OpenStegoException {
        String val = getString(key);
        if (val == null) {
            return null;
        }
        val = val.toLowerCase();
        if ("t".equals(val) || "true".equals(val) || "y".equals(val) || "yes".equals(val) || "1".equals(val)) {
            return true;
        } else if ("f".equals(val) || "false".equals(val) || "n".equals(val) || "no".equals(val) || "0".equals(val)) {
            return false;
        } else {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.USERPREF_INVALID_BOOL, key);
        }
    }

    private static void closeStream(InputStream is) {
        try {
            is.close();
        } catch (Exception e) {
            // Ignore
        }
    }

    private static void closeStream(OutputStream os) {
        try {
            os.close();
        } catch (Exception e) {
            // Ignore
        }
    }
}
