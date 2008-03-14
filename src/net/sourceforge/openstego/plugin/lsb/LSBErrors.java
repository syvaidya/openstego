/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.lsb;

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
     * Error Code - Null value provided for image
     */
    public static final int NULL_IMAGE_ARGUMENT = 2;

    /**
     * Error Code - Image size insufficient for data
     */
    public static final int IMAGE_SIZE_INSUFFICIENT = 3;

    /*
     * Initialize the error code - message key map
     */
    static
    {
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, ERR_IMAGE_DATA_READ, "err.image.read");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, NULL_IMAGE_ARGUMENT, "err.image.arg.nullValue");
        OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, IMAGE_SIZE_INSUFFICIENT, "err.image.insufficientSize");
    }
}
