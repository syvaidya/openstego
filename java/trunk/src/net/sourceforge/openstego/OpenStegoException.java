/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
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
    public static final int UNHANDLED_EXCEPTION = 0;

    /**
     * Error Code - Invalid password
     */
    public static final int INVALID_PASSWORD = 1;

    /**
     * Error Code - Error while reading image data
     */
    public static final int ERR_IMAGE_DATA_READ = 2;

    /**
     * Error Code - maxBitsUsedPerChannel is not a number
     */
    public static final int MAX_BITS_NOT_NUMBER = 3;

    /**
     * Error Code - maxBitsUsedPerChannel is not in valid range
     */
    public static final int MAX_BITS_NOT_IN_RANGE = 4;

    /**
     * Error Code - Invalid value for useCompression
     */
    public static final int INVALID_USE_COMPR_VALUE = 5;

    /**
     * Error Code - Invalid value for useEncryption
     */
    public static final int INVALID_USE_ENCRYPT_VALUE = 6;

    /**
     * Error Code - Invalid key name
     */
    public static final int INVALID_KEY_NAME = 7;

    /**
     * Error Code - Invalid stego header data
     */
    public static final int INVALID_STEGO_HEADER = 8;

    /**
     * Error Code - Null value provided for image
     */
    public static final int NULL_IMAGE_ARGUMENT = 10;

    /**
     * Error Code - Image size insufficient for data
     */
    public static final int IMAGE_SIZE_INSUFFICIENT = 11;

    /**
     * Error Code - Image file invalid
     */
    public static final int IMAGE_FILE_INVALID = 12;

    /**
     * Error Code - Image type invalid
     */
    public static final int IMAGE_TYPE_INVALID = 13;

    /**
     * Error Code - Invalid image header version
     */
    public static final int INVALID_HEADER_VERSION = 14;

    /**
     * Error Code - Corrupt Data
     */
    public static final int CORRUPT_DATA = 15;


    /**
     * Map to store error code to message key mapping
     */
    private static HashMap errMsgKeyMap = new HashMap();

    /**
     * Error code for the exception
     */
    private int errorCode = 0;

    /*
     * Initialize the error code - message key map
     */
    static
    {
        errMsgKeyMap.put(new Integer(INVALID_PASSWORD), "err.config.password.invalid");
        errMsgKeyMap.put(new Integer(MAX_BITS_NOT_NUMBER), "err.config.maxBitsUsedPerChannel.notNumber");
        errMsgKeyMap.put(new Integer(MAX_BITS_NOT_IN_RANGE), "err.config.maxBitsUsedPerChannel.notInRange");
        errMsgKeyMap.put(new Integer(INVALID_USE_COMPR_VALUE), "err.config.useCompression.invalid");
        errMsgKeyMap.put(new Integer(INVALID_USE_ENCRYPT_VALUE), "err.config.useEncryption.invalid");
        errMsgKeyMap.put(new Integer(INVALID_KEY_NAME), "err.config.invalidKey");
        errMsgKeyMap.put(new Integer(INVALID_STEGO_HEADER), "err.invalidHeaderStamp");
        errMsgKeyMap.put(new Integer(ERR_IMAGE_DATA_READ), "err.image.read");
        errMsgKeyMap.put(new Integer(NULL_IMAGE_ARGUMENT), "err.image.arg.nullValue");
        errMsgKeyMap.put(new Integer(IMAGE_SIZE_INSUFFICIENT), "err.image.insufficientSize");
        errMsgKeyMap.put(new Integer(IMAGE_FILE_INVALID), "err.image.file.invalid");
        errMsgKeyMap.put(new Integer(IMAGE_TYPE_INVALID), "err.image.type.invalid");
        errMsgKeyMap.put(new Integer(INVALID_HEADER_VERSION), "err.invalidHeaderVersion");
        errMsgKeyMap.put(new Integer(CORRUPT_DATA), "err.corruptData");
    }

    /**
     * Default constructor
     * @param errorCode Error code for the exception
     * @param cause Original exception which caused this exception to be raised
     */
    public OpenStegoException(int errorCode, Throwable cause)
    {
        super((errorCode == UNHANDLED_EXCEPTION) ? cause.toString() :
            LabelUtil.getString((String) errMsgKeyMap.get(new Integer(errorCode))), cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with a single parameter for the message
     * @param errorCode Error code for the exception
     * @param param Parameter for exception message
     * @param cause Original exception which caused this exception to be raised
     */
    public OpenStegoException(int errorCode, String param, Throwable cause)
    {
        super((errorCode == UNHANDLED_EXCEPTION) ? cause.toString() :
            LabelUtil.getString((String) errMsgKeyMap.get(new Integer(errorCode)), new Object[] { param }), cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructor which takes object array for parameters for the message
     * @param errorCode Error code for the exception
     * @param params Parameters for exception message
     * @param cause Original exception which caused this exception to be raised
     */
    public OpenStegoException(int errorCode, Object[] params, Throwable cause)
    {
        super((errorCode == UNHANDLED_EXCEPTION) ? cause.toString() :
            LabelUtil.getString((String) errMsgKeyMap.get(new Integer(errorCode)), params), cause);
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
}