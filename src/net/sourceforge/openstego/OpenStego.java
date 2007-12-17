/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

/**
 * Main class for openstego
 */
public class OpenStego
{
    private static final String VERSION_STRING = "openstego v0.1";

    /**
     * Configuration data
     */
    private StegoConfig config = null;

    /**
     * Default Constructor
     */
    public OpenStego()
    {
        this.config = new StegoConfig();
    }

    /**
     * Constructor with configuration data
     * @param config StegoConfig object with configuration data
     */
    public OpenStego(StegoConfig config)
    {
        this.config = config;
    }

    /**
     * Constructor with configuration data in the form of map
     * @param propMap Configuration data map
     */
    public OpenStego(Map propMap)
    {
        this.config = new StegoConfig(propMap);
    }

    /**
     * Method to embed the data into an image file
     * @param dataFileName Data file name
     * @param imageFileName Image file name
     * @return Image with embedded data
     * @throws IOException
     */
    public BufferedImage embedData(String dataFileName, String imageFileName) throws IOException
    {
        byte[] data = null;
        BufferedImage image = null;
        OutputStream os = null;
        StegoOutputStream stegoOS = null;

        image = readImage(imageFileName);
        data = getFileBytes(new File(dataFileName));
        stegoOS = new StegoOutputStream(image, data.length, config);
        if(config.isUseCompression())
        {
            os = new GZIPOutputStream(stegoOS);
        }
        else
        {
            os = stegoOS;
        }

        os.write(data);
        os.close();
        stegoOS.close();

        return stegoOS.getImage();
    }

    /**
     * Method to extract the data from an image file
     * @param imageFileName Image file name
     * @return Extracted data
     * @throws Exception
     */
    public byte[] extractData(String imageFileName) throws Exception
    {
        byte[] data = null;
        InputStream is = null;
        StegoInputStream stegoIS = null;

        stegoIS = new StegoInputStream(ImageIO.read(new File(imageFileName)), config);
        if(config.isUseCompression())
        {
            is = new GZIPInputStream(stegoIS);
        }
        else
        {
            is = stegoIS;
        }
        data = new byte[stegoIS.getDataLength()];

        is.read(data);
        is.close();
        stegoIS.close();

        return data;
    }

    /**
     * Helper method to get byte array data from given file
     * @param fileName Name of the file
     * @return File data as byte array
     * @throws IOException
     */
    private byte[] getFileBytes(File file) throws IOException
    {
        int offset = 0;
        int bytesRead = 0;
        long len = 0;
        byte[] data = null;
        FileInputStream is = null;

        is = new FileInputStream(file);
        len = file.length();
        data = new byte[(int) len];

        // Read data
        while(offset < data.length && (bytesRead = is.read(data, offset, data.length - offset)) >= 0)
        {
            offset = offset + bytesRead;
        }

        if(offset < data.length)
        {
            throw new IOException("Error loading file: " + file.getName());
        }

        is.close();
        return data;
    }

    /**
     * Method to load the image file
     * @param imageFileName Image file name
     * @throws IOException
     * @return Buffered image
     */
    private BufferedImage readImage(String imageFileName) throws IOException
    {
        return ImageIO.read(new File(imageFileName));
    }

    /**
     * Method to write the image file
     * @param image Image data
     * @param imageFileName Image file name
     * @throws IOException
     * @return Buffered image
     */
    private void writeImage(BufferedImage image, String imageFileName) throws IOException
    {
        ImageIO.write(image, config.getDefaultImageOutputType(), new File(
            imageFileName.substring(0, imageFileName.lastIndexOf('.')) + "_out." + config.getDefaultImageOutputType()));
    }

    /**
     * Main method for calling openstego from command line.
     *   Usage:
     *        java -jar <path_to>/openstego.jar -embed <data_file> <image_file>
     *     OR java -jar <path_to>/openstego.jar -extract <image_file>
     *
     * For '-embed' option, openstego will embed the data into the given image file, and save the file
     * as PNG after appending '_out' to the file name.
     *
     * For '-extract' option, openstego will output the extracted data on the standard OUT stream, so
     * make sure that output is redirected to required file.
     *
     * @param args
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
            stego.writeImage(stego.embedData(dataFileName, imageFileName), imageFileName);
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
            System.out.write(stego.extractData(imageFileName));
        }
        else
        {
            displayUsage();
            return;
        }
    }

    /**
     * Method to display utility usage
     */
    private static void displayUsage()
    {
        System.err.println(VERSION_STRING + ". Usage:");
        System.err.println("     java -jar <path>" + File.separator + "openstego.jar -embed [options] <data_file> <image_file>");
        System.err.println("  OR java -jar <path>" + File.separator + "openstego.jar -extract <image_file>");
        System.err.println();
        System.err.println("  For '-embed' option, openstego will embed the data into the given image file");
        System.err.println("  and save the file as PNG after appending '_out' to the file name.");
        System.err.println();
        System.err.println("    [options] can be specified for '-embed' and should be of the format ");
        System.err.println("    '--name=value' pairs. Supported options are:");
        System.err.println();
        System.err.println("      maxBitsUsedPerChannel  - Max number of bits to use per color channel in");
        System.err.println("                               the image for embedding data. This value can be");
        System.err.println("                               increased at the expense of image quality, in");
        System.err.println("                               case size of image is not able to accommodate");
        System.err.println("                               the data (Default = " +  (new StegoConfig()).getMaxBitsUsedPerChannel() + ")");
        System.err.println();
        System.err.println("      useCompression         - Flag to indicate whether compression should be");
        System.err.println("                               used on the data or not (Default = " +  (new StegoConfig()).isUseCompression() + ")");
        System.err.println();
        System.err.println("  For '-extract' option, openstego will output the extracted data on the");
        System.err.println("  standard OUT stream, so make sure that output is redirected to required file.");
    }
}