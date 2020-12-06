/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.template.dct;

import com.openstego.desktop.OpenStegoException;

/**
 * Class to store error codes for DCT plugin template
 */
public class DCTErrors {
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
    static {
        OpenStegoException.addErrorCode(DCTPluginTemplate.NAMESPACE, INVALID_STEGO_HEADER, "err.invalidHeaderStamp");
        OpenStegoException.addErrorCode(DCTPluginTemplate.NAMESPACE, INVALID_HEADER_VERSION, "err.invalidHeaderVersion");
    }
}
