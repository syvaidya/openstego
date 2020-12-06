/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.randlsb;

import java.io.IOException;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.plugin.lsb.LSBDataHeader;
import com.openstego.desktop.plugin.lsb.LSBErrors;
import com.openstego.desktop.plugin.lsb.LSBPlugin;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.LabelUtil;

/**
 * Plugin for OpenStego which implements the Random LSB algorithm of steganography
 */
public class RandomLSBPlugin extends LSBPlugin {
    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "RandomLSB";

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(NAMESPACE);

    /**
     * Default constructor
     */
    public RandomLSBPlugin() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.RandomLSBPluginLabels");
    }

    /**
     * Gives the name of the plugin
     *
     * @return Name of the plugin
     */
    @Override
    public String getName() {
        return "RandomLSB";
    }

    /**
     * Gives a short description of the plugin
     *
     * @return Short description of the plugin
     */
    @Override
    public String getDescription() {
        return labelUtil.getString("plugin.description");
    }

    /**
     * Method to embed the message into the cover data
     *
     * @param msg Message to be embedded
     * @param msgFileName Name of the message file. If this value is provided, then the filename should be
     *        embedded in the cover data
     * @param cover Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException
     */
    @Override
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) throws OpenStegoException {
        int numOfPixels = 0;
        ImageHolder image = null;
        RandomLSBOutputStream lsbOS = null;

        try {
            // Generate random image, if input image is not provided
            if (cover == null) {
                numOfPixels = (int) (LSBDataHeader.getMaxHeaderSize() * 8 / 3.0);
                numOfPixels += (int) (msg.length * 8 / (3.0 * ((LSBConfig) this.config).getMaxBitsUsedPerChannel()));
                image = ImageUtil.generateRandomImage(numOfPixels);
            } else {
                image = ImageUtil.byteArrayToImage(cover, coverFileName);
            }
            lsbOS = new RandomLSBOutputStream(image, msg.length, msgFileName, this.config);
            lsbOS.write(msg);
            lsbOS.close();

            return ImageUtil.imageToByteArray(lsbOS.getImage(), stegoFileName, this);
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to extract the message file name from the stego data
     *
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Message file name
     * @throws OpenStegoException
     */
    @Override
    public String extractMsgFileName(byte[] stegoData, String stegoFileName) throws OpenStegoException {
        RandomLSBInputStream lsbIS = null;

        try {
            lsbIS = new RandomLSBInputStream(ImageUtil.byteArrayToImage(stegoData, stegoFileName), this.config);
            return lsbIS.getDataHeader().getFileName();
        } finally {
            if (lsbIS != null) {
                try {
                    lsbIS.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Method to extract the message from the stego data
     *
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @param origSigData Optional signature data file for watermark
     * @return Extracted message
     * @throws OpenStegoException
     */
    @Override
    public byte[] extractData(byte[] stegoData, String stegoFileName, byte[] origSigData) throws OpenStegoException {
        int bytesRead = 0;
        byte[] data = null;
        LSBDataHeader header = null;
        RandomLSBInputStream lsbIS = null;

        try {
            lsbIS = new RandomLSBInputStream(ImageUtil.byteArrayToImage(stegoData, stegoFileName), this.config);
            header = lsbIS.getDataHeader();
            data = new byte[header.getDataLength()];

            bytesRead = lsbIS.read(data, 0, data.length);
            if (bytesRead != data.length) {
                throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.ERR_IMAGE_DATA_READ);
            }

            return data;
        } catch (OpenStegoException osEx) {
            throw osEx;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        } finally {
            if (lsbIS != null) {
                try {
                    lsbIS.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Method to get the usage details of the plugin
     *
     * @return Usage details of the plugin
     * @throws OpenStegoException
     */
    @Override
    public String getUsage() throws OpenStegoException {
        LSBConfig defaultConfig = new LSBConfig();
        return labelUtil.getString("plugin.usage", Integer.valueOf(defaultConfig.getMaxBitsUsedPerChannel()));
    }
}
