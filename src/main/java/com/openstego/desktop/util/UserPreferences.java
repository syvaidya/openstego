/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2017 Samir Vaidya
 */
package com.openstego.desktop.util;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.OpenStegoErrors;
import com.openstego.desktop.OpenStegoException;

import java.io.*;
import java.util.Properties;

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
     * @throws OpenStegoException Processing issues
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
            try {
                boolean res = prefFile.createNewFile();
                assert res;
            } catch (IOException e) {
                throw new OpenStegoException(e);
            }

            try (InputStream tmplIS = UserPreferences.class.getResourceAsStream("/" + DEFAULT_PREF_FILENAME);
                 OutputStream prefFileOS = new FileOutputStream(prefFile)) {
                int len;
                byte[] buff = new byte[1024];
                assert tmplIS != null;
                while ((len = tmplIS.read(buff)) >= 0) {
                    prefFileOS.write(buff, 0, len);
                }
            } catch (IOException e) {
                throw new OpenStegoException(e);
            }
        }

        try (InputStream prefFileIS = new FileInputStream(prefFile)) {
            prefs.load(prefFileIS);
        } catch (IOException e) {
            throw new OpenStegoException(e);
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
     * @throws OpenStegoException Processing issues
     */
    @SuppressWarnings("unused")
    public static Integer getInteger(String key) throws OpenStegoException {
        String val = getString(key);
        if (val == null) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.USERPREF_INVALID_INT, key);
        }
    }

    /**
     * Returns the user preference in form of float
     *
     * @param key Preference key
     * @return value
     * @throws OpenStegoException Processing issues
     */
    public static Float getFloat(String key) throws OpenStegoException {
        String val = getString(key);
        if (val == null) {
            return null;
        }
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException e) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.USERPREF_INVALID_FLOAT, key);
        }
    }

    /**
     * Returns the user preference in form of boolean
     *
     * @param key Preference key
     * @return value
     * @throws OpenStegoException Processing issues
     */
    @SuppressWarnings("unused")
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
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.USERPREF_INVALID_BOOL, key);
        }
    }
}
