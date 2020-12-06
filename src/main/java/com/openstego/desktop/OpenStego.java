/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.UIManager;

import com.openstego.desktop.ui.OpenStegoUI;
import com.openstego.desktop.util.CommonUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.PluginManager;
import com.openstego.desktop.util.UserPreferences;

/**
 * This is the main class for OpenStego. It includes the {@link #main(String[])} method which provides the
 * command line interface for the tool. It also has API methods which can be used by external programs
 * when using OpenStego as a library.
 */
public class OpenStego {
    /**
     * Constant for the namespace for labels
     */
    public static final String NAMESPACE = "OpenStego";

    /**
     * Configuration data
     */
    private OpenStegoConfig config = null;

    /**
     * Stego plugin to use for embedding / extracting data
     */
    private OpenStegoPlugin plugin = null;

    static {
        LabelUtil.addNamespace(NAMESPACE, "i18n.OpenStegoLabels");
    }

    /**
     * Constructor using the default configuration
     *
     * @param plugin Stego plugin to use
     * @throws OpenStegoException
     */
    public OpenStego(OpenStegoPlugin plugin) throws OpenStegoException {
        this(plugin, (OpenStegoConfig) null);
    }

    /**
     * Constructor using {@link OpenStegoConfig} object
     *
     * @param plugin Stego plugin to use
     * @param config OpenStegoConfig object with configuration data
     * @throws OpenStegoException
     */
    public OpenStego(OpenStegoPlugin plugin, OpenStegoConfig config) throws OpenStegoException {
        // Plugin is mandatory
        if (plugin == null) {
            throw new OpenStegoException(null, NAMESPACE, OpenStegoException.NO_PLUGIN_SPECIFIED);
        }
        this.plugin = plugin;
        this.config = (config == null) ? new OpenStegoConfig() : config;
    }

