/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import net.sourceforge.openstego.util.LabelUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream to read embedded data from image file
 */
public class StegoInputStream extends InputStream
{
    /**
     * Image data
     */
    private BufferedImage image = null;

    /**
     * Number of bits used per color channel
     */
    private int channelBitsUsed = 1;

    /**
     * Length of the data
     */
    private int dataLength = 0;

    /**
     * Current x co-ordinate
     */
    private int x = 0;

    /**
     * Current y co-ordinate
     */
    private int y = 0;

    /**
     * Current bit number to be read
     */
    private int currBit = 0;

    /**
     * Width of the image
     */
    private int imgWidth = 0;

    /**
     * Height of the image
     */
    private int imgHeight = 0;

    /**
     * Configuration data
     */
    private StegoConfig config = null;

    /**
     * Default constructor
     * @param image Image data to be read
     * @param config Configuration data to use while reading
     * @throws IOException
     */
    public StegoInputStream(BufferedImage image, StegoConfig config) throws IOException
    {
        if(image == null)
        {
            throw new IllegalArgumentException();
        }

        if(image.getColorModel() instanceof java.awt.image.IndexColorModel)
        {
            throw new IllegalArgumentException(LabelUtil.getString("err.image.indexed"));
        }

        this.image = image;
        this.channelBitsUsed = 1;
        this.config = config;

        this.imgWidth = image.getWidth();
        this.imgHeight = image.getHeight();
        readHeader();
    }

    /**
     * Method to read header data from the input stream
     * @throws IOException
     */
    private void readHeader() throws IOException
    {
        DataHeader dataHeader = null;

        dataHeader = new DataHeader(this, config);
        this.channelBitsUsed = dataHeader.getChannelBitsUsed();
        this.dataLength = dataHeader.getDataLength();

        if(currBit != 0)
        {
            currBit = 0;
            x++;
            if(x == imgWidth)
            {
                x = 0;
                y++;
            }
        }
    }

    /**
     * Implementation of <code>InputStream.read()</code> method
     * @throws IOException
     */
    public int read() throws IOException
    {
        int pixel = 0;
        byte[] bitSet = new byte[8];

        if(y == imgHeight)
        {
            return -1;
        }

        for(int i = 0; i < bitSet.length; i++)
        {
            pixel = image.getRGB(x, y);
            bitSet[i] = getCurrBitFromPixel(pixel);

            currBit++;
            if(currBit == (3 * channelBitsUsed))
            {
                currBit = 0;
                x++;
                if(x == imgWidth)
                {
                    x = 0;
                    y++;
                }
            }
        }
        return ((bitSet[0] << 7) + (bitSet[1] << 6) + (bitSet[2] << 5) + (bitSet[3] << 4) + (bitSet[4] << 3)
                + (bitSet[5] << 2) + (bitSet[6] << 1) + (bitSet[7] << 0));
    }

    /**
     * Get method for dataLength
     * @return dataLength
     */
    public int getDataLength()
    {
        return dataLength;
    }

    /**
     * Get Method for channelBitsUsed
     * @return channelBitsUsed
     */
    public int getChannelBitsUsed()
    {
        return channelBitsUsed;
    }

    /**
     * Gets the bit from pixel based on the current bit
     * @param pixel
     * @return Bit
     */
    private byte getCurrBitFromPixel(int pixel)
    {
        int group = 0;
        int groupBit = 0;

        group = currBit / channelBitsUsed;
        groupBit = currBit % channelBitsUsed;

        return (byte) (((pixel >> (16 - (group * 8))) >> (channelBitsUsed - groupBit - 1)) & 1);
    }
}