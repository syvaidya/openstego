/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2014 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.lsb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.plugin.template.image.DHImagePluginTemplate;
import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.ui.PluginEmbedOptionsUI;
import net.sourceforge.openstego.util.ImageUtil;
import net.sourceforge.openstego.util.LabelUtil;
import net.sourceforge.openstego.util.cmd.CmdLineOption;
import net.sourceforge.openstego.util.cmd.CmdLineOptions;

/**
 * Plugin for OpenStego which implements the Least-significant bit algorithm of steganography
 */
public class LSBPlugin extends DHImagePluginTemplate
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
     * Default constructor
     */
    public LSBPlugin()
    {
        LabelUtil.addNamespace(NAMESPACE, "net.sourceforge.openstego.resource.LSBPluginLabels");
        new LSBErrors(); // Initialize error codes
    }

    /**
     * Gives the name of the plugin
     * 
     * @return Name of the plugin
     */
    public String getName()
    {
        return "LSB";
    }

    /**
     * Gives a short description of the plugin
     * 
     * @return Short description of the plugin
     */
    public String getDescription()
    {
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
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName)
            throws OpenStegoException
    {
        int numOfPixels = 0;
        BufferedImage image = null;
        LSBOutputStream lsbOS = null;

        try
        {
            // Generate random image, if input image is not provided
            if(cover == null)
            {
                numOfPixels = (int) (LSBDataHeader.getMaxHeaderSize() * 8 / 3.0);
                numOfPixels += (int) (msg.length * 8 / (3.0 * ((LSBConfig) this.config).getMaxBitsUsedPerChannel()));
                image = ImageUtil.generateRandomImage(numOfPixels);
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
     * 
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
     * 
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @param origSigData Optional signature data file for watermark
     * @return Extracted message
     * @throws OpenStegoException
     */
    public byte[] extractData(byte[] stegoData, String stegoFileName, byte[] origSigData) throws OpenStegoException
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
                throw new OpenStegoException(null, NAMESPACE, LSBErrors.ERR_IMAGE_DATA_READ);
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

        super.getWritableFileExtensions();
        String format = null;
        String[] compTypes = null;
        Iterator<ImageWriter> iter = null;
        ImageWriteParam writeParam = null;

        for(int i = writeFormats.size() - 1; i >= 0; i--)
        {
            format = writeFormats.get(i);
            iter = ImageIO.getImageWritersBySuffix(format);
            while(iter.hasNext())
            {
                writeParam = (iter.next()).getDefaultWriteParam();
                try
                {
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    compTypes = writeParam.getCompressionTypes();
                    if(compTypes.length > 0)
                    {
                        writeParam.setCompressionType(compTypes[0]);
                    }
                    writeFormats.remove(i);
                }
                catch(UnsupportedOperationException uoEx) // Compression not supported
                {
                    break;
                }

                // Only lossless image compression is supported
                if(writeParam.isCompressionLossless())
                {
                    break;
                }
            }
        }

        // Expicilty removing GIF and WBMP formats, as they use unsupported color models
        writeFormats.remove("gif");
        writeFormats.remove("wbmp");

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
        return new LSBEmbedOptionsUI(stegoUI);
    }

    /**
     * Method to populate the standard command-line options used by this plugin
     * 
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     * @throws OpenStegoException
     */
    public void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException
    {
        options.add("-b", "--maxBitsUsedPerChannel", CmdLineOption.TYPE_OPTION, true);
    }

    /**
     * Method to get the configuration class specific to this plugin
     * 
     * @return Configuration class specific to this plugin
     */
    public Class<? extends OpenStegoConfig> getConfigClass()
    {
        return LSBConfig.class;
    }

    /**
     * Method to get the usage details of the plugin
     * 
     * @return Usage details of the plugin
     * @throws OpenStegoException
     */
    public String getUsage() throws OpenStegoException
    {
        LSBConfig defaultConfig = new LSBConfig();
        return labelUtil.getString("plugin.usage", new Integer(defaultConfig.getMaxBitsUsedPerChannel()));
    }
}
