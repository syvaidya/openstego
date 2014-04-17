/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2014 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.dct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.OpenStegoPlugin;
import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.ui.PluginEmbedOptionsUI;
import net.sourceforge.openstego.util.LabelUtil;
import net.sourceforge.openstego.util.cmd.CmdLineOptions;

/**
 * Template plugin for OpenStego which implements the DCT based steganography for images (transfer domain)
 */
public abstract class DCTPluginTemplate extends OpenStegoPlugin
{
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

    static
    {
        LabelUtil.addNamespace(NAMESPACE, "net.sourceforge.openstego.resource.DCTPluginTemplateLabels");
        new DCTErrors(); // Initialize error codes
    }

    /**
     * Method to get the list of supported file extensions for reading
     * 
     * @return List of supported file extensions for reading
     * @throws OpenStegoException
     */
    public List<String> getReadableFileExtensions() throws OpenStegoException
    {
        if(readFormats != null)
        {
            return readFormats;
        }

        String format = null;
        String[] formats = null;
        List<String> formatList = new ArrayList<String>();

        formats = ImageIO.getReaderFormatNames();
        for(int i = 0; i < formats.length; i++)
        {
            format = formats[i].toLowerCase();
            if(format.indexOf("jpeg") >= 0 && format.indexOf("2000") >= 0)
            {
                format = "jp2";
            }
            if(!formatList.contains(format))
            {
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
    public List<String> getWritableFileExtensions() throws OpenStegoException
    {
        if(writeFormats != null)
        {
            return writeFormats;
        }

        String format = null;
        String[] formats = null;
        List<String> formatList = new ArrayList<String>();

        formats = ImageIO.getWriterFormatNames();
        for(int i = 0; i < formats.length; i++)
        {
            format = formats[i].toLowerCase();
            if(format.indexOf("jpeg") >= 0 && format.indexOf("2000") >= 0)
            {
                format = "jp2";
            }
            if(!formatList.contains(format))
            {
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
    public PluginEmbedOptionsUI getEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException
    {
        return null;
    }

    /**
     * Method to populate the standard command-line options used by this plugin
     * 
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     * @throws OpenStegoException
     */
    public void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException
    {
    }

    /**
     * Method to get the configuration class specific to this plugin
     * 
     * @return Configuration class specific to this plugin
     */
    public Class<? extends OpenStegoConfig> getConfigClass()
    {
        return DCTConfig.class;
    }
}
