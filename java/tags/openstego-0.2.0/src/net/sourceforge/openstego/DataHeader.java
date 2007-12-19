/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import net.sourceforge.openstego.util.LabelUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class holds the header data for the data that needs to be embedded in the image.
 * First, the header data gets written inside the image, and then the actual data is written.
 */
public class DataHeader
{
    /**
     * Magic string at the start of the header to identify OpenStego embedded data
     */
    public static final byte[] DATA_STAMP = "#$OpenStego$#".getBytes();

    /**
     * Length of the data embedded in the image (excluding the header data)
     */
    private int dataLength = 0;

    /**
     * Number of bits used per color channel for embedding the data
     */
    private int channelBitsUsed = 0;

    /**
     * StegoConfig instance to hold the configuration data
     */
    private StegoConfig config = null;

    /**
     * This constructor should normally be used when writing the data.
     * @param dataLength Length of the data embedded in the image (excluding the header data)
     * @param channelBitsUsed Number of bits used per color channel for embedding the data
     * @param config StegoConfig instance to hold the configuration data
     */
    public DataHeader(int dataLength, int channelBitsUsed, StegoConfig config)
    {
        this.dataLength = dataLength;
        this.channelBitsUsed = channelBitsUsed;
        this.config = config;
    }

    /**
     * This constructor should be used when reading embedded data from an InputStream.
     * @param dataInStream Data input stream containing the embedded data
     * @param config StegoConfig instance to hold the configuration data
     * @throws IOException
     */
    public DataHeader(InputStream dataInStream, StegoConfig config) throws IOException
    {
        int stampLen = 0;
        byte[] header = null;
        byte[] stamp = null;

        stampLen = DATA_STAMP.length;
        header = new byte[stampLen + 6];
        stamp = new byte[stampLen];

        dataInStream.read(header, 0, stampLen + 6);
        System.arraycopy(header, 0, stamp, 0, stampLen);

        if(!(new String(stamp)).equals(new String(DATA_STAMP)))
        {
            throw new IOException(LabelUtil.getString("err.invalidHeader"));
        }

        dataLength = (byteToInt(header[stampLen]) + (byteToInt(header[stampLen + 1]) << 8)
                + (byteToInt(header[stampLen + 2]) << 16) + (byteToInt(header[stampLen + 3]) << 32));

        channelBitsUsed = header[stampLen + 4];
        config.setUseCompression(header[stampLen + 5] == 1);
        this.config = config;
    }

    /**
     * This method generates the header in the form of byte array based on the parameters provided in the constructor.
     * @return Header data
     */
    public byte[] getHeaderData()
    {
        byte[] out = null;
        int stampLength = 0;

        stampLength = DATA_STAMP.length;
        out = new byte[stampLength + 6];

        System.arraycopy(DATA_STAMP, 0, out, 0, stampLength);
        out[stampLength + 0] = (byte) ((dataLength & 0x000000FF));
        out[stampLength + 1] = (byte) ((dataLength & 0x0000FF00) >> 8);
        out[stampLength + 2] = (byte) ((dataLength & 0x00FF0000) >> 16);
        out[stampLength + 3] = (byte) ((dataLength & 0xFF000000) >> 32);
        out[stampLength + 4] = (byte) channelBitsUsed;
        out[stampLength + 5] = (byte) (config.isUseCompression() ? 1 : 0);

        return out;
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
     * Get Method for dataLength
     * @return dataLength
     */
    public int getDataLength()
    {
        return dataLength;
    }

    /**
     * Method to get standard header size
     * @return Header size
     */
    public static int getHeaderSize()
    {
        return DATA_STAMP.length + 6;
    }

    /**
     * Byte to Int converter
     * @param b
     * @return
     */
    private int byteToInt(int b)
    {
        int i = (int) b;
        if(i < 0)
        {
            i = i + 256;
        }
        return i;
    }
}