/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dctlsb;

import java.io.IOException;
import java.util.List;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.dct.DCTConfig;
import com.openstego.desktop.plugin.template.dct.DCTDataHeader;
import com.openstego.desktop.plugin.template.image.WMImagePluginTemplate;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.dct.DCT;

/**
 * Plugin for OpenStego which implements the DCT based Least-significant bit algorithm
 */
public class DctLSBPlugin extends WMImagePluginTemplate {
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(DctLSBPlugin.NAMESPACE);

    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "DCTLSB";

    /**
     * Default constructor
     */
    public DctLSBPlugin() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.DctLSBPluginLabels");
        new DctLSBErrors(); // Initialize error codes
    }

    /**
     * Gives the name of the plugin
     *
     * @return Name of the plugin
     */
    @Override
    public String getName() {
        return "DctLSB";
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
     * @param msgFileName Name of the message file. If this value is provided, then the filename should be embedded in
     *        the cover data
     * @param cover Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException
     */
    @Override
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) throws OpenStegoException {
        ImageHolder image = null;
        DctLSBOutputStream os = null;
        int imgType = 0;

        try {
            // Generate random image, if input image is not provided
            if (cover == null) {
                image = ImageUtil.generateRandomImage((DCTDataHeader.getMaxHeaderSize() + msg.length) * 8 * DCT.NJPEG * DCT.NJPEG);
            } else {
                image = ImageUtil.byteArrayToImage(cover, coverFileName);
            }
            imgType = image.getImage().getType();
            os = new DctLSBOutputStream(image, msg.length, msgFileName, this.config);
            os.write(msg);
            os.close();

            return ImageUtil.imageToByteArray(os.getImage(imgType), stegoFileName, this);
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
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
        byte[] msg = null;
        DCTDataHeader header = null;
        DctLSBInputStream is = null;
        int bytesRead = 0;

        try {
            is = new DctLSBInputStream(ImageUtil.byteArrayToImage(stegoData, stegoFileName), this.config);
            header = is.getDataHeader();
            msg = new byte[header.getDataLength()];

            bytesRead = is.read(msg, 0, msg.length);
            if (bytesRead != msg.length) {
                throw new OpenStegoException(null, NAMESPACE, DctLSBErrors.ERR_IMAGE_DATA_READ);
            }
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return msg;
    }

    /**
     * Method to generate the signature data
     *
     * @return Signature data
     * @throws OpenStegoException
     */
    @Override
    public byte[] generateSignature() throws OpenStegoException {
        return null; // TODO
    }

    /**
     * Method to get the list of supported file extensions for writing
     *
     * @return List of supported file extensions for writing
     * @throws OpenStegoException
     */
    @Override
    public List<String> getWritableFileExtensions() throws OpenStegoException {
        List<String> formatList = super.getWritableFileExtensions();

        // Expicilty removing unsupported formats
        formatList.remove("jpeg");
        formatList.remove("jpg");

        return formatList;
    }

    /**
     * Method to get the usage details of the plugin
     *
     * @return Usage details of the plugin
     * @throws OpenStegoException
     */
    @Override
    public String getUsage() throws OpenStegoException {
        return labelUtil.getString("plugin.usage");
    }

    /**
     * Method to check the correlation between original signature and the extracted watermark
     *
     * @param origSigData Original signature data
     * @param watermarkData Extracted watermark data
     * @return Correlation
     * @throws OpenStegoException
     */
    @Override
    public double getWatermarkCorrelation(byte[] origSigData, byte[] watermarkData) throws OpenStegoException {
        // TODO
        return 0.0;
    }

    /**
     * Method to get the configuration class specific to this plugin
     *
     * @return Configuration class specific to this plugin
     */
    @Override
    public Class<? extends OpenStegoConfig> getConfigClass() {
        return DCTConfig.class;
    }
}
