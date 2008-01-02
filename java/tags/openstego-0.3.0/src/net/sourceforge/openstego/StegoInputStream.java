/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import net.sourceforge.openstego.util.LabelUtil;

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
     * Data header
     */
    private DataHeader dataHeader = null;

    /**
     * Number of bits used per color channel
     */
    private int channelBitsUsed = 1;

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
    private OpenStegoConfig config = null;

    /**
     * Default constructor
     * @param image Image data to be read
     * @param config Configuration data to use while reading
     * @throws OpenStegoException
     */
    public StegoInputStream(BufferedImage image, OpenStegoConfig config) throws OpenStegoException
    {
        if(image == null)
        {
            throw new OpenStegoException(OpenStegoException.NULL_IMAGE_ARGUMENT, null);
        }

        if(image.getColorModel() instanceof java.awt.image.IndexColorModel)
        {
            throw new OpenStegoException(OpenStegoException.INDEXED_IMAGE_NOT_SUPPORTED, null);
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
     * @throws OpenStegoException
     */
    private void readHeader() throws OpenStegoException
    {
        dataHeader = new DataHeader(this, config);
        this.channelBitsUsed = dataHeader.getChannelBitsUsed();

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
     * Get method for dataHeader
     * @return Data header
     */
    public DataHeader getDataHeader()
    {
        return dataHeader;
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