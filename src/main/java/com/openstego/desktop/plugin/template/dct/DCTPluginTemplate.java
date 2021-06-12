/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.template.dct;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;
import com.openstego.desktop.ui.OpenStegoFrame;
import com.openstego.desktop.ui.PluginEmbedOptionsUI;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.cmd.CmdLineOptions;

import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Template plugin for OpenStego which implements the DCT based steganography for images (transfer domain)
 */
public abstract class DCTPluginTemplate extends OpenStegoPlugin<DCTConfig> {
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
        DCTErrors.init(); // Initialize error codes
    }

    /**
     * Method to get the list of supported file extensions for reading
     *
     * @return List of supported file extensions for reading
     */
    @Override
    public List<String> getReadableFileExtensions() {
        if (readFormats != null) {
            return readFormats;
        }

        String format;
        String[] formats;
        List<String> formatList = new ArrayList<>();

        formats = ImageIO.getReaderFormatNames();
        for (String s : formats) {
            format = s.toLowerCase();
            if (format.contains("jpeg") && format.contains("2000")) {
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
     */
    @Override
    public List<String> getWritableFileExtensions() {
        if (writeFormats != null) {
            return writeFormats;
        }

        String format;
        String[] formats;
        List<String> formatList = new ArrayList<>();

        formats = ImageIO.getWriterFormatNames();
        for (String s : formats) {
            format = s.toLowerCase();
            if (format.contains("jpeg") && format.contains("2000")) {
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
     * Method to populate the standard command-line options used by this plugin
     *
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     */
    @Override
    public void populateStdCmdLineOptions(CmdLineOptions options) {
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
