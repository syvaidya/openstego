/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class to hold the image data header data
 */
public class DataHeader
{
    public static final byte[] DATA_STAMP = "#$OpenStego$#".getBytes();

    private int dataLength = 0;

    private int channelBitsUsed = 0;

    private StegoConfig config = null;

    /**
     * Constructor
     * @param dataLength
     * @param channelBitsUsed
     * @param config
     */
    public DataHeader(int dataLength, int channelBitsUsed, StegoConfig config)
    {
        this.dataLength = dataLength;
        this.channelBitsUsed = channelBitsUsed;
        this.config = config;
    }

    /**
     * Constructor using data input stream
     * @param dataInStream Data input stream
     * @param config Stego configuration data
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
            throw new IOException("Wrong Header: Image does not contain embedded data");
        }

        dataLength = (byteToInt(header[stampLen]) + (byteToInt(header[stampLen + 1]) << 8)
                + (byteToInt(header[stampLen + 2]) << 16) + (byteToInt(header[stampLen + 3]) << 32));

        channelBitsUsed = header[stampLen + 4];
        config.setUseCompression(header[stampLen + 5] == 1);
        this.config = config;
    }

    /**
     * Create header data for data
     * @param dataLength Length of data
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