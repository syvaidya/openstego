/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for stego plugins for OpenStego purpose of which is data hiding. It implements few methods which are
 * specific for data hiding, and provides dummy implementation for the methods which are specific to watermarking
 * purposes so that sub-class does not need to implement them
 *
 * @see WatermarkingPlugin
 */
public abstract class DataHidingPlugin extends OpenStegoPlugin {
    // ------------- Metadata Methods -------------

    /**
     * Gives the purpose(s) of the plugin. This implementation returns only one value - Data Hiding
     *
     * @return Purpose(s) of the plugin
     */
    @Override
    public final List<Purpose> getPurposes() {
        List<Purpose> purposes = new ArrayList<Purpose>();
        purposes.add(Purpose.DATA_HIDING);
        return purposes;
    }

    // ------------- Core Stego Methods -------------

    /**
     * Method to generate the signature data. This implementation returns <code>null</code> as this class is for data
     * hiding plugins only
     *
     * @return Signature data
     * @throws OpenStegoException
     */
    @Override
    public final byte[] generateSignature() throws OpenStegoException {
        return null;
    }

    /**
     * Method to check the correlation between original signature and the extracted watermark. This implementation
     * returns <code>0.0</code> as this class is for data hiding plugins only
     *
     * @param origSigData Original signature data
     * @param watermarkData Extracted watermark data
     * @return Correlation
     * @throws OpenStegoException
     */
    @Override
    public final double getWatermarkCorrelation(byte[] origSigData, byte[] watermarkData) throws OpenStegoException {
        return 0.0;
    }

    /**
     * Method to get correlation value which above which it can be considered that watermark strength is high
     *
     * @return High watermark
     * @throws OpenStegoException
     */
    @Override
    public double getHighWatermarkLevel() throws OpenStegoException {
        return 0;
    }

    /**
     * Method to get correlation value which below which it can be considered that watermark strength is low
     *
     * @return Low watermark
     * @throws OpenStegoException
     */
    @Override
    public double getLowWatermarkLevel() throws OpenStegoException {
        return 0;
    }

    /**
     * Method to find out whether given stego data can be handled by this plugin or not
     *
     * @param stegoData Stego data containing the message
     * @return Boolean indicating whether the stego data can be handled by this plugin or not
     */
    @Override
    public final boolean canHandle(byte[] stegoData) {
        try {
            extractMsgFileName(stegoData, "DUMMY");
        } catch (OpenStegoException osEx) {
            if (osEx.getErrorCode() != OpenStegoException.INVALID_PASSWORD) {
                return false;
            }
        }

        return true;
    }
}
