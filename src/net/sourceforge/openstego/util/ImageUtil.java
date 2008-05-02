/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.OpenStegoPlugin;

/**
 * Image utilities
 */
public class ImageUtil
{
    /**
     * Default image type in case not provided
     */
    public static String DEFAULT_IMAGE_TYPE = "png";

    /**
     * Method to generate a random image filled with noise. 
     * @param numOfPixels Number of pixels required in the image
     * @return Random image filled with noise
     * @throws OpenStegoException
     */
    public static BufferedImage generateRandomImage(int numOfPixels) throws OpenStegoException
    {
        final double ASPECT_RATIO = 4.0 / 3.0;
        int width = 0;
        int height = 0;
        byte[] rgbValue = new byte[3];
        BufferedImage image = null;
        SecureRandom random = null;

        try
        {
            random = SecureRandom.getInstance("SHA1PRNG");

            width = (int) Math.ceil(Math.sqrt(numOfPixels * ASPECT_RATIO));
            height = (int) Math.ceil(numOfPixels / (double) width);

            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for(int x = 0; x < width; x++)
            {
                for(int y = 0; y < height; y++)
                {
                    random.nextBytes(rgbValue);
                    image.setRGB(x, y, byteToInt(rgbValue[0]) + (byteToInt(rgbValue[1]) << 8)
                            + (byteToInt(rgbValue[2]) << 16));
                }
            }

            return image;
        }
        catch(NoSuchAlgorithmException nsaEx)
        {
            throw new OpenStegoException(nsaEx);
        }
    }

    /**
     * Method to convert BufferedImage to byte array
     * @param image Image data
     * @param imageFileName Name of the image file
     * @param plugin Reference to the plugin
     * @return Image data as byte array
     * @throws OpenStegoException
     */
    public static byte[] imageToByteArray(BufferedImage image, String imageFileName, OpenStegoPlugin plugin)
            throws OpenStegoException
    {
        ByteArrayOutputStream barrOS = new ByteArrayOutputStream();
        String imageType = null;

        try
        {
            if(imageFileName != null)
            {
                imageType = imageFileName.substring(imageFileName.lastIndexOf('.') + 1).toLowerCase();
                if(!plugin.getWritableFileExtensions().contains(imageType))
                {
                    throw new OpenStegoException(OpenStego.NAMESPACE, OpenStegoException.IMAGE_TYPE_INVALID, imageType,
                            null);
                }
                if(imageType.equals("jp2"))
                {
                    imageType = "jpeg 2000";
                }
                ImageIO.write(image, imageType, barrOS);
            }
            else
            {
                ImageIO.write(image, DEFAULT_IMAGE_TYPE, barrOS);
            }
            return barrOS.toByteArray();
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to convert byte array to image
     * @param imageData Image data as byte array
     * @param imgFileName Name of the image file
     * @return Buffered image
     * @throws OpenStegoException
     */
    public static BufferedImage byteArrayToImage(byte[] imageData, String imgFileName) throws OpenStegoException
    {
        BufferedImage image = null;
        try
        {
            if(imageData == null)
            {
                return null;
            }

            image = ImageIO.read(new ByteArrayInputStream(imageData));
            if(image == null)
            {
                throw new OpenStegoException(OpenStego.NAMESPACE, OpenStegoException.IMAGE_FILE_INVALID, imgFileName,
                        null);
            }
            return image;
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Byte to Int converter
     * @param b Input byte value
     * @return Int value
     */
    public static int byteToInt(int b)
    {
        int i = (int) b;
        if(i < 0)
        {
            i = i + 256;
        }
        return i;
    }
}
