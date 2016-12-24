/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2014 Samir Vaidya
 */

package net.sourceforge.openstego.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

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
     * Constructor is private so that this class is not instantiated
     */
    private ImageUtil()
    {
    }

    /**
     * Default image type in case not provided
     */
    public static String DEFAULT_IMAGE_TYPE = "png";

    /**
     * Method to generate a random image filled with noise.
     *
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
                    image.setRGB(x, y, CommonUtil.byteToInt(rgbValue[0]) + (CommonUtil.byteToInt(rgbValue[1]) << 8)
                            + (CommonUtil.byteToInt(rgbValue[2]) << 16));
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
     *
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
                    throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.IMAGE_TYPE_INVALID,
                            imageType);
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
     *
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
                throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.IMAGE_FILE_INVALID,
                        imgFileName);
            }
            return image;
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Get RGB data array from given image
     *
     * @param image Image
     * @return List with three elements of two-dimensional int's - R, G and B
     */
    public static List<int[][]> getRgbFromImage(BufferedImage image)
    {
        List<int[][]> rgb = new ArrayList<int[][]>();
        int[][] r = null;
        int[][] g = null;
        int[][] b = null;
        int width = 0;
        int height = 0;

        width = image.getWidth();
        height = image.getHeight();

        r = new int[height][width];
        g = new int[height][width];
        b = new int[height][width];

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                r[i][j] = (image.getRGB(j, i) >> 16) & 0xFF;
                g[i][j] = (image.getRGB(j, i) >> 8) & 0xFF;
                b[i][j] = (image.getRGB(j, i) >> 0) & 0xFF;
            }
        }

        rgb.add(r);
        rgb.add(g);
        rgb.add(b);

        return rgb;
    }

    /**
     * Get YUV data from given image's RGB data
     *
     * @param image Image
     * @return List with three elements of two-dimensional int's - Y, U and V
     */
    public static List<int[][]> getYuvFromImage(BufferedImage image)
    {
        List<int[][]> yuv = new ArrayList<int[][]>();
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

        y = new int[height][width];
        u = new int[height][width];
        v = new int[height][width];

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                r = (image.getRGB(j, i) >> 16) & 0xFF;
                g = (image.getRGB(j, i) >> 8) & 0xFF;
                b = (image.getRGB(j, i) >> 0) & 0xFF;

                // Convert RGB to YUV colorspace
                // y[i][j] = (int) ((0.257 * r) + (0.504 * g) + (0.098 * b) + 16);
                // u[i][j] = (int) (-(0.148 * r) - (0.291 * g) + (0.439 * b) + 128);
                // v[i][j] = (int) ((0.439 * r) - (0.368 * g) - (0.071 * b) + 128);
                // y[i][j] = (int) ((0.2990 * r) + (0.5870 * g) + (0.1140 * b));
                // u[i][j] = (int) ((-0.1687 * r) - (0.3313 * g) + (0.5000 * b) + 128);
                // v[i][j] = (int) ((0.5000 * r) - (0.4187 * g) - (0.0813 * b) + 128);
                y[i][j] = (int) ((0.299 * r) + (0.587 * g) + (0.114 * b));
                u[i][j] = (int) ((-0.147 * r) - (0.289 * g) + (0.436 * b));
                v[i][j] = (int) ((0.615 * r) - (0.515 * g) - (0.100 * b));
            }
        }

        yuv.add(y);
        yuv.add(u);
        yuv.add(v);

        return yuv;
    }

    /**
     * Get image from given RGB data
     *
     * @param rgb List with three elements of two-dimensional int's - R, G and B
     * @return Image
     */
    public static BufferedImage getImageFromRgb(List<int[][]> rgb)
    {
        BufferedImage image = null;
        int width = 0;
        int height = 0;
        int[][] r = null;
        int[][] g = null;
        int[][] b = null;

        r = rgb.get(0);
        g = rgb.get(1);
        b = rgb.get(2);

        height = r.length;
        width = r[0].length;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                image.setRGB(j, i, (r[i][j] << 16) + (g[i][j] << 8) + b[i][j]);
            }
        }

        return image;
    }

    /**
     * Get image (with RGB data) from given YUV data
     *
     * @param yuv List with three elements of two-dimensional int's - Y, U and V
     * @param imgType Type of image (e.g. BufferedImage.TYPE_INT_RGB)
     * @return Image
     */
    public static BufferedImage getImageFromYuv(List<int[][]> yuv, int imgType)
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

        y = yuv.get(0);
        u = yuv.get(1);
        v = yuv.get(2);

        height = y.length;
        width = y[0].length;
        image = new BufferedImage(width, height, (imgType == 0 ? BufferedImage.TYPE_INT_RGB : imgType));

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                // Convert YUV back to RGB
                // r = pixelRange(1.164 * (y[i][j] - 16) + 1.596 * (v[i][j] - 128));
                // g = pixelRange(1.164 * (y[i][j] - 16) - 0.391 * (u[i][j] - 128) - 0.813 * (v[i][j] - 128));
                // b = pixelRange(1.164 * (y[i][j] - 16) + 2.018 * (u[i][j] - 128));
                // r = pixelRange(y[i][j] + 1.40200 * (v[i][j] - 128));
                // g = pixelRange(y[i][j] - 0.34414 * (u[i][j] - 128) - 0.71414 * (v[i][j] - 128));
                // b = pixelRange(y[i][j] + 1.77200 * (u[i][j] - 128));
                r = pixelRange(y[i][j] + 1.140 * v[i][j]);
                g = pixelRange(y[i][j] - 0.395 * u[i][j] - 0.581 * v[i][j]);
                b = pixelRange(y[i][j] + 2.032 * u[i][j]);

                image.setRGB(j, i, (r << 16) + (g << 8) + b);
            }
        }

        return image;
    }

    /**
     * Utility method to limit the value within [0,255] range
     *
     * @param p Input value
     * @return Limited value
     */
    public static int pixelRange(int p)
    {
        return ((p > 255) ? 255 : (p < 0) ? 0 : p);
    }

    /**
     * Utility method to limit the value within [0,255] range
     *
     * @param p Input value
     * @return Limited value
     */
    public static int pixelRange(double p)
    {
        return ((p > 255) ? 255 : (p < 0) ? 0 : (int) p);
    }

    /**
     * Method to pad an image such that it becomes perfect square. The padding uses black color
     *
     * @param image Input image
     * @return Image with square dimensions
     */
    public static BufferedImage makeImageSquare(BufferedImage image)
    {
        int max = 0;

        max = CommonUtil.max(image.getWidth(), image.getHeight());
        return cropImage(image, max, max);
    }

    /**
     * Method crop an image to the given dimensions. If dimensions are more than the input image size, then the image
     * gets padded with black color
     *
     * @param image Input image
     * @param cropWidth Width required for cropped image
     * @param cropHeight Height required for cropped image
     * @return Cropped image
     */
    public static BufferedImage cropImage(BufferedImage image, int cropWidth, int cropHeight)
    {
        BufferedImage retImg = null;
        int width = 0;
        int height = 0;

        width = image.getWidth();
        height = image.getHeight();

        retImg = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_RGB);

        for(int i = 0; i < cropWidth; i++)
        {
            for(int j = 0; j < cropHeight; j++)
            {
                if(i < width && j < height)
                {
                    retImg.setRGB(i, j, image.getRGB(i, j));
                }
                else
                {
                    retImg.setRGB(i, j, 0);
                }
            }
        }

        return retImg;
    }

    /**
     * Method generate difference image between two given images
     *
     * @param leftImage Left input image
     * @param rightImage Right input image
     * @return Difference image
     * @throws OpenStegoException
     */
    public static BufferedImage getDiffImage(BufferedImage leftImage, BufferedImage rightImage)
            throws OpenStegoException
    {
        int leftW = 0;
        int leftH = 0;
        int rightW = 0;
        int rightH = 0;
        int min = 0;
        int max = 0;
        int diff = 0;
        // double error = 0.0;
        BufferedImage diffImage = null;

        leftW = leftImage.getWidth();
        leftH = leftImage.getHeight();
        rightW = rightImage.getWidth();
        rightH = rightImage.getHeight();
        if(leftW != rightW || leftH != rightH)
        {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.IMAGE_FILE_INVALID);
        }
        diffImage = new BufferedImage(leftW, leftH, BufferedImage.TYPE_INT_RGB);

        min = Math.abs(leftImage.getRGB(0, 0) - rightImage.getRGB(0, 0));
        max = min;

        for(int i = 0; i < leftW; i++)
        {
            for(int j = 0; j < leftH; j++)
            {
                diff = Math.abs(leftImage.getRGB(i, j) - rightImage.getRGB(i, j));
                // error += diff * diff;
                if(diff < min)
                {
                    min = diff;
                }
                if(diff > max)
                {
                    max = diff;
                }
            }
        }

        for(int i = 0; i < leftW; i++)
        {
            for(int j = 0; j < leftH; j++)
            {
                diff = Math.abs(leftImage.getRGB(i, j) - rightImage.getRGB(i, j));
                diffImage.setRGB(i, j, pixelRange((double) (diff - min) / (double) (max - min) * Math.pow(2, 32)));
                // TODO
            }
        }

        return diffImage;
    }
}
