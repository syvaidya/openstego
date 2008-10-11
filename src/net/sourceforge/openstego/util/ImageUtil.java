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
import java.util.ArrayList;

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

    public static ArrayList getYuvFromImage(BufferedImage image)
    {
        ArrayList yuv = new ArrayList();
        int[][] y = null;
        int[][] u = null;
        int[][] v = null;
        int r = 0;
        int g = 0;
        int b = 0;
        int width = 0;
        int height = 0;

        width = image.getWidth();
        height = image.getHeight();

        y = new int[width][height];
        u = new int[width][height];
        v = new int[width][height];

        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                r = (image.getRGB(i, j) >> 16) & 0xFF;
                g = (image.getRGB(i, j) >>  8) & 0xFF;
                b = (image.getRGB(i, j) >>  0) & 0xFF;

                // Convert RGB to YUV colorspace
                y[i][j] = pixelRange( (0.257 * r) + (0.504 * g) + (0.098 * b) + 16 );
                u[i][j] = pixelRange(-(0.148 * r) - (0.291 * g) + (0.439 * b) + 128);
                v[i][j] = pixelRange( (0.439 * r) - (0.368 * g) - (0.071 * b) + 128);
                //y[i][j] = pixelRange(( 0.2990 * r) + (0.5870 * g) + (0.1140 * b));
                //u[i][j] = pixelRange((-0.1687 * r) - (0.3313 * g) + (0.5000 * b) + 128);
                //v[i][j] = pixelRange(( 0.5000 * r) - (0.4187 * g) - (0.0813 * b) + 128);
            }
        }

        yuv.add(y);
        yuv.add(u);
        yuv.add(v);

        return yuv;
    }

    public static BufferedImage getImageFromYuv(ArrayList yuv)
    {
        BufferedImage image = null;
        int width = 0;
        int height = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        int[][] y = null;
        int[][] u = null;
        int[][] v = null;

        y = (int[][]) yuv.get(0);
        u = (int[][]) yuv.get(1);
        v = (int[][]) yuv.get(2);

        width = y.length;
        height = y[0].length;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                // Convert YUV back to RGB
                r = pixelRange(1.164 * (y[i][j] - 16) + 1.596 * (v[i][j] - 128)                          );
                g = pixelRange(1.164 * (y[i][j] - 16) - 0.391 * (u[i][j] - 128) - 0.813 * (v[i][j] - 128));
                b = pixelRange(1.164 * (y[i][j] - 16) + 2.018 * (u[i][j] - 128)                          );
                //r = pixelRange(y[i][j]                             + 1.40200 * (v[i][j] - 128));
                //g = pixelRange(y[i][j] - 0.34414 * (u[i][j] - 128) - 0.71414 * (v[i][j] - 128));
                //b = pixelRange(y[i][j] + 1.77200 * (u[i][j] - 128)                            );

                image.setRGB(i, j, (r << 16) + (g << 8) + b);
            }
        }

        return image;
    }

    /**
     * Utility method to limit the value within [0,255] range
     * @param p Input value
     * @return Limited value
     */
    public static int pixelRange(double p)
    {
        return ((p > 255) ? 255 : (p < 0) ? 0 : (int) p);
    }
}
