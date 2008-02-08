/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.util.HashMap;

import net.sourceforge.openstego.util.LabelUtil;

/**
 * Custom exception class for OpenStego
 */
public class OpenStegoException extends Exception
{
    /**
     * Error Code - Unhandled exception
     */
    private static final int UNHANDLED_EXCEPTION = 0;

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
     * Map to store error code to message key mapping
     */
    private static HashMap errMsgKeyMap = new HashMap();

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
    static
    {
        addErrorCode(OpenStego.NAMESPACE, INVALID_PASSWORD, "err.config.password.invalid");
        addErrorCode(OpenStego.NAMESPACE, INVALID_USE_COMPR_VALUE, "err.config.useCompression.invalid");
        addErrorCode(OpenStego.NAMESPACE, INVALID_USE_ENCRYPT_VALUE, "err.config.useEncryption.invalid");
        addErrorCode(OpenStego.NAMESPACE, INVALID_KEY_NAME, "err.config.invalidKey");
        addErrorCode(OpenStego.NAMESPACE, CORRUPT_DATA, "err.corruptData");
        addErrorCode(OpenStego.NAMESPACE, NO_VALID_PLUGIN, "err.noValidPlugin");
    }

    /**
     * Constructor using default namespace for unhandled exceptions
     * @param cause Original exception which caused this exception to be raised
     */
    public OpenStegoException(Throwable cause)
    {
        this(OpenStego.NAMESPACE, UNHANDLED_EXCEPTION, (Object[]) null, cause);
    }

    /**
     * Default constructor
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     * @param cause Original exception which caused this exception to be raised
     */
    public OpenStegoException(String namespace, int errorCode, Throwable cause)
    {
        this(namespace, errorCode, (Object[]) null, cause);
    }

    /**
     * Constructor with a single parameter for the message
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     * @param param Parameter for exception message
     * @param cause Original exception which caused this exception to be raised
     */
    public OpenStegoException(String namespace, int errorCode, String param, Throwable cause)
    {
        this(namespace, errorCode, new Object[] { param }, cause);
    }

    /**
     * Constructor which takes object array for parameters for the message
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     * @param params Parameters for exception message
     * @param cause Original exception which caused this exception to be raised
     */
    public OpenStegoException(String namespace, int errorCode, Object[] params, Throwable cause)
    {
        super((namespace == OpenStego.NAMESPACE && errorCode == UNHANDLED_EXCEPTION)
                  ? cause.toString()
                  : LabelUtil.getInstance(namespace).getString((String) errMsgKeyMap.get(namespace + errorCode),
                        params),
                cause);

        this.namespace = namespace;
        this.errorCode = errorCode;
    }

    /**
     * Get method for errorCode
     * @return errorCode
     */
    public int getErrorCode()
    {
        return errorCode;
    }

    /**
     * Get method for namespace
     * @return namespace
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * Method to add new error codes to the namespace
     * @param namespace Namespace for the error
     * @param errorCode Error code of the error
     * @param labelKey Key of the label for the error
     */
    public static void addErrorCode(String namespace, int errorCode, String labelKey)
    {
        errMsgKeyMap.put(namespace + errorCode, labelKey);
    }
}