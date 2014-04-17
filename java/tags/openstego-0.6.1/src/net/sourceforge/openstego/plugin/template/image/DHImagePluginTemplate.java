/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2014 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.image;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import net.sourceforge.openstego.DataHidingPlugin;
import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.ui.PluginEmbedOptionsUI;
import net.sourceforge.openstego.util.ImageUtil;
import net.sourceforge.openstego.util.cmd.CmdLineOptions;

/**
 * Template plugin for OpenStego which implements image based steganography for data hiding
 */
public abstract class DHImagePluginTemplate extends DataHidingPlugin
{
    /**
     * Static list of supported read formats
     */
    protected static List<String> readFormats = null;

    /**
     * Static list of supported write formats
     */
    protected static List<String> writeFormats = null;

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
    public final byte[] getDiff(byte[] stegoData, String stegoFileName, byte[] coverData, String coverFileName,
            String diffFileName) throws OpenStegoException
    {
        BufferedImage stegoImage = null;
        BufferedImage coverImage = null;
        BufferedImage diffImage = null;

        stegoImage = ImageUtil.byteArrayToImage(stegoData, stegoFileName);
        coverImage = ImageUtil.byteArrayToImage(coverData, coverFileName);
        diffImage = ImageUtil.getDiffImage(stegoImage, coverImage);

        return ImageUtil.imageToByteArray(diffImage, diffFileName, this);
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
        readFormats = new ArrayList<String>();

        formats = ImageIO.getReaderFormatNames();
        for(int i = 0; i < formats.length; i++)
        {
            format = formats[i].toLowerCase();
            if(format.indexOf("jpeg") >= 0 && format.indexOf("2000") >= 0)
            {
                format = "jp2";
            }
            if(!readFormats.contains(format))
            {
                readFormats.add(format);
            }
        }

        Collections.sort(readFormats);
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
        writeFormats = new ArrayList<String>();

        formats = ImageIO.getWriterFormatNames();
        for(int i = 0; i < formats.length; i++)
        {
            format = formats[i].toLowerCase();
            if(format.indexOf("jpeg") >= 0 && format.indexOf("2000") >= 0)
            {
                format = "jp2";
            }
            if(!writeFormats.contains(format))
            {
                writeFormats.add(format);
            }
        }

        Collections.sort(writeFormats);
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
        return OpenStegoConfig.class;
    }
}
