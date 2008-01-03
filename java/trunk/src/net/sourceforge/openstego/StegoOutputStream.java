/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import net.sourceforge.openstego.util.LabelUtil;

/**
 * OutputStream to embed data into image
 */
public class StegoOutputStream extends OutputStream
{
    /**
     * Output Image data
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
     * Name of the source data file
     */
    private String fileName = null;

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
     * Bit set to store three bits per pixel
     */
    private byte[] bitSet = null;

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
     * @param image Source image into which data will be embedded
     * @param dataLength Length of the data that would be written to the image
     * @param fileName Name of the source data file
     * @param config Configuration data to use while writing
     * @throws OpenStegoException
     */
    public StegoOutputStream(BufferedImage image, int dataLength, String fileName, OpenStegoConfig config) throws OpenStegoException
    {
        if(image == null)
        {
            throw new OpenStegoException(OpenStegoException.NULL_IMAGE_ARGUMENT, null);
        }

        this.dataLength = dataLength;
        this.imgWidth = image.getWidth();
        this.imgHeight = image.getHeight();
        this.config = config;
        this.image = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < imgWidth; x++)
        {
            for(int y = 0; y < imgHeight; y++)
            {
                this.image.setRGB(x, y, image.getRGB(x, y));
            }
        }

        this.channelBitsUsed = 1;
        this.fileName = fileName;
        this.bitSet = new byte[3];
        writeHeader();
    }

    /**
     * Method to write header data to stream
     * @throws OpenStegoException
     */
    private void writeHeader() throws OpenStegoException
    {
        int channelBits = 1;
        int noOfPixels = 0;
        int headerSize = 0;
        DataHeader header = null;

        try
        {
            noOfPixels = imgWidth * imgHeight;
            header = new DataHeader(dataLength, channelBits, fileName, config);
            headerSize = header.getHeaderSize();

            while(true)
            {
                if((noOfPixels * 3 * channelBits) / 8 < (headerSize + dataLength))
                {
                    channelBits++;
                    if(channelBits > config.getMaxBitsUsedPerChannel())
                    {
                        throw new OpenStegoException(OpenStegoException.IMAGE_SIZE_INSUFFICIENT, null);
                    }
                }
                else
                {
                    break;
                }
            }

            // Write header with channelBitsUsed = 1
            write(header.getHeaderData());
            if(currBit != 0)
            {
                currBit = 0;
                writeCurrentBitSet();
                nextPixel();
            }

            this.channelBitsUsed = channelBits;
            this.bitSet = new byte[3 * channelBits];
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ex);
        }
    }

    /**
     * Implementation of <code>OutputStream.write(int)</code> method
     * @param data Byte to be written
     * @throws IOException
     */
    public void write(int data) throws IOException
    {
        for(int bit = 0; bit < 8; bit++)
        {
            bitSet[currBit] = (byte) ((data >> (7 - bit)) & 1);
            currBit++;
            if(currBit == bitSet.length)
            {
                currBit = 0;
                writeCurrentBitSet();
                nextPixel();
            }
        }
    }

    /**
     * Flushes the stream
     * @throws IOException
     */
    public void flush() throws IOException
    {
        writeCurrentBitSet();
    }

    /**
     * Closes the stream
     * @throws IOException
     */
    public void close() throws IOException
    {
        if(currBit != 0)
        {
            for(int i = currBit; i < bitSet.length; i++)
            {
                bitSet[i] = 0;
            }
            currBit = 0;
            writeCurrentBitSet();
            nextPixel();
        }
        super.close();
    }

    /**
     * Get the image containing the embedded data. Ideally, this should be called after the stream is closed.
     * @return Image data
     * @throws OpenStegoException
     */
    public BufferedImage getImage() throws OpenStegoException
    {
        try
        {
            flush();
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ioEx);
        }
        return image;
    }

    /**
     * Method to write current bit set
     * @throws IOException
     */
    private void writeCurrentBitSet() throws IOException
    {
        int pixel = 0;
        int offset = 0;
        int mask = 0;
        int maskPerByte = 0;
        int bitOffset = 0;

        if(y == imgHeight)
        {
            throw new IOException(LabelUtil.getString("err.image.insufficientSize"));
        }

        maskPerByte = (int) (Math.pow(2, channelBitsUsed) - 1);
        mask = (maskPerByte << 16) + (maskPerByte << 8) + maskPerByte;
        pixel = image.getRGB(x, y) & (0xFFFFFFFF - mask);

        for(int bit = 0; bit < 3; bit++)
        {
            bitOffset = 0;
            for(int i = 0; i < channelBitsUsed; i++)
            {
                bitOffset = (bitOffset << 1) + bitSet[(bit * channelBitsUsed) + i];
            }
            offset = (offset << 8) + bitOffset;
        }
        image.setRGB(x, y, pixel + offset);
    }

    /**
     * Method to move on to next pixel
     */
    private void nextPixel()
    {
        x++;
        if(x == imgWidth)
        {
            x = 0;
            y++;
        }
    }
}