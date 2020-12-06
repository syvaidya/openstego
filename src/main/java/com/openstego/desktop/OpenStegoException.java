/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import java.util.HashMap;
import java.util.Map;

import com.openstego.desktop.util.LabelUtil;

/**
 * Custom exception class for OpenStego
 */
public class OpenStegoException extends Exception {
    private static final long serialVersionUID = 668241029491685413L;

    /**
     * Error Code - Unhandled exception
     */
    static final int UNHANDLED_EXCEPTION = 0;

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
     * Error Code - Out of memory
     */
    public static final int OUT_OF_MEMORY = 13;

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
     * Map to store error code to message key mapping
     */
    private static Map<String, String> errMsgKeyMap = new HashMap<String, String>();

    /**
     * Error code for the exception
     */
    private int errorCode = 0;

    /**
     * Namespace for the exception
     */
    private String namespace = null;

    /*
     * Initialize the error code - message key map
     */
    static {
        addErrorCode(OpenStego.NAMESPACE, INVALID_PASSWORD, "err.config.password.invalid");
        addErrorCode(OpenStego.NAMESPACE, INVALID_USE_COMPR_VALUE, "err.config.useCompression.invalid");
        addErrorCode(OpenStego.NAMESPACE, INVALID_USE_ENCRYPT_VALUE, "err.config.useEncryption.invalid");
        addErrorCode(OpenStego.NAMESPACE, INVALID_KEY_NAME, "err.config.invalidKey");
        addErrorCode(OpenStego.NAMESPACE, INVALID_CRYPT_ALGO, "err.config.invalidCryptAlgo");
        addErrorCode(OpenStego.NAMESPACE, USERPREF_INVALID_INT, "err.userpref.valueNotInteger");
        addErrorCode(OpenStego.NAMESPACE, USERPREF_INVALID_FLOAT, "err.userpref.valueNotFloat");
        addErrorCode(OpenStego.NAMESPACE, USERPREF_INVALID_BOOL, "err.userpref.valueNotBoolean");
        addErrorCode(OpenStego.NAMESPACE, CORRUPT_DATA, "err.corruptData");
        addErrorCode(OpenStego.NAMESPACE, NO_VALID_PLUGIN, "err.noValidPlugin");
        addErrorCode(OpenStego.NAMESPACE, IMAGE_TYPE_INVALID, "err.image.type.invalid");
        addErrorCode(OpenStego.NAMESPACE, IMAGE_FILE_INVALID, "err.image.file.invalid");
        addErrorCode(OpenStego.NAMESPACE, NO_PLUGIN_SPECIFIED, "err.plugin.notSpecified");
        addErrorCode(OpenStego.NAMESPACE, PLUGIN_DOES_NOT_SUPPORT_WM, "err.plugin.wmNotSupported");
        addErrorCode(OpenStego.NAMESPACE, PLUGIN_DOES_NOT_SUPPORT_DH, "err.plugin.dhNotSupported");
        addErrorCode(OpenStego.NAMESPACE, PLUGIN_NOT_FOUND, "err.plugin.notFound");
        addErrorCode(OpenStego.NAMESPACE, IMAGE_SIZE_MISMATCH, "err.image.size.mismatch");
        addErrorCode(OpenStego.NAMESPACE, PWD_MANDATORY_FOR_GENSIG, "err.gensig.pwdMandatory");
    }

    /**
     * Constructor using default namespace for unhandled exceptions
     *
     * @param cause Original exception which caused this exception to be raised
     */
    public OpenStegoException(Throwable cause) {
        this(cause, OpenStego.NAMESPACE, UNHANDLED_EXCEPTION, (Object[]) null);
    }

    /**
     * Default constructor
     *
     * @param cause Original exception which caused this exception to be raised
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     */
    public OpenStegoException(Throwable cause, String namespace, int errorCode) {
        this(cause, namespace, errorCode, (Object[]) null);
    }

    /**
     * Constructor with a single parameter for the message
     *
     * @param cause Original exception which caused this exception to be raised
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     * @param param Parameter for exception message
     */
    public OpenStegoException(Throwable cause, String namespace, int errorCode, String param) {
        this(cause, namespace, errorCode, new Object[] { param });
    }

    /**
     * Constructor which takes object array for parameters for the message
     *
     * @param cause Original exception which caused this exception to be raised
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     * @param params Parameters for exception message
     */
    public OpenStegoException(Throwable cause, String namespace, int errorCode, Object... params) {
        super((namespace == OpenStego.NAMESPACE && errorCode == UNHANDLED_EXCEPTION) ? cause.toString()
                : LabelUtil.getInstance(namespace).getString(errMsgKeyMap.get(namespace + errorCode), params), cause);

        this.namespace = namespace;
        this.errorCode = errorCode;
    }

    /**
     * Get method for errorCode
     *
     * @return errorCode
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * Get method for namespace
     *
     * @return namespace
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Method to add new error codes to the namespace
     *
     * @param namespace Namespace for the error
     * @param errorCode Error code of the error
     * @param labelKey Key of the label for the error
     */
    public static void addErrorCode(String namespace, int errorCode, String labelKey) {
        errMsgKeyMap.put(namespace + errorCode, labelKey);
    }
}
