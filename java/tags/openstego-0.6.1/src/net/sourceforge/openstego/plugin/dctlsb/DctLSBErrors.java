/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2014 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.dctlsb;

import net.sourceforge.openstego.OpenStegoException;

/**
 * Class to store error codes for DCT LSB plugin
 */
public class DctLSBErrors
{
    /**
     * Error Code - Error while reading image data
     */
    public static final int ERR_IMAGE_DATA_READ = 1;

    /**
     * Error Code - Image size insufficient for data
     */
    public static final int IMAGE_SIZE_INSUFFICIENT = 2;

    /*
     * Initialize the error code - message key map
     */
    static
    {
        OpenStegoException.addErrorCode(DctLSBPlugin.NAMESPACE, ERR_IMAGE_DATA_READ, "err.image.read");
        OpenStegoException.addErrorCode(DctLSBPlugin.NAMESPACE, IMAGE_SIZE_INSUFFICIENT, "err.image.insufficientSize");
    }
}
