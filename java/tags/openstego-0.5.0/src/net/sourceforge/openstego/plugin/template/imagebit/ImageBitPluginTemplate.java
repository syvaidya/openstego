/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.imagebit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.OpenStegoPlugin;
import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.ui.PluginEmbedOptionsUI;
import net.sourceforge.openstego.util.CmdLineOption;
import net.sourceforge.openstego.util.CmdLineOptions;
import net.sourceforge.openstego.util.LabelUtil;

/**
 * Template plugin for OpenStego which implements the bit based steganography for images
 */
public abstract class ImageBitPluginTemplate extends OpenStegoPlugin
{
    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "IMAGEBITTEMPLATE";

    /**
     * Static list of supported read formats
     */
    private static List readFormats = null;

    /**
     * Static list of supported write formats
     */
    private static List writeFormats = null;

    static
    {
        LabelUtil.addNamespace(NAMESPACE, "net.sourceforge.openstego.resource.ImageBitPluginTemplateLabels");
        new ImageBitErrors(); // Initialize error codes
    }

    /**
     * Method to find out whether given stego data can be handled by this plugin or not
     * @param stegoData Stego data containing the message
     * @return Boolean indicating whether the stego data can be handled by this plugin or not
     * @throws OpenStegoException
     */
    public boolean canHandle(byte[] stegoData) throws OpenStegoException
    {
        boolean output = true;

        try
        {
            extractMsgFileName(stegoData, "DUMMY");
        }
        catch(OpenStegoException osEx)
        {
            if(osEx.getErrorCode() != OpenStegoException.INVALID_PASSWORD)
            {
                output = false;
            }
        }

        return output;
    }

    /**
     * Method to get the list of supported file extensions for reading
     * @return List of supported file extensions for reading
     * @throws OpenStegoException
     */
    public List getReadableFileExtensions() throws OpenStegoException
    {
        if(readFormats != null)
        {
            return readFormats;
        }

        String format = null;
        String[] formats = null;
        List formatList = new ArrayList();

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
     * @return List of supported file extensions for writing
     * @throws OpenStegoException
     */
    public List getWritableFileExtensions() throws OpenStegoException
    {
        if(writeFormats != null)
        {
            return writeFormats;
        }

        String format = null;
        String[] formats = null;
        String[] compTypes = null;
        List formatList = new ArrayList();
        Iterator iter = null;
        ImageWriteParam writeParam = null;

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
                iter = ImageIO.getImageWritersBySuffix(format);
                while(iter.hasNext())
                {
                    writeParam = ((ImageWriter) iter.next()).getDefaultWriteParam();
                    try
                    {
                        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        compTypes = writeParam.getCompressionTypes();
                        if(compTypes.length > 0)
                        {
                            writeParam.setCompressionType(compTypes[0]);
                        }
                    }
                    catch(UnsupportedOperationException uoEx) // Compression not supported
                    {
                        formatList.add(format);
                        break;
                    }

                    // Only lossless image compression is supported
                    if(writeParam.isCompressionLossless())
                    {
                        formatList.add(format);
                        break;
                    }
                }
            }
        }

        //Expicilty removing GIF and WBMP formats, as they use unsupported color models
        formatList.remove("gif");
        formatList.remove("wbmp");
        Collections.sort(formatList);

        writeFormats = formatList;
        return writeFormats;
    }

    /**
     * Method to get the UI object specific to this plugin, which will be embedded inside the main OpenStego GUI
     * @param stegoUI Reference to the parent OpenStegoUI object
     * @return UI object specific to this plugin
     * @throws OpenStegoException
     */
    public PluginEmbedOptionsUI getEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException
    {
        return new ImageBitEmbedOptionsUI(stegoUI);
    }

    /**
     * Method to populate the standard command-line options used by this plugin
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     * @throws OpenStegoException
     */
    public void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException
    {
        options.add("-b", "--maxBitsUsedPerChannel", CmdLineOption.TYPE_OPTION, true);
    }

    /**
     * Method to create default configuration data (specific to this plugin)
     * @return Configuration data
     * @throws OpenStegoException
     */
    public OpenStegoConfig createConfig() throws OpenStegoException
    {
        this.config = new ImageBitConfig();
        return this.config;
    }

    /**
     * Method to create configuration data (specific to this plugin) based on the property map
     * @param propMap Property map
     * @return Configuration data
     * @throws OpenStegoException
     */
    public OpenStegoConfig createConfig(Map propMap) throws OpenStegoException
    {
        this.config = new ImageBitConfig(propMap);
        return this.config;
    }

    /**
     * Method to create configuration data (specific to this plugin) based on the command-line options
     * @param options Command-line options
     * @return Configuration data
     * @throws OpenStegoException
     */
    public OpenStegoConfig createConfig(CmdLineOptions options) throws OpenStegoException
    {
        this.config = new ImageBitConfig(options);
        return this.config;
    }
}
