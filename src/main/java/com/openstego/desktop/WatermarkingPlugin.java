/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for stego plugins for OpenStego purpose of which is watermarking. It implements few methods which are
 * specific for watermarking, and provides dummy implementation for the methods which are specific to data hiding
 * purposes so that sub-class does not need to implement them
 *
 * @see DataHidingPlugin
 */
public abstract class WatermarkingPlugin extends OpenStegoPlugin {
    // ------------- Metadata Methods -------------

    /**
     * Gives the purpose(s) of the plugin. This implementation returns only one value - Watermarking
     *
     * @return Purpose(s) of the plugin
     */
    @Override
    public final List<Purpose> getPurposes() {
        List<Purpose> purposes = new ArrayList<Purpose>();
        purposes.add(Purpose.WATERMARKING);
        return purposes;
    }

    // ------------- Core Stego Methods -------------

    /**
     * Method to extract the message file name from the stego data. This implementation returns <code>null</code> as
     * this class is for watermarking plugins only
     *
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Message file name
     * @throws OpenStegoException
     */
    @Override
    public final String extractMsgFileName(byte[] stegoData, String stegoFileName) throws OpenStegoException {
        return null;
    }

    /**
     * Method to get correlation value which above which it can be considered that watermark strength is high (default
     * to 0.5 which is safe for general watermarking)
     *
     * @return High watermark
     * @throws OpenStegoException
     */
    @Override
    public double getHighWatermarkLevel() throws OpenStegoException {
        return 0.5;
    }

    /**
     * Method to get correlation value which below which it can be considered that watermark strength is low (default to
     * 0.2 which is safe for general watermarking)
     *
     * @return Low watermark
     * @throws OpenStegoException
     */
    @Override
    public double getLowWatermarkLevel() throws OpenStegoException {
        return 0.2;
    }

    /**
     * Method to find out whether given stego data can be handled by this plugin or not. This implementation returns
     * <code>false</code> as this class is for watermarking plugins only
     *
     * @param stegoData Stego data containing the message
     * @return Boolean indicating whether the stego data can be handled by this plugin or not
     */
    @Override
    public final boolean canHandle(byte[] stegoData) {
        return false;
    }
}
