/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.util.LabelUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

/**
 * This is the main class for OpenStego. It includes the {@link #main(java.lang.String[])} method which provides the
 * command line interface for the tool. It also has API methods which can be used by external programs
 * when using OpenStego as a library.
 */
public class OpenStego
{
    /**
     * Configuration data
     */
    private StegoConfig config = null;

    /**
     * Constructor using the default configuration
     */
    public OpenStego()
    {
        this.config = new StegoConfig();
    }

    /**
     * Constructor using <code>StegoConfig</code> object
     * @param config StegoConfig object with configuration data
     */
    public OpenStego(StegoConfig config)
    {
        this.config = config;
    }

    /**
     * Constructor with configuration data in the form of <code>Map<code>
     * @param propMap Map containing the configuration data
     */
    public OpenStego(Map propMap)
    {
        this.config = new StegoConfig(propMap);
    }

    /**
     * Method to embed the data into an image
     * @param data Data to be embedded
     * @param image Source image data into which data needs to be embedded
     * @return Image with embedded data
     * @throws IOException
     */
    public BufferedImage embedData(byte[] data, BufferedImage image) throws IOException
    {
        StegoOutputStream stegoOS = null;

        if(config.isUseCompression())
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            zos.write(data);
            zos.finish();
            zos.close();
            bos.close();
            
            data = bos.toByteArray();
        }

        stegoOS = new StegoOutputStream(image, data.length, config);
        stegoOS.write(data);
        stegoOS.close();

        return stegoOS.getImage();
    }

    /**
     * Method to embed the data into an image (alternate API)
     * @param dataFile File containing the data to be embedded
     * @param imageFile Source image file into which data needs to be embedded
     * @return Image with embedded data
     * @throws IOException
     */
    public BufferedImage embedData(File dataFile, File imageFile) throws IOException
    {
        return embedData(getFileBytes(dataFile), readImage(imageFile));
    }

    /**
     * Method to extract the data from an image
     * @param image Image from which data needs to be extracted
     * @return Extracted data
     * @throws IOException
     */
    public byte[] extractData(BufferedImage image) throws IOException
    {
        int bytesRead = 0;
        byte[] data = null;
        StegoInputStream stegoIS = null;

        stegoIS = new StegoInputStream(image, config);
        data = new byte[stegoIS.getDataLength()];
        bytesRead = stegoIS.read(data, 0, data.length);
        if(bytesRead != data.length)
        {
            throw new IOException(LabelUtil.getString("err.imageDataRead"));
        }
        stegoIS.close();

        if(config.isUseCompression())
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream zis = new GZIPInputStream(bis);
            data = getStreamBytes(zis);
            zis.close();
            bis.close();
        }

        return data;
    }

    /**
     * Method to extract the data from an image (alternate API)
     * @param imageFile Image file from which data needs to be extracted
     * @return Extracted data
     * @throws IOException
     */
    public byte[] extractData(File imageFile) throws IOException
    {
        return extractData(ImageIO.read(imageFile));
    }

    /**
     * Helper method to get byte array data from given InputStream
     * @param is InputStream to read
     * @return Stream data as byte array
     * @throws IOException
     */
    private byte[] getStreamBytes(InputStream is) throws IOException
    {
        final int BUF_SIZE = 512;
        ByteArrayOutputStream bos = null;
        int bytesRead = 0;
        byte[] data = null;

        data = new byte[BUF_SIZE];
        bos = new ByteArrayOutputStream();

        while((bytesRead = is.read(data, 0, BUF_SIZE)) >= 0)
        {
            bos.write(data, 0, bytesRead);
        }

        is.close();
        bos.close();

        return bos.toByteArray();
    }

    /**
     * Helper method to get byte array data from given file
     * @param file File to be read
     * @return File data as byte array
     * @throws IOException
     */
    private byte[] getFileBytes(File file) throws IOException
    {
        return getStreamBytes(new FileInputStream(file));
    }

    /**
     * Method to load the image file
     * @param imageFile Image file
     * @return Buffered image
     * @throws IOException
     */
    private BufferedImage readImage(File imageFile) throws IOException
    {
        return ImageIO.read(imageFile);
    }

    /**
     * Method to write the image file
     * @param image Image data
     * @param imageFileName Image file name
     * @throws IOException
     */
    private void writeImage(BufferedImage image, String imageFileName) throws IOException
    {
        ImageIO.write(image, config.getDefaultImageOutputType(), new File(
            imageFileName.substring(0, imageFileName.lastIndexOf('.')) + "_out." + config.getDefaultImageOutputType()));
    }

    /**
     * Main method for calling openstego from command line.
     *
     * @param args Command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        int count = 0;
        int index = 0;
        String key = null;
        String value = null;
        String option = null;
        String dataFileName = null;
        String imageFileName = null;
        OpenStego stego = null;
        Map propMap = new HashMap();

        if(args.length == 0) // Start GUI
        {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch(Exception e)
            {
            }
            new OpenStegoUI().setVisible(true);
        }
        else
        {
            if(args.length < 2)
            {
                displayUsage();
                return;
            }

            option = args[0];
            if(option.equals("-embed"))
            {
                count = 1;
                while(args[count].startsWith("--"))
                {
                    index = args[count].indexOf('=');
                    if(index == -1)
                    {
                        displayUsage();
                        return;
                    }

                    key = args[count].substring(2, index);
                    value = args[count].substring(index + 1);
                    propMap.put(key, value);

                    count++;
                    if(args.length < count)
                    {
                        displayUsage();
                        return;
                    }
                }

                stego = new OpenStego(propMap);

                if(args.length != (count + 2))
                {
                    displayUsage();
                    return;
                }

                dataFileName = args[count];
                imageFileName = args[count + 1];
                stego.writeImage(stego.embedData(new File(dataFileName), new File(imageFileName)), imageFileName);
            }
            else if(option.equals("-extract"))
            {
                if(args.length != 2)
                {
                    displayUsage();
                    return;
                }
                imageFileName = args[1];
                stego = new OpenStego();
                System.out.write(stego.extractData(new File(imageFileName)));
            }
            else
            {
                displayUsage();
                return;
            }
        }
    }

    /**
     * Method to display usage for OpenStego
     */
    private static void displayUsage()
    {
        System.err.print(LabelUtil.getString("versionString"));
        System.err.println(LabelUtil.getString("cmd.usage"));
    }
}
