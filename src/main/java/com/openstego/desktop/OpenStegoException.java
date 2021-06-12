/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import com.openstego.desktop.util.LabelUtil;

import java.util.HashMap;
import java.util.Map;

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
     * Map to store error code to message key mapping
     */
    private static final Map<String, String> errMsgKeyMap = new HashMap<>();

    /**
     * Error code for the exception
     */
    private final int errorCode;

    /**
     * Namespace for the exception
     */
    private final String namespace;

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
     * @param cause     Original exception which caused this exception to be raised
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     */
    public OpenStegoException(Throwable cause, String namespace, int errorCode) {
        this(cause, namespace, errorCode, (Object[]) null);
    }

    /**
     * Constructor with a single parameter for the message
     *
     * @param cause     Original exception which caused this exception to be raised
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     * @param param     Parameter for exception message
     */
    public OpenStegoException(Throwable cause, String namespace, int errorCode, String param) {
        this(cause, namespace, errorCode, new Object[]{param});
    }

    /**
     * Constructor which takes object array for parameters for the message
     *
     * @param cause     Original exception which caused this exception to be raised
     * @param namespace Namespace of the error
     * @param errorCode Error code for the exception
     * @param params    Parameters for exception message
     */
    public OpenStegoException(Throwable cause, String namespace, int errorCode, Object... params) {
        super((OpenStego.NAMESPACE.equals(namespace) && errorCode == UNHANDLED_EXCEPTION) ? cause.toString()
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
     * @param labelKey  Key of the label for the error
     */
    public static void addErrorCode(String namespace, int errorCode, String labelKey) {
        errMsgKeyMap.put(namespace + errorCode, labelKey);
    }
}
