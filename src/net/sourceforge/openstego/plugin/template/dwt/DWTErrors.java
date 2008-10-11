/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.dwt;

import net.sourceforge.openstego.OpenStegoException;

/**
 * Class to store error codes for DWT plugin template
 */
public class DWTErrors
{
    /**
     * Error Code - Invalid stego header data
     */
    public static final int INVALID_STEGO_HEADER = 1;

    /**
     * Error Code - Invalid image header version
     */
    public static final int INVALID_HEADER_VERSION = 2;

    /*
     * Initialize the error code - message key map
     */
    static
    {
        OpenStegoException.addErrorCode(DWTPluginTemplate.NAMESPACE, INVALID_STEGO_HEADER, "err.invalidHeaderStamp");
        OpenStegoException
                .addErrorCode(DWTPluginTemplate.NAMESPACE, INVALID_HEADER_VERSION, "err.invalidHeaderVersion");
    }
}
