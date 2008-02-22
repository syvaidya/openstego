/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.lsb;

import java.util.HashMap;

import net.sourceforge.openstego.OpenStegoException;

/**
 * Class to store error codes for LSB plugin
 */
public class LSBErrors
{
    /**
     * Error Code - Error while reading image data
     */
    public static final int ERR_IMAGE_DATA_READ = 1;

    /**
     * Error Code - maxBitsUsedPerChannel is not a number
     */
    public static final int MAX_BITS_NOT_NUMBER = 2;

    /**
     * Error Code - maxBitsUsedPerChannel is not in valid range
     */
    public static final int MAX_BITS_NOT_IN_RANGE = 3;

    /**
     * Error Code - Invalid stego header data
     */
    public static final int INVALID_STEGO_HEADER = 4;

    /**
     * Error Code - Null value provided for image
     */
    public static final int NULL_IMAGE_ARGUMENT = 5;

    /**
     * Error Code - Image size insufficient for data
     */
    public static final int IMAGE_SIZE_INSUFFICIENT = 6;

    /**
     * Error Code - Image file invalid
     */
    public static final int IMAGE_FILE_INVALID = 7;

    /**
     * Error Code - Image type invalid
     */
    public static final int IMAGE_TYPE_INVALID = 8;

    /**
     * Error Code - Invalid image header version
     */
    public static final int INVALID_HEADER_VERSION = 9;


    /**
     * Map to store error code to message key mapping
     */
    private static HashMap errMsgKeyMap = new HashMap();

    /*
     * Initialize the error code - message key map
     */
    static
    {
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, MAX_BITS_NOT_NUMBER, "err.config.maxBitsUsedPerChannel.notNumber");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, MAX_BITS_NOT_IN_RANGE, "err.config.maxBitsUsedPerChannel.notInRange");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, INVALID_STEGO_HEADER, "err.invalidHeaderStamp");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, ERR_IMAGE_DATA_READ, "err.image.read");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, NULL_IMAGE_ARGUMENT, "err.image.arg.nullValue");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, IMAGE_SIZE_INSUFFICIENT, "err.image.insufficientSize");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, IMAGE_FILE_INVALID, "err.image.file.invalid");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, IMAGE_TYPE_INVALID, "err.image.type.invalid");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, INVALID_HEADER_VERSION, "err.invalidHeaderVersion");
    }
}