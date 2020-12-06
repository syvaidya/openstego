/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.template.dct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;
import com.openstego.desktop.ui.OpenStegoUI;
import com.openstego.desktop.ui.PluginEmbedOptionsUI;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.cmd.CmdLineOptions;

/**
 * Template plugin for OpenStego which implements the DCT based steganography for images (transfer domain)
 */
public abstract class DCTPluginTemplate extends OpenStegoPlugin {
    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "DCTTEMPLATE";

    /**
     * Static list of supported read formats
     */
    private static List<String> readFormats = null;

    /**
     * Static list of supported write formats
     */
    private static List<String> writeFormats = null;

    static {
        LabelUtil.addNamespace(NAMESPACE, "i18n.DCTPluginTemplateLabels");
        new DCTErrors(); // Initialize error codes
    }

    /**
     * Method to get the list of supported file extensions for reading
     *
     * @return List of supported file extensions for reading
     * @throws OpenStegoException
     */
    @Override
    public List<String> getReadableFileExtensions() throws OpenStegoException {
        if (readFormats != null) {
            return readFormats;
        }

        String format = null;
        String[] formats = null;
        List<String> formatList = new ArrayList<String>();

        formats = ImageIO.getReaderFormatNames();
        for (int i = 0; i < formats.length; i++) {
            format = formats[i].toLowerCase();
            if (format.indexOf("jpeg") >= 0 && format.indexOf("2000") >= 0) {
                format = "jp2";
            }
            if (!formatList.contains(format)) {
                formatList.add(format);
            }
        }

        Collections.sort(formatList);
        readFormats = formatList;
        return readFormats;
    }

    /**
     * Method to get the list of supported file extensions for writing
     *
     * @return List of supported file extensions for writing
     * @throws OpenStegoException
     */
    @Override
    public List<String> getWritableFileExtensions() throws OpenStegoException {
        if (writeFormats != null) {
            return writeFormats;
        }

        String format = null;
        String[] formats = null;
        List<String> formatList = new ArrayList<String>();

        formats = ImageIO.getWriterFormatNames();
        for (int i = 0; i < formats.length; i++) {
            format = formats[i].toLowerCase();
            if (format.indexOf("jpeg") >= 0 && format.indexOf("2000") >= 0) {
                format = "jp2";
            }
            if (!formatList.contains(format)) {
                formatList.add(format);
            }
        }

        Collections.sort(formatList);
        writeFormats = formatList;
        return writeFormats;
    }

    /**
     * Method to get the UI object specific to this plugin, which will be embedded inside the main OpenStego GUI
     *
     * @param stegoUI Reference to the parent OpenStegoUI object
     * @return UI object specific to this plugin
     * @throws OpenStegoException
     */
    @Override
    public PluginEmbedOptionsUI getEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException {
        return null;
    }

    /**
     * Method to populate the standard command-line options used by this plugin
     *
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     * @throws OpenStegoException
     */
    @Override
    public void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException {
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
