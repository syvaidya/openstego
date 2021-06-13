/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dctlsb;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.dct.DCTConfig;
import com.openstego.desktop.plugin.template.dct.DCTDataHeader;
import com.openstego.desktop.plugin.template.image.WMImagePluginTemplate;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.cmd.CmdLineOptions;
import com.openstego.desktop.util.dct.DCT;

import java.io.IOException;
import java.util.List;

/**
 * Plugin for OpenStego which implements the DCT based Least-significant bit algorithm
 */
public class DctLSBPlugin extends WMImagePluginTemplate {
    /**
     * LabelUtil instance to retrieve labels
     */
    private static final LabelUtil labelUtil = LabelUtil.getInstance(DctLSBPlugin.NAMESPACE);

    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "DCTLSB";

    /**
     * Default constructor
     */
    public DctLSBPlugin() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.DctLSBPluginLabels");
        DctLSBErrors.init(); // Initialize error codes
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
     * @param msg           Message to be embedded
     * @param msgFileName   Name of the message file. If this value is provided, then the filename should be embedded in
     *                      the cover data
     * @param cover         Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException Processing issues
     */
    @Override
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) throws OpenStegoException {
        ImageHolder image;
        int imgType;

        try {
            // Generate random image, if input image is not provided
            if (cover == null) {
                image = ImageUtil.generateRandomImage((DCTDataHeader.getMaxHeaderSize() + msg.length) * 8 * DCT.NJPEG * DCT.NJPEG);
            } else {
                image = ImageUtil.byteArrayToImage(cover, coverFileName);
            }
            imgType = image.getImage().getType();
            try (DctLSBOutputStream os = new DctLSBOutputStream(image, msg.length, msgFileName, this.config)) {
                os.write(msg);
                image = os.getImage(imgType);
            }

            return ImageUtil.imageToByteArray(image, stegoFileName, this);
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to extract the message from the stego data
     *
     * @param stegoData     Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @param origSigData   Optional signature data file for watermark
     * @return Extracted message
     * @throws OpenStegoException Processing issues
     */
    @Override
    public byte[] extractData(byte[] stegoData, String stegoFileName, byte[] origSigData) throws OpenStegoException {
        byte[] msg;
        DCTDataHeader header;
        int bytesRead;
        ImageHolder imgHolder = ImageUtil.byteArrayToImage(stegoData, stegoFileName);

        try (DctLSBInputStream is = new DctLSBInputStream(imgHolder, this.config)) {
            header = is.getDataHeader();
            msg = new byte[header.getDataLength()];

            bytesRead = is.read(msg, 0, msg.length);
            if (bytesRead != msg.length) {
                throw new OpenStegoException(null, NAMESPACE, DctLSBErrors.ERR_IMAGE_DATA_READ);
            }
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }

        return msg;
    }

    /**
     * Method to generate the signature data
     *
     * @return Signature data
     */
    @Override
    public byte[] generateSignature() {
        return null;
    }

    /**
     * Method to get the list of supported file extensions for writing
     *
     * @return List of supported file extensions for writing
     * @throws OpenStegoException Processing issues
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
     */
    @Override
    public String getUsage() {
        return labelUtil.getString("plugin.usage");
    }

    /**
     * Method to check the correlation between original signature and the extracted watermark
     *
     * @param origSigData   Original signature data
     * @param watermarkData Extracted watermark data
     * @return Correlation
     */
    @Override
    public double getWatermarkCorrelation(byte[] origSigData, byte[] watermarkData) {
        return 0.0;
    }

    /**
     * Method to create default configuration data (specific to this plugin)
     *
     * @return Configuration data
     */
    @Override
    protected DCTConfig createConfig() {
        return new DCTConfig();
    }

    /**
     * Method to create configuration data (specific to this plugin) based on the command-line options
     *
     * @param options Command-line options
     * @return Configuration data
     * @throws OpenStegoException Processing issues
     */
    @Override
    protected DCTConfig createConfig(CmdLineOptions options) throws OpenStegoException {
        DCTConfig config = new DCTConfig();
        config.initialize(options);
        return config;
    }
}
