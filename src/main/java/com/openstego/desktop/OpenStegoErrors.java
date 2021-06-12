/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import static com.openstego.desktop.OpenStego.NAMESPACE;
import static com.openstego.desktop.OpenStegoException.addErrorCode;

/**
 * Custom exception class for OpenStego
 */
public class OpenStegoErrors {
    /**
     * Error Code - Invalid password
     */
    public static final int INVALID_PASSWORD = 1;

    /**
     * Error Code - Invalid value for useCompression
     */
    public static final int INVALID_USE_COMPR_VALUE = 2;

    /**
     * Error Code - Invalid value for useEncryption
     */
    public static final int INVALID_USE_ENCRYPT_VALUE = 3;

    /**
     * Error Code - Invalid key name
     */
    public static final int INVALID_KEY_NAME = 4;

    /**
     * Error Code - Corrupt Data
     */
    public static final int CORRUPT_DATA = 5;

    /**
     * Error Code - No valid plugin
     */
    public static final int NO_VALID_PLUGIN = 6;

    /**
     * Error Code - Image type invalid
     */
    public static final int IMAGE_TYPE_INVALID = 7;

    /**
     * Error Code - Image file invalid
     */
    public static final int IMAGE_FILE_INVALID = 8;

    /**
     * Error Code - No plugin specified
     */
    public static final int NO_PLUGIN_SPECIFIED = 9;

    /**
     * Error Code - Plugin does not support watermarking
     */
    public static final int PLUGIN_DOES_NOT_SUPPORT_WM = 10;

    /**
     * Error Code - Plugin not found
     */
    public static final int PLUGIN_NOT_FOUND = 11;

    /**
     * Error Code - Image sizes mismatch
     */
    public static final int IMAGE_SIZE_MISMATCH = 12;

    /**
     * Error Code - Plugin does not support data hiding
     */
    public static final int PLUGIN_DOES_NOT_SUPPORT_DH = 14;

    /**
     * Error Code - Password is mandatory for 'gensig' operation
     */
    public static final int PWD_MANDATORY_FOR_GENSIG = 15;

    /**
     * Error Code - Invalid key name
     */
    public static final int INVALID_CRYPT_ALGO = 16;

    /**
     * Error Code - Invalid integer in user preference file
     */
    public static final int USERPREF_INVALID_INT = 17;

    /**
     * Error Code - Invalid float in user preference file
     */
    public static final int USERPREF_INVALID_FLOAT = 18;

    /**
     * Error Code - Invalid boolean in user preference file
     */
    public static final int USERPREF_INVALID_BOOL = 19;

    /**
     * Initialize the error code - message key map
     */
    public static void init() {
        addErrorCode(NAMESPACE, INVALID_PASSWORD, "err.config.password.invalid");
        addErrorCode(NAMESPACE, INVALID_USE_COMPR_VALUE, "err.config.useCompression.invalid");
        addErrorCode(NAMESPACE, INVALID_USE_ENCRYPT_VALUE, "err.config.useEncryption.invalid");
        addErrorCode(NAMESPACE, INVALID_KEY_NAME, "err.config.invalidKey");
        addErrorCode(NAMESPACE, INVALID_CRYPT_ALGO, "err.config.invalidCryptAlgo");
        addErrorCode(NAMESPACE, USERPREF_INVALID_INT, "err.userpref.valueNotInteger");
        addErrorCode(NAMESPACE, USERPREF_INVALID_FLOAT, "err.userpref.valueNotFloat");
        addErrorCode(NAMESPACE, USERPREF_INVALID_BOOL, "err.userpref.valueNotBoolean");
        addErrorCode(NAMESPACE, CORRUPT_DATA, "err.corruptData");
        addErrorCode(NAMESPACE, NO_VALID_PLUGIN, "err.noValidPlugin");
        addErrorCode(NAMESPACE, IMAGE_TYPE_INVALID, "err.image.type.invalid");
        addErrorCode(NAMESPACE, IMAGE_FILE_INVALID, "err.image.file.invalid");
        addErrorCode(NAMESPACE, NO_PLUGIN_SPECIFIED, "err.plugin.notSpecified");
        addErrorCode(NAMESPACE, PLUGIN_DOES_NOT_SUPPORT_WM, "err.plugin.wmNotSupported");
        addErrorCode(NAMESPACE, PLUGIN_DOES_NOT_SUPPORT_DH, "err.plugin.dhNotSupported");
        addErrorCode(NAMESPACE, PLUGIN_NOT_FOUND, "err.plugin.notFound");
        addErrorCode(NAMESPACE, IMAGE_SIZE_MISMATCH, "err.image.size.mismatch");
        addErrorCode(NAMESPACE, PWD_MANDATORY_FOR_GENSIG, "err.gensig.pwdMandatory");
    }
}
