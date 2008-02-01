/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.lsb;

import java.awt.image.BufferedImage;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.imageio.ImageIO;

import net.sourceforge.openstego.OpenStegoException;

/**
 * Image utilities
 */
public class ImageUtil
{
    /**
     * Method to generate a random image filled with noise. The size of the image will be calculated based on the
     * length of data (after compression) that needs to be embedded, and the 'maxBitsUsedPerChannel' parameter.
     * @param dataLength Length of data in bytes which the image should be able to accommodate
     * @param maxBitsUsedPerChannel Maximum bits used per color channel
     * @return Random image filled with noise
     * @throws OpenStegoException
     */
    public static BufferedImage generateRandomImage(int dataLength, int maxBitsUsedPerChannel) throws OpenStegoException
    {
        final double ASPECT_RATIO = 4.0 / 3.0;
        int numOfPixels = 0;
        int width = 0;
        int height = 0;
        byte[] rgbValue = new byte[3];
        BufferedImage image = null;
        SecureRandom random = null;

        try
        {
            random = SecureRandom.getInstance("SHA1PRNG");

            numOfPixels = (int) ((LSBDataHeader.getMaxHeaderSize() * 8 / 3.0)
                            + (dataLength * 8 / (3.0 * maxBitsUsedPerChannel)));
            width = (int) Math.ceil(Math.sqrt(numOfPixels * ASPECT_RATIO));
            height = (int) Math.ceil(numOfPixels / (double) width);

            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for(int x = 0; x < width; x++)
            {
                for(int y = 0; y < height; y++)
                {
                    random.nextBytes(rgbValue);
                    image.setRGB(x, y, LSBDataHeader.byteToInt(rgbValue[0])
                                    + (LSBDataHeader.byteToInt(rgbValue[1]) << 8)
                                    + (LSBDataHeader.byteToInt(rgbValue[2]) << 16));
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
     * @param plugin Reference to the LSB plugin
     * @return Image data as byte array
     * @throws OpenStegoException
     */
    public static byte[] imageToByteArray(BufferedImage image, String imageFileName, LSBPlugin plugin)
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
                    throw new OpenStegoException(LSBPlugin.NAMESPACE, LSBErrors.IMAGE_TYPE_INVALID, imageType, null);
                }
                if(imageType.equals("jp2"))
                {
                    imageType = "jpeg 2000";
                }
                ImageIO.write(image, imageType, barrOS);
            }
            else
            {
                ImageIO.write(image, ((LSBConfig) plugin.getConfig()).getImageFileExtension(), barrOS);
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
     * @para imgFileName Name of the image file
     * @return Buffered image
     * @throws OpenStegoException
     */
    public static BufferedImage byteArrayToImage(byte[] imageData, String imgFileName)
        throws OpenStegoException
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
                throw new OpenStegoException(LSBPlugin.NAMESPACE, LSBErrors.IMAGE_FILE_INVALID, imgFileName, null);
            }
            return image;
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(ioEx);
        }
    }
}
