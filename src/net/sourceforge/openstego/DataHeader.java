/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.sourceforge.openstego.util.LabelUtil;

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
     * Name of the file being embedded in the image (as byte array)
     */
    private byte[] fileName = null;

    /**
     * OpenStegoConfig instance to hold the configuration data
     */
    private OpenStegoConfig config = null;

    /**
     * This constructor should normally be used when writing the data.
     * @param dataLength Length of the data embedded in the image (excluding the header data)
     * @param channelBitsUsed Number of bits used per color channel for embedding the data
     * @param fileName Name of the file of data being embedded
     * @param config OpenStegoConfig instance to hold the configuration data
     */
    public DataHeader(int dataLength, int channelBitsUsed, String fileName, OpenStegoConfig config)
    {
        this.dataLength = dataLength;
        this.channelBitsUsed = channelBitsUsed;
        this.config = config;

        if(fileName == null)
        {
        	this.fileName = new byte[0];
        }
        else
        {
	        try
	        {
	        	this.fileName = fileName.getBytes("UTF-8");
	        }
	        catch(UnsupportedEncodingException unEx)
	        {
	        	this.fileName = fileName.getBytes();
	        }
        }
    }

    /**
     * This constructor should be used when reading embedded data from an InputStream.
     * @param dataInStream Data input stream containing the embedded data
     * @param config OpenStegoConfig instance to hold the configuration data
     * @throws IOException
     */
    public DataHeader(InputStream dataInStream, OpenStegoConfig config) throws IOException
    {
        int stampLen = 0;
        int fileNameLen = 0;
        int channelBits = 0;
        byte[] header = null;
        byte[] stamp = null;

        stampLen = DATA_STAMP.length;
        header = new byte[stampLen + 8];
        stamp = new byte[stampLen];

        dataInStream.read(header, 0, stampLen + 8);
        System.arraycopy(header, 0, stamp, 0, stampLen);

        if(!(new String(stamp)).equals(new String(DATA_STAMP)))
        {
            throw new IOException(LabelUtil.getString("err.invalidHeader"));
        }

        dataLength = (byteToInt(header[stampLen]) + (byteToInt(header[stampLen + 1]) << 8)
                + (byteToInt(header[stampLen + 2]) << 16) + (byteToInt(header[stampLen + 3]) << 32));
        channelBits = header[stampLen + 4];
        fileNameLen = header[stampLen + 5];
        config.setUseCompression(header[stampLen + 6] == 1);
        config.setUseEncryption(header[stampLen + 7] == 1);

        if(fileNameLen == 0)
        {
        	fileName = new byte[0];
        }
        else
        {
        	fileName = new byte[fileNameLen];
        	dataInStream.read(fileName, 0, fileNameLen);
        }

        channelBitsUsed = channelBits;
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
        out = new byte[stampLength + 8 + fileName.length];

        System.arraycopy(DATA_STAMP, 0, out, 0, stampLength);
        out[stampLength + 0] = (byte) ((dataLength & 0x000000FF));
        out[stampLength + 1] = (byte) ((dataLength & 0x0000FF00) >> 8);
        out[stampLength + 2] = (byte) ((dataLength & 0x00FF0000) >> 16);
        out[stampLength + 3] = (byte) ((dataLength & 0xFF000000) >> 32);
        out[stampLength + 4] = (byte) channelBitsUsed;
        out[stampLength + 5] = (byte) fileName.length;
        out[stampLength + 6] = (byte) (config.isUseCompression() ? 1 : 0);
        out[stampLength + 7] = (byte) (config.isUseEncryption() ? 1 : 0);
        if(fileName.length > 0)
        {
        	System.arraycopy(fileName, 0, out, stampLength + 8, fileName.length);
        }

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
     * Get Method for fileName
     * @return fileName
     */
    public String getFileName()
    {
    	String name = null;

    	try
    	{
    		name = new String(fileName, "UTF-8");
    	}
    	catch(UnsupportedEncodingException unEx)
    	{
    		name = new String(fileName);
    	}
        return name;
    }

    /**
     * Method to get size of the current header
     * @return Header size
     */
    public int getHeaderSize()
    {
        return DATA_STAMP.length + 7 + fileName.length;
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