/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.util.LabelUtil;

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
import javax.swing.UIManager;

/**
 * This is the main class for OpenStego. It includes the {@link #main(java.lang.String[])} method which provides the
 * command line interface for the tool. It also has API methods which can be used by external programs
 * when using OpenStego as a library.
 */
public class OpenStego
{
    /**
     * Version string for OpenStego
     */
    private static final String VERSION_STRING = "openstego v0.1.1";

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
        GZIPOutputStream os = null;
        StegoOutputStream stegoOS = null;

        stegoOS = new StegoOutputStream(image, data.length, config);
        if(config.isUseCompression())
        {
            os = new GZIPOutputStream(stegoOS);
            os.write(data, 0, data.length);
            os.finish();
            os.close();
        }
        else
        {
            stegoOS.write(data);
        }
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
        InputStream is = null;
        StegoInputStream stegoIS = null;

        stegoIS = new StegoInputStream(image, config);
        if(config.isUseCompression())
        {
            is = new GZIPInputStream(stegoIS);
        }
        else
        {
            is = stegoIS;
        }
        data = new byte[stegoIS.getDataLength()];

        bytesRead = is.read(data, 0, data.length);
        is.close();
        stegoIS.close();

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
     * Helper method to get byte array data from given file
     * @param file File to be read
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
            throw new IOException(LabelUtil.getString("err.fileLoadError", new Object[] { file.getName() }));
        }

        is.close();
        return data;
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
     * <pre>
     *   Usage:
     *        java -jar &lt;path_to&gt;/openstego.jar -embed &lt;data_file&gt; &lt;image_file&gt;
     *     OR java -jar &lt;path_to&gt;/openstego.jar -extract &lt;image_file&gt;
     * </pre>
     * For '-embed' option, openstego will embed the data into the given image file, and save the file
     * as PNG after appending '_out' to the file name.
     * <p>
     * For '-extract' option, openstego will output the extracted data on the standard OUT stream, so
     * make sure that output is redirected to required file.
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