    /**
     * Constructor with configuration data in the form of {@link Map}
     *
     * @param plugin Plugin object
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    public OpenStego(OpenStegoPlugin plugin, Map<String, String> propMap) throws OpenStegoException {
        this(plugin, new OpenStegoConfig(propMap));
    }

    /**
     * Method to embed the message data into the cover data
     *
     * @param msg Message data to be embedded
     * @param msgFileName Name of the message file
     * @param cover Cover data into which message data needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the embedded message
     * @throws OpenStegoException
     */
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.DATA_HIDING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_DH);
        }

        try {
            // Compress data, if requested
            if (this.config.isUseCompression()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream zos = new GZIPOutputStream(bos);
                zos.write(msg);
                zos.finish();
                zos.close();
                bos.close();

                msg = bos.toByteArray();
            }

            // Encrypt data, if requested
            if (this.config.isUseEncryption()) {
                OpenStegoCrypto crypto = new OpenStegoCrypto(this.config.getPassword(), this.config.getEncryptionAlgorithm());
                msg = crypto.encrypt(msg);
            }

            return this.plugin.embedData(msg, msgFileName, cover, coverFileName, stegoFileName);
        } catch (OpenStegoException osEx) {
            throw osEx;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to embed the message data into the cover data (alternate API)
     *
     * @param msgFile File containing the message data to be embedded
     * @param coverFile Cover file into which data needs to be embedded
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the embedded message
     * @throws OpenStegoException
     */
    public byte[] embedData(File msgFile, File coverFile, String stegoFileName) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.DATA_HIDING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_DH);
        }

        InputStream is = null;
        String filename = null;

        try {
            // If no message file is provided, then read the data from stdin
            if (msgFile == null) {
                is = System.in;
            } else {
                is = new FileInputStream(msgFile);
                filename = msgFile.getName();
            }

            return embedData(CommonUtil.getStreamBytes(is), filename, coverFile == null ? null : CommonUtil.getFileBytes(coverFile),
                coverFile == null ? null : coverFile.getName(), stegoFileName);
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to embed the watermark signature data into the cover data
     *
     * @param sig Signature data to be embedded
     * @param sigFileName Name of the signature file
     * @param cover Cover data into which signature data needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the embedded signature
     * @throws OpenStegoException
     */
    public byte[] embedMark(byte[] sig, String sigFileName, byte[] cover, String coverFileName, String stegoFileName) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.WATERMARKING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_WM);
        }

        try {
            // No compression and encryption should be done as this is signature data
            return this.plugin.embedData(sig, sigFileName, cover, coverFileName, stegoFileName);
        } catch (OpenStegoException osEx) {
            throw osEx;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to embed the watermark signature data into the cover data (alternate API)
     *
     * @param sigFile File containing the signature data to be embedded
     * @param coverFile Cover file into which data needs to be embedded
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the embedded signature
     * @throws OpenStegoException
     */
    public byte[] embedMark(File sigFile, File coverFile, String stegoFileName) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.WATERMARKING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_WM);
        }

        InputStream is = null;
        String filename = null;

        try {
            // If no signature file is provided, then read the data from stdin
            if (sigFile == null) {
                is = System.in;
            } else {
                is = new FileInputStream(sigFile);
                filename = sigFile.getName();
            }

            return embedMark(CommonUtil.getStreamBytes(is), filename, coverFile == null ? null : CommonUtil.getFileBytes(coverFile),
                coverFile == null ? null : coverFile.getName(), stegoFileName);
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to extract the message data from stego data
     *
     * @param stegoData Stego data from which the message needs to be extracted
     * @param stegoFileName Name of the stego file
     * @return Extracted message (List's first element is filename and second element is the message as byte array)
     * @throws OpenStegoException
     */
    public List<?> extractData(byte[] stegoData, String stegoFileName) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.DATA_HIDING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_DH);
        }

        byte[] msg = null;
        List<Object> output = new ArrayList<Object>();

        try {
            // Add file name as first element of output list
            output.add(this.plugin.extractMsgFileName(stegoData, stegoFileName));
            msg = this.plugin.extractData(stegoData, stegoFileName, null);

            // Decrypt data, if required
            if (this.config.isUseEncryption()) {
                OpenStegoCrypto crypto = new OpenStegoCrypto(this.config.getPassword(), this.config.getEncryptionAlgorithm());
                msg = crypto.decrypt(msg);
            }

            // Decompress data, if required
            if (this.config.isUseCompression()) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(msg);
                    GZIPInputStream zis = new GZIPInputStream(bis);
                    msg = CommonUtil.getStreamBytes(zis);
                    zis.close();
                    bis.close();
                } catch (IOException ioEx) {
                    throw new OpenStegoException(ioEx, OpenStego.NAMESPACE, OpenStegoException.CORRUPT_DATA);
                }
            }

            // Add message as second element of output list
            output.add(msg);
        } catch (OpenStegoException osEx) {
            throw osEx;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }

        return output;
    }

    /**
     * Method to extract the message data from stego data (alternate API)
     *
     * @param stegoFile Stego file from which message needs to be extracted
     * @return Extracted message (List's first element is filename and second element is the message as byte array)
     * @throws OpenStegoException
     */
    public List<?> extractData(File stegoFile) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.DATA_HIDING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_DH);
        }

        return extractData(CommonUtil.getFileBytes(stegoFile), stegoFile.getName());
    }

    /**
     * Method to extract the watermark data from stego data
     *
     * @param stegoData Stego data from which the watermark needs to be extracted
     * @param stegoFileName Name of the stego file
     * @param origSigData Original signature data
     * @return Extracted watermark
     * @throws OpenStegoException
     */
    public byte[] extractMark(byte[] stegoData, String stegoFileName, byte[] origSigData) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.WATERMARKING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_WM);
        }

        return this.plugin.extractData(stegoData, stegoFileName, origSigData);
    }

    /**
     * Method to extract the watermark data from stego data (alternate API)
     *
     * @param stegoFile Stego file from which watermark needs to be extracted
     * @param origSigFile Original signature file
     * @return Extracted watermark
     * @throws OpenStegoException
     */
    public byte[] extractMark(File stegoFile, File origSigFile) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.WATERMARKING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_WM);
        }

        return extractMark(CommonUtil.getFileBytes(stegoFile), stegoFile.getName(), CommonUtil.getFileBytes(origSigFile));
    }

    /**
     * Method to check the correlation for the given image and the original signature
     *
     * @param stegoData Stego data containing the watermark
     * @param stegoFileName Name of the stego file
     * @param origSigData Original signature data
     * @return Correlation
     * @throws OpenStegoException
     */
    public double checkMark(byte[] stegoData, String stegoFileName, byte[] origSigData) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.WATERMARKING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_WM);
        }

        return this.plugin.checkMark(stegoData, stegoFileName, origSigData);
    }

    /**
     * Method to check the correlation for the given image and the original signature (alternate API)
     *
     * @param stegoFile Stego file from which watermark needs to be extracted
     * @param origSigFile Original signature file
     * @return Correlation
     * @throws OpenStegoException
     */
    public double checkMark(File stegoFile, File origSigFile) throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.WATERMARKING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_WM);
        }

        double correl = checkMark(CommonUtil.getFileBytes(stegoFile), stegoFile.getName(), CommonUtil.getFileBytes(origSigFile));
        if (Double.isNaN(correl)) {
            correl = 0.0;
        }
        return correl;
    }

    /**
     * Method to generate the signature data using the given plugin
     *
     * @return Signature data
     * @throws OpenStegoException
     */
    public byte[] generateSignature() throws OpenStegoException {
        if (!this.plugin.getPurposes().contains(OpenStegoPlugin.Purpose.WATERMARKING)) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PLUGIN_DOES_NOT_SUPPORT_WM);
        }

        if (this.config.getPassword() == null || this.config.getPassword().trim().length() == 0) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.PWD_MANDATORY_FOR_GENSIG);
        }

        return this.plugin.generateSignature();
    }

    /**
     * Method to get difference between original cover file and the stegged file
     *
     * @param stegoData Stego data containing the embedded data
     * @param stegoFileName Name of the stego file
     * @param coverData Original cover data
     * @param coverFileName Name of the cover file
     * @param diffFileName Name of the output difference file
     * @return Difference data
     * @throws OpenStegoException
     */
    public byte[] getDiff(byte[] stegoData, String stegoFileName, byte[] coverData, String coverFileName, String diffFileName)
            throws OpenStegoException {
        return this.plugin.getDiff(stegoData, stegoFileName, coverData, coverFileName, diffFileName);
    }

    /**
     * Method to get difference between original cover file and the stegged file
     *
     * @param stegoFile Stego file containing the embedded data
     * @param coverFile Original cover file
     * @param diffFileName Name of the output difference file
     * @return Difference data
     * @throws OpenStegoException
     */
    public byte[] getDiff(File stegoFile, File coverFile, String diffFileName) throws OpenStegoException {
        return getDiff(CommonUtil.getFileBytes(stegoFile), stegoFile.getName(), CommonUtil.getFileBytes(coverFile), coverFile.getName(),
            diffFileName);
    }

    /**
     * Get method for configuration data
     *
     * @return Configuration data
     */
    public OpenStegoConfig getConfig() {
        return this.config;
    }

    /**
     * Main method for calling openstego from command line.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            // Load the stego plugins
            PluginManager.loadPlugins();
            // Initialize preferences
            UserPreferences.init();

            if (args.length == 0) { // Start GUI
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // Ignore
                }
                new OpenStegoUI().setVisible(true);
            } else {
                OpenStegoCmd.execute(args);
            }
        } catch (OpenStegoException osEx) {
            if (osEx.getErrorCode() == OpenStegoException.UNHANDLED_EXCEPTION) {
                osEx.printStackTrace(System.err);
            } else {
                System.err.println(osEx.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}
