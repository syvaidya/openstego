/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.imagebit;

import net.sourceforge.openstego.OpenStegoException;

/**
 * Class to store error codes for Image Bit plugin template
 */
public class ImageBitErrors
{
    /**
     * Error Code - maxBitsUsedPerChannel is not a number
     */
    public static final int MAX_BITS_NOT_NUMBER = 1;

    /**
     * Error Code - maxBitsUsedPerChannel is not in valid range
     */
    public static final int MAX_BITS_NOT_IN_RANGE = 2;

    /**
     * Error Code - Invalid stego header data
     */
    public static final int INVALID_STEGO_HEADER = 3;

    /**
     * Error Code - Invalid image header version
     */
    public static final int INVALID_HEADER_VERSION = 4;

    /*
     * Initialize the error code - message key map
     */
    static
    {
        OpenStegoException.addErrorCode(ImageBitPluginTemplate.NAMESPACE, MAX_BITS_NOT_NUMBER,
                "err.config.maxBitsUsedPerChannel.notNumber");
        OpenStegoException.addErrorCode(ImageBitPluginTemplate.NAMESPACE, MAX_BITS_NOT_IN_RANGE,
                "err.config.maxBitsUsedPerChannel.notInRange");
        OpenStegoException.addErrorCode(ImageBitPluginTemplate.NAMESPACE, INVALID_STEGO_HEADER,
                "err.invalidHeaderStamp");
        OpenStegoException.addErrorCode(ImageBitPluginTemplate.NAMESPACE, INVALID_HEADER_VERSION,
                "err.invalidHeaderVersion");
    }
}
