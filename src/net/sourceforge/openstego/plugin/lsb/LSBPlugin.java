/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.lsb;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import net.sourceforge.openstego.*;
import net.sourceforge.openstego.ui.OpenStegoPluginUI;
import net.sourceforge.openstego.util.*;

/**
 * Plugin for OpenStego which implements the Least-significant bit algorithm of steganography
 */
public class LSBPlugin extends OpenStegoPlugin
{
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(LSBPlugin.NAMESPACE);

    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "LSB";

    /**
     * Static list of supported read formats
     */
    private static List readFormats = null;

    /**
     * Static list of supported write formats
     */
    private static List writeFormats = null;

    /**
     * Default constructor
     */
    public LSBPlugin()
    {
        LabelUtil.addNamespace(NAMESPACE, "net.sourceforge.openstego.resource.LSBPluginLabels");
    }

    /**
     * Gives the name of the plugin
     * @return Name of the plugin
     */
    public String getName()
    {
        return "LSB";
    }

    /**
     * Gives a short description of the plugin
     * @return Short description of the plugin
     */
    public String getDescription()
    {
        return labelUtil.getString("plugin.description");
    }

    /**
     * Method to embed the message into the cover data
     * @param msg Message to be embedded
     * @param msgFileName Name of the message file. If this value is provided, then the filename should be
     *                    embedded in the cover data
     * @param cover Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException
     */
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName)
        throws OpenStegoException
    {
        BufferedImage image = null;
        LSBOutputStream lsbOS = null;

        try
        {
            // Generate random image, if input image is not provided
            if(cover == null)
            {
                image = ImageUtil.generateRandomImage(msg.length, ((LSBConfig) config).getMaxBitsUsedPerChannel());
            }
            else
            {
                image = ImageUtil.byteArrayToImage(cover, coverFileName);
            }
            lsbOS = new LSBOutputStream(image, msg.length, msgFileName, this.config);
            lsbOS.write(msg);
            lsbOS.close();

            return ImageUtil.imageToByteArray(lsbOS.getImage(), stegoFileName, this);
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to extract the message file name from the stego data
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Message file name
     * @throws OpenStegoException
     */
    public String extractMsgFileName(byte[] stegoData, String stegoFileName) throws OpenStegoException
    {
        LSBInputStream lsbIS = null;

        lsbIS = new LSBInputStream(ImageUtil.byteArrayToImage(stegoData, stegoFileName), this.config);
        return lsbIS.getDataHeader().getFileName();
    }

    /**
     * Method to extract the message from the stego data
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Extracted message
     * @throws OpenStegoException
     */
    public byte[] extractData(byte[] stegoData, String stegoFileName) throws OpenStegoException
    {
        int bytesRead = 0;
        byte[] data = null;
        LSBDataHeader header = null;
        LSBInputStream lsbIS = null;

        try
        {
            lsbIS = new LSBInputStream(ImageUtil.byteArrayToImage(stegoData, stegoFileName), this.config);
            header = lsbIS.getDataHeader();
            data = new byte[header.getDataLength()];

            bytesRead = lsbIS.read(data, 0, data.length);
            if(bytesRead != data.length)
            {
                throw new OpenStegoException(NAMESPACE, LSBErrors.ERR_IMAGE_DATA_READ, null);
            }
            lsbIS.close();

            return data;
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to get the list of supported file extensions for reading
     * @return List of supported file extensions for reading
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
     * Method to populate the standard command-line options used by this plugin
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     */
    public void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException
    {
        options.add("-b", "--maxBitsUsedPerChannel", CmdLineOption.TYPE_OPTION, true);
    }

    /**
     * Method to create default configuration data (specific to this plugin)
     * @return Configuration data
     */
    public OpenStegoConfig createConfig() throws OpenStegoException
    {
        this.config = new LSBConfig();
        return this.config;
    }

    /**
     * Method to create configuration data (specific to this plugin) based on the property map
     * @param propMap Property map
     * @return Configuration data
     */
    public OpenStegoConfig createConfig(Map propMap) throws OpenStegoException
    {
        this.config = new LSBConfig(propMap);
        return this.config;
    }

    /**
     * Method to create configuration data (specific to this plugin) based on the command-line options
     * @param options Command-line options
     * @return Configuration data
     */
    public OpenStegoConfig createConfig(CmdLineOptions options) throws OpenStegoException
    {
        this.config = new LSBConfig(options);
        return this.config;
    }

    /**
     * Method to get the usage details of the plugin
     * @return Usage details of the plugin
     */
    public String getUsage() throws OpenStegoException
    {
        LSBConfig defaultConfig = new LSBConfig();
        return labelUtil.getString("plugin.usage", new Object[] {
                        new Integer(defaultConfig.getMaxBitsUsedPerChannel()) });
    }

    /**
     * Method to get the UI object specific to this plugin, which will be embedded inside the main OpenStego GUI
     * @return UI object specific to this plugin
     */
    public OpenStegoPluginUI getGUI() throws OpenStegoException
    {
        return null;
    }
}